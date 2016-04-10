/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.activity;

import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.jguru.nazgul.tools.validation.api.expression.ExpressionBuilder;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Listable;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Entity class defining an Activity.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = Activity.NAMEDQ_GET_BY_ORGANISATION_AND_DATERANGE,
                query = "select a from Activity a "
                        + " where a.owningOrganisation.organisationName like :" + OrganisationPatterns.PARAM_ORGANISATION_NAME
                        + " and a.startTime between :" + OrganisationPatterns.PARAM_START_TIME
                        + " and :" + OrganisationPatterns.PARAM_END_TIME
                        + " order by a.startTime")
})
@Entity
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"cancelled", "responsible", "admissions",
        "startTime", "endTime", "cost", "currency", "lateAdmissionCost", "lateAdmissionDate",
        "lastAdmissionDate", "location", "addressCategory", "addressShortDescription", "dressCode",
        "openToGeneralPublic"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Activity extends Listable {

    /**
     * NamedQuery for getting Memberships by alias and organisation name.
     * Found Memberships are retrieved irrespective of their LoginPermitted flag.
     */
    public static final String NAMEDQ_GET_BY_ORGANISATION_AND_DATERANGE =
            "Activity.getByOrganisationAndDateRange";

    /**
     * The start time of the Activity. Never null.
     */
    @NotNull
    @Basic(optional = false)
    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Calendar startTime;

    /**
     * The end time of the Activity. Must not be null, and must also be after startTime.
     */
    @NotNull
    @Basic(optional = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    @XmlElement(required = false, nillable = true)
    private Calendar endTime;

    /**
     * The optional cost of the activity. Must not be negative.
     */
    @Min(value = 0, message = "Cannot handle negative 'cost'.")
    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(required = false)
    private BigDecimal cost;

    /**
     * The optional currency for the optional cost of the activity.
     */
    @Basic(optional = true)
    @Column(nullable = true)
    @XmlAttribute(required = false)
    private String currency;

    /**
     * The dress code of the activity, if applicable.
     */
    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(required = false)
    private String dressCode;

    /**
     * The cost if admission after the lateAdmissionDate.
     * Optional, but recommended to be higher than the (standard) cost.
     */
    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(required = false)
    private BigDecimal lateAdmissionCost;

    /**
     * The optional date before which the activity costs {@code cost}.
     * After this date, the activity admission costs {@code lateAdmissionCost}.
     */
    @Basic(optional = true)
    @Temporal(value = TemporalType.DATE)
    @XmlElement(required = false, nillable = true)
    private Calendar lateAdmissionDate;

    /**
     * The last date of admissions to the Activity.
     */
    @Basic(optional = true)
    @Temporal(value = TemporalType.DATE)
    @XmlElement(required = false, nillable = true)
    private Calendar lastAdmissionDate;

    /**
     * If 'true', the Activity is cancelled.
     */
    @NotNull
    @Basic @Column(nullable = false)
    @XmlElement(defaultValue = "false")
    private boolean cancelled = false;

    /**
     * The Category of the location where this Activity takes place.
     */
    @NotNull
    @ManyToOne(optional = false)
    @XmlElement(required = true, nillable = false)
    private Category addressCategory;

    /**
     * The short description of the location for this Activity, such as "Stadsbiblioteket".
     */
    @NotNull
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String addressShortDescription;

    /**
     * The location of the Activity. May not be null.
     */
    @NotNull
    @Embedded
    @XmlElement(required = true, nillable = false)
    private Address location;

    /**
     * The Guild organizing this Activity. Optional.
     */
    @XmlIDREF
    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @XmlElement(required = false, nillable = true)
    private Guild responsible;

    /**
     * All current Admissions to this Activity. May be empty - but not null.
     */
    @NotNull
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "activity")
    @XmlElementWrapper(name = "admissions", nillable = false, required = true)
    @XmlElement(name = "admission")
    private Set<Admission> admissions;

    /**
     * "true" to indicate that the supplied Activity is open to the general public.
     * Otherwise this Activity is open only to Admissions from known Memberships.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute(required = false)
    private boolean openToGeneralPublic;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Activity() {
    }

    /**
     * Compound constructor creating an Activity that wraps the provided data.
     * The {@code admissions} Set is created empty.
     *
     * @param shortDesc               The mandatory and non-empty short description of this activity, visible in listings.
     * @param fullDesc                The full description of this activity (up to 2048 chars), visible in detailed listings.
     *                                May not be null or empty.
     * @param startTime               The start time of the Activity. Must not be null.
     * @param endTime                 The end time of the Activity. Must not be null, and must also be after startTime.
     * @param cost                    The optional cost of the activity. Must not be negative.
     * @param lateAdmissionCost       The cost if admission after the lateAdmissionDate.
     *                                Optional, but recommended to be higher than the (standard) cost.
     * @param lateAdmissionDate       The date before which the activity costs {@code cost}.
     *                                After this date, the activity admission costs {@code lateAdmissionCost}.
     * @param lastAdmissionDate       The last date of admissions to the Activity.
     * @param cancelled               If <code>true</code>, the Activity is cancelled.
     * @param dressCode               The dress code of the activity, if applicable.
     * @param location                The location of the Activity.
     * @param addressShortDescription The short description of the location for this Activity,
     *                                such as "Stadsbiblioteket".
     * @param addressCategory         The Category of the location where this Activity takes place.
     * @param organisation            The organisation within which this Activity takes place.
     * @param responsible             The Guild organizing this Activity. Optional.
     * @param isOpenToGeneralPublic   {@code true} to indicate that the supplied Activity is open to the general
     *                                public, and not only open to Admissions from known Memberships.
     */
    public Activity(final String shortDesc,
            final String fullDesc,
            final ZonedDateTime startTime,
            final ZonedDateTime endTime,
            final Amount cost,
            final Amount lateAdmissionCost,
            final ZonedDateTime lateAdmissionDate,
            final ZonedDateTime lastAdmissionDate,
            final boolean cancelled,
            final String dressCode,
            final Category addressCategory,
            final Address location,
            final String addressShortDescription,
            final Organisation organisation,
            final Guild responsible,
            final boolean isOpenToGeneralPublic) {

        // Delegate
        super(shortDesc, fullDesc, organisation);

        // Assign internal state
        this.startTime = startTime == null ? null : GregorianCalendar.from(startTime);
        this.endTime = endTime == null ? null : GregorianCalendar.from(endTime);

        if (cost != null) {
            this.cost = cost.getValue();
            this.currency = cost.getCurrency().toString();
        }
        if (lateAdmissionCost != null) {
            this.lateAdmissionCost = lateAdmissionCost.getValue();
            if (this.currency == null) {
                this.currency = lateAdmissionCost.getCurrency().toString();
            } else {
                if (!lateAdmissionCost.getCurrency().toString().equals(this.currency)) {
                    throw new IllegalArgumentException("cost and lateAdmissionCost should use the same currency.");
                }
            }
        }

        if (currency == null) {
            currency = WellKnownCurrency.SEK.toString();
        }

        this.lateAdmissionDate = lateAdmissionDate == null ? null : GregorianCalendar.from(lateAdmissionDate);
        this.lastAdmissionDate = lastAdmissionDate == null ? null : GregorianCalendar.from(lastAdmissionDate);
        this.cancelled = cancelled;
        this.addressCategory = addressCategory;
        this.location = location;
        this.responsible = responsible;
        this.dressCode = dressCode;
        this.addressShortDescription = addressShortDescription;
        this.admissions = new TreeSet<>();
        this.openToGeneralPublic = isOpenToGeneralPublic;
    }

    /**
     * @return The start time of the Activity.
     */
    public ZonedDateTime getStartTime() {
        return ZonedDateTime.ofInstant(startTime.toInstant(), startTime.getTimeZone().toZoneId());
    }

    /**
     * @return The end time of the Activity.
     */
    public ZonedDateTime getEndTime() {
        return ZonedDateTime.ofInstant(endTime.toInstant(), endTime.getTimeZone().toZoneId());
    }

    /**
     * @return The optional cost of the activity. Never negative or null.
     */
    public Amount getCost() {
        return new Amount(cost, WellKnownCurrency.valueOf(currency));
    }

    /**
     * @return The cost if admission after the lateAdmissionDate.
     * Optional, but recommended to be higher than the (standard) cost.
     */
    public Amount getLateAdmissionCost() {
        return new Amount(lateAdmissionCost, WellKnownCurrency.valueOf(currency));
    }

    /**
     * @return The date before which attendance to this Activity costs {@code cost}.
     * After this date, the activity admission costs {@code lateAdmissionCost}
     */
    public LocalDate getLateAdmissionDate() {
        return lateAdmissionDate == null ? null : LocalDate.from(lastAdmissionDate.toInstant());
    }

    /**
     * @return The last date of admissions to the Activity.
     */
    public LocalDate getLastAdmissionDate() {
        return lastAdmissionDate == null ? null : LocalDate.from(lastAdmissionDate.toInstant());
    }

    /**
     * Defines if this Activity has been cancelled by an organizer.
     * Cancelled Activities are visible on calendar listings but shown as cancelled.
     *
     * @return If <code>true</code>, the Activity is cancelled.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * @return The location of the Activity.
     */
    public Address getLocation() {
        return location;
    }

    /**
     * @return The short description of this Activity.
     */
    public String getAddressShortDescription() {
        return addressShortDescription;
    }

    /**
     * @return The category of this activity's Address.
     */
    public Category getAddressCategory() {
        return addressCategory;
    }

    /**
     * @return The Guild organizing this Activity. May be {@code null}.
     */
    public Guild getResponsible() {
        return responsible;
    }

    /**
     * @return The Admissions to this Activity. Never null.
     */
    public Set<Admission> getAdmissions() {
        return admissions;
    }

    /**
     * Assigns the startTime of this Activity.
     *
     * @param startTime The start time of the Activity. Must not be null.
     */
    public void setStartTime(@NotNull final ZonedDateTime startTime) {

        // Check sanity
        final ZonedDateTime time = Objects.requireNonNull(startTime, "Cannot handle null 'startTime' argument.");

        // Convert and assign
        this.startTime = GregorianCalendar.from(time);
    }

    /**
     * Assigns the endTime of this Activity.
     *
     * @param endTime The start time of the Activity. Must not be null.
     */
    public void setEndTime(@NotNull final ZonedDateTime endTime) {

        // Check sanity
        final ZonedDateTime time = Objects.requireNonNull(endTime, "Cannot handle null 'endTime' argument.");

        // Convert and assign
        this.endTime = GregorianCalendar.from(time);
    }

    /**
     * Assigns the nominal cost of this Activity.
     *
     * @param cost The cost of this activity. Cannot be null.
     */
    public void setCost(@NotNull final Amount cost) {

        // Check sanity
        final Amount nonNull = Objects.requireNonNull(cost, "Cannot handle null 'cost' argument.");

        // Assign
        this.cost = nonNull.getValue();
        this.currency = nonNull.getCurrency().toString();
    }

    /**
     * Assigns the late admission cost of this Activity.
     *
     * @param lateAdmissionCost The late admission cost of this activity. Cannot be null or negative value.
     *                          The currency of the supplied lateAdmissionCost is ignored, as this Activity
     *                          will use the currency of the [nominal] cost.
     */
    public void setLateAdmissionCost(final Amount lateAdmissionCost) {

        this.lateAdmissionCost = Objects.requireNonNull(lateAdmissionCost,
                "Cannot handle null 'lateAdmissionCost' argument.").getValue();
    }

    /**
     * Assigns the late admission date of this Activity.
     *
     * @param lateAdmissionDate The date before which the activity costs {@code cost}.
     *                          After this date, the activity admission costs {@code lateAdmissionCost}.
     */
    public void setLateAdmissionDate(@NotNull final LocalDate lateAdmissionDate) {

        // Check sanity
        final LocalDate nonNull = Objects.requireNonNull(lateAdmissionDate,
                "Cannot handle null 'lateAdmissionDate' argument.");

        // Assign
        this.lateAdmissionDate = GregorianCalendar.from(nonNull.atStartOfDay(TimeFormat.SWEDISH_TIMEZONE));
    }

    /**
     * Assigns the lastAdmissionDate of this Activity.
     *
     * @param lastAdmissionDate The last date of admissions to the Activity.
     */
    public void setLastAdmissionDate(final LocalDate lastAdmissionDate) {

        // Check sanity
        final LocalDate nonNull = Objects.requireNonNull(lastAdmissionDate,
                "Cannot handle null 'lastAdmissionDate' argument.");

        // Assign
        this.lastAdmissionDate = GregorianCalendar.from(nonNull.atStartOfDay(TimeFormat.SWEDISH_TIMEZONE));
    }

    /**
     * Assigns the cancelled flag of this Activity.
     *
     * @param cancelled If <code>true</code>, the Activity is cancelled.
     */
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Assigns the location of this Activity.
     *
     * @param location The location of the Activity.
     */
    public void setLocation(final Address location) {
        this.location = Objects.requireNonNull(location, "Cannot handle null location argument.");
    }

    /**
     * Assigns the addressCategory of this Activity.
     *
     * @param addressCategory the addressCategory of this Activity.
     */
    public final void setAddressCategory(final Category addressCategory) {
        this.addressCategory = Objects.requireNonNull(addressCategory, "Cannot handle null addressCategory argument.");
    }

    /**
     * (Re-)assigns the addressShortDescription property.
     *
     * @param addressShortDescription The short description of the location for this Activity,
     *                                such as "Stadsbiblioteket".
     */
    public void setAddressShortDescription(final String addressShortDescription) {
        this.addressShortDescription = Objects.requireNonNull(
                addressShortDescription,
                "Cannot handle null or empty addressShortDescription argument.");
    }

    /**
     * Assigns the Guild organizing this Activity. Optional value.
     *
     * @param responsible The Guild organizing this Activity. Optional.
     */
    public void setResponsible(final Guild responsible) {
        this.responsible = responsible;
    }

    /**
     * @return retrieves the Dresscode for this Activity.
     */
    public String getDressCode() {
        return dressCode;
    }

    /**
     * Assigns the dressCode for this Activity.
     *
     * @param dressCode The dress code of the activity, if applicable. Cannot be null.
     */
    public void setDressCode(final String dressCode) {
        this.dressCode = Objects.requireNonNull(dressCode, "Cannot handle null dressCode argument.");
    }

    /**
     * @return {@code true} to indicate that the supplied Activity is open to the general
     * public, and not only open to the memberships within the supplied Organisation.
     */
    public boolean isOpenToGeneralPublic() {
        return openToGeneralPublic;
    }

    /**
     * Assigns the openToGeneralPublic flag.
     *
     * @param openToGeneralPublic {@code true} to indicate that the supplied Activity is open to the general
     *                            public, and not only open to the memberships within the supplied Organisation.
     */
    public void setOpenToGeneralPublic(final boolean openToGeneralPublic) {
        this.openToGeneralPublic = openToGeneralPublic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateListableEntityState() throws InternalStateValidationException {

        // Don't permit null or empty values unnecessarily.
        InternalStateValidationException.create()
                .notNull(startTime, "startTime")
                .notNull(endTime, "endTime")
                .notNull(addressCategory, "addressCategory")
                .notNullOrEmpty(addressShortDescription, "addressShortDescription")
                .endExpressionAndValidate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        int numAdmissions = 0;
        String admissionPrintout = "<No Admissions>";

        if (getAdmissions() != null) {

            // Assign the number of Admissions
            numAdmissions = getAdmissions().size();

            // Compile a detailed list of the admissions
            StringBuilder admissions = new StringBuilder();
            for (Admission current : getAdmissions()) {
                admissions.append(current.toString()).append("\n");
            }
            admissionPrintout = admissions.toString();
        }

        return "Activity [" + getId() + "] " + getShortDesc()
                + "\n" + getFullDesc()
                + "\n Start: " + TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(getStartTime())
                + ", End: " + TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(getEndTime())
                + ".\nDressCode: " + getDressCode()
                + "\nCost: " + getCost() + " until " + TimeFormat.YEAR_MONTH_DATE.print(getLateAdmissionDate())
                + "\n... and " + getLateAdmissionCost() + " thereafter."
                + "\nLast Admission Date: " + TimeFormat.YEAR_MONTH_DATE.print(getLastAdmissionDate())
                + "\nLocation (" + getAddressShortDescription() + "): " + getLocation()
                + "\n - category: " + getAddressCategory()
                + "\n" + numAdmissions + " admissions.....\n"
                + admissionPrintout;
    }

    //
    // Private helpers
    //

    @PrePersist
    protected void validateStateBeforePersisting() {

        // Ensure we can evaluate the state here.
        final ExpressionBuilder expressionBuilder = InternalStateValidationException.create();

        if (responsible == null) {

            // No responsible guild. Ensure that we have some organizer.
            expressionBuilder.notNullOrEmpty(admissions, "admissions");

            boolean responsibleFound = false;
            for (Admission current : admissions) {
                if (current.isResponsible()) {
                    responsibleFound = true;
                    break;
                }
            }

            if (!responsibleFound) {
                expressionBuilder.addDescription("No organizer found among the admissions: " + admissions);
            }
        }

        // All done.
        expressionBuilder.endExpressionAndValidate();
    }

    /**
     * JAXB callback method invoked after this instance is Unmarshalled.
     * This is the gracious JAXB instantiation sledge hammer...
     *
     * @param unmarshaller The unmarshaller used to perform the unmarshalling.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

        // Since the Admissions has an XmlTransient Activity,
        // we need to re-assign it...
        if (getAdmissions() != null) {
            for (Admission current : getAdmissions()) {
                current.setActivity(this);
            }
        }
    }
}
