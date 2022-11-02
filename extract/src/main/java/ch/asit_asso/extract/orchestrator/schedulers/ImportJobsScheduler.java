/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.asit_asso.extract.orchestrator.schedulers;

import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.orchestrator.OrchestratorSettings;
import ch.asit_asso.extract.orchestrator.runners.CommandImportJobRunner;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;



/**
 * Manages the background operation of the jobs that will import the commands from the various connectors instances at a
 * frequency that is defined by the connector instance.
 *
 * @author Yves Grasset
 */
public class ImportJobsScheduler extends JobScheduler {

    /**
     * The Spring Data objects that link the data objects with the data source.
     */
    private final ApplicationRepositories applicationRepositories;

    /**
     * The access to the available connector plugins.
     */
    private final ConnectorDiscovererWrapper connectorsDiscoverer;

    /**
     * The objects that are required to create and send an e-mail message.
     */
    private final EmailSettings emailSettings;

    /**
     * The job that checks at a regular interval if the import jobs of the connectors are correctly
     * scheduled.
     */
    private ScheduledTask importJobsScheduledTask;

    /**
     * The locale of the language that the application displays messages in.
     */
    private final String language;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ImportJobsScheduler.class);

    private final OrchestratorSettings orchestratorSettings;

    /**
     * A collection that keeps a trace of the import job that is scheduled for each connector.
     */
    private Map<Integer, JobSchedulingInfo> scheduledJobsMap;



    /**
     * Creates a new instance of the import job scheduler.
     *
     * @param taskRegistrar               the object that allows to schedule actions at a fixed interval
     * @param repositories                an ensemble of objects linking the data objects with the database
     * @param connectorsPluginsDiscoverer an object that provides access to the available connector plugins
     * @param smtpSettings                the objects required to create and send an e-mail message
     * @param applicationLanguage         the locale code of the language used by the application to display messages
     */
    public ImportJobsScheduler(final ScheduledTaskRegistrar taskRegistrar, final ApplicationRepositories repositories,
            final ConnectorDiscovererWrapper connectorsPluginsDiscoverer, final EmailSettings smtpSettings,
            final String applicationLanguage, final OrchestratorSettings orchestratorSettings) {
        super(taskRegistrar);

        if (connectorsPluginsDiscoverer == null) {
            throw new IllegalArgumentException("The connector plugins discoverer cannot be null.");
        }

        if (repositories == null) {
            throw new IllegalArgumentException("The application repositories object cannot be null.");
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

        this.applicationRepositories = repositories;
        this.connectorsDiscoverer = connectorsPluginsDiscoverer;
        this.emailSettings = smtpSettings;
        this.language = applicationLanguage;
        this.orchestratorSettings = orchestratorSettings;
        this.setSchedulingStep(this.orchestratorSettings.getFrequency());
    }



    /**
     * Starts, updates and removes the background import tasks according to the current connectors instances.
     */
    @Override
    public final void scheduleJobs() {
        final var task = new FixedDelayTask(this::scheduleImportJobs, this.getSchedulingStepInMilliseconds(), 0);

        this.importJobsScheduledTask = this.getTaskRegistrar().scheduleFixedDelayTask(task);
        this.logger.info("Connectors monitoring task configured to run every {} second(s).", this.getSchedulingStep());
    }



    @Override
    public final void unscheduleJobs() {
        this.logger.debug("Unscheduling the connectors import jobs tasks.");

        if (this.importJobsScheduledTask == null) {
            this.logger.debug("The import jobs manager task was not scheduled, so nothing done.");
            return;
        }

        this.importJobsScheduledTask.cancel();
        this.logger.info("The connectors monitoring task has been unscheduled.");
        this.unscheduleImportJobs();
    }



    /**
     * Obtains the object that links the connector instance data objects with the data source.
     *
     * @return the repository
     */
    private ConnectorsRepository getConnectorsRepository() {
        return this.applicationRepositories.getConnectorsRepository();
    }



    /**
     * Ensures that the active connectors (and only the active connectors) will look for orders on their
     * server based on their import frequency.
     */
    private void scheduleImportJobs() {
        assert this.connectorsDiscoverer != null : "The connector plugins discoverer must be set.";
        assert this.applicationRepositories != null : "The application repositories must be set.";
        assert this.applicationRepositories.getConnectorsRepository() != null :
                "The connectors repository must be set.";

        this.logger.debug("Scheduling connectors imports jobs.");

        if (this.scheduledJobsMap == null) {
            this.logger.debug("Instantiating the import jobs map.");
            this.scheduledJobsMap = new ConcurrentHashMap<>();
        }

        List<Connector> activeConnectors = this.getConnectorsRepository().findByActiveTrue();
        this.logger.debug("Found {} currently active connectors.", activeConnectors.size());
        List<Integer> idsOfJobsToCancel = new ArrayList<>(this.scheduledJobsMap.keySet());

        for (Connector connector : activeConnectors) {
            Integer connectorId = connector.getId();
            String connectorName = connector.getName();
            this.logger.debug("Processing connector {}", connectorName);
            long delay = (long) connector.getImportFrequency() * MILLISECONDS_FACTOR;
            JobSchedulingInfo jobSchedulingInfo = this.scheduledJobsMap.get(connectorId);

            if (jobSchedulingInfo == null) {
                this.logger.debug("Connector {} is new or became active.", connectorName);
                this.scheduleConnectorJob(connector);

                continue;
            }

            idsOfJobsToCancel.remove(connectorId);

            if (jobSchedulingInfo.getDelay() != delay) {
                this.logger.debug("Connector {} is currently scheduled but the delay has changed.", connectorName);
                this.rescheduleConnectorJob(connector);

                continue;
            }

            this.logger.debug("No scheduling change for connector {}.", connectorName);
        }

        if (idsOfJobsToCancel.size() > 0) {
            this.logger.debug("Unscheduling {} connectors that have been disabled or deleted.",
                    idsOfJobsToCancel.size());

            for (Integer jobId : idsOfJobsToCancel) {
                this.unscheduleConnectorJob(jobId);
            }
        }
    }



    /**
     * Creates a recurring background import task for a connector instance.
     *
     * @param connector the connector instance
     */
    private void scheduleConnectorJob(final Connector connector) {
        assert connector != null : "The connector instance must not be null";
        assert this.connectorsDiscoverer != null : "The connector plugins discoverer must be set.";

        long delay = (long) connector.getImportFrequency() * MILLISECONDS_FACTOR;
        String connectorName = connector.getName();
        String connectorCode = connector.getConnectorCode();
        IConnector connectorPlugin = this.connectorsDiscoverer.getConnector(connectorCode);

        if (connectorPlugin == null) {
            this.logger.warn("The connector plugin {} for connector instance {} is not available anymore."
                    + " The import job has not been scheduled.", connectorCode, connectorName);
            return;
        }

        try {
            CommandImportJobRunner jobRunner = new CommandImportJobRunner(connector.getId(), connectorPlugin,
                    this.applicationRepositories, this.emailSettings, this.language);
            this.logger.debug("Task to run import job for connector {} created.", connectorName);
            TaskScheduler taskScheduler = this.getTaskScheduler();
            ScheduledFuture jobFuture = taskScheduler.scheduleWithFixedDelay(jobRunner, delay);
            this.logger.debug("Import task for connector {} added to the scheduler.", connectorName);
            int connectorId = connector.getId();
            this.scheduledJobsMap.put(connectorId, new JobSchedulingInfo(connectorId, delay, jobFuture));
            this.logger.info("Connector {} command import job is scheduled.", connectorName);

        } catch (Exception exception) {
            this.logger.error("An error occurred while attempting to schedule the import job for connector {}.",
                    connectorName, exception);
        }
    }



    /**
     * Updates the configuration of the import task for a connector instance.
     *
     * @param connector the connector instance
     */
    private void rescheduleConnectorJob(final Connector connector) {
        assert connector != null : "The connector instance must not be null";

        this.unscheduleConnectorJob(connector.getId());
        this.scheduleConnectorJob(connector);
    }



    /**
     * Cancels the recurring execution of an import task.
     *
     * @param jobId the identifier of the task
     */
    private void unscheduleConnectorJob(final int jobId) {
        assert this.scheduledJobsMap != null : "The scheduled jobs map should have been instantiated by now.";

        this.logger.debug("Unscheduling connector import job with identifier {}.", jobId);
        JobSchedulingInfo schedulingInfo = this.scheduledJobsMap.get(jobId);

        if (schedulingInfo == null) {
            this.logger.warn("Attempted to unschedule job {}, but could not get its scheduling information.");
            return;
        }

        schedulingInfo.cancelJob(false);
        this.logger.debug("Connector job {} has been cancelled.", jobId);
        this.scheduledJobsMap.remove(jobId);
        this.logger.info("Connector job with identifier {} is not scheduled anymore.", jobId);
    }



    /**
     * Prevents all connectors from looking for orders on their server.
     */
    private void unscheduleImportJobs() {
        this.logger.debug("Unscheduling the current connectors import jobs.");

        for (int jobId : this.scheduledJobsMap.keySet()) {
            this.unscheduleConnectorJob(jobId);
        }
    }

}
