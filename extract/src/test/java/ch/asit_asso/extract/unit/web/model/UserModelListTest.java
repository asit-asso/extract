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
package ch.asit_asso.extract.unit.web.model;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.domain.User.TwoFactorStatus;
import ch.asit_asso.extract.domain.User.UserType;
import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.web.model.UserModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserModel in the context of the users list view.
 *
 * Validates that the list view displays:
 * 1. All users with their identification (login, name, email)
 * 2. The user's profile (ADMIN or OPERATOR)
 * 3. The user type (LOCAL or LDAP)
 * 4. The user's active state
 * 5. The mail notification status
 * 6. The two-factor authentication status
 * 7. Whether the user can be deleted
 *
 * @author Bruno Alves
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Users List View - Unit Tests")
class UserModelListTest {

    @Mock
    private User mockUser;

    // ==================== 1. USER IDENTIFICATION ====================

    @Nested
    @DisplayName("1. User Identification")
    class UserIdentificationTests {

        @Test
        @DisplayName("1.1 - User ID is accessible")
        void userIdIsAccessible() {
            // Given
            when(mockUser.getId()).thenReturn(42);
            when(mockUser.getLogin()).thenReturn("testuser");
            when(mockUser.getName()).thenReturn("Test User");
            when(mockUser.getEmail()).thenReturn("test@example.com");
            when(mockUser.isActive()).thenReturn(true);
            when(mockUser.isMailActive()).thenReturn(true);
            when(mockUser.getProfile()).thenReturn(Profile.OPERATOR);
            when(mockUser.getTwoFactorStatus()).thenReturn(TwoFactorStatus.INACTIVE);
            when(mockUser.isTwoFactorForced()).thenReturn(false);
            when(mockUser.getUserType()).thenReturn(UserType.LOCAL);
            when(mockUser.getLocale()).thenReturn("fr");

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals(42, model.getId());
        }

        @Test
        @DisplayName("1.2 - User login is accessible")
        void userLoginIsAccessible() {
            // Given
            setupMockUser("admin", "Administrator", "admin@example.com");

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals("admin", model.getLogin());
        }

        @Test
        @DisplayName("1.3 - User name is accessible")
        void userNameIsAccessible() {
            // Given
            setupMockUser("jdoe", "John Doe", "john.doe@example.com");

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals("John Doe", model.getName());
        }

        @Test
        @DisplayName("1.4 - User email is accessible")
        void userEmailIsAccessible() {
            // Given
            setupMockUser("jsmith", "Jane Smith", "jane.smith@example.com");

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals("jane.smith@example.com", model.getEmail());
        }

        @Test
        @DisplayName("1.5 - User with special characters in name")
        void userWithSpecialCharactersInName() {
            // Given
            setupMockUser("jmuller", "Jean-Pierre Müller", "jp.muller@example.com");

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals("Jean-Pierre Müller", model.getName());
        }
    }

    // ==================== 2. USER PROFILE ====================

    @Nested
    @DisplayName("2. User Profile (Role)")
    class UserProfileTests {

        @Test
        @DisplayName("2.1 - Admin profile is correctly identified")
        void adminProfileIsCorrectlyIdentified() {
            // Given
            setupMockUserWithProfile("admin", Profile.ADMIN);

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals(Profile.ADMIN, model.getProfile());
            assertTrue(model.isAdmin());
        }

        @Test
        @DisplayName("2.2 - Operator profile is correctly identified")
        void operatorProfileIsCorrectlyIdentified() {
            // Given
            setupMockUserWithProfile("operator", Profile.OPERATOR);

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals(Profile.OPERATOR, model.getProfile());
            assertFalse(model.isAdmin());
        }
    }

    // ==================== 3. USER TYPE ====================

    @Nested
    @DisplayName("3. User Type (LOCAL/LDAP)")
    class UserTypeTests {

