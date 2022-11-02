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
package ch.asit_asso.extract.web.model.json;

import com.fasterxml.jackson.annotation.JsonView;



/**
 * Information about a process to be exported as JSON.
 *
 * @author Yves Grasset
 */
public class ProcessInfo {

    /**
     * The number that identifies this process.
     */
    @JsonView(PublicField.class)
    private final Integer id;

    /**
     * The human-friendly name of this process.
     */
    @JsonView(PublicField.class)
    private final String name;



    /**
     * Obtains the identifier for this process.
     *
     * @return the number that identifies this process in the application, or <code>null</code> to indicate that
     *         no process is associated
     */
    public final Integer getId() {
        return this.id;
    }



    /**
     * Obtains the human-friendly name of this process.
     *
     * @return the process name
     */
    public final String getName() {
        return this.name;
    }



    /**
     * Creates a new representation of this process.
     *
     * @param processId   the number that identifies the process
     * @param processName the human-friendly name of the process
     */
    public ProcessInfo(final Integer processId, final String processName) {
        this.id = processId;
        this.name = processName;
    }

}
