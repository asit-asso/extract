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
package ch.asit_asso.extract.plugins.python;

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
 * NO CHANGES NEEDED IN THIS METHOD EXCEPT LOCALIZED_FILE_PATH_FORMAT
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
     * ADUST THIS PATH : REPLACE 'sample' BY THE PLUGIN NAME
     */
    private static final String LOCALIZED_FILE_PATH_FORMAT = "plugins/python/lang/%s/%s";

    /**
     * The name of the file that holds the localized application strings.
     */
    private static final String MESSAGES_FILE_NAME = "messages.properties";

    /**
     * The language to use for the messages to the user.
     */
    private final String language;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(LocalizedMessages.class);

    /**
     * The property file that contains the messages in the local language.
     */
    private Properties propertyFile;



    /**
     * Creates a new localized messages access instance using the default language.
     */
    public LocalizedMessages() {
        this.loadFile(LocalizedMessages.DEFAULT_LANGUAGE);
        this.language = LocalizedMessages.DEFAULT_LANGUAGE;
    }


    /**
     * Creates a new localized messages access instance.
     *
     * @param languageCode the string that identifies the language to use for the messages to the user
     */
    public LocalizedMessages(final String languageCode) {
        // Handle comma-separated language codes by taking the first one
        String primaryLanguage = languageCode;
        if (languageCode != null && languageCode.contains(",")) {
            primaryLanguage = languageCode.split(",")[0].trim();
            this.logger.debug("Multiple languages detected in config: {}. Using primary language: {}", 
                            languageCode, primaryLanguage);
        }
        this.logger.error("Localized message in : "+ primaryLanguage);
        this.loadFile(primaryLanguage);
        this.language = primaryLanguage;
    }


    /**
     * Obtains a localized string in the current language.
     *
     * @param key the string that identifies the localized string
     * @return the string localized in the current language
     */
    public final String getString(final String key) {

        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("The message key cannot be empty.");
        }

        return this.propertyFile.getProperty(key);
    }

    /**
     * Loads the localization file for the specified language code. This method validates the
     * provided language code, attempts to locate and load the appropriate localization file
     * based on a collection of fallback paths, and initializes the {@code propertyFile} field
     * with the loaded properties. If the localization file cannot be found or the language code
     * is invalid, an exception is thrown.
     *
     * @param languageCode the string representing the language code for which the localization
     *                     file should be loaded; must match the locale validation pattern
     *                     specified by {@code LocalizedMessages.LOCALE_VALIDATION_PATTERN}
     *                     and cannot be null
     * @throws IllegalArgumentException if the provided language code is invalid
     * @throws IllegalStateException    if no localization file can be found
     */
    private void loadFile(final String languageCode) {
        this.logger.debug("Loading the localization file for language {}.", languageCode);

        if (languageCode == null || !languageCode.matches(LocalizedMessages.LOCALE_VALIDATION_PATTERN)) {
            this.logger.error("The language string \"{}\" is not a valid locale.", languageCode);
            throw new IllegalArgumentException(String.format("The language code \"%s\" is invalid.", languageCode));
        }

        // Avoid stale state if loadFile is called multiple times
        this.propertyFile = null;

        for (String filePath : this.getFallbackPaths(languageCode, LocalizedMessages.MESSAGES_FILE_NAME)) {
            this.logger.debug("Trying localization file at {}", filePath);

            Optional<Properties> maybeProps = loadPropertiesFrom(filePath);
            if (maybeProps.isPresent()) {
                this.propertyFile = maybeProps.get();
                this.logger.info("Loaded localization from {}", filePath);
                this.logger.info("Localized messages loaded.");
                return; // Early return: do not override with later fallbacks
            }
        }

        this.logger.error("Could not find any localization file, not even the default.");
        throw new IllegalStateException("Could not find any localization file.");
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
                return IOUtils.toString(helpStream, "UTF-8");
            }
        } catch (IOException e) {
            logger.error("Could not read help file: " + filePath, e);
        }
        return "Help file not found: " + filePath;
    }

    /**
     * Builds a collection of possible paths a localized file to ensure that ne is found even if the
     * specific language is not available. As an example, if the language is <code>fr-CH</code>, then the paths
     * will be built for <code>fr-CH</code>, <code>fr</code> and the default language (say, <code>en</code>,
     * for instance).
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

        pathsList.add(String.format(LocalizedMessages.LOCALIZED_FILE_PATH_FORMAT, locale, filename));

        if (locale.length() > 2) {
            pathsList.add(String.format(LocalizedMessages.LOCALIZED_FILE_PATH_FORMAT, locale.substring(0, 2),
                    filename));
        }

        pathsList.add(String.format(LocalizedMessages.LOCALIZED_FILE_PATH_FORMAT, LocalizedMessages.DEFAULT_LANGUAGE,
                filename));

        return pathsList;
    }

}
