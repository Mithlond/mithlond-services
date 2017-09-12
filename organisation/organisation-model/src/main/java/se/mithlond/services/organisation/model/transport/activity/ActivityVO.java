/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.transport.activity;

import se.jguru.nazgul.core.algorithms.api.Validate;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.XmlIdHolder;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.transport.OrganisationVO;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Transport object containing data required for creating or modifying Activity state.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE,
        propOrder = {"organisation", "shortDesc", "fullDesc", "startTime", "endTime",
                "isOpenToGeneralPublic", "addressCategory", "addressShortDescription", "location",
                "cancelled", "cost", "lateAdmissionCost", "lateAdmissionDate", "lastAdmissionDate",
                "dressCode", "responsibleGroupName", "admissions"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ActivityVO extends AbstractSimpleTransportable implements XmlIdHolder {

    /**
     * The Organisation owning this Activity. (Create: Mandatory).
     */
    @XmlIDREF
    @XmlElement(required = true)
    private OrganisationVO organisation;

    /**
     * The short description of this activity, visible in listings. (Create: Mandatory).
     */
    @XmlElement(required = true)
    private String shortDesc;

    /**
     * The full description of this activity, visible in detailed listings. (Create: Mandatory).
     */
    @XmlElement
    private String fullDesc;

    /**
     * The start time of the Activity. (Create: Mandatory).
     */
    @XmlAttribute
    private LocalDateTime startTime;

    /**
     * The end time of the Activity. Must be after {@link #startTime}. (Create: Mandatory).
     */
    @XmlAttribute
    private LocalDateTime endTime;

    /**
     * The cost of attending the Activity. Must not be negative.
     */
    @Min(value = 0L, message = "Kostnaden för en aktivitet kan inte vara negativ.")
    @XmlElement
    private Amount cost;

    /**
     * The cost for Memberships of this Activity, if admitted after the {@link #lateAdmissionDate}.
     * Optional, but recommended to be higher than the {@link #cost}.
     */
    @XmlElement
    private Amount lateAdmissionCost;

    /**
     * The date before which admitting to the Activty costs {@link #cost}.
     * After this date, admissions to this Activity costs {@link #lateAdmissionCost}. (Create: Mandatory).
     */
    @XmlElement
    private LocalDate lateAdmissionDate;

    /**
     * The last date of admissions to the Activity. (Create: Mandatory).
     */
    @XmlElement
    private LocalDate lastAdmissionDate;

    /**
     * {@code true} to indicate that this Activity is cancelled.
     */
    @XmlAttribute(required = true)
    private boolean cancelled;

    /**
     * An optional dress code description for the Activity.
     */
    @XmlElement
    private String dressCode;

    /**
     * The address-classification category of the address supplied. (Create: Mandatory).
     */
    @XmlAttribute
    private String addressCategory;

    /**
     * The location of the Activity. (Create: Mandatory).
     */
    @XmlElement
    private Address location;

    /**
     * The short description of the location for this Activity, such as "Stadsbiblioteket". (Create: Mandatory).
     */
    @XmlElement
    private String addressShortDescription;

    /**
     * The name of the Group organizing this Activity. Optional.
     */
    @XmlElement
    private String responsibleGroupName;

    /**
     * A Set holding all initial admissions to the activity to create. Optional,
     * but should contain at least one responsible AdmissionVO, unless a Group
     * organizes the Activity.
     */
    @XmlElement
    private Set<AdmissionVO> admissions;

    /**
     * If {@code true}, the activity is flagged as being open to the general public
     * (as opposed to being available to members of the supplied organisation only).
     */
    @XmlAttribute
    private boolean isOpenToGeneralPublic;

    /**
     * JAXB-friendly constructor
     */
    public ActivityVO() {
        admissions = new TreeSet<>();
    }

    /**
     * Compound constructor creating an ActivityVO containing the supplied data.
     *
     * @param jpaID                   The JPA ID for the entity corresponding to this {@link ActivityVO}.
     *                                Use {@code null} to indicate that this {@link ActivityVO} does not correspond to
     *                                a (known) Entity within the database.
     * @param organisation            The Organisation owning this Activity. (Create: Mandatory).
     * @param shortDesc               The short description of this activity, visible in listings. (Create: Mandatory).
     * @param fullDesc                The full description of this activity, visible in detailed listings. (Create: Mandatory).
     * @param startTime               The start time of the Activity. (Create: Mandatory).
     * @param endTime                 The end time of the Activity. (Create: Mandatory).
     * @param cost                    The optional cost of admitting to this Activity. (Must not be negative).
     * @param lateAdmissionCost       The cost for attending this Activity, if admitted after the
     *                                {@link #lateAdmissionDate}. Optional, but recommended to be higher than
     *                                the {@link #cost}.
     * @param lateAdmissionDate       The date before which admitting to the Activty costs {@link #cost}.
     *                                After this date, admissions to this Activity costs {@link #lateAdmissionCost}.
     *                                (Create: Mandatory).
     * @param lastAdmissionDate       The last date of admissions to the Activity. (Create: Mandatory).
     * @param cancelled               {@code true} to indicate that this Activity is cancelled.
     * @param dressCode               An optional dress code description for the Activity.
     * @param addressCategory         The address-classification category of the address supplied. (Create: Mandatory).
     * @param location                The location of the Activity. (Create: Mandatory).
     * @param addressShortDescription The short description of the location for this Activity, such as
     *                                "Stadsbiblioteket". (Create: Mandatory).
     * @param responsibleGroupName    The name of the Group organizing this Activity. Optional.
     * @param isOpenToGeneralPublic   If {@code true}, the activity is flagged as being open to the general public
     *                                (as opposed to being available to members of the supplied organisation only).
     */
    public ActivityVO(final Long jpaID,
                      final OrganisationVO organisation,
                      final String shortDesc,
                      final String fullDesc,
                      final LocalDateTime startTime,
                      final LocalDateTime endTime,
                      final Amount cost,
                      final Amount lateAdmissionCost,
                      final LocalDate lateAdmissionDate,
                      final LocalDate lastAdmissionDate,
                      final boolean cancelled,
                      final String dressCode,
                      final String addressCategory,
                      final Address location,
                      final String addressShortDescription,
                      final String responsibleGroupName,
                      final boolean isOpenToGeneralPublic) {

        super(jpaID);

        // Check sanity
        this.organisation = Validate.notNull(organisation, "organisation");
        this.shortDesc = Validate.notEmpty(shortDesc, "shortDesc");
        this.cost = Validate.notNull(cost, "cost");

        // Assign internal state
        this.admissions = new TreeSet<>();
        this.fullDesc = fullDesc;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lateAdmissionCost = lateAdmissionCost;
        this.lateAdmissionDate = lateAdmissionDate;
        this.lastAdmissionDate = lastAdmissionDate;
        this.cancelled = cancelled;
        this.dressCode = dressCode;
        this.addressCategory = addressCategory;
        this.location = location;
        this.addressShortDescription = addressShortDescription;
        this.responsibleGroupName = responsibleGroupName;
        this.isOpenToGeneralPublic = isOpenToGeneralPublic;
    }

    /**
     * Copy constructor to turn an {@link Activity} into an {@link ActivityVO}.
     *
     * @param activity a non-null {@link Activity}.
     */
    public ActivityVO(final Activity activity) {

        // Check sanity
        Validate.notNull(activity, "activity");

        // Assign internal state
        initialize(activity.getId());

        this.organisation = new OrganisationVO(activity.getOwningOrganisation());
        this.shortDesc = Validate.notEmpty(activity.getShortDesc(), "shortDesc");
        this.fullDesc = Validate.notEmpty(activity.getFullDesc(), "fullDesc");

        this.startTime = activity.getStartTime();
        this.endTime = activity.getEndTime();
        this.lateAdmissionDate = activity.getLateAdmissionDate();
        this.lastAdmissionDate = activity.getLastAdmissionDate();
        this.cost = activity.getCost();
        this.lateAdmissionCost = activity.getLateAdmissionCost();

        this.addressCategory = activity.getAddressCategory().getCategoryID();
        this.addressShortDescription = activity.getAddressShortDescription();
        this.location = activity.getLocation();

        this.cancelled = activity.isCancelled();
        this.isOpenToGeneralPublic = activity.isOpenToGeneralPublic();
        this.dressCode = activity.getDressCode();

        this.responsibleGroupName = activity.getResponsible() == null
                ? null
                : activity.getResponsible().getGroupName();
        this.admissions = new TreeSet<>();
        this.admissions.addAll(activity.getAdmissions()
                .stream()
                .filter(Objects::nonNull)
                .map(AdmissionVO::new)
                .collect(Collectors.toList()));
    }

    /**
     * @return The Organisation owning this Activity.
     */
    public OrganisationVO getOrganisation() {
        return organisation;
    }

    /**
     * @return The short description of this activity, visible in listings. (Create: Mandatory).
     */
    public String getShortDesc() {
        return shortDesc;
    }

    /**
     * @return The full description of this activity, visible in detailed listings. (Create: Mandatory).
     */
    public String getFullDesc() {
        return fullDesc;
    }

    /**
     * @return The start time of the Activity. (Create: Mandatory).
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * @return The end time of the Activity. (Create: Mandatory).
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * @return The optional cost of admitting to this Activity. (Must not be negative).
     */
    public Amount getCost() {
        return cost;
    }

    /**
     * @return The cost for attending this Activity, if admitted after the {@link #getLateAdmissionDate()}.
     * Optional, but recommended to be higher than the {@link #getCost()}.
     */
    public Amount getLateAdmissionCost() {
        return lateAdmissionCost;
    }

    /**
     * @return The date before which admitting to the Activty costs {@link #getCost()}.
     * After this date, admissions to this Activity costs {@link #getLateAdmissionCost()}.
     * (Create: Mandatory).
     */
    public LocalDate getLateAdmissionDate() {
        return lateAdmissionDate;
    }

    /**
     * @return The date before which admitting to the Activty costs {@link #getCost()}.
     * After this date, admissions to this Activity costs {@link #getLateAdmissionCost()}.
     * (Create: Mandatory).
     */
    public LocalDate getLastAdmissionDate() {
        return lastAdmissionDate;
    }

    /**
     * @return {@code true} to indicate that this Activity is cancelled.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * @return An optional dress code description for the Activity.
     */
    public String getDressCode() {
        return dressCode;
    }

    /**
     * @return The address-classification category of the address supplied. (Create: Mandatory).
     */
    public String getAddressCategory() {
        return addressCategory;
    }

    /**
     * @return The location of the Activity. (Create: Mandatory).
     */
    public Address getLocation() {
        return location;
    }

    /**
     * @return The short description of the location for this Activity, such as "Stadsbiblioteket". (Create: Mandatory).
     */
    public String getAddressShortDescription() {
        return addressShortDescription;
    }

    /**
     * @return The name of the Group organizing this Activity. Optional.
     */
    public String getResponsibleGroupName() {
        return responsibleGroupName;
    }

    /**
     * @return A Set holding all initial admissions to the activity to create. Optional,
     * but should contain at least one responsible AdmissionDetails, unless a Group
     * organizes the Activity.
     */
    public Set<AdmissionVO> getAdmissions() {
        return admissions;
    }

    /**
     * @return If {@code true}, the activity is flagged as being open to the general public
     * (as opposed to being available to members of the supplied organisation only).
     */
    public boolean isOpenToGeneralPublic() {
        return isOpenToGeneralPublic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        String admissionsText = "<none>";
        if (admissions != null) {
            admissionsText = admissions.stream().map(adm -> "[activityID: " + adm.getActivityID()
                    + ", admittedID: " + adm.getMembershipID()
                    + (adm.getAlias() != null ? ", admittedAlias: " + adm.getAlias() : "")
                    + ", organisation: " + adm.getOrganisation() + "]\n")
                    .reduce((l, r) -> l + " " + r)
                    .orElse("<none>");
        }

        return "ActivityVO{"
                + "organisation=" + (organisation != null ? organisation.getOrganisationName() : "<none>")
                + ", shortDesc='" + shortDesc + '\''
                + ", fullDesc='" + fullDesc + '\''
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + ", cost=" + cost
                + ", lateAdmissionCost=" + lateAdmissionCost
                + ", lateAdmissionDate=" + lateAdmissionDate
                + ", lastAdmissionDate=" + lastAdmissionDate
                + ", cancelled=" + cancelled
                + ", dressCode='" + dressCode + '\''
                + ", addressCategory='" + addressCategory + '\''
                + ", location=" + location
                + ", addressShortDescription='" + addressShortDescription + '\''
                + ", responsibleGroupName='" + responsibleGroupName + '\''
                + ", admissions=" + admissionsText
                + ", isOpenToGeneralPublic=" + isOpenToGeneralPublic
                + '}';
    }
}
