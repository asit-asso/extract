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
package ch.asit_asso.extract.orchestrator.runners;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.email.Email;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.email.LocaleUtils;
import ch.asit_asso.extract.email.TaskFailedEmail;
import ch.asit_asso.extract.email.TaskStandbyEmail;
import ch.asit_asso.extract.exceptions.SystemUserNotFoundException;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import ch.asit_asso.extract.persistence.RequestHistoryRepository;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorDiscovererWrapper;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;



/**
 * A job that executes the next task in the process of a request.
 *
 * @author Yves Grasset
 */
public class RequestTaskRunner implements Runnable {

    /**
     * The number of characters that the remark field can contain.
     */
    private final static int MAXIMUM_REMARK_LENGTH = 4000;

    /**
     * An ensemble of objects that link the data objects to the database.
     */
    private final ApplicationRepositories applicationRepositories;

    /**
     * A collection of objects that asked to be notified when this task is done.
     */
    private final Set<TaskCompleteListener<Integer>> completionListeners;

    /**
     * The objects required to create and send an e-mail message.
     */
    private final EmailSettings emailSettings;

    /**
     * The language used by the application to display messages to the user.
     */
    private final String language;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(RequestTaskRunner.class);

    /**
     * The request to process.
     */
    private Request request;

    /**
     * The item that traces the execution of the current task.
     */
    private RequestHistoryRecord taskHistoryRecord;

    /**
     * The access to the available task plugins.
     */
    private final TaskProcessorDiscovererWrapper taskPluginsDiscoverer;



    /**
     * Creates a new instance of this request task executer.
     *
     * @param requestToProcess    the request for which a task has to be executed
     * @param repositories        an ensemble of objects that link the data objects with the database
     * @param pluginsDiscoverer   the access to the available task plugins
     * @param smtpSettings        the objects required to create and send an e-mail message
     * @param applicationLanguage the locale code of the language used by the application to display messages to the
     *                            user
     */
    public RequestTaskRunner(final Request requestToProcess, final ApplicationRepositories repositories,
            final TaskProcessorDiscovererWrapper pluginsDiscoverer, final EmailSettings smtpSettings,
            final String applicationLanguage) {

        if (requestToProcess == null) {
            throw new IllegalArgumentException("The request to process cannot be null.");
        }

        if (requestToProcess.getStatus() != Request.Status.ONGOING) {
            throw new IllegalStateException("The request to process must be ongoing.");
        }

        if (repositories == null) {
            throw new IllegalArgumentException("The application repositories object cannot be null.");
        }

        if (pluginsDiscoverer == null) {
            throw new IllegalArgumentException("The task plugins discoverer cannot be null.");
        }

        if (smtpSettings == null) {
            throw new IllegalArgumentException("The e-mail settings object cannot be null.");
        }

        if (applicationLanguage == null) {
            throw new IllegalArgumentException("The application language code cannot be null.");
        }

        this.request = requestToProcess;
        this.applicationRepositories = repositories;
        this.taskPluginsDiscoverer = pluginsDiscoverer;
        this.emailSettings = smtpSettings;
        this.language = applicationLanguage;
        this.completionListeners = new CopyOnWriteArraySet<>();
    }



    /**
     * Executes this job.
     */
    @Override
    public final void run() {
        final int requestId = this.request.getId();
        this.logger.debug("Looking for the next task to execute for request {}.", requestId);

        try {

            if (request.isRejected()) {
                this.logger.debug("The request {} is set as rejected. Marking it for export.", requestId);
                this.prepareRequestForExport();

            } else {
                final Task nextTask = this.getNextTask();

                if (nextTask == null) {
                    this.logger.debug("No task remaining for request {}. Marking it for export.", requestId);
                    this.prepareRequestForExport();
                } else {
                    this.logger.debug("Running the next task for request {}.", requestId);
                    this.executeTask(nextTask);
                }
            }

        } catch (Exception exception) {
            this.logger.error("An error occurred when processing the next task for request {}.", requestId, exception);
        }

        this.notifyCompletionListeners();
    }



    /**
     * Adds an object to those who will be notified when this task finishes.
     *
     * @param listener the object to notify
     */
    public final void subscribeToCompletionNotification(final TaskCompleteListener<Integer> listener) {
        this.completionListeners.add(listener);
    }



