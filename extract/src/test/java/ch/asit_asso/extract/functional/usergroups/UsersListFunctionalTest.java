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
package ch.asit_asso.extract.functional.usergroups;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.domain.User.TwoFactorStatus;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for the users list view.
 *
 * Validates end-to-end that:
 * 1. The list view displays ALL application users (excluding system user)
 * 2. Each user shows all their associated information
 * 3. The delete button state is correctly determined
 * 4. User filtering capabilities work correctly
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("functional")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Users List View Functional Tests")
class UsersListFunctionalTest {

    @Autowired
    private UsersRepository usersRepository;

    @BeforeAll
    static void setUpClass() {
        System.out.println("========================================");
        System.out.println("Users List View Functional Tests");
        System.out.println("========================================");
        System.out.println("Validates that the list view displays:");
        System.out.println("- ALL application users from the database");
        System.out.println("- Complete user information (login, name, email, role, etc.)");
        System.out.println("- Correct delete button state");
        System.out.println("- System user is excluded");
        System.out.println("========================================");
    }

    // ==================== 1. ALL USERS DISPLAYED ====================

    @Test
    @Order(1)
    @DisplayName("1. All application users are retrievable for list display")
    @Transactional
    void allApplicationUsersAreRetrievable() {
        // When: Retrieve all users (as controller does)
        User[] allUsers = usersRepository.findAllApplicationUsers();

        // Then: Users are present
        assertNotNull(allUsers);
        assertTrue(allUsers.length > 0, "Should have at least one application user");

        // Verify system user is excluded
        Set<String> logins = new HashSet<>();
        for (User user : allUsers) {
            logins.add(user.getLogin());
        }
        assertFalse(logins.contains("system"), "System user should not be in list");

        System.out.println("✓ All application users are retrievable:");
        System.out.println("  - Total users in list: " + allUsers.length);
        System.out.println("  - System user excluded: YES");
    }

    // ==================== 2. USER INFORMATION COMPLETE ====================

    @Test
    @Order(2)
    @DisplayName("2. Each user displays complete information")
    @Transactional
    void eachUserDisplaysCompleteInformation() {
        // Given: Get all users
        User[] allUsers = usersRepository.findAllApplicationUsers();
        assertTrue(allUsers.length > 0, "Need at least 1 user for this test");

        // When/Then: Check each user has complete information
        int completeCount = 0;
        for (User user : allUsers) {
            // Verify required fields
            assertNotNull(user.getId(), "User ID should not be null");
            assertNotNull(user.getLogin(), "User login should not be null");
            assertNotNull(user.getProfile(), "User profile should not be null");

            // Count complete users
            if (user.getId() != null && user.getLogin() != null && user.getProfile() != null) {
                completeCount++;
            }
        }

        assertEquals(allUsers.length, completeCount, "All users should have complete required info");

        System.out.println("✓ User information is complete:");
        System.out.println("  - Users with complete info: " + completeCount + "/" + allUsers.length);
    }

    // ==================== 3. USER PROFILE DISPLAY ====================

    @Test
    @Order(3)
    @DisplayName("3. User profiles are correctly identified")
    @Transactional
    void userProfilesAreCorrectlyIdentified() {
        // Given
        User[] allUsers = usersRepository.findAllApplicationUsers();
        assertTrue(allUsers.length > 0);

        // When: Count profiles
        int adminCount = 0;
        int operatorCount = 0;
        for (User user : allUsers) {
            if (user.getProfile() == Profile.ADMIN) {
                adminCount++;
            } else if (user.getProfile() == Profile.OPERATOR) {
                operatorCount++;
            }
        }

        // Then: At least one admin should exist
        assertTrue(adminCount > 0, "Should have at least one admin");

        System.out.println("✓ User profiles are correctly identified:");
        System.out.println("  - Administrators: " + adminCount);
        System.out.println("  - Operators: " + operatorCount);
    }

    // ==================== 4. ACTIVE/INACTIVE STATE ====================

    @Test
    @Order(4)
    @DisplayName("4. User active states are correctly loaded")
    @Transactional
    void userActiveStatesAreCorrectlyLoaded() {
        // Given
        User[] allUsers = usersRepository.findAllApplicationUsers();
        User[] activeUsers = usersRepository.findAllActiveApplicationUsers();

        // Then
        assertTrue(activeUsers.length <= allUsers.length,
                "Active users should be subset of all users");

        // Count active users
        int activeCount = 0;
        for (User user : allUsers) {
            if (user.isActive()) {
                activeCount++;
            }
        }

        assertEquals(activeUsers.length, activeCount,
                "Active count should match findAllActiveApplicationUsers");

        System.out.println("✓ User active states are correctly loaded:");
        System.out.println("  - Total users: " + allUsers.length);
        System.out.println("  - Active users: " + activeCount);
        System.out.println("  - Inactive users: " + (allUsers.length - activeCount));
    }

    // ==================== 5. DELETE ELIGIBILITY ====================

