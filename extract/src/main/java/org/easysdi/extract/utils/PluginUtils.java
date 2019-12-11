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
package org.easysdi.extract.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.easysdi.extract.web.model.PluginItemModelParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Helper methods to deal with the extensions of this application.
 *
 * @author Yves Grasset
 */
public abstract class PluginUtils {

    /**
     * The writer to the application logs.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(PluginUtils.class);



    /**
     * Converts a collection of plugin non-standard parameters from JSON to
     * {@link PluginItemModelParameter}.
     *
     * @param parametersJson a valid JSON string that describes the plugin non-standard parameters
     * @return an array that contains the parameters objects, or <code>null</code> if there was an error
     */
    public static final PluginItemModelParameter[] parseParametersJson(final String parametersJson) {
        PluginUtils.LOGGER.debug("The parameters JSON for the plugin is {}.", parametersJson);
        final List<PluginItemModelParameter> parametersList = new ArrayList<>();
        final ObjectMapper objectMapper = new ObjectMapper();

        try {
            final JsonNode paramsArray = objectMapper.readTree(parametersJson);

            if (!paramsArray.isArray()) {
                PluginUtils.LOGGER.error("The parameters JSON for the connector plugin is invalid.");
                return null;
            }

            final Iterator<JsonNode> elementsIterator = paramsArray.elements();

            while (elementsIterator.hasNext()) {
                JsonNode parameterObject = elementsIterator.next();

                if (!parameterObject.toString().equals("{}")) {
                    PluginItemModelParameter parameter = new PluginItemModelParameter();
                    Boolean required = parameterObject.has("req") ? parameterObject.get("req").asBoolean() : false;

                    parameter.setName(parameterObject.get("code").asText());
                    parameter.setLabel(parameterObject.get("label").asText());
                    parameter.setType(parameterObject.get("type").asText());
                    parameter.setRequired(required);

                    //defaut value false if parameter is boolean
                    if (parameterObject.get("type").asText().equals("boolean")) {
                        parameter.setValue("false");
                    }

                    if (parameterObject.has("maxlength")) {
                        parameter.setMaxLength(parameterObject.get("maxlength").asInt());
                    }

                    if (parameterObject.has("max")) {
                        parameter.setMaxValue(parameterObject.get("max").asInt());
                    }

                    if (parameterObject.has("min")) {
                        parameter.setMinValue(parameterObject.get("min").asInt());
                    }

                    if (parameterObject.has("step")) {
                        parameter.setStep(parameterObject.get("step").asInt());
                    }

                    PluginUtils.LOGGER.debug("Adding parameter {} to the array.", parameter.getName());
                    parametersList.add(parameter);
                }
            }

            int number = parametersList.size();
            PluginUtils.LOGGER.debug("Found {} non-standard parameter{} for the plugin.", number,
                    (number > 1) ? "s" : "");

            return parametersList.toArray(new PluginItemModelParameter[]{});

        } catch (IOException ex) {
            PluginUtils.LOGGER.error("The parameters JSON for the plugin is invalid.", ex);
            return null;
        }

    }

}
