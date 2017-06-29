/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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
package se.mithlond.services.organisation.model.localization;

import se.jguru.nazgul.core.algorithms.api.Validate;
import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Locale;

/**
 * Parent-child relation between {@link LocalizedText} - {@link LocaleDefinition} - {@link LocalizedTexts}
 * (parent/suite).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
@XmlTransient
@XmlType(namespace = OrganisationPatterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalizedTextId implements Serializable {

    // Shared state
    @Column(name = "suite_id")
    public long localizedTextsSuiteId;

    @Column(name = "locale")
    public String languageTag;

    @Column
    public String classifier;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public LocalizedTextId() {
    }

    /**
     * Compound constructor creating a LocalizedTextId object wrapping the supplied data/keys.
     *
     * @param localizedTextsSuiteId The JPA ID of the {@link LocalizedTexts} parent object.
     * @param locale                The {@link LocaleDefinition} for which this {@link LocalizedTextId} pertains.
     * @param classifier            The non-empty classifier of this {@link LocalizedTextId}.
     */
    public LocalizedTextId(final long localizedTextsSuiteId, final Locale locale, final String classifier) {

        // Simply assign it already.
        this.localizedTextsSuiteId = localizedTextsSuiteId;
        this.languageTag = locale.toLanguageTag();
        this.classifier = Validate.notEmpty(classifier, "classifier");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof LocalizedTextId) {

            final LocalizedTextId that = (LocalizedTextId) obj;

            final String thisClassifier = this.classifier == null ? "" : this.classifier;
            final String thatClassifier = that.classifier == null ? "" : that.classifier;

            return this.languageTag.equals(that.languageTag)
                    && this.localizedTextsSuiteId == that.localizedTextsSuiteId
                    && thisClassifier.equals(thatClassifier);
        }

        // All done.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) (this.languageTag.hashCode() + this.localizedTextsSuiteId + this.classifier.hashCode())
                % Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "LocalizedTextId [LanguageTag: " + languageTag
                + ", LocalizedTextsID: " + localizedTextsSuiteId
                + ", Classifier: " + classifier + "]";
    }
}
