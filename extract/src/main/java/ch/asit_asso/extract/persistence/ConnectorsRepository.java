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
import ch.asit_asso.extract.domain.Connector;
import org.springframework.data.repository.PagingAndSortingRepository;



/**
 *
 * @author Yves Grasset
 */
public interface ConnectorsRepository extends PagingAndSortingRepository<Connector, Integer> {

    /**
     * Fetches all the connectors that are currently active and sort them by their name.
     *
     * @return a list that contain the connector data objects
     */
    Iterable<Connector> findAllByOrderByName();



    /**
     * Fetches all the connectors that are currently active.
     *
     * @return a list that contain the connector data objects
     */
    List<Connector> findByActiveTrue();



    /**
     * Fetches all the connectors that are currently active and sort them by their name.
     *
     * @return a list that contain the connector data objects
     */
    List<Connector> findByActiveTrueOrderByName();

}
