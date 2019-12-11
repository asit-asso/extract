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
package org.easysdi.extract.context;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.easysdi.extract.connectors.ConnectorDiscovererWrapper;
import org.easysdi.extract.orchestrator.Orchestrator;
import org.easysdi.extract.plugins.TaskProcessorDiscovererWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;



/**
 * An object that carries actions based on the lifecycle of the servlet context.
 *
 * @author Yves Grasset
 */
@Component
public class ContextListener implements ServletContextListener {

    /**
     * The code of the language to use to localize the application strings.
     */
    @Value("${extract.i18n.language}")
    private String applicationLanguage;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ContextListener.class);

    /**
     * The information about the context of the servlet that executes this application.
     */
    private ServletContext servletContext;



    /**
     * Carries the appropriate actions when the servlet context has been created.
     *
     * @param event the event that triggered this method
     */
    @Override
    public final void contextInitialized(final ServletContextEvent event) {
        this.logger.info("The servlet context {} has been created.", event.getServletContext());
        this.servletContext = event.getServletContext();
    }



    /**
     * Carries the appropriate actions when the servlet context has been stopped.
     *
     * @param event the event that triggered this method
     */
    @Override
    public final void contextDestroyed(final ServletContextEvent event) {
        this.logger.info("The servlet context {} has been destroyed.", event.getServletContext());
        Orchestrator.getInstance().unscheduleMonitoring(true);

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        // Loop through all drivers
        Enumeration<Driver> driversEnumeration = DriverManager.getDrivers();

        while (driversEnumeration.hasMoreElements()) {
            Driver driver = driversEnumeration.nextElement();

            if (driver.getClass().getClassLoader() != contextClassLoader) {
                // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
                this.logger.debug("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader",
                        driver);
                continue;
            }

            // This driver was registered by the webapp's ClassLoader, so deregister it:
            try {
                this.logger.info("Deregistering JDBC driver {}", driver);
                DriverManager.deregisterDriver(driver);

            } catch (SQLException ex) {
                this.logger.error("Error deregistering JDBC driver {}", driver, ex);
            }
        }

        this.servletContext = null;
    }



    /**
     * Initializes the object that finds the available plugins to fetch and export data orders.
     *
     * @return the connector plugin finder bean
     */
    @Bean
    public ConnectorDiscovererWrapper connectorDiscoverer() {
        ConnectorDiscovererWrapper connectorDiscoverer = new ConnectorDiscovererWrapper();
        connectorDiscoverer.setApplicationLanguage(this.applicationLanguage);
        connectorDiscoverer.setServletContext(this.servletContext);

        return connectorDiscoverer;
    }



    /**
     * Initializes the object that finds the available plugins to carry process data orders.
     *
     * @return the task processor plugins finder bean
     */
    @Bean
    public TaskProcessorDiscovererWrapper taskProcessorDiscoverer() {
        TaskProcessorDiscovererWrapper taskProcessorDiscoverer = new TaskProcessorDiscovererWrapper();
        taskProcessorDiscoverer.setApplicationLanguage(this.applicationLanguage);
        taskProcessorDiscoverer.setServletContext(this.servletContext);

        return taskProcessorDiscoverer;
    }

}
