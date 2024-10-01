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

import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorApplication;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorBackupCodes;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorRememberMe;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorService;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.UserType;
import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.persistence.RecoveryCodeRepository;
import ch.asit_asso.extract.persistence.RememberMeTokenRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.utils.Secrets;
import ch.asit_asso.extract.utils.UrlUtils;
import ch.asit_asso.extract.web.Message.MessageType;
import ch.asit_asso.extract.web.model.UserModel;
import ch.asit_asso.extract.web.validators.UserValidator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
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
 * Web controller that processes requests related to the management of the application users.
 *
 * @author Yves Grasset
 */
@Controller
@Scope("session")
@RequestMapping("/users")
public class UsersController extends BaseController {

    /**
     * The string that identifies the part of the website that this controller manages.
     */
    private static final String CURRENT_SECTION_IDENTIFIER = "users";

    /**
     * The string that identifies the part of the website that this controller manages.
     */
    private static final String CURRENT_USER_SECTION_IDENTIFIER = "currentUser";

    /**
     * The string that identifies the view to display the information about one user.
     */
    private static final String DETAILS_VIEW = "users/details";

    /**
     * The string that identifies the view to display all the application users.
     */
    private static final String LIST_VIEW = "users/list";

    /**
     * The string that tells this controller to redirect the user to the view that shows all the users.
     */
    private static final String REDIRECT_TO_LIST = "redirect:/users";

    public static final String REDIRECT_TO_2FA_REGISTER = "redirect:/2fa/register";

    private final String applicationPath;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(UsersController.class);

    private final RecoveryCodeRepository backupCodesRepository;

    /**
     * The Spring Security object that allows to hash passwords.
     */
    private final Secrets secrets;

    private final RememberMeTokenRepository rememberMeRepository;

    private final TwoFactorService twoFactorService;

    /**
     * The Spring Data repository that links the user data objects to the data source.
     */
    private final UsersRepository usersRepository;

    private final LdapSettings ldapSettings;



    public UsersController(RecoveryCodeRepository codesRepository, Secrets secrets,
                           RememberMeTokenRepository tokensRepository, TwoFactorService twoFactorService,
                           UsersRepository usersRepository, LdapSettings ldapSettings, Environment environment) {
        this.backupCodesRepository = codesRepository;
        this.secrets = secrets;
        this.rememberMeRepository = tokensRepository;
        this.twoFactorService = twoFactorService;
        this.usersRepository = usersRepository;
        this.ldapSettings = ldapSettings;
        this.applicationPath = UrlUtils.getApplicationPath(environment.getProperty("application.external.url"));
    }


    /**
     * Defines the links between form data and Java objects.
     *
     * @param binder the object that makes the link between web forms data and Java beans
     */
    @InitBinder("user")
    public final void initBinder(final WebDataBinder binder) {
        binder.setValidator(new UserValidator(this.usersRepository));
    }



