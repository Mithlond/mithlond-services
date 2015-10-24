# Creating unit tests for POJO Entities

Unit tests for POJO entities validate converting the entity to and from transport 
forms, as well as sorting POJO entity instances. 
The POJO entity junit test creation patterns are described in the process below.

## 1. Entity unit test class naming and inheritance

Create a unit test class with the name XXXXTest where `XXXXX` is the name of the entity you should test.
The unit test class should reside within the same package as the class it is testing (but, of course, in test scope 
of the model project where the entity resides). 
 
Make the unit test extend AbstractEntityTest as shown below:

<pre class="brush: java"><![CDATA[
    public class CategorizedAddressTest extends AbstractEntityTest { ... }
]]></pre>

The AbstractEntityTest class extends AbstractPlainJaxbTest and AbstractStandardizedTimezoneTest, which means that all 
dates and times are consistent for all test executions irrespective of the native TimeZone set in the underlying 
operating system - and that the JAXB binder and marshalling operations are supported smoothly. 
 
## 2. Setup shared state

The term `Shared state` within a unit test indicates the presence of private variables, holding state which 
should be accessible from **more than one test** method. (Shared state variables should only be used if data must 
be accessed from more than one test method).

Create shared state variables during testcase setup - and remove or revert them during testcase teardown, if their 
presence could create problems for other testcases. To ensure the order of setup/teardown statements, do not define 
more than one method annotated with the standard jUnit @Before or @After:

<pre class="brush: java"><![CDATA[
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
]]></pre>

## 3. Create transport classes for test

Elements within XML documents can refer to other Elements within the same XML document by means of an XML ID Reference.
This implies that XML documents may use powerful properties for shared references - but it also means that 
any Elements which refers to another Element must be placed **after** their reference in the XML document. If the 
order of XML ID references is not properly respected, the JAXB marshaller throws an Exception.
 
The simplest way to ensure that marshalling is always running smoothly when XMLID/XMLIDREF pairs are present is to 
create a transport class. Since we are now creating a model, and the transport model normally resides within compile 
scope in the API project, this transport class should be located in the model project's test classes.

Typically, transport classes can be copied to the API project for normal use in the application. In the case of our 
CategorizedAddress, there are two XMLIDREF-annotated members - in the CategorizedAddress class itself:

<pre class="brush: java"><![CDATA[
        @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
        @XmlIDREF
        @XmlAttribute(required = true, name = "categoryReference")
        private Category category;
]]></pre>


... and in its superclass, Listable:

<pre class="brush: java"><![CDATA[
	@NotNull
	@XmlAttribute(required = true, name = "organisationReference")
	@XmlIDREF
	@ManyToOne(optional = false)
	private Organisation owningOrganisation;
]]></pre>

This implies that XML elements marshalled from Organisation objects must be marshalled before the CategorizedAddress 
referring to that Organisation. It also means that XML elements marshalled from Category objects must be marshalled 
before the CategorizedAddress referring to that Category. Hence, we can create a transport entity which adheres to 
that order:

<pre class="brush: java"><![CDATA[
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlType(propOrder = {"organisations", "categories", "categorizedAddresses"})
@XmlAccessorType(XmlAccessType.FIELD)
public class CategorizedAddresses {

    // Internal state
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = false, required = true, name = "organisation")
    private List<Organisation> organisations;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = false, required = true, name = "category")
    private List<Category> categories;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = true, required = false, name = "categorizedAddress")
    private List<CategorizedAddress> categorizedAddresses;
    
    ....

]]></pre>

Note that the transport class should sport an `@XmlRootElement` annotation, as well as the `@XmlType` annotation 
defining the marshalling propOrder. The propOrder caters for the correct order of the CategorizedAddress's attributes. 

## 4. Create a `validateMarshalling()` test method skeleton

All unit test methods should be annotated with the jUnit `@Test` annotation, and have names on the form
`validateSomething()` where the `Something` name part must clearly describe what is being validated.
The first test method to create is `validateMarshalling`, where we should ensure that the JAXB annotations 
in the POJO entity class create correctly formed XML structures.
 
The validateMarshalling method is *very* important for entities that use relational database structures which 
typically are difficult to translate to tree structures (i.e. joins and many-to-many relationships). In these cases, 
the validateMarshalling method form the template basis for service implementations. *Do not underestimate the time 
saved in service implementation development by creating a solid validateMarshalling method*.

As all jUnit methods, the validateMarshalling method should have 3 parts which should be marked with comments:

1. **Assemble**: Sets up data for the test method. 
2. **Act**: Executes the test itself. 
3. **Assert**: Asserts that the expected outcome is attained.

In this case, the validateMarshalling method only uses shared state objects set up in the `@Before`-annotated method, 
implying that the *Assemble* part of the jUnit test simply consists of creating an instance of the CategorizedAddress
object which should be marshalled:  

<pre class="brush: java"><![CDATA[
        @Test
        public void validateMarshalling() {
    
            // Assemble
            final CategorizedAddresses toMarshal = new CategorizedAddresses(addressArray);
    
            // Act
            final String result = marshal(toMarshal);
    
            // Assert
            System.out.println("Got: " + result);
        }
]]></pre>

The `marshal` method internally uses a `PlainJaxbContextRule` which contains a JaxbXmlBinder and a JAXBContext to 
marshal all objects to an XML-formatted String. The superclass sets up the binder for our use - simply fire the test
method to see the marshalled result of  the CategorizedAddress object in the example above. 

