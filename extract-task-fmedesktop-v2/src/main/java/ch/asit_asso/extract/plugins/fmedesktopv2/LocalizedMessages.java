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
package ch.asit_asso.extract.plugins.fmedesktopv2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * An access to the plugin strings localized in a given language.
 *
 * @author Yves Grasset
 */
public class LocalizedMessages {

    /**
     * The language code to use if none has been provided or if the one provided is not available.
     */
    private static final String DEFAULT_LANGUAGE = "fr";

    /**
     * The regular expression that checks if a language code is correctly formatted.
     */
    private static final String LOCALE_VALIDATION_PATTERN = "^[a-z]{2}(?:-[A-Z]{2})?$";

    /**
     * A string with placeholders to build the relative path to the files that holds the strings localized
     * in the defined language.
     */
    private static final String LOCALIZED_FILE_PATH_FORMAT = "plugins/fmedesktopv2/lang/%s/%s";

    /**
     * The name of the file that holds the localized application strings.
     */
    private static final String MESSAGES_FILE_NAME = "messages.properties";

    /**
     * The primary language to use for the messages to the user.
     */
    private final String language;

    /**
     * All configured languages for cascading fallback (e.g., ["de", "en", "fr"]).
     */
    private final List<String> allLanguages;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(LocalizedMessages.class);

    /**
     * All loaded property files in fallback order (primary language first, then fallbacks).
     * When looking up a key, we check each properties file in order.
     */
    private final List<Properties> propertyFiles = new ArrayList<>();



    /**
     * Creates a new localized messages access instance using the default language.
     */
    public LocalizedMessages() {
        this.allLanguages = new ArrayList<>();
        this.allLanguages.add(LocalizedMessages.DEFAULT_LANGUAGE);
        this.language = LocalizedMessages.DEFAULT_LANGUAGE;
        this.loadFile(this.language);
    }



    /**
     * Creates a new localized messages access instance with cascading language fallback.
     * If languageCode contains multiple languages (comma-separated), they will all be used for fallback.
     *
     * @param languageCode the string that identifies the language(s) to use for the messages to the user
     *                     (e.g., "de,en,fr" for German with English and French fallbacks)
     */
    public LocalizedMessages(final String languageCode) {
        // Parse all languages from comma-separated string
        this.allLanguages = new ArrayList<>();
        if (languageCode != null && languageCode.contains(",")) {
            String[] languages = languageCode.split(",");
            for (String lang : languages) {
                String trimmedLang = lang.trim();
                if (trimmedLang.matches(LocalizedMessages.LOCALE_VALIDATION_PATTERN)) {
                    this.allLanguages.add(trimmedLang);
                }
            }
            this.logger.debug("Multiple languages configured: {}. Using cascading fallback: {}",
                            languageCode, this.allLanguages);
        } else if (languageCode != null && languageCode.matches(LocalizedMessages.LOCALE_VALIDATION_PATTERN)) {
            this.allLanguages.add(languageCode.trim());
        }

        // If no valid languages found, use default
        if (this.allLanguages.isEmpty()) {
            this.allLanguages.add(LocalizedMessages.DEFAULT_LANGUAGE);
            this.logger.warn("No valid language found in '{}', using default: {}",
                           languageCode, LocalizedMessages.DEFAULT_LANGUAGE);
        }

        this.language = this.allLanguages.get(0);
        this.loadFile(this.language);
    }



    /**
     * Obtains a localized string with cascading fallback through all configured languages.
     * If the key is not found in the primary language, fallback languages are checked in order.
     * If the key is not found in any language, the key itself is returned.
     *
     * @param key the string that identifies the localized string
     * @return the string localized in the best available language, or the key itself if not found
     */
    public final String getString(final String key) {

        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("The message key cannot be empty.");
        }

        // Check each properties file in fallback order
        for (Properties props : this.propertyFiles) {
            String value = props.getProperty(key);
            if (value != null) {
                return value;
            }
        }

