/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.finance;

import se.mithlond.services.organisation.model.Patterns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Common definitions for well-known currencies.</p>
 * <p>The International Organization for Standardization publishes a list of standard currency codes referred to as the
 * ISO 4217 code list. This enumeration uses a snippet of the values within the ISO 4217 codes for currencies in
 * circulation, plus some (marked with an asterisk) that are not officially recognized by the ISO.
 * This list excludes obsolete and old Euro-zone currencies. Currency codes are composed of a country's two-character
 * Internet country code plus a third character denoting the currency unit. For example,
 * the Canadian Dollar code (CAD) is made up of Canada's Internet code ("CA") plus a currency designator ("D"). For
 * a list of currency symbols like the dollar sign "$", the Pound sign "&pound;", and the Euro sign "&euro;",
 * refer to the <a href="http://www.xe.com/symbols.php">Currency Symbols page</a>.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Patterns.NAMESPACE)
@XmlEnum(String.class)
@XmlAccessorType(XmlAccessType.FIELD)
public enum WellKnownCurrency {

	/**
	 * Swedish kronor currency definition.
	 */
	SEK("krona", "kronor", "kr"),

	/**
	 * Danish currency definition.
	 */
	DKK("krone", "kronor", "kr"),

	/**
	 * Norwegian currency definition.
	 */
	NOK("krone", "kronor", "kr"),

	/**
	 * Euro currency definition.
	 */
	EUR("euro", "euros", "\u8364");

	// Internal state
	@XmlElement(required = true, nillable = false)
	private String currencyNameSingular;

	@XmlElement(required = true, nillable = false)
	private String currencyNamePlural;

	@XmlElement(required = true, nillable = false)
	private String currencySymbol;

	/**
	 * Compound constructor creating a WellKnownCurrency instance wrapping the supplied data.
	 *
	 * @param currencyNameSingular The singular-form name for this currency, such as "krona".
	 * @param currencyNamePlural   The plural-form name for this currency, such as "kronor".
	 * @param currencySymbol       The unicode currency symbol for this currency, such as "SEK" or "$".
	 */
	WellKnownCurrency(final String currencyNameSingular,
					  final String currencyNamePlural,
					  final String currencySymbol) {
		this.currencyNameSingular = currencyNameSingular;
		this.currencyNamePlural = currencyNamePlural;
		this.currencySymbol = currencySymbol;
	}

	/**
	 * @return The singular-form name for this currency, such as "krona".
	 */
	public String getCurrencyNameSingular() {
		return currencyNameSingular;
	}

	/**
	 * @return The plural-form name for this currency, such as "kronor".
	 */
	public String getCurrencyNamePlural() {
		return currencyNamePlural;
	}

	/**
	 * @return The unicode currency symbol for this currency, such as "SEK" or "$".
	 */
	public String getCurrencySymbol() {
		return currencySymbol;
	}
}
