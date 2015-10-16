/*
 * #%L
 * Nazgul Project: mithlond-services-shared-entity-test
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
package se.mithlond.services.shared.test.entity;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Utility class to assign JPA ID for test purposes.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class JpaIdMutator {

	// Internal state
	private static final String METHOD_NAME = "setId";
	private static Method SET_JPAID_METHOD;

	static {
		for (Method current : NazgulEntity.class.getDeclaredMethods()) {
			if (METHOD_NAME.equals(current.getName())) {
				SET_JPAID_METHOD = current;
			}
		}

		if (SET_JPAID_METHOD != null) {
			SET_JPAID_METHOD.setAccessible(true);
		}
	}

	/**
	 * Assigns the JPA ID to the supplied obj object.
	 *
	 * @param obj   The instance to assign JPA ID for.
	 * @param newId the new JPA ID to assign.
	 * @param <T>   The type of object to assign.
	 */
	public static <T extends NazgulEntity> void setId(final T obj, final long newId) {

		final Object nonNull = Objects.requireNonNull(obj, "Cannot handle null 'obj' argument.");

		try {
			SET_JPAID_METHOD.invoke(nonNull, newId);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not set ID of [" + obj.getClass().getName()
					+ "] to [" + newId + "]", e);
		}
	}
}
