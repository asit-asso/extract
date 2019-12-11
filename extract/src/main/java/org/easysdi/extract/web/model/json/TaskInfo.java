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
package org.easysdi.extract.web.model.json;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.Calendar;



/**
 * Information about a task that must be exported as JSON.
 *
 * @author Yves Grasset
 */
public class TaskInfo {

    /**
     * Information about when the status of this task was last updated.
     */
    @JsonView(PublicField.class)
    private final DateInfo taskDateInfo;

    /**
     * The name of this task.
     */
    @JsonView(PublicField.class)
    private final String taskLabel;



    /**
     * Obtains the information about the last status update for this task.
     *
     * @return the last status update information
     */
    public final DateInfo getTaskDateInfo() {
        return this.taskDateInfo;
    }



    /**
     * Obtains the name of this task.
     *
     * @return the task label
     */
    public final String getTaskLabel() {
        return this.taskLabel;
    }



    /**
     * Creates a new representation of a task.
     *
     * @param dateText a string representation of when the task status was last updated.
     * @param taskDate when the task status was last updated
     * @param label    the name of the task
     */
    public TaskInfo(final String dateText, final Calendar taskDate, final String label) {
        this.taskDateInfo = new DateInfo(dateText, taskDate);
        this.taskLabel = label;
    }

}
