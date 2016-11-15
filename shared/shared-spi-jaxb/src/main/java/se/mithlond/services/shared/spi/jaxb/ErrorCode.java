package se.mithlond.services.shared.spi.jaxb;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Enumeration of the possible error codes available for {@link AbstractSimpleTransporter}s.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public enum ErrorCode {

    UNAUTHORIZED(401),

    INTERNAL_SERVER_ERROR(500),

    VALUE_NOT_FOUND(1023);

    // Internal state
    private int code;

    /**
     * Compound constructor creating an ErrorCode instance wrapping the supplied data.
     *
     * @param code The integer value of this ErrorCode.
     */
    ErrorCode(final int code) {
        this.code = code;
    }

    /**
     * Retrieves the integer code value of this ErrorCode.
     *
     * @return the integer code value of this ErrorCode.
     */
    public int getCode() {
        return code;
    }
}
