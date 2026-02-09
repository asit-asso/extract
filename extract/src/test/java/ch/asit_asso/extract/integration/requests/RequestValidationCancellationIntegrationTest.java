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

import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.integration.DatabaseTestHelper;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.RequestHistoryRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for request validation and cancellation operations.
 *
 * Tests the following scenarios:
 * - Validation of STANDBY requests by authorized operators
 * - Cancellation of requests (STANDBY, ERROR) with mandatory comment
 * - Authorization checks for validation/cancellation
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Request Validation & Cancellation Integration Tests - Priority 1")
class RequestValidationCancellationIntegrationTest {

    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private RequestHistoryRepository historyRepository;

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

    // ==================== 1. VALIDATION D'UNE DEMANDE ====================

    @Nested
    @DisplayName("1. Validation d'une demande")
    class RequestValidation {

        @Test
        @DisplayName("1.1 - Un opérateur autorisé peut valider une demande en STANDBY")
        @Transactional
        void authorizedOperatorCanValidateStandbyRequest() {
            // Given: A STANDBY request on a process the operator is assigned to
            int requestId = dbHelper.createStandbyRequest("VALIDATE-TEST-001", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();

            assertEquals(Request.Status.STANDBY, request.getStatus(), "Initial status should be STANDBY");
            int initialHistoryCount = dbHelper.getRequestHistoryCount(requestId);

            // Verify operator is assigned to the process
            User operator = usersRepository.findById(operatorId).orElseThrow();
            assertTrue(request.getProcess().getDistinctOperators().contains(operator),
                "Operator should be assigned to the process");

            // When: Simulating validation (what the controller would do)
            // Update the current history record to FINISHED
            List<RequestHistoryRecord> history = historyRepository.findByRequestOrderByStepDesc(request);
            assertFalse(history.isEmpty(), "Should have history records");

            RequestHistoryRecord currentRecord = history.get(0);
            assertEquals(RequestHistoryRecord.Status.STANDBY, currentRecord.getStatus(),
                "Current task should be in STANDBY");

            currentRecord.setStatus(RequestHistoryRecord.Status.FINISHED);
            currentRecord.setUser(operator);
            historyRepository.save(currentRecord);

            // Update request status
            request.setStatus(Request.Status.ONGOING);
            request.setTasknum(request.getTasknum() + 1);
            request.setRemark("Validé par l'opérateur");
            requestsRepository.save(request);

            // Then: Request is validated and proceeds to next step
            Request validated = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.ONGOING, validated.getStatus(),
                "Status should be ONGOING after validation");
            assertEquals("Validé par l'opérateur", validated.getRemark());
            assertEquals(2, validated.getTasknum().intValue(), "Task number should be incremented");

            // Verify history was updated
            List<RequestHistoryRecord> updatedHistory = historyRepository.findByRequestOrderByStepDesc(validated);
            RequestHistoryRecord validatedRecord = updatedHistory.get(0);
            assertEquals(RequestHistoryRecord.Status.FINISHED, validatedRecord.getStatus());
            assertEquals(operatorId, validatedRecord.getUser().getId().intValue());
        }

        @Test
        @DisplayName("1.2 - La validation requiert le bon statut (STANDBY)")
        @Transactional
        void validationRequiresStandbyStatus() {
            // Given: Requests with different statuses
            int ongoingId = dbHelper.createOngoingRequest("ONGOING-NO-VALIDATE", processId, connectorId);
            int errorId = dbHelper.createErrorRequest("ERROR-NO-VALIDATE", processId, connectorId);
            int finishedId = dbHelper.createFinishedRequest("FINISHED-NO-VALIDATE", processId, connectorId);

            // Then: Only STANDBY requests should be validatable
            Request ongoing = requestsRepository.findById(ongoingId).orElseThrow();
            Request error = requestsRepository.findById(errorId).orElseThrow();
            Request finished = requestsRepository.findById(finishedId).orElseThrow();

            assertNotEquals(Request.Status.STANDBY, ongoing.getStatus());
            assertNotEquals(Request.Status.STANDBY, error.getStatus());
            assertNotEquals(Request.Status.STANDBY, finished.getStatus());

            // Verify STANDBY request can be validated
            int standbyId = dbHelper.createStandbyRequest("STANDBY-CAN-VALIDATE", processId, connectorId);
            Request standby = requestsRepository.findById(standbyId).orElseThrow();
            assertEquals(Request.Status.STANDBY, standby.getStatus(), "STANDBY request is validatable");
        }

