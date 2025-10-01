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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import ch.asit_asso.extract.authentication.ApplicationUser;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.web.Message;
import ch.asit_asso.extract.web.Message.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;
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
     * The locale resolver to determine the current user's locale.
     */
    @Autowired(required = false)
    private LocaleResolver localeResolver;

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
     * Uses the current request context to determine the user's locale.
     *
     * @param model the data to be displayed by the next view
     */
    protected final void addJavascriptMessagesAttribute(final ModelMap model) {
        HttpServletRequest request = this.getCurrentRequest();
        model.addAttribute("jsMessagesPath", this.getJavascriptMessagesPath(request));
    }

    /**
     * Adds to the model the path of the file that contains the localized messages to be used by Javascript.
     *
     * @param model the data to be displayed by the next view
     * @param request the HTTP request to determine the current locale
     */
    protected final void addJavascriptMessagesAttribute(final ModelMap model, final HttpServletRequest request) {
        model.addAttribute("jsMessagesPath", this.getJavascriptMessagesPath(request));
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

        if (!(this.getCurrentAuthentication().getPrincipal() instanceof ApplicationUser)) {
            return false;
        }

        return this.isApplicationRoleInAuthorities();
    }



    private boolean isApplicationRoleInAuthorities() {
        final List<String> acceptedAuthoritiesNames = Arrays.stream(Profile.values())
                                                      .map(Profile::name)
                                                      .toList();

        List<String> grantedAuthoritiesNames = this.getCurrentAuthentication().getAuthorities().stream()
                                                   .map(GrantedAuthority::getAuthority).toList();

        this.logger.debug("Looking for one of [{}] among granted authorities ([{}])",
                          String.join(", ", acceptedAuthoritiesNames),
                          String.join(", ", grantedAuthoritiesNames));

        return grantedAuthoritiesNames.stream().anyMatch(acceptedAuthoritiesNames::contains);
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

            if (remoteAddress == null || remoteAddress.isEmpty()) {
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
                        this.getCurrentUserLogin());
                return true;
            }
        }

        this.logger.debug("The user {} does not possess the authority {}.", this.getCurrentUserLogin(),
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
     * @param request the HTTP request to determine the current locale
     * @return the URL of the messages file
     */
    protected final String getJavascriptMessagesPath(final HttpServletRequest request) {
        String currentLanguage = this.getCurrentLanguage(request);
        return String.format(BaseController.JAVASCRIPT_MESSAGES_PATH_FORMAT, currentLanguage);
    }

    /**
     * Gets the current language code for the user.
     * Attempts to use LocaleResolver first, with fallback to default configuration.
     *
     * @param request the HTTP request to determine the current locale
     * @return the language code (never null)
     */
    private String getCurrentLanguage(final HttpServletRequest request) {
        // Try to get language from LocaleResolver if available
        if (this.localeResolver != null && request != null) {
            try {
                Locale currentLocale = this.localeResolver.resolveLocale(request);
                if (currentLocale != null) {
                    String lang = currentLocale.getLanguage();
                    if (lang != null && !lang.isEmpty()) {
                        // Validate that the language is supported
                        if (this.isLanguageSupported(lang)) {
                            return lang;
                        }
                        this.logger.debug("Language {} not supported, using default", lang);
                    }
                }
            } catch (Exception e) {
                this.logger.warn("Error resolving locale, using default: {}", e.getMessage());
            }
        }
        
        // Fallback to first language from configuration if available
        return this.getDefaultLanguage();
    }

    /**
     * Checks if a language is supported by the application.
     *
     * @param language the language code to check
     * @return true if the language is supported
     */
    private boolean isLanguageSupported(final String language) {
        if (this.applicationLanguage == null || language == null) {
            return false;
        }
        
        String[] supportedLanguages = this.applicationLanguage.split(",");
        for (String supported : supportedLanguages) {
            if (supported.trim().equalsIgnoreCase(language)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the default language from configuration.
     *
     * @return the default language code
     */
    private String getDefaultLanguage() {
        if (this.applicationLanguage != null && this.applicationLanguage.contains(",")) {
            return this.applicationLanguage.split(",")[0].trim();
        }
        return this.applicationLanguage != null ? this.applicationLanguage : "fr";
    }

    /**
     * Gets the current language code for the logged-in user.
     * Public method for controllers that need to pass the language to the model.
     *
     * @return the language code
     */
    protected final String getCurrentUserLanguage() {
        HttpServletRequest request = this.getCurrentRequest();
        if (request != null) {
            return this.getCurrentLanguage(request);
        }
        return this.getDefaultLanguage();
    }



    /**
     * Obtains the authentication information for the current user.
     *
     * @return the authentication object, or <code>null</code> if none is available
     */
    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Gets the current HTTP request from the request context.
     *
     * @return the current HTTP request, or null if not available
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            this.logger.debug("Could not get current request: {}", e.getMessage());
            return null;
        }
    }

}
