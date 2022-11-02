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

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.orchestrator.OrchestratorSettings;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.web.model.json.ConnectorJsonModel;
import ch.asit_asso.extract.web.model.json.DataTableResponse;
import com.fasterxml.jackson.annotation.JsonView;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.http.HttpSession;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Request.Status;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.exceptions.BaseFolderNotFoundException;
import ch.asit_asso.extract.persistence.RequestHistoryRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.persistence.sorts.RequestSort;
import ch.asit_asso.extract.persistence.specifications.RequestSpecification;
import ch.asit_asso.extract.web.Message.MessageType;
import ch.asit_asso.extract.web.model.RequestModel;
import ch.asit_asso.extract.web.model.comparators.RequestModelByTaskDateComparator;
import ch.asit_asso.extract.web.model.json.PublicField;
import ch.asit_asso.extract.web.model.json.RequestJsonModel;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



/**
 * An object that processes the request for the home page of the site.
 *
 * @author Yves Grasset
 */
@Controller
@RequestMapping("")
public class IndexController extends BaseController {

    /**
     * The string that identifies the part of the website that this controller manages.
     */
    private static final String CURRENT_SECTION_IDENTIFIER = "home";

    /**
     * The string that identifies the view to display the home page.
     */
    private static final String HOME_VIEW = "requests/list";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(IndexController.class);

    /**
     * The Spring Data object that links the connector data objects with the data source.
     */
    @Autowired
    private ConnectorsRepository connectorsRepository;

    /**
     * The access to the localized application strings.
     */
    @Autowired
    private MessageSource messageSource;

    /**
     * The Spring Data object that links the application parameters data objects with the data source.
     */
    @Autowired
    private SystemParametersRepository parametersRepository;

    /**
     * The Spring Data object that links the process data objects with the data source.
     */
    @Autowired
    private ProcessesRepository processesRepository;

    /**
     * The Spring Data object that links the requests data objects with the data source.
     */
    @Autowired
    private RequestsRepository requestsRepository;

    /**
     * The Spring Data object that links the requests history entry data objects with the data source.
     */
    @Autowired
    private RequestHistoryRepository requestsHistoryRepository;

    /**
     * The number of items to display in tables that uses paging.
     */
    @Value("${table.page.size}")
    private int tablePageSize;

    /**
     * The Spring Data object that links the requests history entry data objects with the data source.
     */
    @Autowired
    private UsersRepository usersRepository;



    /**
     * Creates a new instance of this controller.
     */
    public IndexController() {
        this.logger.debug("Instantiating the controller for the web site index page.");
    }



