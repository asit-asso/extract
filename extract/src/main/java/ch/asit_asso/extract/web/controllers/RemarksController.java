package ch.asit_asso.extract.web.controllers;

import ch.asit_asso.extract.domain.Remark;
import ch.asit_asso.extract.persistence.RemarkRepository;
import ch.asit_asso.extract.persistence.TasksRepository;
import ch.asit_asso.extract.web.Message;
import ch.asit_asso.extract.web.model.RemarkModel;
import ch.asit_asso.extract.web.validators.RemarkValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Objects;

/**
 * Web controller that processes requests related to the management of the predefined remarks.
 *
 * @author Yves Grasset
 */
@Controller
@Scope("session")
@RequestMapping("/remarks")
public class RemarksController extends BaseController {

    /**
     * The string that identifies the part of the web site that this controller manages.
     */
    private static final String CURRENT_SECTION_IDENTIFIER = "parameters";

    /**
     * The string that identifies the view to display the information about one predefined remark.
     */
    private static final String DETAILS_VIEW = "remarks/details";

    /**
     * The string that identifies the view to display all the predefined remarks.
     */
    private static final String LIST_VIEW = "remarks/list";

    /**
     * The string that tells this controller to redirect the user to the view that shows all the predefined remarks.
     */
    private static final String REDIRECT_TO_LIST = "redirect:/remarks";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(RemarksController.class);

    /**
     * The Spring Data repository that links the predefined remark data objects to the data source.
     */
    @Autowired
    private RemarkRepository remarksRepository;

    /**
     * The Spring Data repository that links the task data objects to the data source.
     */
    @Autowired
    private TasksRepository tasksRepository;



    /**
     * Defines the links between form data and Java objects.
     *
     * @param binder the object that makes the link between web forms data and Java beans
     */
    @InitBinder("remark")
    public final void initBinder(final WebDataBinder binder) {
        binder.setValidator(new RemarkValidator(this.remarksRepository));
    }



    /**
     * Processes the data submitted to create a predefined remark.
     *
     * @param remarkModel          the data submitted for the creation
     * @param bindingResult      an object that assembles the result of the predefined remark data validation
     * @param model              the data to display in the next view
     * @param redirectAttributes the data to pass to the next page if a redirection is necessary
     * @return the string that identifies the view to display next
     */
    @PostMapping("add")
    public final String addItem(@Valid @ModelAttribute("remark") final RemarkModel remarkModel,
                                final BindingResult bindingResult, final ModelMap model,
                                final RedirectAttributes redirectAttributes) {

        this.logger.debug("Processing a request to add a predefined remark");

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (remarkModel.getId() != null) {
            this.logger.warn("The user {} tried to add a predefined remark, but the identifier in the data model was set to {}."
                            + " The data may have been tampered with, so the operation is denied.", this.getCurrentUserLogin(),
                            remarkModel.getId());
            this.addStatusMessage(redirectAttributes, "remarksList.errors.user.edit.invalidData", Message.MessageType.ERROR);

            return RemarksController.REDIRECT_TO_LIST;
        }

        if (bindingResult.hasErrors()) {
            this.logger.info("Adding the predefined remark failed because of invalid data.");

            return this.prepareModelForDetailsView(model, false, null, redirectAttributes);
        }

        Remark domainRemark = remarkModel.createDomainObject();
        boolean success;

        try {
            domainRemark = this.remarksRepository.save(domainRemark);
            success = (domainRemark != null);

        } catch (Exception exception) {
            this.logger.error("Could not save the new predefined remark.", exception);
            success = false;
        }

        if (!success) {
            this.addStatusMessage(model, "remarkDetails.errors.remark.add.failed", Message.MessageType.ERROR);

            return this.prepareModelForDetailsView(model, false, null, redirectAttributes);
        }

        this.addStatusMessage(redirectAttributes, "remarksList.remark.added", Message.MessageType.SUCCESS);

        return RemarksController.REDIRECT_TO_LIST;
    }