    /**
     * Removes an object from those who will be notify when this task finishes.
     *
     * @param listener the object that should not be notified after all
     */
    public final void unsubscribeFromCompletionNotification(final TaskCompleteListener<Integer> listener) {
        this.completionListeners.remove(listener);
    }



    /**
     * Adds a new item to the request history to trace the execution of the current task.
     *
     * @param task the task to trace
     */
    private void createNewHistoryRecord(final Task task) {
        assert task != null : "The task cannot be null.";

        final User systemUser = this.applicationRepositories.getUsersRepository().getSystemUser();

        if (systemUser == null) {
            throw new SystemUserNotFoundException();
        }

        final int requestId = request.getId();
        this.logger.debug("Creating a new request history record for task {} of request {}.", task.getId(),
                requestId);

        final RequestHistoryRepository repository = this.applicationRepositories.getRequestHistoryRepository();
        final int step = repository.findByRequestOrderByStep(this.request).size() + 1;
        this.logger.debug("The request history step for request {} is {}.", requestId, step);

        final RequestHistoryRecord historyRecord = new RequestHistoryRecord();
        historyRecord.setRequest(this.request);
        historyRecord.setStartDate(new GregorianCalendar());
        historyRecord.setStep(step);
        historyRecord.setProcessStep(task.getPosition());
        historyRecord.setTaskLabel(task.getLabel());
        historyRecord.setStatus(RequestHistoryRecord.Status.ONGOING);
        historyRecord.setUser(systemUser);

        this.taskHistoryRecord = repository.save(historyRecord);
        this.logger.debug("History record {} for request {} created and set to ongoing.", step, requestId);
    }



    /**
     * Runs the plugin for the current task.
     *
     * @param task the task to run
     */
    private void executeTask(final Task task) {
        int taskNumber = this.request.getTasknum();
        this.createNewHistoryRecord(task);

        try {
            final String pluginCode = task.getCode();
            final ITaskProcessor taskPlugin = this.taskPluginsDiscoverer.getTaskProcessor(pluginCode);

            if (taskPlugin == null) {
                final String errorMessage = String.format("Plugin %s not found.", pluginCode);
                this.logger.error(String.format("The plugin %s could not be found.", pluginCode));
                this.taskHistoryRecord.setToError(errorMessage);
                this.taskHistoryRecord
                        = this.applicationRepositories.getRequestHistoryRepository().save(this.taskHistoryRecord);
                this.request.setStatus(Request.Status.ERROR);
                this.request = this.applicationRepositories.getRequestsRepository().save(this.request);
                this.sendErrorEmailToOperators(task, errorMessage, new GregorianCalendar());

                return;
            }

            final ITaskProcessor pluginInstance = taskPlugin.newInstance(this.language, task.getParametersValues());
            final String dataFoldersBasePath = this.applicationRepositories.getParametersRepository().getBasePath();
            final TaskProcessorRequest taskProcessorRequest
                    = new TaskProcessorRequest(this.request, dataFoldersBasePath);
            final ITaskProcessorResult pluginResult = pluginInstance.execute(taskProcessorRequest, this.emailSettings);

            this.processTaskResult(task, pluginResult, new GregorianCalendar());

        } catch (Exception exception) {
            this.logger.error("An error occurred when executing task {} (ID: {}) for request {}.", task.getId(),
                    taskNumber, this.request.getId(), exception);
            String errorMessage = exception.getMessage();

            if (errorMessage == null) {
                errorMessage = "(Null)";
            }

            this.processTaskError(task, errorMessage, new GregorianCalendar());
        }
    }



    /**
     * Obtains the task that has needs to be run to continue the processing of the request.
     *
     * @return the next task to run, or <code>null</code> if all the tasks for the request process have been done
     */
    private Task getNextTask() {
        final Process process = this.request.getProcess();

        if (process == null) {
            throw new IllegalStateException("The process for the ongoing request is not defined or does not exist.");
        }

        final Integer currentTaskNumber = this.request.getTasknum();

        if (currentTaskNumber == null || currentTaskNumber < 1) {
            throw new IllegalStateException("The task number of an ongoing request must be set and greater than 0.");
        }

        Task[] processTasks = this.applicationRepositories.getTasksRepository().findByProcessOrderByPosition(process);

        if (currentTaskNumber > processTasks.length) {
            this.logger.debug("Task number {} is greater than the length of the process task collection ({}).",
                    currentTaskNumber, processTasks.length);
            return null;
        }

        return processTasks[currentTaskNumber - 1];
    }



