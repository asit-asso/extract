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

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.email.PasswordResetEmail;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.utils.EmailUtils;
import ch.asit_asso.extract.utils.Secrets;
import ch.asit_asso.extract.web.Message.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



/**
 * A web controller that processes requests related to the reinitialization of a forgotten password.
 *
 * @author Yves Grasset
 */
@Controller
@RequestMapping("/passwordReset")
public class PasswordResetController extends BaseController {

    //TODO Centralize the password complexity requirements
    /**
     * The number of characters that a password must at least contains.
     */
    private static final int PASSWORD_MINIMUM_SIZE = 8;

    /**
     * The name of the role to be granted to be able to redefine a password with a token.
     */
    private static final String PASSWORD_RESET_AUTHORITY = "CAN_RESET_PASSWORD";

    /**
     * The string that tells this controller to redirect the user to the password reset form.
     */
    private static final String REDIRECT_TO_RESET_FORM = "redirect:/passwordReset/reset";

    /**
     * The string that identifies the view allowing the user to ask for a password reset token.
     */
    private static final String REQUEST_FORM_VIEW = "passwordReset/requestForm";

    /**
     * The string that identifies the view allowing the user to reset her password with a token.
     */
    private static final String RESET_FORM_VIEW = "passwordReset/resetForm";

    /**
     * The object that contains the configuration objects required to create and send e-mail messages.
     */
    private final EmailSettings emailSettings;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    /**
     * The Spring Security object that allows to hash passwords to store them in the data source.
     */
    private final Secrets secrets;

    /**
     * The Spring Data repository that links the user data objects with the data source.
     */
    private final UsersRepository usersRepository;



    public PasswordResetController(EmailSettings emailSettings, Secrets secrets,
                                   UsersRepository usersRepository) {
        this.emailSettings = emailSettings;
        this.secrets = secrets;
        this.usersRepository = usersRepository;
    }


    /**
     * Processes the data submitted to ask for a password reset token.
     *
     * @param email              the e-mail address of the user whose password is to be reset
     * @param model              the data to display in the next view
     * @param redirectAttributes the data to pass to a page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("request")
    @Transactional
    public String requestReset(@RequestParam final String email, final ModelMap model,
            final RedirectAttributes redirectAttributes) {
        this.logger.debug("A request to send the password reset e-mail has been received.");

        if (this.isCurrentUserApplicationUser()) {
            this.logger.warn("The user {} submitted data to receive a password reset token while authenticated.",
                    this.getCurrentUserLogin());

            return PasswordResetController.REDIRECT_TO_LOGIN;
        }

        final String errorMessage = this.defineUserToken(email);

        if (errorMessage != null) {
            return this.returnToRequestFormWithError(errorMessage, email, model);
        }

        return PasswordResetController.REDIRECT_TO_RESET_FORM;
    }



    /**
     * Processes the data submitted to reset a password.
     *
     * @param token                the string that allows a user to reset her password
     * @param password             the new password submitted
     * @param passwordConfirmation the password confirmation submitted
     * @param request              the HTTP request that submitted the data
     * @param model                the data to display in the next view
     * @param redirectAttributes   the data to pass to the page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @PostMapping("reset")
    public String resetPassword(@RequestParam final String token, @RequestParam final String password,
            @RequestParam final String passwordConfirmation, final HttpServletRequest request, final ModelMap model,
            final RedirectAttributes redirectAttributes) {

        this.logger.debug("Received a request to reset a password.");

        if (!this.hasCurrentUserAuthority(PasswordResetController.PASSWORD_RESET_AUTHORITY)) {
            this.logger.warn("The user {} with IP address {} sent password reset data without the required"
                    + " authority.", this.getCurrentUserLogin(), this.getRemoteIpAddress(request));
            return PasswordResetController.REDIRECT_TO_ACCESS_DENIED;
        }

        User tokenUser = this.usersRepository.findByPasswordResetTokenAndActiveTrue(token);
        String nextView = this.checkTokenUser(tokenUser, model, redirectAttributes, request);

        if (nextView != null) {
            return nextView;
        }

        final String passwordErrorMessage = this.checkPassword(password, passwordConfirmation);

        if (passwordErrorMessage != null) {
            return this.returnToResetFormWithError(passwordErrorMessage, model);
        }

        tokenUser = this.resetUserPassword(tokenUser, password);

        if (tokenUser == null) {
            return this.returnToResetFormWithError("passwordResetForm.errors.generic", model);
        }

        return this.terminateSession(redirectAttributes, "passwordResetForm.success", MessageType.SUCCESS,
                request.getSession());
    }



    /**
     * Process a request to display the form to ask for a password reset token.
     *
     * @return the string that identifies the view to display next
     */
    @GetMapping("request")
    public String showRequestForm() {
        this.logger.debug("Received a request to display the password reset request form.");

        if (this.isCurrentUserApplicationUser()) {
            this.logger.warn("The user {} requested the password reset request form while authenticated.",
                    this.getCurrentUserLogin());
            return PasswordResetController.REDIRECT_TO_HOME;
        }

        this.logger.debug("Displaying the password reset request form.");
        return PasswordResetController.REQUEST_FORM_VIEW;
    }



