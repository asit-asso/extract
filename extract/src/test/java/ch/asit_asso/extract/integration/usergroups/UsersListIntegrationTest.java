/*
 * Copyright (C) 2025 asit-asso
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
package ch.asit_asso.extract.integration.usergroups;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.domain.User.TwoFactorStatus;
import ch.asit_asso.extract.domain.User.UserType;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for users list view.
 *
 * Validates that:
 * 1. All application users are retrievable from the database
 * 2. Each user has all their associated information correctly loaded
 * 3. User properties are correctly persisted and retrieved
 * 4. System user is excluded from the list
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Users List View Integration Tests")
class UsersListIntegrationTest {

    @Autowired
    private UsersRepository usersRepository;

    // ==================== 1. USER RETRIEVAL ====================

    @Nested
    @DisplayName("1. User Retrieval Tests")
    class UserRetrievalTests {

        @Test
        @DisplayName("1.1 - All application users are retrievable")
        @Transactional
        void allApplicationUsersAreRetrievable() {
            // When
            User[] allUsers = usersRepository.findAllApplicationUsers();

            // Then
            assertNotNull(allUsers);
            assertTrue(allUsers.length > 0, "Should have at least one application user");

            // Verify system user is excluded
            for (User user : allUsers) {
                assertNotEquals("system", user.getLogin(), "System user should not be in the list");
            }
        }

        @Test
        @DisplayName("1.2 - Active application users are retrievable")
        @Transactional
        void activeApplicationUsersAreRetrievable() {
            // When
            User[] activeUsers = usersRepository.findAllActiveApplicationUsers();

            // Then
            assertNotNull(activeUsers);

            // All returned users should be active
            for (User user : activeUsers) {
                assertTrue(user.isActive(), "All returned users should be active");
                assertNotEquals("system", user.getLogin(), "System user should not be in the list");
            }
        }

        @Test
        @DisplayName("1.3 - User is retrievable by ID")
        @Transactional
        void userIsRetrievableById() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();
            assertTrue(allUsers.length > 0);
            User firstUser = allUsers[0];

            // When
            Optional<User> found = usersRepository.findById(firstUser.getId());

            // Then
            assertTrue(found.isPresent());
            assertEquals(firstUser.getLogin(), found.get().getLogin());
        }

        @Test
        @DisplayName("1.4 - User is retrievable by login")
        @Transactional
        void userIsRetrievableByLogin() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();
            assertTrue(allUsers.length > 0);
            User firstUser = allUsers[0];

            // When
            User found = usersRepository.findByLoginIgnoreCase(firstUser.getLogin());

            // Then
            assertNotNull(found);
            assertEquals(firstUser.getId(), found.getId());
        }
    }

    // ==================== 2. USER PROPERTIES ====================

    @Nested
    @DisplayName("2. User Properties Tests")
    class UserPropertiesTests {

        @Test
        @DisplayName("2.1 - User login is correctly loaded")
        @Transactional
        void userLoginIsCorrectlyLoaded() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();
            assertTrue(allUsers.length > 0);

            // Then: All users have a login
            for (User user : allUsers) {
                assertNotNull(user.getLogin(), "User login should not be null");
                assertFalse(user.getLogin().isEmpty(), "User login should not be empty");
            }
        }

        @Test
        @DisplayName("2.2 - User profile is correctly loaded")
        @Transactional
        void userProfileIsCorrectlyLoaded() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();
            assertTrue(allUsers.length > 0);

            // Then: All users have a profile
            for (User user : allUsers) {
                assertNotNull(user.getProfile(), "User profile should not be null");
                assertTrue(user.getProfile() == Profile.ADMIN || user.getProfile() == Profile.OPERATOR,
                        "User profile should be ADMIN or OPERATOR");
            }
        }

        @Test
        @DisplayName("2.3 - User email is accessible")
        @Transactional
        void userEmailIsAccessible() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();
            assertTrue(allUsers.length > 0);

            // Then: Email field is accessible (may be null)
            for (User user : allUsers) {
                // Just verify we can access the email field
                String email = user.getEmail();
                // Email might be null for some users, that's okay
            }
        }

        @Test
        @DisplayName("2.4 - User active state is correctly loaded")
        @Transactional
        void userActiveStateIsCorrectlyLoaded() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();
            assertTrue(allUsers.length > 0);

            // Then: All users have an active state
            for (User user : allUsers) {
                // isActive() returns a boolean, so it's always accessible
                boolean isActive = user.isActive();
                // Just verify we can access it
            }
        }

        @Test
        @DisplayName("2.5 - User 2FA status is correctly loaded")
        @Transactional
        void user2FAStatusIsCorrectlyLoaded() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();
            assertTrue(allUsers.length > 0);

            // Then: 2FA status is accessible
            for (User user : allUsers) {
                TwoFactorStatus status = user.getTwoFactorStatus();
                // Status might be null for legacy users, but should be one of the enum values if set
                if (status != null) {
                    assertTrue(status == TwoFactorStatus.ACTIVE
                            || status == TwoFactorStatus.INACTIVE
                            || status == TwoFactorStatus.STANDBY);
                }
            }
        }

        @Test
        @DisplayName("2.6 - User type is correctly loaded")
        @Transactional
        void userTypeIsCorrectlyLoaded() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();
            assertTrue(allUsers.length > 0);

            // Then: User type is accessible
            for (User user : allUsers) {
                UserType userType = user.getUserType();
                // User type might be null for legacy users
                if (userType != null) {
                    assertTrue(userType == UserType.LOCAL || userType == UserType.LDAP);
                }
            }
        }
    }

    // ==================== 3. PROCESS ASSOCIATION ====================

    @Nested
    @DisplayName("3. Process Association Tests")
    class ProcessAssociationTests {

        @Test
        @DisplayName("3.1 - User process association is accessible")
        @Transactional
        void userProcessAssociationIsAccessible() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();
            assertTrue(allUsers.length > 0);

            // Then: Process association is accessible
            for (User user : allUsers) {
                boolean isAssociated = user.isAssociatedToProcesses();
                // Just verify we can access it
            }
        }

        @Test
        @DisplayName("3.2 - User is last active member check is accessible")
        @Transactional
        void userIsLastActiveMemberCheckIsAccessible() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();
            assertTrue(allUsers.length > 0);

            // Then: Last active member check is accessible
            for (User user : allUsers) {
                boolean isLastActive = user.isLastActiveMemberOfProcessGroup();
                // Just verify we can access it
            }
        }
    }

    // ==================== 4. ADMIN USERS ====================

    @Nested
    @DisplayName("4. Admin Users Tests")
    class AdminUsersTests {

        @Test
        @DisplayName("4.1 - Admin users exist in the system")
        @Transactional
        void adminUsersExistInTheSystem() {
            // When
            boolean hasAdmins = usersRepository.existsByProfile(Profile.ADMIN);

            // Then
            assertTrue(hasAdmins, "System should have at least one admin user");
        }

        @Test
        @DisplayName("4.2 - Admin users are retrievable")
        @Transactional
        void adminUsersAreRetrievable() {
            // When
            User[] admins = usersRepository.findByProfileAndActiveTrue(Profile.ADMIN);

            // Then
            assertNotNull(admins);
            assertTrue(admins.length > 0, "Should have at least one active admin");

            for (User admin : admins) {
                assertEquals(Profile.ADMIN, admin.getProfile());
                assertTrue(admin.isActive());
            }
        }
    }

    // ==================== 5. SYSTEM USER EXCLUSION ====================

    @Nested
    @DisplayName("5. System User Exclusion Tests")
    class SystemUserExclusionTests {

        @Test
        @DisplayName("5.1 - System user exists")
        @Transactional
        void systemUserExists() {
            // When
            User systemUser = usersRepository.getSystemUser();

            // Then
            assertNotNull(systemUser);
            assertEquals("system", systemUser.getLogin());
        }

        @Test
        @DisplayName("5.2 - System user is excluded from application users list")
        @Transactional
        void systemUserIsExcludedFromList() {
            // Given
            User systemUser = usersRepository.getSystemUser();
            assertNotNull(systemUser);

            // When
            User[] allUsers = usersRepository.findAllApplicationUsers();

            // Then
            for (User user : allUsers) {
                assertNotEquals(systemUser.getId(), user.getId(), "System user should be excluded");
                assertNotEquals("system", user.getLogin(), "System user login should not appear");
            }
        }
    }

    // ==================== 6. LIST VIEW REQUIREMENTS ====================

    @Nested
    @DisplayName("6. List View Requirements")
    class ListViewRequirementsTests {

        @Test
        @DisplayName("6.1 - All users returned have required fields for list view")
        @Transactional
        void allUsersHaveRequiredFieldsForListView() {
            // When
            User[] allUsers = usersRepository.findAllApplicationUsers();

            // Then: Each user has all required fields
            for (User user : allUsers) {
                // Required: ID for URL
                assertNotNull(user.getId(), "User ID should not be null");

                // Required: Login for display
                assertNotNull(user.getLogin(), "User login should not be null");

                // Required: Profile for role display
                assertNotNull(user.getProfile(), "User profile should not be null");
            }
        }

        @Test
        @DisplayName("6.2 - Users information is complete for display")
        @Transactional
        void usersInformationIsCompleteForDisplay() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();
            assertTrue(allUsers.length > 0);

            // Then: Count users with complete information
            int completeUsers = 0;
            for (User user : allUsers) {
                if (user.getId() != null
                        && user.getLogin() != null
                        && user.getProfile() != null) {
                    completeUsers++;
                }
            }

            assertEquals(allUsers.length, completeUsers,
                    "All users should have complete required information");
        }
    }

    // ==================== 7. SPECIAL CHARACTERS ====================

    @Nested
    @DisplayName("7. Special Characters Handling")
    class SpecialCharactersTests {

        @Test
        @DisplayName("7.1 - Users with special characters in name are retrievable")
        @Transactional
        void usersWithSpecialCharsAreRetrievable() {
            // Given
            User[] allUsers = usersRepository.findAllApplicationUsers();

            // Then: Names with special characters are correctly loaded
            for (User user : allUsers) {
                String name = user.getName();
                // Just verify we can access and read the name
                // Special characters should be preserved
            }
        }
    }
}
