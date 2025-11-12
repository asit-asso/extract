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
package ch.asit_asso.extract.plugins.fmeserverv2;

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
 * An access to the plugin strings localized in a given language with enhanced error handling.
 *
 * @author Extract Team
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
    private static final String LOCALIZED_FILE_PATH_FORMAT = "plugins/fmeserverv2/lang/%s/%s";

    /**
     * The name of the file that holds the localized application strings.
     */
    private static final String MESSAGES_FILE_NAME = "messages.properties";

    /**
     * The primary language to use for the messages to the user.
     */
    private String language;

    /**
     * All configured languages for cascading fallback (e.g., ["de", "en", "fr"]).
     */
    private final List<String> allLanguages;

    /**
     * The writer to the application logs.
     */
    private static final Logger logger = LoggerFactory.getLogger(LocalizedMessages.class);

    /**
     * The property file that contains the messages in the local language.
     */
    private Properties propertyFile;

    /**
     * Flag indicating if messages were successfully loaded.
     */
    private boolean isLoaded = false;

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
            logger.debug("Multiple languages configured: {}. Using cascading fallback: {}",
                            languageCode, this.allLanguages);
        } else if (languageCode != null && languageCode.matches(LocalizedMessages.LOCALE_VALIDATION_PATTERN)) {
            this.allLanguages.add(languageCode.trim());
        }

        // If no valid languages found, use default
        if (this.allLanguages.isEmpty()) {
            this.allLanguages.add(LocalizedMessages.DEFAULT_LANGUAGE);
            logger.warn("No valid language found in '{}', using default: {}",
                           languageCode, LocalizedMessages.DEFAULT_LANGUAGE);
        }

        this.language = this.allLanguages.get(0);
        this.loadFile(this.language);
    }

    /**
     * Reads the content of a file in the current language with enhanced error handling.
     *
     * @param filename the name of the file to read
     * @return the content of the file, or an error message if the file could not be read
     */
    public final String getFileContent(final String filename) {
        if (StringUtils.isBlank(filename)) {
            logger.error("Filename is blank");
            return getString("plugin.errors.internal.file.blank");
        }

        // Security: Path traversal prevention
        if (filename.contains("../") || filename.contains("..\\")) {
            logger.error("Filename contains path traversal attempt: {}", filename);
            return getString("plugin.errors.internal.file.invalid");
        }

        for (String filePath : this.getFallbackPaths(this.language, filename)) {
            try (InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
                if (fileStream == null) {
                    logger.debug("File not found at path: {}", filePath);
                    continue;
                }

                String content = IOUtils.toString(fileStream, StandardCharsets.UTF_8);
                logger.debug("Successfully loaded file: {}", filePath);
                return content;

            } catch (IOException exception) {
                logger.error("IO error reading file at path: {}", filePath, exception);
            } catch (Exception exception) {
                logger.error("Unexpected error reading file at path: {}", filePath, exception);
            }
        }

        logger.warn("Could not load file '{}' in any language", filename);
        return getString("plugin.errors.internal.file.notfound");
    }

    /**
     * Obtains a localized string in the current language with fallback support.
     *
     * @param key the string that identifies the localized string
     * @return the string localized in the current language, or the key itself if not found
     */
    public final String getString(final String key) {
        if (StringUtils.isBlank(key)) {
            logger.error("Message key is blank");
            return "[EMPTY_KEY]";
        }

        if (this.propertyFile == null) {
            logger.error("Property file is null when getting key: {}", key);
            return String.format("[%s]", key);
        }

        String value = this.propertyFile.getProperty(key);

        if (value == null) {
            logger.warn("Message key not found: {}", key);
            return String.format("[%s]", key);
        }

        return value;
    }

    /**
     * Formats a localized string with parameters.
     *
     * @param key the string that identifies the localized string
     * @param args the arguments to format into the string
     * @return the formatted localized string
     */
    public final String getString(final String key, final Object... args) {
        String template = getString(key);

        if (template.startsWith("[") && template.endsWith("]")) {
            // Key was not found, return a more informative message
            return template + " " + Arrays.toString(args);
        }

        try {
            return String.format(template, args);
        } catch (IllegalFormatException e) {
            logger.error("Failed to format message for key '{}' with args: {}", key, Arrays.toString(args), e);
            return template;
        }
    }

    /**
     * Checks if a message key exists.
     *
     * @param key the message key to check
     * @return true if the key exists, false otherwise
     */
    public final boolean hasKey(final String key) {
        return this.propertyFile != null &&
               !StringUtils.isBlank(key) &&
               this.propertyFile.containsKey(key);
    }

    /**
     * Obtains the language used for the localized messages.
     *
     * @return the language code
     */
    public final String getLanguage() {
        return this.language;
    }

    /**
     * Checks if messages were successfully loaded.
     *
     * @return true if messages are loaded, false otherwise
     */
    public final boolean isLoaded() {
        return this.isLoaded && this.propertyFile != null && !this.propertyFile.isEmpty();
    }

    /**
     * Reads the file that holds the application strings in a given language with comprehensive error handling.
     *
     * @param guiLanguage the string that identifies the language to use for the messages to the user
     */
    private void loadFile(final String guiLanguage) {
        logger.debug("Loading localization file for language: {}", guiLanguage);

        if (guiLanguage == null || !guiLanguage.matches(LocalizedMessages.LOCALE_VALIDATION_PATTERN)) {
            logger.error("Invalid language string: '{}'", guiLanguage);
            throw new IllegalArgumentException(String.format("Invalid language code: '%s'", guiLanguage));
        }

        boolean fileLoaded = false;
        Exception lastException = null;

        for (String filePath : this.getFallbackPaths(guiLanguage, LocalizedMessages.MESSAGES_FILE_NAME)) {
            try (InputStream languageFileStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
                if (languageFileStream == null) {
                    logger.debug("Localization file not found at: {}", filePath);
                    continue;
                }

                this.propertyFile = new Properties();
                this.propertyFile.load(new InputStreamReader(languageFileStream, StandardCharsets.UTF_8));

                if (this.propertyFile.isEmpty()) {
                    logger.warn("Localization file is empty at: {}", filePath);
                    continue;
                }

                this.isLoaded = true;
                fileLoaded = true;
                logger.info("Successfully loaded {} messages from: {}", this.propertyFile.size(), filePath);

                // Add some default error messages if they don't exist
                ensureDefaultErrorMessages();
                return;

            } catch (IOException exception) {
                logger.error("IO error loading localization file at: {}", filePath, exception);
                lastException = exception;
            } catch (Exception exception) {
                logger.error("Unexpected error loading localization file at: {}", filePath, exception);
                lastException = exception;
            }
        }

        if (!fileLoaded) {
            logger.error("Could not find any localization file for language: {}", guiLanguage);
            // Initialize with minimal error messages
            initializeMinimalMessages();
            this.isLoaded = false;
        }
    }

    /**
     * Ensures that critical error messages exist in the loaded properties.
     */
    private void ensureDefaultErrorMessages() {
        if (this.propertyFile == null) {
            return;
        }

        // Add default error messages if they don't exist
        Map<String, String> defaults = new HashMap<>();
        defaults.put("plugin.errors.internal.file.blank", "File name is blank");
        defaults.put("plugin.errors.internal.file.invalid", "Invalid file path");
        defaults.put("plugin.errors.internal.file.notfound", "File not found");
        defaults.put("plugin.errors.internal.unexpected", "An unexpected error occurred");

        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            this.propertyFile.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Initializes minimal messages when no localization file could be loaded.
     */
    private void initializeMinimalMessages() {
        this.propertyFile = new Properties();
        this.propertyFile.setProperty("plugin.errors.internal.file.blank", "File name is blank");
        this.propertyFile.setProperty("plugin.errors.internal.file.invalid", "Invalid file path");
        this.propertyFile.setProperty("plugin.errors.internal.file.notfound", "File not found");
        this.propertyFile.setProperty("plugin.errors.internal.unexpected", "An unexpected error occurred");
        this.propertyFile.setProperty("plugin.errors.configuration.missing", "Configuration is missing");
        logger.info("Initialized with minimal error messages");
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