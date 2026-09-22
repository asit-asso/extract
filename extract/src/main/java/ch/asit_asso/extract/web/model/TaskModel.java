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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.persistence.TasksRepository;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.services.SecretParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The representation of a task for a view.
 *
 * @author Yves Grasset
 */
public class TaskModel extends PluginItemModel {

    /**
     * The string that indicates that this task is new.
     */
    public static final String TAG_ADDED = "ADDED";

    /**
     * The number that uniquely identifies the task represented by this model.
     */
    private Integer id;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(TaskModel.class);

    /**
     * The string that uniquely identifies the plugin used by this task.
     */
    private String pluginCode;

    /**
     * The user-friendly name of the plugin used by this task.
     */
    private String pluginLabel;

    /**
     * The CSS class for the plugin image.
     */
    private String pluginPictoClass;

    /**
     * The help message of the plugin.
     */
    private String pluginHelpMessage;
    /**
     * The parameter definition provided by the task plugin.
     */
    private String pluginParametersDefinition;

    /**
     * The step number that this task represents in the process.
     */
    private int position;

    /**
     * The string that provides information about the life cycle of this task.
     */
    private String tag;



    /**
     * Obtains the identifier for this task.
     *
     * @return the number that identifies this task in the application
     */
    public final Integer getId() {
        return this.id;
    }



    /**
     * Defines the identifier for this task.
     *
     * @param taskId the number that identifies this task in the application
     */
    public final void setId(final Integer taskId) {
        this.id = taskId;
    }



    /**
     * Obtains the identifier for the plugin used by this task.
     *
     * @return the string that uniquely identifies the task plugin
     */
    public final String getPluginCode() {
        return this.pluginCode;
    }



    /**
     * Defines the identifier for the plugin used by this task.
     *
     * @param pluginId the string that uniquely identifies the task plugin
     */
    public final void setPluginCode(final String pluginId) {
        this.pluginCode = pluginId;
    }



    /**
     * Obtains the user-friendly name of the plugin used by this task.
     *
     * @return the plugin name
     */
    public final String getPluginLabel() {
        return this.pluginLabel;
    }



    /**
     * Defines the user-friendly name of the plugin used by this task.
     *
     * @param pluginName the plugin name
     */
    public final void setPluginLabel(final String pluginName) {
        this.pluginLabel = pluginName;
    }



    /**
     * Obtains the identifier for the icon of the plugin used by this task.
     *
     * @return the plugin icon CSS class
     */
    public final String getPluginPictoClass() {
        return this.pluginPictoClass;
    }



    /**
     * Defines the identifier for the icon of the plugin used by this task.
     *
     * @param pluginIconClass the plugin icon CSS class
     */
    public final void setPluginPictoClass(final String pluginIconClass) {
        this.pluginPictoClass = pluginIconClass;
    }



    /**
     * Obtains the help message of the plugin used by this task.
     *
     * @return the plugin help text
     */
    public final String getPluginHelpMessage() {
        return this.pluginHelpMessage;
    }



    /**
     * Defines the help message of the plugin used by this task.
     *
     * @param pluginHelpText the plugin help text
     */
    public final void setPluginHelpMessage(final String pluginHelpText) {
        this.pluginHelpMessage = pluginHelpText;
    }



    /**
     * Obtains the step of the process that this task represents.
     *
     * @return the position index in the process
     */
    public final int getPosition() {
        return this.position;
    }



    /**
     * Defines the step of the process that this task represents.
     *
     * @param taskPosition the position index in the process
     */
    public final void setPosition(final int taskPosition) {
        this.position = taskPosition;
    }



    /**
     * Obtains information about the life cycle of this task.
     *
     * @return the task life cycle tag string
     */
    public final String getTag() {
        return this.tag;
    }



    /**
     * Defines information about the life cycle of this task.
     *
     * @param lifeCycleTag the task life cycle tag string
     */
    public final void setTag(final String lifeCycleTag) {
        this.tag = lifeCycleTag;
    }



    /**
     * Creates a new instance of this model.
     */
    public TaskModel() {

    }



    /**
     * Creates a new instance of this model.
     *
     * @param taskPlugin the plugin used by the task
     */
    public TaskModel(final ITaskProcessor taskPlugin) {
        this.definePropertiesFromPlugin(taskPlugin);
    }



    /**
     * Creates a new instance of this model.
     */
    public TaskModel(final Task domainTask, final ITaskProcessor taskPlugin,
                     final SecretParameters secretParameters) {
        this(taskPlugin);
        if (secretParameters == null) {
            throw new IllegalArgumentException("The secret parameters service cannot be null.");
        }
        this.definePropertiesFromDataObject(domainTask, secretParameters);
    }



    /**
     * Makes a new data object for this task.
     *
     * @param process the data object for the process that this task is part of
     * @return the created task data object
     */

    public final Task createDomainTask(final Process process, final SecretParameters secretParameters) {
        final Task domainTask = new Task();
        domainTask.setId(this.isNew() ? null : this.getId());
        domainTask.setCode(this.getPluginCode());
        domainTask.setLabel(this.getPluginLabel());

        if (process != null) {
            domainTask.setProcess(process);
        }

        return this.updateDomainTask(domainTask, secretParameters);
    }



    /**
     * Obtains whether this task has yet to be created in the data source.
     *
     * A task that has just been added through the interface carries the identifier that the form gave it to tell
     * it apart from the other blocks of the page. That identifier is not the one of a row of the data source, and
     * it must never be used to fetch one.
     *
     * @return <code>true</code> if this task does not exist in the data source yet
     */
    public final boolean isNew() {
        return TaskModel.TAG_ADDED.equals(this.getTag()) || this.getId() == null;
    }



