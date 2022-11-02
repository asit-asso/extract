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
package ch.asit_asso.extract.authentication;

import ch.asit_asso.extract.domain.User.Profile;
import org.springframework.security.core.GrantedAuthority;



/**
 * A level of access granted to a user of the application.
 *
 * @author Yves Grasset
 */
public class ApplicationUserRole implements GrantedAuthority {

    /**
     * The access level of the user.
     */
    private final Profile profile;



    /**
     * Creates a new permission instance.
     *
     * @param userProfile the profile of the user in the data source
     */
    public ApplicationUserRole(final Profile userProfile) {

        if (userProfile == null) {
            throw new IllegalArgumentException("The user profile cannot be null.");
        }

        this.profile = userProfile;
    }



    /**
     * Obtains the name of this level of access.
     *
     * @return the string that identifies this level of access
     */
    @Override
    public final String getAuthority() {
        return this.profile.name();
    }

}
