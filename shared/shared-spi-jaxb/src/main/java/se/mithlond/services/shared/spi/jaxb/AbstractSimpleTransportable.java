/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jaxb
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
package se.mithlond.services.shared.spi.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract definition for an object which should be transportable and still maintain a connection to
 * a standard database-persisted Entity using a Long for JPA ID. If Java 8 time and date classes are to be
 * marshalled, use the
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see se.mithlond.services.shared.spi.jaxb.adapter.LocalDateAdapter
 * @see se.mithlond.services.shared.spi.jaxb.adapter.LocalDateTimeAdapter
 * @see se.mithlond.services.shared.spi.jaxb.adapter.ZonedDateTimeAdapter
 */
@XmlType(namespace = SharedJaxbPatterns.NAMESPACE, propOrder = {"jpaID"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractSimpleTransportable implements Serializable, Comparable<AbstractSimpleTransportable> {

    /**
     * The JPA ID for the entity corresponding to this {@link AbstractSimpleTransportable}.
     * Use {@code null} to indicate that this {@link AbstractSimpleTransportable} does not correspond to
     * a (known) Entity within the database.
     */
    @XmlAttribute
    private Long jpaID;

    /**
     * JAXB-friendly constructor.
     */
    public AbstractSimpleTransportable() {
    }

    /**
     * Compound constructor creating an {@link AbstractSimpleTransportable} wrapping the supplied JPA ID.
     *
     * @param jpaID The JPA ID for the entity corresponding to this {@link AbstractSimpleTransportable}.
     *              Use {@code null} to indicate that this {@link AbstractSimpleTransportable} does not correspond to
     *              a (known) Entity within the database.
     */
    public AbstractSimpleTransportable(final Long jpaID) {
        this.jpaID = jpaID;
    }

    /**
     * Retrieves the JPAID of this {@link AbstractSimpleTransportable} instance.
     *
     * @return the JPAID of this {@link AbstractSimpleTransportable} instance. A {@code null} value indicates that
     * this {@link AbstractSimpleTransportable} does not correspond to a (known) Entity within the database.
     */
    public Long getJpaID() {
        return jpaID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + ", JpaID: " + jpaID + "]";
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
        if (!(o instanceof AbstractSimpleTransportable)) {
            return false;
        }

        // Delegate
        final AbstractSimpleTransportable that = (AbstractSimpleTransportable) o;
        return compareClassNamesAndJpaIDsTo(that) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getClass().getName(), jpaID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final AbstractSimpleTransportable that) {
        return compareClassNamesAndJpaIDsTo(that);
    }

    /**
     * Compares this and that {@link AbstractSimpleTransportable} instances for equality and sortability originating
     * only from the ClassNames and JPA IDs of the two instances.
     *
     * @param that An {@link AbstractSimpleTransportable} which may be {@code null}.
     * @return A comparison value between this and that.
     * @see Comparable#compareTo(Object)
     */
    protected final int compareClassNamesAndJpaIDsTo(final AbstractSimpleTransportable that) {

        // #0) Fail fast
        if (that == null) {
            return 1;
        } else if (that == this) {
            return 0;
        }

        // #1) Compare the class names
        int toReturn = getClass().getName().compareTo(that.getClass().getName());

        // #2) Compare the JPA IDs.
        if (toReturn == 0) {
            final Long thisJpaID = jpaID == null ? 0L : jpaID;
            final Long thatJpaID = that.jpaID == null ? 0L : that.jpaID;

            toReturn = thisJpaID.compareTo(thatJpaID);
        }

        // All Done.
        return toReturn;
    }
}
