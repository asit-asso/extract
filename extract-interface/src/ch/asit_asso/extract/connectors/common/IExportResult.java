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
package ch.asit_asso.extract.connectors.common;



/**
 * An interface to communicate the result of exporting of a request to its originating server.
 *
 * @author Florent Krin
 */
public interface IExportResult {

    /**
     * Whether the request could be exported.
     *
     * @return <code>true</code> if the export succeeded
     */
    boolean isSuccess();



    /**
     * Obtains the string that identifies the type of result returned by the export.
     *
     * @return the result code string
     */
    String getResultCode();



    /**
     * Obtains the string that explains the result returned by the export.
     *
     * @return the message string
     */
    String getResultMessage();



    /**
     * Obtains additional information about a possible export error.
     *
     * @return a string with additional information about the error, or <code>null</code> if none is available
     */
    String getErrorDetails();

}
