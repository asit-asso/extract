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

import ch.asit_asso.extract.domain.Process;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;



/**
 * A link between the process data objects and the data source.
 *
 * @author fkr
 */
public interface ProcessesRepository extends PagingAndSortingRepository<Process, Integer> {

    Iterable<Process> findAllByOrderByName();



    /**
     * Obtains the e-mail addresses of the operators associated to a given process.
     *
     * @param processId the integer that identifies the process
     * @return an array containing the defined (i.e. not null) addresses
     */
    @Query(nativeQuery = true)
    String[] getProcessOperatorsAddresses(@Param("processId") int processId);



    /**
     * Obtains the numbers that identify the operators associated to a given process.
     *
     * @param processId the integer that identifies the process
     * @return an array containing the identifier of each operator
     */
    @Query(nativeQuery = true)
    int[] getProcessOperatorsIds(@Param("processId") int processId);

}
