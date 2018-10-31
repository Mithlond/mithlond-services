/*-
 * #%L
 * Nazgul Project: mithlond-services-organisation-domain-model
 * %%
 * Copyright (C) 2018 jGuru Europe AB
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
package se.mithlond.services.organisation.domain.model.localization

import java.util.Locale
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.xml.bind.annotation.XmlTransient

/**
 * A text (snippet) which can be translated to several languages.
 *
 * @param id The JPA ID of this Domain Entity.
 * @param suiteIdentifier A human-readable identifier for the suite of LocalizedTexts. Should typically be in english.
 * @param standardLocale The default Locale to use for this LocalizedTexts instance, used whenever the Locale
 * argument of a getter call is not provided.
 * @param texts The List of ClassifiedLocalizedText objects within this TextSuite.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_suite_identifier", columnNames = ["suiteIdentifier"])
])
data class TextSuite(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_TextSuite")
        @field:SequenceGenerator(schema = "organisations", name = "seq_TextSuite",
                sequenceName = "seq_TextSuite", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @Basic(optional = false)
        @Column(nullable = false)
        var suiteIdentifier: String,

        @ManyToOne(cascade = [CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST])
        @JoinColumn(name = "default_localization_id")
        var standardLocale: LocaleDefinition,

        @OneToMany(mappedBy = "textSuite", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
        @XmlTransient
        val texts: MutableList<ClassifiedLocalizedText> = mutableListOf()

) : Localizable {

    override fun getText(locale: Locale?, classifier: String?): String? {

        // Find the correct ClassifiedLocalizedText
        val target = getOrNull(locale, classifier)

        // All Done.
        return target?.text
    }

    //
    // Private helpers
    //

    private fun getOrNull(locale: Locale?, classifier: String?): ClassifiedLocalizedText? {

        // Ensure we always have sane arguments.
        val localization = LocaleDefinition((locale ?: standardLocale) as Locale)
        val theClassifier = classifier ?: DEFAULT_CLASSIFIER

        // All Done.
        return texts.firstOrNull { it.classifier == theClassifier && it.localeDefinition == localization }
    }
}