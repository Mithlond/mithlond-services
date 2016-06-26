package se.mithlond.services.shared.spi.algorithms.introspector;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SimplePropertyHolder {

    private String stringProperty;
    private Long longProperty;
    private Integer intProperty;

    public SimplePropertyHolder() {
    }

    public SimplePropertyHolder(final String stringProperty, final Long longProperty, final Integer intProperty) {
        this.stringProperty = stringProperty;
        this.longProperty = longProperty;
        this.intProperty = intProperty;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(final String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public Long getLongProperty() {
        return longProperty;
    }

    public void setLongProperty(final Long longProperty) {
        this.longProperty = longProperty;
    }

    public Integer getIntProperty() {
        return intProperty;
    }

    public void setIntProperty(final Integer intProperty) {
        this.intProperty = intProperty;
    }
}
