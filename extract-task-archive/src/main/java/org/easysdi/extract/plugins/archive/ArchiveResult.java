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
package org.easysdi.extract.plugins.archive;

import org.easysdi.extract.plugins.common.ITaskProcessorRequest;
import org.easysdi.extract.plugins.common.ITaskProcessorResult;



/**
 * The outcome of an archiving task.
 *
 * @author Florent Krin
 */
public class ArchiveResult implements ITaskProcessorResult {

    /**
     * The string that identifies the type of error that prevented the archiving task from completing,
     * if any.
     */
    private String errorCode;

    /**
     * Additional information about the type of error that prevented the archiving task from completing,
     * if any.
     */
    private String message;

    /**
     * The final state of the archiving task.
     */
    private Status status;

    /**
     * The data item request that required this task as part of its process.
     */
    private ITaskProcessorRequest request;



    @Override
    public final String getErrorCode() {
        return this.errorCode;
    }



    /**
     * Defines the type of error that prevented this archiving task from completing successfully.
     *
     * @param code the string that identifies the type of error, or <code>null</code> if there has not been any
     */
    public final void setErrorCode(final String code) {
        this.errorCode = code;
    }



    @Override
    public final String getMessage() {
        return this.message;
    }



    /**
     * Defines additional information about the error that prevented this archiving task from completing
     * successfully.
     *
     * @param errorMessage the string that explains the error, or <code>null</code> if there has not been any
     */
    public final void setMessage(final String errorMessage) {
        this.message = errorMessage;
    }



    @Override
    public final Status getStatus() {
        return this.status;
    }



    /**
     * Defines the final state of the archiving task.
     *
     * @param taskStatus the status of the task
     */
    public final void setStatus(final Status taskStatus) {
        this.status = taskStatus;
    }



    @Override
    public final ITaskProcessorRequest getRequestData() {
        return this.request;
    }



    /**
     * Defines the data item request that required this archiving task as part of its process.
     * <p>
     * <i>Note that some of its properties may have been updated by the task plugin.</i>
     * </p>
     *
     * @param requestToArchive the request that needed to be archived
     */
    public final void setRequestData(final ITaskProcessorRequest requestToArchive) {
        this.request = requestToArchive;
    }



    @Override
    public final String toString() {

        return String.format("[ status : %s, errorCode : %s, message : %s]", status.name(), errorCode, message);
    }

}
