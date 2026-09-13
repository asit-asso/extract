/*
 * Copyright (C) 2026 asit-asso
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.integration.processes;

import java.util.ArrayList;
import javax.persistence.EntityManager;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.integration.DatabaseTestHelper;
import ch.asit_asso.extract.integration.WithMockApplicationUser;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the description stored with a process (issue #374).
 *
 * @author Bruno Alves
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Process Description Integration Tests (issue #374)")
class ProcessDescriptionIntegrationTest {

    private static final String DESCRIPTION = "Description saved through the process form.";

    private static final String MARKER = "ZZ374";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessesRepository processesRepository;

    @Autowired
    private DatabaseTestHelper dbHelper;

    @Autowired
    private EntityManager entityManager;

    private int operatorId;



    @BeforeEach
    void setUp() {
        this.operatorId = this.dbHelper.createTestOperator(ProcessDescriptionIntegrationTest.MARKER + "_operator",
                                                           ProcessDescriptionIntegrationTest.MARKER + " Operator",
                                                           ProcessDescriptionIntegrationTest.MARKER + "@test.ch", true);
    }



    @Test
    @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
    @Transactional
    @DisplayName("A description submitted through the process controller is persisted")
    void descriptionSubmittedThroughControllerIsPersisted() throws Exception {
        final Process process = this.process();

        this.mockMvc.perform(this.saveProcess(process)).andExpect(status().is3xxRedirection());

        this.entityManager.flush();
        this.entityManager.clear();

        final Process reloadedProcess = this.processesRepository.findById(process.getId()).orElseThrow();
        assertEquals(ProcessDescriptionIntegrationTest.DESCRIPTION, reloadedProcess.getDescription());
    }



    private Process process() {
        final Process process = new Process();
        process.setName(ProcessDescriptionIntegrationTest.MARKER + " process");
        process.setTasksCollection(new ArrayList<>());
        process.setUsersCollection(new ArrayList<>());
        process.setUserGroupsCollection(new ArrayList<>());

        return this.processesRepository.save(process);
    }



    private MockHttpServletRequestBuilder saveProcess(final Process process) {
        return post("/processes/{id}", process.getId()).with(csrf())
                .param("id", String.valueOf(process.getId()))
                .param("name", process.getName())
                .param("description", ProcessDescriptionIntegrationTest.DESCRIPTION)
                .param("readOnly", "false")
                .param("htmlScrollY", "0")
                .param("usersIds", String.valueOf(this.operatorId))
                .param("userGroupsIds", "")
                .param("tasks[0].id", "1")
                .param("tasks[0].pluginCode", "ARCHIVE")
                .param("tasks[0].pluginLabel", "Archivage fichiers")
                .param("tasks[0].pluginPictoClass", "fa-folder")
                .param("tasks[0].tag", "ADDED")
                .param("tasks[0].position", "1")
                .param("tasks[0].parameters[0].name", "path")
                .param("tasks[0].parameters[0].type", "text")
                .param("tasks[0].parameters[0].label", "Chemin")
                .param("tasks[0].parameters[0].required", "true")
                .param("tasks[0].parameters[0].maxLength", "255")
                .param("tasks[0].parameters[0].value", "/tmp/" + ProcessDescriptionIntegrationTest.MARKER);
    }
}
