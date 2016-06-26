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

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ArticleTest extends AbstractPlainJaxbTest {

    private String markup = "<div><h1>Title</h1><div style='foo'>content</div></div>";
    private String transportForm = CDataAdapter.CDATA_START + markup + CDataAdapter.CDATA_END;

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final ArticleExampleVO article = new ArticleExampleVO(markup);

        // Act
        final String marshalled = marshalToXML(article);
        System.out.println("Got: " + marshalled);

        // Assert
        Assert.assertNotNull(marshalled);

    }
}
