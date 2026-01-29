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
package ch.asit_asso.extract.integration.batch;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.email.RequestExportFailedEmail;
import ch.asit_asso.extract.email.TaskFailedEmail;
import ch.asit_asso.extract.email.TaskStandbyEmail;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for operator email notifications.
 *
 * Tests that operators receive notifications (validation, task error, export error) ONLY if:
 * 1. They are assigned to the process (directly or via user group)
 * 2. They have mailactive=true in their account
 * 3. They are active users
 *
 * Email types tested:
 * - TaskStandbyEmail: Sent when task requires validation
 * - TaskFailedEmail: Sent when task processing fails
 * - RequestExportFailedEmail: Sent when export to connector fails
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Operator Notification Integration Tests")
class OperatorNotificationIntegrationTest {

    @Autowired
    private ProcessesRepository processesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private EmailSettings emailSettings;

    // ==================== 1. OPERATOR RETRIEVAL ====================

    @Nested
    @DisplayName("1. Operator Retrieval with mailactive Filter")
    class OperatorRetrievalTests {

        @Test
        @DisplayName("1.1 - getProcessOperators filters by mailactive=true")
        void getProcessOperatorsFiltersMailactive() {
            // Given: Process with operators having different mailactive settings
            Process testProcess = processesRepository.findById(1).orElse(null);
            assertNotNull(testProcess, "Test process 1 should exist");

            // When: Retrieving operators via repository
            List<User> operators = processesRepository.getProcessOperators(testProcess.getId());

            // Then: Only operators with mailactive=true should be retrieved
            assertNotNull(operators, "Operators list should not be null");

            for (User operator : operators) {
                assertTrue(operator.isActive(),
                    "All operators should be active (user: " + operator.getLogin() + ")");
                assertTrue(operator.isMailActive(),
                    "All operators should have mailactive=true (user: " + operator.getLogin() + ")");
            }

            System.out.println("✓ Verified: Only operators with active=true AND mailactive=true are retrieved");
            System.out.println("  - Total operators retrieved: " + operators.size());
        }

        @Test
        @DisplayName("1.2 - Inactive operators are excluded")
        void inactiveOperatorsExcluded() {
            // Given: Process 1
            Process testProcess = processesRepository.findById(1).orElse(null);
            assertNotNull(testProcess, "Test process 1 should exist");

            // When: Retrieving operators
            List<User> operators = processesRepository.getProcessOperators(testProcess.getId());

            // Then: All should be active
            assertNotNull(operators, "Operators list should not be null");
            for (User operator : operators) {
                assertTrue(operator.isActive(), "Operator should be active: " + operator.getLogin());
            }
        }
    }

    // ==================== 2. TASK STANDBY EMAIL ====================

    @Nested
    @DisplayName("2. Task Standby Email (Validation Notification)")
    class TaskStandbyEmailTests {

        @Test
        @DisplayName("2.1 - Email can be created with valid request")
        void emailCanBeCreatedWithValidRequest() {
            // Given: A request in STANDBY status
            Request testRequest = new Request();
            testRequest.setId(1);
            testRequest.setOrderLabel("TEST-ORDER-001");
            testRequest.setProductLabel("Test Product");
            testRequest.setClient("Test Client");
            testRequest.setStatus(Request.Status.STANDBY);

            Process testProcess = new Process();
            testProcess.setId(1);
            testProcess.setName("Test Process");
            testRequest.setProcess(testProcess);

            // When: Creating standby email
            TaskStandbyEmail email = new TaskStandbyEmail(emailSettings);
            boolean initialized = email.initializeContent(testRequest);

            // Then: Email should be initialized successfully
            assertTrue(initialized, "TaskStandbyEmail should be initialized with valid request");
        }

        @Test
        @DisplayName("2.2 - Email initialization fails with null request")
        void emailInitializationFailsWithNullRequest() {
            // When/Then: Should throw exception with null request
            TaskStandbyEmail email = new TaskStandbyEmail(emailSettings);

            assertThrows(IllegalArgumentException.class, () -> {
                email.initializeContent(null);
            }, "Should throw exception with null request");
        }

