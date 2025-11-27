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
package ch.asit_asso.extract.orchestrator.schedulers;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.orchestrator.OrchestratorSettings;
import ch.asit_asso.extract.orchestrator.runners.ExportRequestsJobRunner;
import ch.asit_asso.extract.orchestrator.runners.RequestMatcherJobRunner;
import ch.asit_asso.extract.orchestrator.runners.RequestNotificationJobRunner;
import ch.asit_asso.extract.orchestrator.runners.RequestTaskRunner;
import ch.asit_asso.extract.orchestrator.runners.TaskCompleteListener;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import ch.asit_asso.extract.persistence.RequestHistoryRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorDiscovererWrapper;
import ch.asit_asso.extract.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;



/**
 * Manages the jobs that carry the different treatments required by a command.
 *
 * @author Yves Grasset
 */
public class RequestsProcessingScheduler extends JobScheduler implements TaskCompleteListener<Integer> {

    /**
     * The locale of the language that the application displays messages in.
     */
    private final String applicationLangague;

    /**
     * An ensemble of objects linking the data objects with the database.
     */
    private final ApplicationRepositories applicationRepositories;

    /**
     * The access to the available connector plugins.
     */
    private final ConnectorDiscovererWrapper connectorPluginDiscoverer;

    /**
     * The object that assembles the configuration objects required to create and send an e-mail message.
     */
    private final EmailSettings emailSettings;

    /**
     * The service for obtaining localized messages.
     */
    private final MessageService messageService;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(RequestsProcessingScheduler.class);

    /**
     * The recurring job that attempts to match the new requests with a process.
     */
    private ScheduledTask processMatchingScheduledTask;

    private ScheduledTask requestNotificationScheduledTask;

    /**
     * A collection of request that have a task currently running.
     */
    private final Set<Integer> requestsWithRunningTask;

    /**
     * The access to the available task plugins.
     */
    private final TaskProcessorDiscovererWrapper taskPluginDiscoverer;

    /**
     * The recurring job that executes the next task for ongoing requests.
     */
    private ScheduledTask taskExecutionScheduledTask;

    /**
     * The recurring job that processes requests that are ready to be exported.
     */
    private ScheduledTask taskExportScheduledTask;

    /**
     * The manager for the thread pool used to execute the requests tasks.
     */
    private final ExecutorService taskExecutorService;



    /**
     * Creates a new instance of this scheduler.
     *
     * @param taskRegistrar        the object that allows to schedule action at a given interval
     * @param repositories         an ensemble of objects linking the data objects with the database
     * @param connectorsDiscoverer an access to the available connector plugins
     * @param tasksDiscoverer      an access to the available task plugins
     * @param smtpSettings         an object that contains the configuration objects to send an e-mail message
     * @param applicationLanguage  the locale code of the language used by the application to display messages
     * @param messageService       the service for obtaining localized messages
     */
    public RequestsProcessingScheduler(final ScheduledTaskRegistrar taskRegistrar,
            final ApplicationRepositories repositories, final ConnectorDiscovererWrapper connectorsDiscoverer,
            final TaskProcessorDiscovererWrapper tasksDiscoverer, final EmailSettings smtpSettings,
            final String applicationLanguage, final OrchestratorSettings orchestratorSettings, final MessageService messageService) {

        super(taskRegistrar);

        if (repositories == null) {
            throw new IllegalArgumentException("The application repositories object cannot be null.");
        }

        if (connectorsDiscoverer == null) {
            throw new IllegalArgumentException("The connector plugin discoverer cannot be null.");
        }

        if (tasksDiscoverer == null) {
            throw new IllegalArgumentException("The task plugin discoverer cannot be null.");
        }

        if (smtpSettings == null) {
            throw new IllegalArgumentException("The e-mail settings object cannot be null.");
        }

        if (applicationLanguage == null) {
            throw new IllegalArgumentException("The application language code cannot be null.");
        }

        if (orchestratorSettings == null) {
            throw new IllegalArgumentException("The orchestrator settings cannot be null.");
        }

        if (messageService == null) {
            throw new IllegalArgumentException("The message service cannot be null.");
        }

        this.applicationRepositories = repositories;
        this.connectorPluginDiscoverer = connectorsDiscoverer;
        this.taskPluginDiscoverer = tasksDiscoverer;
        this.requestsWithRunningTask = new CopyOnWriteArraySet<>();
        this.emailSettings = smtpSettings;
        this.applicationLangague = applicationLanguage;
        this.messageService = messageService;
        this.taskExecutorService = Executors.newCachedThreadPool();
        this.setSchedulingStep(orchestratorSettings.getFrequency());
    }



