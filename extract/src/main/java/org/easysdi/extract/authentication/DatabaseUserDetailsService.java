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
package org.easysdi.extract.authentication;

import org.easysdi.extract.domain.User;
import org.easysdi.extract.persistence.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;



/**
 * An object that fetches data about a user of the application in the current database.
 *
 * @author Yves Grasset
 */
public class DatabaseUserDetailsService implements UserDetailsService {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The object that links the user data objects with the database.
     */
    private final UsersRepository usersRepository;



    /**
     * Creates a new instance of this service.
     *
     * @param repository a Spring Data repository that links the user data objects with the database
     */
    public DatabaseUserDetailsService(final UsersRepository repository) {

        if (repository == null) {
            throw new IllegalArgumentException("The users repository cannot be null.");
        }

        this.usersRepository = repository;
    }



    /**
     * Attempts to fetch information about a user that authenticated itself.
     *
     * @param login the string that identifies the user
     * @return the user details object
     * @throws UsernameNotFoundException if no active user matches the given login
     */
    @Transactional()
    @Override
    public final UserDetails loadUserByUsername(final String login) throws UsernameNotFoundException {
        this.logger.debug("Looking for an active user with login {}.", login);
        User domainUser = this.usersRepository.findByLoginIgnoreCaseAndActiveTrue(login);

        if (domainUser == null) {
            this.logger.debug("No active user found with login {}.", login);
            throw new UsernameNotFoundException("User not found.");
        }

        this.logger.debug("An active user with login {} has been found.", login);

        if (domainUser.cleanPasswordResetToken()) {
            domainUser = this.usersRepository.save(domainUser);
        }

        return new ApplicationUser(domainUser);
    }

}