    /**
     * Removes a predefined remark from the data source.
     *
     * @param id                 the number that identifies the predefined remark to delete
     * @param title              the title of the predefined remark to delete
     * @param redirectAttributes the data to pass to the next page
     * @return the string that identifies the view to display next
     */
    @PostMapping("delete")
    public final String deleteItem(@RequestParam final int id, @RequestParam final String title,
                                   final RedirectAttributes redirectAttributes) {

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        Remark domainRemark = this.remarksRepository.findById(id).orElse(null);

        if (domainRemark == null || !Objects.equals(domainRemark.getTitle(), title)) {
            this.addStatusMessage(redirectAttributes, "remarksList.errors.remark.notFound", Message.MessageType.ERROR);

            return RemarksController.REDIRECT_TO_LIST;
        }

        RemarkModel remarkModel = new RemarkModel(domainRemark, tasksRepository);

        if (!remarkModel.isDeletable()) {
            this.addStatusMessage(redirectAttributes, "remarksList.errors.remark.delete.hasProcesses", Message.MessageType.ERROR);

            return RemarksController.REDIRECT_TO_LIST;
        }

        boolean success;

        try {
            this.remarksRepository.delete(domainRemark);
            success = true;

        } catch (Exception exception) {
            this.logger.error("Could not delete remark \"{}\" (ID: {}).", title, id, exception);
            success = false;
        }

        if (!success) {
            this.addStatusMessage(redirectAttributes, "remarksList.errors.remark.delete.failed", Message.MessageType.ERROR);

            return RemarksController.REDIRECT_TO_LIST;
        }

        this.addStatusMessage(redirectAttributes, "remarksList.remark.deleted", Message.MessageType.SUCCESS);

        return RemarksController.REDIRECT_TO_LIST;
    }



    /**
     * Processes the data submitted to modify an existing predefined remark.
     *
     * @param remarkModel        the predefined remark data submitted for the update
     * @param bindingResult      an object assembling the result of the predefined remark data validation
     * @param model              the data to display in the next view
     * @param id                 the number that identifies the predefined remark to update
     * @param redirectAttributes the data to pass to the next page if a redirection is necessary
     * @return the string that identifies the view to display next
     */
    @PostMapping("{id}")
    public final String updateItem(@Valid @ModelAttribute("remark") final RemarkModel remarkModel,
                                   final BindingResult bindingResult, final ModelMap model, @PathVariable final int id,
                                   final RedirectAttributes redirectAttributes) {
        this.logger.debug("Processing the data to update a predefined remark.");

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (id != remarkModel.getId()) {
            this.logger.warn("The user {} tried to update predefined remark id {}, but the data was set for predefined " +
                            "remark id {}. The data may have been tampered with, so the operation is denied.",
                    this.getCurrentUserLogin(), id, remarkModel.getId());
            this.addStatusMessage(redirectAttributes, "remarksList.errors.remark.edit.invalidData",
                    Message.MessageType.ERROR);

            return RemarksController.REDIRECT_TO_LIST;
        }

        if (bindingResult.hasErrors()) {
            this.logger.info("Updating the predefined remark failed because of invalid data.");

            return this.prepareModelForDetailsView(model, false, id, redirectAttributes);
        }

        this.logger.debug("Fetching the predefined remark to update.");
        Remark domainRemark = this.remarksRepository.findById(id).orElse(null);

        if (domainRemark == null) {
            this.logger.error("No predefined remark found in database with id {}.", id);
            this.addStatusMessage(redirectAttributes, "remarksList.errors.remark.notFound",
                    Message.MessageType.ERROR);

            return RemarksController.REDIRECT_TO_LIST;
        }

        remarkModel.updateDomainObject(domainRemark);
        boolean success;

        try {
            domainRemark = this.remarksRepository.save(domainRemark);
            success = (domainRemark != null);

        } catch (Exception exception) {
            this.logger.error("Could not update user with id {}.", id, exception);
            success = false;
        }

        if (!success) {
            this.addStatusMessage(model, "remarkDetails.errors.remark.update.failed", Message.MessageType.ERROR);

            return this.prepareModelForDetailsView(model, false, id, redirectAttributes);
        }

        this.addStatusMessage(redirectAttributes, "remarksList.remark.updated", Message.MessageType.SUCCESS);

        return RemarksController.REDIRECT_TO_LIST;
    }



