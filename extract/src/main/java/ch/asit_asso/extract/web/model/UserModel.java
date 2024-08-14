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
package ch.asit_asso.extract.web.model;

import ch.asit_asso.extract.authentication.twofactor.TwoFactorApplication;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorService;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.domain.User.TwoFactorStatus;
import ch.asit_asso.extract.domain.User.UserType;
import ch.asit_asso.extract.utils.Secrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;



/**
 * The representation of a user for a view.
 *
 * @author Yves Grasset
 */
public class UserModel {

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


    private boolean twoFactorForced;


    private TwoFactorStatus twoFactorStatus;


    private String twoFactorToken;


    private String twoFactorStandbyToken;



    private UserType userType;



    /**
     * Creates an instance of this model for a new user.
     */
    public UserModel() {
        this.logger.debug("Instantiating a model for a new user.");
        this.beingCreated = true;
        this.active = false;
        this.profile = Profile.OPERATOR;
        this.twoFactorForced = false;
        this.twoFactorStatus = TwoFactorStatus.INACTIVE;
        this.userType = UserType.LOCAL;
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
     * Otherwise, you'll get the generic string defined by the {@link Secrets#getGenericPasswordString()} constant.
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


    public final boolean isTwoFactorForced() { return this.twoFactorForced; }


    public void setTwoFactorForced(final boolean isForced) {
        this.twoFactorForced = isForced;
    }

    public TwoFactorStatus getTwoFactorStatus() {
        return this.twoFactorStatus;
    }

    public void setTwoFactorStatus(final TwoFactorStatus status) {
        this.twoFactorStatus = (status != null) ? status : TwoFactorStatus.INACTIVE;
    }

    public String getTwoFactorToken() { return this.twoFactorToken; }

    public void setTwoFactorToken(String token) { this.twoFactorToken = token; }

    public String getTwoFactorStandbyToken() { return this.twoFactorStandbyToken; }

    public void setTwoFactorStandbyToken(String token) { this.twoFactorStandbyToken = token; }

    public UserType getUserType() { return this.userType; }

    public void setUserType(UserType userType) { this.userType = userType; }

//    public final TwoFactorStatus getNewStatusToSet(TwoFactorStatus originalStatus, TwoFactorStatus requestedStatus,
//                                                   boolean is2faForced) {
//        if (originalStatus == null) {
//            return (is2faForced) ? TwoFactorStatus.STANDBY : TwoFactorStatus.INACTIVE;
//        }
//
//        return switch (originalStatus) {
//
//            case ACTIVE -> {
//
//                    if (requestedStatus != TwoFactorStatus.INACTIVE || !is2faForced) {
//                        yield requestedStatus;
//                    }
//
//                    yield TwoFactorStatus.STANDBY;
//                }
//
//            case INACTIVE -> (requestedStatus == TwoFactorStatus.ACTIVE || is2faForced) ? TwoFactorStatus.STANDBY
//                                                                                        : TwoFactorStatus.INACTIVE;
//            case STANDBY -> (requestedStatus == TwoFactorStatus.INACTIVE && !is2faForced) ? TwoFactorStatus.INACTIVE
//                                                                                          : TwoFactorStatus.STANDBY;
//            default -> originalStatus;
//        };
//    }


//    public final void processTwoFactorChange(User originalStateUser, boolean isCurrentUserAdmin,
//                                             BytesEncryptor encryptor, TwoFactorService twoFactorService) {
//        TwoFactorStatus oldStatus = originalStateUser.getTwoFactorStatus();
//        TwoFactorStatus requestedStatus = this.getTwoFactorStatus();
//        boolean oldForcedState = originalStateUser.isTwoFactorForced();
//        boolean requestedForcedState = this.isTwoFactorForced();
//        boolean newForcedState = (isCurrentUserAdmin) ? requestedForcedState : oldForcedState;
//        TwoFactorStatus newStatus = getNewStatusToSet(oldStatus, requestedStatus, newForcedState);
//
//        this.logger.debug("Request to switch status from {} to {}. Granted status: {}.",
//                          (oldStatus != null) ? oldStatus.name() : "null", requestedStatus.name(), newStatus.name());
//        this.logger.debug("Request to switch forced state from {} to {}. Granted state: {}",
//                          oldForcedState, requestedForcedState, newForcedState);
//
//        TwoFactorApplication twoFactorApplication = new TwoFactorApplication(originalStateUser, encryptor,
//                                                                             twoFactorService);
//
//        if (newStatus == TwoFactorStatus.INACTIVE) {
//            twoFactorApplication.disable();
//
//        } else if (newStatus == TwoFactorStatus.STANDBY && oldStatus != TwoFactorStatus.STANDBY) {
//            twoFactorApplication.enable();
//        }
//
//        this.setTwoFactorToken(originalStateUser.getTwoFactorToken());
//        this.setTwoFactorStandbyToken(originalStateUser.getTwoFactorStandbyToken());
//        this.setTwoFactorForced(newForcedState);
//        this.setTwoFactorStatus(newStatus);
//    }

    /**
     * Makes a new data object for this user.
     *
     * @param secrets the secrets utility bean to use to hash the user's password
     * @return the created user data object
     */
    public final User createDomainObject(final Secrets secrets, final TwoFactorService twoFactorService) {

        if (secrets == null) {
            throw new IllegalArgumentException("The password utility bean cannot be null.");
        }

        User domainUser = new User();

        return this.updateDomainObject(domainUser, secrets, twoFactorService, false, true);
    }



    /**
     * Reports the modifications to this model to the user data object.
     *
     * @param domainUser         the data object for this user
     * @param secrets      the password utility bean to use to hash the user's password
     * @param isCurrentUser      <code>true</code> if the user being edited is the currently logged user
     * @param isCurrentUserAdmin <code>true</code> if the currently logged user has administrator privileges
     * @return the updated user data object
     */
    public final User updateDomainObject(final User domainUser, final Secrets secrets,
                                         TwoFactorService twoFactorService, boolean isCurrentUser,
                                         boolean isCurrentUserAdmin) {

        if (domainUser == null) {
            throw new IllegalArgumentException("The user domain object to update cannot be null.");
        }

        if (secrets == null) {
            throw new IllegalArgumentException("The password utility bean cannot be null.");
        }

        domainUser.setMailActive(this.isMailActive());


        if (this.isBeingCreated()){
            // User type is set only at creation (or via the migration tool)
            domainUser.setUserType(this.getUserType());

            // At creation 2FA status is always inactive (but is changed later if TwoFactorForced == true)
            domainUser.setTwoFactorStatus(TwoFactorStatus.INACTIVE);
        }

        //this.processTwoFactorChange(domainUser, isCurrentUserAdmin, encryptor, twoFactorService);
        //domainUser.setTwoFactorStatus(this.getTwoFactorStatus());

        if (isCurrentUserAdmin) {
            domainUser.setTwoFactorForced(this.isTwoFactorForced());

            if (this.isTwoFactorForced() && domainUser.getTwoFactorStatus() == TwoFactorStatus.INACTIVE) {
                TwoFactorApplication twoFactorApplication = new TwoFactorApplication(domainUser, secrets,
                                                                                     twoFactorService);
                twoFactorApplication.enable();
            }
        }

        this.logger.debug("The new forced status of the domain user is {}.", domainUser.isTwoFactorForced());

        if (domainUser.getUserType() == UserType.LOCAL) {
            domainUser.setEmail(this.getEmail());
            domainUser.setLogin(this.getLogin());
            domainUser.setName(this.getName());

            if (this.isPasswordDefined() && !this.isPasswordGenericString()) {
                domainUser.setPassword(secrets.hash(this.getPassword()));
            }

            if (!isCurrentUser) {
                domainUser.setActive(this.isActive());
                domainUser.setProfile(this.getProfile());
            }

        }

        return domainUser;
    }



    /**
     * Obtains whether the password of this user as defined in the model is the generic placeholder, meaning
     * that it has not been modified.
     *
     * @return <code>true</code> if the password has not been modified
     */
    private boolean isPasswordGenericString() {
        return Secrets.isGenericPasswordString(this.password);
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
        this.setPassword(Secrets.getGenericPasswordString());
        this.setPasswordConfirmation(Secrets.getGenericPasswordString());
        this.setProfile(domainUser.getProfile());
        this.setTwoFactorForced(domainUser.isTwoFactorForced());
        this.setTwoFactorStatus(domainUser.getTwoFactorStatus());
        this.setTwoFactorToken(domainUser.getTwoFactorToken());
        this.setTwoFactorStandbyToken(domainUser.getTwoFactorStandbyToken());
        this.setUserType(domainUser.getUserType());
    }

}
