/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
package se.mithlond.services.organisation.api.transport.activity;

import org.apache.commons.lang3.Validate;
import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Convenience transport holder of Admission details.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"alias", "organisation", "note", "responsible", "activityID"})
@XmlAccessorType(XmlAccessType.FIELD)
public class AdmissionVO implements Comparable<AdmissionVO>, Serializable {

    /**
     * Default ActivityID value, indicating that this {@link AdmissionVO} instance is not yet
     * associated with an {@link se.mithlond.services.organisation.model.activity.Activity} in terms of database
     * persistence.
     */
    public static final Long UNINITIALIZED = -1L;

    /**
     * The JPA ID of an Activity to which this AdmissionDetails pertains.
     */
    @XmlAttribute
    private Long activityID;

    /**
     * The Alias of the Member to admit to an Activity.
     */
    @XmlElement(required = true)
    private String alias;

    /**
     * The Organisation name where the Alias exists; must not necessarily be
     * identical to the Organisation of the Activity to which this AdmissionDetails instance is tied.
     */
    @XmlElement(required = true)
    private String organisation;

    /**
     * An optional admission note from the admitted Alias to the organizers of the Activity.
     */
    @XmlElement
    private String note;

    /**
     * A boolean flag indicating if the Alias defines a Membership or Guild organizing the Activity.
     */
    @XmlAttribute
    private boolean responsible;

    /**
     * JAXB-friendly constructor.
     */
    public AdmissionVO() {
        activityID = UNINITIALIZED;
    }

    /**
     * Convenience constructor creating a new AdmissionDetails instance with an {@link #UNINITIALIZED} activityID.
     *
     * @param alias        The alias to admit.
     * @param organisation The organisation where the alias exists; must not necessarily be
     *                     identical to the organisation of the Activity to which this ProtoAdmission is tied.
     * @param note         An optional admission note from teh Alias to the organizer(s) of the Activity.
     * @param responsible  A boolean flag indicating if the Alias defines a Membership or Guild organizing the Activity.
     */
    public AdmissionVO(final String alias,
                            final String organisation,
                            final String note,
                            final boolean responsible) {

        this(UNINITIALIZED, alias, organisation, note, responsible);
    }

    /**
     * Creates a new ProtoAdmission instance, wrapping the supplied data.
     *
     * @param activityID   The JPA ID of the Activity for which these AdmissionDetails pertain.
     * @param alias        The alias to admit.
     * @param organisation The organisation where the alias exists; must not necessarily be
     *                     identical to the organisation of the Activity to which this ProtoAdmission is tied.
     * @param note         An optional admission note from teh Alias to the organizer(s) of the Activity.
     * @param responsible  A boolean flag indicating if the Alias defines a Membership or Guild organizing the Activity.
     */
    public AdmissionVO(final Long activityID,
                            final String alias,
                            final String organisation,
                            final String note,
                            final boolean responsible) {

        // Check sanity
        Validate.notEmpty(alias, "Cannot handle null or empty alias argument.");
        Validate.notEmpty(organisation, "Cannot handle null or empty organisation argument.");

        // Assign internal state
        this.activityID = activityID;
        this.alias = alias;
        this.organisation = organisation;
        this.note = note;
        this.responsible = responsible;
    }

    /**
     * @return The JPA ID of the Activity for which these AdmissionDetails pertain.
     * Can be {@code null} only if the Activity is not known (i.e. not created yet).
     */
    public Long getActivityID() {
        return activityID;
    }

    /**
     * @return The organisation where the alias exists; must not necessarily be
     * identical to the organisation of the Activity to which this ProtoAdmission is tied. Never null nor empty.
     */
    public String getOrganisation() {
        return organisation;
    }

    /**
     * @return The alias to admit.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return An optional admission note.
     */
    public Optional<String> getNote() {
        return note == null ? Optional.empty() : Optional.of(note);
    }

    /**
     * @return {@code true} if this ProtoAdmission alias is responsible for the activity to which it will be tied.
     */
    public boolean isResponsible() {
        return responsible;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object that) {

        // Fail fast
        if (this == that) {
            return true;
        }
        if (!(that instanceof AdmissionVO)) {
            return false;
        }

        // Delegate to internal state
        final AdmissionVO details = (AdmissionVO) that;
        return responsible == details.responsible
                && Objects.equals(activityID, details.activityID)
                && Objects.equals(alias, details.alias)
                && Objects.equals(organisation, details.organisation)
                && Objects.equals(note, details.note);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(activityID, alias, organisation, note, responsible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final AdmissionVO that) {

        // Check sanity
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Delegate to normal value
        int toReturn = getAlias().compareTo(that.getAlias());
        if (toReturn == 0) {
            toReturn = getOrganisation().compareTo(that.getOrganisation());
        }
        if (toReturn == 0) {

            final String thisNote = this.getNote().orElse("");
            final String thatNote = that.getNote().orElse("");
            toReturn = thisNote.compareTo(thatNote);
        }
        if (toReturn == 0) {
            if (isResponsible()) {
                toReturn = that.isResponsible() ? 0 : 1;
            } else {
                toReturn = that.isResponsible() ? -1 : 0;
            }
        }

        // All done.
        return toReturn;
    }
}
