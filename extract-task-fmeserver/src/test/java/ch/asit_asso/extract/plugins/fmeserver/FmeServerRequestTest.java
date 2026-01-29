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
package ch.asit_asso.extract.plugins.fmeserver;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import org.junit.jupiter.api.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FmeServerRequest class.
 */
@DisplayName("FmeServerRequest Tests")
class FmeServerRequestTest {

    private FmeServerRequest request;

    @BeforeEach
    void setUp() {
        request = new FmeServerRequest();
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

        @Test
        @DisplayName("Sets min integer id")
        void setsMinIntegerId() {
            request.setId(Integer.MIN_VALUE);
            assertEquals(Integer.MIN_VALUE, request.getId());
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

        @Test
        @DisplayName("Sets folderIn with spaces")
        void setsFolderInWithSpaces() {
            request.setFolderIn("/path/to/folder with spaces");
            assertEquals("/path/to/folder with spaces", request.getFolderIn());
        }

        @Test
        @DisplayName("Sets folderIn with special characters")
        void setsFolderInWithSpecialCharacters() {
            request.setFolderIn("/path/to/folder-with_special.chars");
            assertEquals("/path/to/folder-with_special.chars", request.getFolderIn());
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

        @Test
        @DisplayName("Sets empty folderOut")
        void setsEmptyFolderOut() {
            request.setFolderOut("");
            assertEquals("", request.getFolderOut());
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

        @Test
        @DisplayName("Sets null client")
        void setsNullClient() {
            request.setClient("Name");
            request.setClient(null);
            assertNull(request.getClient());
        }

        @Test
        @DisplayName("Sets empty client")
        void setsEmptyClient() {
            request.setClient("");
            assertEquals("", request.getClient());
        }

        @Test
        @DisplayName("Sets client with unicode characters")
        void setsClientWithUnicodeCharacters() {
            request.setClient("Jean-Claude André");
            assertEquals("Jean-Claude André", request.getClient());
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

        @Test
        @DisplayName("Sets null clientGuid")
        void setsNullClientGuid() {
            request.setClientGuid("guid");
            request.setClientGuid(null);
            assertNull(request.getClientGuid());
        }

        @Test
        @DisplayName("Sets empty clientGuid")
        void setsEmptyClientGuid() {
            request.setClientGuid("");
            assertEquals("", request.getClientGuid());
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

        @Test
        @DisplayName("Sets null orderGuid")
        void setsNullOrderGuid() {
            request.setOrderGuid("guid");
            request.setOrderGuid(null);
            assertNull(request.getOrderGuid());
        }

        @Test
        @DisplayName("Sets empty orderGuid")
        void setsEmptyOrderGuid() {
            request.setOrderGuid("");
            assertEquals("", request.getOrderGuid());
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

        @Test
        @DisplayName("Sets null orderLabel")
        void setsNullOrderLabel() {
            request.setOrderLabel("label");
            request.setOrderLabel(null);
            assertNull(request.getOrderLabel());
        }

        @Test
        @DisplayName("Sets empty orderLabel")
        void setsEmptyOrderLabel() {
            request.setOrderLabel("");
            assertEquals("", request.getOrderLabel());
        }

        @Test
        @DisplayName("Sets orderLabel with special characters")
        void setsOrderLabelWithSpecialCharacters() {
            request.setOrderLabel("Order #12345 - Test & Validation <2024>");
            assertEquals("Order #12345 - Test & Validation <2024>", request.getOrderLabel());
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

        @Test
        @DisplayName("Sets null organism")
        void setsNullOrganism() {
            request.setOrganism("org");
            request.setOrganism(null);
            assertNull(request.getOrganism());
        }

        @Test
        @DisplayName("Sets empty organism")
        void setsEmptyOrganism() {
            request.setOrganism("");
            assertEquals("", request.getOrganism());
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

        @Test
        @DisplayName("Sets null organismGuid")
        void setsNullOrganismGuid() {
            request.setOrganismGuid("guid");
            request.setOrganismGuid(null);
            assertNull(request.getOrganismGuid());
        }

        @Test
        @DisplayName("Sets empty organismGuid")
        void setsEmptyOrganismGuid() {
            request.setOrganismGuid("");
            assertEquals("", request.getOrganismGuid());
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

        @Test
        @DisplayName("Sets null parameters")
        void setsNullParameters() {
            request.setParameters("{\"key\":\"value\"}");
            request.setParameters(null);
            assertNull(request.getParameters());
        }

        @Test
        @DisplayName("Sets complex JSON parameters")
        void setsComplexJsonParameters() {
            String json = "{\"array\":[1,2,3],\"nested\":{\"key\":\"value\"},\"boolean\":true}";
            request.setParameters(json);
            assertEquals(json, request.getParameters());
        }

        @Test
        @DisplayName("Sets empty string parameters")
        void setsEmptyStringParameters() {
            request.setParameters("");
            assertEquals("", request.getParameters());
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

        @Test
        @DisplayName("Sets null surface")
        void setsNullSurface() {
            request.setSurface("100");
            request.setSurface(null);
            assertNull(request.getSurface());
        }

        @Test
        @DisplayName("Sets empty surface")
        void setsEmptySurface() {
            request.setSurface("");
            assertEquals("", request.getSurface());
        }

        @Test
        @DisplayName("Sets surface with scientific notation")
        void setsSurfaceWithScientificNotation() {
            request.setSurface("1.5E6");
            assertEquals("1.5E6", request.getSurface());
        }

        @Test
        @DisplayName("Sets surface with negative value")
        void setsSurfaceWithNegativeValue() {
            request.setSurface("-100.5");
            assertEquals("-100.5", request.getSurface());
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

        @Test
        @DisplayName("Sets null perimeter")
        void setsNullPerimeter() {
            request.setPerimeter("POINT(0 0)");
            request.setPerimeter(null);
            assertNull(request.getPerimeter());
        }

        @Test
        @DisplayName("Sets empty perimeter")
        void setsEmptyPerimeter() {
            request.setPerimeter("");
            assertEquals("", request.getPerimeter());
        }

        @Test
        @DisplayName("Sets point perimeter")
        void setsPointPerimeter() {
            String wkt = "POINT(6.5 46.5)";
            request.setPerimeter(wkt);
            assertEquals(wkt, request.getPerimeter());
        }

        @Test
        @DisplayName("Sets linestring perimeter")
        void setsLinestringPerimeter() {
            String wkt = "LINESTRING(6.5 46.5, 6.6 46.6, 6.7 46.7)";
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

        @Test
        @DisplayName("Sets null productGuid")
        void setsNullProductGuid() {
            request.setProductGuid("guid");
            request.setProductGuid(null);
            assertNull(request.getProductGuid());
        }

        @Test
        @DisplayName("Sets empty productGuid")
        void setsEmptyProductGuid() {
            request.setProductGuid("");
            assertEquals("", request.getProductGuid());
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

        @Test
        @DisplayName("Sets null productLabel")
        void setsNullProductLabel() {
            request.setProductLabel("label");
            request.setProductLabel(null);
            assertNull(request.getProductLabel());
        }

        @Test
        @DisplayName("Sets empty productLabel")
        void setsEmptyProductLabel() {
            request.setProductLabel("");
            assertEquals("", request.getProductLabel());
        }

        @Test
        @DisplayName("Sets productLabel with special characters")
        void setsProductLabelWithSpecialCharacters() {
            request.setProductLabel("Geodata Extract (v2.0) - Test & Production");
            assertEquals("Geodata Extract (v2.0) - Test & Production", request.getProductLabel());
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

        @Test
        @DisplayName("Sets null tiers")
        void setsNullTiers() {
            request.setTiers("name");
            request.setTiers(null);
            assertNull(request.getTiers());
        }

        @Test
        @DisplayName("Sets empty tiers")
        void setsEmptyTiers() {
            request.setTiers("");
            assertEquals("", request.getTiers());
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

        @Test
        @DisplayName("Sets null remark")
        void setsNullRemark() {
            request.setRemark("remark");
            request.setRemark(null);
            assertNull(request.getRemark());
        }

        @Test
        @DisplayName("Sets empty remark")
        void setsEmptyRemark() {
            request.setRemark("");
            assertEquals("", request.getRemark());
        }

        @Test
        @DisplayName("Sets remark with tabs and special whitespace")
        void setsRemarkWithTabsAndSpecialWhitespace() {
            String remark = "Line 1\t\tTabbed\nLine 2\r\nCarriage return";
            request.setRemark(remark);
            assertEquals(remark, request.getRemark());
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

        @Test
        @DisplayName("Toggle rejected multiple times")
        void toggleRejectedMultipleTimes() {
            assertFalse(request.isRejected());
            request.setRejected(true);
            assertTrue(request.isRejected());
            request.setRejected(false);
            assertFalse(request.isRejected());
            request.setRejected(true);
            assertTrue(request.isRejected());
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
            String[] statuses = {"PENDING", "PROCESSING", "COMPLETED", "ERROR", "TOEXPORT"};
            for (String status : statuses) {
                request.setStatus(status);
                assertEquals(status, request.getStatus());
            }
        }

        @Test
        @DisplayName("Sets null status")
        void setsNullStatus() {
            request.setStatus("PENDING");
            request.setStatus(null);
            assertNull(request.getStatus());
        }

        @Test
        @DisplayName("Sets empty status")
        void setsEmptyStatus() {
            request.setStatus("");
            assertEquals("", request.getStatus());
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

        @Test
        @DisplayName("Sets startDate at midnight")
        void setsStartDateAtMidnight() {
            Calendar cal = new GregorianCalendar(2024, Calendar.JANUARY, 1, 0, 0, 0);
            request.setStartDate(cal);
            assertEquals(cal, request.getStartDate());
        }

        @Test
        @DisplayName("Sets startDate at end of day")
        void setsStartDateAtEndOfDay() {
            Calendar cal = new GregorianCalendar(2024, Calendar.DECEMBER, 31, 23, 59, 59);
            request.setStartDate(cal);
            assertEquals(cal, request.getStartDate());
        }

        @Test
        @DisplayName("Verifies startDate fields")
        void verifiesStartDateFields() {
            Calendar cal = new GregorianCalendar(2024, Calendar.MARCH, 15, 14, 30, 45);
            request.setStartDate(cal);
            Calendar retrieved = request.getStartDate();
            assertEquals(2024, retrieved.get(Calendar.YEAR));
            assertEquals(Calendar.MARCH, retrieved.get(Calendar.MONTH));
            assertEquals(15, retrieved.get(Calendar.DAY_OF_MONTH));
            assertEquals(14, retrieved.get(Calendar.HOUR_OF_DAY));
            assertEquals(30, retrieved.get(Calendar.MINUTE));
            assertEquals(45, retrieved.get(Calendar.SECOND));
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
        @DisplayName("Sets null endDate")
        void setsNullEndDate() {
            Calendar cal = Calendar.getInstance();
            request.setEndDate(cal);
            request.setEndDate(null);
            assertNull(request.getEndDate());
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

        @Test
        @DisplayName("Verifies endDate fields")
        void verifiesEndDateFields() {
            Calendar cal = new GregorianCalendar(2024, Calendar.JUNE, 20, 16, 45, 30);
            request.setEndDate(cal);
            Calendar retrieved = request.getEndDate();
            assertEquals(2024, retrieved.get(Calendar.YEAR));
            assertEquals(Calendar.JUNE, retrieved.get(Calendar.MONTH));
            assertEquals(20, retrieved.get(Calendar.DAY_OF_MONTH));
            assertEquals(16, retrieved.get(Calendar.HOUR_OF_DAY));
            assertEquals(45, retrieved.get(Calendar.MINUTE));
            assertEquals(30, retrieved.get(Calendar.SECOND));
        }

        @Test
        @DisplayName("EndDate can be same as startDate")
        void endDateCanBeSameAsStartDate() {
            Calendar date = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 0, 0);
            request.setStartDate(date);
            request.setEndDate(date);
            assertEquals(request.getStartDate(), request.getEndDate());
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

        @Test
        @DisplayName("Creates request with all null string values")
        void createsRequestWithAllNullStringValues() {
            request.setId(0);
            request.setFolderIn(null);
            request.setFolderOut(null);
            request.setClient(null);
            request.setClientGuid(null);
            request.setOrderGuid(null);
            request.setOrderLabel(null);
            request.setOrganism(null);
            request.setOrganismGuid(null);
            request.setParameters(null);
            request.setSurface(null);
            request.setPerimeter(null);
            request.setProductGuid(null);
            request.setProductLabel(null);
            request.setTiers(null);
            request.setRemark(null);
            request.setRejected(false);
            request.setStatus(null);
            request.setStartDate(null);
            request.setEndDate(null);

            assertEquals(0, request.getId());
            assertNull(request.getFolderIn());
            assertNull(request.getFolderOut());
            assertNull(request.getClient());
            assertNull(request.getClientGuid());
            assertNull(request.getOrderGuid());
            assertNull(request.getOrderLabel());
            assertNull(request.getOrganism());
            assertNull(request.getOrganismGuid());
            assertNull(request.getParameters());
            assertNull(request.getSurface());
            assertNull(request.getPerimeter());
            assertNull(request.getProductGuid());
            assertNull(request.getProductLabel());
            assertNull(request.getTiers());
            assertNull(request.getRemark());
            assertFalse(request.isRejected());
            assertNull(request.getStatus());
            assertNull(request.getStartDate());
            assertNull(request.getEndDate());
        }

        @Test
        @DisplayName("Creates request with all empty string values")
        void createsRequestWithAllEmptyStringValues() {
            request.setId(0);
            request.setFolderIn("");
            request.setFolderOut("");
            request.setClient("");
            request.setClientGuid("");
            request.setOrderGuid("");
            request.setOrderLabel("");
            request.setOrganism("");
            request.setOrganismGuid("");
            request.setParameters("");
            request.setSurface("");
            request.setPerimeter("");
            request.setProductGuid("");
            request.setProductLabel("");
            request.setTiers("");
            request.setRemark("");
            request.setRejected(false);
            request.setStatus("");

            assertEquals(0, request.getId());
            assertEquals("", request.getFolderIn());
            assertEquals("", request.getFolderOut());
            assertEquals("", request.getClient());
            assertEquals("", request.getClientGuid());
            assertEquals("", request.getOrderGuid());
            assertEquals("", request.getOrderLabel());
            assertEquals("", request.getOrganism());
            assertEquals("", request.getOrganismGuid());
            assertEquals("", request.getParameters());
            assertEquals("", request.getSurface());
            assertEquals("", request.getPerimeter());
            assertEquals("", request.getProductGuid());
            assertEquals("", request.getProductLabel());
            assertEquals("", request.getTiers());
            assertEquals("", request.getRemark());
            assertFalse(request.isRejected());
            assertEquals("", request.getStatus());
        }

        @Test
        @DisplayName("Creates rejected request")
        void createsRejectedRequest() {
            request.setId(999);
            request.setClient("Failed Client");
            request.setRejected(true);
            request.setStatus("ERROR");
            request.setRemark("Request was rejected due to invalid perimeter");
            request.setStartDate(new GregorianCalendar(2024, Calendar.JANUARY, 1));

            assertEquals(999, request.getId());
            assertEquals("Failed Client", request.getClient());
            assertTrue(request.isRejected());
            assertEquals("ERROR", request.getStatus());
            assertEquals("Request was rejected due to invalid perimeter", request.getRemark());
            assertNotNull(request.getStartDate());
            assertNull(request.getEndDate());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Sets very long string values")
        void setsVeryLongStringValues() {
            String longString = "A".repeat(10000);
            request.setClient(longString);
            assertEquals(longString, request.getClient());
        }

        @Test
        @DisplayName("Sets string with only whitespace")
        void setsStringWithOnlyWhitespace() {
            request.setClient("   ");
            assertEquals("   ", request.getClient());
        }

        @Test
        @DisplayName("Sets string with unicode characters")
        void setsStringWithUnicodeCharacters() {
            String unicode = "Test avec caracteres speciaux: e e c a a e i o u n";
            request.setClient(unicode);
            assertEquals(unicode, request.getClient());
        }

        @Test
        @DisplayName("Sets string with newlines")
        void setsStringWithNewlines() {
            String withNewlines = "Line1\nLine2\r\nLine3";
            request.setRemark(withNewlines);
            assertEquals(withNewlines, request.getRemark());
        }

        @Test
        @DisplayName("Multiple setter calls override previous values")
        void multipleSetterCallsOverridePreviousValues() {
            request.setClient("First");
            request.setClient("Second");
            request.setClient("Third");
            assertEquals("Third", request.getClient());
        }

        @Test
        @DisplayName("Calendar objects are stored by reference")
        void calendarObjectsAreStoredByReference() {
            Calendar cal = Calendar.getInstance();
            request.setStartDate(cal);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            // The stored calendar should reflect the change since it's the same reference
            assertEquals(cal, request.getStartDate());
        }

        @Test
        @DisplayName("Independent calendars for start and end dates")
        void independentCalendarsForStartAndEndDates() {
            Calendar start = new GregorianCalendar(2024, Calendar.JANUARY, 1);
            Calendar end = new GregorianCalendar(2024, Calendar.DECEMBER, 31);
            request.setStartDate(start);
            request.setEndDate(end);
            assertNotSame(request.getStartDate(), request.getEndDate());
        }
    }
}
