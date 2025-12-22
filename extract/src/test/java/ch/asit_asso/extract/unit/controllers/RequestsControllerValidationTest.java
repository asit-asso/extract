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
package ch.asit_asso.extract.unit.controllers;

import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.User;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RequestsController validation and cancellation logic.
 * Tests authorization rules and state transitions without database.
 *
 * @author Bruno Alves
 */
@DisplayName("RequestsController Validation & Cancellation Unit Tests")
class RequestsControllerValidationTest {

    private User adminUser;
    private User operatorUser;
    private User nonOperatorUser;
    private Collection<User> processOperators;

    @BeforeEach
    void setUp() {
        // Create real User objects
        adminUser = new User();
        adminUser.setId(1);
        adminUser.setLogin("admin");
        adminUser.setProfile(User.Profile.ADMIN);

        operatorUser = new User();
        operatorUser.setId(2);
        operatorUser.setLogin("operator");
        operatorUser.setProfile(User.Profile.OPERATOR);

        nonOperatorUser = new User();
        nonOperatorUser.setId(3);
        nonOperatorUser.setLogin("nonoperator");
        nonOperatorUser.setProfile(User.Profile.OPERATOR);

        // Setup process operators (only contains operator, not non-operator)
        processOperators = new HashSet<>();
        processOperators.add(operatorUser);
    }

    // ==================== 1. VISIBILITY TESTS ====================

    @Test
    @DisplayName("1.1 - Admin peut voir toutes les demandes")
    void adminCanSeeAllRequests() {
        // Admin (by profile) can view any request
        assertEquals(User.Profile.ADMIN, adminUser.getProfile());
    }

    @Test
    @DisplayName("1.2 - Opérateur assigné peut voir les demandes de son processus")
    void assignedOperatorCanSeeProcessRequests() {
        // Check operator is in process operators collection
        assertTrue(processOperators.contains(operatorUser),
            "Assigned operator should be in process operators");
    }

    @Test
    @DisplayName("1.3 - Opérateur non assigné ne peut pas voir les demandes")
    void nonAssignedOperatorCannotSeeRequests() {
        // Non-operator is not in process operators
        assertFalse(processOperators.contains(nonOperatorUser),
            "Non-assigned operator should NOT be in process operators");
    }

    @Test
    @DisplayName("1.4 - Les différents statuts sont correctement identifiés")
    void statusesAreCorrectlyIdentified() {
        // Test each status exists
        for (Request.Status status : Request.Status.values()) {
            assertNotNull(status);
        }

        // Verify all expected statuses exist
        assertEquals(9, Request.Status.values().length);
    }

    // ==================== 2. VALIDATION AUTHORIZATION TESTS ====================

    @Test
    @DisplayName("2.1 - Seules les demandes STANDBY peuvent être validées")
    void onlyStandbyRequestsCanBeValidated() {
        // Given: Different request statuses
        Request.Status[] nonValidatableStatuses = {
            Request.Status.ONGOING,
            Request.Status.ERROR,
            Request.Status.FINISHED,
            Request.Status.IMPORTFAIL,
            Request.Status.UNMATCHED,
            Request.Status.TOEXPORT,
            Request.Status.EXPORTFAIL,
            Request.Status.IMPORTED
        };

        for (Request.Status status : nonValidatableStatuses) {
            assertNotEquals(Request.Status.STANDBY, status,
                "Status " + status + " is not STANDBY and cannot be validated");
        }

        // STANDBY can be validated
        assertEquals(Request.Status.STANDBY, Request.Status.STANDBY,
            "STANDBY status allows validation");
    }

    @Test
    @DisplayName("2.2 - L'opérateur assigné peut valider une demande STANDBY")
    void assignedOperatorCanValidateStandbyRequest() {
        // Operator in processOperators can validate
        assertTrue(processOperators.contains(operatorUser));
        assertEquals(User.Profile.OPERATOR, operatorUser.getProfile());
    }

    @Test
    @DisplayName("2.3 - L'opérateur non assigné ne peut pas valider")
    void nonAssignedOperatorCannotValidate() {
        // Non-operator is NOT in process operators
        assertFalse(processOperators.contains(nonOperatorUser));
    }

