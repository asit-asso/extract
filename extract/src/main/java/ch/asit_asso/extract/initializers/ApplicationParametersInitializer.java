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
package ch.asit_asso.extract.initializers;

import ch.asit_asso.extract.domain.SystemParameter;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * An object that ensures that the parameters generic to the application are set.
 *
 * @author Yves Grasset
 */
class ApplicationParametersInitializer {

    /**
     * The default value for the parameter that defines the path of the requests data folder.
     */
    private static final String DEFAULT_BASE_PATH = "/var/extract/orders";

    /**
     * The default value for the parameter that defines the path of the requests data folder.
     */
    private static final String DEFAULT_BASE_PATH_WINDOWS = "D:\\Temp\\extract\\orders";

    /**
     * The default value for the parameter that defines the refresh interval of the dashboard.
     */
    private static final String DEFAULT_DASHBOARD_INTERVAL = "20";

    /**
     * The default value for the parameter indicating whether the application must send e-mail
     * notifications. Possible values are "true" or "false".
     */
    private static final String DEFAULT_ENABLE_MAIL_NOTIFICATIONS = "false";

    /**
     * The default value for the parameter indicating whether the orchestrator shall run or not. Possible
     * values are <code>ON</code>, <code>RANGES</code> or <code>OFF</code>
     */
    private static final String DEFAULT_SCHEDULER_MODE = "ON";

    /**
     * The default value for the parameter indicating whether the time ranges when then orchestrator shall
     * run. The value must be a valid JSON array.
     */
    private static final String DEFAULT_SCHEDULER_RANGES = "[]";

    /**
     * The default value for the parameter that defines the number of seconds between consecutive
     * executions of the orchestrator jobs.
     */
    private static final String DEFAULT_SCHEDULER_FREQUENCY = "1";

    /**
     * The default value for the parameter that defines the e-mail address of the sender for the messages
     * sent by the application.
     */
    private static final String DEFAULT_SMTP_FROM_MAIL = "extract.noreply@laboite.ch";

    /**
     * The default value for the parameter that defines the name of the sender for the messages sent by the
     * application.
     */
    private static final String DEFAULT_SMTP_FROM_NAME = "Extract";

    /**
     * The default value for the parameter that defines the password of the SMTP server.
     */
    private static final String DEFAULT_SMTP_PASSWORD = "monMotDePasseUltraSecret";

    /**
     * The default value for the parameter that defines the user name of the SMTP server.
     */
    private static final String DEFAULT_SMTP_USER = "monUser";

    /**
     * The default value for the parameter that defines the port to use to communicate with the SMTP server.
     */
    private static final String DEFAULT_SMTP_PORT = "25";

    /**
     * The default value for the parameter that defines the name of the SMTP server.
     */
    private static final String DEFAULT_SMTP_SERVER = "mail.laboite.ch";

    /**
     * The default value for the parameter that defines the type of secured connection to establish.
     */
    private static final EmailSettings.SslType DEFAULT_SSL_TYPE = EmailSettings.SslType.NONE;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ApplicationParametersInitializer.class);

    /**
     * The object that links the application parameter data objects with the data source.
     */
    private final SystemParametersRepository repository;



    /**
     * Creates a new instance of the initializer.
     *
     * @param parametersRepository the object that links the application parameter data objects with the data source.
     */
    ApplicationParametersInitializer(final SystemParametersRepository parametersRepository) {

        if (parametersRepository == null) {
            throw new IllegalArgumentException("The parameters repository cannot be null.");
        }

        this.repository = parametersRepository;
    }



    /**
     * Creates the missing application-generic parameters with default values.
     */
    public final void ensureInitialized() {
        this.logger.debug("Checking if the application parameters are all initialized.");

        this.ensureBasePathInitialized();
        this.ensureParameterInitialized(SystemParametersRepository.SCHEDULER_FREQUENCY_KEY,
                ApplicationParametersInitializer.DEFAULT_SCHEDULER_FREQUENCY);
        this.ensureParameterInitialized(SystemParametersRepository.SMTP_FROM_MAIL_KEY,
                ApplicationParametersInitializer.DEFAULT_SMTP_FROM_MAIL);
        this.ensureParameterInitialized(SystemParametersRepository.SMTP_FROM_NAME_KEY,
                ApplicationParametersInitializer.DEFAULT_SMTP_FROM_NAME);
        this.ensureParameterInitialized(SystemParametersRepository.SMTP_USER_KEY,
                ApplicationParametersInitializer.DEFAULT_SMTP_USER);
        this.ensureParameterInitialized(SystemParametersRepository.SMTP_PASSWORD_KEY,
                ApplicationParametersInitializer.DEFAULT_SMTP_PASSWORD);
        this.ensureParameterInitialized(SystemParametersRepository.SMTP_PORT_KEY,
                ApplicationParametersInitializer.DEFAULT_SMTP_PORT);
        this.ensureParameterInitialized(SystemParametersRepository.SMTP_SERVER_KEY,
                ApplicationParametersInitializer.DEFAULT_SMTP_SERVER);
        this.ensureParameterInitialized(SystemParametersRepository.SMTP_SSL_KEY,
                ApplicationParametersInitializer.DEFAULT_SSL_TYPE.name());
        this.ensureParameterInitialized(SystemParametersRepository.DASHBOARD_INTERVAL_KEY,
                ApplicationParametersInitializer.DEFAULT_DASHBOARD_INTERVAL);
        this.ensureParameterInitialized(SystemParametersRepository.ENABLE_MAIL_NOTIFICATIONS,
                ApplicationParametersInitializer.DEFAULT_ENABLE_MAIL_NOTIFICATIONS);
        this.ensureParameterInitialized(SystemParametersRepository.SCHEDULER_MODE,
                ApplicationParametersInitializer.DEFAULT_SCHEDULER_MODE);
        this.ensureParameterInitialized(SystemParametersRepository.SCHEDULER_RANGES,
                ApplicationParametersInitializer.DEFAULT_SCHEDULER_RANGES);
        this.ensureParameterInitialized(SystemParametersRepository.VALIDATION_FOCUS_PROPERTIES_KEY,
                                        "");

    }



    /**
     * Creates an application-generic parameter.
     *
     * @param key   the string that identifies the parameter
     * @param value the value of the parameter
     */
    private void createParameter(final String key, final String value) {
        this.logger.debug("Creating the parameter {} with value {}.", key, value);
        SystemParameter parameter = new SystemParameter(key);
        parameter.setValue(value);

        this.repository.save(parameter);
        this.logger.info("The application parameter {} has been created.", key);
    }



    /**
     * Creates the application-generic parameter that sets the base path for the requests data, unless this
     * parameters already exists.
     */
    private void ensureBasePathInitialized() {
        String basePath = ApplicationParametersInitializer.DEFAULT_BASE_PATH;

        if (SystemUtils.IS_OS_WINDOWS) {
            basePath = ApplicationParametersInitializer.DEFAULT_BASE_PATH_WINDOWS;
        }

        this.ensureParameterInitialized(SystemParametersRepository.BASE_PATH_KEY, basePath);
    }



    /**
     * Create an application-generic parameter if it does not exist.
     *
     * @param key   the string that identifies the parameter
     * @param value the value of the parameter
     */
    private void ensureParameterInitialized(final String key, final String value) {
        assert !StringUtils.isEmpty(key) : "The parameter key cannot be empty.";
        assert value != null : "The parameter value cannot be null.";

        if (this.repository.findByKey(key) != null) {
            this.logger.debug("The parameter {} has been found.", key);
            return;
        }

        this.createParameter(key, value);
    }

}
