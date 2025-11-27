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
package ch.asit_asso.extract.email;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;
import ch.asit_asso.extract.domain.Request;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;

/**
 * Utility class to build Thymeleaf context models with all request parameters for email templates.
 * This class centralizes the logic for adding request-related variables to email templates,
 * implementing issue #323 requirements.
 *
 * @author Extract Team
 */
public class RequestModelBuilder {

    /**
     * The writer to the application logs.
     */
    private static final Logger logger = LoggerFactory.getLogger(RequestModelBuilder.class);

    /**
     * Jackson ObjectMapper for parsing JSON parameters.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private RequestModelBuilder() {
        // Utility class, no instantiation needed
    }

    /**
     * Adds all standard request variables to the given Thymeleaf context.
     * This includes all fields required by issue #323:
     * - orderLabel, productLabel, startDate, organism, client, tiers, surface
     * - parameters (as both raw JSON and parsed map)
     *
     * @param context The Thymeleaf context to populate
     * @param request The request object containing the data
     */
    public static void addRequestVariables(Context context, Request request) {
        if (context == null || request == null) {
            logger.warn("Cannot add request variables: context or request is null");
            return;
        }

        // Basic request fields
        context.setVariable("orderLabel", request.getOrderLabel() != null ? request.getOrderLabel() : "");
        context.setVariable("productLabel", request.getProductLabel() != null ? request.getProductLabel() : "");
        
        // Client and organization information
        context.setVariable("client", request.getClient() != null ? request.getClient() : "");
        context.setVariable("clientName", request.getClient() != null ? request.getClient() : ""); // Alias for compatibility
        context.setVariable("clientGuid", request.getClientGuid() != null ? request.getClientGuid() : "");
        context.setVariable("organism", request.getOrganism() != null ? request.getOrganism() : "");
        context.setVariable("organisationName", request.getOrganism() != null ? request.getOrganism() : ""); // Alias for compatibility
        context.setVariable("organismGuid", request.getOrganismGuid() != null ? request.getOrganismGuid() : "");
        
        // Tiers information
        context.setVariable("tiers", request.getTiers() != null ? request.getTiers() : "");
        context.setVariable("tiersDetails", request.getTiersDetails() != null ? request.getTiersDetails() : "");
        context.setVariable("tiersGuid", request.getTiersGuid() != null ? request.getTiersGuid() : "");
        
        // Geographic and temporal information
        context.setVariable("perimeter", request.getPerimeter() != null ? request.getPerimeter() : "");
        context.setVariable("surface", request.getSurface() != null ? Double.toString(request.getSurface()) : "");
        
        // Date fields
        if (request.getStartDate() != null) {
            context.setVariable("startDate", DateFormat.getDateTimeInstance().format(request.getStartDate().getTime()));
            context.setVariable("startDateISO", request.getStartDate().getTime().toInstant().toString());
        } else {
            context.setVariable("startDate", "");
            context.setVariable("startDateISO", "");
        }
        
        if (request.getEndDate() != null) {
            context.setVariable("endDate", DateFormat.getDateTimeInstance().format(request.getEndDate().getTime()));
            context.setVariable("endDateISO", request.getEndDate().getTime().toInstant().toString());
        } else {
            context.setVariable("endDate", "");
            context.setVariable("endDateISO", "");
        }
        
        // Status and remarks
        context.setVariable("status", request.getStatus() != null ? request.getStatus().name() : "");
        context.setVariable("remark", request.getRemark() != null ? request.getRemark() : "");
        context.setVariable("clientRemark", request.getRemark() != null ? request.getRemark() : ""); // Alias for compatibility
        context.setVariable("rejected", request.isRejected());
        
        // Handle dynamic parameters
        addDynamicParameters(context, request.getParameters());
        
        logger.debug("Added {} request variables to email context", context.getVariableNames().size());
    }

    /**
     * A HashMap that returns null for non-existent keys (standard behavior).
     * When accessed via bracket notation like ${parametersMap['key']}, Thymeleaf
     * handles null values gracefully without throwing errors.
     *
     * This addresses the issue raised in #323 where missing dynamic parameters
     * should not cause email sending to fail.
     *
     * Note: This map must be accessed using bracket notation ${parametersMap['key']}
     * and NOT dot notation ${parametersMap.key} which would fail with SpEL.
     */
    private static class SafeParametersMap extends HashMap<String, Object> {
        // Inherits standard HashMap behavior - returns null for missing keys
        // No override needed, just a marker class for documentation purposes
    }

    /**
     * Parses and adds dynamic parameters to the context.
     * Parameters are available in multiple ways:
     * 1. As raw JSON string under "parametersJson"
     * 2. As parsed map under "parameters"
     * 3. As individual variables with lowercase keys (e.g., parameters.format for FORMAT key)
     *
     * Non-existent keys in the parameters map return empty strings instead of causing errors,
     * making email templates more resilient to missing optional parameters.
     *
     * @param context The Thymeleaf context to populate
     * @param parametersJson The JSON string containing parameters
     */
    private static void addDynamicParameters(Context context, String parametersJson) {
        // Always set the raw parameters string
        context.setVariable("parametersJson", parametersJson != null ? parametersJson : "{}");

        // Initialize both maps as SafeParametersMap to ensure missing keys return empty string
        // This prevents template errors when accessing ${parametersMap['nonExistentKey']}
        Map<String, Object> parametersMap = new SafeParametersMap();
        Map<String, Object> parametersObject = new SafeParametersMap();

        if (parametersJson != null && !parametersJson.trim().isEmpty()) {
            try {
                // Parse JSON parameters into a temporary map
                Map<String, Object> tempMap = objectMapper.readValue(parametersJson,
                    new TypeReference<Map<String, Object>>() {});
                logger.debug("Successfully parsed {} dynamic parameters", tempMap.size());

                // Copy to SafeParametersMap to guarantee safe access
                parametersMap.putAll(tempMap);

                // Add each parameter with multiple access patterns
                for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
                    String originalKey = entry.getKey();
                    Object value = entry.getValue();
                    String valueStr = value != null ? value.toString() : "";
                    
                    // 1. Add with param_ prefix (keeping original case)
                    String paramKey = "param_" + originalKey;
                    context.setVariable(paramKey, valueStr);
                    logger.trace("Added dynamic parameter: {} = {}", paramKey, valueStr);
                    
                    // 2. Add to parameters object with lowercase key for dot notation access
                    String lowerKey = originalKey.toLowerCase();
                    parametersObject.put(lowerKey, valueStr);
                    
                    // 3. Also add with param_ prefix and lowercase for consistency
                    String paramLowerKey = "param_" + lowerKey;
                    context.setVariable(paramLowerKey, valueStr);
                    logger.trace("Added dynamic parameter (lowercase): {} = {}", paramLowerKey, valueStr);
                }
                
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse request parameters JSON: {}", e.getMessage());
                logger.debug("Invalid JSON content: {}", parametersJson);
            }
        }
        
        // Add the original parameters map (preserves original case)
        context.setVariable("parametersMap", parametersMap);
        
        // Add the parameters object with lowercase keys for dot notation access
        context.setVariable("parameters", parametersObject);
    }

    /**
     * Creates a new Thymeleaf context with all request variables populated.
     * This is a convenience method for creating a new context with request data.
     *
     * @param request The request object containing the data
     * @return A new Context object with all request variables set
     */
    public static Context createContextWithRequest(Request request) {
        Context context = new Context();
        addRequestVariables(context, request);
        return context;
    }
}