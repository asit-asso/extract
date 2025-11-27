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
package ch.asit_asso.extract.plugins.python;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;

/**
 * The outcome of a Python script execution task.
 * 
 * @author Extract Team
 */
public class PythonResult implements ITaskProcessorResult {

    /**
     * Whether the Python script execution was successful.
     */
    private boolean success;
    
    /**
     * The error message if the execution failed.
     */
    private String errorMessage;
    
    /**
     * The success message if the execution succeeded.
     */
    private String message;
    
    /**
     * The path to the result files.
     */
    private String resultFilePath;
    
    /**
     * The data item request that required this task as part of its process.
     */
    private ITaskProcessorRequest request;

    /**
     * Creates a new Python result.
     */
    public PythonResult() {
        this.success = false;
    }

    /**
     * Gets whether the Python script execution was successful.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets whether the Python script execution was successful.
     * 
     * @param success true if successful, false otherwise
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets the error message.
     * 
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     * 
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the success message.
     * 
     * @return the success message
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Sets the success message.
     * 
     * @param message the success message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the path to the result files.
     * 
     * @return the result file path
     */
    public String getResultFilePath() {
        return resultFilePath;
    }

    /**
     * Sets the path to the result files.
     * 
     * @param resultFilePath the result file path
     */
    public void setResultFilePath(String resultFilePath) {
        this.resultFilePath = resultFilePath;
    }

    @Override
    public String getErrorCode() {
        return success ? null : "PYTHON_EXECUTION_ERROR";
    }

    @Override
    public Status getStatus() {
        return success ? Status.SUCCESS : Status.ERROR;
    }

    @Override
    public ITaskProcessorRequest getRequestData() {
        return request;
    }

    /**
     * Defines the data item request that required this Python execution task as part of its process.
     *
     * @param requestToProcess the request that needs Python script execution
     */
    public void setRequestData(ITaskProcessorRequest requestToProcess) {
        this.request = requestToProcess;
    }

    @Override
    public String toString() {
        return String.format("PythonResult[ success: %s, message: %s, errorMessage: %s, resultPath: %s]", 
                            success, message, errorMessage, resultFilePath);
    }
}