    /**
     * Processes the data submitted to create a user.
     *
     * @param userModel          the data submitted for the creation
     * @param bindingResult      an object that assembles the result of the user data validation
     * @param model              the data to display in the next view
     * @param redirectAttributes the data to pass to the next page if a redirection is necessary
     * @return the string that identifies the view to display next
     */
    @PostMapping("add")
    public final String addItem(@Valid @ModelAttribute("user") final UserModel userModel,
            final BindingResult bindingResult, final ModelMap model, final RedirectAttributes redirectAttributes) {

        this.logger.debug("Processing a request to add a user");

        if (!this.isCurrentUserAdmin()) {
            return UsersController.REDIRECT_TO_ACCESS_DENIED;
        }

        if (userModel.getId() != null) {
            this.logger.warn("The user {} tried to add a user, but the identifier in the data model was set to {}."
                    + " The data may have been tampered with, so the operation is denied.", this.getCurrentUserLogin(),
                    userModel.getId());
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData", MessageType.ERROR);

            return UsersController.REDIRECT_TO_LIST;
        }

        if (!userModel.isBeingCreated()) {
            this.logger.warn("The user {} tried to add a user, but the beingCreated flag was set to false."
                    + " The data may have been tampered with, so the operation is denied.", this.getCurrentUserLogin());
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData", MessageType.ERROR);

            return UsersController.REDIRECT_TO_LIST;
        }

        if (userModel.getUserType() != UserType.LOCAL) {
            this.logger.warn("The user {} tried to add a user with a user type different than LOCAL."
                                     + " The data may have been tampered with, so the operation is denied.", this.getCurrentUserLogin());
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData", MessageType.ERROR);

            return UsersController.REDIRECT_TO_LIST;
        }


        if (bindingResult.hasErrors()) {
            this.logger.info("Adding the user failed because of invalid data.");

            return this.prepareModelForDetailsView(model, false, null, redirectAttributes);
        }

        User domainUser = userModel.createDomainObject(this.secrets, this.twoFactorService);
        boolean success;

        try {
            domainUser = this.usersRepository.save(domainUser);
            success = (domainUser != null);

        } catch (Exception exception) {
            this.logger.error("Could not save the new user.", exception);
            success = false;
        }

        if (!success) {
            this.addStatusMessage(model, "userDetails.errors.user.add.failed", MessageType.ERROR);

            return this.prepareModelForDetailsView(model, false, null, redirectAttributes);
        }

        this.addStatusMessage(redirectAttributes, "usersList.user.added", MessageType.SUCCESS);

        return UsersController.REDIRECT_TO_LIST;
    }



    /**
     * Removes a user from the data source.
     *
     * @param id                 the number that identifies the user to delete
     * @param login              the string that identifies the user to delete
     * @param redirectAttributes the data to pass to the next page
     * @return the string that identifies the view to display next
     */
    @PostMapping("delete")
    public final String deleteItem(@RequestParam final int id, @RequestParam final String login,
            final RedirectAttributes redirectAttributes) {

        if (!this.isCurrentUserAdmin()) {
            return UsersController.REDIRECT_TO_ACCESS_DENIED;
        }

        if (id == this.usersRepository.getSystemUserId()) {
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notDeletable", MessageType.ERROR);

            return UsersController.REDIRECT_TO_LIST;
        }

        if (id == this.getCurrentUserId()) {
            this.addStatusMessage(redirectAttributes, "usersList.errors.currentUser.delete", MessageType.ERROR);

            return UsersController.REDIRECT_TO_LIST;
        }

        User domainUser = this.usersRepository.findById(id).orElse(null);

        if (domainUser == null || !Objects.equals(domainUser.getLogin(), login)) {
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notFound", MessageType.ERROR);

            return UsersController.REDIRECT_TO_LIST;
        }

        if (domainUser.isAssociatedToProcesses()) {
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.delete.hasProcesses",
                                  MessageType.ERROR);

            return UsersController.REDIRECT_TO_LIST;
        }

        if (domainUser.isLastActiveMemberOfProcessGroup()) {
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.delete.lastActiveMember",
                                  MessageType.ERROR);

