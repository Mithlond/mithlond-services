/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.transport.user;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDate;

/**
 * SimpleTransportable version of a User.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE, propOrder = {"firstName", "lastName", "birthday"})
@XmlAccessorType(XmlAccessType.FIELD)
public class UserVO extends AbstractSimpleTransportable {

    /**
     * The first name of this User.
     */
    @XmlElement(required = true)
    private String firstName;

    /**
     * The last name of this User.
     */
    @XmlElement(required = true)
    private String lastName;

    /**
     * The birthday of this User.
     */
    @XmlElement(required = true)
    private LocalDate birthday;

    /**
     * JAXB-friendly constructor.
     */
    public UserVO() {
    }

    /**
     * Compound constructor creating a UserVO wrapping the supplied data.
     *
     * @param jpaID     The JPA ID for the entity corresponding to this {@link AbstractSimpleTransportable}.
     *                  Use {@code null} to indicate that this {@link AbstractSimpleTransportable} does not
     *                  correspond to a (known) Entity within the database.
     * @param firstName The first name of this User.
     * @param lastName  The last name of this User.
     * @param birthday  The birthday of this User.
     */
    public UserVO(final Long jpaID, final String firstName, final String lastName, final LocalDate birthday) {
        super(jpaID);
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
    }

    /**
     * Copy constructor creating a {@link UserVO} corresponding to the supplied {@link User}.
     *
     * @param user A non-null {@link User}.
     */
    public UserVO(final User user) {

        // Check sanity
        Validate.notNull(user, "user");

        // Assign internal state
        this.firstName = Validate.notEmpty(user.getFirstName(), "firstName");
        this.lastName = Validate.notEmpty(user.getLastName(), "lastName");
        this.birthday = Validate.notNull(user.getBirthday(), "birthday");
    }

    /**
     * @return The first name of this User.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return The last name of this User.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return The birthday of this User.
     */
    public LocalDate getBirthday() {
        return birthday;
    }
}
