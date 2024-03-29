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
package ch.asit_asso.extract.web.model;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.persistence.*;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorDiscovererWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;



/**
 * The representation of a process for a view.
 *
 * @author Florent Krin
 */
public class ProcessModel {

    /**
     * The number that uniquely identifies this process.
     */
    private int id;

    /**
     * Whether this process can be removed from the data source.
     */
    private boolean deletable;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The user-friendly name of this process.
     */
    private String name;

    /**
     * Whether updates to this process should be allowed.
     */
    private boolean readOnly;

    /**
     * The steps of this process.
     */
    private final List<TaskModel> tasksList;

    /**
     * The operators groups associated to this process.
     */
    private final List<UserGroup> userGroupsList;

    /**
     * An array that contains the identifiers of the operators groups associated with this process.
     */
    private String[] userGroupsIds;

    /**
     * The operators associated to this process.
     */
    private final List<UserModel> usersList;

    /**
     * An array that contains the identifiers of the operators associated with this process.
     */
    private String[] usersIds;

    /**
     * The vertical scroll position of the page.
     */
    private int htmlScrollY;



    /**
     * Obtains the number that uniquely identifies this process.
     *
     * @return the identifier
     */
    public final int getId() {
        return id;
    }



    /**
     * Defines the number that uniquely identifies this process.
     *
     * @param processId the identifier
     */
    public final void setId(final int processId) {
        this.id = processId;
    }



    /**
     * Obtains the user-friendly name of this process.
     *
     * @return the name
     */
    public final String getName() {
        return this.name;
    }



    /**
     * Defines the user-friendly name of this process.
     *
     * @param processName the name
     */
    public final void setName(final String processName) {
        this.name = processName;
    }



    /**
     * Obtains the vertical scroll position of the page.
     *
     * @return the number of pixels from the top of the page
     */
    public final int getHtmlScrollY() {
        return this.htmlScrollY;
    }



    /**
     * Defines the vertical scroll position of the page.
     *
     * @param scrollPosition the number of pixels from the top of the page
     */
    public final void setHtmlScrollY(final int scrollPosition) {
        this.htmlScrollY = scrollPosition;
    }



    /**
     * Obtains the steps of this process.
     *
     * @return an array containing the tasks that make up this process
     */
    public final TaskModel[] getTasks() {
        return this.tasksList.toArray(new TaskModel[]{});
    }



    /**
     * Obtains a task that compose this process.
     *
     * @param taskId the number that identifies the task
     * @return the model that represents the task
     */
    public final TaskModel getTask(final int taskId) {
        assert taskId > 0 : "The task identifier must be strictly positive.";

        for (TaskModel task : this.getTasks()) {

            if (task.getId() == taskId) {
                return task;
            }
        }

        return null;
    }



    /**
     * Obtains the users of this process.
     *
     * @return an array containing the users that make up this process
     */
    public final UserGroup[] getUserGroups() {
        return this.userGroupsList.toArray(new UserGroup[]{});
    }


    /**
     * Obtains the identifiers of the operators groups associated to this process.
     *
     * @return a string with the identifiers separated by commas
     */
    public final String getUserGroupsIds() {
        return StringUtils.join(this.userGroupsIds, ',');
    }



    /**
     * Obtains the users of this process.
     *
     * @return an array containing the users that make up this process
     */
    public final UserModel[] getUsers() {
        return this.usersList.toArray(new UserModel[]{});
    }



    /**
     * Obtains the identifiers of the operators associated to this process.
     *
     * @return a string with the identifiers separated by commas
     */
    public final String getUsersIds() {
        return StringUtils.join(this.usersIds, ',');
    }



    /**
     * Gets the data objects for the tasks that compose this process.
     *
     * @param domainProcess the data object for this process
     * @return a list of task data objects
     */
    private List<Task> getDomainTasks(final Process domainProcess) {
        List<Task> tasksList = new ArrayList<>();

        for (TaskModel taskModel : this.tasksList) {
            Task domainTask = taskModel.createDomainTask(domainProcess);
            tasksList.add(domainTask);
        }

        return tasksList;
    }



    /**
     * Defines the steps of this process.
     *
     * @param tasks an array containing the tasks that make up this process
     */
    public final void setTasks(final TaskModel[] tasks) {
        this.tasksList.clear();
        this.tasksList.addAll(Arrays.asList(tasks));
    }



    /**
     * Defines the users groups of this process.
     *
     * @param userGroups an array containing the user groups that operate on this process
     */
    public final void setUserGroups(final UserGroup[] userGroups) {
        this.userGroupsList.clear();
        this.userGroupsList.addAll(Arrays.asList(userGroups));

        List<String> list = new ArrayList<>();

        for (UserGroup userGroup : this.userGroupsList) {
            list.add(userGroup.getId().toString());
        }
        this.usersIds = list.toArray(new String[]{});
    }



