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
package ch.asit_asso.extract.plugins.implementation;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;

import ch.asit_asso.extract.plugins.TaskProcessorsDiscoverer;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;



/**
 * Provides an interface to the task processor plugins discoverer for this application context.
 *
 * @author Yves Grasset
 */
public class TaskProcessorDiscovererWrapper implements ServletContextAware {

    /**
     * The writer to the application logs.
     */
    private final transient Logger logger = LoggerFactory.getLogger(TaskProcessorDiscovererWrapper.class);

    /**
     * The object that parses JARs to find the available task plugins.
     */
    private TaskProcessorsDiscoverer taskProcessorDiscoverer;

    /**
     * Information about the context that the current servlet executes in.
     */
    private WeakReference<ServletContext> servletContext;

    /**
     * The ISO code of the user interface language.
     */
    private String applicationLanguage;



    /**
     * Obtains all the available task processors plugins.
     *
     * @return a map containing the available task processors plugins with their code as key
     */
    public final Map<String, ITaskProcessor> getTaskProcessors() {
        this.logger.debug("Getting all the task processors plugins available.");

        return this.getTaskProcessorDiscoverer().getTaskProcessors();
    }



    /**
     * Obtains all the available task processor plugins ordered by their label.
     *
     * @return a map of available plugins
     */
    public final Map<String, ITaskProcessor> getTaskProcessorsOrderedByLabel() {
        this.logger.debug("Getting all the task processors plugins available (ordered by label).");

        Map<String, ITaskProcessor> taskProcessors = this.getTaskProcessorDiscoverer().getTaskProcessors();

        //Sorted by label
        List<Map.Entry<String, ITaskProcessor>> values = new ArrayList<>(taskProcessors.entrySet());
        Comparator<Map.Entry<String, ITaskProcessor>> tcComparator
                = Comparator.comparing(entry -> entry.getValue().getLabel());
        values.sort(tcComparator);
        taskProcessors = new LinkedHashMap<>();

        for (Map.Entry<String, ITaskProcessor> entry : values) {
            taskProcessors.put(entry.getKey(), entry.getValue());
        }

        return taskProcessors;
    }



    /**
     * Obtains a specific task processor plugin if available.
     *
     * @param code the code identifying the desired plugin
     * @return The task processor plugin or null if it is not available
     */
    public final ITaskProcessor getTaskProcessor(final String code) {
        this.logger.debug("Getting task processor {}.", code);
        return this.getTaskProcessorDiscoverer().getTaskProcessor(code);
    }



    /**
     * Defines the language used by the application to display messages to the user.
     *
     * @param languageCode the locale code of the language
     */
    public final void setApplicationLanguage(final String languageCode) {

        if (languageCode == null) {
            throw new IllegalArgumentException("The application language code cannot be null.");
        }

        this.applicationLanguage = languageCode;
    }



    /**
     * Defines the context of the servlet that calls the task processors discoverer.
     *
     * @param servletContextInfo The current servlet context
     */
    @Override
    public final void setServletContext(final @NotNull ServletContext servletContextInfo) {
        this.logger.debug("Setting the servlet context.");
        this.servletContext = new WeakReference<>(servletContextInfo);
    }



    /**
     * Gets the current instance of the task processor discoverer and initializes it for the current context if
     * necessary.
     *
     * @return the current task processor discoverer
     */
    private synchronized TaskProcessorsDiscoverer getTaskProcessorDiscoverer() {

        if (this.taskProcessorDiscoverer == null) {
            this.logger.debug("Instantiating the task processor discoverer.");
            this.taskProcessorDiscoverer = TaskProcessorsDiscoverer.getInstance();
            this.taskProcessorDiscoverer.setApplicationLanguage(this.applicationLanguage);
            URL[] jarUrlsArray = this.getJarUrls();

            if (jarUrlsArray != null) {
                this.logger.debug("Setting the task processors JAR URLs.");
                this.taskProcessorDiscoverer.setJarUrls(jarUrlsArray);
            } else {
                this.taskProcessorDiscoverer.setJarUrls(new URL[]{});
                this.logger.warn("The returned JAR URLs was null. Using the default system class loader.");
            }
        }

        return this.taskProcessorDiscoverer;
    }



    /**
     * Assembles a collection of URLs to JAR files that possibly contain task processors plugins.
     *
     * @return an array containing the JAR files URLs
     */
    private URL[] getJarUrls() {
        this.logger.debug("Assembling an array of task processors JAR URLs.");
        ServletContext context = this.servletContext.get();

        if (context == null) {
            return null;
        }

        WebApplicationContext webAppContext = WebApplicationContextUtils.getWebApplicationContext(context);

        if (webAppContext == null) {
            return null;
        }
        
        Resource[] jarResources;

        try {
            jarResources = webAppContext.getResources("WEB-INF/classes/task_processors/*.jar");

        } catch (IOException ex) {
            this.logger.error("An error occurred while the task processors plugins JARs were searched.", ex);
            return null;
        }

        List<URL> jarUrlsArray = new ArrayList<>();

        for (Resource resource : jarResources) {
            File file;

            try {
                file = resource.getFile();
            } catch (IOException ex) {
                this.logger.error("Impossible to load the resource file.", ex);
                continue;
            }

            this.logger.debug("Found task processor plugin JAR {}", file.getAbsolutePath());

            try {
                URL jarUrl = resource.getURL();
                this.logger.debug("The URL of the task processor plugin JAR is {}.", jarUrl);
                jarUrlsArray.add(new URL("jar:" + jarUrl + "!/"));

            } catch (IOException ex) {
                this.logger.error("The JAR URL is not properly formed.", ex);
            }

        }

        return jarUrlsArray.toArray(new URL[]{});
    }

}
