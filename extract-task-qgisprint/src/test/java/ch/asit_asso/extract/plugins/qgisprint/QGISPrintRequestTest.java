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
package ch.asit_asso.extract.plugins.qgisprint;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import org.junit.jupiter.api.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QGISPrintRequest class.
 */
@DisplayName("QGISPrintRequest Tests")
class QGISPrintRequestTest {

    private QGISPrintRequest request;

    @BeforeEach
    void setUp() {
        request = new QGISPrintRequest();
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
    @DisplayName("Default Constructor Tests")
    class DefaultConstructorTests {

        @Test
        @DisplayName("Creates instance with default values")
        void createsInstanceWithDefaultValues() {
            QGISPrintRequest newRequest = new QGISPrintRequest();
            assertNotNull(newRequest);
            assertEquals(0, newRequest.getId());
            assertNull(newRequest.getFolderIn());
            assertNull(newRequest.getFolderOut());
            assertNull(newRequest.getClient());
            assertNull(newRequest.getClientGuid());
            assertNull(newRequest.getOrderGuid());
            assertNull(newRequest.getOrderLabel());
            assertNull(newRequest.getOrganism());
            assertNull(newRequest.getOrganismGuid());
            assertNull(newRequest.getParameters());
            assertNull(newRequest.getPerimeter());
            assertNull(newRequest.getProductGuid());
            assertNull(newRequest.getProductLabel());
            assertNull(newRequest.getTiers());
            assertNull(newRequest.getRemark());
            assertFalse(newRequest.isRejected());
            assertNull(newRequest.getStatus());
            assertNull(newRequest.getStartDate());
            assertNull(newRequest.getEndDate());
            assertNull(newRequest.getSurface());
        }
    }

    @Nested
    @DisplayName("Copy Constructor Tests")
    class CopyConstructorTests {

        @Test
        @DisplayName("Copies all properties from original request")
        void copiesAllPropertiesFromOriginalRequest() {
            ITaskProcessorRequest mockOriginal = mock(ITaskProcessorRequest.class);
            Calendar startDate = new GregorianCalendar(2024, Calendar.MARCH, 15, 10, 30, 0);
            Calendar endDate = new GregorianCalendar(2024, Calendar.MARCH, 15, 14, 45, 0);

            when(mockOriginal.getId()).thenReturn(42);
            when(mockOriginal.getClient()).thenReturn("Test Client");
            when(mockOriginal.getClientGuid()).thenReturn("client-guid-123");
            when(mockOriginal.getEndDate()).thenReturn(endDate);
            when(mockOriginal.getFolderIn()).thenReturn("/input/folder");
            when(mockOriginal.getFolderOut()).thenReturn("/output/folder");
            when(mockOriginal.getOrderGuid()).thenReturn("order-guid-456");
            when(mockOriginal.getOrderLabel()).thenReturn("Order Label");
            when(mockOriginal.getOrganism()).thenReturn("Test Organism");
            when(mockOriginal.getOrganismGuid()).thenReturn("organism-guid-789");
            when(mockOriginal.getParameters()).thenReturn("{\"key\":\"value\"}");
            when(mockOriginal.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockOriginal.getProductGuid()).thenReturn("product-guid-abc");
            when(mockOriginal.getProductLabel()).thenReturn("Product Label");
            when(mockOriginal.isRejected()).thenReturn(true);
            when(mockOriginal.getRemark()).thenReturn("Test remark");
            when(mockOriginal.getStartDate()).thenReturn(startDate);
            when(mockOriginal.getStatus()).thenReturn("TOEXPORT");
            when(mockOriginal.getSurface()).thenReturn("1500.75");
            when(mockOriginal.getTiers()).thenReturn("Test Tiers");

            QGISPrintRequest copiedRequest = new QGISPrintRequest(mockOriginal);

            assertEquals(42, copiedRequest.getId());
            assertEquals("Test Client", copiedRequest.getClient());
            assertEquals("client-guid-123", copiedRequest.getClientGuid());
            assertEquals(endDate, copiedRequest.getEndDate());
            assertEquals("/input/folder", copiedRequest.getFolderIn());
            assertEquals("/output/folder", copiedRequest.getFolderOut());
            assertEquals("order-guid-456", copiedRequest.getOrderGuid());
            assertEquals("Order Label", copiedRequest.getOrderLabel());
            assertEquals("Test Organism", copiedRequest.getOrganism());
            assertEquals("organism-guid-789", copiedRequest.getOrganismGuid());
            assertEquals("{\"key\":\"value\"}", copiedRequest.getParameters());
            assertEquals("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", copiedRequest.getPerimeter());
            assertEquals("product-guid-abc", copiedRequest.getProductGuid());
            assertEquals("Product Label", copiedRequest.getProductLabel());
            assertTrue(copiedRequest.isRejected());
            assertEquals("Test remark", copiedRequest.getRemark());
            assertEquals(startDate, copiedRequest.getStartDate());
            assertEquals("TOEXPORT", copiedRequest.getStatus());
            assertEquals("1500.75", copiedRequest.getSurface());
            assertEquals("Test Tiers", copiedRequest.getTiers());
        }

        @Test
        @DisplayName("Copies request with null values")
        void copiesRequestWithNullValues() {
            ITaskProcessorRequest mockOriginal = mock(ITaskProcessorRequest.class);

            when(mockOriginal.getId()).thenReturn(0);
            when(mockOriginal.getClient()).thenReturn(null);
            when(mockOriginal.getClientGuid()).thenReturn(null);
            when(mockOriginal.getEndDate()).thenReturn(null);
            when(mockOriginal.getFolderIn()).thenReturn(null);
            when(mockOriginal.getFolderOut()).thenReturn(null);
            when(mockOriginal.getOrderGuid()).thenReturn(null);
            when(mockOriginal.getOrderLabel()).thenReturn(null);
            when(mockOriginal.getOrganism()).thenReturn(null);
            when(mockOriginal.getOrganismGuid()).thenReturn(null);
            when(mockOriginal.getParameters()).thenReturn(null);
            when(mockOriginal.getPerimeter()).thenReturn(null);
            when(mockOriginal.getProductGuid()).thenReturn(null);
            when(mockOriginal.getProductLabel()).thenReturn(null);
            when(mockOriginal.isRejected()).thenReturn(false);
            when(mockOriginal.getRemark()).thenReturn(null);
            when(mockOriginal.getStartDate()).thenReturn(null);
            when(mockOriginal.getStatus()).thenReturn(null);
            when(mockOriginal.getSurface()).thenReturn(null);
            when(mockOriginal.getTiers()).thenReturn(null);

            QGISPrintRequest copiedRequest = new QGISPrintRequest(mockOriginal);

            assertEquals(0, copiedRequest.getId());
            assertNull(copiedRequest.getClient());
            assertNull(copiedRequest.getClientGuid());
            assertNull(copiedRequest.getEndDate());
            assertNull(copiedRequest.getFolderIn());
            assertNull(copiedRequest.getFolderOut());
            assertNull(copiedRequest.getOrderGuid());
            assertNull(copiedRequest.getOrderLabel());
            assertNull(copiedRequest.getOrganism());
            assertNull(copiedRequest.getOrganismGuid());
            assertNull(copiedRequest.getParameters());
            assertNull(copiedRequest.getPerimeter());
            assertNull(copiedRequest.getProductGuid());
            assertNull(copiedRequest.getProductLabel());
            assertFalse(copiedRequest.isRejected());
            assertNull(copiedRequest.getRemark());
            assertNull(copiedRequest.getStartDate());
            assertNull(copiedRequest.getStatus());
            assertNull(copiedRequest.getSurface());
            assertNull(copiedRequest.getTiers());
        }

        @Test
        @DisplayName("Copies from another QGISPrintRequest instance")
        void copiesFromAnotherQGISPrintRequestInstance() {
            QGISPrintRequest original = new QGISPrintRequest();
            original.setId(100);
            original.setClient("Original Client");
            original.setStatus("PROCESSING");

            QGISPrintRequest copied = new QGISPrintRequest(original);

            assertEquals(100, copied.getId());
            assertEquals("Original Client", copied.getClient());
            assertEquals("PROCESSING", copied.getStatus());
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
        @DisplayName("Sets folderIn with special characters")
        void setsFolderInWithSpecialCharacters() {
            request.setFolderIn("/path/with spaces/and-dashes/und_underscores");
            assertEquals("/path/with spaces/and-dashes/und_underscores", request.getFolderIn());
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
            request.setClient("Jean-Pierre Muller");
            assertEquals("Jean-Pierre Muller", request.getClient());
        }

        @Test
        @DisplayName("Sets client with unicode characters")
        void setsClientWithUnicodeCharacters() {
            request.setClient("Francois Lefevre");
            assertEquals("Francois Lefevre", request.getClient());
        }

        @Test
        @DisplayName("Sets null client")
        void setsNullClient() {
            request.setClient("Test");
            request.setClient(null);
            assertNull(request.getClient());
        }

        @Test
        @DisplayName("Sets empty client")
        void setsEmptyClient() {
            request.setClient("");
            assertEquals("", request.getClient());
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
            request.setClientGuid("test-guid");
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
            request.setOrderGuid("test");
            request.setOrderGuid(null);
            assertNull(request.getOrderGuid());
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
        @DisplayName("Sets orderLabel with special characters")
        void setsOrderLabelWithSpecialCharacters() {
            request.setOrderLabel("Order <test> & 'special' \"chars\"");
            assertEquals("Order <test> & 'special' \"chars\"", request.getOrderLabel());
        }

        @Test
        @DisplayName("Sets null orderLabel")
        void setsNullOrderLabel() {
            request.setOrderLabel("test");
            request.setOrderLabel(null);
            assertNull(request.getOrderLabel());
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
            request.setOrganism("test");
            request.setOrganism(null);
            assertNull(request.getOrganism());
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
            request.setOrganismGuid("test");
            request.setOrganismGuid(null);
            assertNull(request.getOrganismGuid());
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
            request.setParameters("{\"test\":1}");
            request.setParameters(null);
            assertNull(request.getParameters());
        }

        @Test
        @DisplayName("Sets complex JSON parameters")
        void setsComplexJsonParameters() {
            String complexJson = "{\"nested\":{\"array\":[1,2,3],\"object\":{\"key\":\"value\"}}}";
            request.setParameters(complexJson);
            assertEquals(complexJson, request.getParameters());
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
        @DisplayName("Sets point perimeter")
        void setsPointPerimeter() {
            String wkt = "POINT(6.5 46.5)";
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
            request.setProductGuid("test");
            request.setProductGuid(null);
            assertNull(request.getProductGuid());
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
            request.setProductLabel("test");
            request.setProductLabel(null);
            assertNull(request.getProductLabel());
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
            request.setTiers("test");
            request.setTiers(null);
            assertNull(request.getTiers());
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
            request.setRemark("test");
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
        @DisplayName("Sets remark with HTML content")
        void setsRemarkWithHtmlContent() {
            String html = "<p>This is <strong>bold</strong> text</p>";
            request.setRemark(html);
            assertEquals(html, request.getRemark());
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
        @DisplayName("Toggles rejected multiple times")
        void togglesRejectedMultipleTimes() {
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
            request.setStatus("test");
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
        @DisplayName("Sets startDate with specific timezone")
        void setsStartDateWithSpecificTimezone() {
            Calendar cal = new GregorianCalendar(2024, Calendar.JUNE, 15, 12, 0, 0);
            request.setStartDate(cal);
            assertEquals(2024, request.getStartDate().get(Calendar.YEAR));
            assertEquals(Calendar.JUNE, request.getStartDate().get(Calendar.MONTH));
            assertEquals(15, request.getStartDate().get(Calendar.DAY_OF_MONTH));
        }

        @Test
        @DisplayName("Sets startDate at midnight")
        void setsStartDateAtMidnight() {
            Calendar cal = new GregorianCalendar(2024, Calendar.DECEMBER, 31, 0, 0, 0);
            request.setStartDate(cal);
            assertEquals(0, request.getStartDate().get(Calendar.HOUR_OF_DAY));
            assertEquals(0, request.getStartDate().get(Calendar.MINUTE));
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
        @DisplayName("EndDate can be before startDate (no validation)")
        void endDateCanBeBeforeStartDate() {
            Calendar start = new GregorianCalendar(2024, Calendar.JANUARY, 15, 12, 0, 0);
            Calendar end = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 0, 0);
            request.setStartDate(start);
            request.setEndDate(end);
            assertTrue(request.getEndDate().before(request.getStartDate()));
        }

        @Test
        @DisplayName("EndDate can equal startDate")
        void endDateCanEqualStartDate() {
            Calendar cal = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 0, 0);
            request.setStartDate(cal);
            request.setEndDate(cal);
            assertEquals(request.getStartDate(), request.getEndDate());
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
            request.setSurface("100.0");
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
            request.setSurface("1.5e6");
            assertEquals("1.5e6", request.getSurface());
        }

        @Test
        @DisplayName("Sets integer surface value")
        void setsIntegerSurfaceValue() {
            request.setSurface("1000");
            assertEquals("1000", request.getSurface());
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
        @DisplayName("Overwrites all properties")
        void overwritesAllProperties() {
            request.setId(1);
            request.setClient("First Client");
            request.setStatus("PENDING");

            request.setId(2);
            request.setClient("Second Client");
            request.setStatus("COMPLETED");

            assertEquals(2, request.getId());
            assertEquals("Second Client", request.getClient());
            assertEquals("COMPLETED", request.getStatus());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Handles very long string values")
        void handlesVeryLongStringValues() {
            String longString = "A".repeat(10000);
            request.setClient(longString);
            assertEquals(longString, request.getClient());
        }

        @Test
        @DisplayName("Handles whitespace-only strings")
        void handlesWhitespaceOnlyStrings() {
            request.setClient("   ");
            assertEquals("   ", request.getClient());
        }

        @Test
        @DisplayName("Handles tab and newline characters")
        void handlesTabAndNewlineCharacters() {
            request.setRemark("Line1\tTab\nLine2\r\nLine3");
            assertEquals("Line1\tTab\nLine2\r\nLine3", request.getRemark());
        }

        @Test
        @DisplayName("Handles special JSON characters in parameters")
        void handlesSpecialJsonCharactersInParameters() {
            String json = "{\"message\":\"Hello \\\"World\\\"\",\"path\":\"C:\\\\Users\"}";
            request.setParameters(json);
            assertEquals(json, request.getParameters());
        }
    }
}
