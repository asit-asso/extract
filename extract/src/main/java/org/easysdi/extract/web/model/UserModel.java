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
package org.easysdi.extract.web.model;

import org.easysdi.extract.domain.User;
import org.easysdi.extract.domain.User.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;



/**
 * The representation of a user for a view.
 *
 * @author Yves Grasset
 */
public class UserModel {

    /**
     * The string used as a placeholder for the existing password.
     */
    private static final String PASSWORD_GENERIC_STRING = "*****";

    /**
     * Whether this user can use the application.
     */
    private boolean active;

    /**
     * Whether this user is a new one.
     */
    private boolean beingCreated;

    /**
     * The e-mail address of this user.
     */
    private String email;

    /**
     * The number that uniquely identifies this user in the application.
     */
    private Integer id;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(UserModel.class);

    /**
     * The string that identifies this user in the application.
     */
    private String login;

    private boolean mailActive;

    /**
     * The full name of this user.
     */
    private String name;

    /**
     * The new password to define for this user, or a generic string if it has been modified.
     */
    private String password;

    /**
     * The string entered to make sure that the new password has been correctly typed.
     */
    private String passwordConfirmation;

    /**
     * The access level granted to this user.
     */
    private Profile profile;



    /**
     * Creates an instance of this model for a new user.
     */
    public UserModel() {
        this.logger.debug("Instantiating a model for a new user.");
        this.beingCreated = true;
        this.active = false;
        this.profile = Profile.OPERATOR;
    }



    /**
     * Creates an instance of this model for an existing user.
     *
     * @param domainUser the data object about the user to represent.
     */
    public UserModel(final User domainUser) {

        if (domainUser == null) {
            throw new IllegalArgumentException("The domain object for the user cannot be null.");
        }

        this.logger.debug("Instantiating a model for existing user {}.", domainUser.getLogin());
        this.setPropertiesFromDomainObject(domainUser);
    }



    /**
     * Obtains whether this user is allowed to use the application.
     *
     * @return <code>true</code> if this user can log in
     */
    public final boolean isActive() {
        return this.active;
    }



    /**
     * Defines whether this user is allowed to use the application.
     *
     * @param isActive <code>true</code> to allow this user to log in
     */
    public final void setActive(final boolean isActive) {
        this.active = isActive;
    }



    /**
     * Obtains the address to which electronic messages for this users should be sent.
     *
     * @return this user's e-mail address
     */
    public final String getEmail() {
        return this.email;
    }



    /**
     * Defines the address to which electronic messages for this users should be sent.
     *
     * @param newAddress this user's e-mail address
     */
    public final void setEmail(final String newAddress) {
        this.email = newAddress;
    }



    /**
     * Obtains the number that uniquely identifies this user in the application.
     *
     * @return this user's identifying number
     */
    public final Integer getId() {
        return this.id;
    }



    /**
     * Defines the number that uniquely identifies this user in the application.
     *
     * @param newId the identifying number for this user
     */
    public final void setId(final Integer newId) {
        this.id = newId;
    }



    /**
     * Obtains the string used as an identifier for this user.
     *
     * @return this user's login
     */
    public final String getLogin() {
        return this.login;
    }



    /**
     * Defines the sting to use as an identifier for this user.
     *
     * @param newLogin the login for this user
     */
    public final void setLogin(final String newLogin) {
        this.login = newLogin;
    }



    public final boolean isMailActive() {
        return this.mailActive;
    }



    public final void setMailActive(final boolean isActive) {
        this.mailActive = isActive;
    }



    /**
     * Obtains the full name (first name and last name, for example) of this user.
     *
     * @return this user's full name
     */
    public final String getName() {
        return this.name;
    }



    /**
     * Defines the full name (first name and last name, for example) of this user.
     *
     * @param newName this user's full name
     */
    public final void setName(final String newName) {
        this.name = newName;
    }



    /**
     * Obtains the unhashed password for this user.
     * <p>
     * <b>Note:</b> You will only get the real password if a new one has just been defined to update it.
     * Otherwise, you'll get the get the generic string defined by the
     * {@link UserModel#PASSWORD_GENERIC_STRING} constant.
     *
     * @return the password
     */
    public final String getPassword() {
        return this.password;
    }



    /**
     * Defines the unhashed password for this user.
     *
     * @param newPassword the raw string to use as a password for this user
     */
    public final void setPassword(final String newPassword) {
        this.password = newPassword;
    }



    /**
     * Obtains the string entered to make the sure that the new password for this user has been correctly
     * typed.
     *
     * @return the password confirmation string
     */
    public final String getPasswordConfirmation() {
        return this.passwordConfirmation;
    }



