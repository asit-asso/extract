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
package ch.asit_asso.extract.plugins.fmeserverv2;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The result of an FME Server V2 task processing with enhanced error tracking and validation.
 *
 * @author Extract Team
 */
public class FmeServerV2Result implements ITaskProcessorResult {

    /**
     * The writer to the application logs.
     */
    private static final Logger logger = LoggerFactory.getLogger(FmeServerV2Result.class);

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
     * Timestamp when the result was created.
     */
    private final Instant createdAt;

    /**
     * Timestamp when the result was last updated.
     */
    private Instant updatedAt;

    /**
     * Error code for detailed error tracking.
     */
    private String errorCode;

    /**
     * Detailed error description for debugging.
     */
    private String errorDetails;

    /**
     * Processing duration in milliseconds.
     */
    private Long processingDuration;

    /**
     * Creates a new result instance with timestamp tracking.
     */
    public FmeServerV2Result() {
        this.resultInfo = new HashMap<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.status = Status.ERROR; // Default to error for safety
    }

    @Override
    public final String getMessage() {
        return this.message;
    }

    /**
     * Gets the request data as JSON string with enhanced error handling.
     *
     * @return the request data serialized as JSON, or null if unavailable
     */
    public final String getRequestDataAsString() {
        if (this.requestData == null) {
            logger.debug("Request data is null");
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(this.requestData);
            logger.trace("Request data serialized successfully");
            return json;

        } catch (JsonProcessingException exception) {
            logger.error("Failed to serialize request data to JSON", exception);
            return null;
        } catch (Exception exception) {
            logger.error("Unexpected error serializing request data", exception);
            return null;
        }
    }

    @Override
    public final ITaskProcessorRequest getRequestData() {
        return this.requestData;
    }

    /**
     * Gets the additional result information.
     *
     * @return map containing result information
     */
    public final Map<String, String> getResultInfo() {
        return new HashMap<>(this.resultInfo); // Return defensive copy
    }

    @Override
    public final ITaskProcessorResult.Status getStatus() {
        return this.status;
    }

    @Override
    public final String getErrorCode() {
        return this.errorCode;
    }

    /**
     * Gets detailed error information for debugging.
     *
     * @return detailed error description or null
     */
    public final String getErrorDetails() {
        return this.errorDetails;
    }

    /**
     * Defines the message describing the result with validation.
     *
     * @param messageString the result message
     */
    public final void setMessage(final String messageString) {
        if (messageString != null && messageString.trim().isEmpty()) {
            logger.warn("Setting empty message string");
        }
        this.message = messageString;
        this.updatedAt = Instant.now();

        if (messageString != null) {
            this.resultInfo.put("message", messageString);
        }
    }

    /**
     * Defines the data related to the request processed.
     *
     * @param request the processed request
     */
    public final void setRequestData(final ITaskProcessorRequest request) {
        Objects.requireNonNull(request, "Request data cannot be null");
        this.requestData = request;
        this.updatedAt = Instant.now();

        // Store key request information
        this.resultInfo.put("requestId", String.valueOf(request.getId()));
        if (request.getOrderGuid() != null) {
            this.resultInfo.put("orderGuid", request.getOrderGuid());
        }
        if (request.getProductGuid() != null) {
            this.resultInfo.put("productGuid", request.getProductGuid());
        }
    }

    /**
     * Defines whether the processing of the task succeeded.
     *
     * @param taskStatus the task processing status
     */
    public final void setStatus(final ITaskProcessorResult.Status taskStatus) {
        Objects.requireNonNull(taskStatus, "Status cannot be null");
        this.status = taskStatus;
        this.updatedAt = Instant.now();
        this.resultInfo.put("status", taskStatus.name());

        logger.debug("Status set to: {}", taskStatus);
    }

    /**
     * Sets the error code for detailed error tracking.
     *
     * @param code the error code
     */
    public final void setErrorCode(final String code) {
        this.errorCode = code;
        this.updatedAt = Instant.now();

        if (code != null) {
            this.resultInfo.put("errorCode", code);
        }
    }

    /**
     * Sets detailed error information for debugging.
     *
     * @param details the error details
     */
    public final void setErrorDetails(final String details) {
        this.errorDetails = details;
        this.updatedAt = Instant.now();

        if (details != null) {
            // Truncate very long error messages for storage
            String truncated = details.length() > 1000 ?
                details.substring(0, 997) + "..." : details;
            this.resultInfo.put("errorDetails", truncated);
        }
    }

    /**
     * Sets both error code and details at once.
     *
     * @param code the error code
     * @param details the error details
     */
    public final void setError(final String code, final String details) {
        setErrorCode(code);
        setErrorDetails(details);
        setStatus(Status.ERROR);
    }

    /**
     * Gets the path to the result file.
     *
     * @return the path to the result file
     */
    public final String getResultFilePath() {
        return resultFilePath;
    }

    /**
     * Sets the path to the result file with validation.
     *
     * @param path the path to the result file
     */
    public final void setResultFilePath(final String path) {
        if (path != null && path.trim().isEmpty()) {
            logger.warn("Setting empty result file path");
        }

        this.resultFilePath = path;
        this.updatedAt = Instant.now();

        if (path != null) {
            this.resultInfo.put("resultPath", path);
        }
    }

    /**
     * Gets the timestamp when this result was created.
     *
     * @return creation timestamp
     */
    public final Instant getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Gets the timestamp when this result was last updated.
     *
     * @return last update timestamp
     */
    public final Instant getUpdatedAt() {
        return this.updatedAt;
    }

    /**
     * Sets the processing duration.
     *
     * @param startTime the start time in milliseconds
     */
    public final void setProcessingDuration(final long startTime) {
        this.processingDuration = System.currentTimeMillis() - startTime;
        this.resultInfo.put("processingDurationMs", String.valueOf(this.processingDuration));
        this.updatedAt = Instant.now();
    }

    /**
     * Gets the processing duration in milliseconds.
     *
     * @return processing duration or null if not set
     */
    public final Long getProcessingDuration() {
        return this.processingDuration;
    }

    /**
     * Adds custom information to the result.
     *
     * @param key the information key
     * @param value the information value
     */
    public final void addResultInfo(final String key, final String value) {
        if (key != null && value != null) {
            this.resultInfo.put(key, value);
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Checks if the result represents a successful execution.
     *
     * @return true if successful, false otherwise
     */
    public final boolean isSuccess() {
        return Status.SUCCESS.equals(this.status);
    }

    /**
     * Checks if the result represents an error.
     *
     * @return true if error, false otherwise
     */
    public final boolean isError() {
        return Status.ERROR.equals(this.status);
    }

    @Override
    public String toString() {
        return String.format("FmeServerV2Result{status=%s, message='%s', errorCode='%s', resultPath='%s'}",
                            status, message, errorCode, resultFilePath);
    }
}