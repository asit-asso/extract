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
package ch.asit_asso.extract.web;

import org.springframework.util.StringUtils;



/**
 * An information to the user about an application event.
 *
 * @author Yves Grasset
 */
public class Message {

    /**
     * The string that identifies the localized text of this message.
     */
    private final String messageKey;

    /**
     * The severity of this message.
     */
    private final MessageType messageType;



    /**
     * Obtains the string that identifies the localized text of this message.
     *
     * @return the message text key
     */
    public final String getMessageKey() {
        return this.messageKey;
    }



    /**
     * Obtains the severity of this message.
     *
     * @return the type of this message
     */
    public final MessageType getMessageType() {
        return this.messageType;
    }



    /**
     * The type of information that this message carries.
     */
    public enum MessageType {
        /**
         * The message is about an operation that failed.
         */
        ERROR,
        /**
         * The message is purely informational.
         */
        INFO,
        /**
         * The message is about an operation that completed successfully.
         */
        SUCCESS,
        /**
         * The message is about something that does not prevent the operation to fail but that may have
         * other consequences.
         */
        WARNING
    }



    /**
     * Creates a new message instance.
     *
     * @param key  the string that identifies the localized text of the message
     * @param type the severity of the message
     */
    public Message(final String key, final MessageType type) {

        if (!StringUtils.hasLength(key)) {
            throw new IllegalArgumentException("The message key cannot be empty.");
        }

        this.messageKey = key;
        this.messageType = type;
    }

}
