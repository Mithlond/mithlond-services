/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.articles.layout;

import se.mithlond.services.content.model.ContentPatterns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Layout helper enumeration defining the position within a boundary/box
 * that an item will attempt to float to.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = ContentPatterns.NAMESPACE)
@XmlEnum(String.class)
@XmlAccessorType(XmlAccessType.FIELD)
public enum Gravity {

    /**
     * Gravity to the top/left corner.
     */
    NORTHWEST,

    /**
     * Gravity to the top side.
     */
    NORTH,

    /**
     * Gravity to the top/right corner.
     */
    NORTHEAST,

    /**
     * Gravity to the left side.
     */
    WEST,

    /**
     * Gravity to the middle.
     */
    CENTER,

    /**
     * Gravity to the right side.
     */
    EAST,

    /**
     * Gravity to the bottom/left corner.
     */
    SOUTHWEST,

    /**
     * Gravity to the bottom.
     */
    SOUTH,

    /**
     * Gravity to the bottom/right corner.
     */
    SOUTHEAST
}