    /**
     * Reports the current values of this task to its data object.
     *
     * @param domainTask the data object for this task
     * @return the updated task data object
     */

    public final Task updateDomainTask(final Task domainTask, final SecretParameters secretParameters) {
        if (domainTask == null) {
            throw new IllegalArgumentException("The domain task to update cannot be null.");
        }

        if (secretParameters == null) {
            throw new IllegalArgumentException("The secret parameters service cannot be null.");
        }

        if (this.pluginParametersDefinition == null) {
            throw new IllegalStateException("The task plugin parameters definition is not set.");
        }

        domainTask.setPosition(this.getPosition());
        this.logger.debug("Parameters to update : {}",
                          String.join(", ",
                                      Arrays.stream(this.getParameters()).map(PluginItemModelParameter::getName)
                                            .toList()));
        HashMap<String, String> values = secretParameters.encrypt(this.getParametersValues(),
                this.pluginParametersDefinition);
        domainTask.updateParametersValues(values);

        return domainTask;
    }



    /**
     * Reports the current values of this task in the data source.
     *
     * The task to update is looked for among the tasks of the process being saved, and nowhere else. The
     * identifier that the form carries is either the temporary one of a task that has just been added, or the one
     * of a task of another process when the form has been tampered with or restored by the browser. Fetching it
     * from the whole table would overwrite the position and the parameters of a task that this process has no
     * business touching, and leave it attached to its own process (issue #425). An identifier that is none of
     * this process's tasks is therefore refused rather than turned into a new task, so that a caller that skipped
     * {@link ProcessModel#getForeignTaskIds(Process)} cannot write anything unexpected either.
     *
     * @param taskRepository the link between the task data objects and the data source
     * @param domainProcess  the data object of the process that this task is part of
     * @return the saved task data object
     */
    public final Task saveInDataSource(final TasksRepository taskRepository,
            final Process domainProcess, final SecretParameters secretParameters) {
        if (secretParameters == null) {
            throw new IllegalArgumentException("The secret parameters service cannot be null.");
        }
        Task domainTask;

        if (this.isNew()) {
            domainTask = this.createDomainTask(domainProcess, secretParameters);

        } else {
            domainTask = this.findInProcess(domainProcess);

            if (domainTask == null) {
                throw new IllegalArgumentException(String.format(
                        "The task with identifier %s is not a task of the process with identifier %s.", this.getId(),
                        (domainProcess != null) ? domainProcess.getId() : null));
            }

            this.updateDomainTask(domainTask, secretParameters);
        }

        domainTask = taskRepository.save(domainTask);

        if (domainTask != null) {
            this.setId(domainTask.getId());
        }

        return domainTask;
    }


    /**
     * Obtains the data object of this task among those of the process that it is part of.
     *
     * @param domainProcess the data object of the process being saved
     * @return the task data object, or <code>null</code> if it is not one of that process's tasks
     */
    private Task findInProcess(final Process domainProcess) {

        if (domainProcess == null || domainProcess.getTasksCollection() == null) {
            return null;
        }

        for (Task domainTask : domainProcess.getTasksCollection()) {

            if (Objects.equals(domainTask.getId(), this.getId())) {
                return domainTask;
            }
        }

        return null;
    }



    /**
     * Updates this model with the information of the plugin used by this task.
     *
     * @param taskPlugin the plugin used by this task
     */
    private void definePropertiesFromPlugin(final ITaskProcessor taskPlugin) {
        this.setPluginCode(taskPlugin.getCode());
        this.setPluginLabel(taskPlugin.getLabel());
        this.setPluginPictoClass(taskPlugin.getPictoClass());
        this.setPluginHelpMessage(taskPlugin.getHelp());
        this.pluginParametersDefinition = taskPlugin.getParams();
        this.defineParametersFromJson(this.pluginParametersDefinition);
    }



    /**
     * Updates this model with the information of the data object for this task.
     *
     * @param domainTask the data object for this task
     */
    private void definePropertiesFromDataObject(final Task domainTask, final SecretParameters secretParameters) {
        this.setId(domainTask.getId());
        this.setPosition(domainTask.getPosition());
        Map<String, String> values = secretParameters.decrypt(domainTask.getParametersValues(),
                this.getSecretParameterNames());
        this.setParametersValuesFromMap(values);
    }



    private Set<String> getSecretParameterNames() {
        Set<String> secretNames = new HashSet<>();
        for (PluginItemModelParameter parameter : this.getParameters()) {
            if (SecretParameters.isSecretType(parameter.getType())) {
                secretNames.add(parameter.getName());
            }
        }
        return secretNames;
    }
    /**
     * Defines the authoritative parameter definition used when persisting this task.
     *
     * @param parametersDefinition the definition returned by the task plugin
     */
    public final void definePluginParametersDefinition(final String parametersDefinition) {
        if (parametersDefinition == null) {
            throw new IllegalArgumentException("The task plugin parameters definition cannot be null.");
        }
        this.pluginParametersDefinition = parametersDefinition;
    }
    /**
     * Sets the parameters exposed by the plugin used by this task.
     *
     * @param taskPlugin the plugin used by this task
     */
    private void defineParametersFromPlugin(final ITaskProcessor taskPlugin) {
        assert taskPlugin != null : "The task plugin must not be null.";
        this.logger.debug("Defining the task parameters from the plugin.");
        this.defineParametersFromJson(taskPlugin.getParams());
    }

}