    /**
     * Defines the string entered to make the sure that the new password for this user has been correctly
     * typed.
     *
     * @param newPasswordConfirmation the string entered to confirm the password
     */
    public final void setPasswordConfirmation(final String newPasswordConfirmation) {
        this.passwordConfirmation = newPasswordConfirmation;
    }



    /**
     * Obtains the role granted to this user.
     *
     * @return the user's profile
     */
    public final Profile getProfile() {
        return this.profile;
    }



    /**
     * Defines the role to grant to this user.
     *
     * @param userProfile this user's profile
     */
    public final void setProfile(final Profile userProfile) {
        this.profile = userProfile;
    }



    /**
     * Obtains whether this user has been granted administrative privileges.
     *
     * @return <code>true</code> if this user is an administrator
     */
    public final boolean isAdmin() {
        return (this.profile == Profile.ADMIN);
    }



    /**
     * Obtains whether this user is a new one.
     *
     * @return <code>true</code> if this user has not been persisted yet
     */
    public final boolean isBeingCreated() {
        return this.beingCreated;
    }



    /**
     * Defines whether this user is a new one.
     *
     * @param isNew <code>true</code> if this user has not been persisted yet
     */
    public final void setBeingCreated(final boolean isNew) {
        this.beingCreated = isNew;
    }



    /**
     * Obtains whether the password of this user is considered as set.
     *
     * @return <code>true</code> if the password is set
     */
    public final boolean isPasswordDefined() {
        return StringUtils.hasText(this.password);
    }



    /**
     * Obtains whether the password and its confirmation are the same.
     *
     * @return <code>true</code> if the confirmation matches the password or if the password has not been modified.
     *         <code>false</code> will also be returned if the password is not set.
     */
    public final boolean isPasswordMatch() {
        return this.isPasswordDefined()
                && ((this.isPasswordGenericString() && !this.beingCreated)
                || this.password.equals(this.passwordConfirmation));
    }



    /**
     * Obtains if the password for this user must be kept as it is defined in the data source.
     *
     * @return <code>true</code> if the password has not been modified
     */
    public final boolean isPasswordUnchanged() {
        return !this.beingCreated && this.isPasswordGenericString();
    }



    /**
     * Makes a new data object for this user.
     *
     * @param passwordEncoder the encoder to use to hash the user's password
     * @return the created user data object
     */
    public final User createDomainObject(final PasswordEncoder passwordEncoder) {

        if (passwordEncoder == null) {
            throw new IllegalArgumentException("The password encoder cannot be null.");
        }

        User domainUser = new User();

        return this.updateDomainObject(domainUser, passwordEncoder);
    }



    /**
     * Reports the modifications to this model to the user data object.
     *
     * @param domainUser      the data object for this user
     * @param passwordEncoder the encoder to use to hash the user's password
     * @return the updated user data object
     */
    public final User updateDomainObject(final User domainUser, final PasswordEncoder passwordEncoder) {

        if (domainUser == null) {
            throw new IllegalArgumentException("The user domain object to update cannot be null.");
        }

        if (passwordEncoder == null) {
            throw new IllegalArgumentException("The password encoder cannot be null.");
        }

        domainUser.setActive(this.isActive());
        domainUser.setMailActive(this.isMailActive());
        domainUser.setEmail(this.getEmail());
        domainUser.setLogin(this.getLogin());
        domainUser.setName(this.getName());

        if (this.isPasswordDefined() && !this.isPasswordGenericString()) {
            domainUser.setPassword(passwordEncoder.encode(this.getPassword()));
        }

        domainUser.setProfile(this.getProfile());

        return domainUser;
    }



    /**
     * Obtains whether the password of this user as defined in the model is the generic placeholder, meaning
     * that it has not been modified.
     *
     * @return <code>true</code> if the password has not been modified
     */
    private boolean isPasswordGenericString() {
        return UserModel.PASSWORD_GENERIC_STRING.equals(this.password);
    }



    /**
     * Defines the data in this model based on a user data object.
     *
     * @param domainUser the data object that contains the data to copy to this model.
     */
    private void setPropertiesFromDomainObject(final User domainUser) {
        assert domainUser != null : "The domain user object must be set.";

        this.setBeingCreated(false);
        this.setActive(domainUser.isActive());
        this.setMailActive(domainUser.isMailActive());
        this.setEmail(domainUser.getEmail());
        this.setId(domainUser.getId());
        this.setLogin(domainUser.getLogin());
        this.setName(domainUser.getName());
        this.setPassword(UserModel.PASSWORD_GENERIC_STRING);
        this.setPasswordConfirmation(UserModel.PASSWORD_GENERIC_STRING);
        this.setProfile(domainUser.getProfile());
    }

}
