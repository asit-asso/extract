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
package ch.asit_asso.extract.connectors.sample;

import ch.asit_asso.extract.connectors.common.IExportResult;



/**
 * Information about whether sending the result of a request to its originating server succeeded.
 *
 * @author Florent Krin
 */
public class ExportResult implements IExportResult {

    /**
     * Whether this export succeeded.
     */
    private boolean success;

    /**
     * The string that identifies the type of export outcome that this attempt produced.
     */
    private String resultCode;

    /**
     * The description of the export outcome that this attempt produced.
     */
    private String resultMessage;

    /**
     * Additional information about the error that prevented the export, if any.
     */
    private String errorDetails;



    @Override
    public final boolean isSuccess() {
        return this.success;
    }



    /**
     * Defines whether this export attempt completed successfully.
     *
     * @param isSuccess <code>true</code> if the export result could be sent back to the server
     */
    public final void setSuccess(final boolean isSuccess) {
        this.success = isSuccess;
    }



    @Override
    public final String getResultCode() {
        return this.resultCode;
    }



    /**
     * Defines the identifier of the outcome type that this export produced.
     *
     * @param code the result code string
     */
    public final void setResultCode(final String code) {
        this.resultCode = code;
    }



    @Override
    public final String getResultMessage() {
        return this.resultMessage;
    }



    /**
     * Defines the description of the outcome that this export produced.
     *
     * @param message the result message string
     */
    public final void setResultMessage(final String message) {
        this.resultMessage = message;
    }



    @Override
    public final String getErrorDetails() {
        return this.errorDetails;
    }



    /**
     * Defines additional information about the error that prevented this export attempt.
     *
     * @param details a string with details about the error, or <code>null</code> if no error occurred
     */
    public final void setErrorDetails(final String details) {
        this.errorDetails = details;
    }



    @Override
    public final String toString() {

        return String.format("[ success : %s, resultCode : %s, resultMessage : %s, errorDetails : %s]",
                success, resultCode, resultMessage, errorDetails);

    }

}