    /**
     * Obtains the e-mail addresses of the users who supervise a given process.
     *
     * @param process the process whose operators address must be fetched
     * @return an array containing the addresses of the operators
     */
    @Transactional(readOnly = true)
    public String[] getProcessOperatorsAddresses(final Process process) {
        assert process != null : "The process cannot be null.";

        return this.applicationRepositories.getProcessesRepository().getProcessOperatorsAddresses(process.getId());
    }



    /**
     * Indicates that this task is done to the objects that asked to be informed of it.
     */
    private void notifyCompletionListeners() {
        this.logger.debug("Notifying the completion listening objects that the task is done.");

        for (TaskCompleteListener<Integer> listener : this.completionListeners) {
            listener.notifyTaskCompletion(this.request.getId());
        }
    }



    /**
     * Updates the current request to indicate that it is ready to be exported.
     */
    private void prepareRequestForExport() {
        this.request.setStatus(Request.Status.TOEXPORT);
        this.applicationRepositories.getRequestsRepository().save(request);
    }



    /**
     * Carries the appropriate actions when a task plugin execution is over.
     *
     * @param task         the task that was executed
     * @param pluginResult the object returned by the plugin that executed the task
     * @param taskEndDate  when the task returned from execution
     */
    private void processTaskResult(final Task task, final ITaskProcessorResult pluginResult,
            final Calendar taskEndDate) {
        assert task != null : "The task must not be null.";

        if (pluginResult == null || pluginResult.getStatus() == null) {
            this.logger.warn("The plugin for task {} return a null result.", task.getLabel());
            this.processTaskError(task, "(Null)", taskEndDate);
            return;
        }

        if (!this.processTaskResultRemark(task, pluginResult, taskEndDate)) {
            return;
        }

        this.processTaskResultStatus(task, pluginResult, taskEndDate);
    }



    /**
     * Carries the appropriate actions if the remark returned by the task plugin is invalid.
     *
     * @param task         the task that was executed
     * @param pluginResult the object returned by the plugin that executed the task
     * @param taskEndDate  when the task returned from execution
     * @return <code>true</code> if the remark is valid
     */
    private boolean processTaskResultRemark(final Task task, final ITaskProcessorResult pluginResult,
            final Calendar taskEndDate) {
        assert task != null : "The current task cannot be null.";
        assert pluginResult != null : "The plugin result object cannot be null.";
        assert pluginResult.getRequestData() != null : "The plugin result object must contain the request data.";

        final String remark = pluginResult.getRequestData().getRemark();

        if (remark != null && remark.length() > RequestTaskRunner.MAXIMUM_REMARK_LENGTH) {
            this.processTaskError(task, this.emailSettings.getMessageString("requestDetails.error.remark.tooLong",
                    new Object[]{RequestTaskRunner.MAXIMUM_REMARK_LENGTH}), taskEndDate);
            return false;
        }

        return true;
    }



    /**
     * Carries the appropriate actions based on the status returned by the task.
     *
     * @param task         the task that was executed
     * @param pluginResult the object returned by the plugin that executed the task
     * @param taskEndDate  when the task returned from execution
     */
    private void processTaskResultStatus(final Task task, final ITaskProcessorResult pluginResult,
            final Calendar taskEndDate) {

        switch (pluginResult.getStatus()) {
            case ERROR -> this.processTaskError(task, pluginResult, taskEndDate);

            case NOT_RUN -> {
                this.logger.info("The task {} (ID: {}) could not be run at the moment by the plugin {}. Execution will be attempted again at the next orchestrator step.",
                        task.getLabel(), task.getId(), task.getCode());
                this.applicationRepositories.getRequestHistoryRepository().delete(this.taskHistoryRecord);
                this.taskHistoryRecord = null;
            }

            case STANDBY -> this.processTaskStandby(task, pluginResult, taskEndDate);

            case SUCCESS -> this.processTaskSuccess(pluginResult, taskEndDate);

            default ->
                    this.logger.error("The plugin result status ({}) for task {} is invalid.", pluginResult.getStatus(),
                                      task.getLabel());
        }

    }



