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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Access to the settings for the FME Server V2 plugin.
 *
 * @author Extract Team
 */
public class PluginConfiguration {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(PluginConfiguration.class);

    /**
     * The properties file that holds the plugin settings.
     */
    private Properties properties;

    /**
     * Creates a new settings access instance.
     *
     * @param path a string with the path to the properties file that holds the plugin settings
     */
    public PluginConfiguration(final String path) {
        this.initializeConfiguration(path);
    }

    /**
     * Loads the plugin configuration.
     *
     * @param path a string with the path to the properties file that holds the plugin settings
     */
    private void initializeConfiguration(final String path) {
        this.logger.debug("Initializing FME Server V2 plugin config from path {}.", path);

        try (InputStream propertiesIs = this.getClass().getClassLoader().getResourceAsStream(path)) {
            
            if (propertiesIs == null) {
                this.logger.warn("Configuration file not found at path: {}. Using default configuration.", path);
                this.properties = createDefaultProperties();
            } else {
                this.properties = new Properties();
                this.properties.load(propertiesIs);
                this.logger.debug("FME Server V2 plugin configuration successfully initialized.");
            }

        } catch (IOException ex) {
            this.logger.error("An input/output error occurred during the plugin configuration initialization.", ex);
            this.logger.warn("Using default configuration due to error.");
            this.properties = createDefaultProperties();
        }
    }

    /**
     * Creates default properties for the plugin when configuration file is not available.
     *
     * @return a Properties object with default values
     */
    private Properties createDefaultProperties() {
        Properties defaultProps = new Properties();
        
        // Default timeout values
        defaultProps.setProperty("request.timeout.seconds", "300");
        defaultProps.setProperty("connect.timeout.seconds", "30");
        defaultProps.setProperty("read.timeout.seconds", "300");
        
        // Default retry settings
        defaultProps.setProperty("retry.max.attempts", "3");
        defaultProps.setProperty("retry.backoff.multiplier", "2");
        
        // Default file size limits
        defaultProps.setProperty("download.max.size.mb", "500");
        
        // Default execution mode
        defaultProps.setProperty("execution.mode.default", "sync");
        
        // Default GeoJSON parameter name
        defaultProps.setProperty("geojson.parameter.default", "GEOJSON_INPUT");
        
        // SSL settings
        defaultProps.setProperty("ssl.verify.certificates", "true");
        defaultProps.setProperty("ssl.verify.hostname", "true");
        
        this.logger.debug("Created default configuration properties.");
        return defaultProps;
    }

    /**
     * Obtains the value of a plugin setting.
     *
     * @param key the string that identifies the setting
     * @return the value string, or null if not found
     */
    public final String getProperty(final String key) {
        if (properties == null) {
            throw new IllegalStateException("The configuration file is not loaded.");
        }

        return this.properties.getProperty(key);
    }

    /**
     * Obtains the value of a plugin setting with a default value.
     *
     * @param key the string that identifies the setting
     * @param defaultValue the default value to return if the property is not found
     * @return the value string, or the default value if not found
     */
    public final String getProperty(final String key, final String defaultValue) {
        if (properties == null) {
            throw new IllegalStateException("The configuration file is not loaded.");
        }

        return this.properties.getProperty(key, defaultValue);
    }

    /**
     * Obtains the value of a plugin setting as an integer.
     *
     * @param key the string that identifies the setting
     * @param defaultValue the default value to return if the property is not found or cannot be parsed
     * @return the integer value, or the default value if not found or invalid
     */
    public final int getIntProperty(final String key, final int defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            this.logger.warn("Property '{}' has invalid integer value '{}', using default: {}", 
                           key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Obtains the value of a plugin setting as a long.
     *
     * @param key the string that identifies the setting
     * @param defaultValue the default value to return if the property is not found or cannot be parsed
     * @return the long value, or the default value if not found or invalid
     */
    public final long getLongProperty(final String key, final long defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            this.logger.warn("Property '{}' has invalid long value '{}', using default: {}", 
                           key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Obtains the value of a plugin setting as a boolean.
     *
     * @param key the string that identifies the setting
     * @param defaultValue the default value to return if the property is not found
     * @return the boolean value, or the default value if not found
     */
    public final boolean getBooleanProperty(final String key, final boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    /**
     * Gets the request timeout in seconds.
     *
     * @return the request timeout in seconds
     */
    public final int getRequestTimeoutSeconds() {
        return getIntProperty("request.timeout.seconds", 300);
    }

    /**
     * Gets the connection timeout in seconds.
     *
     * @return the connection timeout in seconds
     */
    public final int getConnectTimeoutSeconds() {
        return getIntProperty("connect.timeout.seconds", 30);
    }

    /**
     * Gets the read timeout in seconds.
     *
     * @return the read timeout in seconds
     */
    public final int getReadTimeoutSeconds() {
        return getIntProperty("read.timeout.seconds", 300);
    }

    /**
     * Gets the maximum number of retry attempts.
     *
     * @return the maximum retry attempts
     */
    public final int getMaxRetryAttempts() {
        return getIntProperty("retry.max.attempts", 3);
    }

    /**
     * Gets the maximum download size in bytes.
     *
     * @return the maximum download size in bytes
     */
    public final long getMaxDownloadSize() {
        long defaultSizeInMb = 500L;
        long sizeInMb = getLongProperty("download.max.size.mb", defaultSizeInMb);
        return sizeInMb * 1024L * 1024L; // Convert MB to bytes
    }

    /**
     * Gets the default execution mode.
     *
     * @return the default execution mode (sync or async)
     */
    public final String getDefaultExecutionMode() {
        return getProperty("execution.mode.default", "sync");
    }

    /**
     * Gets the default GeoJSON parameter name.
     *
     * @return the default GeoJSON parameter name
     */
    public final String getDefaultGeoJsonParameter() {
        return getProperty("geojson.parameter.default", "GEOJSON_INPUT");
    }

    /**
     * Checks if SSL certificate verification is enabled.
     *
     * @return true if SSL certificates should be verified
     */
    public final boolean isSslVerifyCertificates() {
        return getBooleanProperty("ssl.verify.certificates", true);
    }

    /**
     * Checks if SSL hostname verification is enabled.
     *
     * @return true if SSL hostnames should be verified
     */
    public final boolean isSslVerifyHostname() {
        return getBooleanProperty("ssl.verify.hostname", true);
    }
}