package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Request Entity Tests")
class RequestTest {

    private Request request;

    @BeforeEach
    void setUp() {
        request = new Request();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null id")
        void defaultConstructor_createsInstanceWithNullId() {
            Request newRequest = new Request();
            assertNull(newRequest.getId());
        }

        @Test
        @DisplayName("Constructor with id sets the id correctly")
        void constructorWithId_setsIdCorrectly() {
            Integer expectedId = 42;
            Request newRequest = new Request(expectedId);
            assertEquals(expectedId, newRequest.getId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId work correctly")
        void setAndGetId() {
            Integer expectedId = 100;
            request.setId(expectedId);
            assertEquals(expectedId, request.getId());
        }

        @Test
        @DisplayName("setOrderLabel and getOrderLabel work correctly")
        void setAndGetOrderLabel() {
            String expectedLabel = "Test Order Label";
            request.setOrderLabel(expectedLabel);
            assertEquals(expectedLabel, request.getOrderLabel());
        }

        @Test
        @DisplayName("setOrderGuid and getOrderGuid work correctly")
        void setAndGetOrderGuid() {
            String expectedGuid = "order-guid-12345";
            request.setOrderGuid(expectedGuid);
            assertEquals(expectedGuid, request.getOrderGuid());
        }

        @Test
        @DisplayName("setProductGuid and getProductGuid work correctly")
        void setAndGetProductGuid() {
            String expectedGuid = "product-guid-67890";
            request.setProductGuid(expectedGuid);
            assertEquals(expectedGuid, request.getProductGuid());
        }

        @Test
        @DisplayName("setProductLabel and getProductLabel work correctly")
        void setAndGetProductLabel() {
            String expectedLabel = "Test Product";
            request.setProductLabel(expectedLabel);
            assertEquals(expectedLabel, request.getProductLabel());
        }

        @Test
        @DisplayName("setOrganism and getOrganism work correctly")
        void setAndGetOrganism() {
            String expectedOrganism = "Test Organization";
            request.setOrganism(expectedOrganism);
            assertEquals(expectedOrganism, request.getOrganism());
        }

        @Test
        @DisplayName("setOrganismGuid and getOrganismGuid work correctly")
        void setAndGetOrganismGuid() {
            String expectedGuid = "organism-guid-123";
            request.setOrganismGuid(expectedGuid);
            assertEquals(expectedGuid, request.getOrganismGuid());
        }

        @Test
        @DisplayName("setClient and getClient work correctly")
        void setAndGetClient() {
            String expectedClient = "Test Client";
            request.setClient(expectedClient);
            assertEquals(expectedClient, request.getClient());
        }

        @Test
        @DisplayName("setClientGuid and getClientGuid work correctly")
        void setAndGetClientGuid() {
            String expectedGuid = "client-guid-456";
            request.setClientGuid(expectedGuid);
            assertEquals(expectedGuid, request.getClientGuid());
        }

        @Test
        @DisplayName("setClientDetails and getClientDetails work correctly")
        void setAndGetClientDetails() {
            String expectedDetails = "Client details information";
            request.setClientDetails(expectedDetails);
            assertEquals(expectedDetails, request.getClientDetails());
        }

        @Test
        @DisplayName("setTiers and getTiers work correctly")
        void setAndGetTiers() {
            String expectedTiers = "Third Party Name";
            request.setTiers(expectedTiers);
            assertEquals(expectedTiers, request.getTiers());
        }

        @Test
        @DisplayName("setTiersGuid and getTiersGuid work correctly")
        void setAndGetTiersGuid() {
            String expectedGuid = "tiers-guid-789";
            request.setTiersGuid(expectedGuid);
            assertEquals(expectedGuid, request.getTiersGuid());
        }

        @Test
        @DisplayName("setTiersDetails and getTiersDetails work correctly")
        void setAndGetTiersDetails() {
            String expectedDetails = "Third party details";
            request.setTiersDetails(expectedDetails);
            assertEquals(expectedDetails, request.getTiersDetails());
        }

        @Test
        @DisplayName("setPerimeter and getPerimeter work correctly")
        void setAndGetPerimeter() {
            String expectedPerimeter = "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))";
            request.setPerimeter(expectedPerimeter);
            assertEquals(expectedPerimeter, request.getPerimeter());
        }

        @Test
        @DisplayName("setSurface and getSurface work correctly")
        void setAndGetSurface() {
            Double expectedSurface = 1500.50;
            request.setSurface(expectedSurface);
            assertEquals(expectedSurface, request.getSurface());
        }

        @Test
        @DisplayName("setParameters and getParameters work correctly")
        void setAndGetParameters() {
            String expectedParams = "{\"key\":\"value\"}";
            request.setParameters(expectedParams);
            assertEquals(expectedParams, request.getParameters());
        }

        @Test
        @DisplayName("setRemark and getRemark work correctly")
        void setAndGetRemark() {
            String expectedRemark = "Test remark";
            request.setRemark(expectedRemark);
            assertEquals(expectedRemark, request.getRemark());
        }

        @Test
        @DisplayName("setFolderIn and getFolderIn work correctly")
        void setAndGetFolderIn() {
            String expectedFolder = "/path/to/input";
            request.setFolderIn(expectedFolder);
            assertEquals(expectedFolder, request.getFolderIn());
        }

        @Test
        @DisplayName("setFolderOut and getFolderOut work correctly")
        void setAndGetFolderOut() {
            String expectedFolder = "/path/to/output";
            request.setFolderOut(expectedFolder);
            assertEquals(expectedFolder, request.getFolderOut());
        }

        @Test
        @DisplayName("setStartDate and getStartDate work correctly")
        void setAndGetStartDate() {
            Calendar expectedDate = new GregorianCalendar(2024, Calendar.JANUARY, 15);
            request.setStartDate(expectedDate);
            assertEquals(expectedDate, request.getStartDate());
        }

        @Test
        @DisplayName("setEndDate and getEndDate work correctly")
        void setAndGetEndDate() {
            Calendar expectedDate = new GregorianCalendar(2024, Calendar.JANUARY, 20);
            request.setEndDate(expectedDate);
            assertEquals(expectedDate, request.getEndDate());
        }

        @Test
        @DisplayName("setTasknum and getTasknum work correctly")
        void setAndGetTasknum() {
            Integer expectedTasknum = 3;
            request.setTasknum(expectedTasknum);
            assertEquals(expectedTasknum, request.getTasknum());
        }

        @Test
        @DisplayName("setStatus and getStatus work correctly")
        void setAndGetStatus() {
            Request.Status expectedStatus = Request.Status.ONGOING;
            request.setStatus(expectedStatus);
            assertEquals(expectedStatus, request.getStatus());
        }

        @Test
        @DisplayName("setProcess and getProcess work correctly")
        void setAndGetProcess() {
            Process expectedProcess = new Process(1);
            request.setProcess(expectedProcess);
            assertEquals(expectedProcess, request.getProcess());
        }

        @Test
        @DisplayName("setConnector and getConnector work correctly")
        void setAndGetConnector() {
            Connector expectedConnector = new Connector(1);
            request.setConnector(expectedConnector);
            assertEquals(expectedConnector, request.getConnector());
        }

        @Test
        @DisplayName("setRejected and isRejected work correctly")
        void setAndIsRejected() {
            request.setRejected(true);
            assertTrue(request.isRejected());

            request.setRejected(false);
            assertFalse(request.isRejected());
        }

        @Test
        @DisplayName("setExternalUrl and getExternalUrl work correctly")
        void setAndGetExternalUrl() {
            String expectedUrl = "https://example.com/order/123";
            request.setExternalUrl(expectedUrl);
            assertEquals(expectedUrl, request.getExternalUrl());
        }

        @Test
        @DisplayName("setLastReminder and getLastReminder work correctly")
        void setAndGetLastReminder() {
            Calendar expectedDate = new GregorianCalendar(2024, Calendar.FEBRUARY, 1);
            request.setLastReminder(expectedDate);
            assertEquals(expectedDate, request.getLastReminder());
        }
    }

    @Nested
    @DisplayName("Status Enum Tests")
    class StatusEnumTests {

        @Test
        @DisplayName("All status values are defined")
        void allStatusValuesAreDefined() {
            Request.Status[] statuses = Request.Status.values();
            assertEquals(9, statuses.length);
            assertNotNull(Request.Status.IMPORTFAIL);
            assertNotNull(Request.Status.IMPORTED);
            assertNotNull(Request.Status.ONGOING);
            assertNotNull(Request.Status.UNMATCHED);
            assertNotNull(Request.Status.ERROR);
            assertNotNull(Request.Status.STANDBY);
            assertNotNull(Request.Status.TOEXPORT);
            assertNotNull(Request.Status.EXPORTFAIL);
            assertNotNull(Request.Status.FINISHED);
        }

        @Test
        @DisplayName("valueOf returns correct status")
        void valueOfReturnsCorrectStatus() {
            assertEquals(Request.Status.ONGOING, Request.Status.valueOf("ONGOING"));
            assertEquals(Request.Status.FINISHED, Request.Status.valueOf("FINISHED"));
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("isActive returns true for non-FINISHED status")
        void isActive_returnsTrueForNonFinishedStatus() {
            request.setStatus(Request.Status.ONGOING);
            assertTrue(request.isActive());

            request.setStatus(Request.Status.ERROR);
            assertTrue(request.isActive());

            request.setStatus(Request.Status.STANDBY);
            assertTrue(request.isActive());

            request.setStatus(Request.Status.TOEXPORT);
            assertTrue(request.isActive());
        }

        @Test
        @DisplayName("isActive returns false for FINISHED status")
        void isActive_returnsFalseForFinishedStatus() {
            request.setStatus(Request.Status.FINISHED);
            assertFalse(request.isActive());
        }

        @Test
        @DisplayName("isOngoing returns true only for ONGOING status")
        void isOngoing_returnsTrueOnlyForOngoingStatus() {
            request.setStatus(Request.Status.ONGOING);
            assertTrue(request.isOngoing());

            request.setStatus(Request.Status.ERROR);
            assertFalse(request.isOngoing());

            request.setStatus(Request.Status.FINISHED);
            assertFalse(request.isOngoing());
        }

        @Test
        @DisplayName("reject sets correct properties with process")
        void reject_setsCorrectPropertiesWithProcess() {
            Process process = new Process(1);
            List<Task> tasks = new ArrayList<>();
            tasks.add(new Task(1));
            tasks.add(new Task(2));
            process.setTasksCollection(tasks);
            request.setProcess(process);

            String rejectionRemark = "Order rejected due to invalid data";
            request.reject(rejectionRemark);

            assertEquals(Request.Status.TOEXPORT, request.getStatus());
            assertEquals(rejectionRemark, request.getRemark());
            assertTrue(request.isRejected());
            assertEquals(3, request.getTasknum());
        }

        @Test
        @DisplayName("reject sets tasknum to 1 when no process")
        void reject_setsTasknumToOneWhenNoProcess() {
            String rejectionRemark = "Order rejected";
            request.reject(rejectionRemark);

            assertEquals(1, request.getTasknum());
            assertTrue(request.isRejected());
        }

        @Test
        @DisplayName("reject throws exception for blank remark")
        void reject_throwsExceptionForBlankRemark() {
            assertThrows(IllegalArgumentException.class, () -> request.reject(""));
            assertThrows(IllegalArgumentException.class, () -> request.reject("   "));
            assertThrows(IllegalArgumentException.class, () -> request.reject(null));
        }
    }

    @Nested
    @DisplayName("Equals, HashCode, and ToString Tests")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("equals returns true for same id")
        void equals_returnsTrueForSameId() {
            Request request1 = new Request(1);
            Request request2 = new Request(1);
            assertEquals(request1, request2);
        }

        @Test
        @DisplayName("equals returns false for different id")
        void equals_returnsFalseForDifferentId() {
            Request request1 = new Request(1);
            Request request2 = new Request(2);
            assertNotEquals(request1, request2);
        }

        @Test
        @DisplayName("equals returns false for null")
        void equals_returnsFalseForNull() {
            Request request1 = new Request(1);
            assertNotEquals(null, request1);
        }

        @Test
        @DisplayName("equals returns false for different type")
        void equals_returnsFalseForDifferentType() {
            Request request1 = new Request(1);
            assertNotEquals("not a request", request1);
        }

        @Test
        @DisplayName("hashCode is consistent for same id")
        void hashCode_isConsistentForSameId() {
            Request request1 = new Request(1);
            Request request2 = new Request(1);
            assertEquals(request1.hashCode(), request2.hashCode());
        }

        @Test
        @DisplayName("hashCode differs for different id")
        void hashCode_differsForDifferentId() {
            Request request1 = new Request(1);
            Request request2 = new Request(2);
            assertNotEquals(request1.hashCode(), request2.hashCode());
        }

        @Test
        @DisplayName("toString contains id")
        void toString_containsId() {
            Request request1 = new Request(42);
            String result = request1.toString();
            assertTrue(result.contains("42"));
            assertTrue(result.contains("idRequest"));
        }
    }
}
