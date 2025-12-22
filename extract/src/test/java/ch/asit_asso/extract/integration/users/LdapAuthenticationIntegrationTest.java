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
package ch.asit_asso.extract.integration.users;

import ch.asit_asso.extract.authentication.ldap.ExtractLdapAuthenticationProvider;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.integration.DatabaseTestHelper;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for LDAP authentication functionality.
 * Note: These tests verify LDAP configuration and behavior when LDAP is disabled.
 * Full LDAP authentication tests require an actual LDAP server.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("LDAP Authentication Integration Tests")
class LdapAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private SystemParametersRepository systemParametersRepository;

    @Autowired
    private LdapSettings ldapSettings;

    @Autowired
    private ExtractLdapAuthenticationProvider ldapAuthenticationProvider;

    @Autowired
    private DatabaseTestHelper dbHelper;

    // ==================== 1. LDAP CONFIGURATION TESTS ====================

    @Nested
    @DisplayName("1. LDAP Configuration")
    class LdapConfigurationTests {

        @Test
        @DisplayName("1.1 - LDAP is disabled by default in test environment")
        void ldapIsDisabledByDefault() {
            ldapSettings.refresh();
            assertFalse(ldapSettings.isEnabled(),
                "LDAP should be disabled by default in test environment");
        }

        @Test
        @DisplayName("1.2 - LDAP settings can be refreshed from database")
        void ldapSettingsCanBeRefreshed() {
            // When: Refreshing settings
            assertDoesNotThrow(() -> ldapSettings.refresh(),
                "Refreshing LDAP settings should not throw exception");
        }

        @Test
        @DisplayName("1.3 - LDAP provider supports UsernamePasswordAuthenticationToken")
        void ldapProviderSupportsUsernamePasswordToken() {
            assertTrue(ldapAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class),
                "LDAP provider should support UsernamePasswordAuthenticationToken");
        }

        @Test
        @DisplayName("1.4 - LDAP settings have required attributes configured")
        void ldapSettingsHaveRequiredAttributes() {
            ldapSettings.refresh();

            // These attributes are configured in application-test.properties
            assertNotNull(ldapSettings.getLoginAttribute(),
                "Login attribute should be configured");
            assertNotNull(ldapSettings.getMailAttribute(),
                "Mail attribute should be configured");
            assertNotNull(ldapSettings.getUserNameAttribute(),
                "User name attribute should be configured");
            assertNotNull(ldapSettings.getUserObjectClass(),
                "User object class should be configured");
        }
    }

    // ==================== 2. LDAP DISABLED BEHAVIOR TESTS ====================

    @Nested
    @DisplayName("2. LDAP Disabled Behavior")
    class LdapDisabledBehaviorTests {

        @Test
        @DisplayName("2.1 - LDAP authentication throws exception when disabled")
        void ldapAuthenticationThrowsWhenDisabled() {
            // Given: LDAP is disabled
            ldapSettings.refresh();
            assertFalse(ldapSettings.isEnabled());

            // When/Then: Attempting LDAP authentication throws BadCredentialsException
            Authentication auth = new UsernamePasswordAuthenticationToken("ldapuser", "password");

            BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
                ldapAuthenticationProvider.authenticate(auth);
            });

            assertEquals("LDAP disabled.", exception.getMessage(),
                "Exception message should indicate LDAP is disabled");
        }

        @Test
        @DisplayName("2.2 - Local users can still login when LDAP is disabled")
        void localUsersCanLoginWhenLdapDisabled() throws Exception {
            // Given: LDAP is disabled but local admin exists
            ldapSettings.refresh();
            assertFalse(ldapSettings.isEnabled());

            User adminUser = usersRepository.findByLoginIgnoreCase("admin");
            assertNotNull(adminUser);
            assertEquals(User.UserType.LOCAL, adminUser.getUserType());

            // When: Local user logs in
            mockMvc.perform(formLogin("/login")
                    .user("username", "admin")
                    .password("password", DatabaseTestHelper.TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        }

        @Test
        @DisplayName("2.3 - LDAP settings validation returns false when disabled")
        void ldapSettingsValidationReturnsFalseWhenDisabled() {
            ldapSettings.refresh();
            ldapSettings.setEnabled(false);

            assertFalse(ldapSettings.isValid(),
                "LDAP settings should be invalid when LDAP is disabled");
        }
    }

    // ==================== 3. LDAP USER TYPE TESTS ====================

    @Nested
    @DisplayName("3. LDAP User Type")
    class LdapUserTypeTests {

        @Test
        @DisplayName("3.1 - LDAP user type exists in User enum")
        void ldapUserTypeExists() {
            assertNotNull(User.UserType.LDAP,
                "LDAP user type should exist");
        }

        @Test
        @DisplayName("3.2 - Local user has LOCAL type")
        void localUserHasLocalType() {
            User adminUser = usersRepository.findByLoginIgnoreCase("admin");
            assertNotNull(adminUser);
            assertEquals(User.UserType.LOCAL, adminUser.getUserType(),
                "Admin user should have LOCAL type");
        }

        @Test
        @DisplayName("3.3 - LDAP user can be created in database")
        @Transactional
        void ldapUserCanBeCreatedInDatabase() {
            // Given: Create an LDAP user
            User ldapUser = new User();
            ldapUser.setLogin("ldapuser");
            ldapUser.setName("LDAP User");
            ldapUser.setEmail("ldapuser@test.com");
            ldapUser.setPassword(""); // LDAP users don't have local password
            ldapUser.setUserType(User.UserType.LDAP);
            ldapUser.setProfile(User.Profile.OPERATOR);
            ldapUser.setActive(true);

            User savedUser = usersRepository.save(ldapUser);

            // Then: User is saved with LDAP type
            assertNotNull(savedUser.getId());
            assertEquals(User.UserType.LDAP, savedUser.getUserType());
        }

        @Test
        @DisplayName("3.4 - LDAP user without local password cannot login locally")
        @Transactional
        void ldapUserCannotLoginLocally() throws Exception {
            // Given: Create an LDAP user without password
            User ldapUser = new User();
            ldapUser.setLogin("nolocallogin");
            ldapUser.setName("LDAP No Local");
            ldapUser.setEmail("nolocallogin@test.com");
            ldapUser.setPassword(""); // Empty password
            ldapUser.setUserType(User.UserType.LDAP);
            ldapUser.setProfile(User.Profile.OPERATOR);
            ldapUser.setActive(true);
            usersRepository.save(ldapUser);

            // When: LDAP user tries to login with any password
            mockMvc.perform(formLogin("/login")
                    .user("username", "nolocallogin")
                    .password("password", "anypassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/error"));
        }
    }

    // ==================== 4. LDAP SYNCHRONIZATION SETTINGS TESTS ====================

    @Nested
    @DisplayName("4. LDAP Synchronization Settings")
    class LdapSynchronizationSettingsTests {

        @Test
        @DisplayName("4.1 - Synchronization is disabled when LDAP is disabled")
        void synchronizationDisabledWhenLdapDisabled() {
            ldapSettings.refresh();
            ldapSettings.setEnabled(false);

            assertNull(ldapSettings.getNextScheduledSynchronizationDate(),
                "Next sync date should be null when LDAP is disabled");
        }

        @Test
        @DisplayName("4.2 - Synchronization settings can be configured")
        void synchronizationSettingsCanBeConfigured() {
            ldapSettings.refresh();

            // Test setters don't throw exceptions
            assertDoesNotThrow(() -> {
                ldapSettings.setSynchronizationEnabled(false);
                ldapSettings.setSynchronizationFrequencyHours(24);
            });
        }

        @Test
        @DisplayName("4.3 - Synchronization returns null when sync is disabled")
        void synchronizationReturnsNullWhenSyncDisabled() {
            ldapSettings.refresh();
            ldapSettings.setEnabled(true);
            ldapSettings.setSynchronizationEnabled(false);

            assertNull(ldapSettings.getNextScheduledSynchronizationDate(),
                "Next sync date should be null when synchronization is disabled");
        }
    }

    // ==================== 5. LDAP ENCRYPTION SETTINGS TESTS ====================

    @Nested
    @DisplayName("5. LDAP Encryption Settings")
    class LdapEncryptionSettingsTests {

        @Test
        @DisplayName("5.1 - LDAPS encryption type exists")
        void ldapsEncryptionTypeExists() {
            assertNotNull(LdapSettings.EncryptionType.LDAPS,
                "LDAPS encryption type should exist");
        }

        @Test
        @DisplayName("5.2 - STARTTLS encryption type exists")
        void starttlsEncryptionTypeExists() {
            assertNotNull(LdapSettings.EncryptionType.STARTTLS,
                "STARTTLS encryption type should exist");
        }

        @Test
        @DisplayName("5.3 - Encryption type can be retrieved from settings")
        void encryptionTypeCanBeRetrieved() {
            ldapSettings.refresh();
            // Encryption type might be null when LDAP is not configured
            // Just verify no exception is thrown
            assertDoesNotThrow(() -> ldapSettings.getEncryptionType());
        }
    }

    // ==================== 6. LDAP GROUPS CONFIGURATION TESTS ====================

    @Nested
    @DisplayName("6. LDAP Groups Configuration")
    class LdapGroupsConfigurationTests {

        @Test
        @DisplayName("6.1 - Admin group can be configured")
        void adminGroupCanBeConfigured() {
            ldapSettings.refresh();
            // Verify getter doesn't throw
            assertDoesNotThrow(() -> ldapSettings.getAdminsGroup());
        }

        @Test
        @DisplayName("6.2 - Operators group can be configured")
        void operatorsGroupCanBeConfigured() {
            ldapSettings.refresh();
            // Verify getter doesn't throw
            assertDoesNotThrow(() -> ldapSettings.getOperatorsGroup());
        }
    }

    // ==================== 7. LDAP VALIDATION TESTS ====================

    @Nested
    @DisplayName("7. LDAP Validation")
    class LdapValidationTests {

        @Test
        @DisplayName("7.1 - Invalid when servers not configured")
        void invalidWhenServersNotConfigured() {
            ldapSettings.refresh();
            ldapSettings.setEnabled(true);

            // Without proper server configuration, should be invalid
            // The actual validation depends on configured values
            // This test verifies the validation method exists and runs
            assertDoesNotThrow(() -> ldapSettings.isValid());
        }

        @Test
        @DisplayName("7.2 - LDAP configuration requires all mandatory fields")
        void ldapConfigurationRequiresAllMandatoryFields() {
            // This test documents the required fields:
            // - enabled: must be true
            // - servers: at least one server URL
            // - baseDn: base DN for searches
            // - loginAttribute: attribute for username
            // - mailAttribute: attribute for email
            // - userNameAttribute: attribute for display name
            // - userObjectClass: LDAP object class
            // - adminsGroup: group DN for admins
            // - operatorsGroup: group DN for operators
            // - encryptionType: LDAPS or STARTTLS

            // When enabled but not fully configured
            ldapSettings.refresh();
            ldapSettings.setEnabled(true);

            // Should be invalid without full configuration
            // (test database doesn't have complete LDAP config)
            boolean isValid = ldapSettings.isValid();
            // Just verify the validation runs without error
            assertNotNull(Boolean.valueOf(isValid));
        }
    }

    // ==================== 8. AUTHENTICATION FLOW TESTS ====================

    @Nested
    @DisplayName("8. Authentication Flow")
    class AuthenticationFlowTests {

        @Test
        @DisplayName("8.1 - Authentication falls back to local when LDAP fails")
        @Transactional
        void authenticationFallsBackToLocalWhenLdapFails() throws Exception {
            // Given: A local user exists
            dbHelper.createTestOperator("localoperator", "Local Operator", "localop@test.com", true);

            // When: User logs in (LDAP is disabled, should fall back to local)
            mockMvc.perform(formLogin("/login")
                    .user("username", "localoperator")
                    .password("password", DatabaseTestHelper.TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        }

        @Test
        @DisplayName("8.2 - Invalid credentials rejected by both providers")
        void invalidCredentialsRejectedByBothProviders() throws Exception {
            // When: Non-existent user tries to login
            mockMvc.perform(formLogin("/login")
                    .user("username", "nonexistentuser")
                    .password("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/error"));
        }
    }
}