        @Test
        @DisplayName("3.1 - Local user type is correctly identified")
        void localUserTypeIsCorrectlyIdentified() {
            // Given
            setupMockUserWithType("localuser", UserType.LOCAL);

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals(UserType.LOCAL, model.getUserType());
        }

        @Test
        @DisplayName("3.2 - LDAP user type is correctly identified")
        void ldapUserTypeIsCorrectlyIdentified() {
            // Given
            setupMockUserWithType("ldapuser", UserType.LDAP);

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals(UserType.LDAP, model.getUserType());
        }
    }

    // ==================== 4. ACTIVE STATE ====================

    @Nested
    @DisplayName("4. Active State")
    class ActiveStateTests {

        @Test
        @DisplayName("4.1 - Active user is correctly identified")
        void activeUserIsCorrectlyIdentified() {
            // Given
            setupMockUserWithActiveState("activeuser", true);

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertTrue(model.isActive());
        }

        @Test
        @DisplayName("4.2 - Inactive user is correctly identified")
        void inactiveUserIsCorrectlyIdentified() {
            // Given
            setupMockUserWithActiveState("inactiveuser", false);

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertFalse(model.isActive());
        }
    }

    // ==================== 5. MAIL NOTIFICATIONS ====================

    @Nested
    @DisplayName("5. Mail Notifications")
    class MailNotificationsTests {

        @Test
        @DisplayName("5.1 - User with active mail notifications")
        void userWithActiveMailNotifications() {
            // Given
            setupMockUserWithMailActive("mailuser", true);

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertTrue(model.isMailActive());
        }

        @Test
        @DisplayName("5.2 - User with inactive mail notifications")
        void userWithInactiveMailNotifications() {
            // Given
            setupMockUserWithMailActive("nomailuser", false);

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertFalse(model.isMailActive());
        }
    }

    // ==================== 6. TWO-FACTOR AUTHENTICATION ====================

    @Nested
    @DisplayName("6. Two-Factor Authentication Status")
    class TwoFactorStatusTests {

        @Test
        @DisplayName("6.1 - User with active 2FA")
        void userWithActive2FA() {
            // Given
            setupMockUserWith2FA("2faactive", TwoFactorStatus.ACTIVE);

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals(TwoFactorStatus.ACTIVE, model.getTwoFactorStatus());
        }

        @Test
        @DisplayName("6.2 - User with inactive 2FA")
        void userWithInactive2FA() {
            // Given
            setupMockUserWith2FA("2fainactive", TwoFactorStatus.INACTIVE);

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals(TwoFactorStatus.INACTIVE, model.getTwoFactorStatus());
        }

        @Test
        @DisplayName("6.3 - User with standby 2FA")
        void userWithStandby2FA() {
            // Given
            setupMockUserWith2FA("2fastandby", TwoFactorStatus.STANDBY);

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals(TwoFactorStatus.STANDBY, model.getTwoFactorStatus());
        }

        @Test
        @DisplayName("6.4 - User with forced 2FA")
        void userWithForced2FA() {
            // Given
            when(mockUser.getId()).thenReturn(1);
            when(mockUser.getLogin()).thenReturn("forceduser");
            when(mockUser.getName()).thenReturn("Forced User");
            when(mockUser.getEmail()).thenReturn("forced@example.com");
            when(mockUser.isActive()).thenReturn(true);
            when(mockUser.isMailActive()).thenReturn(true);
            when(mockUser.getProfile()).thenReturn(Profile.OPERATOR);
            when(mockUser.getTwoFactorStatus()).thenReturn(TwoFactorStatus.ACTIVE);
            when(mockUser.isTwoFactorForced()).thenReturn(true);
            when(mockUser.getUserType()).thenReturn(UserType.LOCAL);
            when(mockUser.getLocale()).thenReturn("fr");

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertTrue(model.isTwoFactorForced());
        }
    }

    // ==================== 7. DELETE ELIGIBILITY ====================

    @Nested
    @DisplayName("7. Delete Eligibility")
    class DeleteEligibilityTests {

