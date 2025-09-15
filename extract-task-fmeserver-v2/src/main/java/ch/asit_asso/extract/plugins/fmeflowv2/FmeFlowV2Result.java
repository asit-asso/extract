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
package ch.asit_asso.extract.plugins.fmeflowv2;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The result of an FME Flow V2 task processing.
 *
 * @author Extract Team
 */
public class FmeFlowV2Result implements ITaskProcessorResult {

    /**
     * The writer to the application logs.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FmeFlowV2Result.class);

    /**
     * The string explaining the result of the task processing to the user.
     */
    private String message;

    /**
     * The data about the request whose processing produced this result.
     */
    @JsonIgnore
    private ITaskProcessorRequest requestData;

    /**
     * Additional information about the task processing result to export as JSON.
     */
    @JsonProperty("resultInfo")
    private final Map<String, String> resultInfo;

    /**
     * Whether the request processing is successful.
     */
    private ITaskProcessorResult.Status status;

    /**
     * The path to the output folder where results are stored.
     */
    private String resultFilePath;

    /**
     * Creates a new result instance.
     */
    public FmeFlowV2Result() {
        this.resultInfo = new HashMap<>();
    }

    @Override
    public final String getMessage() {
        return this.message;
    }

    /**
     * Gets the request data as JSON string.
     * 
     * @return the request data serialized as JSON
     */
    public final String getRequestDataAsString() {
        
        if (this.requestData == null) {
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this.requestData);

        } catch (JsonProcessingException exception) {
            LOGGER.error("Could not serialize the request data to JSON.", exception);
            return null;
        }
    }

    @Override
    public final ITaskProcessorRequest getRequestData() {
        return this.requestData;
    }

    public final Map<String, String> getResultInfo() {
        return this.resultInfo;
    }

    @Override
    public final ITaskProcessorResult.Status getStatus() {
        return this.status;
    }
    
    @Override
    public final String getErrorCode() {
        // Return error code if available
        return this.resultInfo.get("errorCode");
    }

    /**
     * Defines the message describing the result.
     *
     * @param messageString the result message
     */
    public final void setMessage(final String messageString) {
        this.message = messageString;
    }

    /**
     * Defines the data related to the request processed.
     *
     * @param request the processed request
     */
    public final void setRequestData(final ITaskProcessorRequest request) {
        this.requestData = request;
    }

    /**
     * Defines whether the processing of the task succeeded.
     *
     * @param isSuccessful the task processing status
     */
    public final void setStatus(final ITaskProcessorResult.Status isSuccessful) {
        this.status = isSuccessful;
    }
    
    /**
     * Gets the path to the result file.
     *
     * @return the path to the result file
     */
    public String getResultFilePath() {
        return resultFilePath;
    }
    
    /**
     * Sets the path to the result file.
     *
     * @param path the path to the result file
     */
    public void setResultFilePath(String path) {
        this.resultFilePath = path;
        if (path != null) {
            this.resultInfo.put("resultPath", path);
        }
    }
}