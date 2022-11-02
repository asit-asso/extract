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

import java.util.Collection;
import java.util.List;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Request.Status;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;



/**
 * The link between the requests data objects and the database.
 *
 * @author Yves Grasset
 */
public interface RequestsRepository extends PagingAndSortingRepository<Request, Integer>,
        JpaSpecificationExecutor<Request> {

    /**
     * Fetches the requests that are at a given state of their processing.
     *
     * @param status the state of the requests to get
     * @return a list of the requests at the provided state
     */
    List<Request> findByStatus(Status status);



    /**
     * Fetches the requests that are at a given state of their processing.
     *
     * @param status    the state of the requests to get
     * @param processes the processes that the request must be associated with
     * @return a list of the requests at the provided state
     */
    List<Request> findByStatusAndProcessIn(Status status,
            Collection<Process> processes);



    /**
     * Fetches the requests that are <i>not</i> at a given state of their processing.
     *
     * @param status the state of the requests to ignore
     * @return a list of the requests not at the provided state
     */
    List<Request> findByStatusNot(Status status);



    /**
     * Fetches the requests imported through a given connector that are <i>not</i> at a given state of
     * their processing.
     *
     * @param connector the connector instance that imported the requests
     * @param status    the state of the requests to ignore
     * @return a list of the requests not at the provided state
     */
    List<Request> findByConnectorAndStatusNot(Connector connector, Status status);



    /**
     * Fetches the requests that are <i>not</i> at a given state of their processing.
     *
     * @param status    the state of the requests to ignore
     * @param processes the processes that the request must be associated with
     * @return a list of the requests at the provided state
     */
    List<Request> findByStatusNotAndProcessIn(Status status,
            Collection<Process> processes);

}
