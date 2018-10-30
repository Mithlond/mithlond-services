# Automated integration tests for POJO Entities

Integration tests for POJO entities validate and correct database operations using JPA and JPQL constructs.
Constructs such as NamedQueries are notoriously difficult to test in isolation, since they require an 
operational EntityManager. Therefore, it is sometimes beneficial to create automated integration tests for POJO 
entity classes.

All entity integration tests are run automatically, and communicates with an in-memory database which is 
automatically set up (structure and data) before the tests are executed. The in-memory database is correspondingly 
torn down after test execution, implying that all POJO entity integration tests can destroy database state (for 
example, deleting rows or tables) as required by the test without any fear of corrupting real database state.

The 3 principle steps of POJO entity integration testing are illustrated in the image below:

<img src="images/plantuml/entity_integrationtests.png" style="border: solid DarkGray 1px;" />

Creating a POJO entity integration test is done in the following steps:

## 1. Entity integration test class naming and inheritance

Create a unit test class with the name XXXXIntegrationTest where `XXXXX` is the name of the entity you should test.
The unit test class should reside within the same package as the class it is testing (but, of course, in test scope 
of the model project where the entity resides).
   
The integration test class should extend `AbstractIntegrationTest`, as illustrated in the code snippet below:

<pre class="brush: java"><![CDATA[
    public class GroupIntegrationTest extends AbstractIntegrationTest { ... }
]]></pre>

The AbstractIntegrationTest class extends the StandardPersistenceTest of the Nazgul Core framework, where most of its 
functionality is implemented.
    
## 2. Override the `cleanupTestSchema()` method 

If particular sets of foreign keys are employed between the POJO entity classes, the cleanup method can
throw an exception. Handle that:

<pre class="brush: java"><![CDATA[
    /**
     * Override to ignore Foreign Key violation in dropping all database objects.
     * {@inheritDoc}
     */
    @Override
    protected void cleanupTestSchema(final boolean shutdownDatabase) {
        try {
            // 
            // For all relevant tables, run something like: 
            // ALTER TABLE "MyTable" ALTER COLUMN "ID" RESTART WITH 0
            // 
            super.cleanupTestSchema(shutdownDatabase);
        } catch (Exception e) {
            // Ignore this
        }
    }
]]></pre>

## 3. Override the `doCustomSetup()` method

Integration tests can sometimes use big data structures. It is convenient to read or created those shared POJOs 
before the tests start - and the place for such things are the `doCustomSetup()` method. As illustrated in the 
snippet below, 2 things are done here:
 
1. Add the transport type GroupsAndGuilds to the jaxb instance (which is the PlainJaxbContextRule in the 
   test's superclass) to make it available to the JAXB Marshaller and Unmarshaller.
   
2. Use the unmarshal method to resurrect data already written to a file in the path 
   "testdata/managedOrganisationAndGroups.xml". We now have our plain POJO data structure ready for use.

<pre class="brush: java"><![CDATA[
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        // Add the helper transport type to the JAXBContext within this test
        jaxb.add(GroupsAndGuilds.class);

        // Fake reading some transported data.
        // This occurs when reading / unmarshalling transport model entities from the network.
        final GroupsAndGuilds transported = unmarshal(
                GroupsAndGuilds.class,
                XmlTestUtils.readFully("testdata/managedOrganisationAndGroups.xml"));
        receivedFromClient = transported.getGroupsAndGuilds();
    }
]]></pre>
    
## 4. Create a testdata directory with the same name as the test
    
Integration tests need an EntityManager, which means that they need a `persistence.xml` file. However, we should not 
be required to use the persistence.xml file defined in the project's own `src/main/resources` location. Instead, all 
persistence-related files of an IntegrationTest reside within a directory called the "testdata" directory.

The testdata directory is located under the `src/test/resources/testdata` and has the same name as the integration 
test class. In our case, since the test is called `GroupIntegrationTest`, we should create a directory with the path
`src/test/resources/testdata/GroupIntegrationTest`. The image below shows 3 testdata directories belonging to 
separate integration tests:

<img src="images/testdataDirectories.png" style="border: solid DarkGray 1px;" />

The testdata directory contains a minimum of 3 files:

1. A `persistence.xml` file, which requires no JPA property definitions. These property definitions are instead added
   programmatically by the framework. The name of the persistence unit should be **"InMemoryTestPU"**.

2. A `setup_[testMethodName].xml` file. This is repeated for each test method within the test, and contains a 
   standard dbUnit table data XML file. The data in this file is inserted into the in-memory database after its 
   schema is created, but before the test is run. 

3. An `expected_[testMethodName].xml` file. This is repeated for each test method within the test, and contains a 
   standard dbUnit table data XML file. The data in this file is assumed to contain the expected state in the 
   database after the operations in the test method are run.
     