        // Key not found in any language, return the key itself
        this.logger.warn("Translation key '{}' not found in any language (checked: {})", key, this.allLanguages);
        return key;
    }

    /**
     * Gets the current locale.
     *
     * @return the locale
     */
    public java.util.Locale getLocale() {
        return new java.util.Locale(this.language);
    }

    /**
     * Gets the help content from the specified file path.
     *
     * @param filePath the path to the help file
     * @return the help content as a string
     */
    public String getHelp(String filePath) {
        try (InputStream helpStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (helpStream != null) {
                return IOUtils.toString(helpStream, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.error("Could not read help file: " + filePath, e);
        }
        return "Help file not found: " + filePath;
    }



    /**
     * Loads all available localization files for the configured languages in fallback order.
     * This enables cascading key fallback: if a key is missing in the primary language,
     * it will be looked up in fallback languages.
     *
     * @param languageCode the string representing the language code for which the localization
     *                     file should be loaded; must match the locale validation pattern
     *                     specified by {@code LocalizedMessages.LOCALE_VALIDATION_PATTERN}
     *                     and cannot be null
     * @throws IllegalArgumentException if the provided language code is invalid
     * @throws IllegalStateException    if no localization file can be found
     */
    private void loadFile(final String languageCode) {
        this.logger.debug("Loading localization files for language {} with fallbacks.", languageCode);

        if (languageCode == null || !languageCode.matches(LocalizedMessages.LOCALE_VALIDATION_PATTERN)) {
            this.logger.error("The language string \"{}\" is not a valid locale.", languageCode);
            throw new IllegalArgumentException(String.format("The language code \"%s\" is invalid.", languageCode));
        }

        // Load all available properties files in fallback order
        for (String filePath : this.getFallbackPaths(languageCode, LocalizedMessages.MESSAGES_FILE_NAME)) {
            this.logger.debug("Trying localization file at {}", filePath);

            Optional<Properties> maybeProps = loadPropertiesFrom(filePath);
            if (maybeProps.isPresent()) {
                this.propertyFiles.add(maybeProps.get());
                this.logger.info("Loaded localization from {} with {} keys.", filePath, maybeProps.get().size());
            }
        }

        if (this.propertyFiles.isEmpty()) {
            this.logger.error("Could not find any localization file, not even the default.");
            throw new IllegalStateException("Could not find any localization file.");
        }

        this.logger.info("Loaded {} localization file(s) for cascading fallback.", this.propertyFiles.size());
    }

    /**
     * Loads properties from a file located at the specified file path.
     * Attempts to read the file using UTF-8 encoding and load its contents into a Properties
     * object. If the file is not found or cannot be read, an empty Optional is returned.
     *
     * @param filePath the path to the file from which the properties should be loaded
     * @return an Optional containing the loaded Properties object if successful,
     *         or an empty Optional if the file cannot be found or read
     */
    private Optional<Properties> loadPropertiesFrom(final String filePath) {
        try (InputStream languageFileStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (languageFileStream == null) {
                this.logger.debug("Localization file not found at \"{}\".", filePath);
                return Optional.empty();
            }
            Properties props = new Properties();
            try (InputStreamReader reader = new InputStreamReader(languageFileStream, StandardCharsets.UTF_8)) {
                props.load(reader);
            }
            return Optional.of(props);
        } catch (IOException exception) {
            this.logger.warn("Could not load localization file at {}: {}", filePath, exception.getMessage());
            return Optional.empty();
        }
    }



    /**
     * Builds a collection of possible paths for a localized file with cascading fallback through all
     * configured languages. For example, if languages are ["de", "en", "fr"] and a regional variant like
     * "de-CH" is requested, paths will be built for: de-CH, de, en, fr.
     *
     * @param locale   the string that identifies the desired language
     * @param filename the name of the localized file
     * @return a collection of path strings to try successively to find the desired file
     */
    private Collection<String> getFallbackPaths(final String locale, final String filename) {
        assert locale != null && locale.matches(LocalizedMessages.LOCALE_VALIDATION_PATTERN) :
                "The language code is invalid.";
        assert StringUtils.isNotBlank(filename) && !filename.contains("../");

        Set<String> pathsList = new LinkedHashSet<>();

        // Add requested locale with regional variant if present
        pathsList.add(String.format(LocalizedMessages.LOCALIZED_FILE_PATH_FORMAT, locale, filename));

        if (locale.length() > 2) {
            pathsList.add(String.format(LocalizedMessages.LOCALIZED_FILE_PATH_FORMAT, locale.substring(0, 2),
                    filename));
        }

        // Add all configured languages for cascading fallback
        for (String lang : this.allLanguages) {
            pathsList.add(String.format(LocalizedMessages.LOCALIZED_FILE_PATH_FORMAT, lang, filename));
        }

        // Ensure default language is always included as final fallback
        pathsList.add(String.format(LocalizedMessages.LOCALIZED_FILE_PATH_FORMAT, LocalizedMessages.DEFAULT_LANGUAGE,
                filename));

        return pathsList;
    }

}
