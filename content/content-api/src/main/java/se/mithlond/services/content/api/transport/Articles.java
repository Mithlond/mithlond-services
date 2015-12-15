/*
 * #%L
 * Nazgul Project: mithlond-services-content-api
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
package se.mithlond.services.content.api.transport;

import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.content.model.articles.Article;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Holder for a List of markup-sporting Articles.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class Articles implements Serializable {

    // Internal state
    @XmlAttribute(required = true)
    private String realm;

    @XmlElement
    private String selectionPath;

    @XmlElementWrapper
    @XmlElement(name = "article")
    private List<Article> articleList;

    /**
     * JAXB-friendly constructor.
     */
    public Articles() {
    }

    public Articles(final String realm,
            final String selectionPath,
            final List<Article> articleList) {

        // Check sanity and assign internal state
        this.realm = Validate.notEmpty(realm, "Cannot handle null or empty 'realm' argument.");

        this.selectionPath = selectionPath;
        this.realm = realm;
        this.articleList = articleList;
    }

    /**
     * @return
     */
    public String getRealm() {
        return realm;
    }

    public List<Article> getArticleList() {
        return articleList;
    }
}