A typical persistence.xml file has the following structure. Note that no provider class or JPA properties need be 
supplied, as these are programmatically calculated to fit the in-memory database: 

<pre class="brush: xml"><![CDATA[
    <persistence version="2.1"
             xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd">

    <persistence-unit name="InMemoryTestPU">

        <!-- ===============================================
          |  Nazgul Core Entities
          +============================================== -->
        <class>se.jguru.nazgul.core.persistence.model.NazgulEntity</class>
        <!-- ===============================================
          |  Organisation Model Entities
          +============================================== -->
        <class>se.mithlond.services.organisation.model.Category</class>
        <class>se.mithlond.services.organisation.model.Listable</class>
        <class>se.mithlond.services.organisation.model.Organisation</class>
        <class>se.mithlond.services.organisation.model.address.Address</class>
        <class>se.mithlond.services.organisation.model.address.CategorizedAddress</class>
        <class>se.mithlond.services.organisation.model.address.WellKnownAddressType</class>
        <class>se.mithlond.services.organisation.model.membership.Membership</class>
        <class>se.mithlond.services.organisation.model.membership.PersonalSettings</class>
        <class>se.mithlond.services.organisation.model.membership.Group</class>
        <class>se.mithlond.services.organisation.model.membership.GroupMembership</class>
        <class>se.mithlond.services.organisation.model.membership.GroupMembershipId</class>
        <class>se.mithlond.services.organisation.model.membership.guild.Guild</class>
        <class>se.mithlond.services.organisation.model.membership.guild.GuildMembership</class>
        <class>se.mithlond.services.organisation.model.membership.order.Order</class>
        <class>se.mithlond.services.organisation.model.membership.order.OrderLevel</class>
        <class>se.mithlond.services.organisation.model.membership.order.OrderLevelGrant</class>
        <class>se.mithlond.services.organisation.model.membership.order.OrderLevelGrantId</class>
        <class>se.mithlond.services.organisation.model.user.User</class>
    </persistence-unit>
    </persistence>
]]></pre>      
    
