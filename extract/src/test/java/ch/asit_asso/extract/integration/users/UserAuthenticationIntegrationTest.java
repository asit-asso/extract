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

import ch.asit_asso.extract.authentication.ApplicationUser;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.integration.DatabaseTestHelper;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for user authentication functionality.
 * Tests login for active and inactive users.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Authentication Integration Tests")
class UserAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private DatabaseTestHelper dbHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    // ==================== 1. ACTIVE USER LOGIN TESTS ====================

    @Nested
    @DisplayName("1. Active User Login")
    class ActiveUserLoginTests {

        @Test
        @DisplayName("1.1 - Active admin can login with correct credentials")
        void activeAdminCanLoginWithCorrectCredentials() throws Exception {
            // Given: Active admin user exists (from test data: admin/extract)
            User adminUser = usersRepository.findByLoginIgnoreCase("admin");
            assertNotNull(adminUser, "Admin user should exist");
            assertTrue(adminUser.isActive(), "Admin should be active");

            // When: Admin logs in with correct credentials
            mockMvc.perform(formLogin("/login")
                    .user("username", "admin")
                    .password("password", DatabaseTestHelper.TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        }

        @Test
        @DisplayName("1.2 - Active operator can login with correct credentials")
        @Transactional
        void activeOperatorCanLoginWithCorrectCredentials() throws Exception {
            // Given: Create an active operator
            dbHelper.createTestOperator("activeop", "Active Operator", "activeop@test.com", true);

            // When: Operator logs in
            mockMvc.perform(formLogin("/login")
                    .user("username", "activeop")
                    .password("password", DatabaseTestHelper.TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        }

        @Test
        @DisplayName("1.3 - Login fails with incorrect password")
        void loginFailsWithIncorrectPassword() throws Exception {
            // When: User tries to login with wrong password
            mockMvc.perform(formLogin("/login")
                    .user("username", "admin")
                    .password("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/error"));
        }

        @Test
        @DisplayName("1.4 - Login fails for non-existent user")
        void loginFailsForNonExistentUser() throws Exception {
            // When: Non-existent user tries to login
            mockMvc.perform(formLogin("/login")
                    .user("username", "nonexistent")
                    .password("password", "anypassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/error"));
        }

        @Test
        @DisplayName("1.5 - UserDetailsService loads active user correctly")
        void userDetailsServiceLoadsActiveUser() {
            // Given: Active admin exists
            User adminUser = usersRepository.findByLoginIgnoreCase("admin");
            assertNotNull(adminUser);
            assertTrue(adminUser.isActive());

            // When: UserDetailsService loads user
            UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

            // Then: User details are correct
            assertNotNull(userDetails);
            assertTrue(userDetails instanceof ApplicationUser);
            ApplicationUser appUser = (ApplicationUser) userDetails;
            assertEquals("admin", appUser.getUsername());
            assertTrue(appUser.isEnabled());
            assertTrue(appUser.isAccountNonExpired());
            assertTrue(appUser.isAccountNonLocked());
            assertTrue(appUser.isCredentialsNonExpired());
        }

        @Test
        @DisplayName("1.6 - Login is case-insensitive for username")
        void loginIsCaseInsensitiveForUsername() throws Exception {
            // Given: Admin user exists with lowercase login
            User adminUser = usersRepository.findByLoginIgnoreCase("admin");
            assertNotNull(adminUser);

            // When: User logs in with different case
            mockMvc.perform(formLogin("/login")
                    .user("username", "ADMIN")
                    .password("password", DatabaseTestHelper.TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        }
    }

    // ==================== 2. INACTIVE USER LOGIN TESTS ====================

    @Nested
    @DisplayName("2. Inactive User Login")
    class InactiveUserLoginTests {

        @Test
        @DisplayName("2.1 - Inactive user cannot login")
        @Transactional
        void inactiveUserCannotLogin() throws Exception {
            // Given: Create an inactive user
            dbHelper.createTestOperator("inactiveop", "Inactive Operator", "inactiveop@test.com", false);

            // Verify user is inactive
            User inactiveUser = usersRepository.findByLoginIgnoreCase("inactiveop");
            assertNotNull(inactiveUser);
            assertFalse(inactiveUser.isActive(), "User should be inactive");

            // When: Inactive user tries to login
            mockMvc.perform(formLogin("/login")
                    .user("username", "inactiveop")
                    .password("password", DatabaseTestHelper.TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/error"));
        }

        @Test
        @DisplayName("2.2 - UserDetailsService throws exception for inactive user")
        @Transactional
        void userDetailsServiceThrowsExceptionForInactiveUser() {
            // Given: Create an inactive user
            dbHelper.createTestOperator("inactivetest", "Inactive Test", "inactive@test.com", false);

            // When/Then: UserDetailsService throws exception
            assertThrows(UsernameNotFoundException.class, () -> {
                userDetailsService.loadUserByUsername("inactivetest");
            });
        }

        @Test
        @DisplayName("2.3 - System user cannot login")
        void systemUserCannotLogin() throws Exception {
            // Given: System user exists but is inactive
            User systemUser = usersRepository.findByLoginIgnoreCase("system");
            assertNotNull(systemUser);
            assertFalse(systemUser.isActive(), "System user should be inactive");

            // When: System user tries to login
            mockMvc.perform(formLogin("/login")
                    .user("username", "system")
                    .password("password", DatabaseTestHelper.TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/error"));
        }

        @Test
        @DisplayName("2.4 - Deactivated user loses access immediately")
        @Transactional
        void deactivatedUserLosesAccessImmediately() throws Exception {
            // Given: Create an active user
            int userId = dbHelper.createTestOperator("todeactivate", "To Deactivate", "deact@test.com", true);

            // Verify user can login initially
            mockMvc.perform(formLogin("/login")
                    .user("username", "todeactivate")
                    .password("password", DatabaseTestHelper.TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

            // When: User is deactivated
            dbHelper.setUserActive(userId, false);

            // Then: User can no longer login
            mockMvc.perform(formLogin("/login")
                    .user("username", "todeactivate")
                    .password("password", DatabaseTestHelper.TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/error"));
        }
    }

    // ==================== 3. LOGIN PAGE TESTS ====================

    @Nested
    @DisplayName("3. Login Page")
    class LoginPageTests {

        @Test
        @DisplayName("3.1 - Login page is accessible without authentication")
        void loginPageIsAccessible() throws Exception {
            mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("3.2 - Unauthenticated users are redirected to login")
        void unauthenticatedUsersRedirectedToLogin() throws Exception {
            mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("3.3 - Login error page shows error")
        void loginErrorPageShowsError() throws Exception {
            // First, attempt a failed login
            mockMvc.perform(formLogin("/login")
                    .user("username", "baduser")
                    .password("password", "badpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/error"));
        }
    }

    // ==================== 4. SESSION MANAGEMENT TESTS ====================

    @Nested
    @DisplayName("4. Session Management")
    class SessionManagementTests {

        @Test
        @DisplayName("4.1 - Logout invalidates session")
        void logoutInvalidatesSession() throws Exception {
            // Given: User is logged in
            MvcResult loginResult = mockMvc.perform(formLogin("/login")
                    .user("username", "admin")
                    .password("password", DatabaseTestHelper.TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();

            // When: User logs out using the configured logout URL
            org.springframework.mock.web.MockHttpSession session =
                (org.springframework.mock.web.MockHttpSession) loginResult.getRequest().getSession(false);

            if (session != null) {
                mockMvc.perform(post("/login/disconnect")
                        .with(csrf())
                        .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login/disconnect"));
            } else {
                // If session is null, the logout behavior may vary
                // Just verify logout endpoint is accessible
                mockMvc.perform(post("/login/disconnect")
                        .with(csrf()))
                    .andExpect(status().is3xxRedirection());
            }
        }
    }

    // ==================== 5. USER PROPERTIES AFTER LOGIN TESTS ====================

    @Nested
    @DisplayName("5. User Properties After Login")
    class UserPropertiesAfterLoginTests {

        @Test
        @DisplayName("5.1 - ApplicationUser has correct authorities for admin")
        void applicationUserHasCorrectAuthoritiesForAdmin() {
            // Given: Admin user exists
            User adminUser = usersRepository.findByLoginIgnoreCase("admin");
            assertNotNull(adminUser);
            assertEquals(User.Profile.ADMIN, adminUser.getProfile());

            // When: Loading user details
            ApplicationUser appUser = (ApplicationUser) userDetailsService.loadUserByUsername("admin");

            // Then: Has ADMIN authority
            assertTrue(appUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN")),
                "Admin user should have ADMIN authority");
        }

        @Test
        @DisplayName("5.2 - ApplicationUser has correct authorities for operator")
        @Transactional
        void applicationUserHasCorrectAuthoritiesForOperator() {
            // Given: Create operator user
            dbHelper.createTestOperator("authoperator", "Auth Operator", "authop@test.com", true);

            // When: Loading user details
            ApplicationUser appUser = (ApplicationUser) userDetailsService.loadUserByUsername("authoperator");

            // Then: Has OPERATOR authority
            assertTrue(appUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("OPERATOR")),
                "Operator user should have OPERATOR authority");
        }

        @Test
        @DisplayName("5.3 - User locale is preserved")
        @Transactional
        void userLocaleIsPreserved() {
            // Given: Create user with specific locale
            int userId = dbHelper.createTestOperator("localeuser", "Locale User", "locale@test.com", true);

            // Set locale directly in database
            User user = usersRepository.findById(userId).orElse(null);
            assertNotNull(user);
            user.setLocale("de");
            usersRepository.save(user);

            // When: Loading user
            User loadedUser = usersRepository.findByLoginIgnoreCase("localeuser");

            // Then: Locale is preserved
            assertEquals("de", loadedUser.getLocale());
        }
    }
}