    /**
     * Processes a request to display the form to redefine a password with a token.
     *
     * @param request            the received HTTP request
     * @param model              the data to display in the next view
     * @param redirectAttributes the data to pass to the page that the user may be redirected to
     * @return the string that identifies the view to display next
     */
    @GetMapping("reset")
    public String showResetForm(final HttpServletRequest request, final ModelMap model,
            final RedirectAttributes redirectAttributes) {
        this.logger.debug("Received a request to display the password reset form.");

        if (!this.hasCurrentUserAuthority(PasswordResetController.PASSWORD_RESET_AUTHORITY)) {
            this.logger.warn("The user {} with IP address {} requested the password reset form without the required"
                    + " authority.", this.getCurrentUserLogin(), this.getRemoteIpAddress(request));
            return PasswordResetController.REDIRECT_TO_ACCESS_DENIED;
        }

        this.logger.debug("Displaying the password reset form.");
        return PasswordResetController.RESET_FORM_VIEW;
    }



    /**
     * Defines a token that allows a given user to reset his password in a defined timespan.
     *
     * @param user the user that requested to reset her password
     * @return the user with the token set, or <code>null</code> if the definition failed
     */
    private User addPasswordResetToken(final User user) {
        assert user != null : "The user cannot be null.";
        assert user.isActive() : "A password reset token cannot be added to an inactive user.";

        this.logger.debug("Setting the user properties to allow a password reset.");
        user.setPasswordResetInfo(this.getNewToken());
        this.logger.debug("The password will expire at {}.", user.getTokenExpiration().getTime());

        this.logger.debug("Saving the user.");
        return this.usersRepository.save(user);
    }



    /**
     * Validates a new password and its confirmation.
     *
     * @param password             the submitted new password
     * @param passwordConfirmation the submitted password confirmation
     * @return the validation error message key, or <code>null</code> if the password and he confirmation are OK
     */
    private String checkPassword(final String password, final String passwordConfirmation) {

        if (StringUtils.isEmpty(password)) {
            return "passwordResetForm.errors.password.required";
        }

        if (password.length() < PasswordResetController.PASSWORD_MINIMUM_SIZE) {
            return "passwordResetForm.errors.password.tooShort";
        }

        if (StringUtils.isEmpty(passwordConfirmation)) {
            return "passwordResetForm.errors.passwordConfirmation.required";
        }

        if (!password.equals(passwordConfirmation)) {
            return "passwordResetForm.errors.passwordConfirmation.mismatch";
        }

        return null;
    }



