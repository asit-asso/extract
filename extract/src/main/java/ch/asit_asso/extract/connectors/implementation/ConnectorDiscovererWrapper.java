package ch.asit_asso.extract.connectors.implementation;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;

import ch.asit_asso.extract.connectors.ConnectorDiscoverer;
import ch.asit_asso.extract.connectors.common.IConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;



/**
 * Provides an interface to the connector plugins discoverer for this application context.
 *
 * @author Yves Grasset
 */
public class ConnectorDiscovererWrapper implements ServletContextAware {

    /**
     * The writer to the application logs.
     */
    private final transient Logger logger = LoggerFactory.getLogger(ConnectorDiscovererWrapper.class);

    /**
     * The objects that finds the available connector plugins.
     */
    private ConnectorDiscoverer connectorDiscoverer;

    /**
     * The information about the context that the current servlet runs in.
     */
    private WeakReference<ServletContext> servletContext;

    /**
     * The ISO code of the language to use for the user interface.
     */
    private String applicationLanguage;


    /**
     * Obtains all the available connector plugins.
     *
     * @return a map containing the available connector plugins with their code as key
     */
    public final Map<String, IConnector> getConnectors() {
        this.logger.debug("Getting all the connector plugins available.");

        return this.getConnectorDiscoverer().getConnectors();
    }


    /**
     * Obtains a specific connector plugin if available.
     *
     * @param code the code identifying the desired plugin
     * @return The connector plugin or null if it is not available
     */
    public final IConnector getConnector(final String code) {
        this.logger.debug("Getting connector {}.", code);
        return this.getConnectorDiscoverer().getConnector(code);
    }


    /**
     * Defines the language used by the application to display messages to the users.
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
     * Defines the context of the servlet that calls the connector discoverer.
     *
     * @param context The current servlet context
     */
    @Override
    public final void setServletContext(final ServletContext context) {
        this.logger.debug("Setting the servlet context.");
        this.servletContext = new WeakReference<>(context);
    }


    /**
     * Gets the current instance of the connector discoverer and initializes it for the current context if necessary.
     *
     * @return the current connector discoverer
     */
    private ConnectorDiscoverer getConnectorDiscoverer() {

        if (this.connectorDiscoverer == null) {
            this.logger.debug("Instantiating the connector discoverer.");
            this.connectorDiscoverer = ConnectorDiscoverer.getInstance();
            this.connectorDiscoverer.setApplicationLanguage(this.applicationLanguage);
            URL[] jarUrlsArray = this.getJarUrls();

            if (jarUrlsArray != null) {
                this.logger.debug("Setting the connectors JAR URLs.");
                this.connectorDiscoverer.setJarUrls(jarUrlsArray);
            } else {
                this.connectorDiscoverer.setJarUrls(new URL[]{});
                this.logger.warn("The returned JAR URLs was null. Using the default system class loader.");
            }
        }

        return this.connectorDiscoverer;
    }


    /**
     * Assembles a collection of URLs to JAR files that possibly contain connector plugins.
     *
     * @return an array containing the JAR files URLs
     */
    private URL[] getJarUrls() {
        this.logger.debug("Assembling an array of connectors JAR URLs.");
        WebApplicationContext webAppContext = WebApplicationContextUtils.getWebApplicationContext(servletContext.get());
        Resource[] jarResources;

        try {
            jarResources = webAppContext.getResources("WEB-INF/classes/connectors/*.jar");

        } catch (IOException ex) {
            this.logger.error("An error occurred while the connector plugins JARs were searched.", ex);
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

            this.logger.debug("Found connector plugin JAR {}", file.getAbsolutePath());

            try {
                URL jarUrl = resource.getURL();
                this.logger.debug("The URL of the connector plugin JAR is {}.", jarUrl);
                jarUrlsArray.add(new URL("jar:" + jarUrl + "!/"));

            } catch (IOException ex) {
                this.logger.error("The JAR URL is not properly formed.", ex);
            }
        }

        return jarUrlsArray.toArray(new URL[]{});
    }
}
