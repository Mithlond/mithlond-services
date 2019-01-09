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

import java.util.Comparator
import java.util.Locale
import java.util.SortedSet
import java.util.TreeSet

/**
 * Specification for a Localized comparable.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface LocalizedComparable<T> {

    /**
     * Performs a Locale-sensitive comparison.
     */
    fun compareTo(locale: Locale?, other: T): Int

    /**
     * Retrieves a Comparator performing a locale-aware comparison.
     *
     * @return A [Comparator] for a locale-sensitive comparison.
     */
    fun getComparator(locale: Locale?): Comparator<T>

    /**
     * @return a SortedSet using the [getFoodComparator] as Comparator for the supplied Locale.
     */
    fun mutableSortedSetOf(locale: Locale): SortedSet<T> = TreeSet<T>(getComparator(locale))
}
