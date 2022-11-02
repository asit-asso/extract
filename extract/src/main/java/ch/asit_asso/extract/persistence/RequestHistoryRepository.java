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

import java.util.List;

import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import org.springframework.data.repository.PagingAndSortingRepository;



/**
 * The link between the request history entries data objects and the database.
 *
 * @author Yves Grasset
 */
public interface RequestHistoryRepository extends PagingAndSortingRepository<RequestHistoryRecord, Integer> {

    /**
     * Obtains all the history records related to a given record in execution order.
     *
     * @param request the request whose history must be fetched
     * @return a list of history records
     */
    List<RequestHistoryRecord> findByRequestOrderByStep(Request request);



    /**
     * Obtains all the history records related to a given record in reverse execution order.
     *
     * @param request the request whose history must be fetched
     * @return a list of history records
     */
    List<RequestHistoryRecord> findByRequestOrderByStepDesc(Request request);

}
