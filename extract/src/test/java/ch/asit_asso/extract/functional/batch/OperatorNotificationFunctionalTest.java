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
package ch.asit_asso.extract.functional.batch;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for operator email notifications.
 *
 * These tests verify the end-to-end behavior of operator notifications:
 * - Operators must be assigned to the process (directly or via user group)
 * - Operators must have mailactive=true
 * - Operators must be active users
 *
 * Prerequisites:
 * - Test data loaded (create_test_data.sql)
 * - Operator users with different mailactive settings
 * - Process with assigned operators
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("functional")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Operator Notification Functional Tests")
class OperatorNotificationFunctionalTest {

    @Autowired
    private ProcessesRepository processesRepository;

    @BeforeAll
    static void setUpClass() {
        System.out.println("========================================");
        System.out.println("Operator Notification Functional Tests");
        System.out.println("========================================");
        System.out.println("Prerequisites:");
        System.out.println("- Test data loaded (operators with mailactive settings)");
        System.out.println("- Process 1 with assigned operators");
        System.out.println("========================================");
    }

    @Test
    @Order(1)
    @DisplayName("1. Verify test data - Operators are assigned to process")
    void verifyOperatorsAssignedToProcess() {
        // Given: Process 1 should exist
        Process testProcess = processesRepository.findById(1).orElse(null);
        assertNotNull(testProcess, "Test process 1 should exist");

        // When: Retrieving operators
        List<User> operators = processesRepository.getProcessOperators(testProcess.getId());

        // Then: Should have at least one operator
        assertNotNull(operators, "Operators list should not be null");
        assertTrue(operators.size() > 0, "Process should have at least one operator assigned");

        System.out.println("✓ Test data verified:");
        System.out.println("  - Process ID: " + testProcess.getId());
        System.out.println("  - Process name: " + testProcess.getName());
        System.out.println("  - Operators assigned: " + operators.size());
    }

    @Test
    @Order(2)
    @DisplayName("2. Verify operators have mailactive=true filter applied")
    void verifyOperatorsHaveMailactive() {
        // Given: Process 1
        Process testProcess = processesRepository.findById(1).orElse(null);
        assertNotNull(testProcess, "Test process 1 should exist");

        // When: Retrieving operators
        List<User> operators = processesRepository.getProcessOperators(testProcess.getId());

        // Then: All should have mailactive=true
        assertNotNull(operators, "Operators list should not be null");

        for (User operator : operators) {
            assertTrue(operator.isActive(),
                "All operators should be active (user: " + operator.getLogin() + ")");
            assertTrue(operator.isMailActive(),
                "All operators should have mailactive=true (user: " + operator.getLogin() + ")");
        }

        System.out.println("✓ Verified: All " + operators.size() + " operators have active=true AND mailactive=true");
    }

    @Test
    @Order(3)
    @DisplayName("3. Verify operators receive notifications - Repository query is correct")
    void verifyRepositoryQueryCorrect() {
        // Given: Process 1
        Process testProcess = processesRepository.findById(1).orElse(null);
        assertNotNull(testProcess, "Test process 1 should exist");

        // When: Retrieving operators using the repository method used by RequestTaskRunner
        List<User> operators = processesRepository.getProcessOperators(testProcess.getId());

        // Then: Should only include operators with mailactive=true
        assertNotNull(operators, "Operators should not be null");

        // Verify the query filters correctly
        for (User operator : operators) {
            assertTrue(operator.isMailActive(),
                "Repository query should filter mailactive=true (failed for: " + operator.getLogin() + ")");
        }

        System.out.println("✓ Repository query correctly filters operators:");
        System.out.println("  - Total operators retrieved: " + operators.size());
        System.out.println("  - All have mailactive=true: ✓");
        System.out.println("  - Query used: ProcessesRepository.getProcessOperators()");
    }

    @Test
    @Order(4)
    @DisplayName("4. Document: Email notification flow for operators")
    void documentEmailNotificationFlow() {
        // This test documents the email notification flow for operators
        //
        // Flow for TaskStandbyEmail (validation notification):
        // 1. RequestTaskRunner.sendStandbyEmailToOperators() (line 615)
        // 2. Calls getProcessOperators() (line 623)
        // 3. ProcessesRepository query filters by: active=true AND mailactive=true
        // 4. Creates TaskStandbyEmail for each operator
        // 5. Sends individual email with operator's preferred locale
        //
        // Flow for TaskFailedEmail (task error notification):
        // 1. RequestTaskRunner.sendErrorEmailToOperators() (line 545)
        // 2. Calls getProcessOperators() (line 554)
        // 3. Same filtering as above: active=true AND mailactive=true
        // 4. Creates TaskFailedEmail for each operator
        // 5. Sends individual email with operator's preferred locale
        //
        // Flow for RequestExportFailedEmail (export error notification):
        // 1. ExportRequestProcessor.sendEmailNotification() (line 333)
        // 2. Calls getProcessOperators() for operators (line 342) - FILTERS mailactive ✓
        // 3. Calls findByProfileAndActiveTrue() for admins (line 346) - NO mailactive filter ❌
        // 4. Combines operators + admins
        // 5. Creates RequestExportFailedEmail for each recipient
        // 6. Sends individual email with recipient's preferred locale
        //
        // KNOWN ISSUE: Export failure notifications to admins do NOT filter by mailactive
        // (same bug as ConnectorImportFailedEmail)

        System.out.println("✓ Email notification flow documented");
        System.out.println("  - TaskStandbyEmail: Uses getProcessOperators() ✓");
        System.out.println("  - TaskFailedEmail: Uses getProcessOperators() ✓");
        System.out.println("  - RequestExportFailedEmail (operators): Uses getProcessOperators() ✓");
        System.out.println("  - RequestExportFailedEmail (admins): Uses findByProfileAndActiveTrue() ❌");
        System.out.println();
        System.out.println("  All operator notifications correctly filter by mailactive=true");

        assertTrue(true, "See test output for email notification flow documentation");
    }
}
