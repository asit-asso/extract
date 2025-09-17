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
 * Custom locale resolver that determines the locale based on:
 * 1. User's stored preference (if logged in)
 * 2. Browser's Accept-Language header
 * 3. Default locale from configuration
 *
 * @author Claude Code
 */
public class UserLocaleResolver implements LocaleResolver {

    private static final String LOCALE_SESSION_ATTRIBUTE = "EXTRACT_LOCALE";

    @Autowired
    private UsersRepository usersRepository;

    private List<Locale> availableLocales;
    private Locale defaultLocale;

    /**
     * Resolves the locale for the current request.
     *
     * @param request the HTTP request
     * @return the resolved locale
     */
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // Try to get locale from session first
        HttpSession session = request.getSession(false);
        if (session != null) {
            Locale sessionLocale = (Locale) session.getAttribute(LOCALE_SESSION_ATTRIBUTE);
            if (sessionLocale != null) {
                return sessionLocale;
            }
        }

        // Try to get locale from authenticated user
        Locale userLocale = getUserLocale();
        if (userLocale != null) {
            // Store in session for performance
            if (session != null) {
                session.setAttribute(LOCALE_SESSION_ATTRIBUTE, userLocale);
            }
            return userLocale;
        }

        // Try to match browser locale with available locales
        if (availableLocales != null && !availableLocales.isEmpty()) {
            Locale browserLocale = request.getLocale();
            if (browserLocale != null) {
                // Exact match
                if (availableLocales.contains(browserLocale)) {
                    return browserLocale;
                }
                // Language match (e.g., "en-US" matches "en")
                String browserLang = browserLocale.getLanguage();
                for (Locale availableLocale : availableLocales) {
                    if (availableLocale.getLanguage().equals(browserLang)) {
                        return availableLocale;
                    }
                }
            }
        }

        // Fall back to default locale
        return defaultLocale != null ? defaultLocale : Locale.forLanguageTag("fr");
    }

    /**
     * Sets the locale for the current request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param locale the locale to set
     */
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        HttpSession session = request.getSession();
        session.setAttribute(LOCALE_SESSION_ATTRIBUTE, locale);
        
        // Update user's locale preference if logged in
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && usersRepository != null) {
            String username = auth.getName();
            if (username != null && !username.equals("anonymousUser")) {
                User user = usersRepository.findByLoginIgnoreCase(username);
                if (user != null) {
                    user.setLocale(locale.toLanguageTag());
                    usersRepository.save(user);
                }
            }
        }
    }

    /**
     * Gets the locale for the currently authenticated user.
     *
     * @return the user's locale or null if not authenticated
     */
    private Locale getUserLocale() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && usersRepository != null) {
            String username = auth.getName();
            if (username != null && !username.equals("anonymousUser")) {
                User user = usersRepository.findByLoginIgnoreCase(username);
                if (user != null && user.getLocale() != null) {
                    return Locale.forLanguageTag(user.getLocale());
                }
            }
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