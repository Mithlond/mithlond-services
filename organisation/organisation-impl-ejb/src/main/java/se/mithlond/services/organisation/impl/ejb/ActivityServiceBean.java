package se.mithlond.services.organisation.impl.ejb;

import se.mithlond.services.organisation.api.ActivityService;
import se.mithlond.services.organisation.api.parameters.ActivitySearchParameters;
import se.mithlond.services.organisation.api.transport.AdmissionDetails;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.membership.Membership;

import javax.ejb.Stateless;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ActivityService Stateless EJB implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class ActivityServiceBean implements ActivityService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Activity> getActivities(final ActivitySearchParameters parameters) {
        return null;
    }

    @Override
    public Activity getActivity(final long activityID) {
        return null;
    }

    @Override
    public Activity createActivity(final String organisationName, final String shortDesc, final String fullDesc, final ZonedDateTime startTime, final ZonedDateTime endTime, final Amount cost, final Amount lateAdmissionCost, final LocalDate lateAdmissionDate, final LocalDate lastAdmissionDate, final boolean cancelled, final String dressCode, final String addressCategory, final Address location, final String addressShortDescription, final String responsibleGuildName, final Set<AdmissionDetails> admissions, final boolean isOpenToGeneralPublic) {
        return null;
    }

    @Override
    public Activity updateActivity(final long activityId, final String organisationName, final String shortDesc, final String fullDesc, final ZonedDateTime startTime, final ZonedDateTime endTime, final Amount cost, final Amount lateAdmissionCost, final LocalDate lateAdmissionDate, final LocalDate lastAdmissionDate, final boolean cancelled, final String dressCode, final String addressCategory, final Address location, final String addressShortDescription, final String responsibleGuildName, final Set<AdmissionDetails> admissions, final boolean isOpenToGeneralPublic) {
        return null;
    }

    @Override
    public boolean addOrAlterAdmission(final long activityId, final Membership membership, final boolean responsible, final String admissionNote, final boolean onlyUpdateAdmissionNote) {
        return false;
    }

    @Override
    public Map<Category, List<CategorizedAddress>> getActivityLocationAddresses(final String organisationName) {
        return null;
    }
}