    /**
     * Validates that the password of a user fetched with a token can be reset.
     *
     * @param tokenUser          the user to check
     * @param model              the data to display in the next view
     * @param redirectAttributes the data to pass if a page redirection occurs
     * @param request            the HTTP request that submitted the password reset data
     * @return the name of the view to display next, or <code>null</code> if the password reset operation can
     *         carry on
     */
    private String checkTokenUser(final User tokenUser, final ModelMap model,
            final RedirectAttributes redirectAttributes, final HttpServletRequest request) {
        final String currentUserLogin = this.getCurrentUserLogin();

        if (tokenUser == null || !Objects.equals(tokenUser.getLogin(), currentUserLogin)) {
            this.logger.warn("The user {} with IP address {} submitted password reset data with an invalid token.",
                    currentUserLogin, this.getRemoteIpAddress(request));

            if (tokenUser != null) {
                this.logger.warn("The invalid token submitted by user {} belongs to user {}.", currentUserLogin,
                        tokenUser.getLogin());
            }

            return this.returnToResetFormWithError("passwordResetForm.errors.token.invalid", model);
        }

        if (!new GregorianCalendar().before(tokenUser.getTokenExpiration())) {
            this.logger.warn("The user {} with IP address {} submitted password reset data with an expired token.",
                    currentUserLogin, this.getRemoteIpAddress(request));
            tokenUser.cleanPasswordResetToken();
            this.usersRepository.save(tokenUser);

            return this.terminateSession(redirectAttributes, "passwordResetForm.errors.token.expired",
                    MessageType.ERROR, request.getSession());
        }

        return null;
    }



