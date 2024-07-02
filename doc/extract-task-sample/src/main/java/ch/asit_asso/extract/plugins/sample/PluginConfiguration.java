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
package ch.asit_asso.extract.plugins.sample;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Access to the settings for the remark plugin.
 * NO CHANGES NEEDED IN THIS METHOD
 * @author Florent Krin
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
        this.logger.debug("Initializing config from path {}.", path);

        try {
            InputStream propertiesIs = this.getClass().getClassLoader().getResourceAsStream(path);
            this.properties = new Properties();
            this.properties.load(propertiesIs);
            this.logger.debug("Connector configuration successfully initialized.");

        } catch (IOException ex) {
            this.logger.error("An input/output error occurred during the connector configuration initialization.", ex);
        }
    }



    /**
     * Obtains the value of a plugin setting.
     *
     * @param key the string that identifies the setting
     * @return the value string
     */
    public final String getProperty(final String key) {

        if (properties == null) {
            throw new IllegalStateException("The configuration file is not loaded.");
        }

        return this.properties.getProperty(key);
    }

}
