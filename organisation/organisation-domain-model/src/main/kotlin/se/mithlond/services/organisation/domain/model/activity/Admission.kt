/*-
 * #%L
 * Nazgul Project: mithlond-services-organisation-domain-model
 * %%
 * Copyright (C) 2018 jGuru Europe AB
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
package se.mithlond.services.organisation.domain.model.activity

import se.mithlond.services.organisation.domain.model.membership.Membership
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.ForeignKey
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.PrePersist
import javax.persistence.Table

/**
 * Compound key definition for Admissions.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
data class AdmissionId(

        @field:Column(name = "activity_id")
        var activityId: Long,

        @field:Column(name = "admitted_id")
        var membershipId: Long

) : Serializable

/**
 * Entity implementation for an Admission to an Activity.
 *
 * @param admissionId The Compound JPA ID of this Admission
 * @param admitted The Membership admitted to the corresponding Activity.
 * @param activity The [Activity] to which the [Membership] is admitted.
 * @param admittedBy An Optional Membership indicating who created this Admission, if not the admitted Membership itself.
 * @param admissionTimestamp The timestamp when this Admission was created.
 * @param lastModifiedAt An Optional timestamp when this Admission was last modified.
 * @param admissionNote An optional note of this Admission.
 * @param responsible If "true" the admitted Membership is considered responsible for the activity to which this
 * Admission is linked.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations")
data class Admission @JvmOverloads constructor(

        @field:EmbeddedId
        var id: AdmissionId,

        @field:MapsId("activityId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.DETACH])
        @field:JoinColumn(
                name = "activity_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_admission_activity"))
        var activity: Activity,

        @field:MapsId("membershipId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.REFRESH])
        @field:JoinColumn(
                name = "admitted_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_admission_membership"))
        var admitted: Membership,

        @field:ManyToOne(optional = true)
        @field:JoinColumn(
                name = "admitted_by_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_admission_admitter"))
        var admittedBy: Membership? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var admissionTimestamp: LocalDateTime,

        @field:Basic
        @field:Column
        private var lastModifiedAt: LocalDateTime? = null,

        @field:Basic
        @field:Column
        var admissionNote: String? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var responsible: Boolean = false

) : Serializable, Comparable<Admission> {

    /**
     * Compound constructor. Should only be called from within non-framework code.
     */
    constructor(activity: Activity,
                admitted: Membership,
                admittedBy: Membership?,
                admissionTimestamp: LocalDateTime = LocalDateTime.now(),
                admissionNote: String? = null,
                responsible: Boolean = false
    ) : this(
            AdmissionId(-1, -1),
            activity,
            admitted,
            admittedBy,
            admissionTimestamp,
            null,
            admissionNote,
            responsible)

    init {
        synchronizeKeyValues()
    }

    /**
     * Copies the ID values of the Membership and Template to the respective PK columns.
     */
    @PrePersist
    fun synchronizeKeyValues() {

        // Harmonize the state of the PK
        this.id.membershipId = this.admitted.id!!
        this.id.activityId = this.activity.id!!
    }

    override fun compareTo(other: Admission): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}