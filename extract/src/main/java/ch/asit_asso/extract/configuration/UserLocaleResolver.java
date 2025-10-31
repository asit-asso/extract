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
package ch.asit_asso.extract.configuration;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;

/**
 * Custom locale resolver that ensures only configured locales are used throughout the application.
 * All locale sources are validated against the available locales configured in extract.i18n.language.
 *
 * For unauthenticated users (login pages, etc.):
 * 1. User's explicitly selected locale (validated against available locales)
 * 2. Browser's Accept-Language header (validated and matched to available locales)
 * 3. Default locale (first available locale from configuration)
 *
 * For authenticated users:
 * 1. User's explicitly selected locale (validated against available locales)
 * 2. User's stored preference from database (validated against available locales)
 *    - If stored locale is not available, fallback to first available locale
 *    - Database is updated with the fallback locale for consistency
 * 3. Browser's Accept-Language header (validated and matched to available locales)
 * 4. Default locale (first available locale from configuration)
 *
 * IMPORTANT: If any locale source is not in the available locales list, the system
 * automatically falls back to the first available locale to ensure consistency.
 *
 * @author arx iT
 */
public class UserLocaleResolver implements LocaleResolver {

    private static final String LOCALE_SESSION_ATTRIBUTE = "EXTRACT_LOCALE";
    private static final String USER_SELECTED_LOCALE_ATTRIBUTE = "EXTRACT_USER_SELECTED_LOCALE";

    @Autowired
    private UsersRepository usersRepository;

    private List<Locale> availableLocales;
    private Locale defaultLocale;

    /**
     * Resolves the locale for the current request.
     * All locales are validated against available locales before being returned.
     * If a locale is not available, falls back to the first available locale.
     *
     * @param request the HTTP request
     * @return the resolved locale (always a valid, available locale)
     */
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // Check if user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                                 !auth.getName().equals("anonymousUser");

        // For authenticated users: check explicit selection first, then user preference
        if (isAuthenticated) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Boolean userSelected = (Boolean) session.getAttribute(USER_SELECTED_LOCALE_ATTRIBUTE);
                if (userSelected != null && userSelected) {
                    Locale sessionLocale = (Locale) session.getAttribute(LOCALE_SESSION_ATTRIBUTE);
                    if (sessionLocale != null) {
                        // Validate session locale against available locales
                        if (isLocaleAvailable(sessionLocale)) {
                            return sessionLocale;
                        }
                        // Session locale not available, fallback to first available
                        if (availableLocales != null && !availableLocales.isEmpty()) {
                            Locale fallbackLocale = availableLocales.get(0);
                            // Update session with valid locale
                            session.setAttribute(LOCALE_SESSION_ATTRIBUTE, fallbackLocale);
                            return fallbackLocale;
                        }
                    }
                }
            }

            // For authenticated users: prioritize user preference from database
            Locale userLocale = getUserLocale();
            if (userLocale != null) {
                // Store in session for performance
                if (session != null) {
                    session.setAttribute(LOCALE_SESSION_ATTRIBUTE, userLocale);
                }
                return userLocale;
            }
        }

        // For unauthenticated users: check session first (for explicitly selected locale)
        HttpSession session = request.getSession(false);
        if (session != null) {
            Boolean userSelected = (Boolean) session.getAttribute(USER_SELECTED_LOCALE_ATTRIBUTE);
            if (userSelected != null && userSelected) {
                Locale sessionLocale = (Locale) session.getAttribute(LOCALE_SESSION_ATTRIBUTE);
                if (sessionLocale != null && isLocaleAvailable(sessionLocale)) {
                    return sessionLocale;
                }
            }
        }

        // Check browser locale
        Locale browserLocale = getBrowserLocale(request);
        if (browserLocale != null) {
            return browserLocale;
        }

        // Fall back to default locale (which is the first available locale)
        return this.defaultLocale != null ? this.defaultLocale :
               (availableLocales != null && !availableLocales.isEmpty() ? availableLocales.get(0) : Locale.forLanguageTag("fr"));
    }

    /**
     * Sets the locale for the current request.
     * Validates the locale against available locales before saving.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param locale the locale to set
     */
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        // Validate and potentially fallback to first available locale
        Locale validatedLocale = locale;
        if (!isLocaleAvailable(locale)) {
            if (availableLocales != null && !availableLocales.isEmpty()) {
                validatedLocale = availableLocales.get(0);
            }
        }

        HttpSession session = request.getSession();
        session.setAttribute(LOCALE_SESSION_ATTRIBUTE, validatedLocale);
        // Mark this as explicitly set by the user
        session.setAttribute(USER_SELECTED_LOCALE_ATTRIBUTE, true);

        // Update user's locale preference if logged in
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && usersRepository != null) {
            String username = auth.getName();
            if (username != null && !username.equals("anonymousUser")) {
                User user = usersRepository.findByLoginIgnoreCase(username);
                if (user != null) {
                    user.setLocale(validatedLocale.toLanguageTag());
                    usersRepository.save(user);
                }
            }
        }
    }

    /**
     * Gets the locale for the currently authenticated user.
     * Validates the stored locale against available locales and falls back to the first available locale if needed.
     *
     * @return the user's locale (validated) or null if not authenticated
     */
    private Locale getUserLocale() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && usersRepository != null) {
            String username = auth.getName();
            if (username != null && !username.equals("anonymousUser")) {
                User user = usersRepository.findByLoginIgnoreCase(username);
                if (user != null && user.getLocale() != null && !user.getLocale().trim().isEmpty()) {
                    Locale userLocale = Locale.forLanguageTag(user.getLocale());

                    // Validate that the user's locale is available
                    if (isLocaleAvailable(userLocale)) {
                        return userLocale;
                    }

                    // Fallback to the first available locale if user's locale is not available
                    if (availableLocales != null && !availableLocales.isEmpty()) {
                        Locale fallbackLocale = availableLocales.get(0);

                        // Update the database with the fallback locale for consistency
                        user.setLocale(fallbackLocale.toLanguageTag());
                        usersRepository.save(user);

                        return fallbackLocale;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if a locale is available in the configured locales.
     * Matches both exact locale and language-only match.
     *
     * @param locale the locale to check
     * @return true if the locale is available, false otherwise
     */
    private boolean isLocaleAvailable(Locale locale) {
        if (availableLocales == null || availableLocales.isEmpty() || locale == null) {
            return false;
        }

        // Check for exact match
        if (availableLocales.contains(locale)) {
            return true;
        }

        // Check for language match (e.g., "en-US" matches "en")
        String localeLanguage = locale.getLanguage();
        return availableLocales.stream()
                .anyMatch(availableLocale -> availableLocale.getLanguage().equals(localeLanguage));
    }

    /**
     * Gets the browser locale that matches available locales.
     *
     * @param request the HTTP request
     * @return the matched browser locale or null if no match found
     */
    private Locale getBrowserLocale(HttpServletRequest request) {
        Locale browserLocale = request.getLocale();
        if (browserLocale != null && isLocaleAvailable(browserLocale)) {
            // If browser locale is available, use it directly
            if (availableLocales.contains(browserLocale)) {
                return browserLocale;
            }
            // Otherwise, find the first available locale with matching language
            String browserLang = browserLocale.getLanguage();
            return availableLocales.stream()
                    .filter(locale -> locale.getLanguage().equals(browserLang))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Sets the available locales.
     *
     * @param availableLocales the list of available locales
     */
    public void setAvailableLocales(List<Locale> availableLocales) {
        this.availableLocales = availableLocales;
    }

    /**
     * Sets the default locale.
     *
     * @param defaultLocale the default locale
     */
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
}