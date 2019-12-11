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
package org.easysdi.extract.connectors;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.easysdi.extract.connectors.common.IConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Loads the connectors plugins in the classpath and optionally in a set of dynamically loaded JARs.
 *
 * @author Yves Grasset
 */
public final class ConnectorDiscoverer {

    /**
     * Connector discoverer instance.
     */
    private static ConnectorDiscoverer discoveryService = null;

    /**
     * Whether the plugins have already been discovered.
     */
    private boolean arePluginsInitialized;

    /**
     * A collection of all the discovered connector plugins.
     */
    private final Map<String, IConnector> pluginsMap = new HashMap<>();

    /**
     * The path to the JAR files that are susceptible to hold connector plugins.
     */
    private URL[] jarUrlsArray;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ConnectorDiscoverer.class);

    /**
     * The string that identifies the language used by the user interface.
     */
    private String applicationLanguage;



    /**
     * Creates a new instance of the connector plugin discoverer. This constructor should not be called directly
     * (singleton).
     */
    private ConnectorDiscoverer() {
        this.arePluginsInitialized = false;
    }



    /**
     * Gets the current instance of the connector discoverer and creates it first if necessary.
     *
     * @return the connector discoverer
     */
    public static synchronized ConnectorDiscoverer getInstance() {

        if (ConnectorDiscoverer.discoveryService == null) {
            ConnectorDiscoverer.discoveryService = new ConnectorDiscoverer();
        }

        return ConnectorDiscoverer.discoveryService;
    }



    /**
     * Gets all the available connectors plugins. If they have already been fetched, they will be returned from the
     * cache.
     *
     * @return a map containing all the available connector plugins with their code as key
     */
    public Map<String, IConnector> getConnectors() {
        this.logger.debug("Getting all plugins.");

        return this.getConnectors(false);
    }



    /**
     * Gets all the available connectors plugins.
     *
     * @param force true to search all the available plugins again even if plugins are cached.
     * @return a map containing all the available connector plugins with their code as key
     */
    public synchronized Map<String, IConnector> getConnectors(final boolean force) {

        if (this.arePluginsInitialized && !force) {
            this.logger.debug("The connector plugins have already been fetched. Returning those previously found.");
            return this.pluginsMap;
        }

        this.logger.debug("Fetching all the plugins.");
        this.pluginsMap.clear();
        this.logger.debug("Initializing the service loader.");
        ServiceLoader<IConnector> connectorsLoader = ServiceLoader.load(IConnector.class, this.getClassLoader());
        Iterator<IConnector> connectorsIterator = connectorsLoader.iterator();

        while (connectorsIterator.hasNext()) {
            this.logger.debug("Connector found. Attempting instantiation.");

            try {
                IConnector connector = connectorsIterator.next();
                this.logger.debug("The connector found is {}.", connector.getCode());
                this.pluginsMap.put(connector.getCode(), connector.newInstance(this.applicationLanguage));

            } catch (ServiceConfigurationError error) {
                this.logger.error("Could not instantiate a connector plugin.", error);
            }
        }

        this.arePluginsInitialized = true;
        this.logger.debug("Connector plugins discoverer initialized with {} plugins.", pluginsMap.size());

        return this.pluginsMap;
    }



    /**
     * Gets a connector plugin if available.
     *
     * @param connectorCode the code of the desired connector
     * @return the connector plugin or null if it is not available
     */
    public IConnector getConnector(final String connectorCode) {
        this.logger.debug("Getting connector plugin {}.", connectorCode);
        this.getConnectors();

        return this.pluginsMap.get(connectorCode);
    }



    /**
     * Defines the language used by the user interface.
     *
     * @param languageCode the string that identifies the language
     */
    public void setApplicationLanguage(final String languageCode) {

        if (languageCode == null || "".equals(languageCode.trim())) {
            throw new IllegalArgumentException("The application language code cannot be empty.");
        }

        this.applicationLanguage = languageCode;
    }



    /**
     * Defines a set of URLs to JAR that possibly contain connector plugins.
     *
     * @param jarUrls an array of JAR URLs
     */
    public void setJarUrls(final URL[] jarUrls) {
        this.logger.debug("Defining the URLs of the JAR files possibly containing JAR plugins.");

        if (jarUrls == null) {
            throw new IllegalArgumentException("The jarUrlsArray cannot be null.");
        }

        this.jarUrlsArray = jarUrls;
        this.logger.debug("{} JAR URLs set.", this.jarUrlsArray.length);
        this.logger.debug("Reinitializing the class loader so that it is reinstantiated with new URLs when next used.");
        this.logger.debug("Reinitializing the cached plugins so they are fetched again when next used.");
        this.arePluginsInitialized = false;
        this.pluginsMap.clear();
    }



    /**
     * Gets the class loader to use to load the connector plugins and instantiates it if necessary.
     *
     * @return the class loader object
     */
    private ClassLoader getClassLoader() {
        this.logger.debug("Getting the connectors class loader,");
        this.logger.debug("The class loader is not instantiated. Creating a new instance.");
        ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();

        if (this.jarUrlsArray == null) {
            this.logger.debug("No additional JAR URLs set. Using the default class loader.");
            return defaultClassLoader;

        }

        this.logger.debug("Instantiating a class loader with {} additional JAR URLs.",
                this.jarUrlsArray.length);
        return new URLClassLoader(jarUrlsArray, defaultClassLoader);
    }

}
