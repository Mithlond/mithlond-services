/*
 * #%L
 * Nazgul Project: mithlond-services-shared-entity-test
 * %%
 * Copyright (C) 2015 Mithlond
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.mithlond.services.shared.test.entity;

import org.junit.rules.TestWatcher;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.validation.constraints.Null;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * <p>jUnit rule simplifying working with JAXB marshalling and unmarshalling during tests.
 * Typically, this rule is invoked in 3 steps for marshalling and 2 steps for unmarshalling.</p>
 * <h2>Marshalling Objects to XML Strings</h2>
 * <ol>
 *     <li>Call <code>add(class1, class2, ...);</code> to add any classes that should be bound into the
 *     JAXBContext for marshalling or unmarshalling.</li>
 *     <li>(Optional): Call <code>mapXmlNamespacePrefix(anXmlURI, marshalledXmlPrefix)</code>
 *     to control the XML namespace prefix in the marshalled structure.</li>
 *     <li>Call <code>marshal(classLoader, anObject)</code> to marshal the objects into XML</li>
 * </ol>
 * <h2>Unmarshalling Objects from XML Strings</h2>
 * <ol>
 *     <li>Call <code>add(class1, class2, ...);</code> to add any classes that should be bound into the
 *     JAXBContext for marshalling or unmarshalling.</li>
 *     <li>(Optional): Call <code>mapXmlNamespacePrefix(anXmlURI, marshalledXmlPrefix)</code>
 *     to control the XML namespace prefix in the marshalled structure.</li>
 *     <li>Call <code>unmarshal(classLoader, ResultClass.class, xmlString);</code> to unmarshal the XML String into
 *     Java Objects.</li>
 * </ol>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PlainJaxbContextRule extends TestWatcher {

    private static final Comparator<Class<?>> CLASS_COMPARATOR = (class1, class2) -> {

        // Deal with nulls.
        final String className1 = class1 == null ? "" : class1.getName();
        final String className2 = class2 == null ? "" : class2.getName();

        // All done
        return className1.compareTo(className2);
    };

    // Internal state
    private SortedSet<Class<?>> jaxbAnnotatedClasses;
    private JAXBContext jaxbContext;
    private JaxbNamespacePrefixResolver namespacePrefixResolver;

    /**
     * Default constructor, setting up a clean internal state.
     */
    public PlainJaxbContextRule() {
        this.jaxbAnnotatedClasses = new TreeSet<>(CLASS_COMPARATOR);
        this.namespacePrefixResolver = new JaxbNamespacePrefixResolver();
    }

    /**
     * <p>Adds a set of classes to be used within the JAXBContext for marshalling or unmarshalling.
     * Normally, these classes would need to be annotated with JAXB annotations, and would be injected into the
     * construction of the JAXBContext normally:</p>
     * <pre>
     *     <code>
     *         // Create an array of all added classes.
     *         final Class[] classesToBeBound = ... all add-ed Classes ...
     *
     *         // Create the JAXBContext containing/binding all added classes.
     *         JAXBContext jaxbContext = JAXBContext.newInstance(classesToBeBound);
     *     </code>
     * </pre>
     *
     * @param jaxbAnnotatedClasses The classes to add to any JAXBContext used within this PlainJaxbContextRule, for
     *                             marshalling or unmarshalling. The supplied classes are given in
     */
    public void add(final Class<?>... jaxbAnnotatedClasses) {
        if (jaxbAnnotatedClasses != null) {
            Collections.addAll(this.jaxbAnnotatedClasses, jaxbAnnotatedClasses);
        }
    }

    /**
     * Marshals the supplied objects into an XML String, or throws an IllegalArgumentException
     * containing a wrapped JAXBException indicating why the marshalling was unsuccessful.
     *
     * @param loader  The ClassLoader to use in order to load all classes previously added
     *                by calls to the {@code add} method.
     * @param objects The objects to Marshal into XML.
     * @return An XML-formatted String containing
     * @throws IllegalArgumentException if the marshalling operation failed.
     *                                  The {@code cause} field in the IllegalArgumentException contains
     *                                  the JAXBException thrown by the JAXB framework.
     * @see #add(Class[])
     */
    public String marshal(final ClassLoader loader, final Object... objects) throws IllegalArgumentException {

        // Create an EntityTransporter, to extract the
        // types as required by the plain JAXBContext.
        final EntityTransporter<Object> transporter = new EntityTransporter<>();
        for (Object current : objects) {
            transporter.addItem(current);
        }

        // Extract class info as required by the JAXBContext.
        final SortedSet<String> clsInfo = transporter.getClassInformation();
        try {
            jaxbContext = JAXBContext.newInstance(getClasses(loader, clsInfo));
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Could not create JAXB context.", e);
        }

        final Marshaller marshaller = JaxbUtils.getHumanReadableStandardMarshaller(
                jaxbContext,
                namespacePrefixResolver,
                true);

        final StringWriter result = new StringWriter();
        for (int i = 0; i < objects.length; i++) {
            final StringWriter tmp = new StringWriter();
            try {
                marshaller.marshal(objects[i], tmp);
                result.write(tmp.toString());
            } catch (JAXBException e) {
                final String currentTypeName = objects[i] == null ? "<null>" : objects[i].getClass().getName();
                throw new IllegalArgumentException("Could not marshal object [" + i + "] of type ["
                        + currentTypeName + "].", e);
            } catch(Exception e) {
                throw new IllegalArgumentException("Could not marshal object [" + i + "]: " + objects[i], e);
            }
        }

        // All done.
        return result.toString();
    }

    /**
     * <p>Unmarshals the supplied xmlToUnmarshal into a result of the supplied type, using the given
     * ClassLoader to load all relevant types into the JAXBContext. Typical unarshalling use case involves
     * 2 calls on this PlainJaxbContextRule:</p>
     * <pre>
     *     <code>
     *         // 1) add all types you expect to unmarshal
     *         rule.add(Foo.class, Bar.class, Gnat.class);
     *
     *         // 2) unmarshal your XML string
     *         Foo unmarshalled = rule.unmarshal(getClass().getClassLoader(), Foo.class, aFooXml);
     *     </code>
     * </pre>
     *
     * @param loader         The ClassLoader to use in order to load all classes previously added
     *                       by calls to the {@code add} method.
     * @param resultType     The type of the resulting object.
     * @param xmlToUnmarshal The XML string to unmarshal into a T object.
     * @param <T>            The expected type to unmarshal into.
     * @return The resulting, unmarshalled object.
     * @see #add(Class[])
     */
    public <T> T unmarshal(final ClassLoader loader, final Class<T> resultType, final String xmlToUnmarshal) {

        // Check sanity
        Validate.notNull(resultType, "resultType");
        Validate.notEmpty(xmlToUnmarshal, "xmlToUnmarshal");

        final Source source = new StreamSource(new StringReader(xmlToUnmarshal));

        try {
            jaxbContext = JAXBContext.newInstance(getClasses(loader, null));
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Could not create JAXB context.", e);
        }

        try {
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(source, resultType).getValue();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Could not unmarshal xml into [" + resultType.getName() + "]", e);
        }
    }

    /**
     * Unmarshals without type information resulting in an Object, when no resulting type information
     * has been (or can be) given.
     *
     * @param loader         The ClassLoader to use in order to load all classes previously added
     *                       by calls to the {@code add} method.
     * @param xmlToUnmarshal The XML string to unmarshal into a T object.
     * @return The resulting, unmarshalled object.
     * @see #unmarshal(ClassLoader, Class, String)
     */
    public Object unmarshal(final ClassLoader loader, final String xmlToUnmarshal) {
        return unmarshal(loader, Object.class, xmlToUnmarshal);
    }

    /**
     * Maps an XML URI to a given XML namespace prefix, to yield a better/more user-friendly
     * marshalling of an XML structure.
     *
     * @param uri       The XML URI to map, such as "http://jguru.se/some/url" or "urn:mithlond:data".
     * @param xmlPrefix The XML prefix to use when marshalling types using the uri for namespace.
     */
    public void mapXmlNamespacePrefix(final String uri, final String xmlPrefix) {
        this.namespacePrefixResolver.put(uri, xmlPrefix);
    }

    //
    // Private helpers
    //

    private <C extends Collection<String>> Class[] getClasses(final ClassLoader loader, final C input) {

        final ClassLoader effectiveClassLoader = loader == null ? PlainJaxbContextRule.class.getClassLoader() : loader;
        final SortedMap<String, Class<?>> name2ClassMap = new TreeMap<>();

        if (input != null) {
            for (String current : input) {
                try {
                    final Class<?> aClass = effectiveClassLoader.loadClass(current);
                    if (aClass != null) {
                        name2ClassMap.put(aClass.getName(), aClass);
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Could not load class for [" + current + "]", e);
                }
            }
        }

        // Add any explicitly added classes to the JAXBContext
        for (Class<?> current : this.jaxbAnnotatedClasses) {
            name2ClassMap.put(current.getName(), current);
        }

        // All done.
        return name2ClassMap.values().toArray(new Class<?>[name2ClassMap.size()]);
    }
}
