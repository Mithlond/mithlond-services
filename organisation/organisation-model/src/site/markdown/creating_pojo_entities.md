# Creating a POJO Entity class

Entities should be created in several steps, and follow common patterns. 
The POJO Entity creation patterns are described in the process below.

### 1. Entity class naming and inheritance

Entities model concepts. Therefore, strive to give the name of the concept itself to the entity class.
In this example, the entity describes a Person and it is therefore called `Person`.
Entity classes should extend `NazgulEntity` to reuse as much mechanics as possible.
This is illustrated in the code snippet below:

<pre class="brush: java"><![CDATA[
    public class Person extends NazgulEntity { ... }
]]></pre>

Should you need to create an abstract superclass for use in several subclasses, use the naming convention 
AbstractXXXX such as `AbstractYearlyInformation`. Abstract entity superclasses should also extend `NazulEntity`.

### 2. Add basic JAXB and JPA annotations

Now add some non-intrusive JAXB and JPA annotations to your class, as follows:

<pre class="brush: java"><![CDATA[
    @Entity
    @Access(AccessType.FIELD)
    @XmlAccessorType(XmlAccessType.FIELD)
    public class Person extends NazgulEntity { ... }
]]></pre>

If you need to create an abstract superclass, replace the `@Entity` annotation with `@MappedSuperclass`:

<pre class="brush: java"><![CDATA[
    @MappedSuperclass
    @Access(AccessType.FIELD)
    @XmlAccessorType(XmlAccessType.FIELD)
    public abstract class AbstractSomething extends NazgulEntity { ... }
]]></pre>

Enumeration classes should not be decorated with JPA annotation, but only with JAXB annotations:

<pre class="brush: java"><![CDATA[
     @XmlEnum(String.class)
     public enum CompulsorySchoolType { ... }
]]></pre>

### 3. Define internal state / data properties

The real magic of Entity classes comes from defining their data state, which is copied from persistent storage (i.e. 
databases) to transport forms (i.e. XML or JSON) or objects in memory. Annotate each property with the appropriate 
JPA and JAXB annotations to ensure that the properties are copied properly between persistent storage and transport 
forms. The exact semantics of JPA and JAXB annotations are defined in the JavaEE 7 specifications and the 
corresponding JSRs:

