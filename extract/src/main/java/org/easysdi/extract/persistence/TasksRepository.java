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
package org.easysdi.extract.persistence;

import org.easysdi.extract.domain.Task;
import org.springframework.data.repository.PagingAndSortingRepository;



/**
 * Link between the process tasks data objects and the database.
 *
 * @author Yves Grasset
 */
public interface TasksRepository extends PagingAndSortingRepository<Task, Integer> {

    /**
     * Fetches all the tasks that compose a given process.
     *
     * @param process the process whose tasks must be fetched
     * @return an array of tasks
     */
    Task[] findByProcessOrderByPosition(org.easysdi.extract.domain.Process process);

}
