/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.transport.articles;

import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.content.model.articles.Article;
import se.mithlond.services.shared.spi.algorithms.Validate;
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
 * Holder for a List of markup-sporting Articles, located at a logical path ("selectionPath") within the
 * structure of Articles.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = ContentPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = ContentPatterns.TRANSPORT_NAMESPACE,
        propOrder = {"realm", "selectionPath", "articleList"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Articles extends AbstractSimpleTransporter {

    /**
     * The realm which owns the transported List of Articles.
     */
    @XmlAttribute(required = true)
    private String realm;

    /**
     * A Path qualifier submitted to the backend service finding the Articles to wrap.
     * Typically, not all Articles should be returned for each call - the caller application will normally retrieve
     * only the articles pertaining to the active rendering location of the client. When the client requests all
     * available articles at the selectionPath "/news", only news articles are retrieved.
     */
    @XmlElement
    private String selectionPath;

    /**
     * The List of Articles wrapped in this transport object.
     */
    @XmlElementWrapper
    @XmlElement(name = "article")
    private List<Article> articleList;

    /**
     * JAXB-friendly constructor.
     */
    public Articles() {
        this.articleList = new ArrayList<>();
    }

    /**
     * Compound constructor creating an Articles transport object wrapping the supplied objects.
     *
     * @param realm         The realm which owns the transported List of Articles.
     * @param selectionPath A Path qualifier submitted to the backend service finding the Articles to wrap.
     *                      Typically, not all Articles should be returned for each call - the caller application
     *                      will normally retrieve only the articles pertaining to the active rendering location of
     *                      the client. When the client requests all available articles at the selectionPath "/news",
     *                      only news articles are retrieved.
     * @param articleList   The List of Articles wrapped in this transport object.
     */
    public Articles(final String realm,
                    final String selectionPath,
                    final List<Article> articleList) {

        this();

        // Check sanity and assign internal state
        this.realm = Validate.notEmpty(realm, "Cannot handle null or empty 'realm' argument.");

        this.selectionPath = selectionPath;
        this.realm = realm;
        if (articleList != null) {
            this.articleList.addAll(articleList);
        }
    }

    /**
     * @return The name of the Realm which owns the Articles within this transport object.
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Assigns the realm of this Articles transport object.
     *
     * @param realm A non-empty realm.
     */
    public void setRealm(final String realm) {
        this.realm = Validate.notEmpty(realm, "realm");
    }

    /**
     * Retrieves the active selectionPath, which is a Path qualifier submitted to the backend service finding the
     * Articles wrapped in this transport object.
     *
     * @return the active selectionPath, which is a Path qualifier submitted to the backend service finding the
     * Articles wrapped in this transport object. Typically, when the client requests all
     * available articles at the selectionPath "/news", only news articles are retrieved.
     */
    public String getSelectionPath() {
        return selectionPath;
    }

    /**
     * Retrieves the List of Articles transported.
     *
     * @return the List of Articles transported.
     */
    public List<Article> getArticleList() {
        return articleList;
    }
}