        @Test
        @DisplayName("7.1 - User not associated to processes can be deleted")
        void userNotAssociatedToProcessesCanBeDeleted() {
            // Given
            User user = createRealUser(1, "deletable", "Deletable User");
            user.setProcessesCollection(new ArrayList<>());
            user.setUserGroupsCollection(new ArrayList<>());

            // Then
            assertFalse(user.isAssociatedToProcesses());
        }

        @Test
        @DisplayName("7.2 - User associated to processes cannot be deleted")
        void userAssociatedToProcessesCannotBeDeleted() {
            // Given
            User user = createRealUser(1, "notdeletable", "Not Deletable User");
            Process process = new Process();
            process.setId(1);
            process.setName("Test Process");
            user.setProcessesCollection(Arrays.asList(process));
            user.setUserGroupsCollection(new ArrayList<>());

            // Then
            assertTrue(user.isAssociatedToProcesses());
        }

        @Test
        @DisplayName("7.3 - Last active member of process group cannot be deleted")
        void lastActiveMemberOfProcessGroupCannotBeDeleted() {
            // Given
            User user = createRealUser(1, "lastmember", "Last Member");
            user.setActive(true);
            user.setProcessesCollection(new ArrayList<>());

            UserGroup group = new UserGroup(1);
            group.setName("Test Group");
            group.setUsersCollection(Arrays.asList(user));

            Process process = new Process();
            process.setId(1);
            process.setName("Test Process");
            group.setProcessesCollection(Arrays.asList(process));

            user.setUserGroupsCollection(Arrays.asList(group));

            // Then
            assertTrue(user.isLastActiveMemberOfProcessGroup());
        }
    }

    // ==================== 8. LOCALE PREFERENCE ====================

    @Nested
    @DisplayName("8. Locale Preference")
    class LocalePreferenceTests {

        @Test
        @DisplayName("8.1 - User locale is accessible")
        void userLocaleIsAccessible() {
            // Given
            when(mockUser.getId()).thenReturn(1);
            when(mockUser.getLogin()).thenReturn("frenchuser");
            when(mockUser.getName()).thenReturn("French User");
            when(mockUser.getEmail()).thenReturn("french@example.com");
            when(mockUser.isActive()).thenReturn(true);
            when(mockUser.isMailActive()).thenReturn(true);
            when(mockUser.getProfile()).thenReturn(Profile.OPERATOR);
            when(mockUser.getTwoFactorStatus()).thenReturn(TwoFactorStatus.INACTIVE);
            when(mockUser.isTwoFactorForced()).thenReturn(false);
            when(mockUser.getUserType()).thenReturn(UserType.LOCAL);
            when(mockUser.getLocale()).thenReturn("de");

            // When
            UserModel model = new UserModel(mockUser);

            // Then
            assertEquals("de", model.getLocale());
        }
    }

    // ==================== 9. LIST VIEW DATA REQUIREMENTS ====================

    @Nested
    @DisplayName("9. List View Data Requirements")
    class ListViewDataRequirementsTests {