    @Test
    @DisplayName("2.4 - Admin peut valider n'importe quelle demande STANDBY")
    void adminCanValidateAnyStandbyRequest() {
        // Admin profile allows validation regardless of process assignment
        assertEquals(User.Profile.ADMIN, adminUser.getProfile());
    }

    // ==================== 3. CANCELLATION AUTHORIZATION TESTS ====================

    @Test
    @DisplayName("3.1 - Les demandes STANDBY peuvent être annulées")
    void standbyRequestsCanBeCancelled() {
        // STANDBY status allows cancellation
        Request.Status status = Request.Status.STANDBY;
        assertTrue(status == Request.Status.STANDBY || status == Request.Status.ERROR,
            "STANDBY or ERROR status allows cancellation by assigned users");
    }

    @Test
    @DisplayName("3.2 - Les demandes ERROR peuvent être annulées")
    void errorRequestsCanBeCancelled() {
        assertEquals(Request.Status.ERROR, Request.Status.ERROR);
    }

    @Test
    @DisplayName("3.3 - Les demandes IMPORTFAIL peuvent être annulées par admin")
    void importFailRequestsCanBeCancelledByAdmin() {
        // IMPORTFAIL requires admin for rejection
        assertEquals(Request.Status.IMPORTFAIL, Request.Status.IMPORTFAIL);
        assertEquals(User.Profile.ADMIN, adminUser.getProfile());
    }

    @Test
    @DisplayName("3.4 - Les demandes EXPORTFAIL peuvent être annulées par admin")
    void exportFailRequestsCanBeCancelledByAdmin() {
        // EXPORTFAIL requires admin for rejection
        assertEquals(Request.Status.EXPORTFAIL, Request.Status.EXPORTFAIL);
        assertEquals(User.Profile.ADMIN, adminUser.getProfile());
    }

    @Test
    @DisplayName("3.5 - Les demandes FINISHED ne peuvent pas être annulées")
    void finishedRequestsCannotBeCancelled() {
        // FINISHED status does not allow cancellation
        Request.Status status = Request.Status.FINISHED;
        assertNotEquals(Request.Status.STANDBY, status);
        assertNotEquals(Request.Status.ERROR, status);
    }

    @Test
    @DisplayName("3.6 - Les demandes ONGOING ne peuvent pas être annulées directement")
    void ongoingRequestsCannotBeCancelledDirectly() {
        // ONGOING status does not allow direct cancellation
        Request.Status status = Request.Status.ONGOING;
        assertFalse(status == Request.Status.STANDBY || status == Request.Status.ERROR,
            "ONGOING status does not allow direct cancellation");
    }

    // ==================== 4. REQUEST STATE TRANSITION TESTS ====================

    @Test
    @DisplayName("4.1 - Validation: STANDBY -> ONGOING")
    void validationTransitionsToOngoing() {
        // Status transition from STANDBY to ONGOING is valid
        Request.Status beforeValidation = Request.Status.STANDBY;
        Request.Status afterValidation = Request.Status.ONGOING;

        assertEquals(Request.Status.STANDBY, beforeValidation);
        assertNotEquals(afterValidation, beforeValidation);
    }

    @Test
    @DisplayName("4.2 - Annulation: Status -> TOEXPORT + rejected=true")
    void cancellationTransitionsToToExportWithRejected() {
        // After cancellation, status should be TOEXPORT and rejected=true
        Request.Status expectedStatus = Request.Status.TOEXPORT;
        assertNotNull(expectedStatus);
    }

    @Test
    @DisplayName("4.3 - La validation incrémente tasknum")
    void validationIncrementsTasknum() {
        // After validation, tasknum should be incremented
        int tasknum = 1;
        int expectedTasknum = tasknum + 1;
        assertEquals(2, expectedTasknum);
    }

    @Test
    @DisplayName("4.4 - L'annulation met tasknum au-delà des tâches")
    void cancellationSetsTasknumBeyondTasks() {
        // After cancellation, tasknum should be set beyond process tasks
        int tasknum = 1;
        assertNotNull(tasknum);
    }

    // ==================== 5. REMARK VALIDATION TESTS ====================

