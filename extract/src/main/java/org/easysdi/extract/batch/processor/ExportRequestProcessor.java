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
package org.easysdi.extract.batch.processor;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.easysdi.extract.connectors.ConnectorDiscovererWrapper;
import org.easysdi.extract.connectors.RequestResult;
import org.easysdi.extract.connectors.common.IConnector;
import org.easysdi.extract.connectors.common.IExportRequest;
import org.easysdi.extract.connectors.common.IExportResult;
import org.easysdi.extract.domain.Connector;
import org.easysdi.extract.domain.Request;
import org.easysdi.extract.domain.RequestHistoryRecord;
import org.easysdi.extract.email.EmailSettings;
import org.easysdi.extract.email.RequestExportFailedEmail;
import org.easysdi.extract.persistence.ApplicationRepositories;
import org.easysdi.extract.persistence.RequestHistoryRepository;
import org.easysdi.extract.persistence.TasksRepository;
import org.easysdi.extract.utils.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;



/**
 * An object that exports a request using its connector plugin.
 *
 * @author Yves Grasset
 */
public class ExportRequestProcessor implements ItemProcessor<Request, Request> {

    /**
     * The locale of the language that the application displays messages in.
     */
    private final String applicationLangague;

    /**
     * The absolute path of the folder that contains the data for all requests.
     */
    private final String basePath;

    /**
     * The access to all the available connector plugins.
     */
    private final ConnectorDiscovererWrapper connectorPluginDiscoverer;

