/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
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
package se.mithlond.services.organisation.model.transport.activity;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.XmlIdHolder;
import se.mithlond.services.organisation.model.activity.Admission;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Convenience transport holder of Admission details.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE,
        propOrder = {"alias", "membershipID", "organisation", "note",
                "responsible", "admissionTime", "lastModification", "admitted"})
@XmlAccessorType(XmlAccessType.FIELD)
public class AdmissionVO extends AbstractSimpleTransportable implements XmlIdHolder {

    /**
     * Default JPA ID value, indicating that this {@link AdmissionVO} instance is not yet
     * associated with an {@link se.mithlond.services.organisation.model.activity.Activity} in terms of database
     * persistence.
     */
    public static final Long UNINITIALIZED = -1L;

    /**
     * The JPA ID of the admitted Membership.
     */
    @XmlAttribute(required = true)
    private Long membershipID;

    /**
     * The Alias of the Member to admit to an Activity.
     */
    @XmlElement
    private String alias;

    /**
     * The Organisation name where the Alias exists; must not necessarily be
     * identical to the Organisation of the Activity to which this AdmissionDetails instance is tied.
     */
    @XmlElement(required = true)
    private String organisation;

    /**
     * The admission timestamp
     */
    @XmlAttribute
    private LocalDateTime admissionTime;

    /**
     * The admission modification timestamp.
     */
    @XmlAttribute
    private LocalDateTime lastModification;

    /**
     * An optional admission note from the admitted Alias to the organizers of the Activity.
     */
    @XmlElement
    private String note;

    /**
     * A boolean flag indicating if the Alias defines a Membership or Guild organizing the Activity.
     */
    @XmlAttribute
    private Boolean responsible;

    /**
     * A boolean flag indicating if this AdmissionVO indicates an admission or
     * the desire to revoke the admission corresponding to this AdmissionVO.
     */
    @XmlAttribute
    @Transient
    @SuppressWarnings("all")
    private Boolean admitted;

    /**
     * JAXB-friendly constructor.
     */
    public AdmissionVO() {
        membershipID = UNINITIALIZED;
    }

    /**
     * Copy constructor to turn an {@link Admission} into an {@link AdmissionVO}.
     *
     * @param admission a non-null {@link Admission} entity.
     */
    public AdmissionVO(@NotNull final Admission admission) {

        // Delegate
        this(admission.getActivity().getId(),
                admission.getAdmitted().getId(),
                admission.getAdmitted().getAlias(),
                admission.getActivity().getOwningOrganisation().getOrganisationName(),
                admission.getAdmissionTimestamp(),
                admission.getLastModifiedAt(),
                admission.getAdmissionNote(),
                admission.isResponsible());
    }

    /**
     * Compound constructor creating an AdmissionVO wrapping the supplied data.
     *
     * @param activityID       The JPA ID of the Activity.
     * @param membershipID     The JPA ID of the Membership.
     * @param alias            The Membership alias.
     * @param organisation     The Organisation name.
     * @param admissionTime    The admission timestamp.
     * @param lastModification The latest modification timestamp.
     * @param note             The optional note for the Admission.
     * @param responsible      The flag indicating if this AdmissionVO contains the responsible Membership.
     */
    public AdmissionVO(final Long activityID,
                       final Long membershipID,
                       final String alias,
                       final String organisation,
                       final LocalDateTime admissionTime,
                       final LocalDateTime lastModification,
                       final String note,
                       final boolean responsible) {

        // Delegate
        super(activityID);

        // Assign internal state
        this.membershipID = membershipID;
        this.alias = alias;
        this.organisation = organisation;
        this.admissionTime = admissionTime;
        this.lastModification = lastModification;
        this.note = note;
        this.responsible = responsible;
    }

    /**
     * @return The JPA ID of the Activity for which these AdmissionDetails pertain.
     * Can be {@code null} only if the Activity is not known (i.e. not created yet).
     */
    public Long getActivityID() {
        return super.getJpaID();
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
     * @return {@code true} if the Membership of this AdmissionVO is responsible for the activity
     * to which it is tied.
     */
    public boolean isResponsible() {
        return responsible == null ? false : responsible;
    }

    /**
     * The JPA ID of the Membership of this AdmissionVO.
     *
     * @return the JPA ID of the Membership of this AdmissionVO.
     */
    public Long getMembershipID() {
        return membershipID;
    }

    /**
     * @return The admission timestamp of this AdmissionVO.
     */
    public LocalDateTime getAdmissionTime() {
        return admissionTime;
    }

    /**
     * @return The last modification timestamp of this AdmissionVO.
     */
    public LocalDateTime getLastModification() {
        return lastModification;
    }

    /**
     * @return A boolean flag indicating if this AdmissionVO indicates an admission or the desire to
     * revoke the admission corresponding to this AdmissionVO.
     */
    public Boolean getAdmitted() {
        return admitted == null ? true : admitted;
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        // Delegate to internal state
        final AdmissionVO that = (AdmissionVO) o;
        return Objects.equals(getMembershipID(), that.getMembershipID())
                && Objects.equals(getAlias(), that.getAlias())
                && Objects.equals(getOrganisation(), that.getOrganisation())
                && Objects.equals(getAdmissionTime(), that.getAdmissionTime())
                && Objects.equals(getLastModification(), that.getLastModification())
                && Objects.equals(getNote(), that.getNote())
                && Objects.equals(isResponsible(), that.isResponsible());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        return Objects.hash(
                super.hashCode(),
                getMembershipID(),
                getAlias(),
                getOrganisation(),
                getAdmissionTime(),
                getLastModification(),
                getNote(),
                isResponsible());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final AbstractSimpleTransportable cmp) {

        if (cmp instanceof AdmissionVO) {

            final AdmissionVO that = (AdmissionVO) cmp;

            // Check sanity
            if (that == this) {
                return 0;
            }

            // Delegate to normal value
            int toReturn = (int) (getMembershipID() - that.getMembershipID());
            if (toReturn == 0) {
                final String thisAlias = this.getAlias() == null ? "" : this.getAlias();
                final String thatAlias = that.getAlias() == null ? "" : that.getAlias();
                toReturn = thisAlias.compareTo(thatAlias);
            }
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

        // Delegate.
        return super.compareTo(cmp);
    }

    /**
     * Standard JAXB class-wide listener method, automagically invoked
     * immediately before this object is Marshalled.
     *
     * @param marshaller The active Marshaller.
     */
    @SuppressWarnings("all")
    private void beforeMarshal(final Marshaller marshaller) {

        // Only send the non-required properties if they are non-null.
        //
        if (responsible != null && !responsible) {
            responsible = null;
        }
        if (note != null && note.trim().isEmpty()) {
            note = null;
        }
    }
}