            return UsersController.REDIRECT_TO_LIST;
        }

        boolean success;

        try {
            this.usersRepository.delete(domainUser);
            success = true;

        } catch (Exception exception) {
            this.logger.error("Could not delete user {} (ID: {}).", login, id, exception);
            success = false;
        }

        if (!success) {
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.delete.failed", MessageType.ERROR);

            return UsersController.REDIRECT_TO_LIST;
        }

        this.addStatusMessage(redirectAttributes, "usersList.user.deleted", MessageType.SUCCESS);

        return UsersController.REDIRECT_TO_LIST;
    }



    /**
     * Processes the data submitted to modify an existing user.
     *
     * @param userModel          the user data submitted for the update
     * @param bindingResult      an object assembling the result of the user data validation
     * @param model              the data to display in the next view
     * @param id                 the number that identifies the user to update
     * @param redirectAttributes the data to pass to the next page if a redirection is necessary
     * @return the string that identifies the view to display next
     */
    @PostMapping("{id}")
    public final String updateItem(@Valid @ModelAttribute("user") final UserModel userModel,
                                   final BindingResult bindingResult, final ModelMap model, @PathVariable final int id,
                                   final RedirectAttributes redirectAttributes, final HttpServletRequest request,
                                   final HttpServletResponse response) {
        this.logger.debug("Processing the data to update a user.");

        if (!this.canEditUser(id)) {
            return UsersController.REDIRECT_TO_ACCESS_DENIED;
        }

        final String currentUser = this.getCurrentUserLogin();
        final String redirectTarget = (this.isCurrentUserAdmin()) ? UsersController.REDIRECT_TO_LIST
                : UsersController.REDIRECT_TO_HOME;

//        if (id == this.usersRepository.getSystemUserId()) {
//            this.logger.warn("The user {} tried to edit the details of the system user.", currentUser);
//            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notEditable", MessageType.ERROR);
//
//            return redirectTarget;
//        }

        if (id != userModel.getId()) {
            this.logger.warn("The user {} tried to update user id {}, but the data was set for user id {}."
                    + " The data may have been tampered with, so the operation is denied.",
                    currentUser, id, userModel.getId());
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData", MessageType.ERROR);

            return redirectTarget;
        }

        if (userModel.isBeingCreated()) {
            this.logger.warn("The user {} tried to update user id {}, but the beingCreated flag was set to true."
                    + " The data may have been tampered with, so the operation is denied.", currentUser, id);
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData", MessageType.ERROR);

            return redirectTarget;
        }

        if (bindingResult.hasErrors()) {
            this.logger.info("Updating the user failed because of invalid data.");

            return this.prepareModelForDetailsView(model, false, id, redirectAttributes);
        }

        this.logger.debug("Fetching the user to update.");
        User domainUser = this.usersRepository.findById(id).orElse(null);

        if (domainUser == null) {
            this.logger.error("No user found in database with id {}.", id);
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notFound", MessageType.ERROR);

            return redirectTarget;
        }

        boolean displayWizard = this.isEditingCurrentUser(userModel)
                                && userModel.isTwoFactorForced() && !domainUser.isTwoFactorForced();

        userModel.updateDomainObject(domainUser, this.secrets, this.twoFactorService,
                                     (userModel.getId() == this.getCurrentUserId()), this.isCurrentUserAdmin());

        boolean success;

        try {
            domainUser = this.usersRepository.save(domainUser);

            if (domainUser.getTwoFactorStatus() == User.TwoFactorStatus.INACTIVE) {
                TwoFactorRememberMe rememberMeUser = new TwoFactorRememberMe(domainUser, this.rememberMeRepository,
                                                                             this.secrets, this.applicationPath);
                rememberMeUser.disable(request, response);
                TwoFactorBackupCodes backupCodesUser = new TwoFactorBackupCodes(domainUser, this.backupCodesRepository,
                                                                                this.secrets);
                backupCodesUser.delete();

            }
            success = (domainUser != null);

        } catch (Exception exception) {
            this.logger.error("Could not update user with id {}.", id, exception);
            success = false;
        }

        if (!success) {
            this.addStatusMessage(model, "userDetails.errors.user.update.failed", MessageType.ERROR);

            return this.prepareModelForDetailsView(model, false, id, redirectAttributes);
        }

        this.addStatusMessage(redirectAttributes, "usersList.user.updated", MessageType.SUCCESS);

        if (displayWizard) {
            return this.prepareFor2faWizard(request);
        }

        return redirectTarget;
    }



    @NotNull
    private String prepareFor2faWizard(HttpServletRequest request) {
        request.getSession().setAttribute("2faStep", "REGISTER");

        return UsersController.REDIRECT_TO_2FA_REGISTER;
    }



    private boolean isEditingCurrentUser(UserModel userModel) {
        return this.getCurrentUserLogin().equals(userModel.getLogin());
    }



    @PostMapping("{id}/migrate")
    public final String migrateUserToLdap(@ModelAttribute("user") final UserModel userModel,
                                          final ModelMap model, @PathVariable final int id,
                                          final RedirectAttributes redirectAttributes) {

        this.logger.debug("Processing the data to migrate a user to LDAP.");

        if (!this.canEditUser(id)) {
            return UsersController.REDIRECT_TO_ACCESS_DENIED;
        }

        final String currentUser = this.getCurrentUserLogin();
        final String redirectTarget = (this.isCurrentUserAdmin()) ? UsersController.REDIRECT_TO_LIST
                : UsersController.REDIRECT_TO_HOME;

        if (id == this.usersRepository.getSystemUserId()) {
            this.logger.warn("The user {} tried to migrate the system user to LDAP.", currentUser);
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notEditable", MessageType.ERROR);

            return redirectTarget;
        }

        if (id != userModel.getId()) {
            this.logger.warn("The user {} tried to migrate user id {}, but the data was set for user id {}."
                                     + " The data may have been tampered with, so the operation is denied.",
                             currentUser, id, userModel.getId());
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData", MessageType.ERROR);

            return redirectTarget;
        }

        this.logger.debug("Fetching the user to migrate.");
        User domainUser = this.usersRepository.findById(id).orElse(null);

        if (domainUser == null) {
            this.logger.error("No user found in database with id {}.", id);
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notFound", MessageType.ERROR);

            return redirectTarget;
        }

        domainUser.setUserType(UserType.LDAP);
        domainUser.setPassword(null);

        domainUser = this.usersRepository.save(domainUser);

        if (domainUser == null) {
            this.addStatusMessage(model, "userDetails.errors.user.migration.failed", MessageType.ERROR);

            return this.prepareModelForDetailsView(model, false, id, redirectAttributes);
        }

        this.addStatusMessage(redirectAttributes, "usersList.user.migrated", MessageType.SUCCESS);

        return redirectTarget;
    }



    @PostMapping("{id}/disable2fa")
    public final String disableTwoFactorAuthentication(@ModelAttribute("user") final UserModel userModel,
                                                       final ModelMap model, @PathVariable final int id,
                                                       final RedirectAttributes redirectAttributes) {

        this.logger.debug("Processing the data to disable two-factor authentication for a user.");

        if (!this.canEditUser(id)) {
            return UsersController.REDIRECT_TO_ACCESS_DENIED;
        }

        final String currentUser = this.getCurrentUserLogin();
        final String redirectTarget = (this.isCurrentUserAdmin()) ? UsersController.REDIRECT_TO_LIST
                : UsersController.REDIRECT_TO_HOME;

        if (id == this.usersRepository.getSystemUserId()) {
            this.logger.warn("The user {} tried to disable two-factor authentication for the system user.", currentUser);
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notEditable", MessageType.ERROR);

            return redirectTarget;
        }

        if (id != userModel.getId()) {
            this.logger.warn("The user {} tried to disable two-factor authentication for user id {}, but the data was set"
                             + " for user id {}. The data may have been tampered with, so the operation is denied.",
                             currentUser, id, userModel.getId());
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData", MessageType.ERROR);

            return redirectTarget;
        }

        this.logger.debug("Fetching the user.");
        User domainUser = this.usersRepository.findById(id).orElse(null);

        if (domainUser == null) {
            this.logger.error("No user found in database with id {}.", id);
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notFound", MessageType.ERROR);

            return redirectTarget;
        }

        if (domainUser.getTwoFactorStatus() == User.TwoFactorStatus.INACTIVE) {
            this.logger.warn("The user {} tried to disable two-factor authentication of user {} for which it was"
                             + " already off.",
                             currentUser, domainUser.getLogin());
            this.addStatusMessage(redirectAttributes, "usersList.errors.operation.illegal", MessageType.ERROR);

            return redirectTarget;
        }

        if (domainUser.isTwoFactorForced() && !this.isCurrentUserAdmin()) {
            this.logger.warn("The user {} tried to disable their forced two-factor authentication of user {} while not"
                             + " being an administrator.",
                             currentUser, domainUser.getLogin());
            this.addStatusMessage(redirectAttributes, "usersList.errors.operation.illegal", MessageType.ERROR);

            return redirectTarget;
        }

        TwoFactorApplication twoFactorApplication = new TwoFactorApplication(domainUser, this.secrets,
                                                                             this.twoFactorService);
        twoFactorApplication.disable();

        domainUser = this.usersRepository.save(domainUser);

        if (domainUser == null) {
            this.addStatusMessage(model, "userDetails.errors.user.2fa.disable.failed", MessageType.ERROR);

            return this.prepareModelForDetailsView(model, false, id, redirectAttributes);
        }

        this.addStatusMessage(redirectAttributes, "usersList.user.2fa.disabled", MessageType.SUCCESS);

        return redirectTarget;
    }



    @PostMapping("{id}/enable2fa")
    public final String enableTwoFactorAuthentication(@ModelAttribute("user") final UserModel userModel,
                                                      final ModelMap model, @PathVariable final int id,
                                                      final RedirectAttributes redirectAttributes,
                                                      final HttpServletRequest request) {

        this.logger.debug("Processing the data to enable two-factor authentication for a user.");

        if (!this.canEditUser(id)) {
            return UsersController.REDIRECT_TO_ACCESS_DENIED;
        }

        final String currentUser = this.getCurrentUserLogin();
        final String redirectTarget = (this.isCurrentUserAdmin()) ? UsersController.REDIRECT_TO_LIST
                : UsersController.REDIRECT_TO_HOME;

        if (id == this.usersRepository.getSystemUserId()) {
            this.logger.warn("The user {} tried to enable two-factor authentication for the system user.", currentUser);
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notEditable", MessageType.ERROR);

            return redirectTarget;
        }

        if (id != userModel.getId()) {
            this.logger.warn("The user {} tried to enable two-factor authentication for user id {}, but the data was set"
                             + " for user id {}. The data may have been tampered with, so the operation is denied.",
                             currentUser, id, userModel.getId());
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData", MessageType.ERROR);

            return redirectTarget;
        }

        this.logger.debug("Fetching the user.");
        User domainUser = this.usersRepository.findById(id).orElse(null);

        if (domainUser == null) {
            this.logger.error("No user found in database with id {}.", id);
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notFound", MessageType.ERROR);

            return redirectTarget;
        }

        if (domainUser.getTwoFactorStatus() != User.TwoFactorStatus.INACTIVE) {
            this.logger.warn("The user {} tried to enable two-factor authentication for user {} for which it was"
                             + " already enabled.",
                             currentUser, domainUser.getLogin());
            this.addStatusMessage(redirectAttributes, "usersList.errors.operation.illegal", MessageType.ERROR);

            return redirectTarget;
        }

        TwoFactorApplication twoFactorApplication = new TwoFactorApplication(domainUser, this.secrets,
                                                                             this.twoFactorService);
        twoFactorApplication.enable();

        domainUser = this.usersRepository.save(domainUser);

        if (domainUser == null) {
            this.addStatusMessage(model, "userDetails.errors.user.2fa.enable.failed", MessageType.ERROR);

            return this.prepareModelForDetailsView(model, false, id, redirectAttributes);
        }

        if (this.isEditingCurrentUser(userModel)) {
            return this.prepareFor2faWizard(request);
        }

        this.addStatusMessage(redirectAttributes, "usersList.user.2fa.enabled", MessageType.SUCCESS);

        return redirectTarget;
    }



    @PostMapping("{id}/reset2fa")
    public final String resetTwoFactorAuthentication(@ModelAttribute("user") final UserModel userModel,
                                                     final ModelMap model, @PathVariable final int id,
                                                     final RedirectAttributes redirectAttributes,
                                                     final HttpServletRequest request) {

        this.logger.debug("Processing the data to reset two-factor authentication for a user.");

        if (!this.canEditUser(id)) {
            return UsersController.REDIRECT_TO_ACCESS_DENIED;
        }

        final String currentUser = this.getCurrentUserLogin();
        final String redirectTarget = (this.isCurrentUserAdmin()) ? UsersController.REDIRECT_TO_LIST
                : UsersController.REDIRECT_TO_HOME;

        if (id == this.usersRepository.getSystemUserId()) {
            this.logger.warn("The user {} tried to reset two-factor authentication for the system user.", currentUser);
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notEditable", MessageType.ERROR);

            return redirectTarget;
        }

        if (id != userModel.getId()) {
            this.logger.warn("The user {} tried to reset two-factor authentication for user id {}, but the data was set"
                             + " for user id {}. The data may have been tampered with, so the operation is denied.",
                             currentUser, id, userModel.getId());
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.edit.invalidData", MessageType.ERROR);

            return redirectTarget;
        }

        this.logger.debug("Fetching the user.");
        User domainUser = this.usersRepository.findById(id).orElse(null);

        if (domainUser == null) {
            this.logger.error("No user found in database with id {}.", id);
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notFound", MessageType.ERROR);

            return redirectTarget;
        }

        if (domainUser.getTwoFactorStatus() != User.TwoFactorStatus.ACTIVE) {
            this.logger.warn("The user {} tried to reset two-factor authentication for user {} for which it was not already active.",
                             currentUser, domainUser.getLogin());
            this.addStatusMessage(redirectAttributes, "usersList.errors.operation.illegal", MessageType.ERROR);

            return redirectTarget;
        }

        TwoFactorApplication twoFactorApplication = new TwoFactorApplication(domainUser, this.secrets,
                                                                             this.twoFactorService);
        twoFactorApplication.enable();

        domainUser = this.usersRepository.save(domainUser);

        if (domainUser == null) {
            this.addStatusMessage(model, "userDetails.errors.user.2fa.reset.failed", MessageType.ERROR);

            return this.prepareModelForDetailsView(model, false, id, redirectAttributes);
        }


        if (this.isEditingCurrentUser(userModel)) {
            return this.prepareFor2faWizard(request);
        }

        this.addStatusMessage(redirectAttributes, "usersList.user.2fa.reset", MessageType.SUCCESS);

        return redirectTarget;
    }



    /**
     * Processes a request to show the view to create a new user.
     *
     * @param model the data to pass to the next view
     * @return the string that identifies the view to display next
     */
    @GetMapping("add")
    public final String viewAddForm(final ModelMap model) {

        if (!this.isCurrentUserAdmin()) {
            return UsersController.REDIRECT_TO_ACCESS_DENIED;
        }

        return this.prepareModelForDetailsView(model, true, null, null);
    }



    /**
     * Processes a request to show the details of a user.
     *
     * @param model              the data to display in the view
     * @param id                 the number that identifies the user to show
     * @param redirectAttributes the data to pass to the next page if a redirection is necessary
     * @return the string that identifies the view to display next
     */
    @GetMapping("{id}")
    public final String viewItem(final ModelMap model, @PathVariable final int id,
            final RedirectAttributes redirectAttributes) {

        if (!this.canEditUser(id)) {
            return UsersController.REDIRECT_TO_ACCESS_DENIED;
        }

        if (Objects.equals(id, this.usersRepository.getSystemUserId())) {
            this.logger.warn("The user {} tried to display the details of the system user.",
                    this.getCurrentUserLogin());
            this.addStatusMessage(redirectAttributes, "usersList.errors.user.notEditable", MessageType.ERROR);

            return UsersController.REDIRECT_TO_LIST;
        }

        return this.prepareModelForDetailsView(model, true, id, redirectAttributes);
    }



    /**
     * Processes a request to display all the application users.
     *
     * @param model the data to display in the next view
     * @return the string that identifies the next view to display
     */
    @GetMapping
    public final String viewList(final ModelMap model) {

        if (!this.isCurrentUserAdmin()) {
            return UsersController.REDIRECT_TO_ACCESS_DENIED;
        }

        return this.prepareModelForListView(model);
    }



    /**
     * Checks if the current user can edit the data of a given user.
     *
     * @param userId the number that uniquely identifies the user to edit
     * @return <code>true</code> if the user data can be edited
     */
    private boolean canEditUser(final int userId) {

        return (this.isCurrentUserAdmin() || userId == this.getCurrentUserId());

    }



    /**
     * Carries the actions to display a user in the details view.
     *
     * @param model              the data to display in the model view
     * @param createModel        <code>true</code> to if a new representation of the user must be created. If
     *                           <code>false</code> is passed, the <code>user</code> attribute of the model will be
     *                           left as is
     * @param id                 the number that identifies the user to display in the details view, or
     *                           <code>null</code> if the user
     *                           is a new one
     * @param redirectAttributes the data to pass to the next if a redirection is necessary. <code>null</code> can be
     *                           passed only if the user to display is a new one (because there won't be a redirection).
     * @return the string that identifies the next view
     */
    private String prepareModelForDetailsView(final ModelMap model, final boolean createModel, final Integer id,
            final RedirectAttributes redirectAttributes) {
        assert redirectAttributes != null || id == null :
                "The redirect attributes must be set if the user to display is not a new one.";

        String currentSection = UsersController.CURRENT_SECTION_IDENTIFIER;
        this.ldapSettings.refresh();
        this.addJavascriptMessagesAttribute(model);
        model.addAttribute("isLdapOn", this.ldapSettings.isEnabled());
        model.addAttribute("isAdmin", this.isCurrentUserAdmin());

        if (id == null) {

            if (createModel) {
                model.addAttribute("user", new UserModel());
            }

            model.addAttribute("isOwnAccount", false);
            model.addAttribute("isLocalUser", true);
            model.addAttribute("isAssociatedToProcesses", false);
            model.addAttribute("userGroups", "");

        } else {
            User domainUser = this.usersRepository.findById(id).orElse(null);

            if (domainUser == null) {
                this.addStatusMessage(redirectAttributes, "usersList.errors.user.notFound", MessageType.ERROR);

                return UsersController.REDIRECT_TO_LIST;
            }

            if (createModel) {
                model.addAttribute("user", new UserModel(domainUser));
            }

            final boolean isCurrentUser = (id == this.getCurrentUserId());
            model.addAttribute("isOwnAccount", isCurrentUser);
            model.addAttribute("isLocalUser", domainUser.getUserType() == UserType.LOCAL);
            model.addAttribute("isAssociatedToProcesses", domainUser.isAssociatedToProcesses());
            model.addAttribute("userGroups", domainUser.getUserGroupsCollection()
                                                        .stream()
                                                        .map(UserGroup::getName)
                                                        .sorted()
                                                        .collect(Collectors.joining(", ")));

            if (isCurrentUser) {
                currentSection = UsersController.CURRENT_USER_SECTION_IDENTIFIER;
            }
        }

        this.addCurrentSectionToModel(currentSection, model);

        return UsersController.DETAILS_VIEW;

    }



    /**
     * Defines the generic model attributes that ensure a proper display of the users list view.
     *
     * @param model the data to display in the view
     * @return the string that identifies the list view
     */
    private String prepareModelForListView(final ModelMap model) {
        model.addAttribute("users", this.usersRepository.findAllApplicationUsers());
        model.addAttribute("currentUserId", this.getCurrentUserId());
        this.addJavascriptMessagesAttribute(model);
        this.addCurrentSectionToModel(UsersController.CURRENT_SECTION_IDENTIFIER, model);

        return UsersController.LIST_VIEW;
    }

}
