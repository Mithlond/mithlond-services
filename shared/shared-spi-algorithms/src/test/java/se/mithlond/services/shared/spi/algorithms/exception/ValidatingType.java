package se.mithlond.services.shared.spi.algorithms.exception;


import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ValidatingType {

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @Min(18)
    private int age;

    public ValidatingType(final String firstName, final String lastName, final int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }
}
