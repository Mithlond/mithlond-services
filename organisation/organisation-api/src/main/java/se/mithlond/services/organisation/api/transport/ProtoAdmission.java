/*
 * #%L
 * Nazgul Project: mithlond-finance-api
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
package se.mithlond.services.organisation.api.transport;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;

/**
 * Convenience holder of Admission details before having created an Admission.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ProtoAdmission implements Comparable<ProtoAdmission>, Serializable {

    // Internal state
    private String alias;
    private String organisation;
    private String note;
    private boolean responsible;

    /**
     * Creates a new ProtoAdmission instance, wrapping the supplied data.
     *
     * @param alias        The alias to admit.
     * @param organisation The organisation where the alias exists; must not necessarily be
     *                     identical to the organisation of the Activity to which this ProtoAdmission is tied.
     * @param note         An optional admission note.
     * @param responsible  The
     */
    public ProtoAdmission(final String alias,
                          final String organisation,
                          final String note,
                          final boolean responsible) {

        // Check sanity
        Validate.notEmpty(alias, "Cannot handle null or empty alias argument.");
        Validate.notEmpty(organisation, "Cannot handle null or empty organisation argument.");

        // Assign internal state
        this.alias = alias;
        this.organisation = organisation;
        this.note = note;
        this.responsible = responsible;
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
     * @return An optional admission note. May be {@code null}.
     */
    public String getNote() {
        return note;
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
    public int hashCode() {
        final int responsibleHash = isResponsible() ? 1 : 0;
        final int noteHash = note == null ? 0 : note.hashCode();
        return alias.hashCode() + organisation.hashCode() + responsibleHash + noteHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final ProtoAdmission that) {

        // Check sanity
        if(that == null) {
            return -1;
        } else if(that == this) {
            return 0;
        }

        // Delegate to normal value
        int toReturn = getAlias().compareTo(that.getAlias());
        if(toReturn == 0) {
            toReturn = getOrganisation().compareTo(that.getOrganisation());
        }
        if(toReturn == 0) {

            final String thisNote = getNote() == null ? "" : getNote();
            final String thatNote = that.getNote() == null ? "" : that.getNote();
            toReturn = thisNote.compareTo(thatNote);
        }
        if(toReturn == 0) {
            if(isResponsible()) {
                toReturn = that.isResponsible() ? 0 : 1;
            } else {
                toReturn = that.isResponsible() ? -1 : 0;
            }
        }

        // All done.
        return toReturn;
    }
}
