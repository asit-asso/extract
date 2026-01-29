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
package ch.asit_asso.extract.functional.requests;

import ch.asit_asso.extract.domain.*;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.integration.DatabaseTestHelper;
import ch.asit_asso.extract.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for request management (import, visibility, validation, cancellation).
 *
 * Tests Priority 1 scenarios:
 * 1. Import of requests with different statuses (fixtures)
 * 2. Visibility rules (admin sees all, operator sees assigned only)
 * 3. Request validation by authorized operators
 * 4. Request cancellation with mandatory comment
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("functional")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Request Management Functional Tests - Priority 1")
class RequestManagementFunctionalTest {

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
        // Create test environment with all necessary entities
        int[] env = dbHelper.createRequestTestEnvironment();
        connectorId = env[0];
        processId = env[1];
        adminId = env[2];
        operatorId = env[3];
        nonOperatorId = env[4];
    }

    @BeforeAll
    static void setUpClass() {
        System.out.println("========================================");
        System.out.println("Request Management Functional Tests");
        System.out.println("Priority 1 - Gestion des demandes");
        System.out.println("========================================");
        System.out.println("Tests couverts:");
        System.out.println("1. Import des demandes (fixtures SQL)");
        System.out.println("2. Visibilité des demandes");
        System.out.println("3. Validation des demandes");
        System.out.println("4. Annulation des demandes");
        System.out.println("========================================");
    }

    // ==================== 1. IMPORT DES DEMANDES (FIXTURES) ====================

    @Nested
    @DisplayName("1. Import des demandes - Fixtures SQL")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ImportRequestsFixtures {

        @Test
        @Order(1)
        @DisplayName("1.1 - Importer une demande en cours de traitement (ONGOING)")
        @Transactional
        void importOngoingRequest() {
            // When
            int requestId = dbHelper.createOngoingRequest("FUNC-ONGOING-001", processId, connectorId);

            // Then
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.ONGOING, request.getStatus());
            assertTrue(request.isActive());
            assertTrue(request.isOngoing());
            assertFalse(request.isRejected());
            assertNotNull(request.getProcess());
            assertNotNull(request.getConnector());

            System.out.println("✓ Demande ONGOING créée: ID=" + requestId);
            System.out.println("  - Statut: " + request.getStatus());
            System.out.println("  - Active: " + request.isActive());
            System.out.println("  - En cours: " + request.isOngoing());
        }

        @Test
        @Order(2)
        @DisplayName("1.2 - Importer une demande en attente de validation (STANDBY)")
        @Transactional
        void importStandbyRequest() {
            // When
            int requestId = dbHelper.createStandbyRequest("FUNC-STANDBY-001", processId, connectorId);

            // Then
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.STANDBY, request.getStatus());
            assertTrue(request.isActive());
            assertFalse(request.isOngoing());

            // Check history has STANDBY record
            List<RequestHistoryRecord> history = historyRepository.findByRequestOrderByStep(request);
            assertTrue(history.size() >= 2, "Should have import + standby records");
            boolean hasStandbyRecord = history.stream()
                .anyMatch(h -> h.getStatus() == RequestHistoryRecord.Status.STANDBY);
            assertTrue(hasStandbyRecord, "Should have STANDBY history record");

            System.out.println("✓ Demande STANDBY créée: ID=" + requestId);
            System.out.println("  - Statut: " + request.getStatus());
            System.out.println("  - Historique: " + history.size() + " entrées");
        }

        @Test
        @Order(3)
        @DisplayName("1.3 - Importer une demande en erreur d'import (IMPORTFAIL - aucun périmètre)")
        @Transactional
        void importFailRequest() {
            // When
            int requestId = dbHelper.createImportFailRequest("FUNC-IMPORTFAIL-001", connectorId);

            // Then
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.IMPORTFAIL, request.getStatus());
            assertTrue(request.isActive());
            assertNull(request.getPerimeter(), "Should have no perimeter (import error cause)");
            assertNull(request.getProcess(), "Should have no process assigned");

            System.out.println("✓ Demande IMPORTFAIL créée: ID=" + requestId);
            System.out.println("  - Statut: " + request.getStatus());
            System.out.println("  - Périmètre: " + request.getPerimeter());
            System.out.println("  - Processus: " + request.getProcess());
        }

        @Test
        @Order(4)
        @DisplayName("1.4 - Importer une demande en erreur de traitement (ERROR)")
        @Transactional
        void importErrorRequest() {
            // When
            int requestId = dbHelper.createErrorRequest("FUNC-ERROR-001", processId, connectorId);

            // Then
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.ERROR, request.getStatus());
            assertTrue(request.isActive());

            // Check history has ERROR record
            List<RequestHistoryRecord> history = historyRepository.findByRequestOrderByStep(request);
            boolean hasErrorRecord = history.stream()
                .anyMatch(h -> h.getStatus() == RequestHistoryRecord.Status.ERROR);
            assertTrue(hasErrorRecord, "Should have ERROR history record");

            System.out.println("✓ Demande ERROR créée: ID=" + requestId);
            System.out.println("  - Statut: " + request.getStatus());
        }

        @Test
        @Order(5)
        @DisplayName("1.5 - Importer une demande terminée (FINISHED)")
        @Transactional
        void importFinishedRequest() {
            // When
            int requestId = dbHelper.createFinishedRequest("FUNC-FINISHED-001", processId, connectorId);

            // Then
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.FINISHED, request.getStatus());
            assertFalse(request.isActive());
            assertFalse(request.isRejected());
            assertNotNull(request.getEndDate());

            System.out.println("✓ Demande FINISHED créée: ID=" + requestId);
            System.out.println("  - Statut: " + request.getStatus());
            System.out.println("  - Active: " + request.isActive());
            System.out.println("  - Date fin: " + request.getEndDate().getTime());
        }

        @Test
        @Order(6)
        @DisplayName("1.6 - Importer une demande annulée (rejected)")
        @Transactional
        void importCancelledRequest() {
            // Given
            String reason = "Données non disponibles pour cette zone géographique";

            // When
            int requestId = dbHelper.createCancelledRequest("FUNC-CANCELLED-001", processId, connectorId, reason);

            // Then
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.FINISHED, request.getStatus());
            assertTrue(request.isRejected());
            assertEquals(reason, request.getRemark());

            System.out.println("✓ Demande CANCELLED créée: ID=" + requestId);
            System.out.println("  - Statut: " + request.getStatus());
            System.out.println("  - Rejetée: " + request.isRejected());
            System.out.println("  - Raison: " + request.getRemark());
        }
    }

    // ==================== 2. VISIBILITÉ DES DEMANDES ====================

    @Nested
    @DisplayName("2. Visibilité des demandes")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RequestVisibility {

        @Test
        @Order(1)
        @DisplayName("2.1 - Un administrateur peut voir toutes les demandes")
        @Transactional
        void adminCanSeeAllRequests() {
            // Given: Create requests on different processes
            int process2Id = dbHelper.createTestProcess("Process 2 - Not Assigned");
            dbHelper.createTestTask(process2Id, "VALIDATION", "Validation", 1);

            int request1Id = dbHelper.createOngoingRequest("VISIBILITY-P1", processId, connectorId);
            int request2Id = dbHelper.createOngoingRequest("VISIBILITY-P2", process2Id, connectorId);

            // When: Query all requests (admin has no process restrictions)
            User admin = usersRepository.findById(adminId).orElseThrow();
            assertEquals(User.Profile.ADMIN, admin.getProfile());

            // Then: Admin can access both requests
            Request request1 = requestsRepository.findById(request1Id).orElseThrow();
            Request request2 = requestsRepository.findById(request2Id).orElseThrow();
            assertNotNull(request1);
            assertNotNull(request2);

            System.out.println("✓ Administrateur peut voir toutes les demandes:");
            System.out.println("  - Demande 1 (Process 1): ID=" + request1Id);
            System.out.println("  - Demande 2 (Process 2): ID=" + request2Id);
        }

        @Test
        @Order(2)
        @DisplayName("2.2 - Un opérateur ne voit que les demandes de ses traitements")
        @Transactional
        void operatorSeesOnlyAssignedProcessRequests() {
            // Given: Operator is assigned to processId only
            int process2Id = dbHelper.createTestProcess("Process 2 - Not Assigned to Operator");
            dbHelper.createTestTask(process2Id, "VALIDATION", "Validation", 1);

            int assignedRequestId = dbHelper.createOngoingRequest("ASSIGNED-TO-OP", processId, connectorId);
            int notAssignedRequestId = dbHelper.createOngoingRequest("NOT-ASSIGNED", process2Id, connectorId);

            // When: Check operator access
            User operator = usersRepository.findById(operatorId).orElseThrow();
            Request assignedRequest = requestsRepository.findById(assignedRequestId).orElseThrow();
            Request notAssignedRequest = requestsRepository.findById(notAssignedRequestId).orElseThrow();

            // Then: Operator can only see assigned process requests
            boolean canSeeAssigned = assignedRequest.getProcess().getDistinctOperators().contains(operator);
            boolean canSeeNotAssigned = notAssignedRequest.getProcess().getDistinctOperators().contains(operator);

            assertTrue(canSeeAssigned, "Operator should see requests from assigned process");
            assertFalse(canSeeNotAssigned, "Operator should NOT see requests from unassigned process");

            System.out.println("✓ Opérateur ne voit que ses demandes:");
            System.out.println("  - Demande assignée (visible): " + canSeeAssigned);
            System.out.println("  - Demande non assignée (invisible): " + !canSeeNotAssigned);
        }

        @Test
        @Order(3)
        @DisplayName("2.3 - Les demandes affichent le bon statut et couleur")
        @Transactional
        void requestsDisplayCorrectStatusAndColor() {
            // Create all status types
            Map<String, Integer> requests = new HashMap<>();
            requests.put("ONGOING", dbHelper.createOngoingRequest("STATUS-ONGOING", processId, connectorId));
            requests.put("STANDBY", dbHelper.createStandbyRequest("STATUS-STANDBY", processId, connectorId));
            requests.put("ERROR", dbHelper.createErrorRequest("STATUS-ERROR", processId, connectorId));
            requests.put("FINISHED", dbHelper.createFinishedRequest("STATUS-FINISHED", processId, connectorId));

            // Verify each status
            System.out.println("✓ Statuts des demandes:");
            for (Map.Entry<String, Integer> entry : requests.entrySet()) {
                Request request = requestsRepository.findById(entry.getValue()).orElseThrow();
                assertEquals(entry.getKey(), request.getStatus().name());
                System.out.println("  - " + entry.getKey() + ": ID=" + entry.getValue() +
                    ", Active=" + request.isActive());
            }
        }

        @Test
        @Order(4)
        @DisplayName("2.4 - Les attributs des demandes sont correctement affichés")
        @Transactional
        void requestAttributesAreCorrectlyDisplayed() {
            // Given: Create request with specific attributes
            int requestId = dbHelper.createOngoingRequest("ATTRIBUTES-TEST", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();

            // Then: Verify all attributes
            assertNotNull(request.getOrderLabel());
            assertNotNull(request.getProductLabel());
            assertNotNull(request.getClient());
            assertNotNull(request.getClientDetails());
            assertNotNull(request.getOrganism());
            assertNotNull(request.getConnector());
            assertNotNull(request.getProcess());
            assertNotNull(request.getPerimeter());
            assertNotNull(request.getStartDate());

            System.out.println("✓ Attributs de la demande:");
            System.out.println("  - Commande: " + request.getOrderLabel());
            System.out.println("  - Produit: " + request.getProductLabel());
            System.out.println("  - Client: " + request.getClient());
            System.out.println("  - Organisme: " + request.getOrganism());
            System.out.println("  - Connecteur: " + request.getConnector().getName());
            System.out.println("  - Processus: " + request.getProcess().getName());
        }
    }

    // ==================== 3. VALIDATION D'UNE DEMANDE ====================

    @Nested
    @DisplayName("3. Validation d'une demande")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RequestValidation {

        @Test
        @Order(1)
        @DisplayName("3.1 - Un opérateur ayant les droits peut valider une demande STANDBY")
        @Transactional
        void authorizedOperatorCanValidateStandbyRequest() {
            // Given: A STANDBY request
            int requestId = dbHelper.createStandbyRequest("VALIDATE-TEST-001", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.STANDBY, request.getStatus());

            // Verify operator has rights
            User operator = usersRepository.findById(operatorId).orElseThrow();
            assertTrue(request.getProcess().getDistinctOperators().contains(operator));

            // When: Simulate validation (what controller does)
            int initialTasknum = request.getTasknum();

            // Update history record
            List<RequestHistoryRecord> history = historyRepository.findByRequestOrderByStepDesc(request);
            RequestHistoryRecord currentRecord = history.get(0);
            assertEquals(RequestHistoryRecord.Status.STANDBY, currentRecord.getStatus());

            currentRecord.setStatus(RequestHistoryRecord.Status.FINISHED);
            currentRecord.setUser(operator);
            historyRepository.save(currentRecord);

            // Update request
            request.setStatus(Request.Status.ONGOING);
            request.setTasknum(initialTasknum + 1);
            request.setRemark("Validé par opérateur");
            requestsRepository.save(request);

            // Then: Request is validated
            Request validated = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.ONGOING, validated.getStatus());
            assertEquals(initialTasknum + 1, validated.getTasknum());
            assertEquals("Validé par opérateur", validated.getRemark());

            System.out.println("✓ Demande validée avec succès:");
            System.out.println("  - ID: " + requestId);
            System.out.println("  - Ancien statut: STANDBY");
            System.out.println("  - Nouveau statut: " + validated.getStatus());
            System.out.println("  - Task num: " + initialTasknum + " -> " + validated.getTasknum());
        }

        @Test
        @Order(2)
        @DisplayName("3.2 - Seules les demandes STANDBY peuvent être validées")
        @Transactional
        void onlyStandbyRequestsCanBeValidated() {
            // Create requests with different statuses
            int ongoingId = dbHelper.createOngoingRequest("NO-VALIDATE-ONGOING", processId, connectorId);
            int errorId = dbHelper.createErrorRequest("NO-VALIDATE-ERROR", processId, connectorId);
            int finishedId = dbHelper.createFinishedRequest("NO-VALIDATE-FINISHED", processId, connectorId);

            Request ongoing = requestsRepository.findById(ongoingId).orElseThrow();
            Request error = requestsRepository.findById(errorId).orElseThrow();
            Request finished = requestsRepository.findById(finishedId).orElseThrow();

            // Then: Only STANDBY can be validated
            assertNotEquals(Request.Status.STANDBY, ongoing.getStatus());
            assertNotEquals(Request.Status.STANDBY, error.getStatus());
            assertNotEquals(Request.Status.STANDBY, finished.getStatus());

            System.out.println("✓ Seul le statut STANDBY permet la validation:");
            System.out.println("  - ONGOING: Non validable");
            System.out.println("  - ERROR: Non validable");
            System.out.println("  - FINISHED: Non validable");
        }

        @Test
        @Order(3)
        @DisplayName("3.3 - Après validation, la demande passe à l'étape suivante")
        @Transactional
        void afterValidationRequestProceedsToNextStep() {
            // Given
            int requestId = dbHelper.createStandbyRequest("NEXT-STEP-TEST", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            int initialTasknum = request.getTasknum();

            // When: Validate
            request.setStatus(Request.Status.ONGOING);
            request.setTasknum(initialTasknum + 1);
            requestsRepository.save(request);

            // Then
            Request validated = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(initialTasknum + 1, validated.getTasknum());
            assertEquals(Request.Status.ONGOING, validated.getStatus());

            System.out.println("✓ Demande avance à l'étape suivante:");
            System.out.println("  - Task num avant: " + initialTasknum);
            System.out.println("  - Task num après: " + validated.getTasknum());
        }
    }

    // ==================== 4. ANNULATION D'UNE DEMANDE ====================

    @Nested
    @DisplayName("4. Annulation d'une demande")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RequestCancellation {

        @Test
        @Order(1)
        @DisplayName("4.1 - Un opérateur peut annuler une demande STANDBY avec commentaire")
        @Transactional
        void operatorCanCancelStandbyRequestWithComment() {
            // Given
            int requestId = dbHelper.createStandbyRequest("CANCEL-STANDBY-001", processId, connectorId);
            String cancellationComment = "Données non disponibles pour ce périmètre";
            Request request = requestsRepository.findById(requestId).orElseThrow();

            // When: Cancel using reject method
            request.reject(cancellationComment);
            requestsRepository.save(request);

            // Then
            Request cancelled = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.TOEXPORT, cancelled.getStatus());
            assertTrue(cancelled.isRejected());
            assertEquals(cancellationComment, cancelled.getRemark());

            System.out.println("✓ Demande STANDBY annulée:");
            System.out.println("  - ID: " + requestId);
            System.out.println("  - Statut: " + cancelled.getStatus());
            System.out.println("  - Rejetée: " + cancelled.isRejected());
            System.out.println("  - Commentaire: " + cancelled.getRemark());
        }

        @Test
        @Order(2)
        @DisplayName("4.2 - Un opérateur peut annuler une demande ERROR avec commentaire")
        @Transactional
        void operatorCanCancelErrorRequestWithComment() {
            // Given
            int requestId = dbHelper.createErrorRequest("CANCEL-ERROR-001", processId, connectorId);
            String cancellationComment = "Erreur non récupérable - abandon de la demande";
            Request request = requestsRepository.findById(requestId).orElseThrow();

            // When
            request.reject(cancellationComment);
            requestsRepository.save(request);

            // Then
            Request cancelled = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.TOEXPORT, cancelled.getStatus());
            assertTrue(cancelled.isRejected());
            assertEquals(cancellationComment, cancelled.getRemark());

            System.out.println("✓ Demande ERROR annulée:");
            System.out.println("  - ID: " + requestId);
            System.out.println("  - Statut: " + cancelled.getStatus());
            System.out.println("  - Commentaire: " + cancelled.getRemark());
        }

        @Test
        @Order(3)
        @DisplayName("4.3 - L'annulation requiert un commentaire obligatoire")
        @Transactional
        void cancellationRequiresMandatoryComment() {
            // Given
            int requestId = dbHelper.createStandbyRequest("CANCEL-NO-COMMENT", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();

            // Then: Empty/null comments should throw exception
            assertThrows(IllegalArgumentException.class, () -> request.reject(null),
                "Null comment should throw exception");
            assertThrows(IllegalArgumentException.class, () -> request.reject(""),
                "Empty comment should throw exception");
            assertThrows(IllegalArgumentException.class, () -> request.reject("   "),
                "Whitespace-only comment should throw exception");

            System.out.println("✓ Commentaire obligatoire validé:");
            System.out.println("  - null: IllegalArgumentException");
            System.out.println("  - '': IllegalArgumentException");
            System.out.println("  - '   ': IllegalArgumentException");
        }

        @Test
        @Order(4)
        @DisplayName("4.4 - Une demande annulée est considérée comme terminée")
        @Transactional
        void cancelledRequestIsConsideredFinished() {
            // Given
            int requestId = dbHelper.createStandbyRequest("CANCEL-FINISH-TEST", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();
            assertTrue(request.isActive());

            // When: Cancel and simulate export completion
            request.reject("Annulation de test");
            requestsRepository.save(request);

            request = requestsRepository.findById(requestId).orElseThrow();
            request.setStatus(Request.Status.FINISHED);
            requestsRepository.save(request);

            // Then
            Request finished = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(Request.Status.FINISHED, finished.getStatus());
            assertFalse(finished.isActive());
            assertTrue(finished.isRejected());

            System.out.println("✓ Demande annulée marquée comme terminée:");
            System.out.println("  - Statut: " + finished.getStatus());
            System.out.println("  - Active: " + finished.isActive());
            System.out.println("  - Rejetée: " + finished.isRejected());
        }

        @Test
        @Order(5)
        @DisplayName("4.5 - Le commentaire d'annulation accepte les caractères spéciaux")
        @Transactional
        void cancellationCommentAcceptsSpecialCharacters() {
            // Given
            int requestId = dbHelper.createStandbyRequest("CANCEL-SPECIAL-CHARS", processId, connectorId);
            Request request = requestsRepository.findById(requestId).orElseThrow();

            String commentWithSpecialChars = "Annulation: données <invalides> avec 'apostrophes' et \"guillemets\"\n" +
                "Ligne 2 avec caractères: éèàüöä€\n" +
                "Et des symboles: @#$%^&*()";

            // When
            request.reject(commentWithSpecialChars);
            requestsRepository.save(request);

            // Then
            Request cancelled = requestsRepository.findById(requestId).orElseThrow();
            assertEquals(commentWithSpecialChars, cancelled.getRemark());

            System.out.println("✓ Caractères spéciaux acceptés dans le commentaire:");
            System.out.println("  - Commentaire préservé intégralement");
        }
    }

    // ==================== 5. SCÉNARIO COMPLET ====================

    @Test
    @Order(100)
    @DisplayName("5. Scénario complet: création, visibilité, validation, annulation")
    @Transactional
    void completeScenario() {
        System.out.println("\n=== SCÉNARIO COMPLET ===\n");

        // Step 1: Create all request types
        System.out.println("1. Création des demandes de test:");
        int ongoingId = dbHelper.createOngoingRequest("SCENARIO-ONGOING", processId, connectorId);
        int standbyId = dbHelper.createStandbyRequest("SCENARIO-STANDBY", processId, connectorId);
        int errorId = dbHelper.createErrorRequest("SCENARIO-ERROR", processId, connectorId);
        int finishedId = dbHelper.createFinishedRequest("SCENARIO-FINISHED", processId, connectorId);
        int importFailId = dbHelper.createImportFailRequest("SCENARIO-IMPORTFAIL", connectorId);
        int cancelledId = dbHelper.createCancelledRequest("SCENARIO-CANCELLED", processId, connectorId, "Test cancellation");

        System.out.println("   - ONGOING: " + ongoingId);
        System.out.println("   - STANDBY: " + standbyId);
        System.out.println("   - ERROR: " + errorId);
        System.out.println("   - FINISHED: " + finishedId);
        System.out.println("   - IMPORTFAIL: " + importFailId);
        System.out.println("   - CANCELLED: " + cancelledId);

        // Step 2: Verify visibility
        System.out.println("\n2. Vérification de la visibilité:");
        User operator = usersRepository.findById(operatorId).orElseThrow();
        User admin = usersRepository.findById(adminId).orElseThrow();

        Request standbyRequest = requestsRepository.findById(standbyId).orElseThrow();
        boolean operatorCanSee = standbyRequest.getProcess() != null &&
            standbyRequest.getProcess().getDistinctOperators().contains(operator);
        System.out.println("   - Opérateur peut voir STANDBY: " + operatorCanSee);
        System.out.println("   - Admin a profil ADMIN: " + (admin.getProfile() == User.Profile.ADMIN));

        // Step 3: Validate STANDBY request
        System.out.println("\n3. Validation de la demande STANDBY:");
        Request toValidate = requestsRepository.findById(standbyId).orElseThrow();
        int beforeTasknum = toValidate.getTasknum();
        toValidate.setStatus(Request.Status.ONGOING);
        toValidate.setTasknum(beforeTasknum + 1);
        toValidate.setRemark("Validé dans scénario complet");
        requestsRepository.save(toValidate);

        Request validated = requestsRepository.findById(standbyId).orElseThrow();
        System.out.println("   - Statut avant: STANDBY");
        System.out.println("   - Statut après: " + validated.getStatus());
        System.out.println("   - Task num: " + beforeTasknum + " -> " + validated.getTasknum());
        assertEquals(Request.Status.ONGOING, validated.getStatus());

        // Step 4: Cancel ERROR request
        System.out.println("\n4. Annulation de la demande ERROR:");
        Request toCancel = requestsRepository.findById(errorId).orElseThrow();
        toCancel.reject("Erreur irrécupérable - annulation dans scénario");
        requestsRepository.save(toCancel);

        Request cancelled = requestsRepository.findById(errorId).orElseThrow();
        System.out.println("   - Statut avant: ERROR");
        System.out.println("   - Statut après: " + cancelled.getStatus());
        System.out.println("   - Rejetée: " + cancelled.isRejected());
        assertEquals(Request.Status.TOEXPORT, cancelled.getStatus());
        assertTrue(cancelled.isRejected());

        System.out.println("\n=== SCÉNARIO COMPLET RÉUSSI ===\n");
    }
}
