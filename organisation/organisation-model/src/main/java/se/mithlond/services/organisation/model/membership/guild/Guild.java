/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.membership.guild;

import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.Group;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * Entity implementation for a Guild, belonging to an Organisation.
 * Guilds are Groups which have extra internal structure, namely a set of GuildMasters.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@DiscriminatorValue("guild")
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"quenyaName", "quenyaPrefix"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Guild extends Group {

    /**
     * The quenya name of this Guild, without any prefixes. Example: "Galabargian".
     */
    @Basic
    @XmlElement(nillable = true, required = false)
    private String quenyaName;

    /**
     * The quenya prefix of this Guild, without any names. Example: "Mellonath".
     */
    @Basic
    @Column(length = 64)
    @XmlElement(nillable = true, required = false)
    private String quenyaPrefix;

    /**
     * JPA/JAXB-friendly constructor.
     */
    public Guild() {
    }

    /**
     * Compound constructor creating a Guild object wrapping the supplied data.
     *
     * @param quenyaName   The quenya name of this Guild.
     * @param quenyaPrefix The quenya prefix of this Guild.
     * @param organisation The Organisation where this guild belongs.
     * @param guildName    The name of this Guild. Identical to group name for Guilds.
     * @param emailList    An optional electronic mail list which delivers elecronic mail to all members of this Group.
     *                     If the emailList property does not contain a full email address, the email suffix of the
     *                     Organisation will be appended to form the full mail address of
     *                     <code>[emailList]@[organisation email suffix]</code>.
     */
    public Guild(
            final String guildName,
            final String description,
            final Organisation organisation,
            final String emailList,
            final String quenyaName,
            final String quenyaPrefix) {

        // Delegate
        super(guildName, description, organisation, null, emailList);

        // Assign internal state
        this.quenyaName = quenyaName;
        this.quenyaPrefix = quenyaPrefix;
        setXmlID();
    }

    /**
     * @return The Quenya name of this Guild, without any prefixes. Example: "Galabargian".
     */
    public String getQuenyaName() {
        return quenyaName;
    }

    /**
     * @return The Quenya prefix of this Guild, without any names. Example: "Mellonath".
     */
    public String getQuenyaPrefix() {
        return quenyaPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Fail fast.
        if (this == o) {
            return true;
        }
        if (!(o instanceof Guild)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        // Delegate to internal state
        final Guild that = (Guild) o;
        return Objects.equals(quenyaName, that.quenyaName)
                && Objects.equals(quenyaPrefix, that.quenyaPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quenyaName, quenyaPrefix);
    }

    //
    // Private helpers
    //

    /**
     * Standard JAXB class-wide listener method, automagically invoked
     * immediately before this object is Marshalled.
     *
     * @param marshaller The active Marshaller.
     */
    @SuppressWarnings("all")
    private void beforeMarshal(final Marshaller marshaller) {
        setXmlID();
    }

    private void setXmlID() {
        this.xmlID = "guild_"
                + getOrganisation().getOrganisationName().replaceAll("\\s+", "_")
                + "_" + getGroupName().trim().replaceAll("\\s+", "_");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getXmlID() {
        setXmlID();
        return super.getXmlID();
    }
}