## 5. Write the XML test data to a file

When running the validateMarshalling method, an XML structure is emitted onto the system console, as illustrated in 
the XML snippet below. While the entire structure is relevant, please be aware that all elements which contain an 
`@XMLID`-annotated element must be marshalled **before** elements that refer to them using an `@XMLIDREF`-annotated 
member: 

<pre class="brush: xml"><![CDATA[
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<organisation:categorizedAddresses xmlns:core="http://www.jguru.se/nazgul/core" xmlns:organisation="http://xmlns.mithlond.se/xml/ns/organisation" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <organisations>
        <organisation xmlID="organisation_name" jpaId="0" version="0">
            <organisationName>name</organisationName>
            <suffix>suffix</suffix>
            <phone>phone</phone>
            <bankAccountInfo>bankAccountInfo</bankAccountInfo>
            <postAccountInfo>postAccountInfo</postAccountInfo>
            <emailSuffix>emailSuffix</emailSuffix>
            <visitingAddress>
                <careOfLine>careOfLine</careOfLine>
                <departmentName>departmentName</departmentName>
                <street>street</street>
                <number>number</number>
                <city>city</city>
                <zipCode>zipCode</zipCode>
                <country>country</country>
                <description>description</description>
            </visitingAddress>
        </organisation>
    </organisations>
    <categories>
        <category xmlID="category_mail_delivery_address_delivery" jpaId="0" version="0">
            <classification>mail_delivery_address</classification>
            <categoryID>delivery</categoryID>
            <description>Address type [mail_delivery_address :: delivery]</description>
        </category>
        <category xmlID="category_visitable_address_cafe" jpaId="0" version="0">
            <classification>visitable_address</classification>
            <categoryID>cafe</categoryID>
            <description>Address type [visitable_address :: cafe]</description>
        </category>
        <category xmlID="category_visitable_address_home" jpaId="0" version="0">
            <classification>visitable_address</classification>
            <categoryID>home</categoryID>
            <description>Address type [visitable_address :: home]</description>
        </category>
        <category xmlID="category_visitable_address_outdoors" jpaId="0" version="0">
            <classification>visitable_address</classification>
            <categoryID>outdoors</categoryID>
            <description>Address type [visitable_address :: outdoors]</description>
        </category>
        <category xmlID="category_visitable_address_pub_restaurant" jpaId="0" version="0">
            <classification>visitable_address</classification>
            <categoryID>pub_restaurant</categoryID>
            <description>Address type [visitable_address :: pub_restaurant]</description>
        </category>
        <category xmlID="category_visitable_address_shop" jpaId="0" version="0">
            <classification>visitable_address</classification>
            <categoryID>shop</categoryID>
            <description>Address type [visitable_address :: shop]</description>
        </category>
        <category xmlID="category_visitable_address_site" jpaId="0" version="0">
            <classification>visitable_address</classification>
            <categoryID>site</categoryID>
            <description>Address type [visitable_address :: site]</description>
        </category>
        <category xmlID="category_visitable_address_visiting" jpaId="0" version="0">
            <classification>visitable_address</classification>
            <categoryID>visiting</categoryID>
            <description>Address type [visitable_address :: visiting]</description>
        </category>
    </categories>
    <categorizedAddresses>
        <categorizedAddress categoryReference="category_visitable_address_home" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_0</shortDesc>
            <fullDesc>fullDesc_0</fullDesc>
            <address>
                <careOfLine>careOfLine_0</careOfLine>
                <departmentName>departmentName_0</departmentName>
                <street>street_0</street>
                <number>number_0</number>
                <city>city_0</city>
                <zipCode>zipCode_0</zipCode>
                <country>country_0</country>
                <description>description_0</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_visiting" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_0</shortDesc>
            <fullDesc>fullDesc_0</fullDesc>
            <address>
                <careOfLine>careOfLine_0</careOfLine>
                <departmentName>departmentName_0</departmentName>
                <street>street_0</street>
                <number>number_0</number>
                <city>city_0</city>
                <zipCode>zipCode_0</zipCode>
                <country>country_0</country>
                <description>description_0</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_mail_delivery_address_delivery" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_0</shortDesc>
            <fullDesc>fullDesc_0</fullDesc>
            <address>
                <careOfLine>careOfLine_0</careOfLine>
                <departmentName>departmentName_0</departmentName>
                <street>street_0</street>
                <number>number_0</number>
                <city>city_0</city>
                <zipCode>zipCode_0</zipCode>
                <country>country_0</country>
                <description>description_0</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_pub_restaurant" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_0</shortDesc>
            <fullDesc>fullDesc_0</fullDesc>
            <address>
                <careOfLine>careOfLine_0</careOfLine>
                <departmentName>departmentName_0</departmentName>
                <street>street_0</street>
                <number>number_0</number>
                <city>city_0</city>
                <zipCode>zipCode_0</zipCode>
                <country>country_0</country>
                <description>description_0</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_shop" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_0</shortDesc>
            <fullDesc>fullDesc_0</fullDesc>
            <address>
                <careOfLine>careOfLine_0</careOfLine>
                <departmentName>departmentName_0</departmentName>
                <street>street_0</street>
                <number>number_0</number>
                <city>city_0</city>
                <zipCode>zipCode_0</zipCode>
                <country>country_0</country>
                <description>description_0</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_outdoors" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_0</shortDesc>
            <fullDesc>fullDesc_0</fullDesc>
            <address>
                <careOfLine>careOfLine_0</careOfLine>
                <departmentName>departmentName_0</departmentName>
                <street>street_0</street>
                <number>number_0</number>
                <city>city_0</city>
                <zipCode>zipCode_0</zipCode>
                <country>country_0</country>
                <description>description_0</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_cafe" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_0</shortDesc>
            <fullDesc>fullDesc_0</fullDesc>
            <address>
                <careOfLine>careOfLine_0</careOfLine>
                <departmentName>departmentName_0</departmentName>
                <street>street_0</street>
                <number>number_0</number>
                <city>city_0</city>
                <zipCode>zipCode_0</zipCode>
                <country>country_0</country>
                <description>description_0</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_site" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_0</shortDesc>
            <fullDesc>fullDesc_0</fullDesc>
            <address>
                <careOfLine>careOfLine_0</careOfLine>
                <departmentName>departmentName_0</departmentName>
                <street>street_0</street>
                <number>number_0</number>
                <city>city_0</city>
                <zipCode>zipCode_0</zipCode>
                <country>country_0</country>
                <description>description_0</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_home" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_1</shortDesc>
            <fullDesc>fullDesc_1</fullDesc>
            <address>
                <careOfLine>careOfLine_1</careOfLine>
                <departmentName>departmentName_1</departmentName>
                <street>street_1</street>
                <number>number_1</number>
                <city>city_1</city>
                <zipCode>zipCode_1</zipCode>
                <country>country_1</country>
                <description>description_1</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_visiting" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_1</shortDesc>
            <fullDesc>fullDesc_1</fullDesc>
            <address>
                <careOfLine>careOfLine_1</careOfLine>
                <departmentName>departmentName_1</departmentName>
                <street>street_1</street>
                <number>number_1</number>
                <city>city_1</city>
                <zipCode>zipCode_1</zipCode>
                <country>country_1</country>
                <description>description_1</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_mail_delivery_address_delivery" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_1</shortDesc>
            <fullDesc>fullDesc_1</fullDesc>
            <address>
                <careOfLine>careOfLine_1</careOfLine>
                <departmentName>departmentName_1</departmentName>
                <street>street_1</street>
                <number>number_1</number>
                <city>city_1</city>
                <zipCode>zipCode_1</zipCode>
                <country>country_1</country>
                <description>description_1</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_pub_restaurant" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_1</shortDesc>
            <fullDesc>fullDesc_1</fullDesc>
            <address>
                <careOfLine>careOfLine_1</careOfLine>
                <departmentName>departmentName_1</departmentName>
                <street>street_1</street>
                <number>number_1</number>
                <city>city_1</city>
                <zipCode>zipCode_1</zipCode>
                <country>country_1</country>
                <description>description_1</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_shop" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_1</shortDesc>
            <fullDesc>fullDesc_1</fullDesc>
            <address>
                <careOfLine>careOfLine_1</careOfLine>
                <departmentName>departmentName_1</departmentName>
                <street>street_1</street>
                <number>number_1</number>
                <city>city_1</city>
                <zipCode>zipCode_1</zipCode>
                <country>country_1</country>
                <description>description_1</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_outdoors" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_1</shortDesc>
            <fullDesc>fullDesc_1</fullDesc>
            <address>
                <careOfLine>careOfLine_1</careOfLine>
                <departmentName>departmentName_1</departmentName>
                <street>street_1</street>
                <number>number_1</number>
                <city>city_1</city>
                <zipCode>zipCode_1</zipCode>
                <country>country_1</country>
                <description>description_1</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_cafe" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_1</shortDesc>
            <fullDesc>fullDesc_1</fullDesc>
            <address>
                <careOfLine>careOfLine_1</careOfLine>
                <departmentName>departmentName_1</departmentName>
                <street>street_1</street>
                <number>number_1</number>
                <city>city_1</city>
                <zipCode>zipCode_1</zipCode>
                <country>country_1</country>
                <description>description_1</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_site" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_1</shortDesc>
            <fullDesc>fullDesc_1</fullDesc>
            <address>
                <careOfLine>careOfLine_1</careOfLine>
                <departmentName>departmentName_1</departmentName>
                <street>street_1</street>
                <number>number_1</number>
                <city>city_1</city>
                <zipCode>zipCode_1</zipCode>
                <country>country_1</country>
                <description>description_1</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_home" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_2</shortDesc>
            <fullDesc>fullDesc_2</fullDesc>
            <address>
                <careOfLine>careOfLine_2</careOfLine>
                <departmentName>departmentName_2</departmentName>
                <street>street_2</street>
                <number>number_2</number>
                <city>city_2</city>
                <zipCode>zipCode_2</zipCode>
                <country>country_2</country>
                <description>description_2</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_visiting" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_2</shortDesc>
            <fullDesc>fullDesc_2</fullDesc>
            <address>
                <careOfLine>careOfLine_2</careOfLine>
                <departmentName>departmentName_2</departmentName>
                <street>street_2</street>
                <number>number_2</number>
                <city>city_2</city>
                <zipCode>zipCode_2</zipCode>
                <country>country_2</country>
                <description>description_2</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_mail_delivery_address_delivery" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_2</shortDesc>
            <fullDesc>fullDesc_2</fullDesc>
            <address>
                <careOfLine>careOfLine_2</careOfLine>
                <departmentName>departmentName_2</departmentName>
                <street>street_2</street>
                <number>number_2</number>
                <city>city_2</city>
                <zipCode>zipCode_2</zipCode>
                <country>country_2</country>
                <description>description_2</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_pub_restaurant" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_2</shortDesc>
            <fullDesc>fullDesc_2</fullDesc>
            <address>
                <careOfLine>careOfLine_2</careOfLine>
                <departmentName>departmentName_2</departmentName>
                <street>street_2</street>
                <number>number_2</number>
                <city>city_2</city>
                <zipCode>zipCode_2</zipCode>
                <country>country_2</country>
                <description>description_2</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_shop" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_2</shortDesc>
            <fullDesc>fullDesc_2</fullDesc>
            <address>
                <careOfLine>careOfLine_2</careOfLine>
                <departmentName>departmentName_2</departmentName>
                <street>street_2</street>
                <number>number_2</number>
                <city>city_2</city>
                <zipCode>zipCode_2</zipCode>
                <country>country_2</country>
                <description>description_2</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_outdoors" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_2</shortDesc>
            <fullDesc>fullDesc_2</fullDesc>
            <address>
                <careOfLine>careOfLine_2</careOfLine>
                <departmentName>departmentName_2</departmentName>
                <street>street_2</street>
                <number>number_2</number>
                <city>city_2</city>
                <zipCode>zipCode_2</zipCode>
                <country>country_2</country>
                <description>description_2</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_cafe" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_2</shortDesc>
            <fullDesc>fullDesc_2</fullDesc>
            <address>
                <careOfLine>careOfLine_2</careOfLine>
                <departmentName>departmentName_2</departmentName>
                <street>street_2</street>
                <number>number_2</number>
                <city>city_2</city>
                <zipCode>zipCode_2</zipCode>
                <country>country_2</country>
                <description>description_2</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_site" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_2</shortDesc>
            <fullDesc>fullDesc_2</fullDesc>
            <address>
                <careOfLine>careOfLine_2</careOfLine>
                <departmentName>departmentName_2</departmentName>
                <street>street_2</street>
                <number>number_2</number>
                <city>city_2</city>
                <zipCode>zipCode_2</zipCode>
                <country>country_2</country>
                <description>description_2</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_home" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_3</shortDesc>
            <fullDesc>fullDesc_3</fullDesc>
            <address>
                <careOfLine>careOfLine_3</careOfLine>
                <departmentName>departmentName_3</departmentName>
                <street>street_3</street>
                <number>number_3</number>
                <city>city_3</city>
                <zipCode>zipCode_3</zipCode>
                <country>country_3</country>
                <description>description_3</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_visiting" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_3</shortDesc>
            <fullDesc>fullDesc_3</fullDesc>
            <address>
                <careOfLine>careOfLine_3</careOfLine>
                <departmentName>departmentName_3</departmentName>
                <street>street_3</street>
                <number>number_3</number>
                <city>city_3</city>
                <zipCode>zipCode_3</zipCode>
                <country>country_3</country>
                <description>description_3</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_mail_delivery_address_delivery" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_3</shortDesc>
            <fullDesc>fullDesc_3</fullDesc>
            <address>
                <careOfLine>careOfLine_3</careOfLine>
                <departmentName>departmentName_3</departmentName>
                <street>street_3</street>
                <number>number_3</number>
                <city>city_3</city>
                <zipCode>zipCode_3</zipCode>
                <country>country_3</country>
                <description>description_3</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_pub_restaurant" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_3</shortDesc>
            <fullDesc>fullDesc_3</fullDesc>
            <address>
                <careOfLine>careOfLine_3</careOfLine>
                <departmentName>departmentName_3</departmentName>
                <street>street_3</street>
                <number>number_3</number>
                <city>city_3</city>
                <zipCode>zipCode_3</zipCode>
                <country>country_3</country>
                <description>description_3</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_shop" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_3</shortDesc>
            <fullDesc>fullDesc_3</fullDesc>
            <address>
                <careOfLine>careOfLine_3</careOfLine>
                <departmentName>departmentName_3</departmentName>
                <street>street_3</street>
                <number>number_3</number>
                <city>city_3</city>
                <zipCode>zipCode_3</zipCode>
                <country>country_3</country>
                <description>description_3</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_outdoors" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_3</shortDesc>
            <fullDesc>fullDesc_3</fullDesc>
            <address>
                <careOfLine>careOfLine_3</careOfLine>
                <departmentName>departmentName_3</departmentName>
                <street>street_3</street>
                <number>number_3</number>
                <city>city_3</city>
                <zipCode>zipCode_3</zipCode>
                <country>country_3</country>
                <description>description_3</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_cafe" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_3</shortDesc>
            <fullDesc>fullDesc_3</fullDesc>
            <address>
                <careOfLine>careOfLine_3</careOfLine>
                <departmentName>departmentName_3</departmentName>
                <street>street_3</street>
                <number>number_3</number>
                <city>city_3</city>
                <zipCode>zipCode_3</zipCode>
                <country>country_3</country>
                <description>description_3</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_site" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_3</shortDesc>
            <fullDesc>fullDesc_3</fullDesc>
            <address>
                <careOfLine>careOfLine_3</careOfLine>
                <departmentName>departmentName_3</departmentName>
                <street>street_3</street>
                <number>number_3</number>
                <city>city_3</city>
                <zipCode>zipCode_3</zipCode>
                <country>country_3</country>
                <description>description_3</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_home" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_4</shortDesc>
            <fullDesc>fullDesc_4</fullDesc>
            <address>
                <careOfLine>careOfLine_4</careOfLine>
                <departmentName>departmentName_4</departmentName>
                <street>street_4</street>
                <number>number_4</number>
                <city>city_4</city>
                <zipCode>zipCode_4</zipCode>
                <country>country_4</country>
                <description>description_4</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_visiting" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_4</shortDesc>
            <fullDesc>fullDesc_4</fullDesc>
            <address>
                <careOfLine>careOfLine_4</careOfLine>
                <departmentName>departmentName_4</departmentName>
                <street>street_4</street>
                <number>number_4</number>
                <city>city_4</city>
                <zipCode>zipCode_4</zipCode>
                <country>country_4</country>
                <description>description_4</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_mail_delivery_address_delivery" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_4</shortDesc>
            <fullDesc>fullDesc_4</fullDesc>
            <address>
                <careOfLine>careOfLine_4</careOfLine>
                <departmentName>departmentName_4</departmentName>
                <street>street_4</street>
                <number>number_4</number>
                <city>city_4</city>
                <zipCode>zipCode_4</zipCode>
                <country>country_4</country>
                <description>description_4</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_pub_restaurant" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_4</shortDesc>
            <fullDesc>fullDesc_4</fullDesc>
            <address>
                <careOfLine>careOfLine_4</careOfLine>
                <departmentName>departmentName_4</departmentName>
                <street>street_4</street>
                <number>number_4</number>
                <city>city_4</city>
                <zipCode>zipCode_4</zipCode>
                <country>country_4</country>
                <description>description_4</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_shop" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_4</shortDesc>
            <fullDesc>fullDesc_4</fullDesc>
            <address>
                <careOfLine>careOfLine_4</careOfLine>
                <departmentName>departmentName_4</departmentName>
                <street>street_4</street>
                <number>number_4</number>
                <city>city_4</city>
                <zipCode>zipCode_4</zipCode>
                <country>country_4</country>
                <description>description_4</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_outdoors" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_4</shortDesc>
            <fullDesc>fullDesc_4</fullDesc>
            <address>
                <careOfLine>careOfLine_4</careOfLine>
                <departmentName>departmentName_4</departmentName>
                <street>street_4</street>
                <number>number_4</number>
                <city>city_4</city>
                <zipCode>zipCode_4</zipCode>
                <country>country_4</country>
                <description>description_4</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_cafe" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_4</shortDesc>
            <fullDesc>fullDesc_4</fullDesc>
            <address>
                <careOfLine>careOfLine_4</careOfLine>
                <departmentName>departmentName_4</departmentName>
                <street>street_4</street>
                <number>number_4</number>
                <city>city_4</city>
                <zipCode>zipCode_4</zipCode>
                <country>country_4</country>
                <description>description_4</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_site" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_4</shortDesc>
            <fullDesc>fullDesc_4</fullDesc>
            <address>
                <careOfLine>careOfLine_4</careOfLine>
                <departmentName>departmentName_4</departmentName>
                <street>street_4</street>
                <number>number_4</number>
                <city>city_4</city>
                <zipCode>zipCode_4</zipCode>
                <country>country_4</country>
                <description>description_4</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_home" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_5</shortDesc>
            <fullDesc>fullDesc_5</fullDesc>
            <address>
                <careOfLine>careOfLine_5</careOfLine>
                <departmentName>departmentName_5</departmentName>
                <street>street_5</street>
                <number>number_5</number>
                <city>city_5</city>
                <zipCode>zipCode_5</zipCode>
                <country>country_5</country>
                <description>description_5</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_visiting" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_5</shortDesc>
            <fullDesc>fullDesc_5</fullDesc>
            <address>
                <careOfLine>careOfLine_5</careOfLine>
                <departmentName>departmentName_5</departmentName>
                <street>street_5</street>
                <number>number_5</number>
                <city>city_5</city>
                <zipCode>zipCode_5</zipCode>
                <country>country_5</country>
                <description>description_5</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_mail_delivery_address_delivery" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_5</shortDesc>
            <fullDesc>fullDesc_5</fullDesc>
            <address>
                <careOfLine>careOfLine_5</careOfLine>
                <departmentName>departmentName_5</departmentName>
                <street>street_5</street>
                <number>number_5</number>
                <city>city_5</city>
                <zipCode>zipCode_5</zipCode>
                <country>country_5</country>
                <description>description_5</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_pub_restaurant" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_5</shortDesc>
            <fullDesc>fullDesc_5</fullDesc>
            <address>
                <careOfLine>careOfLine_5</careOfLine>
                <departmentName>departmentName_5</departmentName>
                <street>street_5</street>
                <number>number_5</number>
                <city>city_5</city>
                <zipCode>zipCode_5</zipCode>
                <country>country_5</country>
                <description>description_5</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_shop" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_5</shortDesc>
            <fullDesc>fullDesc_5</fullDesc>
            <address>
                <careOfLine>careOfLine_5</careOfLine>
                <departmentName>departmentName_5</departmentName>
                <street>street_5</street>
                <number>number_5</number>
                <city>city_5</city>
                <zipCode>zipCode_5</zipCode>
                <country>country_5</country>
                <description>description_5</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_outdoors" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_5</shortDesc>
            <fullDesc>fullDesc_5</fullDesc>
            <address>
                <careOfLine>careOfLine_5</careOfLine>
                <departmentName>departmentName_5</departmentName>
                <street>street_5</street>
                <number>number_5</number>
                <city>city_5</city>
                <zipCode>zipCode_5</zipCode>
                <country>country_5</country>
                <description>description_5</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_cafe" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_5</shortDesc>
            <fullDesc>fullDesc_5</fullDesc>
            <address>
                <careOfLine>careOfLine_5</careOfLine>
                <departmentName>departmentName_5</departmentName>
                <street>street_5</street>
                <number>number_5</number>
                <city>city_5</city>
                <zipCode>zipCode_5</zipCode>
                <country>country_5</country>
                <description>description_5</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_site" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_5</shortDesc>
            <fullDesc>fullDesc_5</fullDesc>
            <address>
                <careOfLine>careOfLine_5</careOfLine>
                <departmentName>departmentName_5</departmentName>
                <street>street_5</street>
                <number>number_5</number>
                <city>city_5</city>
                <zipCode>zipCode_5</zipCode>
                <country>country_5</country>
                <description>description_5</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_home" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_6</shortDesc>
            <fullDesc>fullDesc_6</fullDesc>
            <address>
                <careOfLine>careOfLine_6</careOfLine>
                <departmentName>departmentName_6</departmentName>
                <street>street_6</street>
                <number>number_6</number>
                <city>city_6</city>
                <zipCode>zipCode_6</zipCode>
                <country>country_6</country>
                <description>description_6</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_visiting" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_6</shortDesc>
            <fullDesc>fullDesc_6</fullDesc>
            <address>
                <careOfLine>careOfLine_6</careOfLine>
                <departmentName>departmentName_6</departmentName>
                <street>street_6</street>
                <number>number_6</number>
                <city>city_6</city>
                <zipCode>zipCode_6</zipCode>
                <country>country_6</country>
                <description>description_6</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_mail_delivery_address_delivery" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_6</shortDesc>
            <fullDesc>fullDesc_6</fullDesc>
            <address>
                <careOfLine>careOfLine_6</careOfLine>
                <departmentName>departmentName_6</departmentName>
                <street>street_6</street>
                <number>number_6</number>
                <city>city_6</city>
                <zipCode>zipCode_6</zipCode>
                <country>country_6</country>
                <description>description_6</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_pub_restaurant" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_6</shortDesc>
            <fullDesc>fullDesc_6</fullDesc>
            <address>
                <careOfLine>careOfLine_6</careOfLine>
                <departmentName>departmentName_6</departmentName>
                <street>street_6</street>
                <number>number_6</number>
                <city>city_6</city>
                <zipCode>zipCode_6</zipCode>
                <country>country_6</country>
                <description>description_6</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_shop" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_6</shortDesc>
            <fullDesc>fullDesc_6</fullDesc>
            <address>
                <careOfLine>careOfLine_6</careOfLine>
                <departmentName>departmentName_6</departmentName>
                <street>street_6</street>
                <number>number_6</number>
                <city>city_6</city>
                <zipCode>zipCode_6</zipCode>
                <country>country_6</country>
                <description>description_6</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_outdoors" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_6</shortDesc>
            <fullDesc>fullDesc_6</fullDesc>
            <address>
                <careOfLine>careOfLine_6</careOfLine>
                <departmentName>departmentName_6</departmentName>
                <street>street_6</street>
                <number>number_6</number>
                <city>city_6</city>
                <zipCode>zipCode_6</zipCode>
                <country>country_6</country>
                <description>description_6</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_cafe" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_6</shortDesc>
            <fullDesc>fullDesc_6</fullDesc>
            <address>
                <careOfLine>careOfLine_6</careOfLine>
                <departmentName>departmentName_6</departmentName>
                <street>street_6</street>
                <number>number_6</number>
                <city>city_6</city>
                <zipCode>zipCode_6</zipCode>
                <country>country_6</country>
                <description>description_6</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_site" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_6</shortDesc>
            <fullDesc>fullDesc_6</fullDesc>
            <address>
                <careOfLine>careOfLine_6</careOfLine>
                <departmentName>departmentName_6</departmentName>
                <street>street_6</street>
                <number>number_6</number>
                <city>city_6</city>
                <zipCode>zipCode_6</zipCode>
                <country>country_6</country>
                <description>description_6</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_home" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_7</shortDesc>
            <fullDesc>fullDesc_7</fullDesc>
            <address>
                <careOfLine>careOfLine_7</careOfLine>
                <departmentName>departmentName_7</departmentName>
                <street>street_7</street>
                <number>number_7</number>
                <city>city_7</city>
                <zipCode>zipCode_7</zipCode>
                <country>country_7</country>
                <description>description_7</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_visiting" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_7</shortDesc>
            <fullDesc>fullDesc_7</fullDesc>
            <address>
                <careOfLine>careOfLine_7</careOfLine>
                <departmentName>departmentName_7</departmentName>
                <street>street_7</street>
                <number>number_7</number>
                <city>city_7</city>
                <zipCode>zipCode_7</zipCode>
                <country>country_7</country>
                <description>description_7</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_mail_delivery_address_delivery" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_7</shortDesc>
            <fullDesc>fullDesc_7</fullDesc>
            <address>
                <careOfLine>careOfLine_7</careOfLine>
                <departmentName>departmentName_7</departmentName>
                <street>street_7</street>
                <number>number_7</number>
                <city>city_7</city>
                <zipCode>zipCode_7</zipCode>
                <country>country_7</country>
                <description>description_7</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_pub_restaurant" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_7</shortDesc>
            <fullDesc>fullDesc_7</fullDesc>
            <address>
                <careOfLine>careOfLine_7</careOfLine>
                <departmentName>departmentName_7</departmentName>
                <street>street_7</street>
                <number>number_7</number>
                <city>city_7</city>
                <zipCode>zipCode_7</zipCode>
                <country>country_7</country>
                <description>description_7</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_shop" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_7</shortDesc>
            <fullDesc>fullDesc_7</fullDesc>
            <address>
                <careOfLine>careOfLine_7</careOfLine>
                <departmentName>departmentName_7</departmentName>
                <street>street_7</street>
                <number>number_7</number>
                <city>city_7</city>
                <zipCode>zipCode_7</zipCode>
                <country>country_7</country>
                <description>description_7</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_outdoors" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_7</shortDesc>
            <fullDesc>fullDesc_7</fullDesc>
            <address>
                <careOfLine>careOfLine_7</careOfLine>
                <departmentName>departmentName_7</departmentName>
                <street>street_7</street>
                <number>number_7</number>
                <city>city_7</city>
                <zipCode>zipCode_7</zipCode>
                <country>country_7</country>
                <description>description_7</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_cafe" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_7</shortDesc>
            <fullDesc>fullDesc_7</fullDesc>
            <address>
                <careOfLine>careOfLine_7</careOfLine>
                <departmentName>departmentName_7</departmentName>
                <street>street_7</street>
                <number>number_7</number>
                <city>city_7</city>
                <zipCode>zipCode_7</zipCode>
                <country>country_7</country>
                <description>description_7</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_site" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_7</shortDesc>
            <fullDesc>fullDesc_7</fullDesc>
            <address>
                <careOfLine>careOfLine_7</careOfLine>
                <departmentName>departmentName_7</departmentName>
                <street>street_7</street>
                <number>number_7</number>
                <city>city_7</city>
                <zipCode>zipCode_7</zipCode>
                <country>country_7</country>
                <description>description_7</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_home" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_8</shortDesc>
            <fullDesc>fullDesc_8</fullDesc>
            <address>
                <careOfLine>careOfLine_8</careOfLine>
                <departmentName>departmentName_8</departmentName>
                <street>street_8</street>
                <number>number_8</number>
                <city>city_8</city>
                <zipCode>zipCode_8</zipCode>
                <country>country_8</country>
                <description>description_8</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_visiting" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_8</shortDesc>
            <fullDesc>fullDesc_8</fullDesc>
            <address>
                <careOfLine>careOfLine_8</careOfLine>
                <departmentName>departmentName_8</departmentName>
                <street>street_8</street>
                <number>number_8</number>
                <city>city_8</city>
                <zipCode>zipCode_8</zipCode>
                <country>country_8</country>
                <description>description_8</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_mail_delivery_address_delivery" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_8</shortDesc>
            <fullDesc>fullDesc_8</fullDesc>
            <address>
                <careOfLine>careOfLine_8</careOfLine>
                <departmentName>departmentName_8</departmentName>
                <street>street_8</street>
                <number>number_8</number>
                <city>city_8</city>
                <zipCode>zipCode_8</zipCode>
                <country>country_8</country>
                <description>description_8</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_pub_restaurant" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_8</shortDesc>
            <fullDesc>fullDesc_8</fullDesc>
            <address>
                <careOfLine>careOfLine_8</careOfLine>
                <departmentName>departmentName_8</departmentName>
                <street>street_8</street>
                <number>number_8</number>
                <city>city_8</city>
                <zipCode>zipCode_8</zipCode>
                <country>country_8</country>
                <description>description_8</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_shop" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_8</shortDesc>
            <fullDesc>fullDesc_8</fullDesc>
            <address>
                <careOfLine>careOfLine_8</careOfLine>
                <departmentName>departmentName_8</departmentName>
                <street>street_8</street>
                <number>number_8</number>
                <city>city_8</city>
                <zipCode>zipCode_8</zipCode>
                <country>country_8</country>
                <description>description_8</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_outdoors" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_8</shortDesc>
            <fullDesc>fullDesc_8</fullDesc>
            <address>
                <careOfLine>careOfLine_8</careOfLine>
                <departmentName>departmentName_8</departmentName>
                <street>street_8</street>
                <number>number_8</number>
                <city>city_8</city>
                <zipCode>zipCode_8</zipCode>
                <country>country_8</country>
                <description>description_8</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_cafe" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_8</shortDesc>
            <fullDesc>fullDesc_8</fullDesc>
            <address>
                <careOfLine>careOfLine_8</careOfLine>
                <departmentName>departmentName_8</departmentName>
                <street>street_8</street>
                <number>number_8</number>
                <city>city_8</city>
                <zipCode>zipCode_8</zipCode>
                <country>country_8</country>
                <description>description_8</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_site" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_8</shortDesc>
            <fullDesc>fullDesc_8</fullDesc>
            <address>
                <careOfLine>careOfLine_8</careOfLine>
                <departmentName>departmentName_8</departmentName>
                <street>street_8</street>
                <number>number_8</number>
                <city>city_8</city>
                <zipCode>zipCode_8</zipCode>
                <country>country_8</country>
                <description>description_8</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_home" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_9</shortDesc>
            <fullDesc>fullDesc_9</fullDesc>
            <address>
                <careOfLine>careOfLine_9</careOfLine>
                <departmentName>departmentName_9</departmentName>
                <street>street_9</street>
                <number>number_9</number>
                <city>city_9</city>
                <zipCode>zipCode_9</zipCode>
                <country>country_9</country>
                <description>description_9</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_visiting" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_9</shortDesc>
            <fullDesc>fullDesc_9</fullDesc>
            <address>
                <careOfLine>careOfLine_9</careOfLine>
                <departmentName>departmentName_9</departmentName>
                <street>street_9</street>
                <number>number_9</number>
                <city>city_9</city>
                <zipCode>zipCode_9</zipCode>
                <country>country_9</country>
                <description>description_9</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_mail_delivery_address_delivery" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_9</shortDesc>
            <fullDesc>fullDesc_9</fullDesc>
            <address>
                <careOfLine>careOfLine_9</careOfLine>
                <departmentName>departmentName_9</departmentName>
                <street>street_9</street>
                <number>number_9</number>
                <city>city_9</city>
                <zipCode>zipCode_9</zipCode>
                <country>country_9</country>
                <description>description_9</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_pub_restaurant" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_9</shortDesc>
            <fullDesc>fullDesc_9</fullDesc>
            <address>
                <careOfLine>careOfLine_9</careOfLine>
                <departmentName>departmentName_9</departmentName>
                <street>street_9</street>
                <number>number_9</number>
                <city>city_9</city>
                <zipCode>zipCode_9</zipCode>
                <country>country_9</country>
                <description>description_9</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_shop" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_9</shortDesc>
            <fullDesc>fullDesc_9</fullDesc>
            <address>
                <careOfLine>careOfLine_9</careOfLine>
                <departmentName>departmentName_9</departmentName>
                <street>street_9</street>
                <number>number_9</number>
                <city>city_9</city>
                <zipCode>zipCode_9</zipCode>
                <country>country_9</country>
                <description>description_9</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_outdoors" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_9</shortDesc>
            <fullDesc>fullDesc_9</fullDesc>
            <address>
                <careOfLine>careOfLine_9</careOfLine>
                <departmentName>departmentName_9</departmentName>
                <street>street_9</street>
                <number>number_9</number>
                <city>city_9</city>
                <zipCode>zipCode_9</zipCode>
                <country>country_9</country>
                <description>description_9</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_cafe" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_9</shortDesc>
            <fullDesc>fullDesc_9</fullDesc>
            <address>
                <careOfLine>careOfLine_9</careOfLine>
                <departmentName>departmentName_9</departmentName>
                <street>street_9</street>
                <number>number_9</number>
                <city>city_9</city>
                <zipCode>zipCode_9</zipCode>
                <country>country_9</country>
                <description>description_9</description>
            </address>
        </categorizedAddress>
        <categorizedAddress categoryReference="category_visitable_address_site" organisationReference="organisation_name" jpaId="0" version="0">
            <shortDesc>shortDesc_9</shortDesc>
            <fullDesc>fullDesc_9</fullDesc>
            <address>
                <careOfLine>careOfLine_9</careOfLine>
                <departmentName>departmentName_9</departmentName>
                <street>street_9</street>
                <number>number_9</number>
                <city>city_9</city>
                <zipCode>zipCode_9</zipCode>
                <country>country_9</country>
                <description>description_9</description>
            </address>
        </categorizedAddress>
    </categorizedAddresses>
</organisation:categorizedAddresses>
]]></pre>