    /**
     * Starts the different request processing jobs.
     */
    @Override
    public final void scheduleJobs() {
        this.logger.debug("Scheduling the requests processing jobs.");
        this.scheduleTaskExportJob();
        this.scheduleProcessMatchingJob();
        this.scheduleTasksExecutionManagementJob();
        this.scheduleRequestNotifierJob();
    }



    /**
     * Stops the recurrence of the different request processing jobs.
     */
    @Override
    public final void unscheduleJobs() {
        this.logger.debug("Unscheduling the requests processing jobs.");
        this.unscheduleTaskExportJob();
        this.unscheduleProcessMatchingJob();
        this.unscheduleTasksExecutionManagementJob();
        this.unscheduleRequestNotifierJob();
    }



    /**
     * Registers that a task is currently in execution for a given request.
     *
     * @param requestId the identifier of the request to register
     */
    private synchronized void addRunningRequestToList(final int requestId) {
        assert requestId > 0 : "The request identifier must be greater than 0.";
        assert !this.requestsWithRunningTask.contains(requestId) : "The request is already in the list.";

        this.requestsWithRunningTask.add(requestId);
    }



    /**
     * Verifies that an ongoing request is in a state that is coherent with its history records.
     *
     * @param request the ongoing request
     * @return the status that the request should have based on its history
     */
    private Request.Status checkOngoingRequestHistory(final Request request) {
        assert request != null : "The request cannot be null";
        assert request.getStatus() == Request.Status.ONGOING : "The request must be ongoing.";

        final RequestHistoryRepository historyRepository = this.applicationRepositories.getRequestHistoryRepository();
        final List<RequestHistoryRecord> recordsList = historyRepository.findByRequestOrderByStepDesc(request);

        if (recordsList.isEmpty()) {
            return Request.Status.ONGOING;
        }

        final RequestHistoryRecord lastRecord = recordsList.get(0);

        if (lastRecord.getStatus() == RequestHistoryRecord.Status.ONGOING) {
            this.logger.warn("The processing of request {} has been interrupted. The status has thus been set to "
                             + "ERROR.", request.getId());
            lastRecord.setToError(this.emailSettings.getMessageString("errors.task.interrupted"));
            historyRepository.save(lastRecord);

            return Request.Status.ERROR;
        }

        return Request.Status.ONGOING;
    }



    /**
     * Checks if task job is already in execution for a given request.
     *
     * @param requestId the identifier of the request to check
     * @return <code>true</code> if a task is currently running
     */
    private synchronized boolean isRequestTaskRunning(final int requestId) {
        return this.requestsWithRunningTask.contains(requestId);
    }



    /**
     * Starts the tasks required to process the ongoing requests.
     */
    private void manageTaskProcessingJobs() {
        final RequestsRepository requestsRepository = this.applicationRepositories.getRequestsRepository();
        final List<Request> ongoingRequests = requestsRepository.findByStatus(Request.Status.ONGOING);
        final int requestsNumber = ongoingRequests.size();
        this.logger.debug("Found {} ongoing request{}.", requestsNumber, (requestsNumber > 1) ? "s" : "");

        for (Request request : ongoingRequests) {
            this.processOnGoingRequest(request);
        }
    }