    /**
     * The objects required to create and send an e-mail message.
     */
    private final EmailSettings emailSettings;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ExportRequestProcessor.class);

    /**
     * The link between the various data objects and the data source.
     */
    private final ApplicationRepositories repositories;



    /**
     * Creates a new instance of this processor.
     *
     * @param applicationRepositories the link between the various data objects and the data source
     * @param connectorsDiscoverer    the object that gives access to the available connector plugins
     * @param requestsFolderPath      the absolute path of the folder that contains the data for all requests
     * @param smtpSettings            the objects that are required to create and send an e-mail message
     * @param applicationLanguage     the locale code of the language used by the application to display messages
     */
    public ExportRequestProcessor(final ApplicationRepositories applicationRepositories,
            final ConnectorDiscovererWrapper connectorsDiscoverer, final String requestsFolderPath,
            final EmailSettings smtpSettings, final String applicationLanguage) {

        if (connectorsDiscoverer == null) {
            throw new IllegalArgumentException("The connector plugin discoverer cannot be null.");
        }

        if (applicationRepositories == null) {
            throw new IllegalArgumentException("The application repositories object cannot be null.");
        }

        if (requestsFolderPath == null) {
            throw new IllegalArgumentException("The base path cannot be null.");
        }

        if (!Paths.get(requestsFolderPath).isAbsolute()) {
            throw new IllegalArgumentException("The base path must be absolute.");
        }

        if (smtpSettings == null) {
            throw new IllegalArgumentException("The e-mail settings object cannot be null.");
        }

        if (applicationLanguage == null) {
            throw new IllegalArgumentException("The application language code cannot be null.");
        }

        this.repositories = applicationRepositories;
        this.connectorPluginDiscoverer = connectorsDiscoverer;
        this.basePath = requestsFolderPath;
        this.emailSettings = smtpSettings;
        this.applicationLangague = applicationLanguage;
    }



    /**
     * Exports a request.
     *
     * @param request the request to export
     * @return the request updated to reflect the result of the export
     */
    @Override
    public final Request process(final Request request) {
        assert this.connectorPluginDiscoverer != null : "The connector plugin discoverer must be set.";

        RequestHistoryRecord historyRecord = this.createHistoryRecord(request);
        int requestId = request.getId();
        Connector requestConnector = request.getConnector();
        IConnector connectorPlugin = this.connectorPluginDiscoverer.getConnector(requestConnector.getConnectorCode());

        if (connectorPlugin == null) {
            this.logger.warn("The connector plugin referenced by request {} is not available anymore."
                    + " Cannot export request.", requestId);
            return request;
        }

        File requestsDataFolder = new File(this.basePath);

        if (!requestsDataFolder.exists() || !requestsDataFolder.isDirectory()) {
            this.logger.error("The base requests data folder {} does not exist or is not a directory."
                    + " Cannot export the request.", this.basePath);
            return request;
        }

        IConnector connectorPluginInstance = connectorPlugin.newInstance(this.applicationLangague,
                requestConnector.getConnectorParametersValues());
        IExportRequest exportRequest = new RequestResult(request, this.basePath);
        IExportResult result = connectorPluginInstance.exportResult(exportRequest);

        return this.processExportResult(result, request, historyRecord);
    }



    /**
     * Instantiates a new entry in the history of the given request.
     *
     * @param request the request that is currently exported
     * @return the created request history record, or <code>null</code> if the operation failed
     */
    private RequestHistoryRecord createHistoryRecord(final Request request) {
        assert request != null : "The request cannot be null";

        RequestHistoryRepository repository = this.repositories.getRequestHistoryRepository();

        RequestHistoryRecord exportRecord = new RequestHistoryRecord();
        exportRecord.setRequest(request);
        exportRecord.setStartDate(new GregorianCalendar());
        exportRecord.setStatus(RequestHistoryRecord.Status.ONGOING);
        exportRecord.setStep(repository.findByRequestOrderByStep(request).size() + 1);
        exportRecord.setProcessStep(this.getExportProcessStep(request));
        // TODO Ne pas passer par l'objet EmailSettings
        exportRecord.setTaskLabel(this.emailSettings.getMessageString("requestHistory.tasks.export.label"));
        exportRecord.setUser(this.repositories.getUsersRepository().getSystemUser());

        return repository.save(exportRecord);
    }



    /**
     * Erases the folder that contains the data related to the processing of an order.
     *
     * @param request the order whose data must be deleted
     * @return <code>true</code> if all the data for the order have been deleted.
     */
    private boolean deleteRequestDataFolder(final Request request) {
        this.logger.debug("Deleting the data folder for request {}.", request.getId());

        return FileSystemUtils.purgeRequestFolders(request, this.basePath);
    }



    /**
     * Obtains the number that identifies the step where the export task takes place in the process
     * associated the given request.
     *
     * @param request the request that is being exported
     * @return the export step number in the process
     */
    private int getExportProcessStep(final Request request) {
        assert request != null : "The request must not be null";
        assert request.getProcess() != null : "The request must be associated to a process.";

        TasksRepository tasksRepository = this.repositories.getTasksRepository();

        return tasksRepository.findByProcessOrderByPosition(request.getProcess()).length + 1;
    }



    /**
     * Builds an error message from the result of an export with the details if they are included.
     *
     * @param result the export result object returned by the connector plugin
     * @return a string that contains the error message
     */
    private String getResultErrorMessage(final IExportResult result) {
        assert result != null : "The result must not be null.";
        assert !result.isSuccess() : "The result must be an error.";

        if (StringUtils.isEmpty(result.getErrorDetails())) {
            return result.getResultMessage();
        }

        return String.format("%s - %s", result.getResultMessage(), result.getErrorDetails());
    }



    /**
     * Updates the request and the export item in the request history to reflect the result of the export.
     *
     * @param result        the result of the export as returned by the connector plugin
     * @param request       the request that was exported
     * @param historyRecord the entry in the request history that tracks the export
     * @return the updated request (not saved)
     */
    private Request processExportResult(final IExportResult result, final Request request,
            final RequestHistoryRecord historyRecord) {
        assert request != null : "The request cannot be null.";
        assert historyRecord != null : "The request history record cannot be null.";
        assert request.equals(historyRecord.getRequest()) : "The history request must be related to the given request.";

        final int requestId = request.getId();
        Calendar endDate = new GregorianCalendar();

        if (result == null) {
            this.logger.warn("The connector plugin returned a null result for request {}.", requestId);
            this.sendEmailNotification(request, "Empty result returned", endDate);
            historyRecord.setToError("Empty result returned", endDate);
            request.setStatus(Request.Status.EXPORTFAIL);

        } else if (result.isSuccess()) {
            this.logger.info("Request {} has been correctly exported.", requestId);
            historyRecord.setStatus(RequestHistoryRecord.Status.FINISHED);
            historyRecord.setEndDate(endDate);
            request.setStatus(Request.Status.FINISHED);
            request.setEndDate(endDate);
            this.logger.debug("Deleting the data folder for request {}.", requestId);

            if (!this.deleteRequestDataFolder(request)) {
                this.logger.warn("Some files in the data folder for request {} could not be deleted.", requestId);
            }

        } else {
            this.logger.info("Request {} export has failed with message \"{}\". The details of the error are \"{}\"",
                    requestId, result.getResultMessage(), result.getErrorDetails());
            this.sendEmailNotification(request, result, endDate);
            historyRecord.setToError(this.getResultErrorMessage(result));
            request.setStatus(Request.Status.EXPORTFAIL);
        }

        this.repositories.getRequestHistoryRepository().save(historyRecord);

        this.logger.debug("Request status is {}.", request.getStatus());

        return request;
    }



    /**
     * Notifies the administrator by e-mail that the export failed.
     *
     * @param request   the request that could not be exported
     * @param result    the result of the export as returned by the connector plugin
     * @param errorDate when the export failure occurred
     */
    private void sendEmailNotification(final Request request, final IExportResult result, final Calendar errorDate) {
        assert result != null : "The result object must not be null.";
        assert !result.isSuccess() : "This e-mail notification is sent if the export failed.";

        this.sendEmailNotification(request, this.getResultErrorMessage(result), errorDate);
    }



    /**
     * Notifies the operators by e-mail that the export failed.
     *
     * @param request       the request that could not be exported
     * @param resultMessage the string that explain why the export failed
     * @param errorDate     when the export failure occurred
     */
    private void sendEmailNotification(final Request request, final String resultMessage, final Calendar errorDate) {
        assert request != null : "The request cannot be null.";
        assert request.getConnector() != null : "The request connector cannot be null.";
        assert resultMessage != null : "The result error message cannot be null.";

        this.logger.debug("Sending an e-mail notification to the administrators.");
        final RequestExportFailedEmail message = new RequestExportFailedEmail(this.emailSettings);
        final int processId = request.getProcess().getId();
        final String[] operatorsAddresses
                = this.repositories.getProcessesRepository().getProcessOperatorsAddresses(processId);
        final List<String> recipientsAddresses
                = new ArrayList<>(Arrays.asList(operatorsAddresses));

        for (String adminAddress : this.repositories.getUsersRepository().getActiveAdministratorsAddresses()) {

            if (adminAddress == null || recipientsAddresses.contains(adminAddress)) {
                continue;
            }

            recipientsAddresses.add(adminAddress);
        }

        if (!message.initialize(request, resultMessage, errorDate, recipientsAddresses.toArray(new String[]{}))) {
            this.logger.error("Could not create the request export failure message.");
            return;
        }

        if (message.send()) {
            this.logger.info("The request export failure notification was successfully sent to the operators.");

        } else {
            this.logger.warn("The request export failure notification was not sent.");
        }
    }

}
