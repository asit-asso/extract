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

import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.persistence.UserGroupsRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.web.Message.MessageType;
import ch.asit_asso.extract.web.model.UserGroupModel;
import ch.asit_asso.extract.web.validators.UserGroupValidator;
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
 * Web controller that processes requests related to the management of the application user groups.
 *
 * @author Yves Grasset
 */
@Controller
@Scope("session")
@RequestMapping("/userGroups")
public class UserGroupsController extends BaseController {

    /**
     * The string that identifies the part of the web site that this controller manages.
     */
    private static final String CURRENT_SECTION_IDENTIFIER = "users";

    /**
     * The string that identifies the view to display the information about one user group.
     */
    private static final String DETAILS_VIEW = "userGroups/details";

    /**
     * The string that identifies the view to display all the application user groups.
     */
    private static final String LIST_VIEW = "userGroups/list";

    /**
     * The string that tells this controller to redirect the user to the view that shows all the users.
     */
    private static final String REDIRECT_TO_LIST = "redirect:/userGroups";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(UserGroupsController.class);

    /**
     * The Spring Data repository that links the user group data objects to the data source.
     */
    @Autowired
    private UserGroupsRepository userGroupsRepository;

    /**
     * The Spring Data repository that links the user data objects to the data source.
     */
    @Autowired
    private UsersRepository usersRepository;



    /**
     * Defines the links between form data and Java objects.
     *
     * @param binder the object that makes the link between web forms data and Java beans
     */
    @InitBinder("userGroup")
    public final void initBinder(final WebDataBinder binder) {
        binder.setValidator(new UserGroupValidator(this.userGroupsRepository, this.usersRepository));
    }



    /**
     * Processes the data submitted to create a user.
     *
     * @param userGroupModel     the data submitted for the creation
     * @param bindingResult      an object that assembles the result of the user group data validation
     * @param model              the data to display in the next view
     * @param redirectAttributes the data to pass to the next page if a redirection is necessary
     * @return the string that identifies the view to display next
     */
    @PostMapping("add")
    public final String addItem(@Valid @ModelAttribute("userGroup") final UserGroupModel userGroupModel,
            final BindingResult bindingResult, final ModelMap model, final RedirectAttributes redirectAttributes) {

        this.logger.debug("Processing a request to add a user group");

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        if (userGroupModel.getId() != null) {
            this.logger.warn("The user {} tried to add a user group, but the identifier in the data model was set to {}."
                    + " The data may have been tampered with, so the operation is denied.", this.getCurrentUserLogin(),
                    userGroupModel.getId());
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData",
                                  MessageType.ERROR);

            return UserGroupsController.REDIRECT_TO_LIST;
        }

        if (!userGroupModel.isBeingCreated()) {
            this.logger.warn("The user {} tried to add a user group, but the beingCreated flag was set to false."
                    + " The data may have been tampered with, so the operation is denied.", this.getCurrentUserLogin());
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData",
                                  MessageType.ERROR);

