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
import se.mithlond.services.organisation.model.food.FoodPreference;
import se.mithlond.services.organisation.model.transport.AbstractLocalizedSimpleTransporter;
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
import java.util.Locale;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Transport wrapper for Allergy-related VOs.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"users", "allergyList", "foodPreferences" })
@XmlAccessorType(XmlAccessType.FIELD)
public class Allergies extends AbstractLocalizedSimpleTransporter {

    /**
     * A List of all UserVOs for which the AllergyVOs within the allergyList pertains.
     */
    @XmlElementWrapper
    @XmlElement(name = "user")
    private List<UserVO> users;

    /**
     * The List of AllergyVOs describing the allergies for the supplied Users.
     */
    @XmlElementWrapper
    @XmlElement(name = "allergy")
    private List<AllergyVO> allergyList;

    /**
     * The sorted set of FoodPreferenceVOs transported.
     */
    @XmlElementWrapper
    @XmlElement(name = "preference")
    private SortedSet<FoodPreferenceVO> foodPreferences;

    /**
     * JAXB-friendly constructor.
     */
    public Allergies() {
        this.users = new ArrayList<>();
        this.allergyList = new ArrayList<>();
        this.foodPreferences = new TreeSet<>();
    }

    /**
     * Compound constructor creating an Allergies transport wrapping the supplied data.
     *
     * @param locale      A non-null Locale used throughout this Allergies transporter.
     * @param users       The List of UserVOs to wrap within this Allergies transporter.
     * @param allergyList The List of AllergyVOs to wrap within this Allergies transporter.
     */
    public Allergies(final Locale locale,
                     final List<UserVO> users,
                     final List<AllergyVO> allergyList,
                     final SortedSet<FoodPreferenceVO> foodPreferences) {

        // Delegate
        this();
        initialize(locale);

        // Assign internal state
        if (users != null) {
            add(users.toArray(new UserVO[users.size()]));
        }
        if (allergyList != null) {
            add(allergyList.toArray(new AllergyVO[allergyList.size()]));
        }
        if (foodPreferences != null) {
            foodPreferences.stream()
                    .filter(f -> !this.foodPreferences.contains(f))
                    .forEach(f -> this.foodPreferences.add(f));
        }
    }

    /**
     * Creates an allergies transport wrapper containing the supplied Allergies.
     *
     * @param locale    A non-null Locale used throughout this Allergies transporter.
     * @param allergies The Allergy Entities to add to this Allergies transporter.
     */
    public Allergies(final Locale locale, final Allergy... allergies) {

        // Delegate
        this();

        // Assign internal state
        initialize(locale);
        add(allergies);
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
                        final AllergyVO allergyVO = new AllergyVO(allergy, getLocale());

                        // Add the UserVOs, if missing from the current internal state
                        add(userVO);

                        // Add the AllergyVO, if missing from the current internal state
                        add(allergyVO);
                    });
        }
    }

    /**
     * Adds the supplied FoodPreferences to this Allergies transporter.
     *
     * @param prefs The FoodPreferences to add.
     */
    public void add(final FoodPreference... prefs) {

        if (prefs != null) {
            Stream.of(prefs)
                    .filter(Objects::nonNull)
                    .map(FoodPreferenceVO::new)
                    .filter(f -> !this.foodPreferences.contains(f))
                    .forEach(this.foodPreferences::add);
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
     * @return The sorted set of FoodPreferenceVOs transported.
     */
    public SortedSet<FoodPreferenceVO> getFoodPreferences() {
        return foodPreferences;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + " with "
                + users.size() + " users, "
                + allergyList.size() + " allergies and "
                + foodPreferences.size() + " food preferences.";
    }
}
