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
