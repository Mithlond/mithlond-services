package se.mithlond.services.content.model.articles.helpers;

import se.mithlond.services.content.model.Patterns;
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
@XmlRootElement(namespace = Patterns.NAMESPACE, name = "ArticleList")
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
