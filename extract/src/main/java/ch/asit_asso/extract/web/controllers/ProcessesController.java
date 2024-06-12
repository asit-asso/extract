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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.validation.Valid;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Remark;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.RemarkRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.persistence.TasksRepository;
import ch.asit_asso.extract.persistence.UserGroupsRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorDiscovererWrapper;
import ch.asit_asso.extract.web.Message.MessageType;
import ch.asit_asso.extract.web.model.PluginItemModelParameter;
import ch.asit_asso.extract.web.model.ProcessModel;
import ch.asit_asso.extract.web.model.RemarkModel;
import ch.asit_asso.extract.web.model.TaskModel;
import ch.asit_asso.extract.web.model.UserModel;
import ch.asit_asso.extract.web.validators.PluginItemModelParameterValidator;
import ch.asit_asso.extract.web.validators.ProcessValidator;
import ch.asit_asso.extract.web.validators.TaskValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



/**
 * Processes the web requests related the request processes.
 *
 * @author Yves Grasset
 */
@Controller
@Scope("session")
@RequestMapping("/processes")
public class ProcessesController extends BaseController {

    /**
     * The string that identifies the part of the website that this controller manages.
     */
    private static final String CURRENT_SECTION_IDENTIFIER = "processes";

    /**
     * The string that identifies the view to display information about a process.
     */
    private static final String DETAILS_VIEW = "processes/details";

    /**
     * The string that identifies the view to display all the processes.
     */
    private static final String LIST_VIEW = "processes/list";

    /**
     * The number of pixels from the top of the page to scroll to get to the bottom of the page.
     */
    private static final int PAGE_BOTTOM_SCROLL_VALUE = 99999;

