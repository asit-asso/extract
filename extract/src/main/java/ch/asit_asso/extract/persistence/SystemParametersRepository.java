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
package ch.asit_asso.extract.persistence;

import ch.asit_asso.extract.domain.SystemParameter;
import org.springframework.data.repository.CrudRepository;



/**
 * Link between application setting data objects and the data source.
 *
 * @author Yves Grasset
 */
public interface SystemParametersRepository extends CrudRepository<SystemParameter, String> {

    /**
     * The string identifying the parameter that defines the path of the requests data folder.
     */
    String BASE_PATH_KEY = "base_path";

    /**
     * The string identifying the parameter that defines the refresh interval of the dashboard in seconds.
     */
    String DASHBOARD_INTERVAL_KEY = "dashboard_interval";

    /**
     * The string that identifying the parameter that defines whether the application must send e-mail
     * notifications.
     */
    String ENABLE_MAIL_NOTIFICATIONS = "mails_enable";

    /**
     * The string that identifying the parameter that defines whether the orchestrator shall run.
     */
    String SCHEDULER_MODE = "op_mode";

    /**
     * The string that identifying the parameter that defines time periods when the orchestrator shall run.
     */
    String SCHEDULER_RANGES = "op_ranges";

    /**
     * The string identifying the parameter that defines the delay between successive executions of the
     * orchestrator jobs.
     */
    String SCHEDULER_FREQUENCY_KEY = "freq_scheduler_sec";

    /**
     * The string identifying the parameter that defines the e-mail of the sender for the messages sent by
     * the application.
     */
    String SMTP_FROM_MAIL_KEY = "smtp_from_mail";

    /**
     * The string identifying the parameter that defines the name of the sender for the messages sent by
     * the application.
     */
    String SMTP_FROM_NAME_KEY = "smtp_from_name";

    /**
     * The string identifying the parameter that defines the password of the SMTP server.
     */
    String SMTP_PASSWORD_KEY = "smtp_pass";

    /**
     * The string identifying the parameter that defines the port to use to communicate with the SMTP
     * server.
     */
    String SMTP_PORT_KEY = "smtp_port";

    /**
     * The string identifying the parameter that defines the name of the SMTP server.
     */
    String SMTP_SERVER_KEY = "smtp_server";

    /**
     * The string identifying the parameter that defines the login of the SMTP User.
     */
    String SMTP_USER_KEY = "smtp_user";

    /**
     * The string identifying the parameter that defines the type of secured connection to use to connect
     * to the SMTP server.
     */
    String SMTP_SSL_KEY = "smtp_ssl";


    String VALIDATION_FOCUS_PROPERTIES_KEY = "validation_focus_properties";



    /**
     * Checks if an application setting has been set with a given key.
     *
     * @param key the string that identifies the setting
     * @return <code>true</code> if a setting with the given key is set
     */
    boolean existsByKey(String key);



    /**
     * Obtains the application setting that matches a given key.
     *
     * @param key the string that identifies the setting to find
     * @return the application setting, or <code>null</code> if no settings is set with the given key
     */
    SystemParameter findByKey(String key);



    /**
     * Obtains the path of the folder that contains the data for all the requests.
     *
     * @return the absolute path of the requests data folder
     */
    String getBasePath();



    /**
     * Obtains the interval between refreshes of the dashboard data.
     *
     * @return a string containing the interval in seconds
     */
    String getDashboardRefreshInterval();



    /**
     * Gets the delay to wait before a new execution of an orchestrator job.
     *
     * @return the delay in seconds
     */
    String getSchedulerFrequency();



    /**
     * Gets the run mode of the orchestrator.
     *
     * @return <code>ON</code> if the orchestrator runs permanently, <code>RANGES</code> if it operates only in
     *         set time periods, or <code>OFF</code> if it is permanently stopped.
     */
    String getSchedulerMode();



    /**
     * Gets the time periods when the orchestrator runs if its mode is set to <code>RANGES</code>. This
     * parameter is ignored otherwise.
     *
     * @return the JSON array that contains the definitions of the time periods
     */
    String getSchedulerRanges();



    /**
     * Gets the e-mail address of the sender for the messages sent by the application.
     *
     * @return the sender address
     */
    String getSmtpFromMail();



    /**
     * Gets the name of the sender for the messages sent by the application.
     *
     * @return the sender name
     */
    String getSmtpFromName();



    /**
     * Gets the password to use to authenticate with the SMTP server.
     *
     * @return the password
     */
    String getSmtpPassword();



    /**
     * Gets the port to use to communicate with the SMTP server.
     *
     * @return the port
     */
    String getSmtpPort();



    /**
     * Gets the name of the SMTP server.
     *
     * @return the name
     */
    String getSmtpServer();



    /**
     * Gets the user that allows to authenticate with the SMTP server.
     *
     * @return the SMTP user, or <code>null</code> if the SMTP server does not require authentication
     */
    String getSmtpUser();



    /**
     * Gets the type of secured connection to establish.
     *
     * @return the SSL type
     */
    String getSmtpSSL();



    /**
     * Gets the value that defines whether the application must send e-mail notifications.
     *
     * @return <code>"true"</code> if the notifications are enabled
     */
    String isEmailNotificationEnabled();



    String getValidationFocusProperties();

}