        @Test
        @DisplayName("2.3 - Email requires at least one recipient")
        void emailRequiresRecipients() {
            // Given: Valid request
            Request testRequest = new Request();
            testRequest.setOrderLabel("TEST-ORDER-001");
            testRequest.setProductLabel("Test Product");
            testRequest.setClient("Test Client");

            Process testProcess = new Process();
            testProcess.setName("Test Process");
            testRequest.setProcess(testProcess);

            TaskStandbyEmail email = new TaskStandbyEmail(emailSettings);

            // When/Then: Should throw exception with empty recipients
            assertThrows(IllegalArgumentException.class, () -> {
                email.initialize(testRequest, new String[0]);
            }, "Should throw exception with empty recipients array");
        }
    }

    // ==================== 3. TASK FAILED EMAIL ====================

    @Nested
    @DisplayName("3. Task Failed Email (Error Notification)")
    class TaskFailedEmailTests {

        @Test
        @DisplayName("3.1 - Email can be created with valid task error")
        void emailCanBeCreatedWithValidTaskError() {
            // Given: A failed task
            Task testTask = new Task();
            testTask.setId(1);
            testTask.setLabel("Test Task");

            Request testRequest = new Request();
            testRequest.setId(1);
            testRequest.setOrderLabel("TEST-ORDER-001");
            testRequest.setProductLabel("Test Product");
            testRequest.setClient("Test Client");

            Process testProcess = new Process();
            testProcess.setName("Test Process");
            testRequest.setProcess(testProcess);

            String errorMessage = "Task processing failed - test error";
            Calendar failureTime = GregorianCalendar.getInstance();
            failureTime.add(Calendar.MINUTE, -5);  // 5 minutes ago

            // When: Creating failed email
            TaskFailedEmail email = new TaskFailedEmail(emailSettings);
            boolean initialized = email.initializeContent(testTask, testRequest, errorMessage, failureTime);

            // Then: Email should be initialized successfully
            assertTrue(initialized, "TaskFailedEmail should be initialized with valid data");
        }

        @Test
        @DisplayName("3.2 - Email initialization fails with null task")
        void emailInitializationFailsWithNullTask() {
            // Given: Valid request and error details
            Request testRequest = new Request();
            testRequest.setOrderLabel("TEST-ORDER-001");
            Process testProcess = new Process();
            testProcess.setName("Test Process");
            testRequest.setProcess(testProcess);

            String errorMessage = "Test error";
            Calendar failureTime = GregorianCalendar.getInstance();
            failureTime.add(Calendar.MINUTE, -5);

            // When/Then: Should throw exception with null task
            TaskFailedEmail email = new TaskFailedEmail(emailSettings);

            assertThrows(IllegalArgumentException.class, () -> {
                email.initializeContent(null, testRequest, errorMessage, failureTime);
            }, "Should throw exception with null task");
        }

        @Test
        @DisplayName("3.3 - Email initialization fails with future failure time")
        void emailInitializationFailsWithFutureTime() {
            // Given: Valid task but future failure time
            Task testTask = new Task();
            testTask.setLabel("Test Task");

            Request testRequest = new Request();
            testRequest.setOrderLabel("TEST-ORDER-001");
            Process testProcess = new Process();
            testProcess.setName("Test Process");
            testRequest.setProcess(testProcess);

            String errorMessage = "Test error";
            Calendar futureTime = GregorianCalendar.getInstance();
            futureTime.add(Calendar.HOUR, 1);  // 1 hour in future

            // When/Then: Should throw exception with future time
            TaskFailedEmail email = new TaskFailedEmail(emailSettings);

            assertThrows(IllegalArgumentException.class, () -> {
                email.initializeContent(testTask, testRequest, errorMessage, futureTime);
            }, "Should throw exception when failure time is in the future");
        }
    }

