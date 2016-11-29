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
import se.mithlond.services.organisation.model.transport.user.UserVO;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Transport wrapper for Allergy-related VOs.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"users", "allergyList"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Allergies extends AbstractSimpleTransporter {

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
     * JAXB-friendly constructor.
     */
    public Allergies() {
        this.users = new ArrayList<>();
        this.allergyList = new ArrayList<>();
    }

    /**
     * Compound constructor creating an Allergies transport wrapping the supplied objects.
     *
     * @param users       The List of UserVOs for which the transported AllergyVOs are valid.
     * @param allergyList The List of shallow AllergyVOs transported.
     */
    public Allergies(final List<UserVO> users, final List<AllergyVO> allergyList) {

        // First, delegate.
        this();

        if (users != null) {
            this.users.addAll(users);
        }

        if (allergyList != null) {
            this.allergyList.addAll(allergyList);
        }
    }

    /**
     * Creates an allergies transport wrapper containing the supplied Allergies.
     *
     * @param allergies an array of Allergy objects.
     */
    public Allergies(final LocaleDefinition localeDefinition, final Allergy... allergies) {

        // Delegate
        this();

        // Populate this object.
        if (allergies != null) {
            Arrays.stream(allergies)
                    .filter(Objects::nonNull)
                    .forEach(allergy -> {

                        // Add the current User?
                        final UserVO userVO = new UserVO(allergy.getUser());
                        if(!this.users.contains(userVO)) {
                            this.users.add(userVO);
                        }

                        // Add the current allergy?
                        final AllergyVO allergyVO = new AllergyVO(allergy, localeDefinition);
                        if(!this.allergyList.contains(allergyVO)) {
                            this.allergyList.add(allergyVO);
                        }
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
}