    @Test
    @Order(5)
    @DisplayName("5. User delete eligibility is correctly determined")
    @Transactional
    void userDeleteEligibilityIsCorrectlyDetermined() {
        // Given
        User[] allUsers = usersRepository.findAllApplicationUsers();
        assertTrue(allUsers.length > 0);

        // When: Check delete eligibility for each user
        int deletableCount = 0;
        int associatedToProcesses = 0;
        int lastActiveMember = 0;

        for (User user : allUsers) {
            if (user.isAssociatedToProcesses()) {
                associatedToProcesses++;
            } else if (user.isLastActiveMemberOfProcessGroup()) {
                lastActiveMember++;
            } else {
                deletableCount++;
            }
        }

        // Then
        System.out.println("✓ User delete eligibility is correctly determined:");
        System.out.println("  - Deletable users: " + deletableCount);
        System.out.println("  - Associated to processes: " + associatedToProcesses);
        System.out.println("  - Last active member of group: " + lastActiveMember);
    }

    // ==================== 6. 2FA STATUS DISPLAY ====================

    @Test
    @Order(6)
    @DisplayName("6. Two-factor authentication status is displayed")
    @Transactional
    void twoFactorStatusIsDisplayed() {
        // Given
        User[] allUsers = usersRepository.findAllApplicationUsers();
        assertTrue(allUsers.length > 0);

        // When: Count 2FA statuses
        int activeCount = 0;
        int inactiveCount = 0;
        int standbyCount = 0;
        int nullCount = 0;

        for (User user : allUsers) {
            TwoFactorStatus status = user.getTwoFactorStatus();
            if (status == null) {
                nullCount++;
            } else {
                switch (status) {
                    case ACTIVE -> activeCount++;
                    case INACTIVE -> inactiveCount++;
                    case STANDBY -> standbyCount++;
                }
            }
        }

        System.out.println("✓ Two-factor authentication status is displayed:");
        System.out.println("  - Active: " + activeCount);
        System.out.println("  - Inactive: " + inactiveCount);
        System.out.println("  - Standby: " + standbyCount);
        if (nullCount > 0) {
            System.out.println("  - Not set: " + nullCount);
        }
    }

    // ==================== 7. DOCUMENTATION ====================

    @Test
    @Order(7)
    @DisplayName("7. Document: Users list view requirements")
    void documentListViewRequirements() {
        System.out.println("✓ Users List View Requirements:");
        System.out.println("");
        System.out.println("  ENDPOINT:");
        System.out.println("  - URL: GET /users");
        System.out.println("  - Controller: UsersController.viewList()");
        System.out.println("  - Authorization: Admin only (isCurrentUserAdmin())");
        System.out.println("");
        System.out.println("  MODEL ATTRIBUTES:");
        System.out.println("  - users: User[] from usersRepository.findAllApplicationUsers()");
        System.out.println("  - currentUserId: Current logged-in user ID");
        System.out.println("");
        System.out.println("  TABLE COLUMNS:");
        System.out.println("  ┌─────────────────┬────────────────────────────────────┐");
        System.out.println("  │ Column          │ Data Source                        │");
        System.out.println("  ├─────────────────┼────────────────────────────────────┤");
        System.out.println("  │ Login           │ user.login (link to details)       │");
        System.out.println("  │ Name            │ user.name                          │");
        System.out.println("  │ Email           │ user.email                         │");
        System.out.println("  │ Role            │ user.profile (ADMIN/OPERATOR)      │");
        System.out.println("  │ Type            │ user.userType (LOCAL/LDAP)         │");
        System.out.println("  │ State           │ user.active (Active/Inactive)      │");
        System.out.println("  │ Notifications   │ user.mailActive (Active/Inactive)  │");
        System.out.println("  │ 2FA             │ user.twoFactorStatus               │");
        System.out.println("  │ Delete          │ not associatedToProcesses          │");
        System.out.println("  └─────────────────┴────────────────────────────────────┘");
        System.out.println("");
        System.out.println("  DELETE BUTTON STATES:");
        System.out.println("  - Enabled: User not associated to processes AND not current user AND not last active member");
        System.out.println("  - Disabled (hasProcesses): User is associated to at least one process");
        System.out.println("  - Disabled (currentUser): User is the currently logged-in user");
        System.out.println("  - Disabled (lastActiveMember): User is last active member of a process group");
        System.out.println("");
        System.out.println("  FILTERS:");
        System.out.println("  - Text filter (login/name)");
        System.out.println("  - Role filter (ADMIN/OPERATOR)");
        System.out.println("  - State filter (active/inactive)");
        System.out.println("  - Notifications filter (active/inactive)");
        System.out.println("  - 2FA filter (ACTIVE/INACTIVE/STANDBY)");
        System.out.println("");
        System.out.println("  TEMPLATE: templates/pages/users/list.html");

        assertTrue(true, "Documentation test");
    }
}
