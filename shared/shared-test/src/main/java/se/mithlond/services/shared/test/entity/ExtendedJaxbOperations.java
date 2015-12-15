package se.mithlond.services.shared.test.entity;

/**
 * Specification for a set of extended JAXB operations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ExtendedJaxbOperations {

    /**
     * Marshal all supplied object to an XML String using the standard ClassLoader.
     *
     * @param toMarshal The objects to marshal.
     * @return The resulting XML string.
     */
    String marshalToXML(final Object... toMarshal);

    /**
     * Marshal all supplied object to a JSON String using the standard ClassLoader.
     *
     * @param toMarshal The objects to marshal.
     * @return The resulting JSON string.
     */
    String marshalToJSon(final Object... toMarshal);

    /**
     * Unmarshals the supplied XML string into an object of type T using the standard ClassLoader.
     *
     * @param expectedReturnType The expected return type.
     * @param toUnmarshal        The XML string to unmarshal.
     * @param <T>                The expected return type.
     * @return The resurrected T object.
     */
    <T> T unmarshalFromXML(final Class<T> expectedReturnType, final String toUnmarshal);

    /**
     * Unmarshals the supplied JSON string into an object of type T using the standard ClassLoader.
     *
     * @param expectedReturnType The expected return type.
     * @param toUnmarshal        The JSON string to unmarshal.
     * @param <T>                The expected return type.
     * @return The resurrected T object.
     */
    <T> T unmarshalFromJSON(final Class<T> expectedReturnType, final String toUnmarshal);
}
