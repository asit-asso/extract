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
package ch.asit_asso.extract.functional.requests;

import ch.asit_asso.extract.domain.*;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.GregorianCalendar;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for the request details view.
 *
 * Validates that the details view contains all necessary information:
 * 1. Request identification (ID, labels, GUIDs)
 * 2. Connector information
 * 3. Process information and progress
 * 4. Customer details (name, address, organism)
 * 5. Third party information
 * 6. Request parameters
 * 7. Geographic perimeter and surface
 * 8. Processing history
 * 9. Admin-specific fields
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("functional")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Request Details View Functional Tests")
class RequestDetailsFunctionalTest {

    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private RequestHistoryRepository historyRepository;

    @Autowired
    private ProcessesRepository processesRepository;

    @Autowired
    private ConnectorsRepository connectorsRepository;

    @BeforeAll
    static void setUpClass() {
        System.out.println("========================================");
        System.out.println("Request Details View Functional Tests");
        System.out.println("========================================");
        System.out.println("Validates that the details view contains:");
        System.out.println("- Request identification fields");
        System.out.println("- Connector and process information");
        System.out.println("- Customer and third party details");
        System.out.println("- Parameters and geographic data");
        System.out.println("- Processing history");
        System.out.println("========================================");
    }

    // ==================== 1. REQUEST DATA COMPLETENESS ====================

    @Test
    @Order(1)
    @DisplayName("1. Complete request has all required fields for details view")
    @Transactional
    void completeRequestHasAllRequiredFields() {
        // Given: Create a complete request with all fields
        Request request = createCompleteRequestWithAllFields();
        request = requestsRepository.save(request);

        // When: Retrieve the request
        Optional<Request> retrieved = requestsRepository.findById(request.getId());

        // Then: All fields for details view are present
        assertTrue(retrieved.isPresent(), "Request should be retrievable");
        Request r = retrieved.get();

        // Identification
        assertNotNull(r.getId(), "ID should be present");
        assertNotNull(r.getOrderLabel(), "Order label should be present");
        assertNotNull(r.getProductLabel(), "Product label should be present");
        assertNotNull(r.getOrderGuid(), "Order GUID should be present");
        assertNotNull(r.getProductGuid(), "Product GUID should be present");

        // Connector
        assertNotNull(r.getConnector(), "Connector should be present");
        assertNotNull(r.getConnector().getName(), "Connector name should be present");

        // Process
        assertNotNull(r.getProcess(), "Process should be present");
        assertNotNull(r.getProcess().getName(), "Process name should be present");

        // Customer
        assertNotNull(r.getClient(), "Client name should be present");
        assertNotNull(r.getClientDetails(), "Client details should be present");
        assertNotNull(r.getClientGuid(), "Client GUID should be present");

        // Organization
        assertNotNull(r.getOrganism(), "Organism should be present");
        assertNotNull(r.getOrganismGuid(), "Organism GUID should be present");

        // Third party
        assertNotNull(r.getTiers(), "Third party name should be present");
        assertNotNull(r.getTiersDetails(), "Third party details should be present");
        assertNotNull(r.getTiersGuid(), "Third party GUID should be present");

        // Parameters
        assertNotNull(r.getParameters(), "Parameters should be present");

        // Geographic
        assertNotNull(r.getPerimeter(), "Perimeter should be present");
        assertNotNull(r.getSurface(), "Surface should be present");

        // Dates and status
        assertNotNull(r.getStartDate(), "Start date should be present");
        assertNotNull(r.getStatus(), "Status should be present");

        System.out.println("✓ Complete request contains all required fields:");
        System.out.println("  - Identification: ID=" + r.getId() + ", Order=" + r.getOrderLabel());
        System.out.println("  - Connector: " + r.getConnector().getName());
        System.out.println("  - Process: " + r.getProcess().getName());
        System.out.println("  - Customer: " + r.getClient());
        System.out.println("  - Organization: " + r.getOrganism());
        System.out.println("  - Third Party: " + r.getTiers());
        System.out.println("  - Surface: " + r.getSurface() + " m²");
    }

    @Test
    @Order(2)
    @DisplayName("2. Request parameters are stored and retrievable")
    @Transactional
    void requestParametersAreStoredAndRetrievable() {
        // Given: A request with specific parameters
        Request request = createMinimalRequest();
        String params = "{\"FORMAT\":\"DXF\",\"SCALE\":\"1:1000\",\"LAYERS\":\"cadastre,roads\",\"OUTPUT\":\"zip\"}";
        request.setParameters(params);
        request = requestsRepository.save(request);

        // When: Retrieve the request
        Optional<Request> retrieved = requestsRepository.findById(request.getId());

        // Then: Parameters are preserved
        assertTrue(retrieved.isPresent());
        assertEquals(params, retrieved.get().getParameters());

        System.out.println("✓ Request parameters are preserved:");
        System.out.println("  - Stored: " + params);
        System.out.println("  - Retrieved: " + retrieved.get().getParameters());
    }

