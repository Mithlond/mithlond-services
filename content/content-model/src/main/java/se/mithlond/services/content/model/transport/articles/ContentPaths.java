/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.transport.articles;

import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Transport wrapper for ContentPaths containing published Articles or other media.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = ContentPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = ContentPatterns.TRANSPORT_NAMESPACE, propOrder = {"realm", "contentPaths"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ContentPaths extends AbstractSimpleTransporter {

    /**
     * The realm which owns the transported List of Articles.
     */
    @XmlAttribute(required = true)
    private String realm;

    /**
     * A Path qualifier submitted to the backend service finding the Articles to wrap.
     * Typically, not all Articles should be returned for each call - the caller application will normally retrieve
     * only the articles pertaining to the active rendering location of the client. When the client requests all
     * available articles at the contentPath "/news", only news articles are retrieved.
     */
    @XmlElementWrapper
    @XmlElement(name = "path")
    private List<String> contentPaths;

    /**
     * JAXB-friendly constructor.
     */
    public ContentPaths() {
        this.contentPaths = new ArrayList<>();
    }

    /**
     * @return The name of the Realm which owns the Media placed at the given ContentPaths.
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Retrieves all wrapped ContentPaths.
     *
     * @return The ContentPaths to be transported.
     */
    public List<String> getContentPaths() {
        return contentPaths;
    }
}
