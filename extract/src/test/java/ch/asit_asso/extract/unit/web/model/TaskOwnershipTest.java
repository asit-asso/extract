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
package ch.asit_asso.extract.unit.web.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.persistence.TasksRepository;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.services.SecretParameters;
import ch.asit_asso.extract.testutils.TestSecrets;
import ch.asit_asso.extract.web.model.ProcessModel;
import ch.asit_asso.extract.web.model.TaskModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that saving a process cannot write into a task of another process (issue #425).
 *
 * The identifier that the form carries for a task is not trustworthy: a task added through the interface gets a
 * temporary identifier computed from the tasks of the form, which is very likely to be the real identifier of a
 * task of another process, and the browser can restore a value of its own into a field of the same name. Only the
 * tasks that the edited process holds may be written to.
 *
 * @author Bruno Alves
 */
@DisplayName("Task Ownership Tests (issue #425)")
class TaskOwnershipTest {

    /**
     * The parameters of a plugin whose settings are mandatory, as the Python extraction plugin's are.
     */
    private static final String PYTHON_PARAMETERS
            = "[{\"code\": \"pythonScript\", \"label\": \"Script\", \"type\": \"text\", \"req\": true,"
            + " \"maxlength\": 500}]";

    /**
     * The parameters of another plugin, which have nothing in common with the ones above.
     */
    private static final String ARCHIVE_PARAMETERS
            = "[{\"code\": \"path\", \"label\": \"Chemin\", \"type\": \"text\", \"req\": true, \"maxlength\": 255}]";

    /**
     * The identifier of the task that belongs to another process.
     */
    private static final int FOREIGN_TASK_ID = 1473;

    private TasksRepository tasksRepository;
    private SecretParameters secretParameters;



    @BeforeEach
    public void setUp() throws Exception {
        this.tasksRepository = mock(TasksRepository.class);
        this.secretParameters = new SecretParameters(TestSecrets.create());
        when(this.tasksRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }



    @Nested
    @DisplayName("1. Saving a task")
    class SavingTests {

        @Test
        @DisplayName("1.1 - A task that has just been added does not write into the task that shares its identifier")
        void anAddedTaskLeavesTheTaskWithTheSameIdentifierAlone() {
            final Task foreignTask = TaskOwnershipTest.this.pythonTaskInTheDataSource(TaskOwnershipTest.FOREIGN_TASK_ID);
            final Process editedProcess = TaskOwnershipTest.this.process(1426);
            final TaskModel addedTask = TaskOwnershipTest.this.taskModel(ITaskProcessorStub.ARCHIVE);
            addedTask.setTag(TaskModel.TAG_ADDED);
            addedTask.setId(TaskOwnershipTest.FOREIGN_TASK_ID);
            addedTask.setPosition(6);

            addedTask.saveInDataSource(TaskOwnershipTest.this.tasksRepository, editedProcess,
                                       TaskOwnershipTest.this.secretParameters);

            final ArgumentCaptor<Task> savedTask = ArgumentCaptor.forClass(Task.class);
            verify(TaskOwnershipTest.this.tasksRepository).save(savedTask.capture());
            assertNotSame(foreignTask, savedTask.getValue(), "The added task must not be the task of the other"
                    + " process.");
            assertNull(savedTask.getValue().getId(), "A task that does not exist yet must be saved without an"
                    + " identifier, so that the data source assigns a real one.");
            assertSame(editedProcess, savedTask.getValue().getProcess(), "The added task must belong to the edited"
                    + " process.");
            assertEquals("python", foreignTask.getCode(), "The task of the other process must be untouched.");
            assertEquals(2, foreignTask.getPosition(), "The position of the task of the other process must be"
                    + " untouched.");
            assertEquals("/opt/scripts/other.py", foreignTask.getParametersValues().get("pythonScript"),
                         "The parameters of the task of the other process must be untouched.");
        }



        @Test
        @DisplayName("1.2 - A task identifier that is none of the process's tasks is refused without writing")
        void aForeignTaskIdentifierIsRefused() {
            final Task foreignTask
                    = TaskOwnershipTest.this.pythonTaskInTheDataSource(TaskOwnershipTest.FOREIGN_TASK_ID);
            final Process editedProcess = TaskOwnershipTest.this.process(1426);
            final TaskModel foreignTaskModel = TaskOwnershipTest.this.taskModel(ITaskProcessorStub.ARCHIVE);
            foreignTaskModel.setId(TaskOwnershipTest.FOREIGN_TASK_ID);
            foreignTaskModel.setPosition(6);

            assertThrows(IllegalArgumentException.class,
                         () -> foreignTaskModel.saveInDataSource(TaskOwnershipTest.this.tasksRepository,
                                                                 editedProcess,
                                                                 TaskOwnershipTest.this.secretParameters));
            verify(TaskOwnershipTest.this.tasksRepository, never()).save(any(Task.class));
            assertEquals(2, foreignTask.getPosition(), "The task of the other process must be untouched.");
            assertEquals("/opt/scripts/other.py", foreignTask.getParametersValues().get("pythonScript"),
                         "The parameters of the task of the other process must be untouched.");
        }



        @Test
        @DisplayName("1.3 - A task of the process is still updated in place")
        void aTaskOfTheProcessIsUpdatedInPlace() {
            final Process editedProcess = TaskOwnershipTest.this.process(1426);
            final Task ownTask = TaskOwnershipTest.this.archiveTask(12, editedProcess);
            final TaskModel taskModel = TaskOwnershipTest.this.taskModel(ITaskProcessorStub.ARCHIVE);
            taskModel.setId(12);
            taskModel.setPosition(3);
            taskModel.getParameterByName("path").setValue("/tmp/nouveau");

            taskModel.saveInDataSource(TaskOwnershipTest.this.tasksRepository, editedProcess,
                                       TaskOwnershipTest.this.secretParameters);

            final ArgumentCaptor<Task> savedTask = ArgumentCaptor.forClass(Task.class);
            verify(TaskOwnershipTest.this.tasksRepository).save(savedTask.capture());
            assertSame(ownTask, savedTask.getValue(), "The task of the process must be the one that is saved.");
            assertEquals(3, ownTask.getPosition(), "The position must have been updated.");
            assertEquals("/tmp/nouveau", ownTask.getParametersValues().get("path"),
                         "The parameter must have been updated.");
        }
    }



    @Nested
    @DisplayName("2. Checking the submitted tasks")
    class ForeignTaskIdsTests {

        @Test
        @DisplayName("2.1 - A submitted task that belongs to another process is reported")
        void aForeignTaskIsReported() {
            final Process editedProcess = TaskOwnershipTest.this.process(1426);
            TaskOwnershipTest.this.archiveTask(12, editedProcess);
            final ProcessModel processModel = TaskOwnershipTest.this.processModel(1426, 12,
                                                                                  TaskOwnershipTest.FOREIGN_TASK_ID);

            assertArrayEquals(new Integer[]{TaskOwnershipTest.FOREIGN_TASK_ID},
                              processModel.getForeignTaskIds(editedProcess));
        }



        @Test
        @DisplayName("2.2 - Tasks that all belong to the process are accepted")
        void ownTasksAreAccepted() {
            final Process editedProcess = TaskOwnershipTest.this.process(1426);
            TaskOwnershipTest.this.archiveTask(12, editedProcess);
            TaskOwnershipTest.this.archiveTask(13, editedProcess);
            final ProcessModel processModel = TaskOwnershipTest.this.processModel(1426, 12, 13);

            assertArrayEquals(new Integer[]{}, processModel.getForeignTaskIds(editedProcess));
        }



        @Test
        @DisplayName("2.3 - The temporary identifier of an added task is not reported")
        void anAddedTaskIsNotReported() {
            final Process editedProcess = TaskOwnershipTest.this.process(1426);
            final ProcessModel processModel = TaskOwnershipTest.this.processModel(1426);
            final TaskModel addedTask = TaskOwnershipTest.this.taskModel(ITaskProcessorStub.ARCHIVE);
            addedTask.setTag(TaskModel.TAG_ADDED);
            addedTask.setId(TaskOwnershipTest.FOREIGN_TASK_ID);
            processModel.setTasks(new TaskModel[]{addedTask});

            assertArrayEquals(new Integer[]{}, processModel.getForeignTaskIds(editedProcess));
        }



        @Test
        @DisplayName("2.4 - A task claiming an identifier is reported when the process is being created")
        void aTaskWithAnIdentifierIsReportedOnCreation() {
            final ProcessModel processModel = TaskOwnershipTest.this.processModel(0,
                                                                                  TaskOwnershipTest.FOREIGN_TASK_ID);

            assertArrayEquals(new Integer[]{TaskOwnershipTest.FOREIGN_TASK_ID}, processModel.getForeignTaskIds());
        }



        @Test
        @DisplayName("2.5 - The tasks of a process being created are accepted when they are all new")
        void addedTasksAreAcceptedOnCreation() {
            final ProcessModel processModel = TaskOwnershipTest.this.processModel(0);
            final TaskModel addedTask = TaskOwnershipTest.this.taskModel(ITaskProcessorStub.ARCHIVE);
            addedTask.setTag(TaskModel.TAG_ADDED);
            addedTask.setId(TaskOwnershipTest.FOREIGN_TASK_ID);
            processModel.setTasks(new TaskModel[]{addedTask});

            assertArrayEquals(new Integer[]{}, processModel.getForeignTaskIds());
        }
    }



    @Nested
    @DisplayName("3. Reading a task whose stored parameters do not fit its plugin")
    class CorruptedParametersTests {

        @Test
        @DisplayName("3.1 - A task that lost its mandatory parameters can still be read")
        void aTaskWithTheParametersOfAnotherPluginIsReadable() {
            final Task corruptedTask = new Task(TaskOwnershipTest.FOREIGN_TASK_ID);
            corruptedTask.setCode("python");
            corruptedTask.setPosition(6);
            final HashMap<String, String> archiveValues = new HashMap<>();
            archiveValues.put("path", "/tmp/archive");
            corruptedTask.setParametersValues(archiveValues);

            final TaskModel taskModel = new TaskModel(corruptedTask, ITaskProcessorStub.PYTHON.plugin(),
                                                      TaskOwnershipTest.this.secretParameters);

            assertNull(taskModel.getParameterByName("pythonScript").getValue(),
                       "The parameter that the data source no longer holds must be left empty.");
            assertEquals(TaskOwnershipTest.FOREIGN_TASK_ID, taskModel.getId(),
                         "The rest of the task must have been read.");
        }



        @Test
        @DisplayName("3.2 - A task without any stored parameter can still be read")
        void aTaskWithoutStoredParametersIsReadable() {
            final Task emptyTask = new Task(12);
            emptyTask.setCode("python");
            emptyTask.setPosition(1);

            final TaskModel taskModel = new TaskModel(emptyTask, ITaskProcessorStub.PYTHON.plugin(),
                                                      TaskOwnershipTest.this.secretParameters);

            assertNull(taskModel.getParameterByName("pythonScript").getValue(),
                       "The mandatory parameter must be left empty.");
        }
    }



    /**
     * Makes a process data object that holds no task yet.
     *
     * @param processId the identifier of the process
     * @return the process data object
     */
    private Process process(final int processId) {
        final Process domainProcess = new Process(processId);
        domainProcess.setTasksCollection(new ArrayList<>());

        return domainProcess;
    }



    /**
     * Makes a task of the Python plugin that belongs to a process of its own, and hands it out to whoever fetches
     * it by its identifier alone.
     *
     * That global lookup is what used to make the bug: the task is not one of the edited process's, so the code
     * under test must not reach for it, and these tests fail if it does.
     *
     * @param taskId the identifier of the task
     * @return the task data object
     */
    private Task pythonTaskInTheDataSource(final int taskId) {
        final Process otherProcess = this.process(1629);
        final Task domainTask = new Task(taskId);
        domainTask.setCode("python");
        domainTask.setPosition(2);
        domainTask.setProcess(otherProcess);
        final HashMap<String, String> values = new HashMap<>();
        values.put("pythonScript", "/opt/scripts/other.py");
        domainTask.setParametersValues(values);
        otherProcess.getTasksCollection().add(domainTask);
        when(this.tasksRepository.findById(taskId)).thenReturn(Optional.of(domainTask));

        return domainTask;
    }



    /**
     * Makes a task of the archive plugin and adds it to a process.
     *
     * @param taskId        the identifier of the task
     * @param domainProcess the process that the task is part of
     * @return the task data object
     */
    private Task archiveTask(final int taskId, final Process domainProcess) {
        final Task domainTask = new Task(taskId);
        domainTask.setCode("ARCHIVE");
        domainTask.setPosition(1);
        domainTask.setProcess(domainProcess);
        final HashMap<String, String> values = new HashMap<>();
        values.put("path", "/tmp/ancien");
        domainTask.setParametersValues(values);
        domainProcess.getTasksCollection().add(domainTask);

        return domainTask;
    }



    /**
     * Makes the model of a task that uses a given plugin.
     *
     * @param plugin the plugin used by the task
     * @return the task model
     */
    private TaskModel taskModel(final ITaskProcessorStub plugin) {
        return new TaskModel(plugin.plugin());
    }



    /**
     * Makes the model of a process that submits tasks with the given identifiers.
     *
     * @param processId the identifier of the process
     * @param taskIds   the identifiers carried by the submitted tasks
     * @return the process model
     */
    private ProcessModel processModel(final int processId, final int... taskIds) {
        final ProcessModel processModel = new ProcessModel();
        processModel.setId(processId);
        final List<TaskModel> tasks = new ArrayList<>();

        for (int taskId : taskIds) {
            final TaskModel taskModel = this.taskModel(ITaskProcessorStub.ARCHIVE);
            taskModel.setId(taskId);
            tasks.add(taskModel);
        }

        processModel.setTasks(tasks.toArray(TaskModel[]::new));

        return processModel;
    }



    /**
     * The task plugins that these tests use.
     */
    private enum ITaskProcessorStub {

        /**
         * A plugin whose mandatory parameter is a script path.
         */
        PYTHON("python", TaskOwnershipTest.PYTHON_PARAMETERS),

        /**
         * A plugin whose mandatory parameter is a folder path.
         */
        ARCHIVE("ARCHIVE", TaskOwnershipTest.ARCHIVE_PARAMETERS);

        private final String code;

        private final String parameters;



        ITaskProcessorStub(final String pluginCode, final String pluginParameters) {
            this.code = pluginCode;
            this.parameters = pluginParameters;
        }



        /**
         * Makes an instance of this plugin.
         *
         * @return the plugin
         */
        public ITaskProcessor plugin() {
            final ITaskProcessor plugin = mock(ITaskProcessor.class);
            when(plugin.getCode()).thenReturn(this.code);
            when(plugin.getLabel()).thenReturn(this.code);
            when(plugin.getParams()).thenReturn(this.parameters);

            return plugin;
        }
    }
}