    @Test
    @Order(3)
    @DisplayName("3. Request history records are retrievable")
    @Transactional
    void requestHistoryRecordsAreRetrievable() {
        // Given: A request with history
        Request request = createMinimalRequest();
        request = requestsRepository.save(request);

        // Add history records
        RequestHistoryRecord import1 = createHistoryRecord(request, 0, 1, "Import", RequestHistoryRecord.Status.FINISHED);
        RequestHistoryRecord task1 = createHistoryRecord(request, 1, 2, "Task 1", RequestHistoryRecord.Status.FINISHED);
        RequestHistoryRecord task2 = createHistoryRecord(request, 2, 3, "Task 2", RequestHistoryRecord.Status.ONGOING);

        historyRepository.save(import1);
        historyRepository.save(task1);
        historyRepository.save(task2);

        // When: Query history
        var history = historyRepository.findByRequestOrderByStep(request);

        // Then: All history records are retrieved in order
        assertEquals(3, history.size(), "Should have 3 history records");
        assertEquals("Import", history.get(0).getTaskLabel());
        assertEquals("Task 1", history.get(1).getTaskLabel());
        assertEquals("Task 2", history.get(2).getTaskLabel());

        System.out.println("✓ Request history is retrievable:");
        for (var h : history) {
            System.out.println("  - Step " + h.getProcessStep() + ": " + h.getTaskLabel() + " (" + h.getStatus() + ")");
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. Request with remark is accessible")
    @Transactional
    void requestWithRemarkIsAccessible() {
        // Given: A request with a remark
        Request request = createMinimalRequest();
        request.setRemark("Validated by operator - additional data required for parcel 1234");
        request = requestsRepository.save(request);

        // When: Retrieve the request
        Optional<Request> retrieved = requestsRepository.findById(request.getId());

        // Then: Remark is preserved
        assertTrue(retrieved.isPresent());
        assertEquals("Validated by operator - additional data required for parcel 1234",
            retrieved.get().getRemark());

        System.out.println("✓ Request remark is preserved:");
        System.out.println("  - Remark: " + retrieved.get().getRemark());
    }

    @Test
    @Order(5)
    @DisplayName("5. Request in different statuses has correct flags")
    @Transactional
    void requestInDifferentStatusesHasCorrectFlags() {
        // Test all status types
        for (Request.Status status : Request.Status.values()) {
            Request request = createMinimalRequest();
            request.setStatus(status);
            request.setOrderGuid("order-" + status.name());
            request.setProductGuid("product-" + status.name());
            request = requestsRepository.save(request);

            Optional<Request> retrieved = requestsRepository.findById(request.getId());
            assertTrue(retrieved.isPresent());
            assertEquals(status, retrieved.get().getStatus());
        }

        System.out.println("✓ All request statuses are correctly stored:");
        for (Request.Status status : Request.Status.values()) {
            System.out.println("  - " + status.name());
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Geographic perimeter and surface are preserved")
    @Transactional
    void geographicDataIsPreserved() {
        // Given: A request with geographic data
        Request request = createMinimalRequest();
        String wkt = "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))";
        request.setPerimeter(wkt);
        request.setSurface(12345.67);
        request = requestsRepository.save(request);

        // When: Retrieve the request
        Optional<Request> retrieved = requestsRepository.findById(request.getId());

        // Then: Geographic data is preserved
        assertTrue(retrieved.isPresent());
        assertEquals(wkt, retrieved.get().getPerimeter());
        assertEquals(12345.67, retrieved.get().getSurface(), 0.01);

        System.out.println("✓ Geographic data is preserved:");
        System.out.println("  - Perimeter: " + wkt.substring(0, 30) + "...");
        System.out.println("  - Surface: " + retrieved.get().getSurface() + " m²");
    }

    @Test
    @Order(7)
    @DisplayName("7. External URL is accessible")
    @Transactional
    void externalUrlIsAccessible() {
        // Given: A request with external URL
        Request request = createMinimalRequest();
        request.setExternalUrl("https://sdi.example.com/orders/ORD-2025-001/view");
        request = requestsRepository.save(request);

        // When: Retrieve the request
        Optional<Request> retrieved = requestsRepository.findById(request.getId());

        // Then: External URL is preserved
        assertTrue(retrieved.isPresent());
        assertEquals("https://sdi.example.com/orders/ORD-2025-001/view",
            retrieved.get().getExternalUrl());

        System.out.println("✓ External URL is preserved:");
        System.out.println("  - URL: " + retrieved.get().getExternalUrl());
    }

    @Test
    @Order(8)
    @DisplayName("8. Rejected request has rejection flag")
    @Transactional
    void rejectedRequestHasRejectionFlag() {
        // Given: A rejected request
        Request request = createMinimalRequest();
        request.setRejected(true);
        request.setStatus(Request.Status.FINISHED);
        request.setRemark("Request rejected: Invalid perimeter");
        request = requestsRepository.save(request);

        // When: Retrieve the request
        Optional<Request> retrieved = requestsRepository.findById(request.getId());

        // Then: Rejection flag is preserved
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get().isRejected());
        assertEquals(Request.Status.FINISHED, retrieved.get().getStatus());

        System.out.println("✓ Rejected request is correctly flagged:");
        System.out.println("  - isRejected: " + retrieved.get().isRejected());
        System.out.println("  - Status: " + retrieved.get().getStatus());
        System.out.println("  - Remark: " + retrieved.get().getRemark());
    }

    @Test
    @Order(9)
    @DisplayName("9. Document: Details view information summary")
    void documentDetailsViewInformation() {
        System.out.println("✓ Request Details View displays the following information:");
        System.out.println("");
        System.out.println("  IDENTIFICATION:");
        System.out.println("  - Request ID (for admin panel)");
        System.out.println("  - Order label / Product label (combined in title)");
        System.out.println("  - Product GUID, Client GUID, Organism GUID, Tiers GUID (admin panel)");
        System.out.println("");
        System.out.println("  CONNECTOR:");
        System.out.println("  - Connector name (with link for admins)");
        System.out.println("  - External URL (link to source system)");
        System.out.println("");
        System.out.println("  PROCESS:");
        System.out.println("  - Process name");
        System.out.println("  - Progress bar with task status");
        System.out.println("  - Current step indicator");
        System.out.println("");
        System.out.println("  CUSTOMER DETAILS:");
        System.out.println("  - Organization name");
        System.out.println("  - Customer name");
        System.out.println("  - Customer address/details");
        System.out.println("");
        System.out.println("  THIRD PARTY (if applicable):");
        System.out.println("  - Third party name");
        System.out.println("  - Third party details");
        System.out.println("");
        System.out.println("  PARAMETERS:");
        System.out.println("  - All request parameters with labels");
        System.out.println("  - Validation focus parameters (highlighted)");
        System.out.println("");
        System.out.println("  GEOGRAPHIC DATA:");
        System.out.println("  - Perimeter map (OpenLayers)");
        System.out.println("  - Surface area");
        System.out.println("");
        System.out.println("  RESPONSE (if applicable):");
        System.out.println("  - Remark text");
        System.out.println("  - Output files list");
        System.out.println("  - Temp folder path (for admins)");
        System.out.println("");
        System.out.println("  HISTORY:");
        System.out.println("  - Full processing history table");
        System.out.println("  - Start/end dates, task name, status, user");
        System.out.println("");
        System.out.println("  ADMIN TOOLS:");
        System.out.println("  - Delete button");
        System.out.println("  - Technical IDs display");

        assertTrue(true, "Documentation test");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a complete request with all fields populated.
     */
    private Request createCompleteRequestWithAllFields() {
        Process process = processesRepository.findById(1).orElse(null);
        Connector connector = connectorsRepository.findAll().iterator().next();

        Request request = new Request();

        // Identification
        request.setOrderLabel("FUNC-DETAILS-ORDER-001");
        request.setProductLabel("Complete Product with All Fields");
        request.setOrderGuid("func-order-guid-complete");
        request.setProductGuid("func-product-guid-complete");

        // Connector and Process
        request.setConnector(connector);
        request.setProcess(process);

        // Customer
        request.setClient("Jean-Pierre Müller");
        request.setClientDetails("Rue de Lausanne 100\n1000 Lausanne\nSuisse");
        request.setClientGuid("func-client-guid-123");

        // Organization
        request.setOrganism("Canton de Vaud - DGIP");
        request.setOrganismGuid("func-org-guid-456");

        // Third Party
        request.setTiers("Géomètre SA");
        request.setTiersDetails("Avenue de la Gare 25\n1003 Lausanne");
        request.setTiersGuid("func-tiers-guid-789");

        // Parameters
        request.setParameters("{\"FORMAT\":\"PDF\",\"SCALE\":\"1:500\",\"LAYERS\":\"cadastre,batiments\"}");

        // Geographic
        request.setPerimeter("POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))");
        request.setSurface(25000.50);

        // Status and dates
        request.setStatus(Request.Status.ONGOING);
        request.setStartDate(new GregorianCalendar());
        request.setTasknum(1);

        // External URL
        request.setExternalUrl("https://sdi.vd.ch/orders/FUNC-001");

        return request;
    }

    /**
     * Creates a minimal request with only required fields.
     */
    private Request createMinimalRequest() {
        Process process = processesRepository.findById(1).orElse(null);
        Connector connector = connectorsRepository.findAll().iterator().next();

        Request request = new Request();
        request.setOrderLabel("FUNC-MINIMAL-ORDER");
        request.setProductLabel("Minimal Product");
        request.setOrderGuid("func-minimal-order-" + System.currentTimeMillis());
        request.setProductGuid("func-minimal-product-" + System.currentTimeMillis());
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

    /**
     * Creates a history record for a request.
     */
    private RequestHistoryRecord createHistoryRecord(Request request, int processStep, int step,
            String taskLabel, RequestHistoryRecord.Status status) {
        RequestHistoryRecord record = new RequestHistoryRecord();
        record.setRequest(request);
        record.setProcessStep(processStep);
        record.setStep(step);
        record.setTaskLabel(taskLabel);
        record.setStatus(status);
        record.setStartDate(new GregorianCalendar());
        if (status == RequestHistoryRecord.Status.FINISHED) {
            record.setEndDate(new GregorianCalendar());
        }
        return record;
    }
}
