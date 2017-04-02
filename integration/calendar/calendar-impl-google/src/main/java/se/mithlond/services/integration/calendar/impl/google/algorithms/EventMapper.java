/*
 * #%L
 * Nazgul Project: mithlond-services-integration-calendar-impl-google
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
package se.mithlond.services.integration.calendar.impl.google.algorithms;

import com.google.api.services.calendar.model.Event;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.algorithms.diff.DiffHolder;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;

/**
 * Simple holder type for equivalent Events and Activities.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EventMapper implements Comparable<EventMapper>, DiffHolder<Event, Activity, EventMapper> {

    // Internal state
    private Event event;
    private Activity activity;

    /**
     * Constructs an EventMapper wrapping the non-null Event.
     *
     * @param event an Event to wrap within this EventMapper.
     */
    public EventMapper(@NotNull final Event event) {
        this.event = Validate.notNull(event, "event");
    }

    /**
     * Constructas an EventMapper wrapping the non-null Activity.
     *
     * @param activity an Activity to wrap within this EventMapper.
     */
    public EventMapper(@NotNull final Activity activity) {
        this.activity = Validate.notNull(activity, "activity");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Event> getActual() {
        return event == null ? Optional.empty() : Optional.of(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Activity> getComparison() {
        return activity == null ? Optional.empty() : Optional.of(activity);
    }

    /**
     * (Re-)assigns the Activity of this {@link EventMapper}. However, this operation will only succeed if the
     * activity is equal to the event or no Event is set within this EventMapper.
     *
     * @param activity An Activity to assign.
     * @see GoogleCalendarConverters#isEquivalentState(Event, Activity)
     */
    @Override
    public void setComparison(final Activity activity) {

        // Check sanity
        final Activity effectiveActivity = Validate.notNull(activity, "activity");

        final boolean eventPresentButEquivalentState = this.activity == null
                && this.event != null
                && GoogleCalendarConverters.isEquivalentState(this.event, activity);
        final boolean noEventPresent = this.event == null;

        if (noEventPresent || eventPresentButEquivalentState) {

            // All OK. Assign the activity to the internal state.
            this.activity = effectiveActivity;
        }
    }

    /**
     * (Re-)assigns the Event of this {@link EventMapper}. However, this operation will only succeed if the
     * event has state which equates activity or no Activity is set within this EventMapper.
     *
     * @param event An Event to assign to this EventMapper.
     * @see GoogleCalendarConverters#isEquivalentState(Event, Activity)
     */
    @Override
    public void setActual(final Event event) {

        // Check sanity
        final Event effectiveEvent = Validate.notNull(event, "event");

        final boolean activityPresentButEquivalentState = this.event == null
                && this.activity != null
                && GoogleCalendarConverters.isEquivalentState(event, this.activity);
        final boolean noActivityPresent = this.activity == null;

        if (noActivityPresent || activityPresentButEquivalentState) {

            // All OK. Assign the event to internal state.
            this.event = effectiveEvent;
        }
    }

    /**
     * Retrieves the Event within this EventMapper.
     *
     * @return An optional (i.e. nullable) Event of this EventMapper.
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Retrieves the Activity within this EventMapper.
     *
     * @return An optional (i.e. nullable) Activity of this EventMapper.
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Fail fast
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        // Delegate to internal state
        final EventMapper that = (EventMapper) o;
        return Objects.equals(event, that.event)
                && Objects.equals(activity, that.activity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(event, activity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final EventMapper that) {

        // Fail fast
        if (that == null) {
            return -1;
        }

        // Delegate to internal state
        int toReturn = -1;

        final boolean thisHasEvent = this.getEvent() != null;
        final boolean thatHasEvent = that.getEvent() != null;
        final boolean thisHasActivity = this.getActivity() != null;
        final boolean thatHasActivity = that.getActivity() != null;

        if (thisHasEvent && !thatHasEvent) {
            toReturn = -1;
        } else if (!thisHasEvent && !thatHasEvent) {
            toReturn = 1;
        } else if (thisHasEvent && thatHasEvent) {

            final long leftValue = this.getEvent().getStart().getDate().getValue();
            final long rightValue = that.getEvent().getStart().getDate().getValue();

            toReturn = (int) (leftValue - rightValue);
        }

        if (toReturn == 0) {

            if (thisHasActivity && !thatHasActivity) {
                toReturn = -1;
            } else if (!thisHasActivity && !thatHasActivity) {
                toReturn = 1;
            } else if (thisHasActivity && thatHasActivity) {
                toReturn = this.getActivity().getStartTime().compareTo(that.getActivity().getEndTime());
            }
        }

        // All Done.
        return toReturn;
    }
}
