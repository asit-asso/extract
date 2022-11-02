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
package ch.asit_asso.extract.web.model;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import ch.asit_asso.extract.utils.ExtractSimpleTemporalSpanFormatter;
import ch.asit_asso.extract.utils.SimpleTemporalSpan;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.domain.comparators.RequestHistoryRecordByStepComparator;
import ch.asit_asso.extract.domain.converters.JsonToParametersValuesConverter;
import ch.asit_asso.extract.exceptions.BaseFolderNotFoundException;
import ch.asit_asso.extract.persistence.RequestHistoryRepository;
import ch.asit_asso.extract.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;



/**
 * The representation of a customer order for a view.
 *
 * @author Yves Grasset
 */
public class RequestModel {

    /**
     * The string that identifies the localized label of an order export task.
     */
    private static final String EXPORT_TASK_LABEL_KEY = "requestHistory.tasks.export.label";

    /**
     * The string that identifies the localized label of an order whose processing is done.
     */
    private static final String FINISHED_PROCESS_LABEL_KEY = "requestHistory.tasks.done.label";

    /**
     * The string that identifies the localized label of an order whose processing has been denied.
     */
    private static final String REJECTED_PROCESS_LABEL_KEY = "requestHistory.tasks.rejected.label";

    /**
     * The string that identifies the localized label of an order import task.
     */
    private static final String IMPORT_TASK_LABEL_KEY = "requestHistory.tasks.import.label";

    /**
     * The string that identifies the localized label of a task whose original name is not known.
     */
    private static final String UNKNOWN_TASK_LABEL_KEY = "requestHistory.tasks.unknown.label";

    /**
     * The number indicating which task in the order process is currently active or was last executed if
     * the process is not running.
     */
    private int currentProcessStep;

    /**
     * An array that contains all the elements tracing the tasks that have been executed to process the
     * current order, including reruns.
     */
    private final RequestHistoryRecord[] fullHistory;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(RequestModel.class);

    /**
     * The access to the localized application strings.
     */
    private final MessageSource messageSource;

    /**
     * An array containing the representation of the files that have been created during the processing of
     * the order.
     */
    private FileModel[] outputFiles;

    /**
     * An array containing one entry for each task of the process that is (or was) executed for this order.
     * The status of the task reflects the last run of the process, or the last run of the task if it has been
     * relaunched.
     */
    private RequestHistoryRecord[] processHistory;

    /**
     * The order that this model represents.
     */
    private final Request request;

    /**
     * The non-standard configuration data for the order.
     */
    private Map<String, String> requestParameters = null;

    /**
     * The object that converts time spans to a localized string.
     */
    private ExtractSimpleTemporalSpanFormatter temporalSpanFormatter = null;

    /**
     * The path of the folder the data produced by the order that this model represents.
     */
    private final Path outputFolderPath;



