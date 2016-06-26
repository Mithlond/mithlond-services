/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jaxb
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
package se.mithlond.services.shared.spi.jaxb.adapter;

import org.eclipse.persistence.oxm.annotations.XmlCDATA;
import se.mithlond.services.shared.spi.jaxb.SharedJaxbPatterns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = SharedJaxbPatterns.NAMESPACE)
@XmlType(namespace = SharedJaxbPatterns.NAMESPACE,
        propOrder = {"markup"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ArticleExampleVO {

    @XmlElement
    @XmlCDATA
    private String markup;

    public ArticleExampleVO() {
    }

    public ArticleExampleVO(final String markup) {
        this.markup = markup;
    }

    public String getMarkup() {
        return markup;
    }
}
