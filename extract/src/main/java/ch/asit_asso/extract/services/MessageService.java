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
package ch.asit_asso.extract.services;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.LocaleResolver;
import javax.servlet.http.HttpServletRequest;

/**
 * Service for obtaining localized messages from the application's message sources.
 * This service provides a centralized way to access localized strings throughout the application.
 *
 * @author Bruno Alves
 */
@Service
public class MessageService {

    /**
     * The object that gives access to the localized strings.
     */
    private final MessageSource messageSource;

    /**
     * The locale resolver for getting the current locale.
     */
    @Autowired(required = false)
    private LocaleResolver localeResolver;

    /**
     * The current HTTP request.
     */
    @Autowired(required = false)
    private HttpServletRequest request;

    /**
     * Creates a new instance of the message service.
     *
     * @param messageSource the Spring MessageSource for accessing localized strings
     */
    public MessageService(final MessageSource messageSource) {
        if (messageSource == null) {
            throw new IllegalArgumentException("The message source cannot be null.");
        }
        
        this.messageSource = messageSource;
    }

    /**
     * Obtains the application string that matches the given key using the default locale.
     *
     * @param messageKey the key that identifies the desired message
     * @return the localized message
     * @throws IllegalArgumentException if the message key is null or blank
     */
    public String getMessage(final String messageKey) {
        return this.getMessage(messageKey, null);
    }

    /**
     * Obtains the application string that matches the given key with arguments using the default locale.
     *
     * @param messageKey the key that identifies the desired message
     * @param arguments  an array of objects that will replace the placeholders in the message string,
     *                   or null if no substitution is needed
     * @return the localized message with placeholders replaced by the arguments
     * @throws IllegalArgumentException if the message key is null or blank
     */
    public String getMessage(final String messageKey, final Object[] arguments) {
        return this.getMessage(messageKey, arguments, getCurrentLocale());
    }

    /**
     * Obtains the application string that matches the given key with arguments for the specified locale.
     *
     * @param messageKey the key that identifies the desired message
     * @param arguments  an array of objects that will replace the placeholders in the message string,
     *                   or null if no substitution is needed
     * @param locale     the locale for which the message should be retrieved
     * @return the localized message with placeholders replaced by the arguments
     * @throws IllegalArgumentException if the message key is null or blank
     */
    public String getMessage(final String messageKey, final Object[] arguments, final Locale locale) {
        if (StringUtils.isBlank(messageKey)) {
            throw new IllegalArgumentException("The message key cannot be null or blank.");
        }

        final Locale targetLocale = (locale != null) ? locale : getCurrentLocale();
        return this.messageSource.getMessage(messageKey, arguments, targetLocale);
    }

    /**
     * Gets the current locale based on the user's preference or system default.
     *
     * @return the current locale
     */
    private Locale getCurrentLocale() {
        // Try to get locale from LocaleResolver
        if (localeResolver != null && request != null) {
            try {
                return localeResolver.resolveLocale(request);
            } catch (Exception e) {
                // Fall through to other methods
            }
        }
        
        // Try to get locale from Spring's LocaleContextHolder
        try {
            Locale contextLocale = LocaleContextHolder.getLocale();
            if (contextLocale != null) {
                return contextLocale;
            }
        } catch (Exception e) {
            // Fall through to default
        }
        
        // Fall back to system default
        return Locale.getDefault();
    }
}