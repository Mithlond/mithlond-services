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
package se.mithlond.services.backend.war.resources.organisation;

import io.swagger.annotations.Api;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.resources.AbstractResource;
import se.mithlond.services.backend.war.resources.RestfulParameters;
import se.mithlond.services.content.api.report.ExcelReportService;
import se.mithlond.services.content.api.report.ReportService;
import se.mithlond.services.organisation.api.FoodAndAllergyService;
import se.mithlond.services.organisation.api.MembershipService;
import se.mithlond.services.organisation.api.OrganisationService;
import se.mithlond.services.organisation.api.parameters.GroupIdSearchParameters;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.food.Allergy;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.food.Allergies;
import se.mithlond.services.organisation.model.transport.membership.Groups;
import se.mithlond.services.organisation.model.transport.membership.Memberships;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Resource facade to Memberships and Membership management.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Api(description = "Provides information about Memberships to authorized users.")
@Path("/org/{" + RestfulParameters.ORGANISATION_JPA_ID + "}/membership")
public class MembershipResource extends AbstractResource {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(MembershipResource.class);

    // Internal state
    @EJB
    private MembershipService membershipService;

    @EJB
    private OrganisationService organisationService;

    @EJB
    private FoodAndAllergyService foodAndAllergyService;

    @EJB
    private ExcelReportService excelReportService;

    /**
     * Retrieves a {@link Memberships} wrapper containing all Membership (or MembershipVO), including the
     * OrganisationVO of the Organisation
     *
     * @param orgJpaID                 The JPA ID of the Organisation for which Memberships should be retrieved.
     * @param includeLoginNotPermitted If {@code true}, the returned Memberships holder includes all Memberships -
     *                                 including the ones not permitted login.
     * @return A {@link Memberships} wrapper containing all Membership (or MembershipVO)
     */
    @Path("/all")
    @GET
    public Memberships getMemberships(@PathParam(RestfulParameters.ORGANISATION_JPA_ID) final Long orgJpaID,
                                      @QueryParam(RestfulParameters.INCLUDE_LOGIN_NOT_PERMITTED)
                                      @DefaultValue("false") final boolean includeLoginNotPermitted,
                                      @QueryParam(RestfulParameters.DETAILS)
                                      @DefaultValue("false") final boolean getAllDetails) {

        // Create the return value
        final Memberships toReturn = new Memberships();

        // Which mode should we fire?
        if (getAllDetails) {

            // First, find all groups within the supplied organisation
            final Groups groups = organisationService.getGroups(GroupIdSearchParameters.builder()
                    .withOrganisationIDs(orgJpaID)
                    .withDetailedResponsePreferred(true)
                    .build());

            if (log.isDebugEnabled()) {
                log.debug("Retrieved [" + groups.getGroups().size() + "] Groups: "
                        + groups.getGroups().stream()
                        .map(aGroup -> "[" + aGroup.getClass().getSimpleName() + "]: " + aGroup.getGroupName())
                        .reduce((l, r) -> l + ", " + r)
                        .orElse("<none>"));
            }

            // Add all groups to the Memberships
            groups.getGroups().forEach(toReturn::addGroups);
        }

        // Fire the JPQL query
        final List<Membership> memberships = membershipService.getMembershipsIn(orgJpaID, includeLoginNotPermitted);

        // Repackage into a Memberships wrapper.
        memberships.forEach(toReturn::addMembership);

        if (log.isDebugEnabled()) {

            final String organisationName = !memberships.isEmpty()
                    ? memberships.get(0).getOrganisation().getOrganisationName()
                    : "<unknown>";

            log.debug("Found " + memberships.size() + " memberships in organisation "
                    + orgJpaID + " (" + organisationName + ")");
        }

        // All Done
        return toReturn;
    }

    /**
     * Retrieves a {@link Memberships} wrapper containing the full-detail membership with the supplied LOGIN.
     *
     * @param orgJpaID The JPA ID of the Organisation for which Memberships should be retrieved.
     * @return A {@link Memberships} wrapper containing all Membership (or MembershipVO)
     */
    @Path("/active")
    @GET
    public Memberships getActiveMembership(@PathParam(RestfulParameters.ORGANISATION_JPA_ID) final Long orgJpaID) {

        // First, find the given Membership
        final Set<Membership> membershipSet = new TreeSet<>();
        membershipSet.add(getActiveMembership());

        // All Done.
        return new Memberships(membershipSet);
    }

