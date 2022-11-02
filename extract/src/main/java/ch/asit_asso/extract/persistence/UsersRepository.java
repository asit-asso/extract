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
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.Profile;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;



/**
 * A link between the data objects that contain data about the application users and the data source.
 *
 * @author Yves Grasset
 */
public interface UsersRepository extends PagingAndSortingRepository<User, Integer> {

    /**
     * Obtains all the users that are allowed to log in.
     *
     * @return an array of users
     */
    User[] findAllActiveApplicationUsers();



    /**
     * Obtains all the users that are not system users.
     *
     * @return an array of users
     */
    User[] findAllApplicationUsers();



    /**
     * Obtains the user that is registered with a given e-mail address.
     *
     * @param email the e-mail address of the user
     * @return the user, or <code>null</code> if none was found
     */
    User findByEmailIgnoreCase(String email);



    /**
     * Obtains the active user that is registered with a given e-mail address.
     *
     * @param email the e-mail address of the user
     * @return the user, or <code>null</code> if none was found
     */
    User findByEmailIgnoreCaseAndActiveTrue(String email);



    /**
     * Obtains the active user that is registered with a given e-mail address, but only if its number
     * identifier is not a given one.
     *
     * @param email the e-mail address of the user
     * @param id    the number identifier the user must not have
     * @return the user, or <code>null</code> if none was found
     */
    User findByEmailIgnoreCaseAndIdNot(String email, Integer id);



    /**
     * Obtains a user based on the name used to log in.
     *
     * @param login the user login identifier
     * @return the user, or <code>null</code> if none was found
     */
    User findByLoginIgnoreCase(String login);



    /**
     * Obtains an active user based on the name used to log in.
     *
     * @param login the user login identifier
     * @return the user, or <code>null</code> if none was found
     */
    User findByLoginIgnoreCaseAndActiveTrue(String login);



    /**
     * Obtains a user based on the name used to log in, but only if its number identifier is not a given
     * one.
     *
     * @param login the login identifier of the user to find
     * @param id    the number identifier the user must not have
     * @return the user, or <code>null</code> if none matched the criteria
     */
    User findByLoginIgnoreCaseAndIdNot(String login, Integer id);



    /**
     * Obtains the active user that has been attributed a given token to reset her password.
     *
     * @param token the password reset token
     * @return the user, or <code>null</code> if none matched the criteria
     */
    User findByPasswordResetTokenAndActiveTrue(String token);



    /**
     * Obtains all the active users that have been granted a given role in the application.
     *
     * @param profile the role of the users to find
     * @return an array of users
     */
    User[] findByProfileAndActiveTrue(Profile profile);



    /**
     * Obtains the e-mail addresses of all the currently active administrator users.
     *
     * @return an array containing the defined (i.e. not null) addresses
     */
    String[] getActiveAdministratorsAddresses();



    /**
     * Obtains the user that system tasks are bound to.
     *
     * @return the system user
     */
    User getSystemUser();



    /**
     * Obtains the number identifying the user that system tasks are bound to.
     *
     * @return the number that identifies the system user
     */
    Integer getSystemUserId();



    /**
     * Obtains all the requests associated with a user that are in a given state.
     *
     * @param userId the number that identifies the user
     * @param status the state of the requests to get
     * @return a collection that contains the found requests
     */
    Collection<Request> getUserAssociatedRequestsByStatusOrderByEndDate(@Param("userId") int userId,
            @Param("status") Request.Status status);



    /**
     * Obtains all the requests associated with a user that are <i>not</i> in a given state.
     *
     * @param userId the number that identifies the user
     * @param status the state of the requests to ignore
     * @return a collection that contains the found requests
     */
    Collection<Request> getUserAssociatedRequestsByStatusNot(@Param("userId") int userId,
            @Param("status") Request.Status status);

}