    /**
     * Tells this scheduler that a task job is finished.
     *
     * @param requestId the identifier of the request for which a task has completed
     */
    @Override
    public final void notifyTaskCompletion(final Integer requestId) {

        if (requestId == null || requestId < 0) {
            this.logger.debug("The request processing scheduler has been notified of the completion of a task with an"
                              + " invalid request identifier: {}", requestId);
            return;
        }

        this.logger.debug("The task for request {} completed.", requestId);

        if (!this.isRequestTaskRunning(requestId)) {
            this.logger.warn("The request processing scheduler has been notified of the completion of a task for"
                             + " request {}, but this request is not currently registered as running a task.", requestId);
            return;
        }

        this.removeRunningRequestFromList(requestId);
        this.logger.debug("Request {} removed from those with a running task.", requestId);
    }



    /**
     * Start an asynchronous task job if the given request requires one.
     *
     * @param request the request
     */
    private void processOnGoingRequest(final Request request) {

        try {
            final int requestId = request.getId();
            this.logger.debug("Processing request {} (ID : {}).", request.getProductLabel(), requestId);

            if (this.isRequestTaskRunning(requestId)) {
                this.logger.debug("A task is already running for the request {}. Waiting for completion.",
                                  request.getId());
                return;
            }

            this.logger.debug("Checking the status of the last task for the request.");
            final Request.Status historyStatus = this.checkOngoingRequestHistory(request);

            if (historyStatus != Request.Status.ONGOING) {
                request.setStatus(historyStatus);
                this.applicationRepositories.getRequestsRepository().save(request);
                this.logger.warn("The status for request {} (ONGOING) was inconsistent with its last history entry. "
                                 + "It is now set to {}.", requestId, historyStatus);
                return;
            }

            this.logger.debug("Request can proceed to the next task.");

            RequestTaskRunner taskRunner = new RequestTaskRunner(request, this.applicationRepositories,
                                                                 this.taskPluginDiscoverer, this.emailSettings,
                                                                 this.applicationLangague);
            taskRunner.subscribeToCompletionNotification(this);
            this.logger.debug("Created the task runner.");
            this.taskExecutorService.submit(taskRunner);
            this.logger.debug("Task runner submitted.");
            this.addRunningRequestToList(requestId);
            this.logger.debug("Request {} added to the currently running tasks list.", requestId);

        } catch (Exception exception) {
            this.logger.error("Could not launch the next task for request {}.", request.getId(), exception);
        }
    }



    /**
     * Registers that no task is running anymore for a given request.
     *
     * @param requestId the identifier of the request
     */
    private synchronized void removeRunningRequestFromList(final int requestId) {
        assert requestId > 0 : "The request identifier must be greater than 0.";

        this.requestsWithRunningTask.remove(requestId);
    }



    /**
     * Starts the batch process that will attempt to match the freshly imported requests with a task
     * process.
     */
    private void scheduleProcessMatchingJob() {
        this.logger.debug("Scheduling the request process matching job.");
        final RequestMatcherJobRunner requestMatcherJobRunner
                = new RequestMatcherJobRunner(/*this.getJobRunnerComponents(),*/this.applicationRepositories,
                                                                                this.emailSettings);
        final var recurringTask = new FixedDelayTask(requestMatcherJobRunner,
                                                     this.getSchedulingStepInMilliseconds(), 0);
        this.processMatchingScheduledTask = this.getTaskRegistrar().scheduleFixedDelayTask(recurringTask);
        this.logger.debug("The request process matching job is scheduled with a {} second(s) delay.",
                          this.getSchedulingStep());
    }



    /**
     * Starts the batch process that will export requests results at a given interval.
     */
    private void scheduleRequestNotifierJob() {
        this.logger.debug("Scheduling the request notification job.");
        final RequestNotificationJobRunner notificationJobRunner = new RequestNotificationJobRunner(
                this.applicationRepositories, this.emailSettings, this.applicationLangague);
        final var recurringTask = new FixedDelayTask(notificationJobRunner, this.getSchedulingStepInMilliseconds(), 0);
        this.requestNotificationScheduledTask = this.getTaskRegistrar().scheduleFixedDelayTask(recurringTask);
        this.logger.debug("The request notification job is scheduled with a {} second(s) delay.", this.getSchedulingStep());
    }



