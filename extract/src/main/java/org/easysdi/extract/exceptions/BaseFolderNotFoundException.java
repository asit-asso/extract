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
package org.easysdi.extract.exceptions;



/**
 * An error thrown when the directory that contains the data for all data item request is not accessible.
 *
 * @author Yves Grasset
 */
public class BaseFolderNotFoundException extends RuntimeException {

    /**
     * The string that describes this error if no other is set.
     */
    private static final String DEFAULT_MESSAGE
            = "The target of the request data base folder path is not accessible or is not a directory.";



    /**
     * Creates a new instance of this error.
     */
    public BaseFolderNotFoundException() {
        super(BaseFolderNotFoundException.DEFAULT_MESSAGE);
    }



    /**
     * Creates a new instance of this error.
     *
     * @param message The text describing the error
     */
    public BaseFolderNotFoundException(final String message) {
        super(message);
    }



    /**
     * Creates a new instance of this error.
     *
     * @param cause the error at the origin of this one, or <code>null</code> if there is not one or it is not known.
     */
    public BaseFolderNotFoundException(final Throwable cause) {
        super(BaseFolderNotFoundException.DEFAULT_MESSAGE, cause);
    }



    /**
     * Creates a new instance of this error.
     *
     * @param message The text describing the error
     * @param cause   the error at the origin of this one, or <code>null</code> if there is not one or it is not
     *                known.
     */
    public BaseFolderNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