    /**
     * Defines the identifiers of the operators groups associated with this process.
     *
     * @param joinedUserGroupsIds a string with the operator group identifiers separated by commas
     */
    public final void setUserGroupsIds(final String joinedUserGroupsIds) {
        this.userGroupsIds = joinedUserGroupsIds.split(",");
    }



    /**
     * Defines the users of this process.
     *
     * @param users an array containing the operators directly attached to this process
     */
    public final void setUsers(final UserModel[] users) {
        this.usersList.clear();
        this.usersList.addAll(Arrays.asList(users));

        List<String> list = new ArrayList<>();
        for (UserModel user : this.usersList) {
            list.add(user.getId().toString());
        }
        this.usersIds = list.toArray(new String[]{});
    }



    /**
     * Defines the identifiers of the operators associated with this process.
     *
     * @param joinedUsersIds a string with the operator identifiers separated by commas
     */
    public final void setUsersIds(final String joinedUsersIds) {
        this.usersIds = joinedUsersIds.split(",");
    }



    /**
     * Inserts a task in this process.
     *
     * @param taskToAdd the new task
     */
    public final void addTask(final TaskModel taskToAdd) {

        if (taskToAdd == null) {
            throw new IllegalArgumentException("The task to add cannot be null.");
        }
        taskToAdd.setPosition(this.tasksList.size() + 1);
        if (TaskModel.TAG_ADDED.equals(taskToAdd.getTag())) {
            taskToAdd.setId(this.getTemporaryRuleId());
        }
        this.logger.debug("Adding a task (ID: {}) at position {}.", taskToAdd.getId(), taskToAdd.getPosition());

        this.tasksList.add(taskToAdd);
    }



    /**
     * Provides a temporary identifier for a new rule. This only ensures that the new object has a unique
     * identifier among the rules for this controller until it is saved. But this value should be ignored when this
     * rule is persisted. The real identifier should be assigned by the regular means, e.g. database sequence.
     *
     * @return a temporary identifier that is unique among the rules for this connector
     */
    private int getTemporaryRuleId() {
        int maxTaskId = 0;

        for (TaskModel task : this.tasksList) {
            int taskId = task.getId();

            if (taskId > maxTaskId) {
                maxTaskId = taskId;
            }
        }

        return maxTaskId + 1;
    }



    /**
     * Obtains whether this process should prevent updates.
     *
     * @return <code>true</code> if editing must be disabled
     */
    public final boolean isReadOnly() {
        return this.readOnly;
    }



    /**
     * Defines whether this process should prevent updates.
     *
     * @param isReadOnly <code>true</code> to disallow updates
     */
    public final void setReadOnly(final boolean isReadOnly) {
        this.readOnly = isReadOnly;
    }



    /**
     * Obtains whether this process can be removed from the data source.
     *
     * @return <code>true</code> if this process is in a state that allows its deletion
     */
    public final boolean isDeletable() {
        return this.deletable;
    }



    /**
     * Defines whether this process can be removed from the data source.
     *
     * @param canBeDeleted <code>true</code> if this process is in a state that allows its deletion
     */
    public final void setDeletable(final boolean canBeDeleted) {
        this.deletable = canBeDeleted;
    }



    /**
     * Creates a new instance of this model.
     */
    public ProcessModel() {
        this.tasksList = new ArrayList<>();
        this.userGroupsList = new ArrayList<>();
        this.usersList = new ArrayList<>();
    }



    /**
     * Creates a new instance of this model.
     *
     * @param processId   the number that uniquely identifies the process that this model represents
     * @param processName the name of the process that this model represents
     */
    public ProcessModel(final int processId, final String processName) {
        this();

        this.setId(processId);
        this.setName(processName);
    }



    /**
     * Creates a new process model instance.
     *
     * @param domainProcess         the data object for the process to represent
     * @param taskPluginsDiscoverer the available task plugins provider
     * @param requestsRepository    the link between request data objects and the data source
     */
    public ProcessModel(final Process domainProcess,
                        final TaskProcessorDiscovererWrapper taskPluginsDiscoverer, final RequestsRepository requestsRepository) {
        this();

        if (domainProcess == null) {
            throw new IllegalArgumentException("The process data object cannot be null.");
        }

        if (taskPluginsDiscoverer == null) {
            throw new IllegalArgumentException("The task plugin discoverer cannot be null.");
        }

        if (requestsRepository == null) {
            this.logger.warn("The requests repository that was passed is null. The state of the requests bound to this"
                    + " process will be computed from the full collection, which can be VERY long if there are a large"
                    + " number of requests in the data source. See if you can pass the requests repository for better"
                    + " performance.");
        }

        this.setId(domainProcess.getId());
        this.setName(domainProcess.getName());
        this.readOnly = (requestsRepository != null) ? !domainProcess.canBeEdited(requestsRepository)
                : !domainProcess.canBeEdited();
        this.deletable = (requestsRepository != null) ? domainProcess.canBeDeleted(requestsRepository)
                : domainProcess.canBeDeleted();
        this.setTasksFromDomainObject(domainProcess, taskPluginsDiscoverer);
        this.setUserGroupsFromDomainObject(domainProcess);
        this.setUsersFromDomainObject(domainProcess);
    }



