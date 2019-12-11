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
package org.easysdi.extract.plugins.common;

import java.util.Properties;



/**
 * Interface to communicate the parameters required to send a notification e-mail.
 *
 * @author Yves Grasset
 */
public interface IEmailSettings {

    /**
     * Obtains the e-mail address to use as the sender of application messages.
     *
     * @return the e-mail address of the sender
     */
    String getSenderAddress();



    /**
     * Obtains the human-friendly name to use for the sender.
     *
     * @return the name of the sender
     */
    String getSenderName();



    /**
     * Obtains the name of the server to use to send electronic messages.
     *
     * @return the SMTP server name
     */
    String getSmtpHost();



    /**
     * Obtains the string to use to authenticate with the mail server.
     *
     * @return the password, or <code>null</code> if no authentication is necessary
     */
    String getSmtpPassword();



    /**
     * Obtains the TCP port to use to communicate with the mail server.
     *
     * @return the TCP port number
     */
    int getSmtpPort();



    /**
     * Obtains the user name to use to authenticate with the mail server.
     *
     * @return the SMTP user name
     */
    String getSmtpUser();



    /**
     * Obtains the protocol to use to communicate with the server.
     *
     * @return the string that identifies the protocol to use
     */
    String getTransport();



    /**
     * Obtains the type of secure connection to establish when connecting to the mail server.
     *
     * @return <code>true</code> if SSL must be used
     */
    String getSslTypeAsString();



    /**
     * Obtains whether the application must send e-mail notifications.
     *
     * @return <code>true</code> if the e-mail notifications must be sent
     */
    boolean isNotificationEnabled();



    /**
     * Determines if the current SMTP configuration data is correctly defined.
     *
     * @return <code>true</code> if the configuration is valid
     */
    boolean isValid();



    /**
     * Creates a {@link java.util.Properties} object with the current e-mail settings that are necessary to
     * start a session.
     *
     * @return the SMTP configuration properties
     */
    Properties toSystemProperties();



    /**
     * Obtains whether the connection with the mail server should authenticated with a user and password.
     *
     * @return <code>true</code> if the connection must be authenticated
     */
    boolean useAuthentication();

}
