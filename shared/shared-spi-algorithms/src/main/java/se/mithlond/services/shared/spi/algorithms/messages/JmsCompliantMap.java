/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.shared.spi.algorithms.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Map implementation which validates that keys and values supplied are in compliance with the JMS specification.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public class JmsCompliantMap extends TreeMap<String, Object> {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(JmsCompliantMap.class);

    /**
     * Accepted DataPumpMessage property value types.
     */
    public static final List<Class<?>> ACCEPTED_PROPERTY_TYPES = Arrays.asList(Boolean.class, Boolean.TYPE,
            Byte.class, Byte.TYPE, Short.class, Short.TYPE, Character.class, Character.TYPE,
            Integer.class, Integer.TYPE, Long.class, Long.TYPE, Double.class, Double.TYPE, String.class);

    /**
     * <p>Validates that the value is one of the accepted types, before putting the key/value pair into this Map.</p>
     * {@inheritDoc}
     */
    @Override
    public Object put(final String key, final Object value) {

        // Do not permit nulls.
        if (value == null) {
            return null;
        }

        final String valueType = value.getClass().getName();
        final boolean isAcceptedValueType = ACCEPTED_PROPERTY_TYPES
                .stream()
                .anyMatch(current -> value.getClass() == current);

        if (!isAcceptedValueType) {
            throw new IllegalArgumentException("JMS specification does not permit adding ["
                    + valueType + "] values. Accepted types: "
                    + ACCEPTED_PROPERTY_TYPES.stream().map(Class::getName)
                    .reduce((left, right) -> left + ", " + right));
        }

        // All done.
        return super.put(key, value);
    }

    /**
     * Indicates if the supplied value object is compliant with the JMS specification's definition
     * for JMS property values.
     *
     * @param value The value to check for JMS type compliance.
     * @return {@code true} if the value is compliant with the JMS specification's permitted types for JMS property
     * values.
     */
    public static boolean isCompliantValue(final Object value) {

        // We dont accept nulls.
        if (value == null) {
            return false;
        }

        // All Done.
        return ACCEPTED_PROPERTY_TYPES
                .stream()
                .anyMatch(current -> value.getClass() == current);
    }

    /**
     * Extracts a JmsCompliantMap containing all (relevant) JMS Properties within the supplied jmsMessage.
     *
     * @param jmsMessage A non-null JMS Message from which all JMS properties should be extracted.
     * @return a JmsCompliantMap containing all properties found within the supplied Message.
     */
    public static JmsCompliantMap getPropertyMap(final Message jmsMessage) {

        // Check sanity
        Validate.notNull(jmsMessage, "jmsMessage");

        final JmsCompliantMap toReturn = new JmsCompliantMap();

        try {

            // Extract all non-standard JMS headers
            final List<String> nonDefaultHeaderNames = Collections.list(jmsMessage.getPropertyNames());
            for (String current : nonDefaultHeaderNames) {
                toReturn.put(current, jmsMessage.getObjectProperty(current));
            }

            // Copy default JMS properties by firing all Getters within the Message class.
            for (PropertyDescriptor pd : Introspector.getBeanInfo(Message.class).getPropertyDescriptors()) {
                if (pd.getReadMethod() != null && !"class".equalsIgnoreCase(pd.getName())) {

                    final Method getter = pd.getReadMethod();
                    final Object result = getter.invoke(jmsMessage);

                    // Add the property only if it is not null and compliant with the JMS specification.
                    if (JmsCompliantMap.isCompliantValue(result)) {
                        toReturn.put(pd.getName(), result);
                    }
                }
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Could not extract properties from JMS message", e);
        }

        // All Done.
        return toReturn;
    }

    /**
     * Copies all properties from the supplied JmsCompliantMap to the given target Message.
     *
     * @param target     A non-null JMS outbound Message (i.e. created by the JMSContext for sending).
     * @param properties The non-null JmsCompliantMap containing the JMS properties to copy.
     */
    public static void copyProperties(@NotNull final Message target,
                                      @NotNull final JmsCompliantMap properties) {

        // Check sanity
        Validate.notNull(target, "target");
        Validate.notNull(properties, "properties");

        // Set all available properties as JMS Object Properties.
        properties.forEach((key, value) -> {

            final String valueType = value == null ? "<null>" : value.getClass().getName();

            try {
                target.setObjectProperty(key, value);
            } catch (JMSException e) {
                log.warn("Could not set a JMS Property [" + key + "] of type [" + valueType + "]. Ignoring it.", e);
            }
        });

        // Copy default JMS properties by firing all Setters within
        // the Message class where we have a non-null property.
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(Message.class).getPropertyDescriptors()) {
                if (pd.getWriteMethod() != null && !"class".equalsIgnoreCase(pd.getName())) {

                    final Object value = properties.get(pd.getName());
                    if (value != null) {

                        // Only attempt to set a non-null value
                        final Method setter = pd.getWriteMethod();
                        try {
                            setter.invoke(target, value);
                        } catch (Exception e) {
                            log.warn("Could not assign value [" + value + "] to Message using setter ["
                                    + setter.getName() + "]", e);
                        }
                    }
                }
            }
        } catch (IntrospectionException e) {
            log.error("Could not perform Introspection of Message class", e);
        }
    }
}