    /**
     * Processes a request to show the view to create a new predefined remark.
     *
     * @param model the data to pass to the next view
     * @return the string that identifies the view to display next
     */
    @GetMapping("add")
    public final String viewAddForm(final ModelMap model) {

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        return this.prepareModelForDetailsView(model, true, null, null);
    }



    /**
     * Processes a request to show the details of a predefined remark.
     *
     * @param model              the data to display in the view
     * @param id                 the number that identifies the predefined remark to show
     * @param redirectAttributes the data to pass to the next page if a redirection is necessary
     * @return the string that identifies the view to display next
     */
    @GetMapping("{id}")
    public final String viewItem(final ModelMap model, @PathVariable final int id,
                                 final RedirectAttributes redirectAttributes) {

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        return this.prepareModelForDetailsView(model, true, id, redirectAttributes);
    }



    /**
     * Processes a request to display all the application predefined remarks.
     *
     * @param model the data to display in the next view
     * @param redirectAttributes the data to pass to the next page if a redirection is necessary
     * @return the string that identifies the next view to display
     */
    @GetMapping
    public final String viewList(final ModelMap model, final RedirectAttributes redirectAttributes) {

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        return this.prepareModelForListView(model, redirectAttributes);
    }



    /**
     * Carries the actions to display a predefined remark in the details view.
     *
     * @param model              the data to display in the model view
     * @param createModel        <code>true</code> to if a new representation of the remark must be created. If
     *                           <code>false</code> is passed, the <code>user</code> attribute of the model will be
     *                           left as is
     * @param id                 the number that identifies the remark to display in the details view, or
     *                           <code>null</code> if the remark is a new one
     * @param redirectAttributes the data to pass to the next if a redirection is necessary. <code>null</code> can be
     *                           passed only if the remark to display is a new one (because there won't be a redirection.
     * @return the string that identifies the next view
     */
    private String prepareModelForDetailsView(final ModelMap model, final boolean createModel, final Integer id,
                                              final RedirectAttributes redirectAttributes) {
        assert redirectAttributes != null || id == null :
                "The redirect attributes must be set if the remark to display is not a new one.";

        String currentSection = RemarksController.CURRENT_SECTION_IDENTIFIER;

        try {
            if (id == null) {

                if (createModel) {
                    model.addAttribute("remark", new RemarkModel());
                    model.addAttribute("isNew", true);
                }

            } else {
                Remark domainRemark = this.remarksRepository.findById(id).orElse(null);

                if (domainRemark == null) {
                    this.addStatusMessage(redirectAttributes, "remarksList.errors.remark.notFound",
                            Message.MessageType.ERROR);

                    return RemarksController.REDIRECT_TO_LIST;
                }

                if (createModel) {
                    model.addAttribute("remark", new RemarkModel(domainRemark, this.tasksRepository));
                }

                model.addAttribute("isNew", false);
            }

            this.addCurrentSectionToModel(currentSection, model);

        } catch (Exception exception) {
            this.logger.error("An error occurred when the remark details view was prepared.", exception);
            this.addStatusMessage(redirectAttributes, "remarksList.errors.remark.general",
                    Message.MessageType.ERROR);
            return RemarksController.REDIRECT_TO_LIST;
        }

        return RemarksController.DETAILS_VIEW;
    }



    /**
     * Defines the generic model attributes that ensure a proper display of the predefined remarks list view.
     *
     * @param model the data to display in the view
     * @param redirectAttributes the data to pass to the next if a redirection is necessary.
     * @return the string that identifies the list view
     */
    private String prepareModelForListView(final ModelMap model, final RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute("remarks",
                    RemarkModel.fromDomainObjectsCollection(this.remarksRepository.findAll(),
                            this.tasksRepository));
            this.addJavascriptMessagesAttribute(model);
            this.addCurrentSectionToModel(RemarksController.CURRENT_SECTION_IDENTIFIER, model);

        } catch (Exception exception) {
            this.logger.error("An error occurred when the remarks list view was prepared.", exception);
            this.addStatusMessage(redirectAttributes, "remarksList.errors.remark.general",
                    Message.MessageType.ERROR);
            return RemarksController.REDIRECT_TO_HOME;
        }

        return RemarksController.LIST_VIEW;
    }
}