    /**
     * Process a request to display the home page.
     *
     * @param session            the current HTTP session
     * @param redirectAttributes the data to pass to a redirecting page or passed by a page that redirects to this one.
     * @param model              the data to display in the next view
     * @return the name of the next view
     */
    @GetMapping
    public final String index(final HttpSession session, final RedirectAttributes redirectAttributes,
            final ModelMap model) {
        this.logger.debug("Processing the index page.");

        if (!this.isCurrentUserApplicationUser()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (!this.testBaseFolder()) {
            this.logger.debug("The requests base folder is not accessible. Displaying an error message on the"
                    + " home page.");
            model.addAttribute("baseFolderError", true);

        } else {
            model.addAttribute("processes", this.processesRepository.findAllByOrderByName());
            model.addAttribute("connectors", this.connectorsRepository.findAllByOrderByName());
            model.addAttribute("language", this.getApplicationLanguage());
            model.addAttribute("refreshInterval",
                    Integer.valueOf(this.parametersRepository.getDashboardRefreshInterval()));
            model.addAttribute("tablePageSize", this.tablePageSize);
        }

        this.addCurrentSectionToModel(IndexController.CURRENT_SECTION_IDENTIFIER, model);

        return IndexController.HOME_VIEW;
    }



    /**
     * Processes a request indicating that the current user is not allowed to access a resource.
     *
     * @param redirectAttributes the data to pass to the view that the user will be redirected to
     * @return the string that identifies the next view to display
     */
    @GetMapping("forbidden")
    public final String handleForbidden(final RedirectAttributes redirectAttributes) {
        this.logger.debug("A forbidden access error has been issued.");

        if (!this.isCurrentUserApplicationUser()) {
            return REDIRECT_TO_LOGIN;
        }

        this.addStatusMessage(redirectAttributes, "permissions.insufficient.message", MessageType.ERROR);
        this.logger.info("The user is not an admin. Redirecting to the home page.");
        return REDIRECT_TO_HOME;
    }



    /**
     * Processes a request to get the description of all the connectors whose import is active.
     *
     * @return a JSON object that describe the active connectors
     */
    @JsonView(PublicField.class)
    @GetMapping("getActiveConnectors")
    @ResponseBody
    public final ConnectorJsonModel[] handleGetActiveConnectors() {

        if (!this.isCurrentUserApplicationUser()) {
            return null;
        }

        Connector[] activeConnectors
                = this.connectorsRepository.findByActiveTrueOrderByName().toArray(new Connector[]{});
        return ConnectorJsonModel.fromConnectorsArray(activeConnectors, this.messageSource, this.isCurrentUserAdmin());
    }



    @GetMapping("getWorkingState")
    @ResponseBody
    public final String handleGetWorkingState() {

        if (!this.isCurrentUserApplicationUser()) {
            return null;
        }

        OrchestratorSettings orchestratorSettings = new OrchestratorSettings(this.parametersRepository);

        return orchestratorSettings.getStateString();
    }



    /**
     * Processes a request to get the description of the orders that whose processing has started but has
     * not completed yet.
     *
     * @return a JSON object that describes the current orders
     */
    @JsonView(PublicField.class)
    @GetMapping("getCurrentRequests")
    @ResponseBody
    public final DataTableResponse handleGetCurrentRequests(/*@RequestParam int draw*/) {

        if (!this.isCurrentUserApplicationUser()) {
            return null;
        }

        try {
            RequestJsonModel[] requestsData
                    = RequestJsonModel.fromRequestModelsArray(this.getCurrentRequests(), this.messageSource);

            return new DataTableResponse(1, requestsData.length, requestsData.length, requestsData);

        } catch (BaseFolderNotFoundException baseFolderException) {
            this.logger.error("The finished requests retrieval failed.", baseFolderException);

            return new DataTableResponse(1);

        } catch (Exception exception) {
            this.logger.error("The current requests retrieval failed.", exception);

            return new DataTableResponse(1, exception.getMessage());
        }
    }



    /**
     * Processes a request to get the description of the orders whose processing is complete.
     *
     * @param draw                    the number that identifies the query submitted
     * @param pageStart               the index of the item that start the page to display
     * @param sortFields              a string that contains the fields that the data must be sorted by, separated by
     *                                a comma
     * @param sortDirection           <code>asc</code> to sort in ascending order or
     *                                <code>desc</code> to sort in descending order
     * @param filterText              a string that contains the text to use to filter the requests to display, or an
     *                                empty string if the textual filter must not be applied
     * @param filterConnectorIdString a string that contains the number that identifies the connectors whose requests
     *                                must be displayed, or an empty string if the requests must not be filter by
     *                                connector
     * @param filterProcessIdString   a string that contains the number that identifies the process whose requests
     *                                must be displayed, or an empty string if the requests must not be filter by
     *                                process
     * @param filterDateFrom          a string that contains the timestamp of the date from which requests to display
     *                                have been received, or an empty string if the reception date must not have
     *                                a lower limit
     * @param filterDateTo            a string that contains the timestamp of the date until which requests
     *                                to display have been received, or an empty string if the reception date must
     *                                not have an upper limit
     * @return a JSON object that describes the finished orders that match the given criteria
     */
    @JsonView(PublicField.class)
    @GetMapping("getFinishedRequests")
    @ResponseBody
    public final DataTableResponse handleGetFinishedRequests(@RequestParam final int draw,
            @RequestParam("start") final int pageStart, @RequestParam final String sortFields,
            @RequestParam final String sortDirection, @RequestParam final String filterText,
            @RequestParam("filterConnector") final String filterConnectorIdString,
            @RequestParam("filterProcess") final String filterProcessIdString,
            @RequestParam final String filterDateFrom, @RequestParam final String filterDateTo) {

        if (!this.isCurrentUserApplicationUser()) {
            return null;
        }

        if (pageStart < 0) {
            throw new IllegalArgumentException("The page start index cannot be negative.");
        }

        if (sortFields == null) {
            throw new IllegalArgumentException("The sort fields string cannot be null.");
        }

        if (sortDirection == null) {
            throw new IllegalArgumentException("The sort direction cannot be null");
        }

        try {

            final Connector connector = this.getConnectorForFilter(filterConnectorIdString);
            final Process process = this.getProcessForFilter(filterProcessIdString);
            final Calendar startDateFrom = this.getDateForFilter(filterDateFrom, false);
            final Calendar startDateTo = this.getDateForFilter(filterDateTo, true);

            final Page<Request> pagedResult = this.getFinishedRequests(pageStart, sortFields, sortDirection, filterText,
                    connector, process, startDateFrom, startDateTo);
            RequestModel[] requestModelArray = RequestModel.fromDomainRequestsPage(pagedResult,
                    this.requestsHistoryRepository, this.parametersRepository.getBasePath(), this.messageSource);
            RequestJsonModel[] requestsData
                    = RequestJsonModel.fromRequestModelsArray(requestModelArray, this.messageSource);

            return new DataTableResponse(draw, this.requestsRepository.count(), pagedResult.getTotalElements(),
                    requestsData);

        } catch (BaseFolderNotFoundException baseFolderException) {
            this.logger.error("The finished requests retrieval failed.", baseFolderException);

            return new DataTableResponse(draw);

        } catch (Exception exception) {
            this.logger.error("The finished requests retrieval failed.", exception);

            return new DataTableResponse(draw, exception.getMessage());
        }
    }



    /**
     * Obtains the process whose requests should be displayed.
     *
     * @param connectorIdString a string that contains the number that identifies the process to filter,
     *                          or an empty string
     * @return the process, or <code>null</code> if the requests of all processes (and of no process) should be
     *         displayed
     */
    private Connector getConnectorForFilter(final String connectorIdString) {
        assert connectorIdString != null : "The string containing the connector identifier to filter cannot be null.";

        if (connectorIdString.isEmpty()) {
            return null;
        }

        int connectorId = Integer.parseInt(connectorIdString);

        return this.connectorsRepository.findById(connectorId).orElse(null);
    }



    /**
     * Gets models for all the requests that are not finished and that the current user is allowed to view.
     *
     * @return an array that contains the current requests models
     */
    private RequestModel[] getCurrentRequests() {
        assert this.isCurrentUserApplicationUser() : "The user must be authenticated.";

        Collection<Request> currentDomainRequests;

        if (this.isCurrentUserAdmin()) {
            currentDomainRequests = this.requestsRepository.findByStatusNot(Status.FINISHED);

        } else {
            currentDomainRequests = this.usersRepository.getUserAssociatedRequestsByStatusNot(this.getCurrentUserId(),
                    Status.FINISHED);
        }

        RequestModel[] currentRequests = RequestModel.fromDomainRequestsCollection(currentDomainRequests,
                this.requestsHistoryRepository, this.parametersRepository.getBasePath(), this.messageSource);
        Arrays.sort(currentRequests, new RequestModelByTaskDateComparator(true));

        return currentRequests;
    }



    /**
     * Transforms a date filter string into a date.
     *
     * @param dateFilterString the string that contains the timestamp of the date, or an empty string
     * @param isEndDate        <code>true</code> if the date is to be treated as the end of the filtering period,
     *                         that is whether it should be excluded from the results
     * @return the date, or <code>null</code> if the field date must not be filtered
     */
    private Calendar getDateForFilter(final String dateFilterString, final boolean isEndDate) {
        assert dateFilterString != null : "The date filter string cannot be null.";

        if (dateFilterString.isEmpty()) {
            return null;
        }

        DateTime dateTime = new DateTime(Long.parseLong(dateFilterString)).withTimeAtStartOfDay();

        if (isEndDate) {
            dateTime = dateTime.plusDays(1);
        }

        return dateTime.toCalendar(Locale.getDefault());
    }



    /**
     * Gets models for all the requests that are done and that the current user is allowed to view.
     *
     * @param pageStart       the index of the item that start the page to display
     * @param sortFields      a string that contains the fields that the data must be sorted by, separated by a
     *                        comma
     * @param sortDirection   <code>asc</code> to sort in ascending order or
     *                        <code>desc</code> to sort in descending order
     * @param filterText      a string that contains the text to use to filter the requests to display, or an
     *                        empty string if the textual filter must not be applied
     * @param filterConnector the connector whose requests must be displayed, or <code>null</code> if the requests
     *                        must not be filter by connector
     * @param filterProcess   the process whose requests must be displayed, or <code>null</code> if the requests
     *                        must not be filter by process
     * @param filterDateFrom  the date from which requests to display have been received, or <code>null</code> if
     *                        the reception date must not have a lower limit
     * @param filterDateTo    the date until which requests to display have been received, or <code>null</code> if
     *                        the reception date must not have an upper limit
     * @return an array that contains the finished requests models that match the given criteria
     */
    private Page<Request> getFinishedRequests(final int pageStart, final String sortFields,
            final String sortDirection, final String filterText,
            final Connector filterConnector,
            final Process filterProcess,
            final Calendar filterDateFrom, final Calendar filterDateTo) {
        assert this.isCurrentUserApplicationUser() : "The user must be authenticated.";
        assert pageStart >= 0 : "The page start index cannot be negative";
        assert sortFields != null : "The sort fields string cannot be null";
        assert sortDirection != null : "The sort direction cannot be null";

        final PageRequest paging = PageRequest.of(pageStart / this.tablePageSize, this.tablePageSize,
                RequestSort.getSort(sortFields.split(","), sortDirection));
        final Specification<Request> filterCriteria
                = RequestSpecification.getFilterSpecification(filterText, filterConnector, filterProcess,
                        filterDateFrom, filterDateTo);
        final Specification<Request> searchCriteria
                = Specification.where(RequestSpecification.isFinished()).and(filterCriteria);

        if (this.isCurrentUserAdmin()) {
            return this.requestsRepository.findAll(searchCriteria, paging);
        }

        final User currentUser = this.usersRepository.findById(this.getCurrentUserId())
                .orElseThrow(() -> new UnsupportedOperationException("User does not exist."));
        final Specification<Request> userCriteria
                = RequestSpecification.isProcessInList(currentUser.getProcessesCollection());

        return this.requestsRepository.findAll(Specification.where(userCriteria).and(searchCriteria), paging);
    }



    /**
     * Obtains the process whose requests should be displayed.
     *
     * @param processIdString a string that contains the number that identifies the process to filter,
     *                        or an empty string
     * @return the process, or <code>null</code> if the requests of all processes (and of no process) should be
     *         displayed
     */
    private Process getProcessForFilter(final String processIdString) {
        assert processIdString != null : "The string containing the process identifier to filter cannot be null.";

        if (processIdString.isEmpty()) {
            return null;
        }

        int processId = Integer.parseInt(processIdString);

        return this.processesRepository.findById(processId).orElse(null);
    }



    /**
     * Checks if the folder that hosts the data for the running orders is accessible.
     *
     * @return <code>true</code> if the folder matches the requirements
     */
    private boolean testBaseFolder() {
        final File baseFolder = new File(this.parametersRepository.getBasePath());

        return (baseFolder.exists() && baseFolder.isDirectory() && baseFolder.canRead() && baseFolder.canWrite());
    }

}
