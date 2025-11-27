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
package ch.asit_asso.extract.plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Loads the task processors plugins in the classpath and optionally in a set of dynamically loaded JARs.
 *
 * @author Yves Grasset
 */
public final class TaskProcessorsDiscoverer {

    /**
     * The instance of the task plugins discoverer.
     */
    private static TaskProcessorsDiscoverer discoveryService = null;

    /**
     * Whether the task plugins have already been discovered.
     */
    private boolean arePluginsInitialized;

    /**
     * A collection of the task plugins that have been discovered.
     */
    private final Map<String, ITaskProcessor> pluginsMap = new HashMap<>();

    /**
     * The paths of the JAR files that are susceptible to hold task plugins.
     */
    private URL[] jarUrlsArray;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(TaskProcessorsDiscoverer.class);

    /**
     * The language used by the user interface.
     */
    private String applicationLanguage;



    /**
     * Creates a new instance of the task processor plugin discoverer. This constructor should not be called directly
     * (singleton).
     */
    private TaskProcessorsDiscoverer() {
        this.arePluginsInitialized = false;
    }



    /**
     * Gets the current instance of the connector discoverer and creates it first if necessary.
     *
     * @return the task processor discoverer
     */
    public static synchronized TaskProcessorsDiscoverer getInstance() {

        if (TaskProcessorsDiscoverer.discoveryService == null) {
            TaskProcessorsDiscoverer.discoveryService = new TaskProcessorsDiscoverer();
        }

        return TaskProcessorsDiscoverer.discoveryService;
    }



    /**
     * Gets all the available task processors plugins. If they have already been fetched, they will be returned from the
     * cache.
     *
     * @return a map containing all the available task processors plugins with their code as key
     */
    public Map<String, ITaskProcessor> getTaskProcessors() {
        this.logger.debug("Getting all plugins.");

        return this.getTaskProcessors(false);
    }



    /**
     * Gets all the available task processors plugins.
     *
     * @param force true to search all the available plugins again even if plugins are cached.
     * @return a map containing all the available task processors plugins with their code as key
     */
    public synchronized Map<String, ITaskProcessor> getTaskProcessors(final boolean force) {

        if (this.arePluginsInitialized && !force) {
            this.logger.debug("The task processors plugins have already been fetched. Returning those previously"
                    + " found.");
            return this.pluginsMap;
        }

        this.logger.debug("Fetching all the plugins.");
        this.pluginsMap.clear();
        this.logger.debug("Initializing the service loader.");
        ServiceLoader<ITaskProcessor> taskProcessorsLoader
                = ServiceLoader.load(ITaskProcessor.class, this.getClassLoader());
        Iterator<ITaskProcessor> taskProcessorsIterator = taskProcessorsLoader.iterator();

        while (taskProcessorsIterator.hasNext()) {
            this.logger.debug("Task processor found. Attempting instantiation.");

            try {
                ITaskProcessor taskProcessor = taskProcessorsIterator.next();
                this.logger.debug("The task processor found is {}.", taskProcessor.getCode());
                this.pluginsMap.put(taskProcessor.getCode(), taskProcessor.newInstance(this.applicationLanguage));

            } catch (ServiceConfigurationError error) {
                this.logger.error("Could not instantiate a task processor plugin.", error);
            }
        }

        this.arePluginsInitialized = true;
        this.logger.debug("Task processors plugins discoverer initialized with {} plugins.", pluginsMap.size());

        return this.pluginsMap;
    }



    /**
     * Gets a task processor plugin if available.
     *
     * @param code the code of the desired task processor
     * @return the task processor plugin or null if it is not available
     */
    public ITaskProcessor getTaskProcessor(final String code) {
        this.logger.debug("Getting task processor plugin {}.", code);

        return this.getTaskProcessors().get(code);
    }



    /**
     * Defines the language used by the application to display messages to the users.
     *
     * @param languageCode the locale code of the language
     */
    public void setApplicationLanguage(final String languageCode) {

        if (languageCode == null) {
            throw new IllegalArgumentException("The application language code cannot be null.");
        }

        // If the language has changed, force reinitialization of plugins
        if (!languageCode.equals(this.applicationLanguage)) {
            this.applicationLanguage = languageCode;
            this.logger.debug("Application language changed to {}. Forcing plugin reinitialization.", languageCode);
            this.arePluginsInitialized = false;
            this.pluginsMap.clear();
        } else {
            this.applicationLanguage = languageCode;
        }
    }



    /**
     * Defines a set of URLs to JAR that possibly contain task processors plugins.
     *
     * @param jarUrls an array of JAR URLs
     */
    public void setJarUrls(final URL[] jarUrls) {
        this.logger.debug("Defining the URLs of the JAR files possibly containing task processors plugins.");

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
     * Gets the class loader to use to load the task processors plugins and instantiates it if necessary.
     *
     * @return the class loader object
     */
    private ClassLoader getClassLoader() {
        this.logger.debug("Getting the task processors class loader,");

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
