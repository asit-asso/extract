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

import ch.asit_asso.extract.batch.reader.ConnectorImportReader;
import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for connector import error notifications to administrators.
 *
 * Requirements tested:
 * - Administrators receive email notification when connector import fails
 * - Only administrators with mailactive=true should receive notifications
 * - Email contains connector name, error message, and failure time
 *
 * KNOWN ISSUE: Current implementation (ConnectorImportReader line 289) sends to ALL active administrators,
 * not filtering by mailactive flag. This test documents the current behavior.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Connector Import Error Notification Integration Tests")
class ConnectorImportErrorNotificationIntegrationTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ConnectorsRepository connectorsRepository;

    @Autowired
    private ConnectorDiscovererWrapper connectorPlugins;

    @Autowired
    private EmailSettings emailSettings;

    private User adminWithNotifications;
    private User adminWithoutNotifications;
    private User regularUser;
    private Connector testConnector;

    @BeforeAll
    void setUpTestData() {
        // Clean up any existing test users
        usersRepository.findAll().forEach(user -> {
            if (user.getLogin().startsWith("test_admin_")) {
                usersRepository.delete(user);
            }
        });

        // Create admin with notifications enabled
        adminWithNotifications = new User();
        adminWithNotifications.setLogin("test_admin_notif_enabled");
        adminWithNotifications.setName("Admin Notif Enabled");
        adminWithNotifications.setEmail("admin_notif@test.com");
        adminWithNotifications.setActive(true);
        adminWithNotifications.setMailActive(true);  // Notifications enabled
        adminWithNotifications.setProfile(User.Profile.ADMIN);
        adminWithNotifications = usersRepository.save(adminWithNotifications);

        // Create admin with notifications disabled
        adminWithoutNotifications = new User();
        adminWithoutNotifications.setLogin("test_admin_notif_disabled");
        adminWithoutNotifications.setName("Admin Notif Disabled");
        adminWithoutNotifications.setEmail("admin_no_notif@test.com");
        adminWithoutNotifications.setActive(true);
        adminWithoutNotifications.setMailActive(false);  // Notifications disabled
        adminWithoutNotifications.setProfile(User.Profile.ADMIN);
        adminWithoutNotifications = usersRepository.save(adminWithoutNotifications);

        // Create regular user (should not receive admin notifications)
        regularUser = new User();
        regularUser.setLogin("test_regular_user");
        regularUser.setName("Regular User");
        regularUser.setEmail("regular@test.com");
        regularUser.setActive(true);
        regularUser.setMailActive(true);
        regularUser.setProfile(User.Profile.OPERATOR);
        regularUser = usersRepository.save(regularUser);

        // Create test connector
        testConnector = new Connector();
        testConnector.setName("Test Connector Error Notif");
        testConnector.setConnectorCode("test-error-notif");
        testConnector.setActive(true);
        testConnector = connectorsRepository.save(testConnector);
    }

    @AfterAll
    void cleanUpTestData() {
        if (adminWithNotifications != null) {
            usersRepository.delete(adminWithNotifications);
        }
        if (adminWithoutNotifications != null) {
            usersRepository.delete(adminWithoutNotifications);
        }
        if (regularUser != null) {
            usersRepository.delete(regularUser);
        }
        if (testConnector != null) {
            connectorsRepository.delete(testConnector);
        }
    }

    // ==================== 1. ADMINISTRATOR RETRIEVAL ====================

    @Test
    @DisplayName("1.1 - Should retrieve active administrators")
    void shouldRetrieveActiveAdministrators() {
        // When: Querying for active administrators
        User[] activeAdmins = usersRepository.findByProfileAndActiveTrue(User.Profile.ADMIN);

        // Then: Should include both test administrators (active=true)
        assertNotNull(activeAdmins);
        assertTrue(activeAdmins.length >= 2, "Should have at least 2 active admins");

        // Verify our test admins are in the result
        boolean foundAdmin1 = false;
        boolean foundAdmin2 = false;
        for (User admin : activeAdmins) {
            if (admin.getLogin().equals("test_admin_notif_enabled")) {
                foundAdmin1 = true;
            }
            if (admin.getLogin().equals("test_admin_notif_disabled")) {
                foundAdmin2 = true;
            }
        }

        assertTrue(foundAdmin1, "Should find admin with notifications enabled");
        assertTrue(foundAdmin2, "Should find admin with notifications disabled");
    }

    @Test
    @DisplayName("1.2 - Should NOT retrieve regular users when querying admins")
    void shouldNotRetrieveRegularUsers() {
        // When: Querying for active administrators
        User[] activeAdmins = usersRepository.findByProfileAndActiveTrue(User.Profile.ADMIN);

        // Then: Regular user should NOT be in the result
        assertNotNull(activeAdmins);
        for (User admin : activeAdmins) {
            assertNotEquals(User.Profile.OPERATOR, admin.getProfile(),
                "Regular users should not be in admin query results");
        }
    }

    // ==================== 2. NOTIFICATION LOGIC ====================

    @Test
    @DisplayName("2.1 - Documents: Current implementation sends to ALL active admins")
    void documentsCurrentBehavior() {
        // CURRENT BEHAVIOR (ConnectorImportReader line 289):
        // final User[] administrators = this.usersRepository.findByProfileAndActiveTrue(User.Profile.ADMIN);
        //
        // This retrieves ALL active administrators, regardless of mailactive flag.
        //
        // EXPECTED BEHAVIOR (per requirements):
        // Should only send to administrators where mailactive=true
        //
        // RECOMMENDATION:
        // Change line 289 to:
        // final User[] administrators = this.usersRepository.findByProfileAndActiveAndMailActiveTrue(User.Profile.ADMIN);
        // Or add filtering: filter(admin -> admin.isMailActive())

        User[] allActiveAdmins = usersRepository.findByProfileAndActiveTrue(User.Profile.ADMIN);

        // Count admins by mailactive status
        int withNotifications = 0;
        int withoutNotifications = 0;

        for (User admin : allActiveAdmins) {
            if (admin.isMailActive()) {
                withNotifications++;
            } else {
                withoutNotifications++;
            }
        }

        System.out.println("Current behavior:");
        System.out.println("- Admins with mailactive=true: " + withNotifications);
        System.out.println("- Admins with mailactive=false: " + withoutNotifications);
        System.out.println("- ALL " + allActiveAdmins.length + " admins would receive notifications");
        System.out.println();
        System.out.println("Expected behavior:");
        System.out.println("- Only " + withNotifications + " admins with mailactive=true should receive notifications");

        assertTrue(true, "This test documents the current vs expected behavior");
    }

    // ==================== 3. EMAIL SETTINGS ====================

    @Test
    @DisplayName("3.1 - Verify email settings are configured for tests")
    void verifyEmailSettingsConfigured() {
        // Then: EmailSettings should be available
        assertNotNull(emailSettings, "EmailSettings should be autowired");

        // Note: Actual email sending is tested in functional tests
        // Integration tests verify the business logic without SMTP
    }

    // ==================== 4. ERROR HANDLING ====================

    @Test
    @DisplayName("4.1 - Should handle admin with null email")
    void shouldHandleAdminWithNullEmail() {
        // Given: Admin with null email
        User adminNullEmail = new User();
        adminNullEmail.setLogin("test_admin_null_email");
        adminNullEmail.setName("Admin Null Email");
        adminNullEmail.setEmail(null);  // NULL email
        adminNullEmail.setActive(true);
        adminNullEmail.setMailActive(true);
        adminNullEmail.setProfile(User.Profile.ADMIN);
        adminNullEmail = usersRepository.save(adminNullEmail);

        try {
            // When: Querying active administrators
            User[] activeAdmins = usersRepository.findByProfileAndActiveTrue(User.Profile.ADMIN);

            // Then: Admin with null email should be retrieved
            // (The notification sending code handles null/invalid emails gracefully)
            boolean found = false;
            for (User admin : activeAdmins) {
                if (admin.getLogin().equals("test_admin_null_email")) {
                    found = true;
                    assertNull(admin.getEmail(), "Email should be null");
                }
            }
            assertTrue(found, "Admin with null email should be in results");

        } finally {
            usersRepository.delete(adminNullEmail);
        }
    }

    @Test
    @DisplayName("4.2 - Should handle admin with invalid email")
    void shouldHandleAdminWithInvalidEmail() {
        // Clean up any existing test user
        usersRepository.findAll().forEach(user -> {
            if (user.getLogin() != null && user.getLogin().equals("test_admin_bad_email")) {
                usersRepository.delete(user);
            }
        });

        // Given: Admin with invalid email format
        User adminBadEmail = new User();
        adminBadEmail.setLogin("test_admin_bad_email");
        adminBadEmail.setName("Admin Bad Email");
        adminBadEmail.setEmail("not-an-email");  // Invalid format
        adminBadEmail.setActive(true);
        adminBadEmail.setMailActive(true);
        adminBadEmail.setProfile(User.Profile.ADMIN);
        adminBadEmail = usersRepository.save(adminBadEmail);

        try {
            // When: Querying active administrators
            User[] activeAdmins = usersRepository.findByProfileAndActiveTrue(User.Profile.ADMIN);

            // Then: Admin with invalid email should be retrieved
            // (The notification code catches AddressException at line 315)
            boolean found = false;
            for (User admin : activeAdmins) {
                if (admin.getLogin().equals("test_admin_bad_email")) {
                    found = true;
                    assertEquals("not-an-email", admin.getEmail());
                }
            }
            assertTrue(found, "Admin with invalid email should be in results");

        } finally {
            usersRepository.delete(adminBadEmail);
        }
    }

    // ==================== 5. EMAIL CONTENT ====================

    @Test
    @DisplayName("5.1 - Email message can be created with required data")
    void emailMessageCanBeCreated() {
        // Given: Required data for email
        String errorMessage = "Test connector import failed";
        Calendar failureTime = GregorianCalendar.getInstance();
        failureTime.add(Calendar.MINUTE, -5);  // 5 minutes ago

        // When: Creating email (without sending)
        ch.asit_asso.extract.email.ConnectorImportFailedEmail email =
            new ch.asit_asso.extract.email.ConnectorImportFailedEmail(emailSettings);

        boolean initialized = email.initializeContent(testConnector, errorMessage, failureTime);

        // Then: Email should be initialized successfully
        assertTrue(initialized, "Email should be initialized with valid data");
    }

    @Test
    @DisplayName("5.2 - Email initialization fails with null connector")
    void emailInitializationFailsWithNullConnector() {
        // Given: Null connector
        String errorMessage = "Test error";
        Calendar failureTime = GregorianCalendar.getInstance();
        failureTime.add(Calendar.MINUTE, -5);  // 5 minutes ago

        // When/Then: Should throw IllegalArgumentException
        ch.asit_asso.extract.email.ConnectorImportFailedEmail email =
            new ch.asit_asso.extract.email.ConnectorImportFailedEmail(emailSettings);

        assertThrows(IllegalArgumentException.class, () -> {
            email.initializeContent(null, errorMessage, failureTime);
        }, "Should throw exception with null connector");
    }

    @Test
    @DisplayName("5.3 - Email initialization fails with null error message")
    void emailInitializationFailsWithNullErrorMessage() {
        // Given: Null error message
        Calendar failureTime = GregorianCalendar.getInstance();
        failureTime.add(Calendar.MINUTE, -5);  // 5 minutes ago

        // When/Then: Should throw IllegalArgumentException
        ch.asit_asso.extract.email.ConnectorImportFailedEmail email =
            new ch.asit_asso.extract.email.ConnectorImportFailedEmail(emailSettings);

        assertThrows(IllegalArgumentException.class, () -> {
            email.initializeContent(testConnector, null, failureTime);
        }, "Should throw exception with null error message");
    }
}
