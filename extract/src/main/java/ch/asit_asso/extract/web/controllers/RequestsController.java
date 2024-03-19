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
package ch.asit_asso.extract.web.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Remark;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.orchestrator.OrchestratorSettings;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.RemarkRepository;
import ch.asit_asso.extract.persistence.RequestHistoryRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.persistence.TasksRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.utils.FileSystemUtils;
import ch.asit_asso.extract.utils.FileSystemUtils.RequestDataFolder;
import ch.asit_asso.extract.utils.ZipUtils;
import ch.asit_asso.extract.web.Message;
import ch.asit_asso.extract.web.Message.MessageType;
import ch.asit_asso.extract.web.model.RequestModel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Processes the web requests related to the orders operations.
 *
 * @author Yves Grasset
 */
@Controller
@Scope("session")
@RequestMapping("/requests")
public class RequestsController extends BaseController {

    /**
     * The string that identifies the part of the web site that this controller manages.
     */
    private static final String CURRENT_SECTION_IDENTIFIER = "home";

    /**
     * The string that contains the path to the file defining a custom request details map configuration.
     */
    private static final String CUSTOM_MAP_DATA_PATH = "static/js/requestMap/map.custom.js";

    /**
     * The string that contains the name of the file defining the default request details map configuration.
     */
    private static final String DEFAULT_MAP_DATA_FILE_NAME = "map.js";

    /**
     * The string that identifies the view to use to display detailed information about a request.
     */
    private static final String DETAILS_VIEW = "requests/details";

    /**
     * The number to return as an HTTP status to indicate that the requested ressource cannot be found.
     */
    private static final int HTTP_NOT_FOUND_ERROR_CODE = 404;

    /**
     * The string that identifies tells this controller to redirect the user to the details of a request.
     * This must be used as a format to set the identifier of the request to display.
     */
    private static final String REDIRECT_TO_DETAILS_FORMAT = "redirect:/requests/%d";

    /**
     * The string that tells this controller to redirect the user to the requests list page.
     */
    private static final String REDIRECT_TO_LIST = "redirect:/requests";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(RequestsController.class);

    /**
     * The access to the localized application strings.
     */
    @Autowired
    private MessageSource messageSource;

    /**
     * The Spring Data object that links the system parameter data objects with the data source.
     */
    @Autowired
    private SystemParametersRepository parametersRepository;

    /**
     * The Spring Data object that links the process data objects with the data source.
     */
    @Autowired
    private ProcessesRepository processesRepository;

    /**
     * The Spring Data object that links the remark data objects with the data source.
     */
    @Autowired
    private RemarkRepository remarksRepository;

    /**
     * The Spring Data object that links the request data objects with the data source.
     */
    @Autowired
    private RequestsRepository requestsRepository;

    /**
     * The Spring Data object that links the request history entry data objects with the data source.
     */
    @Autowired
    private RequestHistoryRepository requestHistoryRepository;

    /**
     * The Spring Data object that links the task entry data objects with the data source.
     */
    @Autowired
    private TasksRepository tasksRepository;

    /**
     * The Spring Data object that links the user data objects with the data source.
     */
    @Autowired
    private UsersRepository usersRepository;



    /**
     * Processes a request to display detailed information about an order.
     *
     * @param itemId             the number that identifies the request to display
     * @param model              the data to display in the details view
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @GetMapping("{itemId}")
    public final String viewItem(@PathVariable final int itemId, final ModelMap model,
            final RedirectAttributes redirectAttributes) {
        this.logger.debug("Received a web request to display the details of request {}.", itemId);

        final Request request = this.getDomainRequest(itemId);

        if (request == null) {
            this.logger.error("No request found in database with the identifier {}.", itemId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notFound", MessageType.ERROR);

            return RequestsController.REDIRECT_TO_LIST;
        }

        if (!this.canCurrentUserViewRequestDetails(request)) {
            this.logger.warn("The user {} tried to access the details of request {} but is not allowed to view them.",
                    this.getCurrentUserLogin(), itemId);
            return REDIRECT_TO_ACCESS_DENIED;
        }

        this.addCurrentSectionToModel(RequestsController.CURRENT_SECTION_IDENTIFIER, model);
        this.addJavascriptMessagesAttribute(model);
        final RequestModel requestModel = new RequestModel(request,
                this.requestHistoryRepository.findByRequestOrderByStep(request).toArray(new RequestHistoryRecord[]{}),
                Paths.get(this.parametersRepository.getBasePath()), this.messageSource,
                this.parametersRepository.getValidationFocusProperties().split(","));

        model.addAttribute("request", requestModel);
        Task currentTask = this.getCurrentTask(requestModel);

        if (currentTask != null) {
            model.addAttribute("validationMessages", this.getValidationMessagesTemplates(currentTask));
            model.addAttribute("rejectionMessages", this.getRejectionMessagesTemplates(currentTask));
        }

        model.addAttribute("displayTempFolder",
                           "true".equals(this.parametersRepository.isTempFolderDisplayed()));

        String mapDataFileName = RequestsController.DEFAULT_MAP_DATA_FILE_NAME;
        Resource customMapResource = new ClassPathResource(RequestsController.CUSTOM_MAP_DATA_PATH);

        if (customMapResource.exists()) {
            mapDataFileName = customMapResource.getFilename();
        }

        model.addAttribute("mapDataFileName", mapDataFileName);
        OrchestratorSettings orchestratorSettings = new OrchestratorSettings(this.parametersRepository);
        model.addAttribute("orchestratorState", orchestratorSettings.getStateString());

        this.logger.debug("Displaying request details.");

        return RequestsController.DETAILS_VIEW;
    }



    @GetMapping(value = "getRemarkText", produces = "text/plain")
    @ResponseBody
    public final String getRemarkText(@RequestParam("id") int remarkId, @RequestParam int requestId,
                                      @RequestParam String remarkType, HttpServletResponse response) {

        this.logger.debug("Processing request to get the text of {} remark {} for request {}.",
                          remarkType, remarkId, requestId);
        Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("No request found in database with the identifier {}.", requestId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        if (!this.canCurrentUserViewRequestDetails(request)) {
            this.logger.warn("The user {} tried to access the text of a remark for request {} but is not allowed to view it.",
                    this.getCurrentUserLogin(), requestId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        final RequestModel requestModel = new RequestModel(request,
                this.requestHistoryRepository.findByRequestOrderByStep(request).toArray(new RequestHistoryRecord[]{}),
                Paths.get(this.parametersRepository.getBasePath()), this.messageSource,
                this.parametersRepository.getValidationFocusProperties().split(","));

        Task currentTask = this.getCurrentTask(requestModel);

        if (request.getStatus() != Request.Status.STANDBY && "VALIDATION".equals(currentTask.getCode())) {
            this.logger.warn("The user {} tried to access the text of a remark for request {} but the request is not "
                             + "in a state that allows the input of a remark template.", this.getCurrentUserLogin(),
                             requestId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        List<Integer> messagesIdsList;

        switch (remarkType) {

            case "rejection":
                messagesIdsList = currentTask.getRejectionMessagesTemplatesIds();
                break;

            case "validation":
                messagesIdsList = currentTask.getValidationMessagesTemplatesIds();
                break;

            default:
                this.logger.warn("The user {} tried to access the text of a remark with type {}, which is invalid.",
                        this.getCurrentUserLogin(), remarkType);
                return RequestsController.REDIRECT_TO_ACCESS_DENIED;
        }

        if (messagesIdsList == null || !messagesIdsList.contains(remarkId)) {
            this.logger.warn("The user {} tried to access the text of a remark {} for request {} but the remark is not allowed for this request.",
                    this.getCurrentUserLogin(), remarkId, requestId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        Optional<Remark> remark = this.remarksRepository.findById(remarkId);

        if (remark.isEmpty()) {
            this.logger.warn("The user {} tried to access the text of a remark {} but it could not be found.",
                    this.getCurrentUserLogin(), remarkId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        User operator = this.usersRepository.findByLoginIgnoreCase(this.getCurrentUserLogin());

        return remark.get().getContent()
                .replace("{operatorName}", operator.getName())
                .replace("{operatorEmail}", operator.getEmail());
    }




    private Task getCurrentTask(RequestModel currentRequest) {
        Integer processId = currentRequest.getProcessId();

        if (processId == null) {
            return null;
        }

        int currentStep = currentRequest.getCurrentProcessStep();

        if (currentStep <= 0) {
            return null;
        }

        return this.tasksRepository.findByProcessIdAndPosition(processId.intValue(), currentStep);
    }



    private Iterable<Remark> getRejectionMessagesTemplates(Task currentTask) {
        assert currentTask != null : "The current task cannot be null.";

        List<Integer> messagesIds = currentTask.getRejectionMessagesTemplatesIds();

        if (messagesIds == null || messagesIds.size() == 0) {
            return null;
        }

        return this.remarksRepository.findAllById(messagesIds);
    }



    private Iterable<Remark> getValidationMessagesTemplates(Task currentTask) {
        assert currentTask != null : "The current task cannot be null.";

        List<Integer> messagesIds = currentTask.getValidationMessagesTemplatesIds();

        if (messagesIds == null || messagesIds.size() == 0) {
            return null;
        }

        return this.remarksRepository.findAllById(messagesIds);
    }



    /**
     * Processes a request to display the state of all the order processes.
     *
     * @param model              the data to display in the view
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @GetMapping
    public final String viewList(final ModelMap model, final RedirectAttributes redirectAttributes) {
        this.logger.debug("Received a web request to display the list of requests.");
        Message statusMessage = (Message) model.get("statusMessage");

        if (statusMessage != null) {
            this.addStatusMessage(redirectAttributes, statusMessage.getMessageKey(), statusMessage.getMessageType());
        }

        return REDIRECT_TO_HOME;
    }



    /**
     * Processes a web request to obtain a file produced by the processing of an order.
     *
     * @param requestId  the number that identifies the order whose processing produced the file
     * @param fileString the path of the output path relative to the output folder
     * @param response   the HTTP response to this request
     * @return a file system resource wrapping the requested file, or <code>null</code> if the file cannot be served
     */
    @GetMapping(path = "{requestId}/getFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public final FileSystemResource viewRequestOutputFile(@PathVariable final int requestId,
            @RequestParam("file") final String fileString, final HttpServletResponse response) {
        this.logger.debug("Received a web request to display the output file \"{}\" for request {}.", fileString,
                requestId);

        if (StringUtils.isBlank(fileString)) {
            this.logger.error("The user {} requested a file with a blank relative path for request {}.",
                    this.getCurrentUserLogin(), requestId);
            response.setStatus(RequestsController.HTTP_NOT_FOUND_ERROR_CODE);
            return null;
        }

        final Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("No request found in database with the identifier {}.", requestId);
            response.setStatus(RequestsController.HTTP_NOT_FOUND_ERROR_CODE);
            return null;
        }