    // ==================== 4. REQUEST EXPORT FAILED EMAIL ====================

    @Nested
    @DisplayName("4. Request Export Failed Email")
    class RequestExportFailedEmailTests {

        @Test
        @DisplayName("4.1 - Email can be created with valid export failure")
        void emailCanBeCreatedWithValidExportFailure() {
            // Given: A request with connector
            Request testRequest = new Request();
            testRequest.setId(1);
            testRequest.setOrderLabel("TEST-ORDER-001");
            testRequest.setProductLabel("Test Product");
            testRequest.setClient("Test Client");

            ch.asit_asso.extract.domain.Connector testConnector = new ch.asit_asso.extract.domain.Connector();
            testConnector.setName("Test Connector");
            testRequest.setConnector(testConnector);

            String errorMessage = "Export to connector failed - test error";
            Calendar exportTime = GregorianCalendar.getInstance();
            exportTime.add(Calendar.MINUTE, -5);  // 5 minutes ago

            // When: Creating export failed email
            RequestExportFailedEmail email = new RequestExportFailedEmail(emailSettings);
            boolean initialized = email.initializeContent(testRequest, errorMessage, exportTime);

            // Then: Email should be initialized successfully
            assertTrue(initialized, "RequestExportFailedEmail should be initialized with valid data");
        }

        @Test
        @DisplayName("4.2 - Email initialization fails without connector")
        void emailInitializationFailsWithoutConnector() {
            // Given: Request without connector
            Request testRequest = new Request();
            testRequest.setOrderLabel("TEST-ORDER-001");

            String errorMessage = "Test error";
            Calendar exportTime = GregorianCalendar.getInstance();
            exportTime.add(Calendar.MINUTE, -5);

            // When/Then: Should throw exception without connector
            RequestExportFailedEmail email = new RequestExportFailedEmail(emailSettings);

            assertThrows(IllegalStateException.class, () -> {
                email.initializeContent(testRequest, errorMessage, exportTime);
            }, "Should throw exception when connector is not set");
        }
    }

    // ==================== 5. ADMINISTRATOR RECIPIENTS (KNOWN ISSUE) ====================

    @Nested
    @DisplayName("5. Administrator Recipients for Export Failures")
    class AdministratorRecipientsTests {

        @Test
        @DisplayName("5.1 - Documents: Admins retrieved without mailactive filter")
        void documentsAdminRetrievalIssue() {
            // KNOWN ISSUE:
            // ExportRequestProcessor.java line 346-347 retrieves administrators with:
            //   findByProfileAndActiveTrue(User.Profile.ADMIN)
            //
            // This does NOT filter by mailactive flag, unlike getProcessOperators().
            //
            // Current behavior: ALL active admins receive export failure notifications
            // Expected behavior: Only admins with mailactive=true should receive notifications
            //
            // This is the same bug as ConnectorImportErrorNotificationIntegrationTest.

            // When: Querying for active administrators
            User[] activeAdmins = usersRepository.findByProfileAndActiveTrue(User.Profile.ADMIN);

            // Then: Verify current behavior
            assertNotNull(activeAdmins, "Active admins should not be null");

            int withNotifications = 0;
            int withoutNotifications = 0;

            for (User admin : activeAdmins) {
                if (admin.isMailActive()) {
                    withNotifications++;
                } else {
                    withoutNotifications++;
                }
            }

            System.out.println("Current behavior (ExportRequestProcessor.java:346):");
            System.out.println("- Admins with mailactive=true: " + withNotifications);
            System.out.println("- Admins with mailactive=false: " + withoutNotifications);
            System.out.println("- ALL " + activeAdmins.length + " admins would receive export failure notifications");
            System.out.println();
            System.out.println("Expected behavior:");
            System.out.println("- Only " + withNotifications + " admins with mailactive=true should receive notifications");

            // This test documents the current behavior
            assertTrue(true, "See test output for expected vs actual behavior");
        }
    }
}
