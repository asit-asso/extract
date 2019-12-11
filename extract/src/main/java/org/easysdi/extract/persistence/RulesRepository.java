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

import java.util.List;
import org.easysdi.extract.domain.Connector;
import org.easysdi.extract.domain.Rule;
import org.springframework.data.repository.PagingAndSortingRepository;



/**
 *
 * @author fkr
 */
public interface RulesRepository extends PagingAndSortingRepository<Rule, Integer> {

    /**
     * Obtains all the rules for a given connector by matching order.
     *
     * @param connector the connector whose rules must be fetched
     * @return a list of rules
     */
    List<Rule> findByConnectorOrderByPosition(Connector connector);



    /**
     * Obtains all the active rules for a given connector by matching order.
     *
     * @param connector the connector whose active rules must be fetched
     * @return a list of rules
     */
    List<Rule> findByConnectorAndActiveTrueOrderByPosition(Connector connector);

}
