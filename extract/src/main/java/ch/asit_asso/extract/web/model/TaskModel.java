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
import ch.asit_asso.extract.persistence.TasksRepository;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.domain.Task;
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
     *
     * @param domainTask the data object of the task to represent
     * @param taskPlugin the plugin used by the task
     */
    public TaskModel(final Task domainTask, final ITaskProcessor taskPlugin) {
        this(taskPlugin);
        this.definePropertiesFromDataObject(domainTask);
    }



    /**
     * Makes a new data object for this task.
     *
     * @param process the data object for the process that this task is part of
     * @return the created task data object
     */
    public final Task createDomainTask(final Process process) {
        final Task domainTask = new Task();
        domainTask.setId(this.getId());
        domainTask.setCode(this.getPluginCode());
        domainTask.setLabel(this.getPluginLabel());

        if (process != null) {
            domainTask.setProcess(process);
        }

        return this.updateDomainTask(domainTask);
    }



    /**
     * Reports the current values of this task to its data object.
     *
     * @param domainTask the data object for this task
     * @return the updated task data object
     */
    public final Task updateDomainTask(final Task domainTask) {

        if (domainTask == null) {
            throw new IllegalArgumentException("The domain task to update cannot be null.");
        }

        domainTask.setPosition(this.getPosition());
        domainTask.setParametersValues(this.getParametersValues());

        return domainTask;
    }



    public final Task saveInDataSource(final TasksRepository taskRepository,
            final Process domainProcess) {
        Task domainTask = this.createDomainTask(domainProcess);

        domainTask = this.saveInDataSource(taskRepository, domainProcess, domainTask);

        if (domainTask != null) {
            this.setId(domainTask.getId());
        }

        return domainTask;
    }



    private final Task saveInDataSource(final TasksRepository taskRepository,
                                        final Process domainProcess, final Task domainTask) {

        if (taskRepository == null) {
            throw new IllegalArgumentException("The task repository cannot be null.");
        }

        if (domainProcess == null) {
            throw new IllegalArgumentException("The process domain object cannot be null.");
        }

        if (domainTask == null) {
            throw new IllegalArgumentException("The task domain object cannot be null.");
        }

        this.updateDomainTask(domainTask);

        return taskRepository.save(domainTask);
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
        this.defineParametersFromPlugin(taskPlugin);
    }



    /**
     * Updates this model with the information of the data object for this task.
     *
     * @param domainTask the data object for this task
     */
    private void definePropertiesFromDataObject(final Task domainTask) {
        this.setId(domainTask.getId());
        this.setPosition(domainTask.getPosition());
        this.setParametersValuesFromMap(domainTask.getParametersValues());
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