    /**
     * Carries the appropriate actions when a task fails.
     *
     * @param task         the task that failed
     * @param errorMessage the string that explains why the task failed
     * @param errorDate    when the task failed
     */
    private void processTaskError(final Task task, final String errorMessage, final Calendar errorDate) {
        assert task != null : "The task must not be null.";

        this.updateResult(RequestHistoryRecord.Status.ERROR, errorMessage, errorDate, null);
        this.sendErrorEmailToOperators(task, errorMessage, errorDate);
    }



    /**
     * Carries the appropriate actions when a task fails.
     *
     * @param task         the task that failed
     * @param pluginResult the data returned by the plugin that executed the task
     * @param errorDate    when the task failed
     */
    private void processTaskError(final Task task, final ITaskProcessorResult pluginResult,
            final Calendar errorDate) {
        assert pluginResult != null : "The result must not be null";
        assert pluginResult.getStatus() == ITaskProcessorResult.Status.ERROR : "The result must be an error.";
        assert errorDate != null : "The error date cannot be null.";

        final String errorMessage = String.format("%s (%s)", pluginResult.getMessage(),
                pluginResult.getErrorCode());
        this.processTaskError(task, errorMessage, errorDate);
    }



    /**
     * Carries the appropriate actions when a task requires validation by an operator.
     *
     * @param task         the task that requires validation
     * @param pluginResult the data returned by the plugin that executed the task
     * @param standbyDate  when the task returned in standby state
     */
    private void processTaskStandby(final Task task, final ITaskProcessorResult pluginResult,
            final Calendar standbyDate) {
        assert task != null : "The task must not be null.";
        assert pluginResult != null : "The result must not be null.";
        assert pluginResult.getStatus() == ITaskProcessorResult.Status.STANDBY : "The result must be a standby.";
        assert standbyDate != null : "The standby date cannot be null.";

        this.updateResult(RequestHistoryRecord.Status.STANDBY, pluginResult.getMessage(), standbyDate, null);
        this.sendStandbyEmailToOperators(task);

        // Set lastReminder to prevent immediate reminder from StandbyRequestsReminderProcessor.
        // First reminder will be sent X days after this date (as configured in system parameters).
        this.request.setLastReminder(standbyDate);
    }



    /**
     * Carries the appropriate actions when a task completes.
     *
     * @param pluginResult the data returned by the plugin that executed the task
     * @param taskEndDate  when the task returned successfully
     */
    private void processTaskSuccess(final ITaskProcessorResult pluginResult, final Calendar taskEndDate) {
        assert pluginResult != null : "The task plugin result cannot be null";
        assert pluginResult.getStatus() == ITaskProcessorResult.Status.SUCCESS : "The plugin result must be a success.";
        assert taskEndDate != null : "The task end date cannot be null.";

        this.updateResult(RequestHistoryRecord.Status.FINISHED, pluginResult.getMessage(), taskEndDate,
                pluginResult.getRequestData());
    }



    /**
     * Gets the operators for a given process.
     *
     * @param process the process whose operators must be fetched
     * @return a list of User objects representing the operators
     */
    @Transactional(readOnly = true)
    public java.util.List<User> getProcessOperators(final Process process) {
        assert process != null : "The process cannot be null.";
        this.logger.debug("Fetching the operators for process {}.", process.getId());
        return this.applicationRepositories.getProcessesRepository().getProcessOperators(process.getId());
    }



