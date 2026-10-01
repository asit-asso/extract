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
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.utils.AlphabeticalOrder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;



/**
 * A link between the process data objects and the data source.
 *
 * @author fkr
 */
public interface ProcessesRepository extends PagingAndSortingRepository<Process, Integer> {

    /**
     * Fetches all the processes sorted by their name, ignoring the case and the accents.
     *
     * @return a list that contains the process data objects
     */
    default List<Process> findAllSortedByName() {
        return AlphabeticalOrder.sort(this.findAll(), Process::getName);
    }



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

    /**
     * Obtains the User objects for the operators associated to a given process.
     *
     * @param processId the integer that identifies the process
     * @return a list containing the User objects for each active operator with email notifications enabled
     */
    @Query("SELECT DISTINCT u FROM User u WHERE (u.id IN (SELECT pu.id FROM Process p JOIN p.usersCollection pu WHERE p.id = :processId) "
            + " OR u.id IN (SELECT uu.id FROM Process p JOIN p.userGroupsCollection ug JOIN ug.usersCollection uu WHERE p.id = :processId))"
            + " AND u.active = true AND u.mailActive = true")
    java.util.List<ch.asit_asso.extract.domain.User> getProcessOperators(@Param("processId") int processId);



    /**
     * Obtains the User objects for the watchers associated to a given process.
     *
     * @param processId the integer that identifies the process
     * @return a list containing the User objects for each active watcher with email notifications enabled
     */
    @Query("SELECT DISTINCT u FROM User u WHERE (u.id IN (SELECT pw.id FROM Process p JOIN p.watchersCollection pw WHERE p.id = :processId) "
            + " OR u.id IN (SELECT uw.id FROM Process p JOIN p.watcherGroupsCollection wg JOIN wg.usersCollection uw WHERE p.id = :processId))"
            + " AND u.active = true AND u.mailActive = true")
    java.util.List<ch.asit_asso.extract.domain.User> getProcessWatchers(@Param("processId") int processId);



    /**
     * Obtains the processes that a given user observes, either directly or through one of his user groups.
     *
     * @param userId the integer that identifies the user
     * @return a list containing the processes watched by the user
     */
    @Query("SELECT DISTINCT p FROM Process p WHERE p.id IN (SELECT p2.id FROM Process p2 JOIN p2.watchersCollection w WHERE w.id = :userId) "
            + " OR p.id IN (SELECT p3.id FROM Process p3 JOIN p3.watcherGroupsCollection wg JOIN wg.usersCollection gw WHERE gw.id = :userId)")
    List<Process> findWatchedProcessesByUser(@Param("userId") int userId);

}
