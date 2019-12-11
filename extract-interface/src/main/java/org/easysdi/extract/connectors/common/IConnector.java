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
package org.easysdi.extract.connectors.common;

import java.util.Map;



/**
 * Common interface for all the connector plugins.
 *
 * @author Yves Grasset
 */
public interface IConnector {

    /**
     * Create a new connector instance.
     *
     * @param language the locale of the language used to display messages to the user in the application
     * @param inputs   the parameters for this task
     * @return the new connector instance
     */
    IConnector newInstance(String language, Map<String, String> inputs);



    /**
     * Create a new connector instance.
     *
     * @param language the locale of the language used to display messages to the user in the application
     * @return the new connector instance
     */
    IConnector newInstance(String language);



    /**
     * Returns the string that uniquely identify this connector plugin.
     *
     * @return the plugin code
     */
    String getCode();



    /**
     * Returns a text explaining what this connector plugin does.
     *
     * @return the description text
     */
    String getDescription();



    /**
     * Returns a text explaining how to use this connector plugin.
     *
     * @return the help text
     */
    String getHelp();



    /**
     * Gets the user-friendly name of this connector plugin.
     *
     * @return the label
     */
    String getLabel();



    /**
     * Gets the path of the icon for this connector plugin.
     *
     * @return the path of the image file
     */
    String getPicto();



    /**
     * Gets the description of the parameters that must be provided to use this connector.
     *
     * @return a JSON string containing the definition of the parameters
     */
    String getParams();



    /**
     * Fetches the commands based on the provided parameters.
     *
     * @return the commands
     */
    IConnectorImportResult importCommands();



    /**
     * Sends the result obtained by processing a command to the server that produced it.
     *
     * @param request the result to export
     * @return the object describing the export result (code, message, error details)
     */
    IExportResult exportResult(IExportRequest request);

}