The typical setup file contains the data intended for insertion into the in-memory database, supplied in XML form.
This corresponds to the dbUnit `FlatXmlDataSet` structure. For convenience, at any time during the integration test, 
a call to the method `printCurrentDatabaseState();` or the method 
`String flatXmlForm = extractFlatXmlDataSet(iDatabaseConnection.createDataSet());` can retrieve the current in-memory
database state as a FlatXmlDataSet structure. (The former method uses an SLF4J logger to print the result onto the 
standard output stream. 
    
<pre class="brush: xml"><![CDATA[
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
  <ORGANISATION ID="1" BANKACCOUNTINFO="bankAccountInfo_0" EMAILSUFFIX="emailSuffix_0" ORGANISATIONNAME="name_0"
                PHONE="phone_0" POSTACCOUNTINFO="postAccountInfo_0" SUFFIX="suffix_0" VERSION="1"
                CAREOFLINE="careOfLine_0" CITY="city_0" COUNTRY="country_0" DEPARTMENTNAME="departmentName_0"
                DESCRIPTION="description_0" NUMBER="number_0" STREET="street_0" ZIPCODE="zipCode_0"/>
  <ORGANISATION ID="2" BANKACCOUNTINFO="bankAccountInfo_1" EMAILSUFFIX="emailSuffix_1" ORGANISATIONNAME="name_1"
                PHONE="phone_1" POSTACCOUNTINFO="postAccountInfo_1" SUFFIX="suffix_1" VERSION="1"
                CAREOFLINE="careOfLine_1" CITY="city_1" COUNTRY="country_1" DEPARTMENTNAME="departmentName_1"
                DESCRIPTION="description_1" NUMBER="number_1" STREET="street_1" ZIPCODE="zipCode_1"/>
  <ORGANISATION ID="3" BANKACCOUNTINFO="bankAccountInfo_2" EMAILSUFFIX="emailSuffix_2" ORGANISATIONNAME="name_2"
                PHONE="phone_2" POSTACCOUNTINFO="postAccountInfo_2" SUFFIX="suffix_2" VERSION="1"
                CAREOFLINE="careOfLine_2" CITY="city_2" COUNTRY="country_2" DEPARTMENTNAME="departmentName_2"
                DESCRIPTION="description_2" NUMBER="number_2" STREET="street_2" ZIPCODE="zipCode_2"/>
  <CATEGORIZEDADDRESS/>
  <CATEGORY/>
  <GROUPMEMBERSHIP/>
  <INTERNALGROUPS ID="1" DISCRIMINATOR="group" EMAILLIST="emailList_0" GROUPNAME="groupName_0" VERSION="1"
                  ORGANISATION_ID="1"/>
  <INTERNALGROUPS ID="2" DISCRIMINATOR="group" EMAILLIST="emailList_1" GROUPNAME="groupName_1" VERSION="1"
                  ORGANISATION_ID="2"/>
  <INTERNALGROUPS ID="3" DISCRIMINATOR="group" EMAILLIST="emailList_2" GROUPNAME="groupName_2" VERSION="1"
                  ORGANISATION_ID="3" PARENT_ID="2"/>
  <INTERNALGROUPS ID="4" DISCRIMINATOR="group" EMAILLIST="emailList_3" GROUPNAME="groupName_3" VERSION="1"
                  ORGANISATION_ID="1" PARENT_ID="3"/>
  <INTERNALGROUPS ID="5" DISCRIMINATOR="group" EMAILLIST="emailList_4" GROUPNAME="groupName_4" VERSION="1"
                  ORGANISATION_ID="2" PARENT_ID="4"/>
  <INTERNALGROUPS ID="6" DISCRIMINATOR="guild" EMAILLIST="emailList_0" GROUPNAME="guildName_0" VERSION="1"
                  ORGANISATION_ID="1" QUENYANAME="quenyaName_0" QUENYAPREFIX="quenyaPrefix_0"/>
  <INTERNALGROUPS ID="7" DISCRIMINATOR="guild" EMAILLIST="emailList_1" GROUPNAME="guildName_1" VERSION="1"
                  ORGANISATION_ID="2" QUENYANAME="quenyaName_1" QUENYAPREFIX="quenyaPrefix_1"/>
  <INTERNALGROUPS ID="8" DISCRIMINATOR="guild" EMAILLIST="emailList_2" GROUPNAME="guildName_2" VERSION="1"
                  ORGANISATION_ID="3" QUENYANAME="quenyaName_2" QUENYAPREFIX="quenyaPrefix_2"/>
  <INTERNALGROUPS ID="9" DISCRIMINATOR="guild" EMAILLIST="emailList_3" GROUPNAME="guildName_3" VERSION="1"
                  ORGANISATION_ID="1" QUENYANAME="quenyaName_3" QUENYAPREFIX="quenyaPrefix_3"/>
  <INTERNALGROUPS ID="10" DISCRIMINATOR="guild" EMAILLIST="emailList_4" GROUPNAME="guildName_4" VERSION="1"
                  ORGANISATION_ID="2" QUENYANAME="quenyaName_4" QUENYAPREFIX="quenyaPrefix_4"/>
  <MEMBERSHIP/>
  <MEMBERSHIP_GROUPMEMBERSHIP/>
  <NAZGULORDER/>
  <ORDERLEVEL/>
  <ORDERLEVELGRANT/>
  <PERSONALSETTINGS/>
  <PERSONALSETTINGS_SETTINGS/>
  <USER/>
  <USER_CONTACTDETAILS/>
</dataset>
]]></pre>
    
Update the expected file with the database changes expected to be done by the test. You are now done with the data 
setup for an EntityManager integration test.

## 5. Implement a test method

A somewhat commented test method is shown below. Compare its methods to the sequence diagram rendered above. 

<pre class="brush: java"><![CDATA[
    @Test
    public void validateEntityManagerMergingAndUpdating() throws Exception {

        // Assemble
        final IDataSet expected = performStandardTestDbSetup();

        // Act
        for(Group current : receivedFromClient) {

            // Can't use EntityManager.merge since data was changed during transport.
            // Instead, re-read from database and update using supplied data if required.
            //
            // This illustrates one of the bigger design flaws of JPA - EntityManager.merge
            // cannot be used when data has been changed during transport.
            // *sigh* - pointless much, yes?
            //
            // managedGroups.add(entityManager.merge(current));

            // a) Get the managed Group corresponding to the received/current one.
            final Group currentManaged = entityManager.find(Group.class, current.getId());

            // b) Check the version of the existing and the received object.
            //    Was the database version updated while the transported version was
            boolean notUpdatedInDatabase = currentManaged.getVersion() <= current.getVersion();
            boolean emailListUpdated = !currentManaged.getEmailList().equals(current.getEmailList());

            // c) Update the internal state of the managed object with the state of the transported one.
            if(notUpdatedInDatabase && emailListUpdated) {
                updateEmailList(currentManaged, current.getEmailList());
            }

            // d) Commit the transaction to flush the EntityManager to DB.
            commitAndStartNewTransaction();
        }

        final Group modifiedGroup = entityManager.createNamedQuery(Group.NAMEDQ_GET_BY_NAME_ORGANISATION, Group.class)
                .setParameter(Patterns.PARAM_GROUP_NAME, "groupName_0")
                .setParameter(Patterns.PARAM_ORGANISATION_NAME, "name_0")
                .getSingleResult();

        // Assert
        Assert.assertEquals("modifiedEmailList", modifiedGroup.getEmailList());
        Assert.assertEquals(2, modifiedGroup.getVersion());

        Assertion.assertEquals(expected, iDatabaseConnection.createDataSet());
    }
]]></pre>    