Copy the data to a test resource file, which should reside somewhere below the `src/test/java/resources/testdata` 
directory. Name the test resource similar to the classname, followed by the suffix `.xml`. In this example, the test 
validates the Program entity - and the test resource is therefore called `programs.xml`:
 
<img src="images/programsResource.png" style="border: solid DarkGray 1px;" />

## 5. Insert XML validation

Comment out the `System.out.println` statement, and insert the standard XML validation statements. 
Follow the template code below:

<pre class="brush: java"><![CDATA[
    @Test
    public void validateMarshalling() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/categorizedAddresses.xml");
        final CategorizedAddresses toMarshal = new CategorizedAddresses(addressArray);

        // Act
        final String result = marshal(toMarshal);

        // Assert
        // System.out.println("Got: " + result);
        validateIdenticalContent(expected, result);
    }
]]></pre>

This concludes the `validateMarshalling()` method.

## 6. Create a `validateUnmarshalling()` method

We should now validate the inverse operation, that is unmarshalling an XML structure (back) into a set of POJO 
entities. The unmarshalling process uses the shared data already presented to validate that resurrected entity 
objects are identical to (but not the same as) expected results.

While the method structure is similar to that of the validateMarshalling method, we should use the `unmarshal` method
to read all data from an XML formatted String and automatically perform unmarshalling back to 
an entity object of the supplied class:  
 
<pre class="brush: java"><![CDATA[
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
]]></pre> 

This concludes the mandatory parts of the unit tests - but do not feel restricted in creating *additional* 
validation methods in the unit test class.