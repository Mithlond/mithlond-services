package se.mithlond.services.organisation.api.parameters;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Abstract SearchParameter specification.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractSearchParameters<E extends AbstractParameterBuilder<E>> implements Serializable {

	/**
	 * Serializable-friendly constructor.
	 */
	public AbstractSearchParameters() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {

		final String nLine = System.getProperty("line.separator");
		final StringBuilder builder = new StringBuilder(getClass().getSimpleName() + nLine);

		final SortedMap<String, String> internalState = new TreeMap<>();
		populateInternalStateMap(internalState);

		if(internalState.size() == 0) {
			throw new IllegalStateException("Empty internal state is not supported for a SearchParameters instance."
			+ " Check implementation of class [" + getClass().getSimpleName() + "]");
		}

		if(internalState.size() > 0) {

			final int maxWidth = internalState.keySet().stream().mapToInt(String::length).max().getAsInt();
			final String formatString = "%1$-" + maxWidth + "s";

			// "    [groupIDs          : " + groupIDs + nLine
			internalState.entrySet().stream().forEach(c -> {
				builder
						.append(" [")
						.append(String.format(formatString, c.getKey()))
						.append(" : ")
						.append(c.getValue())
						.append(nLine);
			});
		} else {
			builder.append("No internal state recorded.");
		}

		// All done.
		return builder.toString();
	}

	/**
	 * <p>Populates the internal state map of this AbstractSearchParameters subclass with the state
	 * of the concrete search parameter class. Typical example:</p>
	 * <pre>
	 *     <code>
	 *         // Assume the following internal state - minus annotations
	 *         private List<Long> groupIDs;
	 *         private List<Long> organisationIDs;
	 *         private List<Long> classifierIDs;
	 *
	 *         // Populate with the supplied internal state
	 *         protected void populateInternalStateMap(final SortedMap<String, String> toPopulate) {
	 *         		toPopulate.put("groupIDs", groupIDs.toString());
	 *         		toPopulate.put("organisationIDs", organisationIDs.toString());
	 *         		toPopulate.put("classifierIDs", classifierIDs.toString());
	 *         }
	 *     </code>
	 * </pre>
	 *
	 * @param toPopulate The internal state map to populate.
	 */
	protected abstract void populateInternalStateMap(final SortedMap<String, String> toPopulate);
}