    public final Process createInDataSource(ProcessesRepository processRepository, TasksRepository taskRepository,
                                            UsersRepository userRepository, UserGroupsRepository userGroupsRepository) {
        Process domainProcess = this.createDomainObject(userRepository, userGroupsRepository);

        return this.saveInDataSource(processRepository, taskRepository, userRepository, domainProcess);
    }



    private final Process saveInDataSource(ProcessesRepository processRepository,
                                           TasksRepository taskRepository, UsersRepository userRepository,
                                           Process domainProcess) {
        domainProcess = processRepository.save(domainProcess);
        this.setId(domainProcess.getId());
        this.updateTasksInDataSource(taskRepository, domainProcess);

        return domainProcess;
    }



    public final Process updateInDataSource(ProcessesRepository processRepository,
                                            TasksRepository taskRepository, UsersRepository userRepository,
                                            UserGroupsRepository userGroupsRepository, Process domainProcess) {
        domainProcess = this.updateDomainObject(domainProcess, userRepository, userGroupsRepository);

        return this.saveInDataSource(processRepository, taskRepository, userRepository, domainProcess);
    }



    public final void updateTasksInDataSource(TasksRepository taskRepository,
            Process domainProcess) {

        for (TaskModel taskModel : this.getTasks()) {
            this.logger.debug("Updating task in position {} with plugin {}.", taskModel.getPosition(), taskModel.getPluginCode());
            taskModel.saveInDataSource(taskRepository, domainProcess);
        }

        for (Task taskToDelete : this.getDeletedDomainTasks(domainProcess)) {
            this.logger.info("The task with identifier {} has been deleted.", taskToDelete.getId());
            taskRepository.delete(taskToDelete);
        }
    }



    /**
     * Make a new process data object based on the current values in this model.
     *
     * @return the new process data object
     */
    public final Process createDomainObject(UsersRepository userRepository, UserGroupsRepository userGroupsRepository) {
        final Process domainProcess = new Process();
        domainProcess.setId(this.getId());

        return this.updateDomainObject(domainProcess, userRepository, userGroupsRepository);
    }



    /**
     * Reports the current values in this model to the data object for the represented process.
     *
     * @param domainProcess the represented process data object
     * @return the updated data object
     */
    public final Process updateDomainObject(final Process domainProcess, final UsersRepository userRepository,
                                            final UserGroupsRepository userGroupsRepository) {

        if (domainProcess == null) {
            throw new IllegalArgumentException("The process data object cannot be null.");
        }

        domainProcess.setName(this.getName());
        this.copyUserGroups(domainProcess, userGroupsRepository);
        this.copyUsers(domainProcess, userRepository);

        return domainProcess;
    }



    /**
     * Obtains the tasks that were originally set for this process but have since been removed.
     *
     * @param domainProcess the data object for this process
     * @return an array with the data objects for the deleted tasks
     */
    public final Task[] getDeletedDomainTasks(final Process domainProcess) {

        if (domainProcess == null) {
            throw new IllegalArgumentException("The process data object cannot be null.");
        }

        if (domainProcess.getId() != this.getId()) {
            throw new IllegalArgumentException("The data object is not the process that this model represents.");
        }

        List<Task> deletedTasks = new ArrayList<>();
        Collection<Task> tasksInDataSource = domainProcess.getTasksCollection();

        if (tasksInDataSource != null) {

            for (Task domainTask : tasksInDataSource) {
                TaskModel taskToDelete = this.getTask(domainTask.getId());

                if (taskToDelete == null) {
                    deletedTasks.add(domainTask);
                }
            }
        }

        return deletedTasks.toArray(Task[]::new);
    }