1. [The JAXB 2.0 specification: JSR 222](https://jcp.org/en/jsr/detail?id=222)
2. [The JPA 2.1 specification: JSR 338](https://jcp.org/en/jsr/detail?id=338)
3. [The Bean Validation specification: JSR 303](https://jcp.org/en/jsr/detail?id=303)

The resulting annotated internal state is shown below for simple String variables

<pre class="brush: java"><![CDATA[
    @Entity
    @Access(AccessType.FIELD)
    @XmlAccessorType(XmlAccessType.FIELD)
    public class Person extends NazgulEntity {
     
            // Internal state
            @NotNull
            @Pattern(regexp = Patterns.NONEMPTY_WORDS)
            @Basic(optional = false)
            @Column(nullable = false)
            @XmlElement(nillable = false, required = true)
            private String firstName;
        
            @NotNull
            @Pattern(regexp = Patterns.NONEMPTY_WORDS)
            @Basic(optional = false)
            @Column(nullable = false)
            @XmlElement(nillable = false, required = true)
            private String lastName;
        
            @Basic(optional = true)
            @Column(nullable = true)
            @XmlElement(nillable = true, required = false)
            private String phone;
        
            @Basic(optional = true)
            @Column(nullable = true)
            @XmlElement(nillable = true, required = false)
            private String email;
             
            ...
    }
]]></pre>

Some more patterns for internal state properties are provided below:

<table>
    <tr>
        <th>Pattern</th>
        <th>Desired outcome</th>
        <th>Code sample</th>
    </tr>
    <tr>
        <td>Mandatory number</td>
        <td>XML attribute, DB column (not null)</td>
        <td>
<pre class="brush: java"><![CDATA[
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute(required = true)
    private int numStudents;
]]></pre>
        </td>
    </tr>
    <tr>
        <td>Non-empty, mandatory Set</td>
        <td>XML sequence, DB foreign key</td>
        <td>
<pre class="brush: java"><![CDATA[
    @NotNull
    @Size(min = 1)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "school")
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = false, required = true, name = "schoolUnit")
    private Set<CompulsorySchoolUnit> schoolUnits;
]]></pre>
        </td>
    </tr>
    <tr>
        <td>Optional String</td>
        <td>XML optional element, DB column (nullable)</td>
        <td>
<pre class="brush: java"><![CDATA[
    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(nillable = true, required = false)
    private String email;
]]></pre>
            </td>
        </tr>
</table>    

#### 3b. Logger definition

You would seldom need loggers in Entity classes since they mainly store/wrap data, implying that their methods mostly
are getters and setters. However, should you need to perform logging from within an Entity class, use the SLF4J 
logging framework. The Logger should be declared `private static final` since it is a singleton. Also, the category 
of the Logger should correspond to the class containing the logger:

<pre class="brush: java"><![CDATA[
    @Entity
    @Access(AccessType.FIELD)
    @XmlAccessorType(XmlAccessType.FIELD)
    public class Person extends NazgulEntity {

        // Our log
        private static final Logger log = LoggerFactory.getLogger(Person.class);
]]></pre>

### 4. Implement constructors

You normally need to define at least 2 constructors for an Entity POJO class; the default (no-argument) constructor 
is required by the JPA and JAXB specifications and the compound constructor can be used programmatically. The latter 
should accept enough arguments to initialize all internal state within the Entity POJO. Javadoc is required on the 
compound constructor to provide guidance to developers on which values should be supplied when calling the constructor.

> Validation of inbound parameters should **not** be performed in the compound constructor, but instead in a separate 
> validation method (c.f. [validation]()).  

<pre class="brush: java"><![CDATA[
    @Entity
    @Access(AccessType.FIELD)
    @XmlAccessorType(XmlAccessType.FIELD)
    public class Person extends NazgulEntity implements Comparable<Person> {

        // Our log
        private static final Logger log = LoggerFactory.getLogger(Person.class);

        /**
         * JAXB/JPA-friendly constructor.
         */
        public Person() {
        }
    
        /**
         * Compound constructor creating a Person object wrapping the provided data.
         *
         * @param firstName The first name of the Person.
         * @param lastName  The last name of the Person.
         * @param phone     An optional Phone number.
         * @param email     An optional Email address.
         */
        public Person(@NotNull @Pattern(regexp = Patterns.NONEMPTY_WORDS) final String firstName,
                      @NotNull @Pattern(regexp = Patterns.NONEMPTY_WORDS) final String lastName,
                      final String phone,
                      final String email) {
    
            // Assign internal state
            this.firstName = firstName;
            this.lastName = lastName;
            this.phone = phone;
            this.email = email;
        }
        ...
    }
]]></pre>

### 5. Add JAXB namespace and propOrder annotations

Now, add the JAXB annotation `@XmlType` which should contain at least two attributes:

1. *namespace*: Contains the XML namespace of the Entity POJO. Use a commonly defined constant, to ensure that all 
   Entity POJO classes in the same service use the same XML namespace in their transport forms. The format of the 
   namespace should adhere to the pattern "http://www.skolverket.se/services/selectschool" where *selectschool* 
   should be replaced with the corresponding service name.
2. *propOrder*: Defines the order of the properties within the XML Schema ComplexType generated by JAXB for the 
   Entity POJO. Unless this attribute is supplied, the XML Schema property sequence is undefined which leads to 
   several types of problems. 

<pre class="brush: java"><![CDATA[
    @Entity
    @Access(AccessType.FIELD)
    @XmlType(namespace = Patterns.NAMESPACE, propOrder = {"firstName", "lastName", "phone", "email"})
    @XmlAccessorType(XmlAccessType.FIELD)
    public class Person extends NazgulEntity implements Comparable<Person> {
        ...
    }
]]></pre>

Adding this annotation implies that the JAXB (un-)marshalling process will work predictably.

### 6. Implement *validateEntityState* method

Validating internal state is important whenever a POJO entity is read from Database storage using JPA or converted 
from XML/JSON using JAXB. However, validation is equally important to perform whenever a POJO entity is created by a 
call to `new`. To cope with both scenarios, each entity must implement a method called `validateEntityState` which 
may throw an exception if some part of the internal state within the POJO Entity is incorrectly set up.

Typically, the validateEntityState method looks similar to the following:

<pre class="brush: java"><![CDATA[
        /**
         * {@inheritDoc}
         */
        @Override
        protected void validateEntityState() throws InternalStateValidationException {
    
            InternalStateValidationException.create()
                    .notNullOrEmpty(firstName, "firstName")
                    .notNullOrEmpty(lastName, "lastName")
                    .endExpressionAndValidate();
        }
]]></pre>

The typical methods within the ExpressionBuilder returned by the `create()` method are

1. `notNull`: Throws an InternalStateValidationException if the supplied argument was null. The second argument 
    should contain the variable name of the property which was null. 
1. `notNullOrEmpty`: Throws an InternalStateValidationException if the supplied argument was null or empty. The second 
    argument should contain the variable name of the property which was null or empty.
1. `notTrue`: Throws an InternalStateValidationException if the supplied argument was false. The second 
    argument should contain the variable name of the property which was false.
    
Another example of a validateInternalState method implementation is found below:

<pre class="brush: java"><![CDATA[    
    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notTrue(Math.abs(latitude) > MAX_ABSOLUTE_LATITUDE, ERROR_LATITUDE_MSG)
                .notTrue(Math.abs(longitude) > MAX_ABSOLUTE_LONGITUDE, ERROR_LONGITUDE_MSG)
                .endExpressionAndValidate();
    }
]]></pre>

### 7. Implement getters and - if required - setters

Simply implement the methods required to get access to the entity's internal state.
Prefer entity classes with only getters, implying that POJOs are immutable.
When we really need to change some value, a setter should be defined.
 
<pre class="brush: java"><![CDATA[    
    /**
     * @return The first name of the Person. Never null or empty.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return The last name of the Person. Never null or empty.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return The optional phone number of the Person.
     */
    public String getPhone() {
        return phone;
    }
]]></pre>

### 8. Implement `hashCode()`, `equals` and `toString()`
 
Most entities are stashed, sorted and filtered by Collections or Maps. For the operations in the collection classes 
to work properly all entity classes should implement hashCode(), equals() and Comparable&lt;TheEntityClass&gt;.
The Comparable method is handled in the next section, and this section contains instructions for overriding the 
three methods defined in `java.lang.Object`.

<pre class="brush: java"><![CDATA[
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Entities.hashCode(this, Object.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return Entities.equals(this, obj, Object.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Person ["
                + "\n  firstName : " + firstName
                + "\n  lastName  : " + lastName
                + "\n  email     : " + email
                + "\n  phone     : " + phone
                + "\n]";
    }
]]></pre>

The typical implementation of the equals and hashCode methods do not include the fields `id` and `version`.
Implement according to the rules for hashCode and equals if you have other needs.

### 8. Make the POJO Entity implement Comparable

One is frequently required to store POJO entities in SortedMaps or SortedSets to ensure a natural sort order. Unless 
there is a good reason not to, all POJO entities should implement Comparable to avoid exceptions whenever they are 
added to a Sorted collection.  

<pre class="brush: java"><![CDATA[
    public class Person extends NazgulEntity implements Comparable<Person> {
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final Person that) {
            
            int toReturn = that == null ? -1 : 0;
            if(this != that) {
                toReturn = this.getFirstName().compareTo(that.getFirstName());
            }
                    
            // All done.
            return toReturn;
        }
        ...
]]></pre>

### 8. Add NamedQueries

NamedQueries are database-stored and pre-compiled PreparedStatements which can provide considerable performance
gains compared to on-the-fly compiling JPQL for each query. Whenever a query can be provided in the form of a 
NamedQuery, it should be. Use named arguments with the same name as the property they are assigning (i.e. use the
JPQL parameter :firstName to assign the property firstName). Also, prefix each NamedQuery by the simple classname
where it is defined to simplifying finding it quickly.

<pre class="brush: java"><![CDATA[
@Entity
@NamedQueries({
        @NamedQuery(name = "Person.getByFirstName",
                query = "select p from Person p where p.firstName like concat('%', :firstName, '%')"
                        + " order by p.firstName"),
        @NamedQuery(name = "Person.getByLastName",
                query = "select p from Person p where p.lastName like like concat('%', :lastName, '%')"
                        + " order by p.firstName")
})
@Access(AccessType.FIELD)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"firstName", "lastName", "phone", "email"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Person extends NazgulEntity implements Comparable<Person> { ... }
]]></pre>        

### 9. All done.

The POJO Entity result is typically similar to the following:

<pre class="brush: java"><![CDATA[
/**
 * Specification for a Person with contact information.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "Person.getByFirstName",
                query = "select p from Person p where p.firstName like concat('%', :firstName, '%')"
                        + " order by p.firstName"),
        @NamedQuery(name = "Person.getByLastName",
                query = "select p from Person p where p.lastName like like concat('%', :lastName, '%')"
                        + " order by p.firstName")
})
@Access(AccessType.FIELD)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"firstName", "lastName", "phone", "email"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Person extends NazgulEntity implements Comparable<Person> {

    // Internal state
    @NotNull
    @Pattern(regexp = Patterns.NONEMPTY_WORDS)
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(nillable = false, required = true)
    private String firstName;

    @NotNull
    @Pattern(regexp = Patterns.NONEMPTY_WORDS)
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(nillable = false, required = true)
    private String lastName;

    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(nillable = true, required = false)
    private String phone;

    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(nillable = true, required = false)
    private String email;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Person() {
    }

    /**
     * Compound constructor creating a Person object wrapping the provided data.
     *
     * @param firstName The first name of the Person.
     * @param lastName  The last name of the Person.
     * @param phone     An optional Phone number.
     * @param email     An optional Email address.
     */
    public Person(@NotNull @Pattern(regexp = Patterns.NONEMPTY_WORDS) final String firstName,
                  @NotNull @Pattern(regexp = Patterns.NONEMPTY_WORDS) final String lastName,
                  final String phone,
                  final String email) {

        // Assign internal state
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
    }

    /**
     * @return The first name of the Person. Never null or empty.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return The last name of the Person. Never null or empty.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return The optional phone number of the Person.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @return The optional email address of the Person.
     */
    public String getEmail() {
        return email;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Person that) {

        int toReturn = that == null ? -1 : 0;
        if(this != that) {
            toReturn = this.getFirstName().compareTo(that.getFirstName());
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Entities.hashCode(this, Object.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return Entities.equals(this, obj, Object.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Person ["
                + "\n  firstName : " + firstName
                + "\n  lastName  : " + lastName
                + "\n  email     : " + email
                + "\n  phone     : " + phone
                + "\n]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(firstName, "firstName")
                .notNullOrEmpty(lastName, "lastName")
                .endExpressionAndValidate();
    }
}
]]></pre>        

This concludes the POJO Entity tutorial. Now please 
[create unit tests for the POJO Entity](./creating_entity_unit_tests.html).