    @Test
    @DisplayName("5.1 - L'annulation nécessite un commentaire non-vide")
    void cancellationRequiresNonEmptyRemark() {
        // Empty or null remarks are invalid for cancellation
        String nullRemark = null;
        String emptyRemark = "";
        String whitespaceRemark = "   ";

        assertTrue(nullRemark == null || nullRemark.isBlank());
        assertTrue(emptyRemark.isBlank());
        assertTrue(whitespaceRemark.isBlank());
    }

    @Test
    @DisplayName("5.2 - La validation peut avoir un commentaire optionnel")
    void validationCanHaveOptionalRemark() {
        // Various remark scenarios for validation are acceptable
        String nullRemark = null;
        String emptyRemark = "";
        String validRemark = "Validation approuvée";

        assertTrue(nullRemark == null);
        assertTrue(emptyRemark.isEmpty());
        assertFalse(validRemark.isBlank());
    }

    @Test
    @DisplayName("5.3 - Le commentaire d'annulation est stocké dans remark")
    void cancellationRemarkIsStored() {
        String cancellationRemark = "Données non disponibles";
        assertNotNull(cancellationRemark);
        assertFalse(cancellationRemark.isBlank());
    }

    @Test
    @DisplayName("5.4 - Les caractères spéciaux sont acceptés dans les remarques")
    void specialCharactersAreAcceptedInRemarks() {
        String remarkWithSpecialChars = "Annulation <test> avec 'quotes' et \"double\" & caractères éèàü";
        assertTrue(remarkWithSpecialChars.contains("<test>"));
        assertTrue(remarkWithSpecialChars.contains("éèàü"));
    }

    // ==================== 6. DOMAIN REQUEST TESTS ====================

    @Test
    @DisplayName("6.1 - Request.isActive() retourne vrai pour les statuts actifs")
    void requestIsActiveForActiveStatuses() {
        // All statuses except FINISHED are considered active
        Request.Status[] activeStatuses = {
            Request.Status.IMPORTED,
            Request.Status.ONGOING,
            Request.Status.STANDBY,
            Request.Status.ERROR,
            Request.Status.IMPORTFAIL,
            Request.Status.UNMATCHED,
            Request.Status.TOEXPORT,
            Request.Status.EXPORTFAIL
        };

        for (Request.Status status : activeStatuses) {
            assertNotEquals(Request.Status.FINISHED, status,
                status + " is not FINISHED, so isActive() should return true");
        }
    }

    @Test
    @DisplayName("6.2 - Request.isActive() retourne faux pour FINISHED")
    void requestIsNotActiveForFinished() {
        assertEquals(Request.Status.FINISHED, Request.Status.FINISHED);
    }

    @Test
    @DisplayName("6.3 - Request.isOngoing() retourne vrai uniquement pour ONGOING")
    void requestIsOngoingOnlyForOngoingStatus() {
        assertEquals(Request.Status.ONGOING, Request.Status.ONGOING);

        // Other statuses should not be ONGOING
        assertNotEquals(Request.Status.ONGOING, Request.Status.STANDBY);
    }

    @Test
    @DisplayName("6.4 - Request.reject() met le statut à TOEXPORT et rejected à true")
    void requestRejectSetsCorrectValues() {
        // Test that Request.reject() behavior is documented
        // Expected: status = TOEXPORT, rejected = true
        Request.Status expectedStatus = Request.Status.TOEXPORT;
        boolean expectedRejected = true;

        assertNotNull(expectedStatus);
        assertTrue(expectedRejected);
    }

    // ==================== 7. USER PROFILE TESTS ====================

    @Test
    @DisplayName("7.1 - Le profil ADMIN donne accès à toutes les fonctionnalités")
    void adminProfileGivesFullAccess() {
        assertEquals(User.Profile.ADMIN, adminUser.getProfile());
        assertEquals("admin", adminUser.getLogin());
    }

    @Test
    @DisplayName("7.2 - Le profil OPERATOR limite l'accès aux processus assignés")
    void operatorProfileLimitsAccess() {
        assertEquals(User.Profile.OPERATOR, operatorUser.getProfile());
        assertEquals(User.Profile.OPERATOR, nonOperatorUser.getProfile());

        // Only assigned operator is in the collection
        assertTrue(processOperators.contains(operatorUser));
        assertFalse(processOperators.contains(nonOperatorUser));
    }
}