    /**
     * Notifies the users who supervise the current process that the current task failed.
     *
     * @param task         the task that failed
     * @param errorMessage the string returned by the task plugin to explain why it failed
     * @param failureTime  when the task failed
     */
    private void sendErrorEmailToOperators(final Task task, final String errorMessage, final Calendar failureTime) {
        assert task != null : "The task that failed cannot be null.";
        assert this.request != null : "The request that failed cannot be null.";
        assert errorMessage != null : "The error message cannot be null.";
        assert failureTime != null : "The failure time cannot be null.";

        this.logger.debug("Sending e-mail notifications to the operators of the process that failed.");

        // Get operators as User objects
        final java.util.List<User> operators = this.getProcessOperators(task.getProcess());

        if (operators == null || operators.isEmpty()) {
            this.logger.error("Could not fetch the operators for this process.");
            this.logger.debug("Task id is {}. Process id is {}.", task.getId(), task.getProcess().getId());
            return;
        }

        this.logger.debug("Found {} operators for process {}.", operators.size(), task.getProcess().getId());

        // Parse available locales from configuration
        final java.util.List<java.util.Locale> availableLocales = LocaleUtils.parseAvailableLocales(this.language);
        boolean atLeastOneEmailSent = false;

        // Send individual emails to each operator with their preferred locale
        for (User operator : operators) {
            try {
                final TaskFailedEmail message = new TaskFailedEmail(this.emailSettings);

                // Get validated locale for this operator
                java.util.Locale userLocale = LocaleUtils.getValidatedUserLocale(operator, availableLocales);

                if (!message.initializeContent(task, this.request, errorMessage, failureTime, userLocale)) {
                    this.logger.error("Could not create the message for user {}.", operator.getLogin());
                    continue;
                }

                try {
                    message.addRecipient(operator.getEmail());
                } catch (javax.mail.internet.AddressException e) {
                    this.logger.error("Invalid email address for user {}: {}", operator.getLogin(), operator.getEmail());
                    continue;
                }

                if (message.send()) {
                    this.logger.debug("Task failure notification sent successfully to {} with locale {}.",
                                    operator.getEmail(), userLocale.toLanguageTag());
                    atLeastOneEmailSent = true;
                } else {
                    this.logger.warn("Failed to send task failure notification to {}.", operator.getEmail());
                }

            } catch (Exception exception) {
                this.logger.warn("Error sending notification to user {}: {}", operator.getLogin(), exception.getMessage());
            }
        }

        if (atLeastOneEmailSent) {
            this.logger.info("The task failure notification was successfully sent to at least one operator.");
        } else {
            this.logger.warn("The task failure notification was not sent to any operator.");
        }
    }



    /**
     * Notifies the users who supervise the current process that the current task requires an intervention.
     *
     * @param task the task that is in standby mode
     */
    private void sendStandbyEmailToOperators(final Task task) {
        assert task != null : "The task that failed cannot be null.";
        assert task.getProcess() != null : "The task process cannot be null.";
        assert this.request != null : "The request that failed cannot be null.";

        this.logger.debug("Sending e-mail notifications to the operators of the process is in standby mode.");

        // Get operators as User objects
        final java.util.List<User> operators = this.getProcessOperators(task.getProcess());

        if (operators == null || operators.isEmpty()) {
            this.logger.error("Could not fetch the operators for this process.");
            this.logger.debug("Task id is {}. Process id is {}.", task.getId(), task.getProcess().getId());
            return;
        }

        this.logger.debug("Found {} operators for process {}.", operators.size(), task.getProcess().getId());

        // Parse available locales from configuration
        final java.util.List<java.util.Locale> availableLocales = LocaleUtils.parseAvailableLocales(this.language);
        boolean atLeastOneEmailSent = false;

        // Send individual emails to each operator with their preferred locale
        for (User operator : operators) {
            try {
                final TaskStandbyEmail message = new TaskStandbyEmail(this.emailSettings);

                // Get validated locale for this operator
                java.util.Locale userLocale = LocaleUtils.getValidatedUserLocale(operator, availableLocales);

                if (!message.initializeContent(this.request, userLocale)) {
                    this.logger.error("Could not create the message for user {}.", operator.getLogin());
                    continue;
                }

                try {
                    message.addRecipient(operator.getEmail());
                } catch (javax.mail.internet.AddressException e) {
                    this.logger.error("Invalid email address for user {}: {}", operator.getLogin(), operator.getEmail());
                    continue;
                }

                if (message.send()) {
                    this.logger.debug("Task standby notification sent successfully to {} with locale {}.",
                                    operator.getEmail(), userLocale.toLanguageTag());
                    atLeastOneEmailSent = true;
                } else {
                    this.logger.warn("Failed to send task standby notification to {}.", operator.getEmail());
                }

            } catch (Exception exception) {
                this.logger.warn("Error sending notification to user {}: {}", operator.getLogin(), exception.getMessage());
            }
        }

        if (atLeastOneEmailSent) {
            this.logger.info("The task standby notification was successfully sent to at least one operator.");
        } else {
            this.logger.warn("The task standby notification was not sent to any operator.");
        }
    }