    /**
     * The string that tells this controller to redirect the user to the processes list view.
     */
    private static final String REDIRECT_TO_LIST = "redirect:/processes";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ProcessesController.class);

    /**
     * The repository that links processes data objects with the data source.
     */
    @Autowired
    private ProcessesRepository processesRepository;

    /**
     * The repository that links remarks data objects with the data source.
     */
    @Autowired
    private RemarkRepository remarksRepository;

    /**
     * The repository that links order data objects with the data source.
     */
    @Autowired
    private RequestsRepository requestsRepository;

    /**
     * The access to the available task processing plugins.
     */
    @Autowired
    private TaskProcessorDiscovererWrapper taskPluginsDiscoverer;

    /**
     * The repository that links tasks data objects with the data source.
     */
    @Autowired
    private TasksRepository tasksRepository;

    /**
     * The repository that links user groups data objects with the data source.
     */
    @Autowired
    private UserGroupsRepository userGroupsRepository;

    /**
     * The repository that links users data objects with the data source.
     */
    @Autowired
    private UsersRepository usersRepository;




    /**
     * Defines the links between form data and Java objects.
     *
     * @param binder the web data binder that allows to configure the form data bindings
     */
    @InitBinder("process")
    public final void initBinder(final WebDataBinder binder) {
        binder.setValidator(new ProcessValidator(new TaskValidator(new PluginItemModelParameterValidator())));
    }



    /**
     * Processes the submission of data to create a new process.
     *
     * @param processModel       the process data submitted
     * @param bindingResult      the validation information about the submitted process
     * @param model              the data to display in the next view
     * @param redirectAttributes the data to pass to a view that the user may be redirected to
     * @return the string that identifies the next view to display
     */
    @PostMapping("add")
    public final String addItem(@Valid @ModelAttribute("process") final ProcessModel processModel,
            final BindingResult bindingResult, final ModelMap model, final RedirectAttributes redirectAttributes) {
        this.logger.debug("Processing the data to add a process.");

        if (!this.isCurrentUserAdmin()) {
            return ProcessesController.REDIRECT_TO_ACCESS_DENIED;
        }

        if (bindingResult.hasErrors()) {
            this.logger.info("The process add failed.");
            //model.addAttribute("isNew", true);
            return this.prepareModelForDetailsView(model, true);
        }

        processModel.createInDataSource(this.processesRepository, this.tasksRepository, this.usersRepository,
                                        this.userGroupsRepository);

        this.addStatusMessage(redirectAttributes, "connectorsList.connector.added", MessageType.SUCCESS);
        return ProcessesController.REDIRECT_TO_LIST;
    }



    /**
     * Inserts a new task in the current process.
     *
     * @param processModel       the model that represents the current process
     * @param model              the data to display in the next view
     * @param processId          the number that identifies the current process
     * @param taskCode           the string that identifies the type of task to insert
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display
     */
    @PostMapping("{processId}/addTask/{taskCode}")
    public final String addTask(@ModelAttribute("process") final ProcessModel processModel, final ModelMap model,
                                @PathVariable final int processId, @PathVariable final String taskCode,
                                final RedirectAttributes redirectAttributes) {

        this.logger.debug("Processing the data to add a task to the proces.");

        if (!this.isCurrentUserAdmin()) {
            return ProcessesController.REDIRECT_TO_ACCESS_DENIED;
        }

        ITaskProcessor taskProcessor = this.taskPluginsDiscoverer.getTaskProcessor(taskCode);
        if (taskProcessor == null) {
            this.logger.error("No task found with code {}.", taskCode);
            this.addStatusMessage(redirectAttributes, "processDetails.errors.task.notFound", MessageType.ERROR);

            return ProcessesController.DETAILS_VIEW;
        }

        final TaskModel taskModel = new TaskModel(taskProcessor);
        taskModel.setTag(TaskModel.TAG_ADDED);
        processModel.addTask(taskModel);
        processModel.setHtmlScrollY(ProcessesController.PAGE_BOTTOM_SCROLL_VALUE);

        return this.prepareModelForDetailsView(model, false, processModel);
    }



    /**
     * Processes the submission of data to modify an existing process.
     *
     * @param processModel       the process data submitted
     * @param bindingResult      the validation information about the submitted process
     * @param model              the data to display in the next view
     * @param id                 the number that identifies the process to update
     * @param redirectAttributes the data to be passed to a view that the user may be redirected to
     * @return the string that identifies the next view to display
     */
    @PostMapping("{id}")
    public final String updateItem(@Valid @ModelAttribute("process") final ProcessModel processModel,
            final BindingResult bindingResult, final ModelMap model, @PathVariable final int id,
            final RedirectAttributes redirectAttributes) {

        this.logger.debug("Processing the data to update a process.");

        try {

            if (!this.isCurrentUserAdmin()) {
                return ProcessesController.REDIRECT_TO_ACCESS_DENIED;
            }

            if (bindingResult.hasErrors()) {
                this.logger.info("The process update failed.");

                return this.prepareModelForDetailsView(model, false);
            }

            this.logger.debug("Model parameters are : {}",
                              String.join("\n",
                                          Arrays.stream(processModel.getTasks()).map(task ->
                                                                                     String.format("%s: [%s]",
                                                                                                   task.getPluginCode(),
                                                                                                   String.join(", ",
                                                                                                               Arrays.stream(task.getParameters()).map(
                                                                                                               PluginItemModelParameter::getName).toList()
                                                                                                   )
                                                                                     )
                                          ).toList()
                              )
            );

            this.logger.debug("Fetching the process to update.");
            Process domainProcess = this.processesRepository.findById(id).orElse(null);

            if (domainProcess == null) {
                return this.addItem(processModel, bindingResult, model, redirectAttributes);
            }

            if (!domainProcess.canBeEdited(this.requestsRepository)) {
                this.logger.error("Could not update the process because at least one if its associated requests is"
                        + " still ongoing.");
                this.addStatusMessage(model, "processDetails.errors.request.ongoing", MessageType.ERROR);
                return this.prepareModelForDetailsView(model, false);
            }

            this.saveProcessModifications(processModel, domainProcess);
            this.logger.info("Updating the process # {} has succeeded.", domainProcess.getId());
            this.addStatusMessage(redirectAttributes, "processesList.process.updated", MessageType.SUCCESS);

        } catch (Exception exception) {
            this.logger.error("An error occurred during the update of the process with identifier {}.",
                    processModel.getId(), exception);
            throw exception;
        }

        return ProcessesController.REDIRECT_TO_LIST;

    }



    /**
     * Creates a copy of a process in the data source.
     *
     * @param id                 the number that identifies the process to duplicate
     * @param name               the name of the process to duplicate
     * @param redirectAttributes the data to pass to the view that the user will be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("clone")
    public final String cloneItem(@RequestParam final int id, @RequestParam final String name,
            final RedirectAttributes redirectAttributes) {

        if (!this.isCurrentUserAdmin()) {
            return ProcessesController.REDIRECT_TO_ACCESS_DENIED;
        }

        Process domainProcess = this.processesRepository.findById(id).orElse(null);

        if (domainProcess == null || !domainProcess.getName().equals(name)) {
            this.logger.warn("The process {} (ID: {}) could not be found. Nothing to clone.", name, id);
            this.addStatusMessage(redirectAttributes, "processesList.errors.process.notFound", MessageType.WARNING);

            return ProcessesController.REDIRECT_TO_LIST;
        }

        try {
            this.cloneProcess(domainProcess);
            this.addStatusMessage(redirectAttributes, "processesList.process.cloned", MessageType.SUCCESS);

        } catch (Exception exception) {
            this.logger.error("Process {} could not be cloned.", name, exception);
            this.addStatusMessage(redirectAttributes, "processesList.errors.process.clone.generic",
                                  MessageType.ERROR);
        }

        return ProcessesController.REDIRECT_TO_LIST;
    }



    public void cloneProcess(Process processToClone) {
        Process clonedProcess = processToClone.createCopy();
        this.logger.debug("Saving process clone \"{}\" without tasks.", clonedProcess.getName());
        clonedProcess = this.processesRepository.save(clonedProcess);
        clonedProcess.setTasksCollection(processToClone.createTasksCopy(clonedProcess));

        for (Task clonedTask : clonedProcess.getTasksCollection()) {
            this.logger.debug("Saving cloned task \"{}\" at position {}.",
                              clonedTask.getLabel(), clonedTask.getPosition());
            this.tasksRepository.save(clonedTask);
        }
    }



    /**
     * Removes a process from the data source.
     *
     * @param id                 the number that identifies the process to delete
     * @param name               the name of the process to delete
     * @param redirectAttributes the data to pass to the view that the user will be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("delete")
    public final String deleteItem(@RequestParam final int id, @RequestParam final String name,
            final RedirectAttributes redirectAttributes) {

        if (!this.isCurrentUserAdmin()) {
            return ProcessesController.REDIRECT_TO_ACCESS_DENIED;
        }

        Process domainProcess = this.processesRepository.findById(id).orElse(null);

        if (domainProcess == null || !domainProcess.getName().equals(name)) {
            this.logger.warn("The process {} (ID: {}) could not be found. Nothing to delete.", name, id);
            this.addStatusMessage(redirectAttributes, "processesList.errors.process.notFound", MessageType.WARNING);

            return ProcessesController.REDIRECT_TO_LIST;
        }

        if (!domainProcess.canBeDeleted(this.requestsRepository)) {
            this.logger.error("The process {} cannot be deleted because active requests are bound to it or rules are"
                    + " associated with it.", name);
            this.addStatusMessage(redirectAttributes, "processesList.errors.process.notDeletable", MessageType.ERROR);

            return ProcessesController.REDIRECT_TO_LIST;
        }

        boolean success = false;

        try {
            this.processesRepository.delete(domainProcess);
            success = true;

        } catch (Exception exception) {
            this.logger.error("Process {} could not be deleted.", name, exception);
        }

        if (success) {
            this.addStatusMessage(redirectAttributes, "processesList.process.deleted", MessageType.SUCCESS);

        } else {
            this.addStatusMessage(redirectAttributes, "processesList.errors.process.delete.generic",
                    MessageType.ERROR);
        }

        return ProcessesController.REDIRECT_TO_LIST;
    }



    /**
     * Processes a request to delete a connector rule.
     *
     * @param processModel       the model representing the connector currently being edited
     * @param model              the collection of model objects to communicate to the next view
     * @param taskId             the identifier of the task to delete
     * @param processId          the identifier of the process associated to the task
     * @param redirectAttributes the data to pass to the view that the user will be redirected to
     * @return the identifier of the next view to display
     */
    @PostMapping("{processId}/deleteTask/{taskId}")
    public final String deleteTask(@ModelAttribute("process") final ProcessModel processModel,
            final ModelMap model, @PathVariable final int taskId, @PathVariable final int processId,
            final RedirectAttributes redirectAttributes) {

        this.logger.debug("Processing a request to delete the task with id {}.", taskId);

        if (!this.isCurrentUserAdmin()) {
            return ProcessesController.REDIRECT_TO_ACCESS_DENIED;
        }

        processModel.removeTask(taskId);

        return this.prepareModelForDetailsView(model, false, processModel);
    }



    /**
     * Processes a request to display the view to add a new process.
     *
     * @param model the data to display in the next view
     * @return the string that identifies the next view
     */
    @GetMapping("add")
    public final String viewAddForm(final ModelMap model) {

        try {

            if (!this.isCurrentUserAdmin()) {
                return ProcessesController.REDIRECT_TO_ACCESS_DENIED;
            }

            final ProcessModel newProcessModel = new ProcessModel();

            return this.prepareModelForDetailsView(model, true, newProcessModel);

        } catch (Exception exception) {
            this.logger.error("An error occurred when the process add form was prepared.", exception);
            throw exception;
        }
    }



    /**
     * Processes a request to display a list of all the available processes.
     *
     * @param model the data to be displayed by the view
     * @return the string that identifies of the view to display next
     */
    @GetMapping
    public final String viewList(final ModelMap model) {

        if (!this.isCurrentUserAdmin()) {
            return ProcessesController.REDIRECT_TO_ACCESS_DENIED;
        }

        return this.prepareModelForListView(model);
    }



    /**
     * Processes the request to display the details of a process.
     *
     * @param model              the data to display in the next view
     * @param id                 the number that identifies the process to display
     * @param redirectAttributes the data to pass to a view that the user may be redirected to
     * @return the string that identifies the view to display
     */
    @GetMapping("{id}")
    public final String viewItem(final ModelMap model, @PathVariable final int id,
            final RedirectAttributes redirectAttributes) {

        if (!this.isCurrentUserAdmin()) {
            return ProcessesController.REDIRECT_TO_ACCESS_DENIED;
        }

        Process domainProcess = this.processesRepository.findById(id).orElse(null);

        if (domainProcess == null) {
            this.logger.error("No process found in database with identifier {}.", id);
            this.addStatusMessage(redirectAttributes, "processesList.errors.process.notFound", MessageType.ERROR);

            return ProcessesController.REDIRECT_TO_LIST;
        }

        ProcessModel processModel = new ProcessModel(domainProcess, this.taskPluginsDiscoverer,
                this.requestsRepository);

        if (processModel.isReadOnly()) {
            this.addStatusMessage(model, "processDetails.readonly.info", MessageType.WARNING);
        }

        return this.prepareModelForDetailsView(model, false, processModel);
    }



    /**
     * Defines the generic model attributes that ensure a proper display of the process details view.
     * <b>Important:</b> This method does not set the attribute for the process itself. This must be set separately,
     * if necessary.
     *
     * @param model the data to display in the view
     * @param isNew <code>true</code> if the details view shows a process that is being created
     * @return the string that identifies the details view
     */
    private String prepareModelForDetailsView(final ModelMap model, final boolean isNew) {
        return this.prepareModelForDetailsView(model, isNew, null);
    }



    /**
     * Defines the generic model attributes that ensure a proper display of the process details view.
     *
     * @param model        the data to display in the view
     * @param isNew        <code>true</code> if the details view shows a process that is being created
     * @param processModel the model that represents the process to display in the details view, or
     *                     <code>null</code> not to define any process specifically (because it is set elsewhere,
     *                     for example)
     * @return the string that identifies the details view
     */
    private String prepareModelForDetailsView(final ModelMap model, final boolean isNew,
            final ProcessModel processModel) {
        assert model != null : "The model must be set.";

        model.addAttribute("isNew", isNew);

        this.addCurrentSectionToModel(ProcessesController.CURRENT_SECTION_IDENTIFIER, model);
        this.addJavascriptMessagesAttribute(model);

        Collection<ITaskProcessor> alltasks = this.taskPluginsDiscoverer.getTaskProcessorsOrderedByLabel().values();
        model.addAttribute("alltasks", alltasks.toArray(new ITaskProcessor[]{}));
        model.addAttribute("allactiveusers", this.getAllActiveUsers());
        model.addAttribute("allusergroups", this.getAllUserGroups());
        model.addAttribute("allremarks", this.getAllRemarks());

        if (processModel != null) {
            model.addAttribute("process", processModel);
        }

        return ProcessesController.DETAILS_VIEW;
    }



    /**
     * Defines the generic model attributes that ensure a proper display of the processes list view.
     *
     * @param model the data to display in the view
     * @return the string that identifies the list view
     */
    private String prepareModelForListView(final ModelMap model) {
        assert model != null : "The model must be set.";

        model.addAttribute("processes", ProcessModel.fromDomainObjectsCollection(this.processesRepository.findAll(),
                this.taskPluginsDiscoverer, this.requestsRepository));
        this.addJavascriptMessagesAttribute(model);
        this.addCurrentSectionToModel(ProcessesController.CURRENT_SECTION_IDENTIFIER, model);

        return ProcessesController.LIST_VIEW;
    }



    /**
     * Make the modifications to the current process permanent.
     *
     * @param processModel  the model that contains the modifications to the current process
     * @param domainProcess the data object for the current process
     */
    private void saveProcessModifications(final ProcessModel processModel,
            final Process domainProcess) {
        processModel.updateInDataSource(this.processesRepository, this.tasksRepository, this.usersRepository,
                                        this.userGroupsRepository, domainProcess);
    }

    /**
     * Fetches a list of users from the repository and returns a collection of active user objects.
     *
     * @return a list of existing active users
     */
    private List<UserModel> getAllActiveUsers() {
        final List<UserModel> usersList = new ArrayList<>();

        for (User domainUser : this.usersRepository.findAllActiveApplicationUsers()) {

            usersList.add(new UserModel(domainUser));
        }

        return usersList;
    }



    private Collection<UserGroup> getAllUserGroups() {
        return this.userGroupsRepository.findAllByOrderByName();
    }



    /**
     * Fetches a list of predefined remarks from the repository and returns a collection of predefined remarks objects.
     *
     * @return a list of existing predefined remarks
     */
    private List<RemarkModel> getAllRemarks() {
        final List<RemarkModel> remarksList = new ArrayList<>();

        for (Remark domainRemark : this.remarksRepository.findAllByOrderByTitle()) {
            remarksList.add(new RemarkModel(domainRemark, this.tasksRepository));
        }

        return remarksList;
    }

}
