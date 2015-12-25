package se.mithlond.services.shared.spi.algorithms.converter;


import org.aspectj.lang.JoinPoint;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JoinPointAdapter implements XmlAdapter<JoinPoint.StaticPart, String> {
}
