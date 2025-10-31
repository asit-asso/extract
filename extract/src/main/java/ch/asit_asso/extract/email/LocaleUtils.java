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
package ch.asit_asso.extract.email;

import ch.asit_asso.extract.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Utility class for locale validation and resolution for email sending.
 * Ensures that only configured locales are used when sending emails.
 *
 * @author Extract Team
 */
public class LocaleUtils {

    /**
     * The writer to the application logs.
     */
    private static final Logger logger = LoggerFactory.getLogger(LocaleUtils.class);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private LocaleUtils() {
        // Utility class
    }

    /**
     * Gets a validated locale for a user based on available locales.
     * If the user's stored locale is not available, returns the first available locale.
     *
     * @param user the user whose locale to validate
     * @param availableLocalesConfig the comma-separated string of available locales from configuration
     * @return the validated locale for the user, never null
     */
    public static Locale getValidatedUserLocale(User user, String availableLocalesConfig) {
        List<Locale> availableLocales = parseAvailableLocales(availableLocalesConfig);
        return getValidatedUserLocale(user, availableLocales);
    }

    /**
     * Gets a validated locale for a user based on available locales.
     * If the user's stored locale is not available, returns the first available locale.
     *
     * @param user the user whose locale to validate
     * @param availableLocales the list of available locales
     * @return the validated locale for the user, never null
     */
    public static Locale getValidatedUserLocale(User user, List<Locale> availableLocales) {
        // Check if user has a locale set
        if (user != null && user.getLocale() != null && !user.getLocale().trim().isEmpty()) {
            Locale userLocale = Locale.forLanguageTag(user.getLocale());

            // Validate the user's locale against available locales
            if (isLocaleAvailable(userLocale, availableLocales)) {
                logger.debug("Using user {} locale: {}", user.getLogin(), userLocale.toLanguageTag());
                return userLocale;
            }

            logger.debug("User {} locale {} is not available, using fallback",
                        user.getLogin(), userLocale.toLanguageTag());
        }

        // Return the first available locale as fallback
        Locale fallback = getDefaultLocale(availableLocales);
        if (user != null) {
            logger.debug("Using fallback locale {} for user {}", fallback.toLanguageTag(), user.getLogin());
        }
        return fallback;
    }

    /**
     * Parses the available locales from a configuration string.
     *
     * @param availableLocalesConfig comma-separated string of locale codes
     * @return list of available locales
     */
    public static List<Locale> parseAvailableLocales(String availableLocalesConfig) {
        if (availableLocalesConfig == null || availableLocalesConfig.trim().isEmpty()) {
            logger.warn("No available locales configured, using French as default");
            return Arrays.asList(Locale.forLanguageTag("fr"));
        }

        return Arrays.stream(availableLocalesConfig.split(","))
                .map(String::trim)
                .filter(lang -> !lang.isEmpty())
                .map(Locale::forLanguageTag)
                .collect(Collectors.toList());
    }

    /**
     * Gets the default locale (first available locale).
     *
     * @param availableLocales the list of available locales
     * @return the default locale, never null
     */
    public static Locale getDefaultLocale(List<Locale> availableLocales) {
        if (availableLocales != null && !availableLocales.isEmpty()) {
            return availableLocales.get(0);
        }
        // Last resort fallback
        return Locale.forLanguageTag("fr");
    }

    /**
     * Checks if a locale is available in the list of configured locales.
     * Matches both exact locale and language-only match.
     *
     * @param locale the locale to check
     * @param availableLocales the list of available locales
     * @return true if the locale is available, false otherwise
     */
    private static boolean isLocaleAvailable(Locale locale, List<Locale> availableLocales) {
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
}