    /**
     * Starts the batch process that will export requests results at a given interval.
     */
    private void scheduleTaskExportJob() {
        this.logger.debug("Scheduling the request export job.");
        final ExportRequestsJobRunner exportJobRunner = new ExportRequestsJobRunner(/*this.getJobRunnerComponents(),*/
                this.emailSettings, this.applicationRepositories, this.connectorPluginDiscoverer,
                this.applicationLangague, this.messageService);
        final var recurringTask = new FixedDelayTask(exportJobRunner, this.getSchedulingStepInMilliseconds(), 0);
        this.taskExportScheduledTask = this.getTaskRegistrar().scheduleFixedDelayTask(recurringTask);
        this.logger.debug("The request export job is scheduled with a {} second(s) delay.", this.getSchedulingStep());
    }



    /**
     * Starts the batch process that will run the tasks required to process the ongoing requests.
     */
    private void scheduleTasksExecutionManagementJob() {
        this.logger.debug("Scheduling the request task execution job.");
        final var recurringTask = new FixedDelayTask(this::manageTaskProcessingJobs,
                                                     this.getSchedulingStepInMilliseconds(), 0);
        this.taskExecutionScheduledTask = this.getTaskRegistrar().scheduleFixedDelayTask(recurringTask);

        this.logger.debug("The request task execution management job is scheduled with a {} second(s) delay.",
                          this.getSchedulingStep());
    }



    /**
     * Stops the thread pool that is used to execute the requests tasks.
     */
    private void shutdownTaskExecutionPool() {
        this.logger.debug("Forcing the shutdown of the thread pool that executes requests tasks.");
        int unexecutedTasksNumber = this.taskExecutorService.shutdownNow().size();
        this.logger.info("The requests tasks execution thread pool has been shut down.");

        if (unexecutedTasksNumber > 0) {
            this.logger.info("{} task{} could not have been run before the thread pool was shut down.",
                             unexecutedTasksNumber, (unexecutedTasksNumber > 1) ? "s" : "");
        }
    }



    /**
     * Stops the recurrence of the batch process that attempts to match the new requests with a process.
     */
    private void unscheduleProcessMatchingJob() {
        this.logger.debug("Unscheduling the request process matching job.");

        if (this.processMatchingScheduledTask == null) {
            this.logger.debug("The process matching job is not scheduled, so nothing done.");
            return;
        }

        this.processMatchingScheduledTask.cancel();
        this.logger.debug("The process matching job has been unscheduled.");
    }



    private void unscheduleRequestNotifierJob() {
        this.logger.debug("Unscheduling the request notification job.");

        if (this.requestNotificationScheduledTask == null) {
            this.logger.debug("The request notification job is not scheduled, so nothing done.");
            return;
        }

        this.requestNotificationScheduledTask.cancel();
        this.logger.debug("The request notification job has been unscheduled.");
    }



    /**
     * Stops the recurrence of the job that processes the requests that are ready to be exported.
     */
    private void unscheduleTaskExportJob() {
        this.logger.debug("Unscheduling the request export job.");

        if (this.taskExportScheduledTask == null) {
            this.logger.debug("The task export job is not scheduled, so nothing done.");
            return;
        }

        this.taskExportScheduledTask.cancel();
        this.logger.debug("The task export job has been unscheduled.");
    }



    /**
     * Stops the recurrence of the process that executes the tasks of the on-going requests.
     */
    private void unscheduleTasksExecutionManagementJob() {
        this.logger.debug("Unscheduling the requests task execution management job.");

        if (this.taskExecutionScheduledTask == null) {
            this.logger.debug("The requests task execution management job is not scheduled, so nothing done.");
            return;
        }

        this.taskExecutionScheduledTask.cancel();
        this.logger.debug("The request task execution management job has been unscheduled.");
        this.shutdownTaskExecutionPool();
    }
}
