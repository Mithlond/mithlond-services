package se.mithlond.services.organisation.model.address;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.helpers.Categories;
import se.mithlond.services.organisation.model.helpers.CategorizedAddresses;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CategorizedAddressTest extends AbstractEntityTest {

    // Shared state
    private CategorizedAddress[] addressArray;
    private List<CategorizedAddress> addresses;
    private Organisation organisation;
    private Address organisationAddress;

    @Before
    public void setupSharedState() {

        organisationAddress = new Address("careOfLine", "departmentName", "street", "number",
                "city", "zipCode", "country", "description");
        organisation = new Organisation("name", "suffix", "phone", "bankAccountInfo",
                "postAccountInfo", organisationAddress, "emailSuffix");

        addresses = new ArrayList<>();

        for(int i = 0; i < 10; i++) {

            for(WellKnownAddressType current : WellKnownAddressType.values()) {

                final Address currentAddress = new Address(
                        "careOfLine_" + i,
                        "departmentName_" + i,
                        "street_" + i,
                        "number_" + i,
                        "city_" + i,
                        "zipCode_" + i,
                        "country_" + i,
                        "description_" + i);

                addresses.add(new CategorizedAddress(
                        "shortDesc_" + i,
                        "fullDesc_" + i,
                        current,
                        organisation,
                        currentAddress));
            }
        }

        addressArray = addresses.toArray(new CategorizedAddress[addresses.size()]);
    }

    @Test
    public void validateMarshalling() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/categorizedAddresses.xml");

        // Act
        final String result = marshal(new CategorizedAddresses(addressArray));

        // Assert
        // System.out.println("Got: " + result);
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateUnmarshalling() {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/categorizedAddresses.xml");
        jaxb.add(CategorizedAddresses.class);

        // Act
        final CategorizedAddresses result = unmarshal(CategorizedAddresses.class, data);

        // Assert
        Assert.assertNotNull(result);

        final List<CategorizedAddress> resurrected = result.getCategorizedAddresses();
        Assert.assertEquals(addresses.size(), resurrected.size());

        for(int i = 0; i < addresses.size(); i++) {

            final CategorizedAddress expected = addresses.get(i);
            final CategorizedAddress actual = resurrected.get(i);

            Assert.assertNotSame(expected, actual);
            Assert.assertEquals(expected, actual);
        }
    }
}
