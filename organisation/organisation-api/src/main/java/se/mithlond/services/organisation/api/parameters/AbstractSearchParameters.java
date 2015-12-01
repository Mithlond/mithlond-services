/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
 * %%
 * Copyright (C) 2015 Mithlond
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
