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
package org.easysdi.extract.plugins.common;

import java.util.Map;



/**
 * Common interface for all the task plugins.
 *
 * @author Yves Grasset
 */
public interface ITaskProcessor {

    /**
     * Returns a new task processor instance with the provided settings.
     *
     * @param language the locale code of the language to display the messages in
     * @return the new task processor instance
     */
    ITaskProcessor newInstance(String language);



    /**
     * Returns a new task processor instance with the provided settings.
     *
     * @param language the locale code of the language to display the messages in
     * @param inputs   the parameters for this task
     * @return the new task processor instance
     */
    ITaskProcessor newInstance(String language, Map<String, String> inputs);



    /**
     * Gets the string that uniquely identifies this task plugin.
     *
     * @return the plugin code
     */
    String getCode();



    /**
     * Gets a description of what this task does.
     *
     * @return the description text
     */
    String getDescription();



    /**
     * Gets a text explaining how to use this task.
     *
     * @return the help text for this task.
     */
    String getHelp();



    /**
     * Gets the user-friendly name of this task.
     *
     * @return the label
     */
    String getLabel();



    /**
     * Gets the description of the parameters that must be provided for the execution of this task.
     *
     * @return a JSON string with the definition of the parameters
     */
    String getParams();



    /**
     * Gets the path of the icon for this task plugin.
     *
     * @return the path of the image file
     */
    String getPictoClass();



    /**
     * Executes the task.
     *
     * @param request       the request that requires the execution of this task
     * @param emailSettings the parameters required to send an e-mail notification
     * @return 0 if the task succeeded or an error code otherwise
     */
    ITaskProcessorResult execute(ITaskProcessorRequest request, IEmailSettings emailSettings);

}
