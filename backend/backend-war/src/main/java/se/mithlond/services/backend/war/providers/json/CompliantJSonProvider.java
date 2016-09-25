/*
 * #%L
 * Nazgul Project: mithlond-services-backend-war
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
package se.mithlond.services.backend.war.providers.json;

import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ext.Provider;

/**
 * MOXyJsonProvider extension provider which sports best-practise-compliant JSON rendering.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@ApplicationScoped
@Provider
public class CompliantJSonProvider extends MOXyJsonProvider {

    /**
     * The value of the JSON element corresponding to an {@link javax.xml.bind.annotation.XmlValue} annotation.
     */
    public static final String JSON_VALUE_WRAPPER = "value";

    /**
     * <p>Retrieves a fully configured {@link MOXyJsonProvider}</p>
     *
     * @return a fully configured {@link MOXyJsonProvider}.
     * @see #CompliantJSonProvider()
     */
    public static CompliantJSonProvider getEclipseLinkJSONProvider() {
        return new CompliantJSonProvider();
    }

    /**
     * <p>Default constructor, creating a fully configured {@link MOXyJsonProvider}. The JSON structure
     * configuration emits as "native/natural" JSON as possible, and is defined as follows:</p>
     * <ol>
     * <li><strong>Formatted/Human-readable</strong> output, implying indented JSON output</li>
     * <li>JAXB Document Root element <strong>not</strong> included in the emitted JSON</li>
     * <li>JAXB Collection wrappers converted to JSON arrays with array item names <strong>not</strong>
     * included. (C.f. {@link MOXyJsonProvider#setWrapperAsArrayName(boolean)})</li>
     * <li>JAXB {@link javax.xml.bind.annotation.XmlValue} converted to a JSON element with the
     * key "<strong>{@value #JSON_VALUE_WRAPPER}</strong>"</li>
     * </ol>
     */
    public CompliantJSonProvider() {

        super();

        // Implement default configuration, to be compliant
        // with best JSON practises.
        //
        setFormattedOutput(true);
        setIncludeRoot(false);
        setValueWrapper(JSON_VALUE_WRAPPER);
        setWrapperAsArrayName(true);
    }
}
