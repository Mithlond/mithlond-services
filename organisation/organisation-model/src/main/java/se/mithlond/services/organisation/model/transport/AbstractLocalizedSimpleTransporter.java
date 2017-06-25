/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.transport;

import se.jguru.nazgul.core.algorithms.api.Validate;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Locale;

/**
 * AbstractSimpleTransporter implementation which also sports a LocaleDefinition.
 * This is typically relevant to simplify all names / descriptions / designations of
 * VOs to use a single Locale (i.e. Language/Country/Flavour) within this AbstractLocalizedSimpleTransporter.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"locale"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractLocalizedSimpleTransporter extends AbstractSimpleTransporter {

    /**
     * The LocaleDefinition used for all AllergyVOs transported within this Allergies wrapper.
     */
    @XmlElement(required = true)
    private Locale locale;

    /**
     * JAXB-friendly constructor.
     */
    public AbstractLocalizedSimpleTransporter() {
    }

    /**
     * Compound constructor creating a new AbstractLocalizedSimpleTransporter wrapping the supplied locale.
     *
     * @param locale A non-null LocaleDefinition used throughout this AbstractLocalizedSimpleTransporter.
     */
    protected AbstractLocalizedSimpleTransporter(final Locale locale) {

        // Check sanity
        Validate.notNull(locale, "locale");

        // Assign internal state
        this.locale = locale;
    }

    /**
     * Lazy initialization method for this AbstractLocalizedSimpleTransporter, which will initialize this
     * AbstractLocalizedSimpleTransporter to use the supplied locale only if the current member
     * LocaleDefinition is null.
     *
     * @param locale The non-null Locale to initialize this AbstractLocalizedSimpleTransporter from.
     * @throws RuntimeException if the supplied Locale is null, empty or not equal to the existing one.
     */
    protected void initialize(final Locale locale) throws RuntimeException {

        // Check sanity
        Validate.notNull(locale, "locale");

        // Assign only if the local definition is null.
        // Complain if the local definition differs from the supplied one.

        if (this.locale == null) {
            this.locale = locale;

        } else if (!this.locale.toLanguageTag().equals(locale.toLanguageTag())) {

            // Refuse to overwrite existing LocaleDefinition
            throw new IllegalArgumentException("Cannot re-initialize Locale from ("
                    + this.locale.toLanguageTag() + ") to (" + locale.toLanguageTag() + ")");
        }
    }

    /**
     * Retrieves the LocaleDefinition used within this Foods transport wrapper.
     *
     * @return The non-null LocaleDefinition used within this Foods transport wrapper.
     */
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + ", Locale: " + locale.toLanguageTag();
    }
}