            return UserGroupsController.REDIRECT_TO_LIST;
        }

        if (bindingResult.hasErrors()) {
            this.logger.info("Adding the user group failed because of invalid data.");

            return this.prepareModelForDetailsView(model, false, null, redirectAttributes);
        }

        UserGroup domainUserGroup = userGroupModel.createDomainObject(this.usersRepository);
        boolean success;

        try {
            domainUserGroup = this.userGroupsRepository.save(domainUserGroup);
            success = (domainUserGroup != null);

        } catch (Exception exception) {
            this.logger.error("Could not save the new user group.", exception);
            success = false;
        }

        if (!success) {
            this.addStatusMessage(model, "userGroupDetails.errors.userGroup.add.failed", MessageType.ERROR);

            return this.prepareModelForDetailsView(model, false, null, redirectAttributes);
        }

        this.addStatusMessage(redirectAttributes, "userGroupsList.userGroup.added", MessageType.SUCCESS);

        return UserGroupsController.REDIRECT_TO_LIST;
    }



    /**
     * Removes a user group from the data source.
     *
     * @param id                 the number that identifies the user group to delete
     * @param name               the string that identifies the user group to delete
     * @param redirectAttributes the data to pass to the next page
     * @return the string that identifies the view to display next
     */
    @PostMapping("delete")
    public final String deleteItem(@RequestParam final int id, @RequestParam final String name,
            final RedirectAttributes redirectAttributes) {

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        UserGroup domainGroupUser = this.userGroupsRepository.findById(id).orElse(null);

        if (domainGroupUser == null || !Objects.equals(domainGroupUser.getName(), name)) {
            this.addStatusMessage(redirectAttributes, "userGroupsList.errors.userGroup.notFound",
                                  MessageType.ERROR);

            return UserGroupsController.REDIRECT_TO_LIST;
        }

        if (domainGroupUser.isAssociatedToProcesses()) {
            this.addStatusMessage(redirectAttributes, "userGroupsList.errors.userGroup.delete.hasProcesses",
                                  MessageType.ERROR);

            return UserGroupsController.REDIRECT_TO_LIST;
        }

        boolean success;

        try {
            this.userGroupsRepository.delete(domainGroupUser);
            success = true;

        } catch (Exception exception) {
            this.logger.error("Could not delete user {} (ID: {}).", name, id, exception);
            success = false;
        }

        if (!success) {
            this.addStatusMessage(redirectAttributes, "userGroupsList.errors.userGroup.delete.failed",
                                  MessageType.ERROR);

            return UserGroupsController.REDIRECT_TO_LIST;
        }

        this.addStatusMessage(redirectAttributes, "userGroupsList.userGroup.deleted", MessageType.SUCCESS);

        return UserGroupsController.REDIRECT_TO_LIST;
    }



    /**
     * Processes the data submitted to modify an existing user group.
     *
     * @param userModel          the user group data submitted for the update
     * @param bindingResult      an object assembling the result of the user group data validation
     * @param model              the data to display in the next view
     * @param id                 the number that identifies the user group to update
     * @param redirectAttributes the data to pass to the next page if a redirection is necessary
     * @return the string that identifies the view to display next
     */
    @PostMapping("{id}")
    public final String updateItem(@Valid @ModelAttribute("userGroup") final UserGroupModel userGroupModel,
            final BindingResult bindingResult, final ModelMap model, @PathVariable final int id,
            final RedirectAttributes redirectAttributes) {
        this.logger.debug("Processing the data to update a user group.");

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        final String currentUser = this.getCurrentUserLogin();
        final String redirectTarget = UserGroupsController.REDIRECT_TO_LIST;

        if (id != userGroupModel.getId()) {
            this.logger.warn("The user {} tried to update user group id {}, but the data was set for user group id {}."
                    + " The data may have been tampered with, so the operation is denied.",
                    currentUser, id, userGroupModel.getId());
            this.addStatusMessage(redirectAttributes, "userGroupsList.errors.userGroup.edit.invalidData",
                                  MessageType.ERROR);

            return redirectTarget;
        }

        if (userGroupModel.isBeingCreated()) {
            this.logger.warn("The user {} tried to update user id {}, but the beingCreated flag was set to true."
                    + " The data may have been tampered with, so the operation is denied.", currentUser, id);
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData",
                                  MessageType.ERROR);

            return redirectTarget;
        }

        if (bindingResult.hasErrors()) {
            this.logger.info("Updating the user failed because of invalid data.");

            return this.prepareModelForDetailsView(model, false, id, redirectAttributes);
        }

        this.logger.debug("Fetching the user group to update.");
        UserGroup domainUserGroup = this.userGroupsRepository.findById(id).orElse(null);

        if (domainUserGroup == null) {
            this.logger.error("No user group found in database with id {}.", id);
            this.addStatusMessage(redirectAttributes, "userGroupsList.errors.userGroup.notFound", MessageType.ERROR);

            return redirectTarget;
        }

        userGroupModel.updateDomainObject(domainUserGroup, this.usersRepository);
        boolean success;

        try {
            domainUserGroup = this.userGroupsRepository.save(domainUserGroup);
            success = (domainUserGroup != null);

        } catch (Exception exception) {
            this.logger.error("Could not update user with id {}.", id, exception);
            success = false;
        }

        if (!success) {
            this.addStatusMessage(model, "userGroupDetails.errors.userGroup.update.failed", MessageType.ERROR);

            return this.prepareModelForDetailsView(model, false, id, redirectAttributes);
        }

        this.addStatusMessage(redirectAttributes, "userGroupsList.userGroup.updated", MessageType.SUCCESS);

        return redirectTarget;
    }



    /**
     * Processes a request to show the view to create a new user group.
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
     * Processes a request to show the details of a user group.
     *
     * @param model              the data to display in the view
     * @param id                 the number that identifies the user group to show
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
     * Processes a request to display all the application user groups.
     *
     * @param model the data to display in the next view
     * @return the string that identifies the next view to display
     */
    @GetMapping
    public final String viewList(final ModelMap model) {

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        return this.prepareModelForListView(model);
    }



    /**
     * Carries the actions to display a user group in the details view.
     *
     * @param model              the data to display in the model view
     * @param createModel        <code>true</code> to if a new representation of the user group must be created. If
     *                           <code>false</code> is passed, the <code>userGroup</code> attribute of the model will be
     *                           left as is
     * @param id                 the number that identifies the user group to display in the details view, or
     *                           <code>null</code> if the user group is a new one
     * @param redirectAttributes the data to pass to the next if a redirection is necessary. <code>null</code> can be
     *                           passed only if the user group to display is a new one (because there won't be a
     *                           redirection).
     * @return the string that identifies the next view
     */
    private String prepareModelForDetailsView(final ModelMap model, final boolean createModel, final Integer id,
                                              final RedirectAttributes redirectAttributes) {
        assert redirectAttributes != null || id == null :
                "The redirect attributes must be set if the user group to display is not a new one.";

        try {
            String currentSection = UserGroupsController.CURRENT_SECTION_IDENTIFIER;

            if (id == null) {

                if (createModel) {
                    model.addAttribute("userGroup", new UserGroupModel());
                }

            } else {
                UserGroup domainUserGroup = this.userGroupsRepository.findById(id).orElse(null);

                if (domainUserGroup == null) {
                    this.addStatusMessage(redirectAttributes, "userGroupsList.errors.userGroup.notFound",
                                          MessageType.ERROR);

                    return UserGroupsController.REDIRECT_TO_LIST;
                }

                if (createModel) {
                    model.addAttribute("userGroup", new UserGroupModel(domainUserGroup));
                }
            }

            model.addAttribute("allactiveusers", this.usersRepository.findAllApplicationUsers());
            this.addCurrentSectionToModel(currentSection, model);

        } catch (Exception exception) {
            this.logger.error("An error occurred when the details view was prepared.", exception);
            throw new RuntimeException(exception);
        }

        return UserGroupsController.DETAILS_VIEW;
    }



    /**
     * Defines the generic model attributes that ensure a proper display of the user groups list view.
     *
     * @param model the data to display in the view
     * @return the string that identifies the list view
     */
    private String prepareModelForListView(final ModelMap model) {
        model.addAttribute("userGroups", this.userGroupsRepository.findAll());
        this.addJavascriptMessagesAttribute(model);
        this.addCurrentSectionToModel(UserGroupsController.CURRENT_SECTION_IDENTIFIER, model);

        return UserGroupsController.LIST_VIEW;
    }

}
