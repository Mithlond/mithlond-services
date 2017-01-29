/*
 * #%L
 * Nazgul Project: mithlond-services-backend-war
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.backend.war.customization;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.InheritancePolicy;
import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.internal.databaseaccess.Platform;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ManyToOneMapping;
import org.eclipse.persistence.platform.database.DatabasePlatform;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.GroupMembership;
import se.mithlond.services.organisation.model.membership.guild.Guild;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>Customizer for EclipseLink to handle missing RelationalDescriptors.</p>
 * <pre>
 *     2017-01-29 15:14:32,783 ERROR [org.jboss.as.ejb3.invocation] (default task-36)
 *     WFLYEJB0034: EJB Invocation failed on component MembershipServiceBean for method
 *     public abstract java.util.List se.mithlond.services.organisation.api.MembershipService
 *      .getMembershipsIn(java.lang.Long,boolean): javax.ejb.EJBException: javax.persistence.PersistenceException:
 *     Exception [EclipseLink-43] (Eclipse Persistence Services - 2.6.4.v20160829-44060b6):
 *     org.eclipse.persistence.exceptions.DescriptorException
 *
 *     Exception Description: Missing class for indicator field value [group] of type [class java.lang.String].
 *     Descriptor: RelationalDescriptor(se.mithlond.services.organisation.model.membership.GroupMembership -->
 *     [DatabaseTable(GROUPMEMBERSHIP)])
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EclipseLinkCustomizer implements SessionCustomizer {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(EclipseLinkCustomizer.class);

    /**
     * Default constructor.
     */
    public EclipseLinkCustomizer() {

        if (log.isInfoEnabled()) {
            log.info("Creating instance of [" + getClass().getSimpleName() + "] to customize EclipseLink Sessions.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void customize(final Session session) throws Exception {

        final DatabaseLogin login = session.getLogin();
        final Platform datasourcePlatform = session.getDatasourcePlatform();

        // Amend the GroupMembership RelationalDescriptor to
        // cope with the values within the DiscriminatorColumn, as found
        // within Group and Guild.
        final RelationalDescriptor groupMembershipDescriptor =
                (RelationalDescriptor) session.getClassDescriptor(GroupMembership.class);

        // Introspect a little.
        final Map<String, DatabaseMapping> attributeMappings = new TreeMap<>();
        groupMembershipDescriptor
                .getMappings()
                .forEach(current -> attributeMappings.put(current.getAttributeName(), current));

        final StringBuilder tmp = new StringBuilder();
        attributeMappings.entrySet().forEach(entry ->
                tmp.append("\n Attribute [" + entry.getKey() + "]: " + entry.getValue().toString()));
        log.info("Got attributeMappings for GroupMembership: "+ tmp.toString());

        // Attribute [group]: org.eclipse.persistence.mappings.ManyToOneMapping[group]
        final DatabaseMapping groupMapping = groupMembershipDescriptor.getMappingForAttributeName("group");
        if(groupMapping.isManyToOneMapping()) {

            final ManyToOneMapping m21Mapping = (ManyToOneMapping) groupMapping;
            
        }

        final Map groupMembershipIndicatorMapping = groupMembershipDescriptor
                .getInheritancePolicy()
                .getClassIndicatorMapping();


        // It would appear one needs to put both
        // forward mapping: [string] --> [class], and
        // reverse mapping: [class] --> [string]
        //
        // in this Map....
        /*
        groupMembershipIndicatorMapping.put("group", Group.class);
        groupMembershipIndicatorMapping.put(Group.class, "group");

        groupMembershipIndicatorMapping.put("guild", Guild.class);
        groupMembershipIndicatorMapping.put(Guild.class, "guild");
        */

        // Log after the augmentation ... 
        logRelationalDescriptorInformation(GroupMembership.class, session);
        logRelationalDescriptorInformation(Group.class, session);
        logRelationalDescriptorInformation(Guild.class, session);
    }

    //
    // Private helpers
    //

    private void logRelationalDescriptorInformation(final Class<?> theClass, final Session session) {

        final RelationalDescriptor classDescriptor = (RelationalDescriptor) session.getClassDescriptor(theClass);
        final String className = theClass.getCanonicalName();
        final InheritancePolicy policy = classDescriptor.getInheritancePolicy();

        final String logString = "=================== EclipseLink RelationalDescriptor Info ============"
                + "\n ClassName            : " + className
                + "\n InheritancePolicy    : " + policy.toString()
                + "\n AllTables            : " + policy.getAllTables().stream().reduce((l, r) -> ("" + l) + ", " +
                ("" + r)).orElse("<none>");

        log.info(logString);

        final Map<?, ?> classIndicatorMapping = policy.getClassIndicatorMapping();
        final AtomicInteger index = new AtomicInteger();
        final String classIndicatorLog = classIndicatorMapping.entrySet().stream()
                .filter(Objects::nonNull)
                .map(entry -> {

                    final String keyDesc = entry.getKey() + " (" + entry.getKey().getClass().getCanonicalName() + ")";
                    final String valueDesc = entry.getValue() + " (" + entry.getValue().getClass().getCanonicalName()
                            + ")";

                    return "\n " + index.getAndIncrement() + ": [" + keyDesc + "] --> " + valueDesc;
                }).reduce((l, r) -> l + " " + r).orElse("<none>");
        log.info("Got ClassIndicatorMapping: " + classIndicatorLog);
    }

    private void logDatabaseLoginInformation(final DatabaseLogin login) {

        final String logString = "=================== EclipseLink DatabaseLogin Info ============"
                + "\n ClassName            : " + login.getClass().getName()
                + "\n Database name        : " + login.getDatabaseName()
                + "\n DataSource name      : " + login.getDataSourceName()
                + "\n Uses streams binding : " + login.getUsesStreamsForBinding()
                + "\n Driver class name    : " + login.getDriverClassName()
                + "\n Db URL               : " + login.getDatabaseURL()
                + "\n=================== EclipseLink DatabaseLogin Info ============\n";

        log.info(logString);
    }

    private void logPlatformInformation(final DatabasePlatform platform) {

        final Class<byte[]> byteArrayClass = byte[].class;

        final String byteArrayConvertFromTypes = platform.getDataTypesConvertedFrom(byteArrayClass) == null
                ? "<None>"
                : "" + platform.getDataTypesConvertedFrom(byteArrayClass).stream()
                .map(a -> ((Class<?>) a).getName())
                .reduce((l, r) -> l + ", " + r)
                .orElse("<No classes>");

        final String byteArrayConvertToTypes = platform.getDataTypesConvertedTo(byteArrayClass) == null
                ? "<None>"
                : "" + platform.getDataTypesConvertedTo(byteArrayClass).stream()
                .map(a -> ((Class<?>) a).getName())
                .reduce((l, r) -> l + ", " + r)
                .orElse("<No classes>");

        final String logString = "=================== EclipseLink DatabasePlatform Info ============"
                + "\n ClassName             : " + platform.getClass().getName()
                + "\n Ping SQL              : " + platform.getPingSQL()
                + "\n byte[] from types     : " + byteArrayConvertFromTypes
                + "\n byte[] to types       : " + byteArrayConvertToTypes
                + "\n BLOB Jdbc typename    : " + platform.getJdbcTypeName(Types.BLOB)
                + "\n LONGVARBINARY typename: " + platform.getJdbcTypeName(Types.LONGVARBINARY)
                + "\n=================== EclipseLink DatabasePlatform Info ============\n";

        log.info(logString);
    }

    private void logDataSourcePlatformInformation(final Platform platform) {

        final String logString = "=================== EclipseLink DataSource Platform Info ============"
                + "\n ClassName             : " + platform.getClass().getName()
                + "\n=================== EclipseLink DatabasePlatform Info ============\n";

        log.info(logString);
    }
}
