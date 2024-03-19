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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import ch.asit_asso.extract.domain.RecoveryCode;
import org.apache.commons.lang3.StringUtils;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;



/**
 * Information about a person that is allowed to use the application.
 *
 * @author Yves Grasset
 */
public class ApplicationUser implements UserDetails {

    /**
     * Whether this user is allowed to log in.
     */
    private final boolean isActive;

    /**
     * The usual, human-friendly name of this user.
     */
    private final String fullName;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ApplicationUser.class);

    /**
     * The encrypted version of the string that allows this user to access the application.
     */
    private final String passwordHash;

    /**
     * The collection of permissions that have been granted to this user.
     */
    private final List<GrantedAuthority> rolesList;

    private final boolean twoFactorForced;

    private final User.TwoFactorStatus twoFactorStatus;

    private final String twoFactorActiveToken;

    private final String twoFactorStandbyToken;

    /**
     * The number that uniquely identifies this user in the application.
     */
    private final int userId;

    /**
     * The string that uniquely identifies this user in the application.
     */
    private final String userName;


    //private final Collection<RecoveryCode> recoveryCodes;



    /**
     * Creates a new instance of an application user based on a user data object.
     *
     * @param domainUser the user data object of the person to represent
     */
    public ApplicationUser(final User domainUser) {

        if (domainUser == null) {
            throw new IllegalArgumentException("The domain user cannot be null.");
        }

        this.logger.debug("Creating application user object for {}.", domainUser.getLogin());
        this.fullName = domainUser.getName();
        this.userId = domainUser.getId();
        this.userName = domainUser.getLogin();
        this.passwordHash = domainUser.getPassword();
        this.isActive = domainUser.isActive();
        this.twoFactorForced = domainUser.isTwoFactorForced();
        this.twoFactorStatus = domainUser.getTwoFactorStatus();
        this.twoFactorActiveToken = domainUser.getTwoFactorToken();
        this.twoFactorStandbyToken = domainUser.getTwoFactorStandbyToken();
        //this.recoveryCodes = domainUser.getTwoFactorRecoveryCodesCollection();
        this.rolesList = this.buildRolesList(domainUser);
    }



    /**
     * Obtains the permissions that have been granted to this user.
     *
     * @return a collection of authorities granted
     */
    @Override
    public final Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableCollection(this.rolesList);
    }



    /**
     * Obtains the encrypted version of the string that allows this user to access the application.
     *
     * @return the password hash string
     */
    @Override
    public final String getPassword() {
        return this.passwordHash;
    }



    /**
     * Obtains the usual, human-friendly name of this user.
     *
     * @return the full name
     */
    public final String getFullName() {
        return this.fullName;
    }



    /**
     * Obtains the number that uniquely identifies this user in the application.
     *
     * @return the user identifier
     */
    public final int getUserId() {
        return this.userId;
    }



    /**
     * Obtains the string that uniquely identifies this user in the application.
     *
     * @return the login name
     */
    @Override
    public final String getUsername() {
        return this.userName;
    }



    public final boolean isTwoFactorForced() { return this.twoFactorForced; }



    public final User.TwoFactorStatus getTwoFactorStatus() { return this.twoFactorStatus; }



    public String getTwoFactorActiveToken() { return this.twoFactorActiveToken; }



    //public Collection<RecoveryCode> getTwoFactorRecoveryCodes() { return this.recoveryCodes; }


    public String getTwoFactorStandbyToken() { return twoFactorStandbyToken; }

    /**
     * Checks if this user has been granted a given permission.
     *
     * @param authorityName the string that identifies the permission to check
     * @return <code>true</code> if the user has been granted the permission
     */
    public final boolean hasAuthority(final String authorityName) {

        if (StringUtils.isBlank(authorityName)) {
            throw new IllegalArgumentException("The authority to check cannot be empty.");
        }

        for (GrantedAuthority authority : this.getAuthorities()) {

            if (authorityName.equals(authority.getAuthority())) {
                return true;
            }
        }

        return false;
    }



    /**
     * Obtains whether the period of validity of the account for this user is over.
     *
     * @return <code>true</code> if the account is still valid
     */
    @Override
    public final boolean isAccountNonExpired() {
        return this.isActive;
    }



    /**
     * Obtains whether the authentification is disallowed for the account for this user.
     *
     * @return <code>true</code> if the account is allowed to log in
     */
    @Override
    public final boolean isAccountNonLocked() {
        return this.isActive;
    }



    /**
     * Determines whether the validity period for the password of this user is over.
     *
     * @return <code>true</code> if the password for this user is still valid
     */
    @Override
    public final boolean isCredentialsNonExpired() {
        return this.isActive;
    }



    /**
     * Determines if the account for this user has been turned off.
     *
     * @return <code>true</code> if the account for this user is operational
     */
    @Override
    public final boolean isEnabled() {
        return this.isActive;
    }



    /**
     * Constructs a collection of permissions granted to this user.
     *
     * @param domainUser the user data object that contains information about this user
     * @return a list of granted authorities
     */
    private List<GrantedAuthority> buildRolesList(final User domainUser) {
        assert domainUser != null : "The user cannot be null.";

        final String userLogin = domainUser.getLogin();
        this.logger.debug("Building the list of profiles for user {}.", userLogin);

        final List<GrantedAuthority> list = new ArrayList<>();
        final Profile userProfile = domainUser.getProfile();

        if (userProfile != null) {
            this.logger.debug("Profile {} was found for user {}.", userProfile.name(), userLogin);
            list.add(new ApplicationUserRole(userProfile));
        }

        if (domainUser.getTwoFactorStatus() == User.TwoFactorStatus.ACTIVE) {
            list.add(new SimpleGrantedAuthority("CAN_AUTHENTICATE_2FA"));
        } else if (domainUser.isTwoFactorForced() || domainUser.getTwoFactorStatus() == User.TwoFactorStatus.STANDBY) {
            list.add(new SimpleGrantedAuthority("CAN_REGISTER_2FA"));
        }

        if (list.isEmpty()) {
            this.logger.warn("No profile found for user {}.", userLogin);
        }

        return list;
    }
}