        final File outputFile = this.getRequestOutputFile(request, fileString);

        if (outputFile == null) {
            this.logger.debug("The file \"{}\" for request {} is not available.", fileString, requestId);
            response.setStatus(RequestsController.HTTP_NOT_FOUND_ERROR_CODE);
            return null;
        }

        response.setHeader("Content-Disposition", "attachment; filename=" + outputFile.getName());
        return new FileSystemResource(outputFile);
    }



    /**
     * Processes a web request to obtain an archive with all the files produced by the processing of
     * an order.
     *
     * @param requestId the number that identifies the order whose processing produced the file
     * @param response  the HTTP response to this request
     * @return a byte array resource wrapping the requested archive file, or <code>null</code> if the output content
     *         cannot be served
     */
    @GetMapping(path = "{requestId}/getAllFiles", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public final ByteArrayResource viewRequestOutputContent(@PathVariable final int requestId,
            final HttpServletResponse response) {
        this.logger.debug("Received a web request to display all the output files for request {}.", requestId);

        final Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("No request found in database with the identifier {}.", requestId);
            response.setStatus(RequestsController.HTTP_NOT_FOUND_ERROR_CODE);
            return null;
        }

        final byte[] outputFilesZip = this.getRequestOutputContent(request);
        final String outputZipName = String.format("%s.zip", request.getId());

        if (outputFilesZip == null) {
            this.logger.debug("The output content for request {} is not available.", requestId);
            response.setStatus(RequestsController.HTTP_NOT_FOUND_ERROR_CODE);
            return null;
        }

        response.setHeader("Content-Disposition", "attachment; filename=" + outputZipName);
        return new ByteArrayResource(outputFilesZip);
    }



    @PostMapping("{requestId}/addFiles")
    public final String handleAddOutputFiles(@PathVariable final int requestId,
            @RequestParam final MultipartFile[] filesToAdd, @RequestParam final int currentStep,
            final RedirectAttributes redirectAttributes) {

        this.logger.debug("Received a web request to add {} files \"{}\" to request {}.", filesToAdd.length, requestId);
        Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("The user {} attempted to add files to request {}, which does not exist.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notFound", MessageType.ERROR);

            return RequestsController.REDIRECT_TO_LIST;
        }

        if (!this.canCurrentUserChangeRequestOutput(request)) {
            this.logger.warn("The user {} tried to add output files to request {} but is not allowed to do so.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.outputChange.notAllowed",
                    MessageType.ERROR);

            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (!this.canRequestOutputBeChanged(request, currentStep, redirectAttributes)) {
            return RequestsController.REDIRECT_TO_LIST;
        }

        if (filesToAdd.length == 0) {
            this.logger.debug("The user {} sent an empty array of output files to add to request {}.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.addFiles.empty", MessageType.WARNING);

        } else {
            int addedFilesNumber = 0;

            for (MultipartFile uploadedFile : filesToAdd) {

                if (this.copyUploadedFileToOutputFolder(uploadedFile, request)) {
                    addedFilesNumber++;
                }
            }

            if (addedFilesNumber == 0) {
                this.addStatusMessage(redirectAttributes, "requestDetails.addFiles.failed", MessageType.ERROR);

            } else if (addedFilesNumber < filesToAdd.length) {
                this.addStatusMessage(redirectAttributes, "requestDetails.addFiles.partial", MessageType.WARNING);

            } else {
                this.addStatusMessage(redirectAttributes, "requestDetails.addFiles.success", MessageType.SUCCESS);
            }
        }

        return String.format(RequestsController.REDIRECT_TO_DETAILS_FORMAT, requestId);
    }



    /**
     * Processes a request to delete an order.
     *
     * @param requestId          the number that identifies the request to delete
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("{requestId}/delete")
    public final String handleDeleteRequest(@PathVariable final int requestId,
            final RedirectAttributes redirectAttributes) {

        this.logger.debug("Received a web request to delete request {}.", requestId);
        final Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("The user {} attempted to delete request {}, which does not exist.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notFound", MessageType.ERROR);

            return RequestsController.REDIRECT_TO_LIST;
        }

        if (!this.canCurrentUserDeleteRequest()) {
            this.logger.warn("The user {} tried to delete request {} but is not allowed to do so.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.delete.notAllowed",
                    MessageType.ERROR);

            return REDIRECT_TO_ACCESS_DENIED;
        }

        try {
            FileSystemUtils.purgeRequestFolders(request, this.parametersRepository.getBasePath());
            this.requestsRepository.delete(request);

        } catch (RuntimeException exception) {
            this.logger.error("Could not delete the request {}.", requestId, exception);
            this.addStatusMessage(redirectAttributes, "requestDetails.deletion.failed", MessageType.ERROR);

            return String.format(RequestsController.REDIRECT_TO_DETAILS_FORMAT, requestId);
        }

        this.addStatusMessage(redirectAttributes, "requestDetails.deletion.success", MessageType.SUCCESS);
        this.logger.info("The request {} was successfully deleted by {}.", requestId, this.getCurrentUserLogin());
        return RequestsController.REDIRECT_TO_LIST;
    }



    /**
     * Processes a request to delete a file from the output of an order.
     *
     * @param requestId          the number that identifies the request whose output must be changed
     * @param targetFile         the string that identifies the output file to delete
     * @param currentStep        the step that was active when the user submitted the output file deletion request
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("{requestId}/deleteFile")
    public final synchronized String handleDeleteOutputFile(@PathVariable final int requestId,
            @RequestParam final String targetFile, @RequestParam final int currentStep,
            final RedirectAttributes redirectAttributes) {

        this.logger.debug("Received a web request to delete file \"{}\" from request {}.", targetFile, requestId);
        Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("The user {} attempted to delete a file from request {}, which does not exist.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notFound", MessageType.ERROR);

            return RequestsController.REDIRECT_TO_LIST;
        }

        if (!this.canCurrentUserChangeRequestOutput(request)) {
            this.logger.warn("The user {} tried to delete an output file of request {} but is not allowed to do so.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.outputChange.notAllowed",
                    MessageType.ERROR);

            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (!this.canRequestOutputBeChanged(request, currentStep, redirectAttributes)) {
            return RequestsController.REDIRECT_TO_LIST;
        }

        File fileToDelete = StringUtils.isNotBlank(targetFile) ? this.getRequestOutputFile(request, targetFile) : null;

        if (fileToDelete == null) {
            this.logger.debug("The user {} tried to delete output file \"{}\" for request {}, but it cannot be found.",
                    this.getCurrentUserLogin(), targetFile, requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.deleteFile.notFound", MessageType.ERROR);

        } else {

            try {

                if (fileToDelete.delete()) {
                    this.logger.debug("The output file \"{}\" for request {} was successfully deleted.",
                            fileToDelete.getAbsolutePath(), requestId);
                    this.addStatusMessage(redirectAttributes, "requestDetails.deleteFile.success", MessageType.SUCCESS);

                } else {
                    this.logger.error("The deletion of the file \"{}\" for the request {} failed silently.",
                            fileToDelete.getAbsolutePath(), requestId);
                    this.addStatusMessage(redirectAttributes, "requestDetails.deleteFile.failed", MessageType.ERROR);
                }

            } catch (RuntimeException exception) {
                this.logger.error("Could not delete the file \"{}\" for the request {}.",
                        fileToDelete.getAbsolutePath(), requestId, exception);
                this.addStatusMessage(redirectAttributes, "requestDetails.deleteFile.failed", MessageType.ERROR);
            }
        }

        return String.format(RequestsController.REDIRECT_TO_DETAILS_FORMAT, requestId);
    }



    /**
     * Processes a web request to abandon the processing of an order.
     *
     * @param requestId          the number that identifies the order whose processing should be abandoned
     * @param remark             the string entered by the user to explain why the processing is abandoned
     * @param currentStep        the step that was active when the user submitted the validation request
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("{requestId}/reject")
    public final synchronized String handleRejectRequest(@PathVariable final int requestId,
            @RequestParam final String remark, @RequestParam final int currentStep,
            final RedirectAttributes redirectAttributes) {

        this.logger.debug("Received a web request to reject request {}.", requestId);
        Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("The user {} attempted to reject request {}, which does not exist.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notFound", MessageType.ERROR);

            return RequestsController.REDIRECT_TO_LIST;
        }

        if (!this.canCurrentUserRejectRequest(request)) {
            this.logger.warn("The user {} tried to reject request {} but is not allowed to access it.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notAllowed", MessageType.ERROR);

            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (!this.canRequestBeRejected(request, currentStep, redirectAttributes)) {
            return RequestsController.REDIRECT_TO_LIST;
        }

        if (StringUtils.isBlank(remark)) {
            this.logger.debug("The user {} tried to reject request {} but did not provide a remark.",
                    this.getCurrentUserLogin(), requestId);
            redirectAttributes.addFlashAttribute("validationError", "remark");
            this.addStatusMessage(redirectAttributes, "requestDetails.error.reject.remark.required", MessageType.ERROR);

        } else {

            try {
                this.rejectRequest(request, remark);
                this.addStatusMessage(redirectAttributes, "requestDetails.rejection.success", MessageType.SUCCESS);

            } catch (RuntimeException exception) {
                this.logger.error("Could not reject the request {}.", requestId, exception);
                this.addStatusMessage(redirectAttributes, "requestDetails.rejection.failed", MessageType.ERROR);
            }
        }

        return String.format(RequestsController.REDIRECT_TO_DETAILS_FORMAT, requestId);
    }



    /**
     * Processes a web request to restart the processing of an order from the beginning.
     *
     * @param requestId          the number that identifies the order whose processing must be relaunched
     * @param currentStep        the step that was active when the user submitted the validation request
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("{requestId}/relaunch")
    public final synchronized String handleRelaunchProcess(@PathVariable final int requestId,
            @RequestParam final int currentStep, final RedirectAttributes redirectAttributes) {
        this.logger.debug("Received a web request to relaunch the processing of request {}.", requestId);
        Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("The user {} attempted to relaunch the processing of request {}, which does not exist.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notFound", MessageType.ERROR);

            return RequestsController.REDIRECT_TO_LIST;
        }

        if (!this.canCurrentUserRelaunchProcess(request)) {
            this.logger.warn("The user {} tried to restart the processing of request {} but is not allowed to do so.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notAllowed", MessageType.ERROR);

            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (!this.canRequestProcessBeRelaunched(request, currentStep, redirectAttributes)) {
            return RequestsController.REDIRECT_TO_LIST;
        }

        try {
            this.relaunchProcess(request);
            this.addStatusMessage(redirectAttributes, "requestDetails.processRelaunch.success", MessageType.SUCCESS);

        } catch (RuntimeException exception) {
            this.logger.error("Could not relaunch the process of request {}.", requestId, exception);
            this.addStatusMessage(redirectAttributes, "requestDetails.processRelaunch.failed", MessageType.ERROR);
        }

        return String.format(RequestsController.REDIRECT_TO_DETAILS_FORMAT, requestId);
    }



    /**
     * Processes a request to execute the current task of a request again.
     *
     * @param requestId          the number that identifies the request whose current task must be restarted
     * @param currentStep        the number that identifies the position of the task to restart in the process
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("{requestId}/restartTask")
    public final synchronized String handleRestartCurrentTask(@PathVariable final int requestId,
            @RequestParam final int currentStep, final RedirectAttributes redirectAttributes) {
        this.logger.debug("Received a web request to restart the current task of request {}.", requestId);
        Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("The user {} attempted to restart the current task of request {}, which does not exist.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notFound", MessageType.ERROR);

            return RequestsController.REDIRECT_TO_LIST;
        }

        if (!this.canCurrentUserRestartCurrentTask(request)) {
            this.logger.warn("The user {} tried to restart the current task of request {} but is not allowed to do so.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notAllowed", MessageType.ERROR);

            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (!this.canRequestCurrentTaskBeRestarted(request, currentStep, redirectAttributes)) {
            return RequestsController.REDIRECT_TO_LIST;
        }

        try {
            this.restartCurrentTask(request);
            this.addStatusMessage(redirectAttributes, "requestDetails.taskRestart.success", MessageType.SUCCESS);

        } catch (RuntimeException exception) {
            this.logger.error("Could not restart the current task of request {}.", requestId, exception);
            this.addStatusMessage(redirectAttributes, "requestDetails.taskRestart.failed", MessageType.ERROR);
        }

        return String.format(RequestsController.REDIRECT_TO_DETAILS_FORMAT, requestId);
    }



    /**
     * Processes a request to attempt tosend the result of an order to its originating server again.
     *
     * @param requestId          the number that identifies the order to export
     * @param currentStep        the number that identifies the position of the export task in the process
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("{requestId}/retryExport")
    public final synchronized String handleRetryExport(@PathVariable final int requestId,
            @RequestParam final int currentStep, final RedirectAttributes redirectAttributes) {
        this.logger.debug("Received a web request to export request {}.", requestId);
        Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("The user {} attempted to export request {}, which does not exist.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notFound", MessageType.ERROR);

            return RequestsController.REDIRECT_TO_LIST;
        }

        if (!this.canCurrentUserExportRequest(request)) {
            this.logger.warn("The user {} tried to export request {} but is not allowed to do so.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notAllowed", MessageType.ERROR);

            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (!this.canRequestBeExportedAgain(request, currentStep, redirectAttributes)) {
            return RequestsController.REDIRECT_TO_LIST;
        }

        try {
            this.restartExport(request);
            this.addStatusMessage(redirectAttributes, "requestDetails.exportRetry.success", MessageType.SUCCESS);

        } catch (RuntimeException exception) {
            this.logger.error("Could not retry the export of request {}.", requestId, exception);
            this.addStatusMessage(redirectAttributes, "requestDetails.exportRetry.failed", MessageType.ERROR);
        }

        return String.format(RequestsController.REDIRECT_TO_DETAILS_FORMAT, requestId);
    }



    /**
     * Processes a request to launch the process matching operation for an order.
     *
     * @param requestId          the number that identifies the request to match with a process again
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("{requestId}/retryMatching")
    public final synchronized String handleRetryMatching(@PathVariable final int requestId,
            final RedirectAttributes redirectAttributes) {
        this.logger.debug("Received a web request to restart the process matching of request {}.", requestId);
        Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("The user {} attempted to restart the process matching of request {}, which"
                    + " does not exist.", this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notFound", MessageType.ERROR);

            return RequestsController.REDIRECT_TO_LIST;
        }

        if (!this.canCurrentUserMatchRequest()) {
            this.logger.warn("The user {} tried to restart the process matching of request {} but is not allowed"
                    + " to do so.", this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notAllowed", MessageType.ERROR);

            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (!this.canRequestBeMatchedAgain(request, redirectAttributes)) {
            return RequestsController.REDIRECT_TO_LIST;
        }

        try {
            this.restartMatching(request);
            this.addStatusMessage(redirectAttributes, "requestDetails.matchingRetry.success", MessageType.SUCCESS);

        } catch (RuntimeException exception) {
            this.logger.error("Could not validate the request {}.", requestId, exception);
            this.addStatusMessage(redirectAttributes, "requestDetails.matchingRetry.failed", MessageType.ERROR);
        }

        return String.format(RequestsController.REDIRECT_TO_DETAILS_FORMAT, requestId);
    }



    /**
     * Processes a web request to abandon the execution of the current task and go on with the process.
     *
     * @param requestId          the number that identifies the order whose current task should be skipped
     * @param currentStep        the step that was active when the user submitted the skip request
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("{requestId}/skipTask")
    public final synchronized String handleSkipCurrentTask(@PathVariable final int requestId,
            @RequestParam final int currentStep, final RedirectAttributes redirectAttributes) {

        this.logger.debug("Received a web request to skip the current task of request {}.", requestId);
        Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("The user {} attempted to skip the current task of request {}, which does not exist.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notFound", MessageType.ERROR);

            return RequestsController.REDIRECT_TO_LIST;
        }

        if (!this.canCurrentUserSkipTask(request)) {
            this.logger.warn("The user {} tried to skip the current task of request {} but is not allowed to access it.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notAllowed", MessageType.ERROR);

            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (!this.canRequestCurrentTaskBeSkipped(request, currentStep, redirectAttributes)) {
            return RequestsController.REDIRECT_TO_LIST;
        }

        try {
            this.skipCurrentTask(request);
            this.addStatusMessage(redirectAttributes, "requestDetails.taskSkip.success", MessageType.SUCCESS);

        } catch (RuntimeException exception) {
            this.logger.error("Could not skip the current task of request {}.", requestId, exception);
            this.addStatusMessage(redirectAttributes, "requestDetails.taskSkip.failed", MessageType.ERROR);
        }

        return String.format(RequestsController.REDIRECT_TO_DETAILS_FORMAT, requestId);
    }



    /**
     * Processes a web request to allow a request that is in standby to proceed.
     *
     * @param requestId          the integer that identifies the standby request
     * @param remark             the string entered by the user to provide the customer additional information about
     *                           the
     *                           validation
     * @param currentStep        the step that was active when the user submitted the validation request
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("{requestId}/validate")
    public final synchronized String handleValidateStandbyRequest(@PathVariable final int requestId,
            @RequestParam final int currentStep, @RequestParam final String remark,
            final RedirectAttributes redirectAttributes) {
        this.logger.debug("Received a web request to validate standby request {}.", requestId);
        Request request = this.getDomainRequest(requestId);

        if (request == null) {
            this.logger.error("The user {} attempted to validate the request {}, which does not exist.");
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notFound", MessageType.ERROR);

            return RequestsController.REDIRECT_TO_LIST;
        }

        if (!this.canCurrentUserValidateRequest(request)) {
            this.logger.warn("The user {} tried to validate the request {} but is not allowed to access it.",
                    this.getCurrentUserLogin(), requestId);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.request.notAllowed", MessageType.ERROR);

            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (!this.canRequestBeValidated(request, currentStep, redirectAttributes)) {
            return RequestsController.REDIRECT_TO_LIST;
        }

        try {
            this.validateRequest(request, remark);
            this.addStatusMessage(redirectAttributes, "requestDetails.validation.success", MessageType.SUCCESS);

        } catch (RuntimeException exception) {
            this.logger.error("Could not validate the request {}.", requestId, exception);
            this.addStatusMessage(redirectAttributes, "requestDetails.validation.failed", MessageType.ERROR);
        }

        return String.format(RequestsController.REDIRECT_TO_DETAILS_FORMAT, requestId);
    }



    /**
     * Adds record entries for the remaining tasks when a requests is set to skip to the end of its process.
     *
     * @param request the request whose process is aborted
     */
    @Transactional
    void addSkippedTasksRecords(final Request request) {
        assert request != null : "The request cannot be null.";
        assert request.getProcess() != null : "The request must be associated with a process.";

        Process process = request.getProcess();
        Calendar skipDate = new GregorianCalendar();

        for (Task processTask : process.getTasksCollection()) {

            if (processTask.getPosition() <= request.getTasknum()) {
                continue;
            }

            this.createSkippedTaskHistoryRecord(request, processTask, skipDate);
        }
    }



    /**
     * Checks if the user that is currently authenticated can modify (add or delete) the files generated
     * as an order output.
     *
     * @param request the request whose output is being changed
     * @return <code>true</code> if the current user can change the output files
     */
    private boolean canCurrentUserChangeRequestOutput(final Request request) {
        return this.canCurrentUserViewRequestDetails(request);
    }



    /**
     * Checks if the user that is currently authenticated can delete an order.
     *
     * @return <code>true</code> if the current user can delete orders
     */
    private boolean canCurrentUserDeleteRequest() {
        return this.isCurrentUserAdmin();
    }



    /**
     * Checks if the user that is currently identified (if any) is allowed to send the result of an order
     * to its originating server.
     *
     * @param request the request to export
     * @return <code>true</code> if the current user can export the request
     */
    private boolean canCurrentUserExportRequest(final Request request) {
        return this.canCurrentUserViewRequestDetails(request);
    }



    /**
     * Checks if the user that is currently identified (if any) is allowed to launch the process matching
     * of an order.
     *
     * @return <code>true</code> if the current user can launch the process match operation
     */
    private boolean canCurrentUserMatchRequest() {
        return this.isCurrentUserAdmin();
    }



    /**
     * Checks if the user that is currently identified (if any) is allowed to stop the processing of
     * a request.
     *
     * @param request the request to reject
     * @return <code>true</code> if the current user can reject the request
     */
    private boolean canCurrentUserRejectRequest(final Request request) {
        return ((request.getStatus() == Request.Status.EXPORTFAIL || request.getStatus() == Request.Status.IMPORTFAIL)
                && this.isCurrentUserAdmin()) || this.canCurrentUserViewRequestDetails(request);
    }



    /**
     * Checks if the user that is currently identified (if any) is allowed to restart the processing of
     * a request from the beginning.
     *
     * @param request the request to restart
     * @return <code>true</code> if the current user can restart the processing
     */
    private boolean canCurrentUserRelaunchProcess(final Request request) {
        return (request.getStatus() == Request.Status.EXPORTFAIL && this.isCurrentUserAdmin())
                || this.canCurrentUserViewRequestDetails(request);
    }



    /**
     * Checks if the user that is currently identified (if any) is allowed to restart the current task of
     * a request.
     *
     * @param request the request whose current task must be restarted
     * @return <code>true</code> if the current user can restart the current task
     */
    private boolean canCurrentUserRestartCurrentTask(final Request request) {
        return this.canCurrentUserViewRequestDetails(request);
    }



    /**
     * Checks if the user that is currently identified (if any) is allowed to abandon the current task of
     * a request and go on with its process.
     *
     * @param request the request whose current task must be skipped
     * @return <code>true</code> if the current user can skip the current task
     */
    private boolean canCurrentUserSkipTask(final Request request) {
        return this.canCurrentUserViewRequestDetails(request);
    }



    /**
     * Checks if the user that is currently identified (if any) is allowed to modify a request in standby
     * so that it proceeds.
     *
     * @param request the request to validate
     * @return <code>true</code> if the current user can validate the request
     */
    private boolean canCurrentUserValidateRequest(final Request request) {
        return this.canCurrentUserViewRequestDetails(request);
    }



    /**
     * Checks if the user that is currently identified (if any) is allowed to view detailed information
     * about a given request.
     *
     * @param request the request to display
     * @return <code>true</code> if detailed information about the request can be displayed to the current user
     */
    private boolean canCurrentUserViewRequestDetails(final Request request) {
        assert request != null : "The request cannot be null.";

        if (!this.isCurrentUserApplicationUser()) {
            return false;
        }

        if (this.isCurrentUserAdmin()) {
            return true;
        }

        final Process process = request.getProcess();

        if (process == null) {
            return false;
        }

        Integer[] operatorsIds = process.getDistinctOperators().stream().map(User::getId).toArray(Integer[]::new);
        return ArrayUtils.contains(operatorsIds, this.getCurrentUserId());
    }



    /**
     * Determines whether the result of an order can be sent again to its originating server.
     *
     * @param request            the order to export
     * @param activeStep         the number that identifies the process step of the export task
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return <code>true</code> if the current task can be exported again
     */
    private boolean canRequestBeExportedAgain(final Request request, final int activeStep,
            final RedirectAttributes redirectAttributes) {

        if (!this.checkActiveStep(activeStep, request)) {
            this.logger.warn("The user {} tried to export the request at step {} of the process {}, but it is not"
                    + " the active step anymore.", this.getCurrentUserLogin(), activeStep, request.getId());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.invalidStep", MessageType.ERROR);

            return false;
        }

        if (request.getStatus() != Request.Status.EXPORTFAIL) {
            this.logger.warn("The user {} tried to export request {} again but the request is not in"
                    + " an export failure state.", this.getCurrentUserLogin(), request.getId());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.restartTask.invalidState",
                    MessageType.ERROR);

            return false;
        }

        return true;
    }



    /**
     * Determines whether the process matching can be attempted again for an order.
     *
     * @param request            the order to match with a process again
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return <code>true</code> if the order process can be restarted
     */
    private boolean canRequestBeMatchedAgain(final Request request, final RedirectAttributes redirectAttributes) {

        if (request.getTasknum() != null) {
            this.logger.warn("The user {} tried to restart the process matching of request {}, but it is not"
                    + " the active step anymore.", this.getCurrentUserLogin(), request.getId());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.invalidStep", MessageType.ERROR);

            return false;
        }

        if (request.getStatus() != Request.Status.UNMATCHED) {
            this.logger.warn("The user {} tried to restart the process matching of request {} but the request is not in"
                    + " an unmatched state.", this.getCurrentUserLogin(), request.getId());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.restartTask.invalidState",
                    MessageType.ERROR);

            return false;
        }

        return true;
    }



    /**
     * Determines whether the processing of a given order can be abandoned.
     *
     * @param request            the order whose processing should be abandoned
     * @param activeStep         the number that identifies the process step that was active when the user asked for the
     *                           order cancellation
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return <code>true</code> if the order can be abandoned
     */
    private boolean canRequestBeRejected(final Request request, final int activeStep,
            final RedirectAttributes redirectAttributes) {
        assert request != null : "The request cannot be null.";

        if (!this.checkActiveStep(activeStep, request)) {
            this.logger.warn("The user {} tried to reject the request {} from step {}, but it is not"
                    + " the active step anymore.", this.getCurrentUserLogin(), request.getId(), activeStep);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.invalidStep", MessageType.ERROR);

            return false;
        }

        if (request.isRejected() && request.getStatus() != Request.Status.EXPORTFAIL) {
            this.logger.warn("The user {} tried to reject request {} but it is already rejected.",
                    this.getCurrentUserLogin(), request.getId());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.reject.rejected", MessageType.ERROR);

            return false;
        }

        if (request.getStatus() != Request.Status.ERROR && request.getStatus() != Request.Status.EXPORTFAIL
                && request.getStatus() != Request.Status.UNMATCHED && request.getStatus() != Request.Status.STANDBY
                && request.getStatus() != Request.Status.IMPORTFAIL) {
            this.logger.warn("The user {} tried to reject request {} but it is neither in error nor in standby.",
                    this.getCurrentUserLogin(), request.getId());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.reject.invalidState", MessageType.ERROR);

            return false;
        }

        return true;
    }



    /**
     * Determines whether the processing of a given order can be restarted from the beginning.
     *
     * @param request            the order whose processing should be restarted
     * @param activeStep         the index of the current process step
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return <code>true</code> if the order process can be restarted
     */
    private boolean canRequestBeValidated(final Request request, final int activeStep,
            final RedirectAttributes redirectAttributes) {
        assert request != null : "The request cannot be null.";

        if (!this.checkActiveStep(activeStep, request)) {
            this.logger.warn("The user {} tried to validate request {} from step {}, but it is not"
                    + " the active step anymore.", this.getCurrentUserLogin(), request.getId(), activeStep);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.invalidStep", MessageType.ERROR);

            return false;
        }

        if (request.getStatus() != Request.Status.STANDBY) {
            this.logger.warn("The user {} tried to validate request {} but its status is {}.",
                    this.getCurrentUserLogin(), request.getId(), request.getStatus().name());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.validate.invalidState", MessageType.ERROR);

            return false;
        }

        return true;
    }



    /**
     * Determines whether the output files generated for a given order can be modified.
     *
     * @param request            the order whose output should be changed
     * @param activeStep         the index of the current process step
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return <code>true</code> if the order output can be changed
     */
    private boolean canRequestOutputBeChanged(final Request request, final int activeStep,
            final RedirectAttributes redirectAttributes) {
        assert request != null : "The request cannot be null.";

        if (!this.checkActiveStep(activeStep, request)) {
            this.logger.warn("The user {} tried to change the output of request {} from step {}, but it is not"
                    + " the active step anymore.", this.getCurrentUserLogin(), request.getId(), activeStep);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.invalidStep", MessageType.ERROR);

            return false;
        }

        if (request.getStatus() != Request.Status.ERROR && request.getStatus() != Request.Status.EXPORTFAIL
                && request.getStatus() != Request.Status.IMPORTFAIL && request.getStatus() != Request.Status.STANDBY
                && request.getStatus() != Request.Status.UNMATCHED) {
            this.logger.warn("The user {} tried to validate request {} but its status is {}.",
                    this.getCurrentUserLogin(), request.getId(), request.getStatus().name());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.outputChange.invalidState",
                    MessageType.ERROR);

            return false;
        }

        return true;
    }



    /**
     * Determines whether the current task of a given order process can be restarted.
     *
     * @param request            the order whose current task should be restarted
     * @param activeStep         the number that identifies the process step of the task that must be restarted
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return <code>true</code> if the current task can be restarted
     */
    private boolean canRequestCurrentTaskBeRestarted(final Request request, final int activeStep,
            final RedirectAttributes redirectAttributes) {
        assert request != null : "The request cannot be null.";

        if (!this.checkActiveStep(activeStep, request)) {
            this.logger.warn("The user {} tried to restart the task at step {} of request {}, but it is not"
                    + " the active step anymore.", this.getCurrentUserLogin(), activeStep, request.getId());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.invalidStep", MessageType.ERROR);

            return false;
        }

        if (request.getStatus() != Request.Status.ERROR) {
            this.logger.warn("The user {} tried to restart the current task of request {} but the request is not"
                    + " in error.", this.getCurrentUserLogin(), request.getId());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.restartTask.invalidState",
                    MessageType.ERROR);

            return false;
        }

        return true;
    }



    /**
     * Determines whether the current task of a given order process can be abandoned.
     *
     * @param request            the order whose current task should be abandoned
     * @param activeStep         the number that identifies the process step of the task that must be abandoned
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return <code>true</code> if the current task can be abandoned
     */
    private boolean canRequestCurrentTaskBeSkipped(final Request request, final int activeStep,
            final RedirectAttributes redirectAttributes) {
        assert request != null : "The request cannot be null.";

        if (!this.checkActiveStep(activeStep, request)) {
            this.logger.warn("The user {} tried to skip the task at step {} of request {}, but it is not"
                    + " the active step anymore.", this.getCurrentUserLogin(), activeStep, request.getId());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.invalidStep", MessageType.ERROR);

            return false;
        }

        if (request.getStatus() != Request.Status.ERROR) {
            this.logger.warn("The user {} tried to skip the current task of request {} but the request is not"
                    + " in error.", this.getCurrentUserLogin(), request.getId());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.restartTask.invalidState",
                    MessageType.ERROR);

            return false;
        }

        return true;
    }



    /**
     * Determines whether the processing of a given order can be restarted from the beginning.
     *
     * @param request            the order whose processing should be restarted
     * @param activeStep         the number that identifies the process step that was active when the user asked for the
     *                           process restart
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return <code>true</code> if the order process can be restarted
     */
    private boolean canRequestProcessBeRelaunched(final Request request, final int activeStep,
            final RedirectAttributes redirectAttributes) {
        assert request != null : "The request cannot be null.";

        if (!this.checkActiveStep(activeStep, request)) {
            this.logger.warn("The user {} tried to relaunch the processing of request {} from step {}, but it is not"
                    + " the active step anymore.", this.getCurrentUserLogin(), request.getId(), activeStep);
            this.addStatusMessage(redirectAttributes, "requestDetails.error.invalidStep", MessageType.ERROR);

            return false;
        }

        if (request.getStatus() != Request.Status.STANDBY && request.getStatus() != Request.Status.ERROR
                && request.getStatus() != Request.Status.EXPORTFAIL) {
            this.logger.warn("The user {} tried to relaunch the processing of request {} but its status is {}.",
                    this.getCurrentUserLogin(), request.getId(), request.getStatus().name());
            this.addStatusMessage(redirectAttributes, "requestDetails.error.relaunch.invalidState", MessageType.ERROR);

            return false;
        }

        return true;
    }



    /**
     * Ensures that the step that was active when the user asked for an action is still the active one.
     *
     * @param activeStep the number identifying the process step that was active when the form was submitted
     * @param request    the request that the action must be carried on
     * @return <code>true</code> if the active step is still the same
     */
    private boolean checkActiveStep(final int activeStep, final Request request) {
        assert request != null : "The request cannot be null.";

        return ((request.getTasknum() == null && activeStep < 1) || activeStep == request.getTasknum());
    }



    private boolean copyUploadedFileToOutputFolder(final MultipartFile uploadedFile, final Request request) {
        assert uploadedFile != null : "The uploaded file to copy cannot be null";
        assert request != null : "The request to add the file to cannot be null";

        final int requestId = request.getId();
        final Path outputFolderPath = Paths.get(this.parametersRepository.getBasePath(), request.getFolderOut());
        final File outputFolder = outputFolderPath.toFile();

        if (!outputFolder.exists() || !outputFolder.canRead() || !outputFolder.isDirectory()) {
            this.logger.debug("The output folder \"{}\" for request {} cannot be read or is not a directory.",
                    outputFolder.getAbsolutePath(), request.getId());
            return false;
        }

        final String outputFileName = FileSystemUtils.sanitizeFileName(uploadedFile.getOriginalFilename());
        final File targetFile = outputFolderPath.resolve(outputFileName).toFile();

        try {
            uploadedFile.transferTo(targetFile);

        } catch (IOException ioException) {
            this.logger.error("Could not copy the uploaded file \"{}\" for the request {} to path {}.",
                    uploadedFile.getOriginalFilename(), requestId, targetFile.getAbsolutePath(), ioException);
            return false;

        } catch (RuntimeException runtimeException) {
            this.logger.error("Could not add the file \"{}\" for the request {}.",
                    uploadedFile.getOriginalFilename(), requestId, runtimeException);
            return false;
        }

        return true;
    }



    /**
     * Creates an entry in the processing history of a request to indicate that a certain task has not been
     * executed at all.
     *
     * @param request     the request whose task has been skipped
     * @param skippedTask the task that has been skipped
     */
    private void createSkippedTaskHistoryRecord(final Request request, final Task skippedTask) {
        this.createSkippedTaskHistoryRecord(request, skippedTask, new GregorianCalendar());
    }



    /**
     * Creates an entry in the processing history of a request to indicate that a certain task has not been
     * executed at all.
     *
     * @param request     the request whose task has been skipped
     * @param skippedTask the task that has been skipped
     * @param skipDate    the date and time when the task has been skipped
     */
    private void createSkippedTaskHistoryRecord(final Request request, final Task skippedTask,
            final Calendar skipDate) {
        assert request != null : "The request with a skipped task cannot be null";
        assert skippedTask != null : "The skipped task cannot be null";
        assert request.getProcess() != null : "The request with a skipped task must have a process attributed";
        assert request.getProcess().getTasksCollection().contains(skippedTask) :
                "The skipped task must be part of the process attributed to the request";
        assert skipDate != null : "The date and time when the task was skipped cannot be null";

        //TODO Find an elegant way to centralize the history record creation
        RequestHistoryRecord record = new RequestHistoryRecord();
        record.setProcessStep(skippedTask.getPosition());
        record.setRequest(request);
        record.setStartDate(skipDate);
        record.setEndDate(skipDate);
        record.setStatus(RequestHistoryRecord.Status.SKIPPED);
        record.setStep(this.requestHistoryRepository.findByRequestOrderByStep(request).size() + 1);
        record.setTaskLabel(skippedTask.getLabel());
        record.setUser(this.usersRepository.findById(this.getCurrentUserId()).orElse(null));

        if (this.requestHistoryRepository.save(record) == null) {
            throw new RuntimeException(String.format("Could not save a record history for skipped task \"%s\".",
                    skippedTask.getLabel()));
        }

    }



    /**
     * Obtains a file that was produced by the processing of an order.
     *
     * @param request      the order that produced the desired file
     * @return the file, or <code>null</code> if it is not available or accessible
     */
    private byte[] getRequestOutputContent(final Request request) {
        assert request != null : "The request must not be null.";
        this.logger.debug("Getting output content for request {}.", request.getId());

        if (request.getFolderOut() == null) {
            this.logger.debug("The request {} has no output folder set.", request.getId());
            return null;
        }

        if (!this.canCurrentUserViewRequestDetails(request)) {
            this.logger.warn("The user {} tried to view the output content for request {} but is not allowed"
                    + " to view it.", this.getCurrentUserLogin(), request.getId());
            return null;
        }

        final Path outputFolderPath = Paths.get(this.parametersRepository.getBasePath(), request.getFolderOut());
        final File outputFolder = outputFolderPath.toFile();

        if (!outputFolder.exists() || !outputFolder.canRead() || !outputFolder.isDirectory()) {
            this.logger.debug("The output folder \"{}\" for request {} cannot be read or is not a directory.",
                    outputFolder.getAbsolutePath(), request.getId());
            return null;
        }

        final byte[] outputZipBytes;

        try {
            outputZipBytes = ZipUtils.zipFolderContentToByteArray(outputFolder);

        } catch (IOException exception) {
            this.logger.error("An error occurred when trying to zip the content of the output folder.", exception);
            return null;
        }

        return outputZipBytes;
    }



    /**
     * Obtains a file that was produced by the processing of an order.
     *
     * @param request      the order that produced the desired file
     * @param relativePath the path of the file relative to the output folder of the order
     * @return the file, or <code>null</code> if it is not available or accessible
     */
    private File getRequestOutputFile(final Request request, final String relativePath) {
        assert request != null : "The request must not be null.";
        assert !StringUtils.isBlank(relativePath) : "The relative path of the output file cannot be null.";
        this.logger.debug("Getting output file \"{}\" for request {}.", relativePath, request.getId());

        if (request.getFolderOut() == null) {
            this.logger.debug("The request {} has no output folder set.", request.getId());
            return null;
        }

        final File relativeFile = new File(relativePath);

        if (relativeFile.isAbsolute()) {
            this.logger.warn("The requested file path must be relative. An absolute path was provided.");
            return null;
        }

        if (!this.canCurrentUserViewRequestDetails(request)) {
            this.logger.warn("The user {} tried to view the output file \"{}\" for request {} but is not allowed"
                    + " to view it.", this.getCurrentUserLogin(), relativePath, request.getId());
            return null;
        }

        final Path outputFolderPath = Paths.get(this.parametersRepository.getBasePath(), request.getFolderOut());
        final File outputFolder = outputFolderPath.toFile();

        if (!outputFolder.exists() || !outputFolder.canRead() || !outputFolder.isDirectory()) {
            this.logger.debug("The output folder \"{}\" for request {} cannot be read or is not a directory.",
                    outputFolder.getAbsolutePath(), request.getId());
            return null;
        }

        final File outputFile = outputFolderPath.resolve(relativePath).toFile();

        String outputFileCanonicalAbsolutePath;
        String outputFolderCanonicalAbsolutePath;

        try {
            outputFileCanonicalAbsolutePath = outputFile.getCanonicalFile().getAbsolutePath();
            outputFolderCanonicalAbsolutePath = outputFolder.getCanonicalFile().getAbsolutePath();
        } catch (IOException ioException) {
            this.logger.error("Unable to get canonical paths.", ioException);
            return null;
        }

        if (!outputFileCanonicalAbsolutePath.startsWith(outputFolderCanonicalAbsolutePath)) {
            this.logger.warn("Directory traversal blocked.");
            return null;
        }

        if (!outputFile.exists() || !outputFile.exists() || !outputFile.isFile()) {
            this.logger.warn("The request output file \"{}\" cannot be read or is not a file.",
                    outputFile.getAbsolutePath());
            return null;
        }

        return outputFile;
    }



    /**
     * Abandons the processing of a request.
     *
     * @param request the request to reject
     * @param remark  the string entered by the user to explain why the request was rejected
     */
    @Transactional
    void rejectRequest(final Request request,
                       final String remark
    ) {
        assert request != null : "The request to reject must not be null";
        assert !request.isRejected() || request.getStatus() == Request.Status.EXPORTFAIL :
                "The request to reject must not already be rejected (unless the rejected export failed).";
        assert request.getStatus() == Request.Status.ERROR || request.getStatus() == Request.Status.STANDBY
                || request.getStatus() == Request.Status.EXPORTFAIL || request.getStatus() == Request.Status.UNMATCHED
                || request.getStatus() == Request.Status.IMPORTFAIL :
                "The request must be in error or in standby.";
        assert !StringUtils.isBlank(remark) : "The remark must not be empty.";

        if (request.getStatus() == Request.Status.STANDBY) {

            if (!this.validateCurrentTask(request)) {
                this.logger.error("The validation of the current task for request {} failed.", request.getId());
                throw new RuntimeException("Could not save the current task.");
            }
        }

        if (request.getProcess() != null) {
            this.addSkippedTasksRecords(request);
        }

        request.reject(remark);

        if (this.requestsRepository.save(request) == null) {
            throw new RuntimeException("Could not save the request.");
        }
    }



    /**
     * Prepares the request so that its processing is restarted from the beginning.
     *
     * @param request the request whose processing must be restarted
     */
    @Transactional
    void relaunchProcess(final Request request) {
        assert request != null : "The request to relaunch cannot be null.";
        assert request.getStatus() == Request.Status.ERROR || request.getStatus() == Request.Status.STANDBY :
                "The request can only be relaunched if it is in error or in standby.";
        assert !request.isRejected() : "The request cannot be restarted if it has been rejected.";
        assert request.getProcess() != null : "The request to restart must be associated to a process";

        final int requestId = request.getId();

        if (request.getStatus() == Request.Status.STANDBY) {

            if (!this.validateCurrentTask(request)) {
                this.logger.error("The validation of the current task for request {} failed.", request.getId());
                throw new RuntimeException("Could not save the current task.");
            }
        }

        this.logger.info("Deleting the content of the output folder for request {}.", requestId);
        String basePath = this.parametersRepository.getBasePath();

        if (!FileSystemUtils.purgeRequestFolderContent(request, RequestDataFolder.OUTPUT, basePath)) {
            this.logger.warn("Not all the content of the output folder for request {} could be deleted.", requestId);
        }

        request.setTasknum(1);
        request.setStatus(Request.Status.ONGOING);

        if (this.requestsRepository.save(request) == null) {
            throw new RuntimeException("Could not save the request.");
        }
    }



    /**
     * Prepares the request so that the current task is rerun.
     *
     * @param request the request whose current task must be restarted
     */
    @Transactional
    void restartCurrentTask(final Request request
    ) {
        assert request != null : "The request cannot be null.";
        assert request.getStatus() == Request.Status.ERROR :
                "Cannot restart the current task if the request is not in error";
        assert request.getProcess() != null :
                "Cannot restart the current task if the request is not associated with a process.";

        request.setStatus(Request.Status.ONGOING);

        if (this.requestsRepository.save(request) == null) {
            throw new RuntimeException("Could not save the request.");
        }
    }



    /**
     * Prepares the request so that the export is attempted again.
     *
     * @param request the request that must be exported again
     */
    @Transactional
    void restartExport(final Request request
    ) {
        assert this.isCurrentUserAdmin() : "The current user must be an administrator.";
        assert request != null : "The request cannot be null.";
        assert request.getStatus() == Request.Status.EXPORTFAIL :
                "Cannot restart the export if the request is not in export fail";

        request.setStatus(Request.Status.TOEXPORT);

        if (this.requestsRepository.save(request) == null) {
            throw new RuntimeException("Could not save the request.");
        }
    }



    /**
     * Prepares the request so that the export is attempted again.
     *
     * @param request the request that must be exported again
     */
    @Transactional
    void restartMatching(final Request request
    ) {
        assert this.isCurrentUserAdmin() : "The current user must be an administrator.";
        assert request != null : "The request cannot be null.";
        assert request.getStatus() == Request.Status.UNMATCHED :
                "Cannot restart the export if the request is not unmatched";

        request.setStatus(Request.Status.IMPORTED);

        if (this.requestsRepository.save(request) == null) {
            throw new RuntimeException("Could not save the request.");
        }
    }



    /**
     * Prepares the request so that the process goes on with the next task in the process.
     *
     * @param request the request whose current task must be abandoned
     */
    @Transactional
    void skipCurrentTask(final Request request) {
        assert request != null : "The request cannot be null.";
        assert request.getStatus() == Request.Status.ERROR :
                "Cannot restart the current task if the request is not in error";
        assert request.getProcess() != null :
                "Cannot restart the current task if the request is not associated with a process.";
        Task[] processTasks = request.getProcess().getTasksCollection().toArray(new Task[]{});
        Task currentTask = processTasks[request.getTasknum() - 1];
        this.createSkippedTaskHistoryRecord(request, currentTask);

        request.setTasknum(request.getTasknum() + 1);
        request.setStatus(Request.Status.ONGOING);

        if (this.requestsRepository.save(request) == null) {
            throw new RuntimeException("Could not save the request.");
        }
    }



    /**
     * Set the current task of a request as validated by the current user.
     *
     * @param request the request whose current task must be validated
     * @return <code>true</code> if the task has been successfully validated
     */
    private boolean validateCurrentTask(final Request request) {
        this.logger.debug("Setting the current task of request {} as finished.");
        assert request != null : "The request must not be null.";

        final RequestHistoryRecord currentTaskRecord
                = this.requestHistoryRepository.findByRequestOrderByStepDesc(request).get(0);

        assert currentTaskRecord.getStatus() == RequestHistoryRecord.Status.ERROR
                || currentTaskRecord.getStatus() == RequestHistoryRecord.Status.ERROR :
                "The current task must be in error or in standby";

        currentTaskRecord.setEndDate(new GregorianCalendar());
        currentTaskRecord.setStatus(RequestHistoryRecord.Status.FINISHED);
        User currentUser = this.usersRepository.findByLoginIgnoreCase(this.getCurrentUserLogin());

        if (currentUser == null) {
            throw new RuntimeException(String.format("Could not fetch the current user data object (login : %s).",
                    this.getCurrentUserLogin()));
        }

        currentTaskRecord.setUser(currentUser);

        return (this.requestHistoryRepository.save(currentTaskRecord) != null);
    }



    /**
     * Modifies a request that is currently in standby to allow it to proceed.
     *
     * @param request the request in standby
     * @param remark  the remark entered by the user to give additional information about the validation to the
     *                customer, or <code>null</code> if the user did not enter a remark
     */
    @Transactional
    void validateRequest(final Request request, final String remark) {
        assert request != null : "The request must not be null";
        assert request.getStatus() == Request.Status.STANDBY : "The request must be in standby.";

        if (!this.validateCurrentTask(request)) {
            this.logger.error("The validation of the current task for request {} failed.", request.getId());
            throw new RuntimeException("Could not save the current task.");
        }

        request.setStatus(Request.Status.ONGOING);
        request.setTasknum(request.getTasknum() + 1);
        request.setRemark(remark);

        if (this.requestsRepository.save(request) == null) {
            throw new RuntimeException("Could not save the request.");
        }
    }



    /**
     * Returns the request domain object that is identified by a given identifier.
     *
     * @param requestId the number that identifies the request
     * @return the request domain object or <code>null</code> if no request matches the given identifier
     */
    private Request getDomainRequest(int requestId) {
        return this.requestsRepository.findById(requestId).orElse(null);
    }

}