    /**
     * Retrieves the Allergies for the Membership within the supplied organisation.
     *
     * @param membershipJpaID The membership JPA ID.
     * @return A populated Allergies VO carrying all allergies for the supplied Membership.
     */
    @Path("{" + RestfulParameters.MEMBERSHIP_JPA_ID + "}/allergies")
    @GET
    public Allergies getAllergiesFor(@PathParam(RestfulParameters.MEMBERSHIP_JPA_ID) final Long membershipJpaID) {

        // Create the return value
        final Allergies toReturn = new Allergies();

        // Find the allergies for the supplied Membership
        final Membership theMembership = membershipService.findByPrimaryKey(Membership.class, membershipJpaID);
        final SortedSet<Allergy> foundAllergies = foodAndAllergyService.getAllergiesFor(theMembership);

        // Repack into the Allergies return object.
        foundAllergies.forEach(toReturn::add);

        // All Done.
        return toReturn;
    }

    /**
     * Retrieves an excel report containing the Memberships within the current organisation.
     *
     * @param includeLoginNotPermitted If {@code true}, even memberships currently not permitted login are included.
     * @return An Excel report containing Memberships.
     */
    @GET
    @Path("/report/excel")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(ExcelReportService.EXCEL_CONTENT_TYPE)
    public Response getMembershipsReport(
            @QueryParam(RestfulParameters.INCLUDE_LOGIN_NOT_PERMITTED) final Boolean includeLoginNotPermitted) {

        // Get the ActiveMembership
        final Membership activeMembership = getActiveMembership();
        final String activeOrgName = activeMembership.getOrganisation().getOrganisationName();
        final LocalDateTime now = LocalDateTime.now();

        // Create the workbook to return
        final Workbook workbook = excelReportService.createDocument(activeMembership,
                "Medlemmar i " + activeOrgName);
        final String fileName = "memberships_" + TimeFormat.COMPACT_LOCALDATETIME.print(now) + ".xls";
        final boolean includeAll = includeLoginNotPermitted == null ? false : includeLoginNotPermitted;

        final List<String> membershipColumns = Arrays.asList("Alias",
                "Förnamn",
                "Efternamn",
                "Telefon",
                "Mobil",
                "Email",
                "Adress",
                "Födelsedag");
        final Sheet allergySheet = excelReportService.createStandardExcelSheet(
                workbook,
                "Rullan",
                activeOrgName + "s Rulla ",
                membershipColumns);

        if (log.isDebugEnabled()) {
            log.debug("Got non-null Workbook: " + (workbook != null));
        }

        // #1) Find the Memberships
        final Memberships memberships = getMemberships(activeMembership.getOrganisation().getId(),
                includeAll,
                false);

        // #2) Create an Excel Sheet holding the Membership report details.
        int currentRowIndex = 2;
        final CellStyle standardCellStyle = excelReportService.getCellStyle(
                ExcelReportService.ExcelElement.NON_WRAPPING,
                workbook);

        for (Membership current : memberships.getMemberships()) {

            final Row currentRow = allergySheet.createRow(currentRowIndex++);

            // Harvest some data for convenience reasons.
            final String alias = current.getAlias()
                    + (current.getSubAlias() != null ? " " + current.getSubAlias() : "");
            final Map<String, String> contactDetails = current.getUser().getContactDetails();
            final String homePhone = contactDetails.get("HOME_PHONE") == null ? "" : contactDetails.get("HOME_PHONE");
            final String cellPhone = contactDetails.get("CELL_PHONE") == null ? "" : contactDetails.get("CELL_PHONE");
            final String email = current.getEmailAlias() + "@" + current.getOrganisation().getEmailSuffix();

            final Address homeAddress = current.getUser().getHomeAddress();
            final String address = (homeAddress.getCareOfLine() != null ? homeAddress.getCareOfLine() + "\n" : "")
                    + homeAddress.getStreet() + " " + homeAddress.getNumber() + "\n"
                    + homeAddress.getZipCode() + " " + homeAddress.getCity() + "\n"
                    + homeAddress.getCountry();

            final String bDay = TimeFormat.YEAR_MONTH_DATE.print(current.getUser().getBirthday());

            // Populate the row with Cells.
            excelReportService.addCell(0, currentRow, alias, standardCellStyle);
            excelReportService.addCell(1, currentRow, current.getUser().getFirstName(), standardCellStyle);
            excelReportService.addCell(2, currentRow, current.getUser().getLastName(), standardCellStyle);
            excelReportService.addCell(3, currentRow, homePhone, standardCellStyle);
            excelReportService.addCell(4, currentRow, cellPhone, standardCellStyle);
            excelReportService.addCell(5, currentRow, email, standardCellStyle);
            excelReportService.addCell(6, currentRow, address, standardCellStyle);
            excelReportService.addCell(7, currentRow, bDay, standardCellStyle);
        }

        // Convert the workbook to a byte[], and send it back to the client.
        final Response.ResponseBuilder builder = Response.ok(
                excelReportService.convertToByteArray(workbook), ExcelReportService.EXCEL_CONTENT_TYPE)
                .header(ReportService.SUGGESTED_FILENAME_HEADER, fileName);
        builder.header("Content-Disposition", "attachment; filename=" + fileName);

        // All Done.
        return builder.build();
    }
}
