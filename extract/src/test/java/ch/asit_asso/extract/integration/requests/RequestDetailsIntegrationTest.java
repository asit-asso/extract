/*
 * Copyright (C) 2025 SecureMind Sàrl
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
package ch.asit_asso.extract.integration.requests;

import ch.asit_asso.extract.domain.*;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for request details view data accessibility.
 *
 * Verifies that:
 * 1. All required request fields are persisted and retrievable
 * 2. History records are properly saved and loaded
 * 3. Customer and third-party information is correctly stored
 * 4. Related entities (connector, process) are accessible
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Request Details View Integration Tests")
class RequestDetailsIntegrationTest {

    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private RequestHistoryRepository historyRepository;

    @Autowired
    private ProcessesRepository processesRepository;

    @Autowired
    private ConnectorsRepository connectorsRepository;

    // ==================== 1. REQUEST IDENTIFICATION ====================

    @Nested
    @DisplayName("1. Request Identification Tests")
    class RequestIdentificationTests {

        @Test
        @DisplayName("1.1 - All identification fields are persisted and retrievable")
        @Transactional
        void allIdentificationFieldsArePersisted() {
            // Given: A complete request
            Request request = createCompleteRequest();
            request = requestsRepository.save(request);
            Integer requestId = request.getId();

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(requestId);

            // Then: All identification fields are accessible
            assertTrue(retrieved.isPresent());
            Request r = retrieved.get();
            assertNotNull(r.getId());
            assertEquals("ORDER-DETAILS-TEST", r.getOrderLabel());
            assertEquals("Product for Details Test", r.getProductLabel());
            assertEquals("prod-guid-details", r.getProductGuid());
            assertEquals("order-guid-details", r.getOrderGuid());
        }

        @Test
        @DisplayName("1.2 - Minimal request has required fields")
        @Transactional
        void minimalRequestHasRequiredFields() {
            // Given: A minimal request
            Request request = createMinimalRequest();
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Required fields are present
            assertTrue(retrieved.isPresent());
            assertNotNull(retrieved.get().getId());
            assertNotNull(retrieved.get().getOrderLabel());
            assertNotNull(retrieved.get().getStatus());
        }
    }

    // ==================== 2. CONNECTOR INFORMATION ====================

    @Nested
    @DisplayName("2. Connector Information Tests")
    class ConnectorInformationTests {

        @Test
        @DisplayName("2.1 - Connector is accessible from request")
        @Transactional
        void connectorIsAccessible() {
            // Given: A request with connector
            Request request = createCompleteRequest();
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Connector is accessible
            assertTrue(retrieved.isPresent());
            assertNotNull(retrieved.get().getConnector());
            assertNotNull(retrieved.get().getConnector().getName());
        }

        @Test
        @DisplayName("2.2 - External URL is persisted")
        @Transactional
        void externalUrlIsPersisted() {
            // Given: A request with external URL
            Request request = createCompleteRequest();
            request.setExternalUrl("https://example.com/order/12345");
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: External URL is preserved
            assertTrue(retrieved.isPresent());
            assertEquals("https://example.com/order/12345", retrieved.get().getExternalUrl());
        }
    }

    // ==================== 3. PROCESS INFORMATION ====================

    @Nested
    @DisplayName("3. Process Information Tests")
    class ProcessInformationTests {

        @Test
        @DisplayName("3.1 - Process is accessible from request")
        @Transactional
        void processIsAccessible() {
            // Given: A request with process
            Request request = createCompleteRequest();
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Process is accessible
            assertTrue(retrieved.isPresent());
            assertNotNull(retrieved.get().getProcess());
            assertNotNull(retrieved.get().getProcess().getName());
        }

        @Test
        @DisplayName("3.2 - Unmatched request has null process")
        @Transactional
        void unmatchedRequestHasNullProcess() {
            // Given: An unmatched request
            Request request = createMinimalRequest();
            request.setStatus(Request.Status.UNMATCHED);
            request.setProcess(null);
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Process is null
            assertTrue(retrieved.isPresent());
            assertNull(retrieved.get().getProcess());
        }
    }

    // ==================== 4. CUSTOMER DETAILS ====================

    @Nested
    @DisplayName("4. Customer Details Tests")
    class CustomerDetailsTests {

        @Test
        @DisplayName("4.1 - All customer fields are persisted")
        @Transactional
        void allCustomerFieldsArePersisted() {
            // Given: A request with complete customer info
            Request request = createCompleteRequest();
            request.setClient("Jean Dupont");
            request.setClientDetails("Rue de Lausanne 50\n1000 Lausanne");
            request.setClientGuid("client-guid-123");
            request.setOrganism("Canton de Vaud");
            request.setOrganismGuid("org-guid-456");
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: All customer fields are preserved
            assertTrue(retrieved.isPresent());
            Request r = retrieved.get();
            assertEquals("Jean Dupont", r.getClient());
            assertEquals("Rue de Lausanne 50\n1000 Lausanne", r.getClientDetails());
            assertEquals("client-guid-123", r.getClientGuid());
            assertEquals("Canton de Vaud", r.getOrganism());
            assertEquals("org-guid-456", r.getOrganismGuid());
        }

        @Test
        @DisplayName("4.2 - Third party information is persisted")
        @Transactional
        void thirdPartyInformationIsPersisted() {
            // Given: A request with third party
            Request request = createCompleteRequest();
            request.setTiers("Mandataire SA");
            request.setTiersDetails("Rue du Commerce 10\n1200 Genève");
            request.setTiersGuid("tiers-guid-789");
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Third party fields are preserved
            assertTrue(retrieved.isPresent());
            Request r = retrieved.get();
            assertEquals("Mandataire SA", r.getTiers());
            assertEquals("Rue du Commerce 10\n1200 Genève", r.getTiersDetails());
            assertEquals("tiers-guid-789", r.getTiersGuid());
        }
    }

    // ==================== 5. PARAMETERS ====================

    @Nested
    @DisplayName("5. Parameters Tests")
    class ParametersTests {

        @Test
        @DisplayName("5.1 - Request parameters are persisted as JSON")
        @Transactional
        void parametersArePersisted() {
            // Given: A request with JSON parameters
            Request request = createCompleteRequest();
            request.setParameters("{\"FORMAT\":\"DXF\",\"SCALE\":\"1:1000\"}");
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Parameters are preserved
            assertTrue(retrieved.isPresent());
            String params = retrieved.get().getParameters();
            assertNotNull(params);
            assertTrue(params.contains("FORMAT"));
            assertTrue(params.contains("DXF"));
        }

        @Test
        @DisplayName("5.2 - Empty parameters handled gracefully")
        @Transactional
        void emptyParametersHandled() {
            // Given: A request with empty parameters
            Request request = createCompleteRequest();
            request.setParameters("{}");
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Parameters are empty JSON
            assertTrue(retrieved.isPresent());
            assertEquals("{}", retrieved.get().getParameters());
        }
    }

    // ==================== 6. GEOGRAPHIC DATA ====================

    @Nested
    @DisplayName("6. Geographic Data Tests")
    class GeographicDataTests {

        @Test
        @DisplayName("6.1 - Perimeter geometry is persisted")
        @Transactional
        void perimeterGeometryIsPersisted() {
            // Given: A request with geometry
            String wkt = "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))";
            Request request = createCompleteRequest();
            request.setPerimeter(wkt);
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Geometry is preserved
            assertTrue(retrieved.isPresent());
            assertEquals(wkt, retrieved.get().getPerimeter());
        }

        @Test
        @DisplayName("6.2 - Surface area is persisted")
        @Transactional
        void surfaceAreaIsPersisted() {
            // Given: A request with surface
            Request request = createCompleteRequest();
            request.setSurface(25000.50);
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Surface is preserved
            assertTrue(retrieved.isPresent());
            assertEquals(25000.50, retrieved.get().getSurface());
        }
    }

    // ==================== 7. STATUS AND HISTORY ====================

    @Nested
    @DisplayName("7. Status and History Tests")
    class StatusAndHistoryTests {

        @Test
        @DisplayName("7.1 - Request status is persisted")
        @Transactional
        void statusIsPersisted() {
            // Given: A finished request
            Request request = createCompleteRequest();
            request.setStatus(Request.Status.FINISHED);
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Status is preserved
            assertTrue(retrieved.isPresent());
            assertEquals(Request.Status.FINISHED, retrieved.get().getStatus());
        }

        @Test
        @DisplayName("7.2 - Start date is persisted")
        @Transactional
        void startDateIsPersisted() {
            // Given: A request with start date
            Request request = createCompleteRequest();
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Start date is preserved
            assertTrue(retrieved.isPresent());
            assertNotNull(retrieved.get().getStartDate());
        }

        @Test
        @DisplayName("7.3 - History records are saved and retrieved")
        @Transactional
        void historyRecordsAreSavedAndRetrieved() {
            // Given: A request with history
            Request request = createCompleteRequest();
            request = requestsRepository.save(request);

            // Add history record
            RequestHistoryRecord historyRecord = new RequestHistoryRecord();
            historyRecord.setRequest(request);
            historyRecord.setProcessStep(0);
            historyRecord.setStep(1);
            historyRecord.setTaskLabel("Import");
            historyRecord.setStatus(RequestHistoryRecord.Status.FINISHED);
            historyRecord.setStartDate(new GregorianCalendar());
            historyRecord.setEndDate(new GregorianCalendar());
            historyRepository.save(historyRecord);

            // When: Querying history
            List<RequestHistoryRecord> history = historyRepository.findByRequestOrderByStep(request);

            // Then: History is retrieved
            assertNotNull(history);
            assertFalse(history.isEmpty());
            assertEquals("Import", history.get(0).getTaskLabel());
        }
    }

    // ==================== 8. REMARK AND OUTPUT ====================

    @Nested
    @DisplayName("8. Remark and Output Tests")
    class RemarkAndOutputTests {

        @Test
        @DisplayName("8.1 - Remark is persisted")
        @Transactional
        void remarkIsPersisted() {
            // Given: A request with remark
            Request request = createCompleteRequest();
            request.setRemark("Validated by admin on 2025-01-15");
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Remark is preserved
            assertTrue(retrieved.isPresent());
            assertEquals("Validated by admin on 2025-01-15", retrieved.get().getRemark());
        }

        @Test
        @DisplayName("8.2 - Output folder paths are persisted")
        @Transactional
        void outputFoldersArePersisted() {
            // Given: A request with folder paths
            Request request = createCompleteRequest();
            request.setFolderIn("request-1/input");
            request.setFolderOut("request-1/output");
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Folders are preserved
            assertTrue(retrieved.isPresent());
            assertEquals("request-1/input", retrieved.get().getFolderIn());
            assertEquals("request-1/output", retrieved.get().getFolderOut());
        }

        @Test
        @DisplayName("8.3 - Rejection status is persisted")
        @Transactional
        void rejectionStatusIsPersisted() {
            // Given: A rejected request
            Request request = createCompleteRequest();
            request.setRejected(true);
            request.setStatus(Request.Status.FINISHED);
            request = requestsRepository.save(request);

            // When: Retrieving the request
            Optional<Request> retrieved = requestsRepository.findById(request.getId());

            // Then: Rejection is preserved
            assertTrue(retrieved.isPresent());
            assertTrue(retrieved.get().isRejected());
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a complete request with all fields populated.
     */
    private Request createCompleteRequest() {
        Process process = processesRepository.findById(1).orElse(null);
        Connector connector = connectorsRepository.findAll().iterator().next();

        Request request = new Request();
        request.setOrderLabel("ORDER-DETAILS-TEST");
        request.setProductLabel("Product for Details Test");
        request.setProductGuid("prod-guid-details");
        request.setOrderGuid("order-guid-details");
        request.setClient("Test Client");
        request.setClientDetails("Test Address");
        request.setClientGuid("client-guid-details");
        request.setOrganism("Test Organization");
        request.setOrganismGuid("org-guid-details");
        request.setStatus(Request.Status.ONGOING);
        request.setConnector(connector);
        request.setProcess(process);
        request.setStartDate(new GregorianCalendar());
        request.setParameters("{}");
        request.setPerimeter("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
        request.setTasknum(1);
        request.setSurface(1000.0);

        return request;
    }

    /**
     * Creates a minimal request with only required fields.
     */
    private Request createMinimalRequest() {
        Connector connector = connectorsRepository.findAll().iterator().next();
        Process process = processesRepository.findById(1).orElse(null);

        Request request = new Request();
        request.setOrderLabel("MINIMAL-ORDER");
        request.setProductLabel("Minimal Product");
        request.setOrderGuid("min-order-guid-" + System.currentTimeMillis());
        request.setProductGuid("min-prod-guid-" + System.currentTimeMillis());
        request.setClient("Minimal Client");
        request.setClientDetails("Address");
        request.setStatus(Request.Status.ONGOING);
        request.setConnector(connector);
        request.setProcess(process);
        request.setStartDate(new GregorianCalendar());
        request.setParameters("{}");
        request.setPerimeter("{}");
        request.setTasknum(1);

        return request;
    }
}