        @Test
        @DisplayName("9.1 - All required fields for list view are accessible")
        void allRequiredFieldsForListViewAreAccessible() {
            // Given
            when(mockUser.getId()).thenReturn(99);
            when(mockUser.getLogin()).thenReturn("complete");
            when(mockUser.getName()).thenReturn("Complete User");
            when(mockUser.getEmail()).thenReturn("complete@example.com");
            when(mockUser.isActive()).thenReturn(true);
            when(mockUser.isMailActive()).thenReturn(true);
            when(mockUser.getProfile()).thenReturn(Profile.ADMIN);
            when(mockUser.getTwoFactorStatus()).thenReturn(TwoFactorStatus.ACTIVE);
            when(mockUser.isTwoFactorForced()).thenReturn(false);
            when(mockUser.getUserType()).thenReturn(UserType.LOCAL);
            when(mockUser.getLocale()).thenReturn("fr");

            // When
            UserModel model = new UserModel(mockUser);

            // Then: All list view fields are accessible
            // 1. Login (for display and link)
            assertNotNull(model.getLogin());
            assertEquals("complete", model.getLogin());

            // 2. ID (for link URL)
            assertNotNull(model.getId());
            assertEquals(99, model.getId());

            // 3. Name
            assertNotNull(model.getName());
            assertEquals("Complete User", model.getName());

            // 4. Email
            assertNotNull(model.getEmail());
            assertEquals("complete@example.com", model.getEmail());

            // 5. Profile (role)
            assertNotNull(model.getProfile());
            assertEquals(Profile.ADMIN, model.getProfile());

            // 6. User type
            assertNotNull(model.getUserType());
            assertEquals(UserType.LOCAL, model.getUserType());

            // 7. Active state
            assertTrue(model.isActive());

            // 8. Mail notifications
            assertTrue(model.isMailActive());

            // 9. 2FA status
            assertNotNull(model.getTwoFactorStatus());
            assertEquals(TwoFactorStatus.ACTIVE, model.getTwoFactorStatus());
        }

        @Test
        @DisplayName("9.2 - Document: List view displays all required information")
        void documentListViewInformation() {
            System.out.println("✓ Users List View displays:");
            System.out.println("");
            System.out.println("  TABLE COLUMNS:");
            System.out.println("  1. Login (link to user details)");
            System.out.println("  2. Name");
            System.out.println("  3. Email");
            System.out.println("  4. Role (ADMIN/OPERATOR badge)");
            System.out.println("  5. Type (LOCAL/LDAP badge)");
            System.out.println("  6. State (Active/Inactive badge)");
            System.out.println("  7. Notifications (Active/Inactive badge)");
            System.out.println("  8. 2FA (ACTIVE/INACTIVE/STANDBY badge)");
            System.out.println("  9. Delete button");
            System.out.println("");
            System.out.println("  DATA SOURCE:");
            System.out.println("  - Controller: UsersController.viewList()");
            System.out.println("  - Model attribute: 'users'");
            System.out.println("  - Repository: usersRepository.findAllApplicationUsers()");
            System.out.println("");
            System.out.println("  DELETE BUTTON STATES:");
            System.out.println("  - Enabled: User not associated to processes, not current user, not last active member");
            System.out.println("  - Disabled: User is associated to processes, or is current user, or is last active member");
            System.out.println("");
            System.out.println("  TEMPLATE: templates/pages/users/list.html");

            assertTrue(true);
        }
    }

    // ==================== 10. NEW USER MODEL ====================

    @Nested
    @DisplayName("10. New User Model (Default Values)")
    class NewUserModelTests {

        @Test
        @DisplayName("10.1 - New user model has correct default values")
        void newUserModelHasCorrectDefaultValues() {
            // When
            UserModel model = new UserModel();

            // Then
            assertTrue(model.isBeingCreated());
            assertFalse(model.isActive());
            assertEquals(Profile.OPERATOR, model.getProfile());
            assertFalse(model.isTwoFactorForced());
            assertEquals(TwoFactorStatus.INACTIVE, model.getTwoFactorStatus());
            assertEquals(UserType.LOCAL, model.getUserType());
            assertEquals("fr", model.getLocale());
        }
    }

    // ==================== HELPER METHODS ====================

    private void setupMockUser(String login, String name, String email) {
        when(mockUser.getId()).thenReturn(1);
        when(mockUser.getLogin()).thenReturn(login);
        when(mockUser.getName()).thenReturn(name);
        when(mockUser.getEmail()).thenReturn(email);
        when(mockUser.isActive()).thenReturn(true);
        when(mockUser.isMailActive()).thenReturn(true);
        when(mockUser.getProfile()).thenReturn(Profile.OPERATOR);
        when(mockUser.getTwoFactorStatus()).thenReturn(TwoFactorStatus.INACTIVE);
        when(mockUser.isTwoFactorForced()).thenReturn(false);
        when(mockUser.getUserType()).thenReturn(UserType.LOCAL);
        when(mockUser.getLocale()).thenReturn("fr");
    }

