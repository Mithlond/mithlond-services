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
package se.mithlond.services.organisation.model.transport.food;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.food.Allergy;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
import se.mithlond.services.organisation.model.transport.AbstractLocalizedSimpleTransporter;
import se.mithlond.services.organisation.model.transport.membership.MembershipVO;
import se.mithlond.services.organisation.model.transport.user.UserVO;
import se.mithlond.services.organisation.model.user.User;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Transport wrapper for Allergy-related VOs.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"users", "memberships", "allergyList"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Allergies extends AbstractLocalizedSimpleTransporter {

    /**
     * A List of all UserVOs for which the AllergyVOs within the allergyList pertains.
     */
    @XmlElementWrapper
    @XmlElement(name = "user")
    private List<UserVO> users;

    /**
     * An optional List of all MembershipVOs for UserVOs with supplied AllergyVOs
     * within the allergyList.
     */
    @XmlElementWrapper
    @XmlElement(name = "membership")
    private List<MembershipVO> memberships;

    /**
     * The List of AllergyVOs describing the allergies for the supplied Users.
     */
    @XmlElementWrapper
    @XmlElement(name = "allergy")
    private List<AllergyVO> allergyList;

    /**
     * JAXB-friendly constructor.
     */
    public Allergies() {
        this.users = new ArrayList<>();
        this.memberships = new ArrayList<>();
        this.allergyList = new ArrayList<>();
    }

    /**
     * Compound constructor creating an Allergies transport wrapping the supplied data.
     *
     * @param localeDefinition A non-null LocaleDefinition used throughout this Allergies transporter.
     * @param users            The List of UserVOs to wrap within this Allergies transporter.
     * @param allergyList      The List of AllergyVOs to wrap within this Allergies transporter.
     */
    public Allergies(final LocaleDefinition localeDefinition,
                     final List<UserVO> users,
                     final List<MembershipVO> memberships,
                     final List<AllergyVO> allergyList) {

        // Delegate
        this();
        initialize(localeDefinition);

        // Assign internal state
        if (users != null) {
            add(users.toArray(new UserVO[users.size()]));
        }
        if (memberships != null) {
            add(memberships.toArray(new MembershipVO[memberships.size()]));
        }
        if (allergyList != null) {
            add(allergyList.toArray(new AllergyVO[allergyList.size()]));
        }
    }

    /**
     * Creates an allergies transport wrapper containing the supplied Allergies.
     *
     * @param localeDefinition A non-null LocaleDefinition used throughout this Allergies transporter.
     * @param allergies        The Allergy Entities to add to this Allergies transporter.
     */
    public Allergies(final LocaleDefinition localeDefinition, final Allergy... allergies) {

        // Delegate
        this();

        // Assign internal state
        initialize(localeDefinition);
        add(allergies);
    }

    /**
     * Adds the supplied MembershipVO to this Allergies transport, provided they are not null or already added.
     *
     * @param memberships The MembershipVOs to add.
     */
    public void add(final MembershipVO... memberships) {

        if (memberships != null) {
            Stream.of(memberships)
                    .filter(Objects::nonNull)
                    .forEach(m -> {

                        // Add the MembershipVO, if missing from the current internal state
                        if (!this.memberships.contains(m)) {
                            this.memberships.add(m);
                        }
                    });
        }
    }

    /**
     * Adds the supplied UserVOs to this Allergies transport, provided they are not null or already added.
     *
     * @param users The UserVOs to add.
     */
    public void add(final UserVO... users) {

        if (users != null) {

            Stream.of(users)
                    .filter(Objects::nonNull)
                    .forEach(user -> {

                        // Add the UserVOs, if missing from the current internal state
                        if (!this.users.contains(user)) {
                            this.users.add(user);
                        }
                    });
        }
    }

    /**
     * Adds the supplied AllergyVOs to this Allergies transport, provided they are not null or already added.
     *
     * @param allergyVOs The AllergyVOs to add.
     */
    public void add(final AllergyVO... allergyVOs) {

        if (allergyVOs != null) {

            Stream.of(allergyVOs)
                    .filter(Objects::nonNull)
                    .forEach(allergyVO -> {

                        if (!allergyList.contains(allergyVO)) {
                            allergyList.add(allergyVO);
                        }
                    });
        }
    }

    /**
     * Adds the supplied AllergyVOs to this Allergies transport, provided they are not null or already added.
     *
     * @param allergies The Allergies to convert to shallow representation and add to this Allergies transport.
     */
    public void add(final Allergy... allergies) {

        if (allergies != null) {

            Stream.of(allergies)
                    .filter(Objects::nonNull)
                    .forEach(allergy -> {

                        // Extract the shallow-state Allergy data
                        final User user = allergy.getUser();
                        final UserVO userVO = new UserVO(user);
                        final AllergyVO allergyVO = new AllergyVO(allergy, getLocaleDefinition());

                        // Add the UserVOs, if missing from the current internal state
                        add(userVO);

                        // Add the AllergyVO, if missing from the current internal state
                        add(allergyVO);
                    });
        }
    }

    /**
     * @return The List of UserVOs for which the transported AllergyVOs are valid.
     */
    public List<UserVO> getUsers() {
        return users;
    }

    /**
     * @return The List of shallow AllergyVOs transported.
     */
    public List<AllergyVO> getAllergyList() {
        return allergyList;
    }

    /**
     * @return The List of shallow MembershipVOs transported.
     */
    public List<MembershipVO> getMemberships() {
        return memberships;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + " with "
                + users.size() + " users, "
                + memberships.size() + " memberships and "
                + allergyList.size() + " allergies.";
    }
}
