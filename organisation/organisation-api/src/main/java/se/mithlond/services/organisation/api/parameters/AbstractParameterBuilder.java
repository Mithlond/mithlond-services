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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 * Abstract implementation of common functionality for a Builder-type class.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractParameterBuilder<E extends AbstractParameterBuilder<E>> {

	@XmlTransient
	private static final Logger log = LoggerFactory.getLogger(AbstractParameterBuilder.class);

	/**
	 * Adds the values within the toAdd in order to the supplied list with the given parameterName.
	 *
	 * @param list          The List of strings holding the configuration parameter.
	 * @param parameterName The name of the parameter to add.
	 * @param toAdd         An array of parameters to add.
	 * @return This AbstractParameterBuilder, for chaining.
	 */
	@SafeVarargs
	protected final <T> E addValuesIfApplicable(@NotNull final List<T> list,
												@NotNull final String parameterName,
												@NotNull final T... toAdd) {

		// Check sanity
		Validate.notNull(list, "Cannot handle null list argument.");

		if (toAdd.length > 0) {
			for (int i = 0; i < toAdd.length; i++) {

				// Check the current argument
				final T currentArgument = toAdd[i];
				Validate.notNull(currentArgument, "'" + parameterName + "' (index: " + i + ")");
				if (currentArgument instanceof String) {
					Validate.notEmpty((String) currentArgument, "'" + parameterName + "' (index: " + i + ")");
				}

				// Assign internal state
				if (!list.contains(currentArgument)) {
					list.add(currentArgument);
				}
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Not adding empty '" + parameterName + "' argument.");
			}
		}

		// All done.
		return (E) this;
	}

	/**
	 * @return A completely set-up AbstractSearchParameters instance containing the aggregated state
	 * within this AbstractParameterBuilder object.
	 */
	public abstract AbstractSearchParameters<E> build();
}
