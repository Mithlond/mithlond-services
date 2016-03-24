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
package se.mithlond.services.content.model.articles.helpers;

import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.content.model.articles.Article;
import se.mithlond.services.content.model.articles.Markup;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = ContentPatterns.NAMESPACE, name = "ArticleList")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(Markup.class)
public class Articles {

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = false, required = true, name = "article")
    private List<Article> articles;

    public Articles() {
        articles = new ArrayList<>();
    }

    public List<Article> getArticles() {
        return articles;
    }
}
