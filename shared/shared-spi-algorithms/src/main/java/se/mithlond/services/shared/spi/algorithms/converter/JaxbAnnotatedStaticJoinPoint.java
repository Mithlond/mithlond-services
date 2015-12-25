package se.mithlond.services.shared.spi.algorithms.converter;

import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;

import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbAnnotatedStaticJoinPoint {

    /** Returns the signature at the join point.  */
    Signature getSignature();

    /** <p>Returns the source location corresponding to the join point.</p>
     *
     *  <p>If there is no source location available, returns null.</p>
     *
     *  <p>Returns the SourceLocation of the defining class for default constructors.</p>
     */
    SourceLocation getSourceLocation();

    /** <p> Returns a String representing the kind of join point.  This String
     *       is guaranteed to be interned</p>
     */
    String getKind();

    /**
     * Return the id for this JoinPoint.StaticPart.  All JoinPoint.StaticPart
     * instances are assigned an id number upon creation.  For each advised type
     * the id numbers start at 0.
     * <br>
     * The id is guaranteed to remain constant across repeated executions
     * of a program but may change if the code is recompiled.
     * <br>
     * The benefit of having an id is that it can be used for array index
     * purposes which can be quicker than using the JoinPoint.StaticPart
     * object itself in a map lookup.
     * <br>
     * Since two JoinPoint.StaticPart instances in different advised types may have
     * the same id, then if the id is being used to index some joinpoint specific
     * state then that state must be maintained on a pertype basis - either by
     * using pertypewithin() or an ITD.
     *
     * @return the id of this joinpoint
     */
    int getId();

    String toString();

    /**
     * Returns an abbreviated string representation of the join point
     */
    String toShortString();

    /**
     * Returns an extended string representation of the join point
     */
    String toLongString();
}
