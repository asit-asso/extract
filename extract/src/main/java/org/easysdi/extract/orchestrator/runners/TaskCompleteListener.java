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
package org.easysdi.extract.orchestrator.runners;



/**
 * An object that wants to be notified when threads finish.
 *
 * @author Yves Grasset
 * @param <T> the type of the result object returned by the task.
 */
public interface TaskCompleteListener<T> {

    /**
     * Tells this object that a thread has finished its processing.
     *
     * @param result the result returned by the task.
     */
    void notifyTaskCompletion(final T result);

}
