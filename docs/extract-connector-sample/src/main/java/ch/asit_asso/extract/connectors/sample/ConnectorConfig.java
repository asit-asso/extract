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
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Access to the connector plugin configuration.
 *
 * @author Florent Krin
 */
public class ConnectorConfig {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ConnectorConfig.class);

    /**
     * The configuration entries.
     */
    private Properties configurationProperties;



    /**
     * Creates a new plugin configuration instance.
     *
     * @param path a string containing the path to the properties file that holds the plugin confguration
     */
    public ConnectorConfig(final String path) {
        initConfig(path);
    }



    /**
     * Processes the plugin configuration.
     *
     * @param path a string containing the path to the properties file that holds the plugin confguration
     */
    private void initConfig(final String path) {
        this.logger.debug("Initializing config from path {}.", path);

        try {
            InputStream propertiesIs = this.getClass().getClassLoader().getResourceAsStream(path);
            this.configurationProperties = new Properties();
            this.configurationProperties.load(propertiesIs);
            this.logger.debug("Connector configuration successfully initialized.");

        } catch (IOException ex) {
            this.logger.error("An input/output error occurred during the connector configuration initialization.", ex);
        }
    }



    /**
     * Obtains the value of a configuration entry.
     *
     * @param key the string that identifies the entry to fetch
     * @return the configuration value as a string
     */
    public final String getProperty(final String key) {
        String value = null;

        if (configurationProperties != null) {
            value = configurationProperties.getProperty(key);
        }

        return value;
    }

}