    /**
     * Sets an authentication principal that allows the given user to reset his password with a token.
     *
     * @param user the user
     */
    private void definePasswordResetAuthentication(final User user) {

        if (user == null || user.getPasswordResetToken() == null
                || !new GregorianCalendar().before(user.getTokenExpiration())) {
            throw new IllegalStateException("User does not qualify for a password reset authentication.");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getLogin(), null,
                List.of(new SimpleGrantedAuthority(PasswordResetController.PASSWORD_RESET_AUTHORITY)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }



    /**
     * Creates a password reset token for the active user that matches the given e-mail address, if any.
     *
     * @param email the user e-mail address
     * @return the error message produced, or <code>null</code> if the token must successfully set
     */
    private String defineUserToken(final String email) {

        if (!EmailUtils.isAddressValid(email)) {
            this.logger.debug("The submitted e-mail {} is invalid.", email);
            return "passwordResetDemand.errors.email.invalid";
        }

        User user = this.usersRepository.findByEmailIgnoreCaseAndActiveTrue(email);

        if (user == null) {
            this.logger.warn("A password reset request has been submitted for the e-mail address {}, but it does not"
                    + " match any active user.", email);
            return "passwordResetDemand.errors.user.notFound";
        }

        this.logger.debug("The e-mail {} submitted for a password reset request matches user {}.", email,
                user.getLogin());
        user = this.addPasswordResetToken(user);

        if (user == null) {
            this.logger.error("Could not save the user with the password reset data.");
            return "passwordResetDemand.errors.token.failed";
        }

        this.logger.info("A password reset token has been defined for user {}", user.getLogin());
        this.definePasswordResetAuthentication(user);
        this.sendPasswordResetEmail(user);

        return null;
    }



    /**
     * Creates a new, unique password reset token.
     *
     * @return the new token
     */
    private String getNewToken() {
        this.logger.debug("Creating a new user token.");
        String token;

        do {
            token = UUID.randomUUID().toString();
            this.logger.debug("Created token : {}", token);
        } while (this.usersRepository.findByPasswordResetTokenAndActiveTrue(token) != null);

        this.logger.debug("The token is unique.");
        return token;
    }



    /**
     * Revokes the authentication principal that allowed the current user to change her password with a
     * token.
     *
     * @param session the current HTTP session
     */
    private void invalidatePasswordResetAuthentication(final HttpSession session) {
        assert !this.isCurrentUserApplicationUser() : "The current user must not be an application user.";
        assert this.isCurrentUserAuthenticated() : "There should be a non-anonymous authentication.";

        if (this.isCurrentUserApplicationUser()) {
            this.logger.warn("Current authentication is not a password reset authentication. This method should not"
                    + " have been called.");
        }

        SecurityContextHolder.getContext().setAuthentication(null);
        session.invalidate();
    }



    /**
     * Replaces the password of a given user with a new one.
     *
     * @param user     the user whose password must be changed
     * @param password the raw new password
     * @return the user with the new password (hashed), or <code>null</code> if the password reset operation failed
     */
    private User resetUserPassword(final User user, final String password) {
        assert user != null : "The user must be set";
        assert user.getPasswordResetToken() != null && new GregorianCalendar().before(user.getTokenExpiration()) :
                "The user must have a non-expired token";
        assert password != null && password.length() >= PasswordResetController.PASSWORD_MINIMUM_SIZE :
                String.format("The password must be at least %d characters long.",
                        PasswordResetController.PASSWORD_MINIMUM_SIZE);

        user.setPassword(this.secrets.hash(password));
        user.cleanPasswordResetToken();

        return this.usersRepository.save(user);
    }



    /**
     * Carries the appropriate actions to send the user back to the password reset token request view
     * after an error has occured.
     *
     * @param errorMessageKey the string that identifies the message explaining the error that has occurred
     * @param enteredEmail    the e-mail address that was submitted
     * @param model           the data to display in the request form view
     * @return the string that identifies the request form view
     */
    private String returnToRequestFormWithError(final String errorMessageKey, final String enteredEmail,
            final ModelMap model) {

        model.addAttribute("errorMessage", errorMessageKey);
        model.addAttribute("enteredEmail", enteredEmail);

        return PasswordResetController.REQUEST_FORM_VIEW;
    }



    /**
     * Carries the appropriate actions to send the user back to the password reset view after an error has
     * occurred.
     *
     * @param errorMessageKey the string that identifies the message explaining the error that has occurred
     * @param model           the data to display in the password reset form view
     * @return the string that identifies the reset form view
     */
    private String returnToResetFormWithError(final String errorMessageKey, final ModelMap model) {
        model.addAttribute("errorMessage", errorMessageKey);

        return PasswordResetController.RESET_FORM_VIEW;
    }



    /**
     * Sends an electronic message with a token to a user that asked to reset her password.
     *
     * @param user the user that asked for a password reset token
     */
    private void sendPasswordResetEmail(final User user) {
        assert user != null : "The user cannot be null.";
        assert user.isActive() : "Inactive users are not eligible for password reset.";
        assert user.getPasswordResetToken() != null && new GregorianCalendar().before(user.getTokenExpiration()) :
                "The user must have a token set and an expiration date in the future";

        this.logger.debug("Preparing the password reset e-mail.");
        PasswordResetEmail message = new PasswordResetEmail(this.emailSettings);

        if (!message.initialize(user.getPasswordResetToken(), user.getEmail())) {
            this.logger.warn("The password reset e-mail could not be created due to an internal error.");
            return;
        }

        if (!message.send()) {
            this.logger.warn("The password reset e-mail was not sent.");
            return;
        }

        this.logger.debug("The password reset e-mail was successfully sent.");
    }



    /**
     * Closes the current password reset session a redirects the user to the login form.
     *
     * @param redirectAttributes the data to pass to the login form
     * @param messageKey         the string that identifies the message to pass to the login form
     * @param messageType        the type of message to pass to the login form
     * @param session            the current user session
     * @return the string that tells this controller to go to the login form
     */
    private String terminateSession(final RedirectAttributes redirectAttributes, final String messageKey,
            final MessageType messageType, final HttpSession session) {

        this.addStatusMessage(redirectAttributes, messageKey, messageType);
        this.invalidatePasswordResetAuthentication(session);

        return PasswordResetController.REDIRECT_TO_LOGIN;
    }

}
