/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-ejb
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
package se.mithlond.services.content.impl.ejb;

import org.joda.time.DateTimeZone;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;
import se.mithlond.services.shared.test.entity.AbstractIntegrationTest;

import java.lang.reflect.Field;
import java.util.TimeZone;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractContentIntegrationTest extends AbstractIntegrationTest {

    // Shared state
    protected static final String ORG_MIFFLOND = "Mifflond";
    protected static final String ORG_FJODJIM = "Fjodjim";
    protected static final Long MIFFLOND_JPA_ID = 1L;
    protected static final Long FJODJIM_JPA_ID = 2L;

    /**
     * Default constructor, setting a Swedish DateTimeZone.
     */
    public AbstractContentIntegrationTest() {
        super(DateTimeZone.forTimeZone(TimeZone.getTimeZone(TimeFormat.SWEDISH_TIMEZONE)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        // Prime the PersistenceHelper, and setup the database
        PersistenceHelper.setEntityManager(entityManager);
        PersistenceHelper.doStandardSetup();
        commitAndStartNewTransaction();
        PersistenceHelper.setEntityManager(entityManager);
    }

    protected void injectEntityManager(final AbstractJpaService toBeInjected) {

        final String className = toBeInjected.getClass().getSimpleName();

        // Inject the EntityManager into the MembershipServiceBean.
        try {
            Field entityManagerField = AbstractJpaService.class.getDeclaredField("entityManager");
            entityManagerField.setAccessible(true);
            entityManagerField.set(toBeInjected, entityManager);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not inject EntityManager into " + className + ".", e);
        }
    }
}