    /**
     * Defines the process tasks in this model based on what is in the data source.
     *
     * @param domainProcess         the data object for this process
     * @param taskPluginsDiscoverer the available task plugins provider
     */
    private void setTasksFromDomainObject(final Process domainProcess,
            final TaskProcessorDiscovererWrapper taskPluginsDiscoverer) {
        assert domainProcess != null : "The process data object must not be null.";
        assert taskPluginsDiscoverer != null : "The task plugin discoverer must not be null.";

        for (Task task : domainProcess.getTasksCollection()) {
            ITaskProcessor taskPlugin = taskPluginsDiscoverer.getTaskProcessor(task.getCode());

            if (taskPlugin == null) {
                this.logger.warn("Could not find the plugin {} used by task with ID {}.", task.getCode(), task.getId());
                continue;
            }

            this.addTask(new TaskModel(task, taskPlugin));
        }

    }



    /**
     * Defines the process operators groups in this model based on what is in the data source.
     *
     * @param domainProcess the data object for this process
     */
    private void setUserGroupsFromDomainObject(final Process domainProcess) {
        assert domainProcess != null : "The process data object must not be null.";

        List<String> userGroupsIdsList = new ArrayList<>();

        for (UserGroup userGroup : domainProcess.getUserGroupsCollection()) {
            this.userGroupsList.add(userGroup);
            userGroupsIdsList.add(userGroup.getId().toString());
        }

        this.userGroupsIds = userGroupsIdsList.toArray(String[]::new);
    }



    /**
     * Defines the process operators in this model based on what is in the data source.
     *
     * @param domainProcess the data object for this process
     */
    private void setUsersFromDomainObject(final Process domainProcess) {
        assert domainProcess != null : "The process data object must not be null.";

        List<String> usersIdsList = new ArrayList<>();
        for (User user : domainProcess.getUsersCollection()) {
            this.usersList.add(new UserModel(user));
            usersIdsList.add(user.getId().toString());
        }
        this.usersIds = usersIdsList.toArray(String[]::new);
    }



    /**
     * Removes the task that matches the provided identifier. (There should only be one
     * anyway.)
     *
     * @param taskId the number that identifies the task to remove
     */
    public final void removeTask(final int taskId) {
        TaskModel taskToRemove = this.getTask(taskId);

        if (taskToRemove == null) {
            return;
        }

        this.removeTask(taskToRemove);
    }



    /**
     * Removes the given task from the collection.
     *
     * @param task the task to remove
     */
    public final void removeTask(final TaskModel task) {

        if (task == null) {
            throw new IllegalArgumentException("The task to remove cannot be null.");
        }

        this.tasksList.remove(task);
    }



    /**
     * Creates models to represent a collection of process data objects.
     *
     * @param domainObjectsCollection the process data objects to represent
     * @param taskPluginsDiscoverer   the link to the task plugins that are available in the application
     * @param requestsRepository      the link between the request data objects and the data source
     * @return a collection that contains the models representing the data objects
     */
    public static Collection<ProcessModel> fromDomainObjectsCollection(
            final Iterable<Process> domainObjectsCollection,
            final TaskProcessorDiscovererWrapper taskPluginsDiscoverer, final RequestsRepository requestsRepository) {

        if (domainObjectsCollection == null) {
            throw new IllegalArgumentException("The collection of process data objects cannot be null.");
        }

        if (taskPluginsDiscoverer == null) {
            throw new IllegalArgumentException("The task plugins discoverer cannot be null.");
        }

        List<ProcessModel> modelsList = new ArrayList<>();

        for (Process domainProcess : domainObjectsCollection) {
            modelsList.add(new ProcessModel(domainProcess, taskPluginsDiscoverer, requestsRepository));
        }

        return modelsList;
    }



    private void copyUserGroups(Process domainProcess, UserGroupsRepository userGroupsRepository) {
        final Collection<UserGroup> userGroupsCollection = new ArrayList<>();
        final String userGroupsIds = this.getUserGroupsIds();

        if (!StringUtils.isEmpty(userGroupsIds)) {

            for (String userGroupId : userGroupsIds.split(",")) {
                Optional<UserGroup> userGroup = userGroupsRepository.findById(Integer.parseInt(userGroupId));

                if (!userGroup.isPresent()) {
                    this.logger.warn("Could not find a user group to copy with id {}", userGroupId);
                }

                userGroupsCollection.add(userGroup.get());
            }
        }

        domainProcess.setUserGroupsCollection(userGroupsCollection);
    }



    private void copyUsers(Process domainProcess, UsersRepository userRepository) {
        final Collection<User> collUsers = new ArrayList<>();
        final String userIds = this.getUsersIds();

        if (!StringUtils.isEmpty(userIds)) {

            for (String userId : userIds.split(",")) {
                Optional<User> user = userRepository.findById(Integer.parseInt(userId));

                if (!user.isPresent()) {
                    this.logger.warn("Could not find a user to copy with id {}", userId);
                }

                collUsers.add(user.get());
            }
        }

        domainProcess.setUsersCollection(collUsers);
    }

}
