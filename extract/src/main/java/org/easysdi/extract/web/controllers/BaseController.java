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
package org.easysdi.extract.web.controllers;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.easysdi.extract.authentication.ApplicationUser;
import org.easysdi.extract.domain.User.Profile;
import org.easysdi.extract.web.Message;
import org.easysdi.extract.web.Message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



/**
 * Foundation for the web controller of this application.
 *
 * @author Yves Grasset
 */
public abstract class BaseController {

    /**
     * The name of the authority role indicating that the current user has been granted administrative
     * privileges.
     */
    private static final String ADMIN_AUTHORITY = Profile.ADMIN.name();

    /**
     * The name of the model attribute to use to define the part of the website that is currently browsed.
     */
    private static final String CURRENT_SECTION_ATTRIBUTE = "currentSection";

    /**
     * The string to use to generate the relative path of the file that contains the localized JavaScript
     * application strings.
     */
    private static final String JAVASCRIPT_MESSAGES_PATH_FORMAT = "lang/%s/messages";

    /**
     * The string that tells this controller to redirect the user to the view indicating that the access
     * to a resource is not allowed.
     */
    protected static final String REDIRECT_TO_ACCESS_DENIED = "redirect:/forbidden";

    /**
     * The string that tells this controller to redirect the user to the home page.
     */
    protected static final String REDIRECT_TO_HOME = "redirect:/";

    /**
     * The string that tells this controller to redirect the user to the authentication form.
     */
    protected static final String REDIRECT_TO_LOGIN = "redirect:/login";

    /**
     * The name of the flash attribute to use to pass a status message to the view that is the target of
     * redirection.
     */
    private static final String STATUS_MESSAGE_ATTRIBUTE = "statusMessage";

    /**
     * The code of the language to use to localize the application strings.
     */
    @Value("${extract.i18n.language}")
    private String applicationLanguage;

    /**
     * The URL of the folder containing the localized messages to be used by the scripts on the page.
     */
    //@Value("${javascript.lang.basename}")
    private String javascriptMessagesPath = null;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(BaseController.class);



    /**
     * Defines an attribute in the model to tell the view which part of the website is currently browsed.
     *
     * @param currentSection the string that identifies the section that is currently active
     * @param model          the data to display in the view
     */
    protected final void addCurrentSectionToModel(final String currentSection, final ModelMap model) {

        if (currentSection == null) {
            throw new IllegalArgumentException("The current section identifier cannot be null.");
        }

        if (model == null) {
            throw new IllegalArgumentException("The model cannot be null.");
        }

        model.addAttribute(BaseController.CURRENT_SECTION_ATTRIBUTE, currentSection);
    }



    /**
     * Adds to the model the path of the file that contains the localized messages to be used by Javascript.
     *
     * @param model the data to be displayed by the next view
     */
    protected final void addJavascriptMessagesAttribute(final ModelMap model) {
        model.addAttribute("jsMessagesPath", this.getJavascriptMessagesPath());
    }



    /**
     * Passes a status message to the view.
     *
     * @param model       the data to be displayed by the view
     * @param messageKey  the string that identify the status message in the language file
     * @param messageType the type of message (success, error, warning,…)
     */
    protected final void addStatusMessage(final ModelMap model, final String messageKey,
            final MessageType messageType) {

        if (model == null) {
            throw new IllegalArgumentException("The model map cannot be null.");
        }

        if (StringUtils.isBlank(messageKey)) {
            throw new IllegalArgumentException("The message key cannot be empty.");
        }

        if (messageType == null) {
            throw new IllegalArgumentException("The message type cannot be null.");
        }

        Message statusMessage = new Message(messageKey, messageType);
        model.addAttribute(BaseController.STATUS_MESSAGE_ATTRIBUTE, statusMessage);
    }



    /**
     * Passes a message giving details about a view redirection.
     *
     * @param redirectAttributes the data to be passed to the next view
     * @param messageKey         the string that identify the status message in the language file
     * @param messageType        the type of message (success, error, warning,…)
     */
    protected final void addStatusMessage(final RedirectAttributes redirectAttributes, final String messageKey,
            final MessageType messageType) {

        if (redirectAttributes == null) {
            throw new IllegalArgumentException("The redirection attributes cannot be null.");
        }

        if (StringUtils.isBlank(messageKey)) {
            throw new IllegalArgumentException("The message key cannot be empty.");
        }

        if (messageType == null) {
            throw new IllegalArgumentException("The message type cannot be null.");
        }

        Message statusMessage = new Message(messageKey, messageType);
        redirectAttributes.addFlashAttribute(BaseController.STATUS_MESSAGE_ATTRIBUTE, statusMessage);
    }



