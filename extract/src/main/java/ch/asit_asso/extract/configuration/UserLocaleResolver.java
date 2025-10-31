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
import org.jetbrains.annotations.NotNull;
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
    public @NotNull Locale resolveLocale(@NotNull HttpServletRequest request) {
        Locale candidateLocale;
        HttpSession session = request.getSession(false);

        // Step 1: Check for explicitly selected locale in session (highest priority)
        candidateLocale = getExplicitlySelectedLocale(session);
        if (candidateLocale != null) {
            Locale validatedLocale = validateOrFallback(candidateLocale);
            // Update session with validated locale if needed
            if (!candidateLocale.equals(validatedLocale)) {
                session.setAttribute(LOCALE_SESSION_ATTRIBUTE, validatedLocale);
            }
            return validatedLocale;
        }

        // Step 2: For authenticated users, check database preference
        if (isUserAuthenticated()) {
            candidateLocale = getUserLocaleFromDatabase();
            if (candidateLocale != null) {
                // Note: getUserLocaleFromDatabase already validates and updates DB if needed
                // Store in session for performance
                if (session != null) {
                    session.setAttribute(LOCALE_SESSION_ATTRIBUTE, candidateLocale);
                }
                return candidateLocale;
            }
        }

        // Step 3: Check browser locale
        candidateLocale = getBrowserLocale(request);
        if (candidateLocale != null) {
            // getBrowserLocale already returns a validated locale
            return candidateLocale;
        }

        // Step 4: Return fallback locale
        return getFallbackLocale();
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
        // Validate the locale and fallback if necessary
        Locale validatedLocale = validateOrFallback(locale);

        // Update session
        HttpSession session = request.getSession();
        session.setAttribute(LOCALE_SESSION_ATTRIBUTE, validatedLocale);
        session.setAttribute(USER_SELECTED_LOCALE_ATTRIBUTE, true);

        // Update user's locale preference in database if authenticated
        if (isUserAuthenticated() && usersRepository != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = usersRepository.findByLoginIgnoreCase(username);
            if (user != null) {
                user.setLocale(validatedLocale.toLanguageTag());
                usersRepository.save(user);
            }
        }
    }

    /**
     * Gets the explicitly selected locale from the session.
     *
     * @param session the HTTP session (can be null)
     * @return the explicitly selected locale or null if not set
     */
    private Locale getExplicitlySelectedLocale(HttpSession session) {
        if (session != null) {
            Boolean userSelected = (Boolean) session.getAttribute(USER_SELECTED_LOCALE_ATTRIBUTE);
            if (userSelected != null && userSelected) {
                return (Locale) session.getAttribute(LOCALE_SESSION_ATTRIBUTE);
            }
        }
        return null;
    }

    /**
     * Validates a locale against available locales and returns it if valid,
     * or returns the fallback locale if invalid.
     *
     * @param locale the locale to validate
     * @return the locale if valid, or the fallback locale
     */
    private Locale validateOrFallback(Locale locale) {
        if (isLocaleAvailable(locale)) {
            return locale;
        }
        return getFallbackLocale();
    }

    /**
     * Checks if the current user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    private boolean isUserAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() &&
               !auth.getName().equals("anonymousUser");
    }

    /**
     * Gets the fallback locale (the first available locale).
     * If a default locale is set, validates it first.
     *
     * @return the fallback locale
     */
    private Locale getFallbackLocale() {
        // If default locale is set and available, use it
        if (this.defaultLocale != null && isLocaleAvailable(this.defaultLocale)) {
            return this.defaultLocale;
        }

        // Otherwise, use the first available locale
        if (availableLocales != null && !availableLocales.isEmpty()) {
            return availableLocales.get(0);
        }

        // Last resort: French
        return Locale.forLanguageTag("fr");
    }

    /**
     * Gets the locale for the currently authenticated user from the database.
     * Validates the stored locale against available locales and falls back to the first available locale if needed.
     *
     * @return the user's locale (validated) or null if not authenticated
     */
    private Locale getUserLocaleFromDatabase() {
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

                    // Fallback to the first available locale and update DB
                    Locale fallbackLocale = getFallbackLocale();
                    user.setLocale(fallbackLocale.toLanguageTag());
                    usersRepository.save(user);
                    return fallbackLocale;
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