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
package ch.asit_asso.extract.connectors.sample;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * An access to the application strings localized in a given language.
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
    private static final String LOCALIZED_FILE_PATH_FORMAT = "connectors/sample/lang/%s/%s";

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
     * The property file that contains the messages in the local language.
     */
    private Properties propertyFile;



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
     * Reads the content of a file in the current language. Fallbacks will be used if the file is not available
     * in the current language.
     *
     * @param filename the name of the file to read
     * @return the content of the file, or <code>null</code> if the file could not be read in any compatible language
     */
    public final String getFileContent(final String filename) {

        if (StringUtils.isBlank(filename) || filename.contains("../")) {
            throw new IllegalArgumentException("The filename is invalid.");
        }

        for (String filePath : this.getFallbackPaths(this.language, filename)) {

            try (InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {

                if (fileStream == null) {
                    continue;
                }

                return IOUtils.toString(fileStream, "UTF-8");

            } catch (IOException exception) {
                this.logger.error("The help page could not be loaded.", exception);
            }
        }

        return null;
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
     * Reads the file that holds the application strings in a given language. Fallbacks will be used if the
     * application string file is not available in the given language.
     *
     * @param guiLanguage the string that identifies the language to use for the messages to the user
     */
    private void loadFile(final String guiLanguage) {
        this.logger.debug("Loading the localization file for language {}.", guiLanguage);

        if (guiLanguage == null || !guiLanguage.matches(LocalizedMessages.LOCALE_VALIDATION_PATTERN)) {
            this.logger.error("The language string \"{}\" is not a valid locale.", guiLanguage);
            throw new IllegalArgumentException(String.format("The language code \"%s\" is invalid.", guiLanguage));
        }

        for (String filePath : this.getFallbackPaths(guiLanguage, LocalizedMessages.MESSAGES_FILE_NAME)) {

            try (InputStream languageFileStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {

                if (languageFileStream == null) {
                    this.logger.debug("Could not find a localization file at \"{}\".", filePath);
                    continue;
                }

                this.propertyFile = new Properties();
                this.propertyFile.load(languageFileStream);
                break; // Stop after successfully loading the first available file

            } catch (IOException exception) {
                this.logger.error("Could not load the localization file.");
                this.propertyFile = null;
            }
        }

        if (this.propertyFile == null) {
            this.logger.error("Could not find any localization file, not even the default.");
            throw new IllegalStateException("Could not find any localization file.");
        }

        this.logger.info("Localized messages loaded.");
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