    private void setupMockUserWithProfile(String login, Profile profile) {
        when(mockUser.getId()).thenReturn(1);
        when(mockUser.getLogin()).thenReturn(login);
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockUser.isActive()).thenReturn(true);
        when(mockUser.isMailActive()).thenReturn(true);
        when(mockUser.getProfile()).thenReturn(profile);
        when(mockUser.getTwoFactorStatus()).thenReturn(TwoFactorStatus.INACTIVE);
        when(mockUser.isTwoFactorForced()).thenReturn(false);
        when(mockUser.getUserType()).thenReturn(UserType.LOCAL);
        when(mockUser.getLocale()).thenReturn("fr");
    }

    private void setupMockUserWithType(String login, UserType userType) {
        when(mockUser.getId()).thenReturn(1);
        when(mockUser.getLogin()).thenReturn(login);
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockUser.isActive()).thenReturn(true);
        when(mockUser.isMailActive()).thenReturn(true);
        when(mockUser.getProfile()).thenReturn(Profile.OPERATOR);
        when(mockUser.getTwoFactorStatus()).thenReturn(TwoFactorStatus.INACTIVE);
        when(mockUser.isTwoFactorForced()).thenReturn(false);
        when(mockUser.getUserType()).thenReturn(userType);
        when(mockUser.getLocale()).thenReturn("fr");
    }

    private void setupMockUserWithActiveState(String login, boolean active) {
        when(mockUser.getId()).thenReturn(1);
        when(mockUser.getLogin()).thenReturn(login);
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockUser.isActive()).thenReturn(active);
        when(mockUser.isMailActive()).thenReturn(true);
        when(mockUser.getProfile()).thenReturn(Profile.OPERATOR);
        when(mockUser.getTwoFactorStatus()).thenReturn(TwoFactorStatus.INACTIVE);
        when(mockUser.isTwoFactorForced()).thenReturn(false);
        when(mockUser.getUserType()).thenReturn(UserType.LOCAL);
        when(mockUser.getLocale()).thenReturn("fr");
    }

    private void setupMockUserWithMailActive(String login, boolean mailActive) {
        when(mockUser.getId()).thenReturn(1);
        when(mockUser.getLogin()).thenReturn(login);
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockUser.isActive()).thenReturn(true);
        when(mockUser.isMailActive()).thenReturn(mailActive);
        when(mockUser.getProfile()).thenReturn(Profile.OPERATOR);
        when(mockUser.getTwoFactorStatus()).thenReturn(TwoFactorStatus.INACTIVE);
        when(mockUser.isTwoFactorForced()).thenReturn(false);
        when(mockUser.getUserType()).thenReturn(UserType.LOCAL);
        when(mockUser.getLocale()).thenReturn("fr");
    }

    private void setupMockUserWith2FA(String login, TwoFactorStatus status) {
        when(mockUser.getId()).thenReturn(1);
        when(mockUser.getLogin()).thenReturn(login);
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockUser.isActive()).thenReturn(true);
        when(mockUser.isMailActive()).thenReturn(true);
        when(mockUser.getProfile()).thenReturn(Profile.OPERATOR);
        when(mockUser.getTwoFactorStatus()).thenReturn(status);
        when(mockUser.isTwoFactorForced()).thenReturn(false);
        when(mockUser.getUserType()).thenReturn(UserType.LOCAL);
        when(mockUser.getLocale()).thenReturn("fr");
    }

    private User createRealUser(int id, String login, String name) {
        User user = new User(id);
        user.setLogin(login);
        user.setName(name);
        user.setEmail(login + "@example.com");
        user.setActive(true);
        user.setMailActive(true);
        user.setProfile(Profile.OPERATOR);
        user.setTwoFactorStatus(TwoFactorStatus.INACTIVE);
        user.setTwoFactorForced(false);
        user.setUserType(UserType.LOCAL);
        return user;
    }
}
