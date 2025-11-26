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

import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
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
 * Integration tests for connectors list filtering functionality (Issue #344).
 * Tests that the connectors list page contains all necessary filtering elements including
 * text filter and type dropdown.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@Transactional
public class ConnectorsListFilteringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConnectorsRepository connectorsRepository;

    private Connector testConnector1;
    private Connector testConnector2;

    @BeforeEach
    public void setUp() {
        // Create test connectors with different names for filtering
        testConnector1 = new Connector();
        testConnector1.setName("easySDI v4 Connector");
        testConnector1.setConnectorCode("easysdi_v4");
        testConnector1 = connectorsRepository.save(testConnector1);

        testConnector2 = new Connector();
        testConnector2.setName("FTP Data Connector");
        testConnector2.setConnectorCode("ftp");
        testConnector2 = connectorsRepository.save(testConnector2);
    }

    @AfterEach
    public void tearDown() {
        // Clean up test data
        if (testConnector1 != null && testConnector1.getId() > 0) {
            connectorsRepository.deleteById(testConnector1.getId());
        }
        if (testConnector2 != null && testConnector2.getId() > 0) {
            connectorsRepository.deleteById(testConnector2.getId());
        }
    }

    @Test
    @DisplayName("Connectors page contains text filter input")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testConnectorsPageContainsTextFilterInput() throws Exception {
        mockMvc.perform(get("/connectors"))
            .andExpect(status().isOk())
            .andExpect(view().name("connectors/list"))
            // Verify text filter input exists with correct attributes
            .andExpect(content().string(containsString("id=\"textFilter\"")))
            .andExpect(content().string(containsString("class=\"form-control connector-filter-control\"")))
            .andExpect(content().string(containsString("type=\"text\"")));
    }

    @Test
    @DisplayName("Connectors page contains type dropdown filter")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testConnectorsPageContainsTypeDropdownFilter() throws Exception {
        mockMvc.perform(get("/connectors"))
            .andExpect(status().isOk())
            // Verify type dropdown exists
            .andExpect(content().string(containsString("id=\"typeFilter\"")))
            .andExpect(content().string(containsString("class=\"select2 form-control connector-filter-control\"")))
            // Verify it's a select element
            .andExpect(content().string(containsString("<select")));
    }

    @Test
    @DisplayName("Connectors page contains DataTables configuration")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testConnectorsPageContainsDataTablesConfiguration() throws Exception {
        mockMvc.perform(get("/connectors"))
            .andExpect(status().isOk())
            // Verify table has DataTables classes
            .andExpect(content().string(containsString("class=\"table table-striped table-hover dataTables dataTable\"")))
            // Verify DataTables JavaScript configuration
            .andExpect(content().string(containsString("dataTablesProperties.searching = true")))
            .andExpect(content().string(containsString("$('.dataTables').dataTable(dataTablesProperties)")));
    }

    @Test
    @DisplayName("Connectors page loads connector data in model")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testConnectorsPageLoadsConnectorDataInModel() throws Exception {
        mockMvc.perform(get("/connectors"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("connectors"))
            // Verify test connectors are in the response
            .andExpect(content().string(containsString("easySDI v4 Connector")))
            .andExpect(content().string(containsString("FTP Data Connector")));
    }

    @Test
    @DisplayName("Connectors page contains filter button")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testConnectorsPageContainsFilterButton() throws Exception {
        mockMvc.perform(get("/connectors"))
            .andExpect(status().isOk())
            // Verify filter button exists
            .andExpect(content().string(containsString("id=\"filterButton\"")))
            .andExpect(content().string(containsString("class=\"btn btn-extract-filled\"")))
            .andExpect(content().string(containsString("fa-search")));
    }

    @Test
    @DisplayName("Connectors page table structure is correct")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testConnectorsPageTableStructureIsCorrect() throws Exception {
        mockMvc.perform(get("/connectors"))
            .andExpect(status().isOk())
            // Verify table structure
            .andExpect(content().string(containsString("<thead>")))
            .andExpect(content().string(containsString("<tbody>")))
            // Verify table columns exist (name, type, state, delete)
            .andExpect(content().string(containsString("class=\"nameCell\"")));
    }

    @Test
    @DisplayName("Connectors page includes JavaScript for list management")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testConnectorsPageIncludesJavaScriptForListManagement() throws Exception {
        mockMvc.perform(get("/connectors"))
            .andExpect(status().isOk())
            // Verify JavaScript file is loaded
            .andExpect(content().string(containsString("/js/connectorsList.js")))
            // Verify DataTables initialization
            .andExpect(content().string(containsString("getDataTableBaseProperties()")));
    }

    @Test
    @DisplayName("Connectors page requires admin authentication")
    public void testConnectorsPageRequiresAdminAuthentication() throws Exception {
        // Without authentication, should redirect or return 401/403
        mockMvc.perform(get("/connectors"))
            .andExpect(status().is(anyOf(is(302), is(401), is(403))));
    }

    @Test
    @DisplayName("Connectors page filter form has correct structure")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testConnectorsPageFilterFormHasCorrectStructure() throws Exception {
        mockMvc.perform(get("/connectors"))
            .andExpect(status().isOk())
            // Verify form exists
            .andExpect(content().string(containsString("class=\"filter-form-inline float-end\"")))
            .andExpect(content().string(containsString("role=\"form\"")))
            // Verify filter label exists
            .andExpect(content().string(containsString("<label for=\"textFilter\"")))
            // Verify both filter controls exist
            .andExpect(content().string(containsString("connector-filter-control")));
    }

    @Test
    @DisplayName("Connectors page type filter has placeholder attribute")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testConnectorsPageTypeFilterHasPlaceholderAttribute() throws Exception {
        mockMvc.perform(get("/connectors"))
            .andExpect(status().isOk())
            // Verify type filter has data-placeholder attribute for Select2
            .andExpect(content().string(containsString("data-placeholder")));
    }
}
