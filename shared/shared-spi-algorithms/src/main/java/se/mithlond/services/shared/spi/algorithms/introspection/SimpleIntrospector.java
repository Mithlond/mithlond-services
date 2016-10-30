/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
package se.mithlond.services.shared.spi.algorithms.introspection;

import se.mithlond.services.shared.spi.algorithms.Validate;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Simple reflective utility used to introspect and copy values between objects.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class SimpleIntrospector {

    /**
     * Predicate which accepts JavaBean getter methods.
     */
    public static final Predicate<Method> IS_GETTER_METHOD = m -> m.getName().startsWith("get")
            && m.getParameterCount() == 0
            && !(Void.TYPE.equals(m.getReturnType()) || Void.class.equals(m.getReturnType()))
            && !Modifier.isPrivate(m.getModifiers());

    /**
     * Predicate which accepts JavaBean setter methods.
     */
    public static final Predicate<Method> IS_SETTER_METHOD = m -> m.getName().startsWith("set")
            && m.getParameterCount() == 1
            && (Void.TYPE.equals(m.getReturnType()) || Void.class.equals(m.getReturnType()))
            && !Modifier.isPrivate(m.getModifiers());

    /**
     * Function which converts a Method name to its corresponding JavaBean property name.
     */
    public static final Function<Method, String> TO_JAVABEAN_PROPERTY_NAME = f -> {

        final String fullMethodName = f.getName();

        if (IS_GETTER_METHOD.test(f) || IS_SETTER_METHOD.test(f)) {

            // Peel off the "get"/"set" part
            final StringBuilder builder = new StringBuilder(fullMethodName.substring(3));

            // Remove initial capitalization.
            builder.setCharAt(0, Character.toLowerCase(builder.charAt(0)));

            // All Done.
            return builder.toString();
        }

        // Nah.
        throw new IllegalArgumentException("Method [" + fullMethodName + "] was neither a javabean setter nor getter.");
    };

    /*
     * Hide constructor for utility classes.
     */
    private SimpleIntrospector() {
        // Do nothing
    }

    /**
     * Copies all JavaBean property values from the from object to the to object, by means of reading values from
     * javaBean getters and setting them using javabean setters.
     *
     * @param from The source from which all javabean properties are read.
     * @param to   The value to which all javabean properties are written.
     */
    public static void copyJavaBeanProperties(final Object from, final Object to) {

        // Check sanity
        Validate.notNull(from, "from");
        Validate.notNull(to, "to");

        // Collect all getter and setter methods into maps.
        final Map<String, Method> name2GetterMap = getJavaBeanPropertyMethodMap(from, IS_GETTER_METHOD);
        final Map<String, Method> name2SetterMap = getJavaBeanPropertyMethodMap(to, IS_SETTER_METHOD);

        // Copy all data
        name2GetterMap.entrySet().stream()
                .filter(c -> name2SetterMap.containsKey(c.getKey()))
                .forEach(c -> {

                    final String propertyName = c.getKey();
                    final Method getterMethod = c.getValue();
                    final Method setterMethod = name2SetterMap.get(propertyName);

                    try {

                        // Ensure that the methods are callable
                        getterMethod.setAccessible(true);
                        setterMethod.setAccessible(true);

                        // Copy the data
                        final Object value = getterMethod.invoke(from);
                        final Object[] valueArray = new Object[]{value};
                        setterMethod.invoke(to, valueArray);

                    } catch (Exception e) {
                        throw new IllegalArgumentException("Could not copy javaBean property [" + propertyName
                                + "] from [" + from.getClass().getName() + "] to [" + to.getClass().getName() + "]", e);
                    }
                });
    }

    //
    // Private helpers
    //

    private static Map<String, Method> getJavaBeanPropertyMethodMap(final Object object,
                                                                    final Predicate<Method> methodPredicate) {

        // Check sanity
        Validate.notNull(object, "object");
        Validate.notNull(methodPredicate, "methodPredicate");

        // All Done.
        return Arrays.stream(object.getClass().getMethods())
                .filter(methodPredicate)
                .collect(Collectors.toMap(TO_JAVABEAN_PROPERTY_NAME, m -> m));
    }
}
