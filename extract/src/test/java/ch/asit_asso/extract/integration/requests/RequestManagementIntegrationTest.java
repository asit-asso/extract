/*
 * Copyright (C) 2025 arx iT
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

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.integration.DatabaseTestHelper;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for request management (import, visibility, status).
 *
 * Tests the following scenarios:
 * - Import of requests with different statuses (fixtures)
 * - Visibility rules for administrators vs operators
 * - Request status and attributes persistence
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Request Management Integration Tests - Priority 1")
class RequestManagementIntegrationTest {

    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private ProcessesRepository processesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private DatabaseTestHelper dbHelper;

    private int connectorId;
    private int processId;
    private int adminId;
    private int operatorId;
    private int nonOperatorId;

    @BeforeEach
    void setUp() {
        // Create test environment
        int[] env = dbHelper.createRequestTestEnvironment();
        connectorId = env[0];
        processId = env[1];
        adminId = env[2];
        operatorId = env[3];
        nonOperatorId = env[4];
    }

    // ==================== 1. IMPORT DES DEMANDES (FIXTURES) ====================

    @Nested
    @DisplayName("1. Import des demandes - Fixtures SQL")
    class ImportRequestsFixtures {

        @Test
        @DisplayName("1.1 - Création d'une demande en cours de traitement (ONGOING)")
        @Transactional
        void shouldCreateOngoingRequest() {
            // When
            int requestId = dbHelper.createOngoingRequest("ORDER-ONGOING-001", processId, connectorId);

            // Then
            Optional<Request> request = requestsRepository.findById(requestId);
            assertTrue(request.isPresent(), "Request should be created");
            assertEquals(Request.Status.ONGOING, request.get().getStatus());
            assertFalse(request.get().isRejected());
            assertNotNull(request.get().getProcess());
            assertEquals(1, dbHelper.getRequestHistoryCount(requestId), "Should have import history record");
        }

        @Test
        @DisplayName("1.2 - Création d'une demande en attente de validation (STANDBY)")
        @Transactional
        void shouldCreateStandbyRequest() {
            // When
            int requestId = dbHelper.createStandbyRequest("ORDER-STANDBY-001", processId, connectorId);

            // Then
            Optional<Request> request = requestsRepository.findById(requestId);
            assertTrue(request.isPresent(), "Request should be created");
            assertEquals(Request.Status.STANDBY, request.get().getStatus());
            assertFalse(request.get().isRejected());
            assertEquals(2, dbHelper.getRequestHistoryCount(requestId), "Should have import + standby history records");
        }

        @Test
        @DisplayName("1.3 - Création d'une demande en erreur d'import (IMPORTFAIL - aucun périmètre)")
        @Transactional
        void shouldCreateImportFailRequest() {
            // When
            int requestId = dbHelper.createImportFailRequest("ORDER-IMPORTFAIL-001", connectorId);

            // Then
            Optional<Request> request = requestsRepository.findById(requestId);
            assertTrue(request.isPresent(), "Request should be created");
            assertEquals(Request.Status.IMPORTFAIL, request.get().getStatus());
            assertNull(request.get().getPerimeter(), "Should have no perimeter (import error cause)");
            assertNull(request.get().getProcess(), "Should have no process assigned");
        }

        @Test
        @DisplayName("1.4 - Création d'une demande en erreur de traitement (ERROR)")
        @Transactional
        void shouldCreateErrorRequest() {
            // When
            int requestId = dbHelper.createErrorRequest("ORDER-ERROR-001", processId, connectorId);

            // Then
            Optional<Request> request = requestsRepository.findById(requestId);
            assertTrue(request.isPresent(), "Request should be created");
            assertEquals(Request.Status.ERROR, request.get().getStatus());
            assertFalse(request.get().isRejected());
            assertEquals(2, dbHelper.getRequestHistoryCount(requestId), "Should have import + error history records");
        }

        @Test
        @DisplayName("1.5 - Création d'une demande terminée (FINISHED)")
        @Transactional
        void shouldCreateFinishedRequest() {
            // When
            int requestId = dbHelper.createFinishedRequest("ORDER-FINISHED-001", processId, connectorId);

            // Then
            Optional<Request> request = requestsRepository.findById(requestId);
            assertTrue(request.isPresent(), "Request should be created");
            assertEquals(Request.Status.FINISHED, request.get().getStatus());
            assertFalse(request.get().isRejected());
            assertNotNull(request.get().getEndDate(), "Finished request should have end date");
        }

        @Test
        @DisplayName("1.6 - Création d'une demande annulée (rejected)")
        @Transactional
        void shouldCreateCancelledRequest() {
            // When
            String cancellationReason = "Données non disponibles pour cette zone";
            int requestId = dbHelper.createCancelledRequest("ORDER-CANCELLED-001", processId, connectorId, cancellationReason);

            // Then
            Optional<Request> request = requestsRepository.findById(requestId);
            assertTrue(request.isPresent(), "Request should be created");
            assertEquals(Request.Status.FINISHED, request.get().getStatus());
            assertTrue(request.get().isRejected(), "Should be marked as rejected");
            assertEquals(cancellationReason, request.get().getRemark());
        }

        @Test
        @DisplayName("1.7 - Toutes les demandes de test sont créées avec attributs corrects")
        @Transactional
        void shouldCreateAllRequestTypesWithCorrectAttributes() {
            // Create all request types
            int ongoingId = dbHelper.createOngoingRequest("ORDER-ONGOING", processId, connectorId);
            int standbyId = dbHelper.createStandbyRequest("ORDER-STANDBY", processId, connectorId);
            int importFailId = dbHelper.createImportFailRequest("ORDER-IMPORTFAIL", connectorId);
            int errorId = dbHelper.createErrorRequest("ORDER-ERROR", processId, connectorId);
            int finishedId = dbHelper.createFinishedRequest("ORDER-FINISHED", processId, connectorId);
            int cancelledId = dbHelper.createCancelledRequest("ORDER-CANCELLED", processId, connectorId, "Cancelled");

            // Verify all exist
            assertTrue(dbHelper.requestExists(ongoingId));
            assertTrue(dbHelper.requestExists(standbyId));
            assertTrue(dbHelper.requestExists(importFailId));
            assertTrue(dbHelper.requestExists(errorId));
            assertTrue(dbHelper.requestExists(finishedId));
            assertTrue(dbHelper.requestExists(cancelledId));

            // Verify statuses
            assertEquals("ONGOING", dbHelper.getRequestStatus(ongoingId));
            assertEquals("STANDBY", dbHelper.getRequestStatus(standbyId));
            assertEquals("IMPORTFAIL", dbHelper.getRequestStatus(importFailId));
            assertEquals("ERROR", dbHelper.getRequestStatus(errorId));
            assertEquals("FINISHED", dbHelper.getRequestStatus(finishedId));
            assertEquals("FINISHED", dbHelper.getRequestStatus(cancelledId));

            // Verify rejection flags
            assertFalse(dbHelper.isRequestRejected(ongoingId));
            assertFalse(dbHelper.isRequestRejected(standbyId));
            assertFalse(dbHelper.isRequestRejected(importFailId));
            assertFalse(dbHelper.isRequestRejected(errorId));
            assertFalse(dbHelper.isRequestRejected(finishedId));
            assertTrue(dbHelper.isRequestRejected(cancelledId));
        }
    }

    // ==================== 2. VISIBILITÉ DES DEMANDES ====================

    @Nested
    @DisplayName("2. Visibilité des demandes")
    class RequestVisibility {

        @Test
        @DisplayName("2.1 - Un administrateur peut voir toutes les demandes")
        @Transactional
        void adminCanSeeAllRequests() {
            // Given: Requests on different processes
            int process2Id = dbHelper.createTestProcess("Process 2");
            dbHelper.createTestTask(process2Id, "VALIDATION", "Validation", 1);

            int request1Id = dbHelper.createOngoingRequest("ORDER-PROCESS1", processId, connectorId);
            int request2Id = dbHelper.createOngoingRequest("ORDER-PROCESS2", process2Id, connectorId);

            // When: Admin queries all requests
            Iterable<Request> allRequests = requestsRepository.findAll();

            // Then: Admin sees all requests
            int count = 0;
            boolean foundRequest1 = false;
            boolean foundRequest2 = false;
            for (Request r : allRequests) {
                count++;
                if (r.getId() == request1Id) foundRequest1 = true;
                if (r.getId() == request2Id) foundRequest2 = true;
            }

            assertTrue(count >= 2, "Admin should see at least 2 requests");
            assertTrue(foundRequest1, "Admin should see request from process 1");
            assertTrue(foundRequest2, "Admin should see request from process 2");
        }

        @Test
        @DisplayName("2.2 - Un opérateur ne voit que les demandes de ses traitements")
        @Transactional
        void operatorSeesOnlyAssignedProcessRequests() {
            // Given: Operator is assigned to processId only
            int process2Id = dbHelper.createTestProcess("Process Not Assigned");
            dbHelper.createTestTask(process2Id, "VALIDATION", "Validation", 1);

            int request1Id = dbHelper.createOngoingRequest("ORDER-ASSIGNED", processId, connectorId);
            int request2Id = dbHelper.createOngoingRequest("ORDER-NOT-ASSIGNED", process2Id, connectorId);

            // When: Get the process the operator is assigned to
            Process assignedProcess = processesRepository.findById(processId).orElse(null);
            assertNotNull(assignedProcess);

            // Then: Operator should only see requests from assigned process
            Collection<User> operators = assignedProcess.getDistinctOperators();
            User operator = usersRepository.findById(operatorId).orElse(null);
            assertNotNull(operator);

            assertTrue(operators.contains(operator), "Operator should be in process operators");

            // Check visibility through process assignment
            Request request1 = requestsRepository.findById(request1Id).orElse(null);
            Request request2 = requestsRepository.findById(request2Id).orElse(null);

            assertNotNull(request1);
            assertNotNull(request2);

            // Request 1 should be visible (same process)
            assertTrue(request1.getProcess().getDistinctOperators().contains(operator),
                "Operator should be able to see request from assigned process");

            // Request 2 should not be visible (different process)
            assertFalse(request2.getProcess().getDistinctOperators().contains(operator),
                "Operator should NOT see request from non-assigned process");
        }

        @Test
        @DisplayName("2.3 - Un opérateur voit les demandes via groupe d'utilisateurs")
        @Transactional
        void operatorSeesRequestsThroughUserGroup() {
            // Given: Create a user group and add operator to it
            int groupId = dbHelper.createTestUserGroup("Test Operators Group");
            int newOperatorId = dbHelper.createTestOperator("group_operator", "Group Operator", "group_op@test.com", true);
            dbHelper.addUserToGroup(newOperatorId, groupId);

            // Create new process and assign group (not individual user)
            int groupProcessId = dbHelper.createTestProcess("Group Process");
            dbHelper.createTestTask(groupProcessId, "VALIDATION", "Validation", 1);
            dbHelper.assignGroupToProcess(groupId, groupProcessId);

            int requestId = dbHelper.createOngoingRequest("ORDER-GROUP", groupProcessId, connectorId);

            // When: Check operator visibility
            Process groupProcess = processesRepository.findById(groupProcessId).orElse(null);
            assertNotNull(groupProcess);

            User groupOperator = usersRepository.findById(newOperatorId).orElse(null);
            assertNotNull(groupOperator);

            // Then: Operator should see request through group membership
            Collection<User> operators = groupProcess.getDistinctOperators();
            assertTrue(operators.contains(groupOperator),
                "Operator should be visible through group membership");
        }

        @Test
        @DisplayName("2.4 - Les demandes affichent le bon statut")
        @Transactional
        void requestsDisplayCorrectStatus() {
            // Create requests with all statuses
            int ongoingId = dbHelper.createOngoingRequest("STATUS-ONGOING", processId, connectorId);
            int standbyId = dbHelper.createStandbyRequest("STATUS-STANDBY", processId, connectorId);
            int errorId = dbHelper.createErrorRequest("STATUS-ERROR", processId, connectorId);
            int finishedId = dbHelper.createFinishedRequest("STATUS-FINISHED", processId, connectorId);

            // Verify each status
            Request ongoing = requestsRepository.findById(ongoingId).orElseThrow();
            Request standby = requestsRepository.findById(standbyId).orElseThrow();
            Request error = requestsRepository.findById(errorId).orElseThrow();
            Request finished = requestsRepository.findById(finishedId).orElseThrow();

            assertEquals(Request.Status.ONGOING, ongoing.getStatus());
            assertTrue(ongoing.isActive());
            assertTrue(ongoing.isOngoing());

            assertEquals(Request.Status.STANDBY, standby.getStatus());
            assertTrue(standby.isActive());
            assertFalse(standby.isOngoing());

            assertEquals(Request.Status.ERROR, error.getStatus());
            assertTrue(error.isActive());
            assertFalse(error.isOngoing());

            assertEquals(Request.Status.FINISHED, finished.getStatus());
            assertFalse(finished.isActive());
            assertFalse(finished.isOngoing());
        }

        @Test
        @DisplayName("2.5 - Les attributs de la demande sont correctement stockés")
        @Transactional
        void requestAttributesAreCorrectlyStored() {
            // Given
            int requestId = dbHelper.createOngoingRequest("ATTRIBUTES-TEST-ORDER", processId, connectorId);

            // When
            Request request = requestsRepository.findById(requestId).orElseThrow();

            // Then - verify all key attributes
            assertNotNull(request.getId());
            assertEquals("ATTRIBUTES-TEST-ORDER", request.getOrderLabel());
            assertEquals("Test Product", request.getProductLabel());
            assertEquals("Test Client", request.getClient());
            assertEquals("Test Address", request.getClientDetails());
            assertEquals("Test Org", request.getOrganism());
            assertNotNull(request.getPerimeter());
            assertTrue(request.getPerimeter().contains("POLYGON"));
            assertNotNull(request.getStartDate());
            assertNotNull(request.getConnector());
            assertNotNull(request.getProcess());
        }
    }

    // ==================== 3. RECHERCHE ET FILTRAGE ====================

    @Nested
    @DisplayName("3. Recherche et filtrage des demandes")
    class RequestSearchAndFiltering {

        @Test
        @DisplayName("3.1 - Recherche par statut")
        @Transactional
        void findRequestsByStatus() {
            // Given
            dbHelper.createOngoingRequest("SEARCH-ONGOING-1", processId, connectorId);
            dbHelper.createOngoingRequest("SEARCH-ONGOING-2", processId, connectorId);
            dbHelper.createStandbyRequest("SEARCH-STANDBY-1", processId, connectorId);
            dbHelper.createErrorRequest("SEARCH-ERROR-1", processId, connectorId);

            // When
            List<Request> ongoingRequests = requestsRepository.findByStatus(Request.Status.ONGOING);
            List<Request> standbyRequests = requestsRepository.findByStatus(Request.Status.STANDBY);
            List<Request> errorRequests = requestsRepository.findByStatus(Request.Status.ERROR);

            // Then
            assertTrue(ongoingRequests.size() >= 2, "Should find at least 2 ONGOING requests");
            assertTrue(standbyRequests.size() >= 1, "Should find at least 1 STANDBY request");
            assertTrue(errorRequests.size() >= 1, "Should find at least 1 ERROR request");
        }

        @Test
        @DisplayName("3.2 - Recherche des demandes actives (non terminées)")
        @Transactional
        void findActiveRequests() {
            // Given
            dbHelper.createOngoingRequest("ACTIVE-ONGOING", processId, connectorId);
            dbHelper.createStandbyRequest("ACTIVE-STANDBY", processId, connectorId);
            dbHelper.createFinishedRequest("INACTIVE-FINISHED", processId, connectorId);

            // When
            List<Request> activeRequests = requestsRepository.findByStatusNot(Request.Status.FINISHED);

            // Then
            assertTrue(activeRequests.size() >= 2, "Should find at least 2 active requests");
            for (Request r : activeRequests) {
                assertNotEquals(Request.Status.FINISHED, r.getStatus());
            }
        }

        @Test
        @DisplayName("3.3 - Recherche par processus et statut")
        @Transactional
        void findRequestsByProcessAndStatus() {
            // Given
            int process2Id = dbHelper.createTestProcess("Search Process 2");
            dbHelper.createTestTask(process2Id, "VALIDATION", "Validation", 1);

            dbHelper.createStandbyRequest("P1-STANDBY", processId, connectorId);
            dbHelper.createStandbyRequest("P2-STANDBY", process2Id, connectorId);

            Process process1 = processesRepository.findById(processId).orElseThrow();
            Process process2 = processesRepository.findById(process2Id).orElseThrow();

            // When
            List<Request> p1StandbyRequests = requestsRepository.findByStatusAndProcessIn(
                Request.Status.STANDBY, List.of(process1));
            List<Request> p2StandbyRequests = requestsRepository.findByStatusAndProcessIn(
                Request.Status.STANDBY, List.of(process2));

            // Then
            assertTrue(p1StandbyRequests.size() >= 1);
            assertTrue(p2StandbyRequests.size() >= 1);

            for (Request r : p1StandbyRequests) {
                assertEquals(processId, r.getProcess().getId());
            }
            for (Request r : p2StandbyRequests) {
                assertEquals(process2Id, r.getProcess().getId());
            }
        }
    }
}