    /**
     * Checks if the currently-logged user (if any) possesses administrative privileges.
     *
     * @return <code>true</code> if the user is an administrator
     */
    protected final boolean isCurrentUserAdmin() {
        return this.hasCurrentUserAuthority(BaseController.ADMIN_AUTHORITY);
    }



    /**
     * Obtains whether the currently-logged user (if any) has logged in with a username and password.
     *
     * @return <code>true</code> if the current user is a legit application user
     */
    protected final boolean isCurrentUserApplicationUser() {

        if (!this.isCurrentUserAuthenticated()) {
            return false;
        }

        return (this.getCurrentAuthentication().getPrincipal() instanceof ApplicationUser);

    }



    /**
     * Checks if an application user is currently identified with the current application session.
     *
     * @return <code>true</code> if the current user is authenticated
     */
    protected final boolean isCurrentUserAuthenticated() {
        final Authentication currentAuthentication = this.getCurrentAuthentication();

        return (currentAuthentication != null && currentAuthentication.isAuthenticated()
                && !(currentAuthentication instanceof AnonymousAuthenticationToken));
    }



    /**
     * Obtains the number that identifies the logged user in the application.
     *
     * @return the number that identifies the current user, or <code>-1</code> if no user is authenticated
     */
    protected final int getCurrentUserId() {

        if (!this.isCurrentUserApplicationUser()) {
            return -1;
        }

        return ((ApplicationUser) this.getCurrentAuthentication().getPrincipal()).getUserId();
    }



    /**
     * Obtains the string that identifies the logged user in the application.
     *
     * @return the current user's login, or <code>null</code> if no user is authenticated
     */
    protected final String getCurrentUserLogin() {

        if (!this.isCurrentUserAuthenticated()) {
            return "<Unauthenticated>";
        }

        if (!this.isCurrentUserApplicationUser()) {
            return this.getCurrentAuthentication().getPrincipal().toString();
        }

        return this.getCurrentAuthentication().getName();
    }



    /**
     * Gets the address of the client that submitted the given request.
     *
     * @param request the request submitted by the client
     * @return the client IP address
     */
    protected final String getRemoteIpAddress(final HttpServletRequest request) {
        String remoteAddress = "";

        if (request != null) {
            remoteAddress = request.getHeader("X-FORWARDED-FOR");

            if (remoteAddress == null || "".equals(remoteAddress)) {
                remoteAddress = request.getRemoteAddr();
            }
        }

        return remoteAddress;
    }



    /**
     * Checks if the currently-authenticated user (if any) has been granted a given access level.
     *
     * @param authority the string that identify the access level to test
     * @return <code>true</code> if the current user possesses the access level
     */
    protected final boolean hasCurrentUserAuthority(final String authority) {
        assert authority != null : "The authority string to test cannot be null.";

        this.logger.debug("Checking if the current user has been granted the {} authority.", authority);

        if (!this.isCurrentUserAuthenticated()) {
            this.logger.debug("No user is currently authenticated, so the authority is not available.");
            return false;
        }

        final Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

        for (GrantedAuthority grantedAuthority : currentAuthentication.getAuthorities()) {

            if (authority.equals(grantedAuthority.getAuthority())) {
                this.logger.debug("The {} authority is available for the current user {}.", authority,
                        currentAuthentication.getPrincipal());
                return true;
            }
        }

        this.logger.debug("The user {} does not possess the authority {}.", currentAuthentication.getPrincipal(),
                authority);
        return false;
    }



    /**
     * Obtains the code for the language used to localize the application strings.
     *
     * @return the language code
     */
    protected final String getApplicationLanguage() {
        return this.applicationLanguage;
    }



    /**
     * Obtains the URL of the file that contains the localized messages to be used by page scripts.
     *
     * @return the URL of the messages file
     */
    protected final String getJavascriptMessagesPath() {

        if (this.javascriptMessagesPath == null) {
            this.javascriptMessagesPath = String.format(BaseController.JAVASCRIPT_MESSAGES_PATH_FORMAT,
                    this.applicationLanguage);
        }

        return this.javascriptMessagesPath;
    }



    /**
     * Obtains the authentication information for the current user.
     *
     * @return the authentication object, or <code>null</code> if none is available
     */
    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
