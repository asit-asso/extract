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
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access to the settings for the FME Server V2 plugin.
 * Provides robust configuration loading with comprehensive error handling.
 *
 * @author Extract Team
 */
public class PluginConfiguration {

    /**
     * The writer to the application logs.
     */
    private static final Logger logger = LoggerFactory.getLogger(PluginConfiguration.class);

    /**
     * The default path to the configuration file.
     */
    private static final String DEFAULT_CONFIG_PATH = "plugins/fmeserverv2/properties/config.properties";

    /**
     * The properties file that holds the plugin settings.
     */
    private Properties properties;

    /**
     * Flag indicating if configuration was successfully loaded.
     */
    private boolean isConfigurationLoaded = false;

    /**
     * Creates a new settings access instance with the default configuration path.
     */
    public PluginConfiguration() {
        this(DEFAULT_CONFIG_PATH);
    }

    /**
     * Creates a new settings access instance.
     *
     * @param path a string with the path to the properties file that holds the plugin settings
     */
    public PluginConfiguration(final String path) {
        if (path == null || path.trim().isEmpty()) {
            logger.error("Configuration path is null or empty");
            throw new IllegalArgumentException("Configuration path cannot be null or empty");
        }

        // Security: Path traversal prevention
        if (path.contains("..") || path.contains("\\")) {
            logger.error("Invalid configuration path: {}", path);
            throw new IllegalArgumentException("Configuration path contains invalid characters");
        }

        this.initializeConfiguration(path);
    }

    /**
     * Loads the plugin configuration with comprehensive error handling.
     *
     * @param path a string with the path to the properties file that holds the plugin settings
     */
    private void initializeConfiguration(final String path) {
        logger.debug("Initializing configuration from path: {}", path);

        try (InputStream propertiesIs = this.getClass().getClassLoader().getResourceAsStream(path)) {

            if (propertiesIs == null) {
                logger.error("Configuration file not found at path: {}", path);
                throw new IllegalStateException("Configuration file not found: " + path);
            }

            this.properties = new Properties();
            this.properties.load(propertiesIs);
            this.isConfigurationLoaded = true;

            // Validate required properties exist
            validateConfiguration();

            logger.info("Plugin configuration successfully initialized from: {}", path);

        } catch (IOException ex) {
            logger.error("Failed to load configuration from path: {}", path, ex);
            throw new IllegalStateException("Failed to load configuration file", ex);
        } catch (Exception ex) {
            logger.error("Unexpected error during configuration initialization", ex);
            throw new IllegalStateException("Configuration initialization failed", ex);
        }
    }

    /**
     * Validates that essential configuration properties are present.
     */
    private void validateConfiguration() {
        // These are the expected configuration keys based on the original config
        String[] requiredKeys = {
            "paramRequestInternalId",
            "paramRequestFolderOut",
            "paramRequestPerimeter",
            "paramRequestParameters"
        };

        for (String key : requiredKeys) {
            if (!properties.containsKey(key)) {
                logger.warn("Configuration missing expected key: {}", key);
            }
        }

        logger.debug("Configuration validation completed. Properties loaded: {}", properties.size());
    }

    /**
     * Obtains the value of a plugin setting with null-safety.
     *
     * @param key the string that identifies the setting
     * @return the value string, or null if the key doesn't exist
     * @throws IllegalStateException if the configuration is not loaded
     * @throws IllegalArgumentException if the key is null or empty
     */
    public final String getProperty(final String key) {
        if (!isConfigurationLoaded || properties == null) {
            logger.error("Attempted to get property '{}' but configuration is not loaded", key);
            throw new IllegalStateException("Configuration file is not loaded");
        }

        if (key == null || key.trim().isEmpty()) {
            logger.error("Property key is null or empty");
            throw new IllegalArgumentException("Property key cannot be null or empty");
        }

        String value = this.properties.getProperty(key);

        if (value == null) {
            logger.debug("Property '{}' not found in configuration", key);
        } else {
            logger.trace("Retrieved property '{}' = '{}'", key, value.length() > 50 ? value.substring(0, 50) + "..." : value);
        }

        return value;
    }

    /**
     * Obtains the value of a plugin setting with a default fallback.
     *
     * @param key the string that identifies the setting
     * @param defaultValue the value to return if the key is not found
     * @return the value string, or the default value if the key doesn't exist
     */
    public final String getProperty(final String key, final String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Checks if a configuration property exists.
     *
     * @param key the string that identifies the setting
     * @return true if the property exists, false otherwise
     */
    public final boolean hasProperty(final String key) {
        if (!isConfigurationLoaded || properties == null) {
            return false;
        }

        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        return this.properties.containsKey(key);
    }

    /**
     * Gets the number of properties loaded.
     *
     * @return the number of configuration properties
     */
    public final int getPropertyCount() {
        return isConfigurationLoaded && properties != null ? properties.size() : 0;
    }

    /**
     * Checks if the configuration was successfully loaded.
     *
     * @return true if configuration is loaded, false otherwise
     */
    public final boolean isConfigurationLoaded() {
        return isConfigurationLoaded;
    }

    /**
     * Reloads the configuration from the default path.
     * Useful for refreshing configuration during runtime.
     */
    public final void reloadConfiguration() {
        reloadConfiguration(DEFAULT_CONFIG_PATH);
    }

    /**
     * Reloads the configuration from a specified path.
     *
     * @param path the path to the configuration file
     */
    public final void reloadConfiguration(final String path) {
        logger.info("Reloading configuration from path: {}", path);
        this.isConfigurationLoaded = false;
        this.properties = null;
        initializeConfiguration(path);
    }
}