package se.mithlond.services.content.model.localization;

import org.junit.Before;
import org.junit.Test;
import se.mithlond.services.content.model.localization.helpers.LocalizedTextsHolder;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalizedTextsTest extends AbstractPlainJaxbTest {

    // Shared state
    private LocalizedTextsHolder holder;

    @Before
    public void setupSharedState() {

        holder = new LocalizedTextsHolder();
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final LocalizedTexts unitUnderTest = new LocalizedTexts(new Localization("sv"), "Hejsan");
        unitUnderTest.setText(new Localization("no"), "Morrn Da");
        unitUnderTest.setText(new Localization("en"), "Hello");
        unitUnderTest.setText(new Localization("en", "US", null), "Hi");
        holder.addAll(unitUnderTest);

        // Act
        final String result = marshalToXML(holder);
        System.out.println("Got: " + result);

        // Assert
    }
}