    /**
     * Creates a new instance of the request model.
     *
     * @param domainRequest           the order that this model must represent
     * @param historyRecords          an array that contains all the elements tracing the tasks that have been executed
     *                                to process the current order, including reruns
     * @param requestBaseFolder       the path of the folder the data produced by all the order processes
     * @param localizedMessagesSource the access to the localized application strings
     */
    public RequestModel(final Request domainRequest, final RequestHistoryRecord[] historyRecords,
            final Path requestBaseFolder, final MessageSource localizedMessagesSource) {

        if (domainRequest == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        if (historyRecords == null) {
            throw new IllegalArgumentException("The request history cannot be null.");
        }

        if (requestBaseFolder == null || !requestBaseFolder.isAbsolute()) {
            throw new IllegalArgumentException("The base folder for the requests data must be set and absolute.");
        }

        if (localizedMessagesSource == null) {
            throw new IllegalArgumentException("The message source cannot be null.");
        }

        this.request = domainRequest;
        this.outputFolderPath = (domainRequest.getFolderOut() != null)
                ? requestBaseFolder.resolve(domainRequest.getFolderOut()) : null;
        this.messageSource = localizedMessagesSource;
        this.fullHistory = historyRecords;
        Arrays.sort(this.fullHistory, new RequestHistoryRecordByStepComparator());
        this.currentProcessStep = (!ArrayUtils.isEmpty(this.fullHistory))
                ? this.fullHistory[this.fullHistory.length - 1].getProcessStep() : -1;

        this.processHistory = this.buildProcessHistory();
        this.logger.debug("The process history contains {} items.", this.processHistory.length);
    }



    /**
     * Obtains the connector that imported this request.
     *
     * @return the connector
     */
    public final Connector getConnector() {
        return this.request.getConnector();
    }



    /**
     * Obtains the step number of the task that was last executed (or is currently active, if any) in the
     * process.
     *
     * @return the process step number
     */
    public final int getCurrentProcessStep() {
        return this.currentProcessStep;
    }



    /**
     * Obtains the message that was returned by the last executed task.
     *
     * @return the current task message, or <code>null</code> if there is not any
     */
    public final String getCurrentStepMessage() {
        final RequestHistoryRecord currentStep = this.getCurrentStep();

        if (currentStep == null) {
            return "";
        }

        return currentStep.getMessage();
    }



    /**
     * Obtains the label of the task that is currently executing or that executed last.
     *
     * @return the string that describes the current step
     */
    public final String getCurrentStepName() {
        Request.Status status = this.request.getStatus();

        if (status == Request.Status.IMPORTED || status == Request.Status.UNMATCHED) {
            return this.messageSource.getMessage(RequestModel.IMPORT_TASK_LABEL_KEY, null, Locale.getDefault());
        }

        if (status == Request.Status.FINISHED) {
            final String key = (this.isRejected())
                    ? RequestModel.REJECTED_PROCESS_LABEL_KEY : RequestModel.FINISHED_PROCESS_LABEL_KEY;
            return this.messageSource.getMessage(key, null, Locale.getDefault());
        }

        final RequestHistoryRecord currentStep = this.getCurrentStep();

        if (currentStep == null) {

            if (status == Request.Status.TOEXPORT) {
                return this.messageSource.getMessage(RequestModel.EXPORT_TASK_LABEL_KEY, null, Locale.getDefault());
            }

            return this.messageSource.getMessage(RequestModel.UNKNOWN_TASK_LABEL_KEY, null, Locale.getDefault());
        }

        return currentStep.getTaskLabel();
    }



    /**
     * Obtains the string that identifies who placed the order.
     *
     * @return a string that contains the customer GUID
     */
    public final String getCustomerGuid() {
        return this.request.getClientGuid();
    }



    /**
     * Obtains the name of who placed the order.
     *
     * @return a string that contains the customer information
     */
    public final String getCustomerName() {
        return this.request.getClient();
    }



    /**
     * Obtains the detailed information about who placed the order.
     *
     * @return a string that contains the customer information
     */
    public final String getCustomerDetails() {
        return this.request.getClientDetails();
    }



    /**
     * Obtains the address that provides an access to the details of this order on the originating server.
     *
     * @return the URL of the order on the source server, or <code>null</code> if there is no such URL
     */
    public final String getExternalUrl() {
        return this.request.getExternalUrl();
    }



    /**
     * Obtains all the records tracing the tasks executed to process this order, including reruns.
     *
     * @return an array that contains all the history records for this order
     */
    public final RequestHistoryRecord[] getFullHistory() {
        return this.fullHistory;
    }



    /**
     * Obtains the number that identifies this order.
     *
     * @return the order identifier
     */
    public final int getId() {
        return this.request.getId();
    }



    /**
     * Gets a string that describe this order and product.
     *
     * @return the label
     */
    public final String getLabel() {
        return String.format("%s / %s", this.getOrderLabel(), this.getProductLabel());
    }



    /**
     * Obtains the string that describes the order that this request is part of.
     *
     * @return the order label
     */
    public final String getOrderLabel() {
        return this.request.getOrderLabel();
    }



    /**
     * Obtains the name of the organization for which this order was placed.
     *
     * @return the organization name
     */
    public final String getOrganism() {
        return this.request.getOrganism();
    }



    /**
     * Obtains the identifying string of the organization for which this order was placed.
     *
     * @return the organization guid
     */
    public final String getOrganismGuid() {
        return this.request.getOrganismGuid();
    }



    /**
     * Obtains a description of each file produced during the processing of this order.
     *
     * @return an array that contains the output files descriptions
     */
    public final FileModel[] getOutputFiles() {

        if (this.outputFiles == null) {
            this.outputFiles = this.readOutputFiles();
        }

        return this.outputFiles;
    }



    /**
     * Obtains the non-standard parameters of the request.
     *
     * @return a map with the parameter name as key
     */
    public final Map<String, String> getParameters() {

        if (this.requestParameters == null) {
            JsonToParametersValuesConverter converter = new JsonToParametersValuesConverter();
            this.requestParameters = converter.convertToEntityAttribute(this.request.getParameters());
        }

        return this.requestParameters;
    }



    /**
     * Obtains the geographical perimeter of this order.
     *
     * @return a string that contains the order perimeter as a WKT geometry
     */
    public final String getPerimeterGeometry() {
        return this.request.getPerimeter();
    }



    /**
     * Obtains the last status of each task in the process that was run with this order.
     *
     * @return an array that contains an history entry for each task in the process
     */
    public final RequestHistoryRecord[] getProcessHistory() {
        return this.processHistory;
    }



    /**
     * Obtains the number that identifies the process associated to this order.
     *
     * @return the process identifier, or <code>null</code> if none is associated (unmatched request or process
     *         deleted, for example).
     */
    public final Integer getProcessId() {
        final Process process = this.request.getProcess();

        if (process == null) {
            return null;
        }

        return process.getId();
    }



    /**
     * Obtains the string that identifies the process associated to this order.
     *
     * @return the process name, or <code>null</code> if none is associated (unmatched request or process deleted,
     *         for example).
     */
    public final String getProcessName() {
        final Process process = this.request.getProcess();

        if (process == null) {
            return "";
        }

        return process.getName();
    }



    /**
     * Obtains the string that identifies the product that was ordered.
     *
     * @return the product GUID
     */
    public final String getProductGuid() {
        return this.request.getProductGuid();
    }



    /**
     * Obtains the string that describes the product that was ordered.
     *
     * @return the product label
     */
    public final String getProductLabel() {
        return this.request.getProductLabel();
    }



    /**
     * Obtains the string that was last entered by an operator to comment on the result of this order.
     *
     * @return the remark, or <code>null</code> if none was entered
     */
    public final String getRemark() {
        return this.request.getRemark();
    }



    /**
     * Obtains when this request started.
     *
     * @return the start date
     */
    public final Calendar getStartDate() {
        return this.request.getStartDate();
    }



    /**
     * Obtains when this request started in a purely numerical form.
     *
     * @return the start date timestamp
     */
    public final long getStartDateTimestamp() {
        return this.request.getStartDate().getTimeInMillis();
    }



    /**
     * Obtains a localized string that describe how much time passed since this request started.
     *
     * @return the start date span string
     */
    public final String getStartDateSpanToNow() {
        return this.getTimeSpanStringTo(this.getStartDate(), new GregorianCalendar());
    }



    /**
     * Obtains a localized string that describe how much time passed between the request start and a given
     * point in time.
     *
     * @param laterDate the point in time to compare the request start date to. This must be at a later point that the
     *                  start date
     * @return the start date span string
     */
    public final String getStartDateSpanTo(final Calendar laterDate) {

        if (laterDate == null) {
            throw new IllegalArgumentException("The date to compare the start date to cannot be null.");
        }

        return this.getTimeSpanStringTo(this.getStartDate(), laterDate);
    }



    /**
     * Obtains the surface of the area for this order.
     *
     * @return the surface in square meters
     */
    public final Double getSurface() {
        return this.request.getSurface();
    }



    /**
     * Obtains when the current task for this request ended if it is stopped or started if it is running.
     *
     * @return the task date
     */
    public final Calendar getTaskDate() {

        final RequestHistoryRecord currentStep = this.getCurrentStep();

        if (currentStep == null) {
            return this.getStartDate();
        }

        if (currentStep.getEndDate() == null) {
            return currentStep.getStartDate();
        }

        return currentStep.getEndDate();
    }



    /**
     * Obtains a localized string that describe how much time passed since the current task for this
     * request ended if it is stopped or started if it is running.
     *
     * @return the task date span string
     */
    public final String getTaskDateSpanToNow() {
        return this.getTimeSpanStringTo(this.getTaskDate(), new GregorianCalendar());
    }



    /**
     * Obtains the difference between the last task date for this request and a given date.
     *
     * @param laterDate the date to compare the task date to
     * @return the difference as a time span
     */
    public final String getTaskDateSpanTo(final Calendar laterDate) {

        if (laterDate == null) {
            throw new IllegalArgumentException("The date to compare the task date to cannot be null.");
        }

        return this.getTimeSpanStringTo(this.getTaskDate(), laterDate);
    }



    /**
     * Obtains detailed information about who this order was placed on behalf of, if any.
     *
     * @return a string that contains the information about the third party, or <code>null</code> if the order was
     *         not placed on behalf of anybody
     */
    public final String getThirdPartyDetails() {
        return this.request.getTiersDetails();
    }



    /**
     * Obtains the name of who this order was placed on behalf of, if any.
     *
     * @return a string that contains the name of the third party, or <code>null</code> if the order was
     *         not placed on behalf of anybody
     */
    public final String getThirdPartyName() {
        return this.request.getTiers();
    }



    /**
     * Obtains whether the result for this order failed to be sent back to the server.
     *
     * @return <code>true</code> if the result export resulted in an error
     */
    public final boolean isExportInError() {
        return (this.request.getStatus() == Request.Status.EXPORTFAIL);
    }



    /**
     * Obtains whether this order completed successfully.
     *
     * @return <code>true</code> if the result export succeeded
     */
    public final boolean isFinished() {
        return (this.request.getStatus() == Request.Status.FINISHED);
    }



    /**
     * Obtains whether the processing of this order is currently stop because of an error of some sort.
     *
     * @return <code>true</code> if the request is in an error state
     */
    public final boolean isInError() {
        return (this.request.getStatus() == Request.Status.IMPORTFAIL
                || this.request.getStatus() == Request.Status.UNMATCHED
                || this.request.getStatus() == Request.Status.ERROR
                || this.request.getStatus() == Request.Status.EXPORTFAIL);
    }



    public final boolean isImportFail() {
        return this.request.getStatus() == Request.Status.IMPORTFAIL;
    }



    /**
     * Obtains whether the processing of this order is currently stopped to wait for a validation by
     * an operator.
     *
     * @return <code>true</code> if the request is in standby
     */
    public final boolean isInStandby() {
        return (this.request.getStatus() == Request.Status.STANDBY);
    }



    /**
     * Obtains whether this order has been marked as impossible to process.
     *
     * @return <code>true</code> if an operator rejected this order
     */
    public final boolean isRejected() {
        return this.request.isRejected();
    }



    /**
     * Obtains whether the processing of this order is currently stop because a task failed.
     *
     * @return <code>true</code> if the current task failed
     */
    public final boolean isTaskInError() {
        return (this.request.getStatus() == Request.Status.ERROR);
    }



    /**
     * Obtains whether this order did not match any processing rule.
     *
     * @return <code>true</code> if the process rule matching failed
     */
    public final boolean isUnmatched() {
        return (this.request.getStatus() == Request.Status.UNMATCHED);
    }



    /**
     * Obtains whether the processing of this order is currently stopped and is not completed.
     *
     * @return <code>true</code> if an intervention by an operator is needed to allow the request to complete
     */
    public final boolean isWaitingIntervention() {
        final RequestHistoryRecord currentStep = this.getCurrentStep();

        if (currentStep == null) {
            return (this.request.getStatus() == Request.Status.IMPORTFAIL
                    || this.request.getStatus() == Request.Status.UNMATCHED
                    || this.request.getStatus() == Request.Status.EXPORTFAIL);
        }

        final RequestHistoryRecord.Status currentTaskStatus = currentStep.getStatus();

        return (currentTaskStatus == RequestHistoryRecord.Status.ERROR
                || currentTaskStatus == RequestHistoryRecord.Status.STANDBY);
    }



    /**
     * Converts an ensemble of request data objects into request models.
     *
     * @param requestsCollection a collection that contains the request data objects to convert
     * @param historyRepository  the Spring Data object that links the request history entry data objects with the data
     *                           source
     * @param basePath           a string that contains the absolute path of the folder containing the data for all the
     *                           requests
     * @param messageSource      the access to the localized application strings
     * @return an array that contains the request model for each request data object
     * @throws BaseFolderNotFoundException if the path defined as the base folder for the requests data could not be
     *                                     accessed
     */
    public static final RequestModel[] fromDomainRequestsCollection(final Collection<Request> requestsCollection,
            final RequestHistoryRepository historyRepository, final String basePath,
            final MessageSource messageSource) {

        if (requestsCollection == null) {
            throw new IllegalArgumentException("The domain requests collection cannot be null.");
        }

        if (historyRepository == null) {
            throw new IllegalArgumentException("The request history repository cannot be null.");
        }

        if (StringUtils.isBlank(basePath)) {
            throw new IllegalArgumentException("The request data folder base path cannot be empty.");
        }

        final File baseFolder = new File(basePath);

        if (!baseFolder.exists() || !baseFolder.canRead() || !baseFolder.isDirectory()) {
            throw new BaseFolderNotFoundException("The target of the request data base folder path is not accessible"
                    + " or is not a directory.");
        }

        if (messageSource == null) {
            throw new IllegalArgumentException("The message source cannot be null.");
        }

        final Path baseFolderPath = baseFolder.toPath();
        List<RequestModel> modelsList = new ArrayList<>();
        List<RequestHistoryRecord> historyRecords = new ArrayList<>();

        for (Request domainRequest : requestsCollection) {
            historyRecords.clear();
            historyRecords.addAll(historyRepository.findByRequestOrderByStep(domainRequest));
            modelsList.add(new RequestModel(domainRequest, historyRecords.toArray(new RequestHistoryRecord[]{}),
                    baseFolderPath, messageSource));
        }

        return modelsList.toArray(new RequestModel[]{});
    }



    /**
     * Converts an ensemble of request data objects into request models.
     *
     * @param requestsPage      a subset of the found requests for a paged result
     * @param historyRepository the Spring Data object that links the request history entry data objects with the data
     *                          source
     * @param basePath          a string that contains the absolute path of the folder containing the data for all the
     *                          requests
     * @param messageSource     the access to the localized application strings
     * @return an array that contains the request model for each request data object
     */
    public static final RequestModel[] fromDomainRequestsPage(final Page<Request> requestsPage,
            final RequestHistoryRepository historyRepository, final String basePath,
            final MessageSource messageSource) {

        if (requestsPage == null) {
            throw new IllegalArgumentException("The domain requests page cannot be null.");
        }

        if (historyRepository == null) {
            throw new IllegalArgumentException("The request history repository cannot be null.");
        }

        if (StringUtils.isBlank(basePath)) {
            throw new IllegalArgumentException("The request data folder base path cannot be empty.");
        }

        final File baseFolder = new File(basePath);

        if (!baseFolder.exists() || !baseFolder.canRead() || !baseFolder.isDirectory()) {
            throw new BaseFolderNotFoundException("The target of the request data base folder path is not accessible"
                    + " or is not a directory.");
        }

        if (messageSource == null) {
            throw new IllegalArgumentException("The message source cannot be null.");
        }

        final Path baseFolderPath = baseFolder.toPath();
        List<RequestModel> modelsList = new ArrayList<>();
        List<RequestHistoryRecord> historyRecords = new ArrayList<>();

        for (Request domainRequest : requestsPage) {
            historyRecords.clear();
            historyRecords.addAll(historyRepository.findByRequestOrderByStep(domainRequest));
            modelsList.add(new RequestModel(domainRequest, historyRecords.toArray(new RequestHistoryRecord[]{}),
                    baseFolderPath, messageSource));
        }

        return modelsList.toArray(new RequestModel[]{});
    }



    /**
     * Reads the files in a directory and adds their description to the given list.
     *
     * @param folder    the directory to parse for files
     * @param filesList the list of file descriptions to add the directory files to
     * @param recursive <code>true</code> to also parse the eventual subdirectories
     */
    private void addFolderFilesToList(final File folder, final List<FileModel> filesList, final boolean recursive) {
        assert folder.isDirectory() : "The file passed as a folder is not a folder.";
        assert folder.exists() && folder.canRead() : "The folder is not accessible.";

        for (File folderFile : folder.listFiles()) {

            if (folderFile.isDirectory() && recursive) {
                this.addFolderFilesToList(folderFile, filesList, true);
                continue;
            }

            if (!folderFile.isFile()) {
                continue;
            }

            Path filePath = folderFile.toPath();
            filesList.add(new FileModel(this.getFileIdentifierString(filePath), folderFile.getName()));
        }
    }



    /**
     * Creates a string that identifies an output file for this order for a later retrieval.
     *
     * @param filePath the path of the output file
     * @return the string identifier
     */
    private String getFileIdentifierString(final Path filePath) {
        assert this.outputFolderPath != null : "The output folder path cannot be null.";
        assert filePath != null && filePath.isAbsolute() && filePath.startsWith(this.outputFolderPath) :
                "The file path is invalid.";

        final String relativePath = this.outputFolderPath.relativize(filePath).toString();

        if (SystemUtils.IS_OS_WINDOWS) {
            return relativePath.replace("\\", "/");
        }

        return relativePath;
    }



    /**
     * Obtains a localized string that describe how much time passed between two points in time.
     *
     * @param earlierDate the point in time that occurred first
     * @param laterDate   the point in time that occurred last
     * @return the span string
     */
    private String getTimeSpanStringTo(final Calendar earlierDate, final Calendar laterDate) {
        assert earlierDate != null : "The earlier date cannot be null.";
        assert laterDate != null : "The later date cannot be null.";

        if (laterDate.before(earlierDate)) {
            throw new IllegalArgumentException("The earlier date is set to a later point in time than the later date.");
        }

        final SimpleTemporalSpan span = DateTimeUtils.getFloorDifference(earlierDate, laterDate);

        return this.getTemporalSpanFormatter().format(span);
    }



    /**
     * Completes the process history for this order by adding entries for the steps that are missing. This
     * method is meant to be used when parsing the full history backwards.
     *
     * @param previousProcessStep the number that identifies the process step of the previously examined history entry
     * @param historyRecordStep   the number that identifies the process step of the currently examined history entry.
     *                            It must be smaller than the <code>previousProcessStep</code> parameter
     * @param historyRecords      the array that contains the entries tracing the tasks of the process associated to
     *                            this order
     */
    private void addMissingSteps(final int previousProcessStep, final int historyRecordStep,
            final RequestHistoryRecord[] historyRecords) {
        assert previousProcessStep >= 0 :
                "The process step of the previously examined history entry cannot be negative.";
        assert historyRecordStep > 0 :
                "The process step of the currently examined history entry must be greater than 0.";
        assert previousProcessStep > historyRecordStep :
                "The previously examined step must be larger than the current one.";

        this.logger.debug("Process steps between {} and {} have been skipped.",
                historyRecordStep, previousProcessStep);

        for (int missingStep = historyRecordStep + 1; missingStep < previousProcessStep; missingStep++) {

            if (historyRecords[missingStep - 1] != null) {
                this.logger.debug("An task history entry already exists for step {}.", missingStep);
                continue;
            }

            this.logger.debug("Adding a pseudo-entry with an (Unknown) task label to the process history"
                    + " for step {}.", missingStep);
            final String unknownLabel = this.messageSource.getMessage(RequestModel.UNKNOWN_TASK_LABEL_KEY, null,
                    Locale.getDefault());

            historyRecords[missingStep - 1] = this.createHistoryPseudoEntry(unknownLabel, missingStep);
        }
    }



    /**
     * Completes the process history for this order by adding entries for the steps that did not have any
     * entry in the full history. These should be tasks that either have been skipped or that have yet to be run.
     *
     * @param tasksHistory an array that contains an entry for each task of the process that was present in the full
     *                     history
     * @param processTasks an array that contains the tasks that make up the process associated with this order sorted
     *                     by step, or <code>null</code> if the processing is finished (because the original process
     *                     may have been modified since then)
     * @return an array that contains the completed process history
     */
    private RequestHistoryRecord[] addMissingTasksToProcessHistory(final RequestHistoryRecord[] tasksHistory,
            final Task[] processTasks) {
        this.logger.debug("Adding entries to the process history for the tasks that are missing in the history.");

        for (int historyIndex = 0; historyIndex < tasksHistory.length; historyIndex++) {
            this.logger.debug("Fetching the task for step {}.", historyIndex + 1);
            String taskLabel = this.messageSource.getMessage(RequestModel.UNKNOWN_TASK_LABEL_KEY, null,
                    Locale.getDefault());
            RequestHistoryRecord.Status taskStatus = RequestHistoryRecord.Status.SKIPPED;

            if (tasksHistory[historyIndex] != null) {
                this.logger.debug("The task has an history entry.");
                continue;
            }

            if (historyIndex == 0) {
                this.logger.debug("The task is the import task.");
                taskLabel = this.messageSource.getMessage(RequestModel.IMPORT_TASK_LABEL_KEY, null,
                        Locale.getDefault());
                taskStatus = RequestHistoryRecord.Status.FINISHED;

            } else if (processTasks != null) {

                if (historyIndex == tasksHistory.length - 1) {
                    this.logger.debug("The task is the export task.");
                    taskLabel = this.messageSource.getMessage(RequestModel.EXPORT_TASK_LABEL_KEY, null,
                            Locale.getDefault());

                } else {
                    Task task = processTasks[historyIndex - 1];

                    if (task != null) {
                        taskLabel = task.getLabel();
                        this.logger.debug("The task has been found. Its label is {}.", taskLabel);

                    } else {
                        this.logger.error("Cannot find a task at step {} in the process {} when building the history"
                                + " for request {}.", historyIndex + 1, this.request.getProcess().getName(),
                                this.request.getId());
                    }
                }
            }

            this.logger.debug("Adding a pseudo-record to the process history.");
            tasksHistory[historyIndex] = this.createHistoryPseudoEntry(taskLabel, historyIndex, taskStatus);
        }

        return tasksHistory;
    }



    /**
     * Creates a collection of entries tracing the tasks that make up the process used to treat this order
     * and the status of their last run.
     *
     * @return an array that contains an entry for each task of the process for this order
     */
    private RequestHistoryRecord[] buildProcessHistory() {
        this.logger.debug("Building the process history for request {}.", this.request.getId());

        if (request.getStatus() == Request.Status.FINISHED) {
            return this.buildFinishedRequestProcessHistory();
        }

        return this.buildActiveRequestHistory();
    }



    /**
     * Creates a collection of entries tracing the tasks that make up the process associated to this order
     * and the status of their last run.
     *
     * @return an array that contains an entry for each task of the process for this order
     */
    private RequestHistoryRecord[] buildActiveRequestHistory() {
        this.logger.debug("Building the process history for a request that is still active.");
        final Process process = this.request.getProcess();

        if (process == null) {
            return new RequestHistoryRecord[]{};
        }

        final Task[] processTasks = process.getTasksCollection().toArray(new Task[]{});
        this.logger.debug("Found {} tasks in process.", processTasks.length);
        final RequestHistoryRecord[] tasksHistory = this.buildTaskHistoryStatus(processTasks.length + 1);

        return this.addMissingTasksToProcessHistory(tasksHistory, processTasks);
    }



    /**
     * Creates a collection of entries tracing the tasks that make up the process used to treat this order
     * and the status of their last run.
     *
     * @return an array that contains an entry for each task of the process for this order
     */
    private RequestHistoryRecord[] buildFinishedRequestProcessHistory() {
        this.logger.debug("Building the process history for a request that has successfully completed.");

        return this.addMissingTasksToProcessHistory(this.buildTaskHistoryStatus(), null);
    }



    /**
     * Creates a process task history from the full history of this order. It may be missing the task that have
     * been skipped or have yet to run.
     *
     * @return an array that contains one entry for each task of the process that has been run for this order
     */
    private RequestHistoryRecord[] buildTaskHistoryStatus() {
        return this.buildTaskHistoryStatus(null);
    }



    /**
     * Creates a process task history from the full history of this order. It may be missing the task that have
     * been skipped or have yet to run.
     *
     * @param numberOfTasks the number of tasks that the process must contain, or <code>null</code> if this is not
     *                      known (because the request has completed, for instance)
     * @return an array that contains one entry for each task of the process that has been run for this order
     */
    private RequestHistoryRecord[] buildTaskHistoryStatus(final Integer numberOfTasks) {
        this.logger.debug("Getting the status of the process tasks that have completed or are running.");
        final RequestHistoryRecord[] tasksStatus = (numberOfTasks != null)
                ? new RequestHistoryRecord[numberOfTasks + 1]
                : new RequestHistoryRecord[Math.max(this.currentProcessStep, 0) + 1];
        this.logger.debug("The total number of tasks in the process history (including future ones) is {}.",
                tasksStatus.length);
        int previousProcessStep = -1;

        for (int historyIndex = this.fullHistory.length - 1; historyIndex >= 0; historyIndex--) {
            this.logger.debug("Reading history item in position {}.", historyIndex);
            final RequestHistoryRecord historyRecord = this.fullHistory[historyIndex];
            final int historyRecordStep = historyRecord.getProcessStep();
            this.logger.debug("The process step for the currently-read record is {}.", historyRecordStep);
            final RequestHistoryRecord taskRecord = tasksStatus[historyRecordStep];

            if (taskRecord != null) {

                if (!RequestModel.UNKNOWN_TASK_LABEL_KEY.equals(taskRecord.getTaskLabel())) {
                    this.logger.debug("The record concerns a task that has been skipped later or has to be rerun.");
                    this.logger.debug("Setting the correct task label.");
                    taskRecord.setTaskLabel(historyRecord.getTaskLabel());

                } else {
                    this.logger.debug("The record concerns a task that has been rerun later.");
                }

                continue;
            }

            if (historyRecordStep > this.currentProcessStep) {
                this.logger.debug("The record concerns a task that has to be (re)run.");
                this.logger.debug("Adding a pseudo-entry to the process history for step {}.", historyRecordStep);
                tasksStatus[historyRecordStep]
                        = this.createHistoryPseudoEntry(historyRecord.getTaskLabel(), historyRecordStep);
                continue;
            }

            this.logger.debug("Adding history record at (total) step {} to the process step {}.",
                    historyRecord.getStep(), historyRecordStep);
            tasksStatus[historyRecordStep] = historyRecord;

            if ((previousProcessStep - historyRecordStep) > 1) {
                this.addMissingSteps(previousProcessStep, historyRecordStep, tasksStatus);
            }

            previousProcessStep = historyRecordStep;
        }

        return tasksStatus;
    }



    /**
     * Creates a placeholder entry for a skipped task that was not present in the full history of this
     * order.
     *
     * @param taskLabel   the string that identifies the task that this entry represents
     * @param processStep the number that identifies the stage of the process where the task takes place
     * @return the entry
     */
    private RequestHistoryRecord createHistoryPseudoEntry(final String taskLabel, final int processStep) {
        return this.createHistoryPseudoEntry(taskLabel, processStep, RequestHistoryRecord.Status.SKIPPED);
    }



    /**
     * Creates a placeholder entry for a task that was not present in the full history of this order.
     *
     * @param taskLabel   the string that identifies the task that this entry represents
     * @param processStep the number that identifies the stage of the process where the task takes place
     * @param status      the state of the task represented by this entry
     * @return the entry
     */
    private RequestHistoryRecord createHistoryPseudoEntry(final String taskLabel, final int processStep,
            final RequestHistoryRecord.Status status) {
        assert !StringUtils.isBlank(taskLabel) : "The task label cannot be empty.";
        assert processStep >= 0 : "The process step must not be negative.";

        this.logger.debug("Creating a pseudo-record for the task {}.", taskLabel);
        RequestHistoryRecord pseudoRecord = new RequestHistoryRecord();
        pseudoRecord.setId(Integer.MAX_VALUE);
        pseudoRecord.setProcessStep(processStep);
        pseudoRecord.setRequest(this.request);
        pseudoRecord.setTaskLabel(taskLabel);
        pseudoRecord.setStatus(status);

        return pseudoRecord;
    }



    /**
     * Obtains the history entry for the task that was last executed (or is currently executing, if any).
     *
     * @return the current task history entry, or <code>null</code> if there is no history entry for the current step
     */
    private RequestHistoryRecord getCurrentStep() {

        if (this.processHistory.length > 0) {

            for (int processHistoryIndex = this.currentProcessStep; processHistoryIndex >= 0; processHistoryIndex--) {
                RequestHistoryRecord processStep = this.processHistory[processHistoryIndex];

                if (processStep.getStatus() == RequestHistoryRecord.Status.SKIPPED) {
                    continue;
                }

                return processStep;
            }

            return this.processHistory[this.currentProcessStep];
        }

        if (this.fullHistory.length > 0) {
            return this.fullHistory[this.fullHistory.length - 1];
        }

        return null;
    }



    /**
     * Obtains the object that converts time spans to a localized string.
     *
     * @return the formatter
     */
    private ExtractSimpleTemporalSpanFormatter getTemporalSpanFormatter() {

        if (this.temporalSpanFormatter == null) {
            this.temporalSpanFormatter = new ExtractSimpleTemporalSpanFormatter(this.messageSource);
        }

        return this.temporalSpanFormatter;
    }



    /**
     * Creates of collection of objects describing the files generated by the processing of this order.
     *
     * @return an array that contains the description of each output file
     */
    private FileModel[] readOutputFiles() {

        if (this.outputFolderPath == null) {
            return new FileModel[]{};
        }

        List<FileModel> filesList = new ArrayList<>();
        File outputFolder = this.outputFolderPath.toFile();

        if (!outputFolder.exists() || !outputFolder.canRead() || !outputFolder.isDirectory()) {
            this.logger.warn("Cannot read the output folder {} for request {}.", outputFolder.getAbsolutePath(),
                    this.request.getId());
            return new FileModel[]{};
        }

        this.addFolderFilesToList(outputFolder, filesList, true);

        return filesList.toArray(new FileModel[]{});
    }

}
