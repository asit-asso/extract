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
package ch.asit_asso.extract.plugins.common;



/**
 * An interface to communicate the result of a processing task.
 *
 * @author Florent Krin
 */
public interface ITaskProcessorResult {

    /**
     * The final state of the task.
     */
    enum Status {

        /**
         * The task failed.
         */
        ERROR,
        /**
         * The task has not been able to run.
         */
        NOT_RUN,
        /**
         * The task requires an intervention by an operator.
         */
        STANDBY,
        /**
         * The task completed successfully.
         */
        SUCCESS
    }



    /**
     * Obtains the final result of the task.
     *
     * @return the task status
     */
    Status getStatus();



    /**
     * Obtains the string that identifies the type of error that occurred.
     *
     * @return the result code string
     */
    String getErrorCode();



    /**
     * Obtains the text that explains the result of the task.
     *
     * @return the task result message string
     */
    String getMessage();



    /**
     * Obtains data about the processed request, possibly modified by the task.
     *
     * @return data about the submitted request
     */
    ITaskProcessorRequest getRequestData();

}
