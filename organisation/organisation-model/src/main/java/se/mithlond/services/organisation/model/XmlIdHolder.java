/*-
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
package se.mithlond.services.organisation.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Specification for how to extract the XmlID from this {@link XmlIdHolder}.
 * This typically is the API equivalent for obtaining an {@link javax.xml.bind.annotation.XmlID}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface XmlIdHolder {

    /**
     * Retrieves the XmlID from this XmlIdHolder.
     *
     * @return the XmlID from this XmlIdHolder.
     */
    @NotNull
    @Size(min=1)
    String getXmlId();
}
