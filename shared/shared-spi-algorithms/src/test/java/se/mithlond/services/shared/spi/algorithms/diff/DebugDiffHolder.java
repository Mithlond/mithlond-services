package se.mithlond.services.shared.spi.algorithms.diff;

import se.mithlond.services.shared.spi.algorithms.Validate;

import java.util.Optional;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DebugDiffHolder implements DiffHolder<String, StringBuffer, DebugDiffHolder> {

    // Internal state
    private String actual;
    private StringBuffer comparison;

    public DebugDiffHolder(final String actual, final StringBuffer comparison) {
        this.actual = actual;
        this.comparison = comparison;
    }

    @Override
    public Optional<String> getActual() {
        return actual == null ? Optional.empty() : Optional.of(actual);
    }

    @Override
    public Optional<StringBuffer> getComparison() {
        return comparison == null ? Optional.empty() : Optional.of(comparison);
    }

    @Override
    public void setComparison(final StringBuffer comparison) {
        this.comparison = Validate.notNull(comparison, "comparison");
    }

    @Override
    public void setActual(final String actual) {
        this.actual = Validate.notNull(actual, "actual");
    }

    @Override
    public int compareTo(final DebugDiffHolder o) {

        if(this == o) {
            return 0;
        } else if (o == null) {
            return -1;
        }


        int toReturn = this.getActual().orElse("").compareTo(o.getActual().orElse(""));
        if(toReturn == 0) {
            final String thisComparison = this.getComparison().isPresent() ? this.getComparison().toString() : "";
            final String thatComparison = o.getComparison().isPresent() ? o.getComparison().toString() : "";

            toReturn = thisComparison.compareTo(thatComparison);
        }

        return toReturn;
    }
}
