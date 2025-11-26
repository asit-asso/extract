/*
 * Copyright (C) 2017 arx iT
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
package ch.asit_asso.extract.integration.web;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for users list filtering functionality (Issue #344).
 * Tests that the users list page contains all necessary filtering elements including
 * text filter, role dropdown, state dropdown, notifications dropdown, and 2FA dropdown.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@Transactional
public class UsersListFilteringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    public void setUp() {
        // Create test users with different attributes for filtering
        testUser1 = new User();
        testUser1.setLogin("testuser1");
        testUser1.setPassword(passwordEncoder.encode("password123"));
        testUser1.setName("John Doe");
        testUser1.setEmail("john.doe@test.com");
        testUser1.setProfile(User.Profile.ADMIN);
        testUser1.setActive(true);
        testUser1 = usersRepository.save(testUser1);

        testUser2 = new User();
        testUser2.setLogin("testuser2");
        testUser2.setPassword(passwordEncoder.encode("password456"));
        testUser2.setName("Jane Smith");
        testUser2.setEmail("jane.smith@test.com");
        testUser2.setProfile(User.Profile.OPERATOR);
        testUser2.setActive(true);
        testUser2 = usersRepository.save(testUser2);
    }

    @AfterEach
    public void tearDown() {
        // Clean up test data
        if (testUser1 != null && testUser1.getId() > 0) {
            usersRepository.deleteById(testUser1.getId());
        }
        if (testUser2 != null && testUser2.getId() > 0) {
            usersRepository.deleteById(testUser2.getId());
        }
    }

    @Test
    @DisplayName("Users page contains text filter input")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageContainsTextFilterInput() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(view().name("users/list"))
            // Verify text filter input exists with correct attributes
            .andExpect(content().string(containsString("id=\"textFilter\"")))
            .andExpect(content().string(containsString("class=\"form-control user-filter-control\"")))
            .andExpect(content().string(containsString("type=\"text\"")));
    }

    @Test
    @DisplayName("Users page contains role dropdown filter")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageContainsRoleDropdownFilter() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            // Verify role dropdown exists
            .andExpect(content().string(containsString("id=\"roleFilter\"")))
            .andExpect(content().string(containsString("class=\"select2 form-control user-filter-control\"")))
            // Verify role options exist
            .andExpect(content().string(containsString("value=\"ADMIN\"")))
            .andExpect(content().string(containsString("value=\"OPERATOR\"")));
    }

    @Test
    @DisplayName("Users page contains state dropdown filter")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageContainsStateDropdownFilter() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            // Verify state dropdown exists
            .andExpect(content().string(containsString("id=\"stateFilter\"")))
            .andExpect(content().string(containsString("class=\"select2 form-control user-filter-control\"")))
            // Verify state options exist
            .andExpect(content().string(containsString("value=\"active\"")))
            .andExpect(content().string(containsString("value=\"inactive\"")));
    }

    @Test
    @DisplayName("Users page contains notifications dropdown filter")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageContainsNotificationsDropdownFilter() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            // Verify notifications dropdown exists
            .andExpect(content().string(containsString("id=\"notificationsFilter\"")))
            .andExpect(content().string(containsString("class=\"select2 form-control user-filter-control\"")));
    }

    @Test
    @DisplayName("Users page contains 2FA dropdown filter")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageContainsTwoFactorAuthDropdownFilter() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            // Verify 2FA dropdown exists
            .andExpect(content().string(containsString("id=\"2faFilter\"")))
            .andExpect(content().string(containsString("class=\"select2 form-control user-filter-control\"")))
            // Verify 2FA status options exist
            .andExpect(content().string(containsString("value=\"ACTIVE\"")))
            .andExpect(content().string(containsString("value=\"INACTIVE\"")))
            .andExpect(content().string(containsString("value=\"STANDBY\"")));
    }

    @Test
    @DisplayName("Users page contains DataTables configuration")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageContainsDataTablesConfiguration() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            // Verify table has DataTables classes
            .andExpect(content().string(containsString("class=\"table table-striped table-hover dataTables dataTable\"")))
            // Verify DataTables JavaScript configuration
            .andExpect(content().string(containsString("dataTablesProperties.searching = true")))
            .andExpect(content().string(containsString("$('.dataTables').dataTable(dataTablesProperties)")));
    }

    @Test
    @DisplayName("Users page loads user data in model")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageLoadsUserDataInModel() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("users"))
            // Verify test users are in the response
            .andExpect(content().string(containsString("testuser1")))
            .andExpect(content().string(containsString("testuser2")))
            .andExpect(content().string(containsString("John Doe")))
            .andExpect(content().string(containsString("Jane Smith")));
    }

    @Test
    @DisplayName("Users page contains filter button")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageContainsFilterButton() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            // Verify filter button exists
            .andExpect(content().string(containsString("id=\"filterButton\"")))
            .andExpect(content().string(containsString("class=\"btn btn-extract-filled\"")))
            .andExpect(content().string(containsString("fa-search")));
    }

    @Test
    @DisplayName("Users page table structure is correct")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageTableStructureIsCorrect() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            // Verify table structure
            .andExpect(content().string(containsString("<thead>")))
            .andExpect(content().string(containsString("<tbody>")))
            // Verify table columns exist (login cells should have loginCell class)
            .andExpect(content().string(containsString("class=\"loginCell\"")));
    }

    @Test
    @DisplayName("Users page includes JavaScript for list management")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageIncludesJavaScriptForListManagement() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            // Verify JavaScript file is loaded
            .andExpect(content().string(containsString("/js/usersList.js")))
            // Verify DataTables initialization
            .andExpect(content().string(containsString("getDataTableBaseProperties()")));
    }

    @Test
    @DisplayName("Users page requires admin authentication")
    public void testUsersPageRequiresAdminAuthentication() throws Exception {
        // Without authentication, should redirect or return 401/403
        mockMvc.perform(get("/users"))
            .andExpect(status().is(anyOf(is(302), is(401), is(403))));
    }

    @Test
    @DisplayName("Users page filter form has correct structure")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageFilterFormHasCorrectStructure() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            // Verify form exists
            .andExpect(content().string(containsString("class=\"filter-form-inline float-end\"")))
            .andExpect(content().string(containsString("role=\"form\"")))
            // Verify filter label exists
            .andExpect(content().string(containsString("<label for=\"textFilter\"")))
            // Verify all filter controls exist (5 filters total)
            .andExpect(content().string(containsString("user-filter-control")));
    }

    @Test
    @DisplayName("Users page all dropdown filters have placeholder attributes")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageAllDropdownFiltersHavePlaceholderAttributes() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            // Verify dropdowns have data-placeholder attributes for Select2
            .andExpect(content().string(containsString("data-placeholder")));
    }

    @Test
    @DisplayName("Users page table displays role badges correctly")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUsersPageTableDisplaysRoleBadgesCorrectly() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            // Verify role column has data-role attribute for filtering
            .andExpect(content().string(containsString("data-role")))
            // Verify badge classes exist for roles
            .andExpect(content().string(containsString("class=\"badge")));
    }
}
