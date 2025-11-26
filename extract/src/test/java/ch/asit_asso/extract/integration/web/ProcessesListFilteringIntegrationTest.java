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

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.SystemParameter;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for processes list filtering functionality (Issue #344).
 * Tests that the processes list page contains all necessary filtering elements.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
public class ProcessesListFilteringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessesRepository processesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private SystemParametersRepository systemParametersRepository;

    private Process testProcess1;
    private Process testProcess2;
    private Process testProcess3;

    @BeforeEach
    public void setUp() {
        // Initialize minimal system parameters to avoid redirect to /setup
        createSystemParameterIfNotExists("base_path", "/tmp/extract");
        createSystemParameterIfNotExists("mails_enable", "false");
        createSystemParameterIfNotExists("freq_scheduler_sec", "60");
        createSystemParameterIfNotExists("op_mode", "AUTO");

        // Create admin user to satisfy AppInitializationService.isConfigured()
        createAdminUserIfNotExists();

        // Create test processes with different names for filtering
        testProcess1 = new Process();
        testProcess1.setName("Extraction MO");
        testProcess1 = processesRepository.save(testProcess1);

        testProcess2 = new Process();
        testProcess2.setName("Extraction DXF");
        testProcess2 = processesRepository.save(testProcess2);

        testProcess3 = new Process();
        testProcess3.setName("Validation Geometry");
        testProcess3 = processesRepository.save(testProcess3);
    }

    @AfterEach
    public void tearDown() {
        // Clean up test data
        if (testProcess1 != null && testProcess1.getId() > 0) {
            processesRepository.deleteById(testProcess1.getId());
        }
        if (testProcess2 != null && testProcess2.getId() > 0) {
            processesRepository.deleteById(testProcess2.getId());
        }
        if (testProcess3 != null && testProcess3.getId() > 0) {
            processesRepository.deleteById(testProcess3.getId());
        }
    }

    @Test
    @DisplayName("Processes page contains text filter input")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testProcessesPageContainsTextFilterInput() throws Exception {
        mockMvc.perform(get("/processes"))
            .andExpect(status().isOk())
            .andExpect(view().name("processes/list"))
            // Verify text filter input exists with correct attributes
            .andExpect(content().string(containsString("id=\"textFilter\"")))
            .andExpect(content().string(containsString("class=\"form-control process-filter-control\"")))
            .andExpect(content().string(containsString("type=\"text\"")));
    }

    @Test
    @DisplayName("Processes page contains DataTables configuration")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testProcessesPageContainsDataTablesConfiguration() throws Exception {
        mockMvc.perform(get("/processes"))
            .andExpect(status().isOk())
            // Verify table has DataTables classes
            .andExpect(content().string(containsString("class=\"table table-striped table-hover dataTables dataTable\"")))
            // Verify DataTables JavaScript configuration
            .andExpect(content().string(containsString("dataTablesProperties.searching = true")))
            .andExpect(content().string(containsString("$('.dataTables').dataTable(dataTablesProperties)")));
    }

    @Test
    @DisplayName("Processes page loads process data in model")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testProcessesPageLoadsProcessDataInModel() throws Exception {
        mockMvc.perform(get("/processes"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("processes"))
            // Verify test processes are in the response
            .andExpect(content().string(containsString("Extraction MO")))
            .andExpect(content().string(containsString("Extraction DXF")))
            .andExpect(content().string(containsString("Validation Geometry")));
    }

    @Test
    @DisplayName("Processes page contains filter button")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testProcessesPageContainsFilterButton() throws Exception {
        mockMvc.perform(get("/processes"))
            .andExpect(status().isOk())
            // Verify filter button exists
            .andExpect(content().string(containsString("id=\"filterButton\"")))
            .andExpect(content().string(containsString("class=\"btn btn-extract-filled\"")))
            .andExpect(content().string(containsString("fa-search")));
    }

    @Test
    @DisplayName("Processes page table structure is correct")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testProcessesPageTableStructureIsCorrect() throws Exception {
        mockMvc.perform(get("/processes"))
            .andExpect(status().isOk())
            // Verify table structure
            .andExpect(content().string(containsString("<thead>")))
            .andExpect(content().string(containsString("<tbody>")))
            // Verify table columns exist (name cells should have nameCell class)
            .andExpect(content().string(containsString("class=\"nameCell\"")));
    }

    @Test
    @DisplayName("Processes page includes JavaScript for list management")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testProcessesPageIncludesJavaScriptForListManagement() throws Exception {
        mockMvc.perform(get("/processes"))
            .andExpect(status().isOk())
            // Verify JavaScript file is loaded
            .andExpect(content().string(containsString("/js/processesList.js")))
            // Verify DataTables initialization
            .andExpect(content().string(containsString("getDataTableBaseProperties()")));
    }

    @Test
    @DisplayName("Processes page requires admin authentication")
    public void testProcessesPageRequiresAdminAuthentication() throws Exception {
        // Without authentication, should redirect or return 401/403
        mockMvc.perform(get("/processes"))
            .andExpect(status().is(anyOf(is(302), is(401), is(403))));
    }

    @Test
    @DisplayName("Processes page filter form has correct structure")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testProcessesPageFilterFormHasCorrectStructure() throws Exception {
        mockMvc.perform(get("/processes"))
            .andExpect(status().isOk())
            // Verify form exists
            .andExpect(content().string(containsString("class=\"filter-form-inline float-end\"")))
            .andExpect(content().string(containsString("role=\"form\"")))
            // Verify filter label exists
            .andExpect(content().string(containsString("<label for=\"textFilter\"")));
    }

    /**
     * Helper method to create a system parameter if it doesn't exist.
     * This prevents redirect to /setup page during tests.
     */
    private void createSystemParameterIfNotExists(String key, String value) {
        if (systemParametersRepository.findById(key).isEmpty()) {
            SystemParameter param = new SystemParameter();
            param.setKey(key);
            param.setValue(value);
            systemParametersRepository.save(param);
        }
    }

    private void createAdminUserIfNotExists() {
        if (usersRepository.findByLoginIgnoreCase("admin") == null) {
            User admin = new User();
            admin.setLogin("admin");
            admin.setName("Test Admin");
            admin.setEmail("admin@test.local");
            admin.setProfile(User.Profile.ADMIN);
            admin.setActive(true);
            usersRepository.save(admin);
        }
    }
}
