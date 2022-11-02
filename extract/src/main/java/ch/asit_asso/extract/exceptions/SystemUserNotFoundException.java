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
package ch.asit_asso.extract.exceptions;



/**
 * An error caused by the absence of a system user in the database.
 *
 * @author Yves Grasset
 */
public class SystemUserNotFoundException extends RuntimeException {

    /**
     * The string that explains this error if not other is set.
     */
    private static final String DEFAULT_MESSAGE = "Could not load the default user.";



    /**
     * Creates a new instance of this error.
     */
    public SystemUserNotFoundException() {
        super(SystemUserNotFoundException.DEFAULT_MESSAGE);
    }



    /**
     * Creates a new instance of this error.
     *
     * @param message The text describing the error
     */
    public SystemUserNotFoundException(final String message) {
        super(message);
    }



    /**
     * Creates a new instance of this error.
     *
     * @param cause the error at the origin of this one, or <code>null</code> if there is not one or it is not known.
     */
    public SystemUserNotFoundException(final Throwable cause) {
        super(SystemUserNotFoundException.DEFAULT_MESSAGE, cause);
    }



    /**
     * Creates a new instance of this error.
     *
     * @param message The text describing the error
     * @param cause   the error at the origin of this one, or <code>null</code> if there is not one or it is not
     *                known.
     */
    public SystemUserNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