    /**
     * Modifies the requests properties based on the result of a task.
     *
     * @param taskResultStatus    the status to set as the result of the task
     * @param modifiedRequestData the possibly modified request properties returned by the task, or <code>null</code>
     *                            if the request has not been modified by the plugin
     */
    private void updateRequestWithResult(final RequestHistoryRecord.Status taskResultStatus,
            final ITaskProcessorRequest modifiedRequestData) {

        switch (taskResultStatus) {
            case ERROR -> this.request.setStatus(Request.Status.ERROR);

            case FINISHED -> {
                this.request.setTasknum(this.request.getTasknum() + 1);

                if (modifiedRequestData != null) {
                    this.updateRequestFromPlugin(modifiedRequestData);
                }
            }
            case STANDBY -> {
                this.request.setStatus(Request.Status.STANDBY);
            }

            default -> this.logger.error("The result status ({}) for task \"{}\" is invalid.", taskResultStatus,
                                         this.taskHistoryRecord.getTaskLabel());
        }

    }



    /**
     * Updates the properties of an order that can be modified by a task plugin. Currently, the supported
     * properties are whether the order must be rejected and the remark. Note that if a plugin rejects the order, it
     * must provide a remark, too.
     *
     * @param modifiedRequestData the modified request data
     */
    private void updateRequestFromPlugin(final ITaskProcessorRequest modifiedRequestData) {
        assert modifiedRequestData != null : "At this point, the modified request data cannot be null.";

        final String modifiedRemark = modifiedRequestData.getRemark();

        if (modifiedRequestData.isRejected()) {

            if (StringUtils.isEmpty(modifiedRemark)) {
                throw new IllegalStateException("The task plugin must set a remark if it rejects the request.");
            }

            this.request.setRejected(true);
            this.request.setRemark(modifiedRemark);

        } else if (!StringUtils.equals(this.request.getRemark(), modifiedRequestData.getRemark())) {
            this.request.setRemark(modifiedRequestData.getRemark());
        }

    }



    /**
     * Modifies the request and its related objects to reflect the outcome of the current task.
     *
     * @param taskResultStatus the status to set as the result of the task
     * @param message          the string returned by the plugin to explain the result
     * @param taskEndDate      when the task returned the result
     * @param modifiedRequest  the possibly modified request data returned by the task plugin, or <code>null</code>
     *                         if the plugin did not modify the request
     *
     */
    private void updateResult(final RequestHistoryRecord.Status taskResultStatus, final String message,
            final Calendar taskEndDate, final ITaskProcessorRequest modifiedRequest) {
        assert taskResultStatus != null : "The task status cannot be null";
        assert taskResultStatus != RequestHistoryRecord.Status.ONGOING :
                "The task status cannot be set to ongoing at this point.";
        assert taskEndDate != null : "The task end date cannot be null.";

        this.updateRequestWithResult(taskResultStatus, modifiedRequest);
        this.updateHistoryRecordWithResult(taskResultStatus, message, taskEndDate);
        this.request = this.applicationRepositories.getRequestsRepository().save(this.request);
    }



    /**
     * Modifies the request history to reflect the outcome of the current task.
     *
     * @param taskResultStatus the status to set as the result of the task
     * @param message          the string returned by the plugin to explain the result
     * @param taskEndDate      when the task returned from execution
     */
    private void updateHistoryRecordWithResult(final RequestHistoryRecord.Status taskResultStatus,
            final String message, final Calendar taskEndDate) {
        assert this.taskHistoryRecord != null
                && this.taskHistoryRecord.getStatus() == RequestHistoryRecord.Status.ONGOING :
                "There must be an active request history record at this point and it must be ongoing.";
        assert taskResultStatus != null : "The task result status cannot be null";
        assert taskResultStatus != RequestHistoryRecord.Status.ONGOING :
                "The task status cannot be set to ongoing at this point.";

        this.taskHistoryRecord.setStatus(taskResultStatus);
        this.taskHistoryRecord.setEndDate(taskEndDate);
        this.logger.debug("Task result message is: {}", message);
        this.taskHistoryRecord.setMessage(message);
        this.taskHistoryRecord
                = this.applicationRepositories.getRequestHistoryRepository().save(this.taskHistoryRecord);
    }

}
