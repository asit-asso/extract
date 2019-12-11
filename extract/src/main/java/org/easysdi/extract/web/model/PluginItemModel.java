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
package org.easysdi.extract.web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.easysdi.extract.utils.PluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The base for a model that represents an item using a plugin to carry its actions.
 *
 * @author Yves Grasset
 */
public abstract class PluginItemModel {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(PluginItemModel.class);

    /**
     * The settings that the plugin exposes.
     */
    private final List<PluginItemModelParameter> parameters = new ArrayList<>();



    /**
     * Obtains the non-standard parameters to be passed to the task plugin.
     *
     * @return an array that contains the parameters
     */
    public final PluginItemModelParameter[] getParameters() {
        return this.parameters.toArray(new PluginItemModelParameter[]{});
    }



    /**
     * Gets a non-standard parameter of this connector instance.
     *
     * @param index The position of the desired parameter
     * @return the parameter
     */
    public final PluginItemModelParameter getParameter(final int index) {

        if (index < 0 || index >= this.parameters.size()) {
            throw new IllegalArgumentException("The parameter index is out of bounds.");
        }

        return this.parameters.get(index);
    }



    /**
     * Obtains the values for the non-standard parameters to pass to the plugin.
     *
     * @return a map with the parameters names as keys and the values as, well, values
     */
    protected final HashMap<String, String> getParametersValues() {
        final HashMap<String, String> parametersValuesMap = new HashMap<>();

        for (PluginItemModelParameter parameter : this.getParameters()) {

            if (!parameter.isDefined()) {
                continue;
            }

            parametersValuesMap.put(parameter.getName(), parameter.getValue().toString());
        }

        return parametersValuesMap;
    }



    /**
     * Defines the non-standard parameters to be passed to the task plugin.
     *
     * @param parametersArray an array that contains the parameters
     */
    public final void setParameters(final PluginItemModelParameter[] parametersArray) {

        if (parametersArray == null) {
            this.logger.debug("The parameters array is null.");
            return;
        }

        this.logger.debug("Defining {} parameters.", parametersArray.length);

        for (PluginItemModelParameter param : parametersArray) {
            this.logger.debug("Defining parameter {} => {}.", param.getName(), param.getValue());
        }
        this.parameters.clear();
        this.parameters.addAll(Arrays.asList(parametersArray));
    }



    /**
     * Defines the values of the non-standard parameters.
     *
     * @param parametersValuesMap a map containing the parameters values
     */
    protected final void setParametersValuesFromMap(final Map<String, String> parametersValuesMap) {

        for (PluginItemModelParameter parameter : this.getParameters()) {
            parameter.updateValue(parametersValuesMap.get(parameter.getName()));
        }

        this.logger.debug("The non-standard parameters values have been updated from a map.");
    }



    /**
     * Adds a non-standard parameter to this task instance.
     *
     * @param parameter the parameter to add
     * @throws IllegalArgumentException A parameter with the same name is already defined
     */
    public final void addParameter(final PluginItemModelParameter parameter) {

        if (parameter == null) {
            return;
        }

        if (this.getParameterByName(parameter.getName()) != null) {
            throw new IllegalArgumentException("A parameter with this name already exists.");
        }

        this.parameters.add(parameter);
    }



    /**
     * Adds a collection of non-standard parameter to this task instance.
     *
     * @param parametersArray an array that contains the parameters to add
     * @throws IllegalArgumentException A parameter with the same name is already defined
     */
    public final void addAllParameters(final PluginItemModelParameter[] parametersArray) {

        if (parametersArray == null) {
            return;
        }

        for (PluginItemModelParameter parameter : parametersArray) {
            this.addParameter(parameter);
        }
    }



    /**
     * Gets a non-standard parameter of this connector instance.
     *
     * @param name the name of the desired parameter
     * @return the connector or <code>null</code> if none was found
     */
    public final PluginItemModelParameter getParameterByName(final String name) {

        if (name == null) {
            throw new IllegalArgumentException("The parameter name cannot be null.");
        }

        for (PluginItemModelParameter parameter : this.parameters) {

            if (name.equals(parameter.getName())) {
                return parameter;
            }
        }

        return null;
    }



    /**
     * Sets the settings exposed by the plugin.
     *
     * @param parametersJson the definition of the parameters in JSON format
     */
    protected final void defineParametersFromJson(final String parametersJson) {
        this.logger.debug("Defining the task parameters from a JSON string.");

        this.logger.debug("The parameter JSON from the plugin is {}", parametersJson);
        final PluginItemModelParameter[] parametersArray = PluginUtils.parseParametersJson(parametersJson);

        if (parametersArray == null) {
            this.logger.warn("An error occurred when the plugin parameters string was parsed,");
            return;
        }

        this.parameters.clear();
        this.addAllParameters(parametersArray);
    }

}
