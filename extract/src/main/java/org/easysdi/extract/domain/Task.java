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
package org.easysdi.extract.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.easysdi.extract.domain.converters.JsonToParametersValuesConverter;



/**
 * An action to carry as a part of the process to generate the data for a data item request.
 *
 * @author Florent Krin
 */
@Entity
@Table(name = "Tasks", indexes = {
    @Index(columnList = "position", name = "IDX_TASK_POSITION"),
    @Index(columnList = "id_process", name = "IDX_TASK_PROCESS")
})
@XmlRootElement
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The number that uniquely identifies this task in the application.
     */
    @Id
    @Basic(optional = false)
    @GeneratedValue
    @NotNull
    @Column(name = "id_task")
    private Integer id;

    /**
     * The string that uniquely identifies the plugin to call to carry this task.
     */
    @Size(max = 50)
    @Column(name = "task_code")
    private String code;

    /**
     * The string that describes this task.
     */
    @Size(max = 255)
    @Column(name = "task_label")
    private String label;

    /**
     * The settings to pass to the plugin that will carry this task.
     */
    @Size(max = 4000)
    @Column(name = "task_params", length = 4000)
    @Convert(converter = JsonToParametersValuesConverter.class)
    private HashMap<String, String> parametersValues;

    /**
     * The number that tells where this task must be executed in relation to the other tasks of the parent
     * process.
     */
    @Column(name = "position")
    private Integer position;

    /**
     * The ensemble of tasks that this task is a part of.
     */
    @JoinColumn(name = "id_process", referencedColumnName = "id_process",
            foreignKey = @ForeignKey(name = "FK_TASK_PROCESS")
    )
    @ManyToOne
    private Process process;



    /**
     * Creates a new task instance.
     */
    public Task() {
    }



    /**
     * Creates a new task instance.
     *
     * @param identifier the number that uniquely identifies this task in the application
     */
    public Task(final Integer identifier) {
        this.id = identifier;
    }



    /**
     * Obtains the number that uniquely identifies this task in the application.
     *
     * @return the task identifier
     */
    public Integer getId() {
        return this.id;
    }



    /**
     * Defines the number that uniquely identifies this task in the application.
     *
     * @param identifier the task identifier
     */
    public void setId(final Integer identifier) {
        this.id = identifier;
    }



    /**
     * Obtains the string that uniquely identifies the plugin to call to execute this task.
     *
     * @return the task plugin identifier
     */
    public String getCode() {
        return this.code;
    }



    /**
     * Defines the string that uniquely identifies the plugin to call to execute this task.
     *
     * @param pluginCode the task plugin identifier
     */
    public void setCode(final String pluginCode) {
        this.code = pluginCode;
    }



    /**
     * Obtains the description of this task.
     *
     * @return the string that describes this task
     */
    public String getLabel() {
        return this.label;
    }



    /**
     * Defines the description of this task.
     *
     * @param taskLabel a string that describes this task
     */
    public void setLabel(final String taskLabel) {
        this.label = taskLabel;
    }



    /**
     * Obtains the settings to pass to the plugin that will execute this task.
     *
     * @return a map that contains the settings keys and their values
     */
    public HashMap<String, String> getParametersValues() {
        return this.parametersValues;
    }



    /**
     * Defines the settings to pass to the plugin that will execute this task. The supported settings are
     * defined by the plugin itself.
     *
     * @param parametersMap a map that contains the settings keys and their values
     */
    public void setParametersValues(final HashMap<String, String> parametersMap) {

        if (parametersMap == null) {
            throw new IllegalArgumentException("The parameters map cannot be null.");
        }

        if (parametersMap.isEmpty()) {
            return;
        }

        if (this.parametersValues == null) {
            this.parametersValues = new HashMap<>();
        }

        for (Map.Entry<String, String> parameterData : parametersMap.entrySet()) {
            this.parametersValues.put(parameterData.getKey(), parameterData.getValue());
        }

    }



    /**
     * Obtains where this task must be executed in relation to the others tasks of the parent process.
     *
     * @return the position index of this task
     */
    public Integer getPosition() {
        return this.position;
    }



    /**
     * Defines where this task must be executed in relation to the others tasks of the parent process.
     *
     * @param positionIndex the position index of this task in the process
     */
    public void setPosition(final Integer positionIndex) {
        this.position = positionIndex;
    }



    /**
     * Obtains the ensemble of tasks that this task is a part of.
     *
     * @return the parent process
     */
    public Process getProcess() {
        return this.process;
    }



    /**
     * Defines the ensemble of tasks that this task is a part of.
     *
     * @param parentProcess the process that contains this task
     */
    public void setProcess(final Process parentProcess) {
        this.process = parentProcess;
    }



    @Override
    public final int hashCode() {
        int hash = 0;
        hash += this.id.hashCode();

        return hash;
    }



    @Override
    public final boolean equals(final Object object) {

        if (object == null || !(object instanceof Task)) {
            return false;
        }
        Task other = (Task) object;

        return this.id.equals(other.id);
    }



    @Override
    public final String toString() {
        return String.format("org.easysdi.extract.Task[ idTask=%d ]", this.id);
    }

}
