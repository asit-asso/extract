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

import ch.asit_asso.extract.domain.UserGroup;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;


/**
 * A link between the data objects that contain data about the groups of application users and the data source.
 *
 * @author Yves Grasset
 */
public interface UserGroupsRepository extends PagingAndSortingRepository<UserGroup, Integer> {

    Collection<UserGroup> findAllByOrderByName();


    /**
     * Obtains a user group based on its name.
     *
     * @param name the name identifier of the user group to find
     * @return the user group, or <code>null</code> if none matched the criteria
     */
    UserGroup findByNameIgnoreCase(String name);



    /**
     * Obtains a user group based on its name.
     *
     * @param name the name identifier of the user group to find
     * @param id    the number identifier the user must not have
     * @return the user group, or <code>null</code> if none matched the criteria
     */
    UserGroup findByNameIgnoreCaseAndIdNot(String name, Integer id);
}
