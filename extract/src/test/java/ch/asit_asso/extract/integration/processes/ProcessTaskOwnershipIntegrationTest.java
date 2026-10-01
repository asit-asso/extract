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
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorDiscovererWrapper;
import ch.asit_asso.extract.integration.DatabaseTestHelper;
import ch.asit_asso.extract.integration.WithMockApplicationUser;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.TasksRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the tasks written when a process is saved (issue #425).
 *
 * Saving a process used to write into whichever task carried the identifier that the form sent, wherever that task
 * lived. A task added through the interface gets a temporary identifier computed from the tasks of the form, so it
 * regularly carries the real identifier of a task of another process: that task was then given the position and
 * the parameters of the added one, and stayed attached to its own process, which no interface can undo.
 *
 * These tests run the real controller against a real PostgreSQL, and assert on the other process, which must come
 * out of the save exactly as it went in.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Process Task Ownership Integration Tests (issue #425)")
class ProcessTaskOwnershipIntegrationTest {

    /**
     * Prefixes the seeded values, so that they cannot collide with the test data set.
     */
    private static final String MARKER = "ZZ425";

    /**
     * The path of the script that the untouched task must still hold.
     */
    private static final String OTHER_SCRIPT = "/opt/scripts/zz425.py";

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
        this.operatorId = this.dbHelper.createTestOperator(ProcessTaskOwnershipIntegrationTest.MARKER + "_operator",
                                                           ProcessTaskOwnershipIntegrationTest.MARKER + " Operator",
                                                           ProcessTaskOwnershipIntegrationTest.MARKER + "@test.ch",
                                                           true);
        ITaskProcessor plugin = mock(ITaskProcessor.class);
        when(plugin.getCode()).thenReturn("ARCHIVE");
        when(plugin.getParams()).thenReturn("[{\"code\":\"path\",\"type\":\"text\"}]");
        when(this.taskPluginsDiscoverer.getTaskProcessor("ARCHIVE")).thenReturn(plugin);
    }



    @Nested
    @DisplayName("1. Adding a task")
    class AddedTaskTests {

        @Test
        @DisplayName("1.1 - A task added with the identifier of another process's task leaves that task alone")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void anAddedTaskDoesNotOverwriteTheTaskOfAnotherProcess() throws Exception {
            final Task otherTask = ProcessTaskOwnershipIntegrationTest.this.pythonTask();
            final Process editedProcess = ProcessTaskOwnershipIntegrationTest.this.process("edited");

            ProcessTaskOwnershipIntegrationTest.this.mockMvc.perform(
                    ProcessTaskOwnershipIntegrationTest.this.saveWithArchiveTask(editedProcess, otherTask.getId(),
                                                                                 true))
                                                            .andExpect(status().is3xxRedirection());

            final Task reloadedOtherTask
                    = ProcessTaskOwnershipIntegrationTest.this.tasksRepository.findById(otherTask.getId())
                                                                             .orElseThrow();
            assertEquals("python", reloadedOtherTask.getCode(), "The task of the other process changed plugin.");
            assertEquals(1, reloadedOtherTask.getPosition(), "The task of the other process changed position.");
            assertEquals(ProcessTaskOwnershipIntegrationTest.OTHER_SCRIPT,
                         reloadedOtherTask.getParametersValues().get("pythonScript"),
                         "The task of the other process lost its parameters.");

            final Task[] savedTasks
                    = ProcessTaskOwnershipIntegrationTest.this.tasksRepository
                            .findByProcessOrderByPosition(editedProcess);
            assertEquals(1, savedTasks.length, "The edited process must have got its new task.");
            assertNotEquals(otherTask.getId(), savedTasks[0].getId(),
                            "The new task must have an identifier of its own.");
            assertEquals("ARCHIVE", savedTasks[0].getCode(), "The new task must use the plugin that was added.");
        }
    }



    @Nested
    @DisplayName("2. Submitting a task that belongs to another process")
    class ForeignTaskTests {

        @Test
        @DisplayName("2.1 - The save is refused and nothing is written")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void aForeignTaskIdentifierIsRefused() throws Exception {
            final Task otherTask = ProcessTaskOwnershipIntegrationTest.this.pythonTask();
            final Process editedProcess = ProcessTaskOwnershipIntegrationTest.this.process("edited");

            ProcessTaskOwnershipIntegrationTest.this.mockMvc.perform(
                    ProcessTaskOwnershipIntegrationTest.this.saveWithArchiveTask(editedProcess, otherTask.getId(),
                                                                                 false))
                                                            .andExpect(status().isOk());

            final Task reloadedOtherTask
                    = ProcessTaskOwnershipIntegrationTest.this.tasksRepository.findById(otherTask.getId())
                                                                             .orElseThrow();
            assertEquals("python", reloadedOtherTask.getCode(), "The task of the other process changed plugin.");
            assertEquals(ProcessTaskOwnershipIntegrationTest.OTHER_SCRIPT,
                         reloadedOtherTask.getParametersValues().get("pythonScript"),
                         "The task of the other process lost its parameters.");
            assertEquals(0,
                         ProcessTaskOwnershipIntegrationTest.this.tasksRepository
                                 .findByProcessOrderByPosition(editedProcess).length,
                         "Nothing must have been written for the edited process.");
        }



        @Test
        @DisplayName("2.2 - Creating a process with a task that claims an identifier writes nothing at all")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void aForeignTaskIdentifierIsRefusedOnCreation() throws Exception {
            final Task otherTask = ProcessTaskOwnershipIntegrationTest.this.pythonTask();
            final long processesBefore
                    = ProcessTaskOwnershipIntegrationTest.this.processesRepository.count();

            ProcessTaskOwnershipIntegrationTest.this.mockMvc.perform(
                    ProcessTaskOwnershipIntegrationTest.this.createWithArchiveTask(otherTask.getId()))
                                                            .andExpect(status().isOk());

            assertEquals(processesBefore,
                         ProcessTaskOwnershipIntegrationTest.this.processesRepository.count(),
                         "No process must have been created.");
            final Task reloadedOtherTask
                    = ProcessTaskOwnershipIntegrationTest.this.tasksRepository.findById(otherTask.getId())
                                                                             .orElseThrow();
            assertEquals("python", reloadedOtherTask.getCode(), "The task of the other process changed plugin.");
            assertEquals(1, reloadedOtherTask.getPosition(), "The task of the other process changed position.");
        }
    }




    /**
     * Creates a process that holds no task.
     *
     * The collections are set even though they are empty, because a process that has just been persisted carries
     * null ones until it is read again, whereas the application always works on processes that it has loaded, and
     * thus on empty collections.
     *
     * @param nameSuffix what tells this process apart from the other ones of the test
     * @return the process data object
     */
    private Process process(final String nameSuffix) {
        final Process domainProcess = new Process();
        domainProcess.setName(String.format("%s %s", ProcessTaskOwnershipIntegrationTest.MARKER, nameSuffix));
        domainProcess.setTasksCollection(new ArrayList<>());
        domainProcess.setUsersCollection(new ArrayList<>());
        domainProcess.setUserGroupsCollection(new ArrayList<>());

        return this.processesRepository.save(domainProcess);
    }



    /**
     * Creates a process of its own holding a task that uses a plugin with mandatory parameters.
     *
     * @return the task data object
     */
    private Task pythonTask() {
        final Process otherProcess = this.process("other");
        final Task domainTask = new Task();
        domainTask.setCode("python");
        domainTask.setLabel("Extraction Python");
        domainTask.setPosition(1);
        domainTask.setProcess(otherProcess);
        final HashMap<String, String> values = new HashMap<>();
        values.put("pythonScript", ProcessTaskOwnershipIntegrationTest.OTHER_SCRIPT);
        values.put("pythonInterpreter", "/usr/bin/python3");
        domainTask.setParametersValues(values);

        return this.tasksRepository.save(domainTask);
    }



    /**
     * Builds the submission of a process holding a single archive task that carries a given identifier.
     *
     * @param editedProcess the process being saved
     * @param taskId        the identifier that the task block carries
     * @param added         <code>true</code> to tag the task as one that the interface has just added
     * @return the request to perform
     */
    private MockHttpServletRequestBuilder saveWithArchiveTask(final Process editedProcess, final int taskId,
            final boolean added) {
        return post("/processes/{id}", editedProcess.getId()).with(csrf())
                .param("id", String.valueOf(editedProcess.getId()))
                .param("name", editedProcess.getName())
                .param("readOnly", "false")
                .param("htmlScrollY", "0")
                .param("usersIds", String.valueOf(this.operatorId))
                .param("userGroupsIds", "")
                .param("tasks[0].id", String.valueOf(taskId))
                .param("tasks[0].pluginCode", "ARCHIVE")
                .param("tasks[0].pluginLabel", "Archivage fichiers")
                .param("tasks[0].pluginPictoClass", "fa-folder")
                .param("tasks[0].tag", added ? "ADDED" : "")
                .param("tasks[0].position", "1")
                .param("tasks[0].parameters[0].name", "path")
                .param("tasks[0].parameters[0].type", "text")
                .param("tasks[0].parameters[0].label", "Chemin")
                .param("tasks[0].parameters[0].required", "true")
                .param("tasks[0].parameters[0].maxLength", "255")
                .param("tasks[0].parameters[0].value", "/tmp/" + ProcessTaskOwnershipIntegrationTest.MARKER);
    }



    /**
     * Builds the creation of a process holding a single archive task that claims a given identifier.
     *
     * @param taskId the identifier that the task block carries
     * @return the request to perform
     */
    private MockHttpServletRequestBuilder createWithArchiveTask(final int taskId) {
        return post("/processes/add").with(csrf())
                .param("id", "0")
                .param("name", ProcessTaskOwnershipIntegrationTest.MARKER + " created")
                .param("readOnly", "false")
                .param("htmlScrollY", "0")
                .param("usersIds", String.valueOf(this.operatorId))
                .param("userGroupsIds", "")
                .param("tasks[0].id", String.valueOf(taskId))
                .param("tasks[0].pluginCode", "ARCHIVE")
                .param("tasks[0].pluginLabel", "Archivage fichiers")
                .param("tasks[0].pluginPictoClass", "fa-folder")
                .param("tasks[0].tag", "")
                .param("tasks[0].position", "1")
                .param("tasks[0].parameters[0].name", "path")
                .param("tasks[0].parameters[0].type", "text")
                .param("tasks[0].parameters[0].label", "Chemin")
                .param("tasks[0].parameters[0].required", "true")
                .param("tasks[0].parameters[0].maxLength", "255")
                .param("tasks[0].parameters[0].value", "/tmp/" + ProcessTaskOwnershipIntegrationTest.MARKER);
    }
}
