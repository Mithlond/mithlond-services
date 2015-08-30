/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 Mithlond
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
package se.mithlond.services.organisation.model;

/**
 * Specification for a suite of types which may contain (and, hence, produce) a Category.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface CategoryProducer {

    /**
     * Retrieves the default Category of this CategoryProducer.
     *
     * @return the default or standard Category of this CategoryProducer.
     */
    Category getCategory();

    /**
     * Generates a Category with the supplied description.
     *
     * @param description The description of the Category retrieved from this CategoryProducer.
     * @return the standard Category of this CategoryProducer, with the supplied description assigned.
     */
    default Category createCategoryWithDescription(final String description) {

        // Get the standard Category
        final Category prototype = getCategory();

        // Return a clone with the supplied description.
        return new Category(prototype.getCategoryID(), prototype.getClassification(), description);
    }
}
