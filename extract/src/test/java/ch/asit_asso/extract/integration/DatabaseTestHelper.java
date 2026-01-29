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
package ch.asit_asso.extract.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

/**
 * Helper class for managing database state during integration tests.
 * Provides methods to reset specific tables or the entire database.
 *
 * @author Extract Test Team
 */
@Component
public class DatabaseTestHelper {

    private final Logger logger = LoggerFactory.getLogger(DatabaseTestHelper.class);
    private final JdbcTemplate jdbcTemplate;

    /**
     * Standard password hash for test users (password: "motdepasse21")
     * Generated with Pbkdf2PasswordEncoder
     */
    public static final String TEST_PASSWORD_HASH = "c92bb53f6ac7efebb63c2ab68b87c11ab66ba104d355f9083daad5579d4265c7a892e4bc58e9b8de";

    /**
     * Standard password for test users
     */
    public static final String TEST_PASSWORD = "motdepasse21";

    public DatabaseTestHelper(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Clears all user-related data from the database while preserving system user.
     * Use this for tests that need a clean user state.
     */
    @Transactional
    public void clearAllUsers() {
        logger.debug("Clearing all users except system user");

        // Delete in correct order to respect foreign key constraints
        jdbcTemplate.execute("DELETE FROM recovery_codes WHERE id_user != 1");
        jdbcTemplate.execute("DELETE FROM remember_me_tokens WHERE id_user != 1");
        jdbcTemplate.execute("DELETE FROM users_usergroups WHERE id_user != 1");
        jdbcTemplate.execute("DELETE FROM processes_users WHERE id_user != 1");
        jdbcTemplate.execute("DELETE FROM users WHERE id_user != 1");

        logger.debug("All users cleared (except system user)");
    }

    /**
     * Clears all user groups from the database.
     */
    @Transactional
    public void clearAllUserGroups() {
        logger.debug("Clearing all user groups");

        // Delete junction tables first
        jdbcTemplate.execute("DELETE FROM users_usergroups");
        jdbcTemplate.execute("DELETE FROM processes_usergroups");
        jdbcTemplate.execute("DELETE FROM usergroups");

        logger.debug("All user groups cleared");
    }

    /**
     * Clears ALL users including admin users (but not system user).
     * This prepares the database for testing first admin creation.
     */
    @Transactional
    public void clearAllUsersForSetupTest() {
        logger.debug("Clearing all users for setup test");

        // First, clear all related data
        jdbcTemplate.execute("DELETE FROM recovery_codes");
        jdbcTemplate.execute("DELETE FROM remember_me_tokens");
        jdbcTemplate.execute("DELETE FROM users_usergroups");
        jdbcTemplate.execute("DELETE FROM processes_users");
        jdbcTemplate.execute("DELETE FROM request_history WHERE id_user IS NOT NULL AND id_user != 1");

        // Clear all users except system user
        jdbcTemplate.execute("DELETE FROM users WHERE id_user != 1");

        // Reset sequences
        resetUserSequence();

        logger.debug("Database cleared for setup test");
    }

    /**
     * Clears all data for a completely fresh database state.
     * WARNING: This removes ALL data including requests and processes.
     */
    @Transactional
    public void clearAllData() {
        logger.debug("Clearing all data from database");

        // Clear in reverse dependency order
        jdbcTemplate.execute("DELETE FROM recovery_codes");
        jdbcTemplate.execute("DELETE FROM remember_me_tokens");
        jdbcTemplate.execute("DELETE FROM request_history");
        jdbcTemplate.execute("DELETE FROM requests");
        jdbcTemplate.execute("DELETE FROM users_usergroups");
        jdbcTemplate.execute("DELETE FROM processes_users");
        jdbcTemplate.execute("DELETE FROM processes_usergroups");
        jdbcTemplate.execute("DELETE FROM tasks");
        jdbcTemplate.execute("DELETE FROM processes");
        jdbcTemplate.execute("DELETE FROM connectors");
        jdbcTemplate.execute("DELETE FROM usergroups");
        jdbcTemplate.execute("DELETE FROM users WHERE id_user != 1");

        // Reset all sequences
        resetAllSequences();

        logger.debug("All data cleared");
    }

    /**
     * Resets the user ID sequence to avoid conflicts after clearing data.
     */
    @Transactional
    public void resetUserSequence() {
        try {
            Integer maxId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id_user), 1) FROM users", Integer.class);
            int nextVal = (maxId != null ? maxId : 1) + 1;

            // Try different sequence names (depends on Hibernate configuration)
            List<String> sequenceNames = List.of(
                "users_id_user_seq",
                "user_seq",
                "hibernate_sequence"
            );

            for (String seqName : sequenceNames) {
                try {
                    jdbcTemplate.execute(String.format(
                        "SELECT setval('%s', %d, false)", seqName, nextVal));
                    logger.debug("Reset sequence {} to {}", seqName, nextVal);
                } catch (Exception e) {
                    // Sequence doesn't exist, try next
                }
            }
        } catch (Exception e) {
            logger.warn("Could not reset user sequence: {}", e.getMessage());
        }
    }

    /**
     * Resets all sequences after clearing data.
     * Note: This application uses a single hibernate_sequence for all entities.
     */
    @Transactional
    public void resetAllSequences() {
        // The application uses a single hibernate_sequence for all entities.
        // We need to ensure it's set higher than any existing ID to avoid conflicts.
        try {
            // Find the maximum ID across all tables
            List<String[]> tables = List.of(
                new String[]{"users", "id_user"},
                new String[]{"connectors", "id_connector"},
                new String[]{"processes", "id_process"},
                new String[]{"tasks", "id_task"},
                new String[]{"requests", "id_request"},
                new String[]{"request_history", "id_record"},
                new String[]{"usergroups", "id_usergroup"}
            );

            int maxId = 0;
            for (String[] tableInfo : tables) {
                try {
                    Integer tableMaxId = jdbcTemplate.queryForObject(
                        String.format("SELECT COALESCE(MAX(%s), 0) FROM %s", tableInfo[1], tableInfo[0]),
                        Integer.class);
                    if (tableMaxId != null && tableMaxId > maxId) {
                        maxId = tableMaxId;
                    }
                } catch (Exception e) {
                    // Table might not exist, skip it
                    logger.debug("Could not query max ID from {}: {}", tableInfo[0], e.getMessage());
                }
            }

            // Set hibernate_sequence to be higher than the max ID found
            int nextVal = maxId + 1;
            jdbcTemplate.execute(String.format("SELECT setval('hibernate_sequence', %d, false)", nextVal));
            logger.debug("Reset hibernate_sequence to {}", nextVal);
        } catch (Exception e) {
            logger.warn("Could not reset hibernate_sequence: {}", e.getMessage());
        }
    }

    /**
     * Creates a standard test admin user.
     *
     * @return the ID of the created admin user
     */
    @Transactional
    public int createTestAdmin() {
        return createTestAdmin("testadmin", "Test Admin", "testadmin@test.com");
    }

    /**
     * Creates a test admin user with specified credentials.
     *
     * @param login the login name
     * @param name the display name
     * @param email the email address
     * @return the ID of the created admin user
     */
    @Transactional
    public int createTestAdmin(String login, String name, String email) {
        logger.debug("Creating test admin user: {}", login);

        // Get next ID from hibernate sequence
        Integer userId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        jdbcTemplate.update(
            "INSERT INTO users(id_user, active, email, login, mailactive, name, pass, profile, two_factor_forced, two_factor_status, user_type) " +
            "VALUES(?, TRUE, ?, ?, FALSE, ?, ?, 'ADMIN', FALSE, 'INACTIVE', 'LOCAL')",
            userId, email, login, name, TEST_PASSWORD_HASH
        );

        logger.debug("Created test admin with ID: {}", userId);
        return userId != null ? userId : -1;
    }

    /**
     * Creates a test operator user.
     *
     * @param login the login name
     * @param name the display name
     * @param email the email address
     * @param active whether the user is active
     * @return the ID of the created operator user
     */
    @Transactional
    public int createTestOperator(String login, String name, String email, boolean active) {
        logger.debug("Creating test operator user: {}", login);

        // Get next ID from hibernate sequence
        Integer userId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        jdbcTemplate.update(
            "INSERT INTO users(id_user, active, email, login, locale, mailactive, name, pass, profile, two_factor_forced, two_factor_status, user_type) " +
            "VALUES(?, ?, ?, ?, 'fr', FALSE, ?, ?, 'OPERATOR', FALSE, 'INACTIVE', 'LOCAL')",
            userId, active, email, login, name, TEST_PASSWORD_HASH
        );

        logger.debug("Created test operator with ID: {}", userId);
        return userId != null ? userId : -1;
    }

    /**
     * Creates a test user group.
     *
     * @param name the group name
     * @return the ID of the created group
     */
    @Transactional
    public int createTestUserGroup(String name) {
        logger.debug("Creating test user group: {}", name);

        // Get next ID from hibernate sequence
        Integer groupId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        jdbcTemplate.update("INSERT INTO usergroups(id_usergroup, name) VALUES(?, ?)", groupId, name);

        logger.debug("Created test user group with ID: {}", groupId);
        return groupId != null ? groupId : -1;
    }

    /**
     * Adds a user to a user group.
     *
     * @param userId the user ID
     * @param groupId the group ID
     */
    @Transactional
    public void addUserToGroup(int userId, int groupId) {
        jdbcTemplate.update(
            "INSERT INTO users_usergroups(id_user, id_usergroup) VALUES(?, ?) ON CONFLICT DO NOTHING",
            userId, groupId
        );
    }

    /**
     * Creates a test process.
     *
     * @param name the process name
     * @return the ID of the created process
     */
    @Transactional
    public int createTestProcess(String name) {
        logger.debug("Creating test process: {}", name);

        // Get next ID from hibernate sequence
        Integer processId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        jdbcTemplate.update("INSERT INTO processes(id_process, name) VALUES(?, ?)", processId, name);

        logger.debug("Created test process with ID: {}", processId);
        return processId != null ? processId : -1;
    }

    /**
     * Assigns a user as operator for a process.
     *
     * @param userId the user ID
     * @param processId the process ID
     */
    @Transactional
    public void assignUserToProcess(int userId, int processId) {
        jdbcTemplate.update(
            "INSERT INTO processes_users(id_process, id_user) VALUES(?, ?) ON CONFLICT DO NOTHING",
            processId, userId
        );
    }

    /**
     * Assigns a user group as operator for a process.
     *
     * @param groupId the group ID
     * @param processId the process ID
     */
    @Transactional
    public void assignGroupToProcess(int groupId, int processId) {
        jdbcTemplate.update(
            "INSERT INTO processes_usergroups(id_process, id_usergroup) VALUES(?, ?) ON CONFLICT DO NOTHING",
            processId, groupId
        );
    }

    /**
     * Checks if the database has any admin users.
     *
     * @return true if at least one admin exists
     */
    public boolean hasAdminUser() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE profile = 'ADMIN' AND active = TRUE AND id_user != 1",
            Integer.class
        );
        return count != null && count > 0;
    }

    /**
     * Gets a user ID by login.
     *
     * @param login the user login
     * @return the user ID or null if not found
     */
    public Integer getUserIdByLogin(String login) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT id_user FROM users WHERE login = ?", Integer.class, login);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if a user is active.
     *
     * @param userId the user ID
     * @return true if the user is active
     */
    public boolean isUserActive(int userId) {
        Boolean active = jdbcTemplate.queryForObject(
            "SELECT active FROM users WHERE id_user = ?", Boolean.class, userId);
        return active != null && active;
    }

    /**
     * Sets a user's active status.
     *
     * @param userId the user ID
     * @param active the active status
     */
    @Transactional
    public void setUserActive(int userId, boolean active) {
        jdbcTemplate.update("UPDATE users SET active = ? WHERE id_user = ?", active, userId);
    }

    /**
     * Gets a user's 2FA status.
     *
     * @param userId the user ID
     * @return the 2FA status string
     */
    public String getUserTwoFactorStatus(int userId) {
        return jdbcTemplate.queryForObject(
            "SELECT two_factor_status FROM users WHERE id_user = ?", String.class, userId);
    }

    /**
     * Creates a test connector.
     *
     * @param name the connector name
     * @return the ID of the created connector
     */
    @Transactional
    public int createTestConnector(String name) {
        // Get next ID from hibernate sequence
        Integer connectorId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        jdbcTemplate.update(
            "INSERT INTO connectors(id_connector, active, connector_code, connector_label, connector_params, import_freq, name, error_count, max_retries) " +
            "VALUES(?, FALSE, 'test', 'Test Connector', '{}', 240, ?, 0, 0)",
            connectorId, name
        );

        return connectorId != null ? connectorId : -1;
    }

    // ==================== REQUEST MANAGEMENT METHODS ====================

    /**
     * Clears all requests and their history from the database.
     */
    @Transactional
    public void clearAllRequests() {
        logger.debug("Clearing all requests and request history");
        jdbcTemplate.execute("DELETE FROM request_history");
        jdbcTemplate.execute("DELETE FROM requests");
        resetRequestSequences();
        logger.debug("All requests cleared");
    }

    /**
     * Resets request-related sequences.
     * Note: This application uses hibernate_sequence for all entities,
     * so we only need to ensure hibernate_sequence is above the max used ID.
     */
    @Transactional
    public void resetRequestSequences() {
        // The application uses a single hibernate_sequence for all entities.
        // No need to reset individual sequences - hibernate_sequence is managed globally.
        // Individual sequence names like "requests_id_request_seq" don't exist in this schema.
        logger.debug("Request sequences use hibernate_sequence - no individual reset needed");
    }

    /**
     * Creates a test request with the specified status.
     *
     * @param orderLabel the order label
     * @param status the request status
     * @param processId the process ID (can be null for UNMATCHED/IMPORTFAIL)
     * @param connectorId the connector ID
     * @return the ID of the created request
     */
    @Transactional
    public int createTestRequest(String orderLabel, String status, Integer processId, int connectorId) {
        return createTestRequest(orderLabel, status, processId, connectorId, 1, false, null);
    }

    /**
     * Creates a test request with full control over parameters.
     *
     * @param orderLabel the order label
     * @param status the request status
     * @param processId the process ID (can be null)
     * @param connectorId the connector ID
     * @param tasknum the current task number
     * @param rejected whether the request is rejected
     * @param remark optional remark
     * @return the ID of the created request
     */
    @Transactional
    public int createTestRequest(String orderLabel, String status, Integer processId, int connectorId,
                                  int tasknum, boolean rejected, String remark) {
        logger.debug("Creating test request: {} with status {}", orderLabel, status);

        Integer requestId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        String guid = java.util.UUID.randomUUID().toString();

        jdbcTemplate.update(
            "INSERT INTO requests(id_request, p_client, p_clientdetails, folder_in, folder_out, p_orderguid, " +
            "p_orderlabel, p_organism, p_parameters, p_perimeter, p_productguid, p_productlabel, rejected, " +
            "remark, start_date, status, tasknum, id_connector, id_process, p_surface) " +
            "VALUES(?, 'Test Client', 'Test Address', ?, ?, ?, ?, 'Test Org', '{}', " +
            "'POLYGON((6.5 46.5,6.6 46.5,6.6 46.6,6.5 46.6,6.5 46.5))', ?, 'Test Product', ?, ?, NOW(), ?, ?, ?, ?, 10000)",
            requestId,
            guid + "/input",
            guid + "/output",
            guid,
            orderLabel,
            guid + "-prod",
            rejected,
            remark,
            status,
            tasknum,
            connectorId,
            processId
        );

        logger.debug("Created test request with ID: {}", requestId);
        return requestId != null ? requestId : -1;
    }

    /**
     * Creates an ONGOING request (in processing).
     */
    @Transactional
    public int createOngoingRequest(String orderLabel, int processId, int connectorId) {
        int requestId = createTestRequest(orderLabel, "ONGOING", processId, connectorId, 1, false, null);
        createImportHistoryRecord(requestId);
        return requestId;
    }

    /**
     * Creates a STANDBY request (awaiting operator validation).
     */
    @Transactional
    public int createStandbyRequest(String orderLabel, int processId, int connectorId) {
        int requestId = createTestRequest(orderLabel, "STANDBY", processId, connectorId, 1, false, null);
        createImportHistoryRecord(requestId);
        createStandbyHistoryRecord(requestId, "Validation opérateur");
        return requestId;
    }

    /**
     * Creates an IMPORTFAIL request (import error, e.g., no perimeter).
     */
    @Transactional
    public int createImportFailRequest(String orderLabel, int connectorId) {
        int requestId = createTestRequest(orderLabel, "IMPORTFAIL", null, connectorId, 0, false, null);
        // Update to have no perimeter (simulating import error)
        jdbcTemplate.update("UPDATE requests SET p_perimeter = NULL WHERE id_request = ?", requestId);
        createImportFailHistoryRecord(requestId, "Aucun périmètre défini pour cette commande");
        return requestId;
    }

    /**
     * Creates an ERROR request (processing error).
     */
    @Transactional
    public int createErrorRequest(String orderLabel, int processId, int connectorId) {
        int requestId = createTestRequest(orderLabel, "ERROR", processId, connectorId, 1, false, null);
        createImportHistoryRecord(requestId);
        createErrorHistoryRecord(requestId, "Tâche FME", "Erreur lors de l'exécution de la tâche");
        return requestId;
    }

    /**
     * Creates a FINISHED request (completed successfully).
     */
    @Transactional
    public int createFinishedRequest(String orderLabel, int processId, int connectorId) {
        int requestId = createTestRequest(orderLabel, "FINISHED", processId, connectorId, 2, false, null);
        jdbcTemplate.update("UPDATE requests SET end_date = NOW() WHERE id_request = ?", requestId);
        createImportHistoryRecord(requestId);
        createFinishedHistoryRecord(requestId, "Validation opérateur");
        createFinishedHistoryRecord(requestId, "Export");
        return requestId;
    }

    /**
     * Creates a cancelled (rejected) request.
     */
    @Transactional
    public int createCancelledRequest(String orderLabel, int processId, int connectorId, String reason) {
        int requestId = createTestRequest(orderLabel, "FINISHED", processId, connectorId, 2, true, reason);
        jdbcTemplate.update("UPDATE requests SET end_date = NOW() WHERE id_request = ?", requestId);
        createImportHistoryRecord(requestId);
        return requestId;
    }

    // ==================== REQUEST HISTORY HELPER METHODS ====================

    /**
     * Creates an import history record (step 0, process_step 0).
     */
    @Transactional
    public void createImportHistoryRecord(int requestId) {
        Integer recordId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        jdbcTemplate.update(
            "INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label, id_request, id_user) " +
            "VALUES(?, NOW(), 'OK', 0, NOW() - INTERVAL '1 minute', 'FINISHED', 1, 'Import', ?, 1)",
            recordId, requestId
        );
    }

    /**
     * Creates a standby history record.
     */
    @Transactional
    public void createStandbyHistoryRecord(int requestId, String taskLabel) {
        Integer recordId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        int step = getNextStepForRequest(requestId);

        jdbcTemplate.update(
            "INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label, id_request, id_user) " +
            "VALUES(?, NULL, 'En attente de validation', 1, NOW(), 'STANDBY', ?, ?, ?, 1)",
            recordId, step, taskLabel, requestId
        );
    }

    /**
     * Creates an error history record.
     */
    @Transactional
    public void createErrorHistoryRecord(int requestId, String taskLabel, String errorMessage) {
        Integer recordId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        int step = getNextStepForRequest(requestId);

        jdbcTemplate.update(
            "INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label, id_request, id_user) " +
            "VALUES(?, NOW(), ?, 1, NOW() - INTERVAL '1 minute', 'ERROR', ?, ?, ?, 1)",
            recordId, errorMessage, step, taskLabel, requestId
        );
    }

    /**
     * Creates a finished history record.
     */
    @Transactional
    public void createFinishedHistoryRecord(int requestId, String taskLabel) {
        Integer recordId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        int step = getNextStepForRequest(requestId);
        int processStep = step; // For finished records, process_step follows step

        jdbcTemplate.update(
            "INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label, id_request, id_user) " +
            "VALUES(?, NOW(), 'OK', ?, NOW() - INTERVAL '1 minute', 'FINISHED', ?, ?, ?, 1)",
            recordId, processStep, step, taskLabel, requestId
        );
    }

    /**
     * Creates an import fail history record.
     */
    @Transactional
    public void createImportFailHistoryRecord(int requestId, String errorMessage) {
        Integer recordId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        jdbcTemplate.update(
            "INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label, id_request, id_user) " +
            "VALUES(?, NOW(), ?, 0, NOW() - INTERVAL '1 minute', 'ERROR', 1, 'Import', ?, 1)",
            recordId, errorMessage, requestId
        );
    }

    /**
     * Gets the next step number for a request's history.
     */
    private int getNextStepForRequest(int requestId) {
        Integer maxStep = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(step), 0) FROM request_history WHERE id_request = ?",
            Integer.class, requestId
        );
        return (maxStep != null ? maxStep : 0) + 1;
    }

    /**
     * Gets a request's current status.
     */
    public String getRequestStatus(int requestId) {
        return jdbcTemplate.queryForObject(
            "SELECT status FROM requests WHERE id_request = ?",
            String.class, requestId
        );
    }

    /**
     * Gets a request's rejected flag.
     */
    public Boolean isRequestRejected(int requestId) {
        return jdbcTemplate.queryForObject(
            "SELECT rejected FROM requests WHERE id_request = ?",
            Boolean.class, requestId
        );
    }

    /**
     * Gets a request's remark.
     */
    public String getRequestRemark(int requestId) {
        return jdbcTemplate.queryForObject(
            "SELECT remark FROM requests WHERE id_request = ?",
            String.class, requestId
        );
    }

    /**
     * Gets a request's task number.
     */
    public Integer getRequestTasknum(int requestId) {
        return jdbcTemplate.queryForObject(
            "SELECT tasknum FROM requests WHERE id_request = ?",
            Integer.class, requestId
        );
    }

    /**
     * Checks if a request exists.
     */
    public boolean requestExists(int requestId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM requests WHERE id_request = ?",
            Integer.class, requestId
        );
        return count != null && count > 0;
    }

    /**
     * Gets the count of history records for a request.
     */
    public int getRequestHistoryCount(int requestId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM request_history WHERE id_request = ?",
            Integer.class, requestId
        );
        return count != null ? count : 0;
    }

    /**
     * Creates a task for a process.
     *
     * @param processId the process ID
     * @param taskCode the task code (e.g., "VALIDATION", "FME")
     * @param taskLabel the task label
     * @param position the position in the process
     * @return the task ID
     */
    @Transactional
    public int createTestTask(int processId, String taskCode, String taskLabel, int position) {
        Integer taskId = jdbcTemplate.queryForObject(
            "SELECT nextval('hibernate_sequence')", Integer.class);

        jdbcTemplate.update(
            "INSERT INTO tasks(id_task, task_code, task_label, task_params, position, id_process) " +
            "VALUES(?, ?, ?, '{}', ?, ?)",
            taskId, taskCode, taskLabel, position, processId
        );

        return taskId != null ? taskId : -1;
    }

    /**
     * Creates a complete test environment for request management tests.
     * Returns an array with [connectorId, processId, adminId, operatorId, nonOperatorId].
     */
    @Transactional
    public int[] createRequestTestEnvironment() {
        // Clear existing test data
        clearAllRequests();
        clearAllUsers();
        clearAllUserGroups();

        // Create connector
        int connectorId = createTestConnector("Test Connector");

        // Create process
        int processId = createTestProcess("Test Process");

        // Create validation task for the process
        createTestTask(processId, "VALIDATION", "Validation opérateur", 1);

        // Create users
        int adminId = createTestAdmin("admin_test", "Admin Test", "admin@test.com");
        int operatorId = createTestOperator("operator_test", "Operator Test", "operator@test.com", true);
        int nonOperatorId = createTestOperator("non_operator", "Non Operator", "nonop@test.com", true);

        // Assign operator to process
        assignUserToProcess(operatorId, processId);

        return new int[]{connectorId, processId, adminId, operatorId, nonOperatorId};
    }
}
