/*
 * Copyright (C) 2017 arx iT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.plugins.fmedesktop;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import org.junit.jupiter.api.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FmeDesktopRequest class.
 */
@DisplayName("FmeDesktopRequest Tests")
class FmeDesktopRequestTest {

    private FmeDesktopRequest request;

    @BeforeEach
    void setUp() {
        request = new FmeDesktopRequest();
    }

    @Nested
    @DisplayName("Interface Implementation Tests")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Implements ITaskProcessorRequest interface")
        void implementsInterface() {
            assertInstanceOf(ITaskProcessorRequest.class, request);
        }
    }

    @Nested
    @DisplayName("Id Property Tests")
    class IdPropertyTests {

        @Test
        @DisplayName("Default id is 0")
        void defaultIdIsZero() {
            assertEquals(0, request.getId());
        }

        @Test
        @DisplayName("Sets and gets id")
        void setsAndGetsId() {
            request.setId(42);
            assertEquals(42, request.getId());
        }

        @Test
        @DisplayName("Sets negative id")
        void setsNegativeId() {
            request.setId(-1);
            assertEquals(-1, request.getId());
        }

        @Test
        @DisplayName("Sets max integer id")
        void setsMaxIntegerId() {
            request.setId(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, request.getId());
        }
    }

    @Nested
    @DisplayName("FolderIn Property Tests")
    class FolderInPropertyTests {

        @Test
        @DisplayName("Default folderIn is null")
        void defaultFolderInIsNull() {
            assertNull(request.getFolderIn());
        }

        @Test
        @DisplayName("Sets and gets folderIn")
        void setsAndGetsFolderIn() {
            request.setFolderIn("/path/to/input");
            assertEquals("/path/to/input", request.getFolderIn());
        }

        @Test
        @DisplayName("Sets null folderIn")
        void setsNullFolderIn() {
            request.setFolderIn("/path");
            request.setFolderIn(null);
            assertNull(request.getFolderIn());
        }

        @Test
        @DisplayName("Sets empty folderIn")
        void setsEmptyFolderIn() {
            request.setFolderIn("");
            assertEquals("", request.getFolderIn());
        }
    }

    @Nested
    @DisplayName("FolderOut Property Tests")
    class FolderOutPropertyTests {

        @Test
        @DisplayName("Default folderOut is null")
        void defaultFolderOutIsNull() {
            assertNull(request.getFolderOut());
        }

        @Test
        @DisplayName("Sets and gets folderOut")
        void setsAndGetsFolderOut() {
            request.setFolderOut("/path/to/output");
            assertEquals("/path/to/output", request.getFolderOut());
        }

        @Test
        @DisplayName("Sets null folderOut")
        void setsNullFolderOut() {
            request.setFolderOut("/path");
            request.setFolderOut(null);
            assertNull(request.getFolderOut());
        }
    }

    @Nested
    @DisplayName("Client Property Tests")
    class ClientPropertyTests {

        @Test
        @DisplayName("Default client is null")
        void defaultClientIsNull() {
            assertNull(request.getClient());
        }

        @Test
        @DisplayName("Sets and gets client")
        void setsAndGetsClient() {
            request.setClient("John Doe");
            assertEquals("John Doe", request.getClient());
        }

        @Test
        @DisplayName("Sets client with special characters")
        void setsClientWithSpecialCharacters() {
            request.setClient("Jean-Pierre Müller");
            assertEquals("Jean-Pierre Müller", request.getClient());
        }
    }

    @Nested
    @DisplayName("ClientGuid Property Tests")
    class ClientGuidPropertyTests {

        @Test
        @DisplayName("Default clientGuid is null")
        void defaultClientGuidIsNull() {
            assertNull(request.getClientGuid());
        }

        @Test
        @DisplayName("Sets and gets clientGuid")
        void setsAndGetsClientGuid() {
            String guid = "550e8400-e29b-41d4-a716-446655440000";
            request.setClientGuid(guid);
            assertEquals(guid, request.getClientGuid());
        }
    }

    @Nested
    @DisplayName("OrderGuid Property Tests")
    class OrderGuidPropertyTests {

        @Test
        @DisplayName("Default orderGuid is null")
        void defaultOrderGuidIsNull() {
            assertNull(request.getOrderGuid());
        }

        @Test
        @DisplayName("Sets and gets orderGuid")
        void setsAndGetsOrderGuid() {
            String guid = "order-guid-12345";
            request.setOrderGuid(guid);
            assertEquals(guid, request.getOrderGuid());
        }
    }

    @Nested
    @DisplayName("OrderLabel Property Tests")
    class OrderLabelPropertyTests {

        @Test
        @DisplayName("Default orderLabel is null")
        void defaultOrderLabelIsNull() {
            assertNull(request.getOrderLabel());
        }

        @Test
        @DisplayName("Sets and gets orderLabel")
        void setsAndGetsOrderLabel() {
            request.setOrderLabel("Order #12345");
            assertEquals("Order #12345", request.getOrderLabel());
        }
    }

    @Nested
    @DisplayName("Organism Property Tests")
    class OrganismPropertyTests {

        @Test
        @DisplayName("Default organism is null")
        void defaultOrganismIsNull() {
            assertNull(request.getOrganism());
        }

        @Test
        @DisplayName("Sets and gets organism")
        void setsAndGetsOrganism() {
            request.setOrganism("ASIT Association");
            assertEquals("ASIT Association", request.getOrganism());
        }
    }

    @Nested
    @DisplayName("OrganismGuid Property Tests")
    class OrganismGuidPropertyTests {

        @Test
        @DisplayName("Default organismGuid is null")
        void defaultOrganismGuidIsNull() {
            assertNull(request.getOrganismGuid());
        }

        @Test
        @DisplayName("Sets and gets organismGuid")
        void setsAndGetsOrganismGuid() {
            String guid = "org-guid-67890";
            request.setOrganismGuid(guid);
            assertEquals(guid, request.getOrganismGuid());
        }
    }

    @Nested
    @DisplayName("Parameters Property Tests")
    class ParametersPropertyTests {

        @Test
        @DisplayName("Default parameters is null")
        void defaultParametersIsNull() {
            assertNull(request.getParameters());
        }

        @Test
        @DisplayName("Sets and gets parameters as JSON")
        void setsAndGetsParametersAsJson() {
            String json = "{\"format\":\"PDF\",\"resolution\":\"300\"}";
            request.setParameters(json);
            assertEquals(json, request.getParameters());
        }

        @Test
        @DisplayName("Sets empty parameters")
        void setsEmptyParameters() {
            request.setParameters("{}");
            assertEquals("{}", request.getParameters());
        }
    }

    @Nested
    @DisplayName("Surface Property Tests")
    class SurfacePropertyTests {

        @Test
        @DisplayName("Default surface is null")
        void defaultSurfaceIsNull() {
            assertNull(request.getSurface());
        }

        @Test
        @DisplayName("Sets and gets surface")
        void setsAndGetsSurface() {
            request.setSurface("1500.50");
            assertEquals("1500.50", request.getSurface());
        }
    }

    @Nested
    @DisplayName("Perimeter Property Tests")
    class PerimeterPropertyTests {

        @Test
        @DisplayName("Default perimeter is null")
        void defaultPerimeterIsNull() {
            assertNull(request.getPerimeter());
        }

        @Test
        @DisplayName("Sets and gets perimeter as WKT")
        void setsAndGetsPerimeterAsWkt() {
            String wkt = "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))";
            request.setPerimeter(wkt);
            assertEquals(wkt, request.getPerimeter());
        }

        @Test
        @DisplayName("Sets multipolygon perimeter")
        void setsMultipolygonPerimeter() {
            String wkt = "MULTIPOLYGON(((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5)))";
            request.setPerimeter(wkt);
            assertEquals(wkt, request.getPerimeter());
        }
    }

    @Nested
    @DisplayName("ProductGuid Property Tests")
    class ProductGuidPropertyTests {

        @Test
        @DisplayName("Default productGuid is null")
        void defaultProductGuidIsNull() {
            assertNull(request.getProductGuid());
        }

        @Test
        @DisplayName("Sets and gets productGuid")
        void setsAndGetsProductGuid() {
            String guid = "prod-guid-abcdef";
            request.setProductGuid(guid);
            assertEquals(guid, request.getProductGuid());
        }
    }

    @Nested
    @DisplayName("ProductLabel Property Tests")
    class ProductLabelPropertyTests {

        @Test
        @DisplayName("Default productLabel is null")
        void defaultProductLabelIsNull() {
            assertNull(request.getProductLabel());
        }

        @Test
        @DisplayName("Sets and gets productLabel")
        void setsAndGetsProductLabel() {
            request.setProductLabel("Geodata Extract");
            assertEquals("Geodata Extract", request.getProductLabel());
        }
    }

    @Nested
    @DisplayName("Tiers Property Tests")
    class TiersPropertyTests {

        @Test
        @DisplayName("Default tiers is null")
        void defaultTiersIsNull() {
            assertNull(request.getTiers());
        }

        @Test
        @DisplayName("Sets and gets tiers")
        void setsAndGetsTiers() {
            request.setTiers("Third Party Company");
            assertEquals("Third Party Company", request.getTiers());
        }
    }

    @Nested
    @DisplayName("Remark Property Tests")
    class RemarkPropertyTests {

        @Test
        @DisplayName("Default remark is null")
        void defaultRemarkIsNull() {
            assertNull(request.getRemark());
        }

        @Test
        @DisplayName("Sets and gets remark")
        void setsAndGetsRemark() {
            request.setRemark("Processing completed successfully");
            assertEquals("Processing completed successfully", request.getRemark());
        }

        @Test
        @DisplayName("Sets multiline remark")
        void setsMultilineRemark() {
            String multiline = "Line 1\nLine 2\nLine 3";
            request.setRemark(multiline);
            assertEquals(multiline, request.getRemark());
        }
    }

    @Nested
    @DisplayName("Rejected Property Tests")
    class RejectedPropertyTests {

        @Test
        @DisplayName("Default rejected is false")
        void defaultRejectedIsFalse() {
            assertFalse(request.isRejected());
        }

        @Test
        @DisplayName("Sets rejected to true")
        void setsRejectedToTrue() {
            request.setRejected(true);
            assertTrue(request.isRejected());
        }

        @Test
        @DisplayName("Sets rejected back to false")
        void setsRejectedBackToFalse() {
            request.setRejected(true);
            request.setRejected(false);
            assertFalse(request.isRejected());
        }
    }

    @Nested
    @DisplayName("Status Property Tests")
    class StatusPropertyTests {

        @Test
        @DisplayName("Default status is null")
        void defaultStatusIsNull() {
            assertNull(request.getStatus());
        }

        @Test
        @DisplayName("Sets and gets status")
        void setsAndGetsStatus() {
            request.setStatus("TOEXPORT");
            assertEquals("TOEXPORT", request.getStatus());
        }

        @Test
        @DisplayName("Sets various status values")
        void setsVariousStatusValues() {
            String[] statuses = {"PENDING", "PROCESSING", "COMPLETED", "ERROR"};
            for (String status : statuses) {
                request.setStatus(status);
                assertEquals(status, request.getStatus());
            }
        }
    }

    @Nested
    @DisplayName("StartDate Property Tests")
    class StartDatePropertyTests {

        @Test
        @DisplayName("Default startDate is null")
        void defaultStartDateIsNull() {
            assertNull(request.getStartDate());
        }

        @Test
        @DisplayName("Sets and gets startDate")
        void setsAndGetsStartDate() {
            Calendar cal = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 30, 0);
            request.setStartDate(cal);
            assertEquals(cal, request.getStartDate());
        }

        @Test
        @DisplayName("Sets null startDate")
        void setsNullStartDate() {
            Calendar cal = Calendar.getInstance();
            request.setStartDate(cal);
            request.setStartDate(null);
            assertNull(request.getStartDate());
        }
    }

    @Nested
    @DisplayName("EndDate Property Tests")
    class EndDatePropertyTests {

        @Test
        @DisplayName("Default endDate is null")
        void defaultEndDateIsNull() {
            assertNull(request.getEndDate());
        }

        @Test
        @DisplayName("Sets and gets endDate")
        void setsAndGetsEndDate() {
            Calendar cal = new GregorianCalendar(2024, Calendar.JANUARY, 15, 14, 45, 30);
            request.setEndDate(cal);
            assertEquals(cal, request.getEndDate());
        }

        @Test
        @DisplayName("EndDate can be after startDate")
        void endDateCanBeAfterStartDate() {
            Calendar start = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 0, 0);
            Calendar end = new GregorianCalendar(2024, Calendar.JANUARY, 15, 12, 0, 0);
            request.setStartDate(start);
            request.setEndDate(end);
            assertTrue(request.getEndDate().after(request.getStartDate()));
        }
    }

    @Nested
    @DisplayName("Complete Request Tests")
    class CompleteRequestTests {

        @Test
        @DisplayName("Sets all properties")
        void setsAllProperties() {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();

            request.setId(1);
            request.setFolderIn("/input");
            request.setFolderOut("/output");
            request.setClient("Client Name");
            request.setClientGuid("client-guid");
            request.setOrderGuid("order-guid");
            request.setOrderLabel("Order Label");
            request.setOrganism("Organism");
            request.setOrganismGuid("organism-guid");
            request.setParameters("{\"key\":\"value\"}");
            request.setSurface("1000.0");
            request.setPerimeter("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            request.setProductGuid("product-guid");
            request.setProductLabel("Product Label");
            request.setTiers("Tiers Name");
            request.setRemark("Remark");
            request.setRejected(false);
            request.setStatus("COMPLETED");
            request.setStartDate(start);
            request.setEndDate(end);

            assertEquals(1, request.getId());
            assertEquals("/input", request.getFolderIn());
            assertEquals("/output", request.getFolderOut());
            assertEquals("Client Name", request.getClient());
            assertEquals("client-guid", request.getClientGuid());
            assertEquals("order-guid", request.getOrderGuid());
            assertEquals("Order Label", request.getOrderLabel());
            assertEquals("Organism", request.getOrganism());
            assertEquals("organism-guid", request.getOrganismGuid());
            assertEquals("{\"key\":\"value\"}", request.getParameters());
            assertEquals("1000.0", request.getSurface());
            assertEquals("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", request.getPerimeter());
            assertEquals("product-guid", request.getProductGuid());
            assertEquals("Product Label", request.getProductLabel());
            assertEquals("Tiers Name", request.getTiers());
            assertEquals("Remark", request.getRemark());
            assertFalse(request.isRejected());
            assertEquals("COMPLETED", request.getStatus());
            assertEquals(start, request.getStartDate());
            assertEquals(end, request.getEndDate());
        }
    }
}
