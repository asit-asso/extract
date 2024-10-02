package ch.asit_asso.extract.connectors.easysdiv4.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutUtils {

    /**
     * The default request timeout in millis (4sec)
     */
    public static final int DEFAULT_TIMEOUT_MILLIS = 4000;

    /**
     * The minimum timeout (1sec)
     */
    public static final int MIN_TIMEOUT_MILLIS = 1000;

    /**
     * The maximum timeout (60sec)
     */
    public static final int MAX_TIMEOUT_MILLIS = 60000;

    /**
     * Class logger
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(TimeoutUtils.class);

    /**
     * Parse the timeout value given as a string
     * @param timeoutStr A string value defining the timeout
     */
    public static int parseTimeout(final String timeoutStr)
    {
        int timeoutInMilliseconds;

        try {
            if (timeoutStr == null) {
                LOGGER.warn("Timeout configuration key is missing, using default timeout of {} ms.", DEFAULT_TIMEOUT_MILLIS);
                timeoutInMilliseconds = DEFAULT_TIMEOUT_MILLIS;
            } else {
                timeoutInMilliseconds = Integer.parseInt(timeoutStr);

                if (timeoutInMilliseconds <= MIN_TIMEOUT_MILLIS || timeoutInMilliseconds > MAX_TIMEOUT_MILLIS) {
                    LOGGER.warn("Timeout value {} ms is out of bounds, using default timeout of {} ms.", timeoutInMilliseconds, DEFAULT_TIMEOUT_MILLIS);
                    timeoutInMilliseconds = DEFAULT_TIMEOUT_MILLIS;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to parse timeout value, using default using default timeout of {} ms.", DEFAULT_TIMEOUT_MILLIS, e);
            timeoutInMilliseconds = DEFAULT_TIMEOUT_MILLIS;
        }

        return timeoutInMilliseconds;
    }
}

