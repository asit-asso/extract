package ch.asit_asso.extract.utils;

import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UrlUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(UrlUtils.class);
    
    public static String getApplicationPath(String fullUrl) {

        UrlUtils.LOGGER.debug("Getting application path from {}", fullUrl);

        try {
            URL url = new URL(fullUrl);
            String path = url.getPath();
            UrlUtils.LOGGER.debug("URL path is {}", path);

            if (path.isEmpty()) {
                UrlUtils.LOGGER.debug("URL application path is empty. Returning /");

                return "/";
            }

            int secondSlashIndex = path.indexOf('/', 1);

            if (secondSlashIndex == -1) {
                UrlUtils.LOGGER.debug("No second slash. Application path is {}.", path);

                return path;
            }

            UrlUtils.LOGGER.debug("Removing path data after second slash at index {}", secondSlashIndex);
            path = path.substring(0, secondSlashIndex);
            UrlUtils.LOGGER.debug("Application path is {}", path);

            return path;

        } catch (MalformedURLException exception) {
            UrlUtils.LOGGER.error("Could not parse the provided URL.", exception);
            throw new IllegalArgumentException("The given URL is invalid", exception);
        }
    }
}