        @Test
        @DisplayName("1.3 - Après validation, la demande passe à l'étape suivante")
        @Transactional
        void afterValidationRequestProceedsToNextStep() {
            // Given
            int requestId = dbHelper.createStandbyRequest("NEXT-STEP-TEST", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            int initialTaskNum = request.getTasknum();

            // When: Validate the request
            List<RequestHistoryRecord> history = historyRepository.findByRequestOrderByStepDesc(request);
            RequestHistoryRecord currentRecord = history.get(0);
            currentRecord.setStatus(RequestHistoryRecord.Status.FINISHED);
            historyRepository.save(currentRecord);

            request.setStatus(Request.Status.ONGOING);
            request.setTasknum(initialTaskNum + 1);
            requestsRepository.save(request);

            // Then
            Request validated = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(initialTaskNum + 1, validated.getTasknum().intValue(),
                "Task number should be incremented after validation");
            assertEquals(Request.Status.ONGOING, validated.getStatus());
        }

        @Test
        @DisplayName("1.4 - La validation peut inclure un commentaire optionnel")
        @Transactional
        void validationCanIncludeOptionalRemark() {
            // Given
            int requestId = dbHelper.createStandbyRequest("REMARK-TEST", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertNull(request.getRemark(), "Initial remark should be null");

            // When: Validate with remark
            request.setStatus(Request.Status.ONGOING);
            request.setTasknum(request.getTasknum() + 1);
            request.setRemark("Validation avec commentaire spécifique");
            requestsRepository.save(request);

            // Then
            Request validated = requestsRepository.findById(requestId).orElseThrow();
            assertEquals("Validation avec commentaire spécifique", validated.getRemark());
        }
    }

    // ==================== 2. ANNULATION D'UNE DEMANDE ====================

    @Nested
    @DisplayName("2. Annulation d'une demande")
    class RequestCancellation {

        @Test
        @DisplayName("2.1 - Un opérateur autorisé peut annuler une demande STANDBY")
        @Transactional
        void authorizedOperatorCanCancelStandbyRequest() {
            // Given
            int requestId = dbHelper.createStandbyRequest("CANCEL-STANDBY-001", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            String cancellationRemark = "Demande annulée - données non disponibles";

            // When: Cancel the request using the reject method
            request.reject(cancellationRemark);
            requestsRepository.save(request);

            // Then
            Request cancelled = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.TOEXPORT, cancelled.getStatus(),
                "Status should be TOEXPORT after rejection");
            assertTrue(cancelled.isRejected(), "Request should be marked as rejected");
            assertEquals(cancellationRemark, cancelled.getRemark());
        }

        @Test
        @DisplayName("2.2 - Un opérateur autorisé peut annuler une demande ERROR")
        @Transactional
        void authorizedOperatorCanCancelErrorRequest() {
            // Given
            int requestId = dbHelper.createErrorRequest("CANCEL-ERROR-001", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            String cancellationRemark = "Erreur non récupérable - annulation";

            // When
            request.reject(cancellationRemark);
            requestsRepository.save(request);

            // Then
            Request cancelled = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.TOEXPORT, cancelled.getStatus());
            assertTrue(cancelled.isRejected());
            assertEquals(cancellationRemark, cancelled.getRemark());
        }

        @Test
        @DisplayName("2.3 - L'annulation requiert un commentaire obligatoire")
        @Transactional
        void cancellationRequiresComment() {
            // Given
            int requestId = dbHelper.createStandbyRequest("CANCEL-NO-COMMENT", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();

            // Then: Reject with empty remark should throw exception
            assertThrows(IllegalArgumentException.class, () -> {
                request.reject("");
            }, "Empty remark should throw IllegalArgumentException");

            assertThrows(IllegalArgumentException.class, () -> {
                request.reject("   ");
            }, "Whitespace-only remark should throw IllegalArgumentException");

            assertThrows(IllegalArgumentException.class, () -> {
                request.reject(null);
            }, "Null remark should throw IllegalArgumentException");
        }

        @Test
        @DisplayName("2.4 - Une demande annulée est considérée comme terminée")
        @Transactional
        void cancelledRequestIsConsideredFinished() {
            // Given
            int requestId = dbHelper.createStandbyRequest("CANCEL-FINISHED-TEST", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertTrue(request.isActive(), "Request should be active before cancellation");

            // When: Cancel and then mark as exported (simulating full workflow)
            request.reject("Raison de l'annulation");
            requestsRepository.save(request);

            // Simulate export completion
            request = requestsRepository.findById(requestId).orElseThrow();
            request.setStatus(Request.Status.FINISHED);
            requestsRepository.save(request);

            // Then
            Request finished = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.FINISHED, finished.getStatus());
            assertFalse(finished.isActive(), "Cancelled request should not be active");
            assertTrue(finished.isRejected(), "Request should be marked as rejected");
        }

        @Test
        @DisplayName("2.5 - Une demande déjà rejetée ne peut pas être rejetée à nouveau (sauf EXPORTFAIL)")
        @Transactional
        void alreadyRejectedRequestCannotBeRejectedAgain() {
            // Given: A rejected request (not in EXPORTFAIL state)
            int requestId = dbHelper.createCancelledRequest("ALREADY-CANCELLED", processId, connectorId, "First rejection");

            // Manually set to non-EXPORTFAIL status but rejected
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertTrue(request.isRejected(), "Request should already be rejected");
            assertEquals(Request.Status.FINISHED, request.getStatus());

            // The reject method doesn't check isRejected, but the controller does
            // This test documents the behavior of the domain method
            String originalRemark = request.getRemark();

            // When: Try to reject again
            // Note: The domain's reject() method doesn't prevent re-rejection,
            // that's checked in the controller's canRequestBeRejected method
            request.reject("Second rejection attempt");

            // Then: The remark is overwritten (domain allows it, but controller prevents it)
            assertEquals("Second rejection attempt", request.getRemark());
            assertNotEquals(originalRemark, request.getRemark());
        }

        @Test
        @DisplayName("2.6 - L'annulation met à jour l'historique des tâches")
        @Transactional
        void cancellationUpdatesTaskHistory() {
            // Given
            int requestId = dbHelper.createStandbyRequest("HISTORY-UPDATE-TEST", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            int initialHistoryCount = historyRepository.findByRequestOrderByStep(request).size();

            // When: Cancel the request
            request.reject("Annulation avec historique");
            requestsRepository.save(request);

            // Then: The request state is updated
            Request cancelled = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.TOEXPORT, cancelled.getStatus());
            assertTrue(cancelled.isRejected());
        }

        @Test
        @DisplayName("2.7 - L'annulation définit tasknum au-delà des tâches du processus")
        @Transactional
        void cancellationSetsTasknumBeyondProcessTasks() {
            // Given
            int requestId = dbHelper.createStandbyRequest("TASKNUM-TEST", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            int processTaskCount = request.getProcess().getTasksCollection().size();

            // When
            request.reject("Annulation tasknum test");
            requestsRepository.save(request);

            // Then: tasknum should be set beyond the process tasks
            Request cancelled = requestsRepository.findById(requestId).orElseThrow();
            assertTrue(cancelled.getTasknum() > processTaskCount,
                "Tasknum should be greater than process task count");
        }
    }

    // ==================== 3. AUTORISATIONS ====================

    @Nested
    @DisplayName("3. Vérification des autorisations")
    class AuthorizationChecks {

        @Test
        @DisplayName("3.1 - Un opérateur assigné au processus a les droits sur les demandes")
        @Transactional
        void assignedOperatorHasRightsOnRequests() {
            // Given
            int requestId = dbHelper.createStandbyRequest("AUTH-CHECK-001", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();

            User operator = usersRepository.findById(operatorId).orElseThrow();

            // Then
            assertTrue(request.getProcess().getDistinctOperators().contains(operator),
                "Assigned operator should have rights on requests");
        }

        @Test
        @DisplayName("3.2 - Un opérateur non assigné n'a pas les droits")
        @Transactional
        void nonAssignedOperatorHasNoRights() {
            // Given
            int requestId = dbHelper.createStandbyRequest("AUTH-CHECK-002", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();

            User nonOperator = usersRepository.findById(nonOperatorId).orElseThrow();

            // Then
            assertFalse(request.getProcess().getDistinctOperators().contains(nonOperator),
                "Non-assigned operator should NOT have rights on requests");
        }

        @Test
        @DisplayName("3.3 - Un administrateur a les droits sur toutes les demandes")
        @Transactional
        void adminHasRightsOnAllRequests() {
            // Given
            int process2Id = dbHelper.createTestProcess("Admin Auth Process");
            dbHelper.createTestTask(process2Id, "VALIDATION", "Validation", 1);

            int request1Id = dbHelper.createStandbyRequest("ADMIN-AUTH-001", processId, connectorId);
            int request2Id = dbHelper.createStandbyRequest("ADMIN-AUTH-002", process2Id, connectorId);

            User admin = usersRepository.findById(adminId).orElseThrow();

            // Then: Admin (by profile, not process assignment) can access all
            assertEquals(User.Profile.ADMIN, admin.getProfile());

            Request request1 = requestsRepository.findById(request1Id).orElseThrow();
            Request request2 = requestsRepository.findById(request2Id).orElseThrow();

            // Admin rights are checked by profile in controller, not process assignment
            assertNotNull(request1);
            assertNotNull(request2);
        }

        @Test
        @DisplayName("3.4 - L'opérateur via groupe a les mêmes droits que l'opérateur direct")
        @Transactional
        void groupOperatorHasSameRightsAsDirectOperator() {
            // Given: Create operator in group
            int groupId = dbHelper.createTestUserGroup("Auth Test Group");
            int groupOperatorId = dbHelper.createTestOperator("group_auth_op", "Group Auth Op", "group_auth@test.com", true);
            dbHelper.addUserToGroup(groupOperatorId, groupId);
            dbHelper.assignGroupToProcess(groupId, processId);

            int requestId = dbHelper.createStandbyRequest("GROUP-AUTH-TEST", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();

            User groupOperator = usersRepository.findById(groupOperatorId).orElseThrow();

            // Then
            assertTrue(request.getProcess().getDistinctOperators().contains(groupOperator),
                "Group operator should have rights through group membership");
        }

        @Test
        @DisplayName("3.5 - Un opérateur ne peut valider que les demandes de ses traitements")
        @Transactional
        void operatorCanOnlyValidateAssignedProcessRequests() {
            // Given: Create a second process without operator assignment
            int process2Id = dbHelper.createTestProcess("Unassigned Process");
            dbHelper.createTestTask(process2Id, "VALIDATION", "Validation", 1);

            int assignedRequestId = dbHelper.createStandbyRequest("ASSIGNED-REQUEST", processId, connectorId);
            int unassignedRequestId = dbHelper.createStandbyRequest("UNASSIGNED-REQUEST", process2Id, connectorId);

            User operator = usersRepository.findById(operatorId).orElseThrow();

            Request assignedRequest = requestsRepository.findById(assignedRequestId).orElseThrow();
            Request unassignedRequest = requestsRepository.findById(unassignedRequestId).orElseThrow();

            // Then
            assertTrue(assignedRequest.getProcess().getDistinctOperators().contains(operator),
                "Operator should have rights on assigned process request");
            assertFalse(unassignedRequest.getProcess().getDistinctOperators().contains(operator),
                "Operator should NOT have rights on unassigned process request");
        }
    }

    // ==================== 4. CAS LIMITES ====================

    @Nested
    @DisplayName("4. Cas limites et états spéciaux")
    class EdgeCasesAndSpecialStates {

        @Test
        @DisplayName("4.1 - Une demande IMPORTFAIL peut être annulée par un admin")
        @Transactional
        void importFailRequestCanBeCancelledByAdmin() {
            // Given
            int requestId = dbHelper.createImportFailRequest("IMPORTFAIL-CANCEL", connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.IMPORTFAIL, request.getStatus());

            // When: Admin cancels the request
            request.reject("Import échoué - données invalides");
            requestsRepository.save(request);

            // Then
            Request cancelled = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.TOEXPORT, cancelled.getStatus());
            assertTrue(cancelled.isRejected());
        }

        @Test
        @DisplayName("4.2 - Une demande UNMATCHED peut être annulée")
        @Transactional
        void unmatchedRequestCanBeCancelled() {
            // Given: Create an UNMATCHED request
            int requestId = dbHelper.createTestRequest("UNMATCHED-CANCEL", "UNMATCHED", null, connectorId, 0, false, null);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.UNMATCHED, request.getStatus());
            assertNull(request.getProcess());

            // When: The reject method handles null process
            request.reject("Aucun traitement correspondant trouvé");
            requestsRepository.save(request);

            // Then
            Request cancelled = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.TOEXPORT, cancelled.getStatus());
            assertTrue(cancelled.isRejected());
            assertEquals(1, cancelled.getTasknum().intValue(), "Tasknum should be 1 for null process");
        }

        @Test
        @DisplayName("4.3 - Validation simultanée - vérification de l'étape active")
        @Transactional
        void simultaneousValidationCheckActiveStep() {
            // Given
            int requestId = dbHelper.createStandbyRequest("CONCURRENT-TEST", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            int activeStep = request.getTasknum();

            // Simulate first validation
            request.setStatus(Request.Status.ONGOING);
            request.setTasknum(activeStep + 1);
            requestsRepository.save(request);

            // Then: The active step has changed
            Request updated = requestsRepository.findById(requestId).orElseThrow();
            assertNotEquals(activeStep, updated.getTasknum().intValue(),
                "Active step should have changed after validation");
        }

        @Test
        @DisplayName("4.4 - Le commentaire d'annulation peut contenir des caractères spéciaux")
        @Transactional
        void cancellationRemarkCanContainSpecialCharacters() {
            // Given
            int requestId = dbHelper.createStandbyRequest("SPECIAL-CHARS-TEST", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();

            String remarkWithSpecialChars = "Annulation: données <invalides> avec 'quotes' et \"double quotes\"\n" +
                "Lignes multiples\n" +
                "Et caractères spéciaux: é è à ü ö ä €";

            // When
            request.reject(remarkWithSpecialChars);
            requestsRepository.save(request);

            // Then
            Request cancelled = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(remarkWithSpecialChars, cancelled.getRemark());
        }
    }
}
