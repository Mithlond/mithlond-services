/*
 * #%L
 * Nazgul Project: mithlond-services-shared-entity-test
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
package se.mithlond.services.shared.test.entity;

import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.test.persistence.StandardPersistenceTest;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;
import se.jguru.nazgul.test.xmlbinding.junit.StandardTimeZoneRule;

import javax.persistence.EntityManager;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract superclass for JAXB- and JPA-related tests using a standard TimeZone for the
 * duration of each test method execution, as well as the framework from the
 * StandardPersistenceTest.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractIntegrationTest extends StandardPersistenceTest implements ExtendedJaxbOperations {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    /**
     * Active rule which assigns a standard DateTimeZone for the remainder of the tests,
     * executed within this AbstractStandardizedTimezoneTest.
     */
    @Rule public PlainJaxbContextRule jaxb;

    /**
     * Active rule which assigns a standard DateTimeZone for the remainder of the tests,
     * executed within this AbstractIntegrationTest.
     */
    @Rule public StandardTimeZoneRule standardTimeZoneRule;

    /**
     * The standard ClassLoader used to load the active test class.
     */
    protected ClassLoader standardClassLoader = getClass().getClassLoader();

    /**
     * Compound constructor, accepting a non-null DateTimeZone to be set as default
     * during the test run. Also creates a PlainJaxbContextRule applied within the test.
     *
     * @param desiredTimeZone The DateTimeZone to be used when the test cases are executed.
     * @see AbstractStandardizedTimezoneTest#AbstractStandardizedTimezoneTest(DateTimeZone)
     */
    protected AbstractIntegrationTest(final DateTimeZone desiredTimeZone) {

        // Delegate
        super();

        // Assign internal state
        this.standardTimeZoneRule = new StandardTimeZoneRule(desiredTimeZone);
        this.jaxb = new PlainJaxbContextRule();
    }

    /**
     * Default constructor, which creates an AbstractPlainJaxbTest using the {@code DateTimeZone.UTC} timezone.
     *
     * @see AbstractStandardizedTimezoneTest#AbstractStandardizedTimezoneTest()
     */
    protected AbstractIntegrationTest() {

        // Delegate
        this(DateTimeZone.UTC);
    }

    /**
     * <p>Adds a call to the {@link #resetIdCounters(EntityManager)} method in addition to
     * performing normal cleanup mechanics after running each test.</p>
     *
     * @param shutdownDatabase if {@code true}, shuts down the database in addition to cleaning the test schema.
     */
    @Override
    protected void cleanupTestSchema(final boolean shutdownDatabase) {

        try {
            super.cleanupTestSchema(shutdownDatabase);
        } catch (Exception e) {
            // Ignore this
        }

        // Reset all ID counters.
        resetIdCounters(entityManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String marshalToXML(final Object... toMarshal) {
        return jaxb.marshal(standardClassLoader, false, toMarshal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String marshalToJSon(final Object... toMarshal) {
        return jaxb.marshal(standardClassLoader, false, toMarshal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T unmarshalFromXML(final Class<T> expectedReturnType, final String toUnmarshal) {
        return jaxb.unmarshal(standardClassLoader, false, expectedReturnType, toUnmarshal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T unmarshalFromJSON(final Class<T> expectedReturnType, final String toUnmarshal) {
        return jaxb.unmarshal(standardClassLoader, true, expectedReturnType, toUnmarshal);
    }

    /**
     * Convenience method to print the current database state onto a flat XML form.
     */
    protected void printCurrentDatabaseState() {

        try {
            String flatXmlForm = extractFlatXmlDataSet(iDatabaseConnection.createDataSet());
            log.info(flatXmlForm);
        } catch (SQLException e) {
            log.error("Could not acquire flat XML form of current DB IDataset", e);
        }
    }

    /**
     * Resets all identity counters within the "PUBLIC" schema accessed by the supplied EntityManager.
     *
     * @param entityManager A non-null EntityManager.
     * @see #entityManager
     */
    @SuppressWarnings("all")
    public static void resetIdCounters(final EntityManager entityManager) {

        if (entityManager != null) {

            // #1) Extract the raw JDBC Connection from the EntityManager
            final Connection connection = entityManager.unwrap(Connection.class);

            // #2) Hit the DB with the crude but efficient SQL statement.
            final String resetSql = "TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK";

            try {
                connection.createStatement().execute(resetSql);
            } catch (SQLException e) {
                throw new IllegalArgumentException("Could not reset the ID Counters", e);
            }
        }
    }

    /**
     * Validates that the expected and actual XML-formatted strings are
     * identical, ignoring any metaClass differences.
     *
     * @param expected The expected XML.
     * @param actual   The actual, received XML.
     */
    @SuppressWarnings("all")
    protected void validateIdenticalContent(final String expected, final String actual) {
        AbstractPlainJaxbTest.validateIdenticalXml(expected, actual);
    }
}
