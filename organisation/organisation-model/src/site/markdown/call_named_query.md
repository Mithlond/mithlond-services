# Example of calling a named query

This example shows how to call the named query County.NAMEDQ_GET_COUNTY_BY_IDS which is flexible and that can take
several parameters that are lists. JPQL cannot not handle an empty list in the "IN" keyword. The IN keyword requires at least
one value in the list. So we have done a work around by passing the list together with the size of the list as parameters.

In case the list is empty we will pass the list with a dummy value which the function padList will add and together
with the size 0 which will cause the JQPL clause not to look at the dummy value.

The named query in County looks like this:

<pre class="brush: java"><![CDATA[
@NamedQuery(name = County.NAMEDQ_GET_COUNTY_BY_IDS,
        query = "select distinct c from County c where"
                + " ((0 = :countyIdsSize OR 0 < :countyIdsSize AND c.id in :countyIDs ) "
                + " or c.id in (select m.county.id from Municipality m where (0 = :municipalityIdsSize OR 0 < :municipalityIdsSize AND m.id in :municipalityIDs)) "
                + " or c.id in (select cd.municipality.county.id from CityDistrict cd "
                + "         where (0 = :cityDistrictIDsSize OR 0 < :cityDistrictIDsSize AND cd.id in :cityDistrictIDs))) order by c.id")
]]></pre>

If we look at the first part of the where clause:

<pre class="brush: java"><![CDATA[
    0 = :countyIdsSize OR 0 < :countyIdsSize AND c.id in :countyIDs
]]></pre>

This construction of the where clause enables us to handle an "empty" list (parameter :countyIDs) which will have a dummy value but the size parameter
will be 0. The where clause will be evaluated to:

<pre class="brush: java"><![CDATA[
    0 = 0 OR 0 &lt; 0 AND c.id in &lt;a list with a dummy value here> => true OR false AND c.id in &lt;a list with a dummy value here>
    the first part 0 = 0 will be true and the second part after OR is false which means that the "empty" list with a dummy value did not affect the
    logics.
]]></pre>


If we have a real value in the list then the size parameter will be larger than 0.

<pre class="brush: java"><![CDATA[
    0 = 1 OR 0 < 1 AND c.id in <a list with a real value>  => false OR true AND c.id in <a list with a real value>
]]></pre>

This clause will use the second part after the OR. And the effective logic will be c.id in &lt;a list with a real value&gt;
that means we are looking for values in the list.

Here is an example calling County.NAMEDQ_GET_COUNTY_BY_IDS:


<pre class="brush: java"><![CDATA[
@Override
public List<County> findCounties(@NotNull final LocationIdSearchParameters searchParameters) {

    // Check sanity
    Validate.notNull(searchParameters, "Cannot handle null searchParameters argument.");

    final List<Long> countyIDs          = searchParameters.getCountyIDs();
    final List<Long> municipalityIDs    = searchParameters.getMunicipalityIDs();
    final List<Long> cityDistrictIDs    = searchParameters.getCityDistrictIDs();

    final int countyIDsSize             = padList(countyIDs);
    final int municipalityIDsSize       = padList(municipalityIDs);
    final int cityDistrictIDsSize       = padList(cityDistrictIDs);

    // Fire the query; retrieve the results
    final List<County> toReturn = new ArrayList<County>();

    final List<County> result = entityManager.createNamedQuery(County.NAMEDQ_GET_COUNTY_BY_IDS, County.class)
            .setParameter(County.COUNTY_IDS, countyIDs)
            .setParameter(County.COUNTY_IDS_SIZE, countyIDsSize)
            .setParameter(County.MUNICIPALITY_IDS, municipalityIDs)
            .setParameter(County.MUNICIPALITY_IDS_SIZE, municipalityIDsSize)
            .setParameter(County.CITY_DISTRICT_IDS, cityDistrictIDs)
            .setParameter(County.CITY_DISTRICT_IDS_SIZE, cityDistrictIDsSize)
            .getResultList();
    toReturn.addAll(result);

    // All done.
    return toReturn;
}
]]></pre>

The helper method padList adds an dummy value to aList if it is empty and returns the size.
If a dummy value was added then size returned will be 0.

<pre class="brush: java"><![CDATA[
private int padList(final List<Long> aList) {
    int size = aList.size();

    if (aList.size() == 0) {
        aList.add(0l);
    }

    return size;
}
]]></pre>