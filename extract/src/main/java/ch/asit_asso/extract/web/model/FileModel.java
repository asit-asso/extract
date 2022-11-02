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
package ch.asit_asso.extract.web.model;

import org.apache.commons.lang3.StringUtils;



/**
 * A representation of an output file for a view.
 *
 * @author Yves Grasset
 */
public class FileModel {

    /**
     * The string to display as the file name.
     */
    private final String name;

    /**
     * The path to the file.
     */
    private final String path;



    /**
     * Creates a new instance of the file model.
     *
     * @param filePath    the file that the model must represent
     * @param displayName the string to display as the file name
     */
    public FileModel(final String filePath, final String displayName) {

        if (StringUtils.isBlank(filePath)) {
            throw new IllegalArgumentException("The file path cannot be null or empty.");
        }

        if (StringUtils.isBlank(displayName)) {
            throw new IllegalArgumentException("The file name cannot be blank.");
        }

        this.name = displayName;
        this.path = filePath;
    }



    /**
     * Obtains the string to display as the file name.
     *
     * @return the file display name
     */
    public final String getName() {
        return this.name;
    }



    /**
     * Obtains the relative path to the file.
     *
     * @return the relative path
     */
    public final String getPath() {
        return this.path;
    }

}
