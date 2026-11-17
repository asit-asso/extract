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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.integration.processes;

import java.util.ArrayList;
import java.util.HashMap;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.integration.DatabaseTestHelper;
import ch.asit_asso.extract.integration.WithMockApplicationUser;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.TasksRepository;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorDiscovererWrapper;
import ch.asit_asso.extract.web.model.ProcessModel;
import ch.asit_asso.extract.web.model.TaskModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



/**
 * Checks that a task added to an existing process can be removed again before the process is saved, even when the
 * browser reports a fractional scroll position (as Chrome does at the bottom of the page).
 *
 * @author Bruno Alves
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Unsaved task removal integration tests")
class UnsavedTaskRemovalIntegrationTest {

    /**
     * Prefixes the seeded values, so that they cannot collide with the test data set.
     */
    private static final String MARKER = "ZZUNSAVED";

    /**
     * The code of the plugin that the tasks use.
     */
    private static final String PLUGIN_CODE = "ARCHIVE";

    /**
     * The scroll position that the browser submits when the page is scrolled to its bottom with sub-pixel layout.
     */
    private static final String FRACTIONAL_SCROLL_POSITION = "3583.75";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessesRepository processesRepository;

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private DatabaseTestHelper dbHelper;

    @MockBean
    private TaskProcessorDiscovererWrapper taskPluginsDiscoverer;

    private int operatorId;



    @BeforeEach
    public void setUp() {
        this.operatorId = this.dbHelper.createTestOperator(UnsavedTaskRemovalIntegrationTest.MARKER + "_operator",
                                                           UnsavedTaskRemovalIntegrationTest.MARKER + " Operator",
                                                           UnsavedTaskRemovalIntegrationTest.MARKER + "@test.ch",
                                                           true);
        ITaskProcessor plugin = mock(ITaskProcessor.class);
        when(plugin.getCode()).thenReturn(UnsavedTaskRemovalIntegrationTest.PLUGIN_CODE);
        when(plugin.getLabel()).thenReturn("Archivage fichiers");
        when(plugin.getPictoClass()).thenReturn("fa-folder");
        when(plugin.getHelp()).thenReturn("<p>Aide</p>");
        when(plugin.getParams()).thenReturn(
                "[{\"code\":\"path\",\"label\":\"Chemin\",\"type\":\"text\",\"req\":true,\"maxlength\":255}]");
        when(this.taskPluginsDiscoverer.getTaskProcessor(UnsavedTaskRemovalIntegrationTest.PLUGIN_CODE))
                .thenReturn(plugin);
    }



    @Test
    @DisplayName("A task added then removed before saving leaves the saved tasks displayed, with a fractional scroll position")
    @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
    @Transactional
    void anUnsavedTaskCanBeRemoved() throws Exception {
        final Process editedProcess = this.processWithArchiveTask();
        final Task savedTask = editedProcess.getTasksCollection().iterator().next();

        final MvcResult addResult = this.mockMvc.perform(
                this.withTask(post("/processes/{processId}/addTask/{taskCode}", editedProcess.getId(),
                                   UnsavedTaskRemovalIntegrationTest.PLUGIN_CODE),
                              editedProcess, 0, savedTask.getId(), "", "0"))
                                                .andExpect(status().isOk())
                                                .andReturn();
        final TaskModel[] tasksAfterAdd = this.processModelOf(addResult).getTasks();
        assertEquals(2, tasksAfterAdd.length, "The added task must be displayed after the saved one.");
        final int addedTaskId = tasksAfterAdd[1].getId();

        final MvcResult deleteResult = this.mockMvc.perform(
                this.withTask(this.withTask(post("/processes/{processId}/deleteTask/{taskId}", editedProcess.getId(),
                                                 addedTaskId),
                                            editedProcess, 0, savedTask.getId(), "",
                                            UnsavedTaskRemovalIntegrationTest.FRACTIONAL_SCROLL_POSITION),
                              editedProcess, 1, addedTaskId, TaskModel.TAG_ADDED, null))
                                                   .andExpect(status().isOk())
                                                   .andReturn();

        final TaskModel[] tasksAfterDelete = this.processModelOf(deleteResult).getTasks();
        assertEquals(1, tasksAfterDelete.length, "Only the saved task must remain.");
        assertEquals(savedTask.getId(), tasksAfterDelete[0].getId(), "The saved task must be the one left.");
        assertEquals(1, this.tasksRepository.findByProcessOrderByPosition(editedProcess).length,
                     "Nothing must have been written for the removed task.");
    }



    /**
     * Creates a process that holds one saved archive task.
     *
     * @return the process data object
     */
    private Process processWithArchiveTask() {
        final Process domainProcess = new Process();
        domainProcess.setName(UnsavedTaskRemovalIntegrationTest.MARKER + " edited");
        domainProcess.setTasksCollection(new ArrayList<>());
        domainProcess.setUsersCollection(new ArrayList<>());
        domainProcess.setUserGroupsCollection(new ArrayList<>());
        final Process savedProcess = this.processesRepository.save(domainProcess);

        final Task domainTask = new Task();
        domainTask.setCode(UnsavedTaskRemovalIntegrationTest.PLUGIN_CODE);
        domainTask.setLabel("Archivage fichiers");
        domainTask.setPosition(1);
        domainTask.setProcess(savedProcess);
        final HashMap<String, String> values = new HashMap<>();
        values.put("path", "/tmp/" + UnsavedTaskRemovalIntegrationTest.MARKER);
        domainTask.setParametersValues(values);
        savedProcess.getTasksCollection().add(this.tasksRepository.save(domainTask));

        return savedProcess;
    }



    /**
     * Adds the process fields, if missing, and the fields of one task block to a submission of the process form.
     *
     * @param request       the submission to complete
     * @param editedProcess the process being edited
     * @param index         the position of the task block in the form
     * @param taskId        the identifier that the task block carries
     * @param tag           the tag that the task block carries
     * @param scrollY       the scroll position of the page, as submitted by the browser (only used with index 0)
     * @return the completed submission
     */
    private MockHttpServletRequestBuilder withTask(final MockHttpServletRequestBuilder request,
            final Process editedProcess, final int index, final int taskId, final String tag,
            final String scrollY) {
        final String prefix = String.format("tasks[%d].", index);

        if (index == 0) {
            request.with(csrf())
                   .param("id", String.valueOf(editedProcess.getId()))
                   .param("name", editedProcess.getName())
                   .param("readOnly", "false")
                   .param("htmlScrollY", scrollY)
                   .param("usersIds", String.valueOf(this.operatorId))
                   .param("userGroupsIds", "");
        }

        return request.param(prefix + "id", String.valueOf(taskId))
                      .param(prefix + "pluginCode", UnsavedTaskRemovalIntegrationTest.PLUGIN_CODE)
                      .param(prefix + "pluginLabel", "Archivage fichiers")
                      .param(prefix + "pluginPictoClass", "fa-folder")
                      .param(prefix + "pluginHelpMessage", "<p>Aide</p>")
                      .param(prefix + "tag", tag)
                      .param(prefix + "position", String.valueOf(index + 1))
                      .param(prefix + "parameters[0].name", "path")
                      .param(prefix + "parameters[0].type", "text")
                      .param(prefix + "parameters[0].label", "Chemin")
                      .param(prefix + "parameters[0].required", "true")
                      .param(prefix + "parameters[0].maxLength", "255")
                      .param(prefix + "parameters[0].value", "/tmp/" + UnsavedTaskRemovalIntegrationTest.MARKER);
    }



    /**
     * Obtains the process that a request passed to the details view.
     *
     * @param result the outcome of the request
     * @return the process model
     */
    private ProcessModel processModelOf(final MvcResult result) {
        assertNotNull(result.getModelAndView(), "The details view must have been displayed.");
        final ProcessModel processModel = (ProcessModel) result.getModelAndView().getModel().get("process");
        assertNotNull(processModel, "The details view must show the process.");

        return processModel;
    }
}
