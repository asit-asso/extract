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
package ch.asit_asso.extract.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import ch.asit_asso.extract.ldap.LdapUser;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;


/**
 * An account that interacts with the application.
 *
 * @author Florent Krin
 */
@Entity
@Table(name = "Users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "login", name = "UNQ_USER_LOGIN"),
    @UniqueConstraint(columnNames = "email", name = "UNQ_USER_EMAIL"),
    @UniqueConstraint(columnNames = "tokenpass", name = "UNQ_USER_TOKENPASS")
})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "User.getSystemUser",
            query = "SELECT u FROM User u WHERE u.login = " + "'" + User.SYSTEM_USER_LOGIN + "'"),
    @NamedQuery(name = "User.getSystemUserId",
            query = "SELECT u.id FROM User u WHERE u.login = " + "'" + User.SYSTEM_USER_LOGIN + "'"),
    @NamedQuery(name = "User.getActiveAdministratorsAddresses",
            query = "SELECT u.email FROM User u WHERE u.profile = ch.asit_asso.extract.domain.User$Profile.ADMIN"
            + " AND u.active = true AND u.email IS NOT NULL"),
    @NamedQuery(name = "User.findAllApplicationUsers",
            query = "SELECT u FROM User u WHERE u.login != " + "'" + User.SYSTEM_USER_LOGIN + "'"),
    @NamedQuery(name = "User.findAllActiveApplicationUsers",
            query = "SELECT u FROM User u WHERE u.login != " + "'" + User.SYSTEM_USER_LOGIN + "' and u.active = true"),
    @NamedQuery(name = "User.getUserAssociatedRequestsByStatusOrderByEndDate",
            query = "SELECT r FROM Request r WHERE (r.process IN (SELECT p FROM User u JOIN u.processesCollection p WHERE u.id = :userId)"
                    + " OR r.process IN (SELECT p FROM User u JOIN u.userGroupsCollection g JOIN g.processesCollection p WHERE u.id = :userId))"
                    + " AND r.status = :status ORDER BY r.endDate DESC"),
    @NamedQuery(name = "User.getUserAssociatedRequestsByStatusNot",
                query = "SELECT r FROM Request r WHERE (r.process IN (SELECT p FROM User u JOIN u.processesCollection p WHERE u.id = :userId)"
                        + " OR r.process IN (SELECT p FROM User u JOIN u.userGroupsCollection g JOIN g.processesCollection p WHERE u.id = :userId))"
                        + " AND r.status != :status")


})
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * The string that identifies the user associated to the application background tasks.
     */
    public static final String SYSTEM_USER_LOGIN = "system";

    /**
     * The number of minutes during which a password can be changed after the request.
     */
    public static final int TOKEN_VALIDITY_IN_MINUTES = 20;

    /**
     * The number that uniquely identifies this user.
     */
    @Id
    @Basic(optional = false)
    @GeneratedValue
    @NotNull
    @Column(name = "id_user")
    private Integer id;

    /**
     * The level of access granted to this user.
     */
    @Column(name = "profile")
    @Enumerated(EnumType.STRING)
    private Profile profile;

    /**
     * The name of this user.
     */
    @Size(max = 50)
    @Column(name = "name")
    private String name;

    /**
     * The string that this user uses to access the application.
     */
    @Size(max = 50)
    @Column(name = "login")
    private String login;

    /**
     * The string known only of this user (hopefully) used to access the application.
     */
    @Size(max = 160)
    @Column(name = "pass")
    private String password;

    /**
     * The address that messages for this user can be sent to.
     */
    @Size(max = 50)
    @Column(name = "email")
    private String email;

    /**
     * Whether this user can access the application.
     */
    @Column(name = "active")
    private boolean active;

    /**
     * Whether this user receives mail notifications.
     */
    @Column(name = "mailactive")
    private boolean mailActive;

    /**
     * A unique string that allows this user to change her password once.
     */
    @Size(max = 50)
    @Column(name = "tokenpass")
    private String passwordResetToken;

    /**
     * When the password reset token will stop being valid.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "tokenexpire")
    private Calendar tokenExpiration;

    @Column(name = "two_factor_forced")
    private boolean twoFactorForced;


    @Column(name = "two_factor_status")
    @Enumerated(EnumType.STRING)
    private TwoFactorStatus twoFactorStatus;

    @Size(max = 100)
    @Column(name = "two_factor_token")
    private String twoFactorToken;


    @Size(max = 100)
    @Column(name = "two_factor_standby_token")
    private String twoFactorStandbyToken;


    @Column(name = "user_type")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    /**
     * The locale preference for this user's interface language.
     */
    @Size(max = 10)
    @Column(name = "locale", length = 10)
    private String locale = "fr";


    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Collection<RecoveryCode> twoFactorRecoveryCodesCollection;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Collection<RememberMeToken> rememberMeTokensCollection;


    /**
     * The request processings that this user can operate.
     */
    @ManyToMany(mappedBy = "usersCollection")
    private Collection<Process> processesCollection;


    /**
     * The groups that this user is a member of.
     */
    @ManyToMany(mappedBy = "usersCollection")
    private Collection<UserGroup> userGroupsCollection;


    /**
     * The possible levels of access to this application.
     */
    public enum Profile {
        /**
         * The user is allowed to manage the application.
         */
        ADMIN,
        /**
         * The user can only interact with the data item requests matched with the processes that he is
         * associated to.
         */
        OPERATOR
    }


    public enum TwoFactorStatus {
        ACTIVE,
        INACTIVE,
        STANDBY
    }


    public enum UserType {
        LDAP,
        LOCAL
    }



    /**
     * Creates a new instance of this user.
     */
    public User() {
    }



    /**
     * Creates a new instance of this user.
     *
     * @param userId the number that identifies this user.
     */
    public User(final Integer userId) {
        this.id = userId;
    }



    /**
     * Obtains the number that uniquely identifies this user.
     *
     * @return the identifier
     */
    public Integer getId() {
        return this.id;
    }



    /**
     * Defines the number that uniquely identifies this user.
     *
     * @param userId the identifier
     */
    public void setId(final Integer userId) {
        this.id = userId;
    }



    /**
     * Obtains whether this user has been granted administrative privileges.
     *
     * @return <code>true</code> if this user is an administrator
     */
    public boolean isAdmin() {
        return (this.getProfile() == Profile.ADMIN);
    }



    /**
     * Obtains the level of privileges granted to this user.
     *
     * @return the profile
     */
    public Profile getProfile() {
        return this.profile;
    }



    /**
     * Defines the level of privileges granted to this user.
     *
     * @param userProfile the profile
     */
    public void setProfile(final Profile userProfile) {
        this.profile = userProfile;
    }



    /**
     * Obtains the string to display as the user identifier.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }



    /**
     * Defines the string to display as the user identifier.
     *
     * @param userName the name
     */
    public void setName(final String userName) {
        this.name = userName;
    }



    /**
     * Obtains the string that allows the user to access the application.
     *
     * @return the login
     */
    public String getLogin() {
        return this.login;
    }



    /**
     * Defines the string that allows the user to access the application.
     *
     * @param userLogin the login
     */
    public void setLogin(final String userLogin) {
        this.login = userLogin;
    }



    /**
     * Obtains the string (hopefully) know only of this user that allows him to guarantee his identity.
     *
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }



    /**
     * Defines the string (hopefully) know only of this user that allows him to guarantee his identity.
     *
     * @param pass the password
     */
    public void setPassword(final String pass) {
        this.password = pass;
    }



    /**
     * Obtains the address that messages for this user can be sent to.
     *
     * @return the e-mail address
     */
    public String getEmail() {
        return this.email;
    }



    /**
     * Defines the address that messages for this user can be sent to.
     *
     * @param emailAddress the e-mail address
     */
    public void setEmail(final String emailAddress) {
        this.email = emailAddress;
    }



    /**
     * Obtains whether this user can access the application.
     *
     * @return <code>true</code> if the user can log in
     */
    public boolean isActive() {
        return this.active;
    }



    /**
     * Defines whether this user can access the application.
     *
     * @param isActive <code>true</code> to allow this user to log in
     */
    public void setActive(final boolean isActive) {
        this.active = isActive;
    }



    /**
     * Obtains whether this user can receive mail notifications.
     *
     * @return <code>true</code> if the user can receive notifications
     */
    public boolean isMailActive() {
        return this.mailActive;
    }



    /**
     * Defines whether this user receive mail notifications.
     *
     * @param isActive <code>true</code> to allow this user to receive notifications
     */
    public void setMailActive(final boolean isActive) {
        this.mailActive = isActive;
    }



    /**
     * Obtains whether this user is the one used for background operations.
     *
     * @return <code>true</code> if this is the system user
     */
    public final boolean isSystemUser() {
        return User.SYSTEM_USER_LOGIN.equals(this.login);
    }



    /**
     * Obtains the string that allows this user to change his password.
     *
     * @return the password reset token
     */
    public String getPasswordResetToken() {
        return this.passwordResetToken;
    }



    /**
     * Defines the string that allows this user to change his password.
     *
     * @param token the token to send to be able to change the password
     */
    public void setPasswordResetToken(final String token) {
        this.passwordResetToken = token;
    }



    /**
     * Obtains when the password reset token will stop being valid.
     *
     * @return the token expiration timestamp
     */
    public Calendar getTokenExpiration() {
        return this.tokenExpiration;
    }



    /**
     * Defines when the password reset token must stop being accepted.
     *
     * @param expiration the token expiration timestamp
     */
    public void setTokenExpiration(final Calendar expiration) {
        this.tokenExpiration = expiration;
    }



    public boolean isTwoFactorForced() { return this.twoFactorForced; }



    public void setTwoFactorForced(final boolean isForced) { this.twoFactorForced = isForced; }



    public TwoFactorStatus getTwoFactorStatus() { return this.twoFactorStatus; }



    public void setTwoFactorStatus(final TwoFactorStatus status) { this.twoFactorStatus = status; }



    public String getTwoFactorToken() { return this.twoFactorToken; }



    public void setTwoFactorToken(String token) { this.twoFactorToken = token; }



    public String getTwoFactorStandbyToken() { return this.twoFactorStandbyToken; }



    public void setTwoFactorStandbyToken(String token) { this.twoFactorStandbyToken = token; }


    public UserType getUserType() {
        return this.userType;
    }


    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    /**
     * Obtains the locale preference for this user's interface language.
     *
     * @return the locale code (e.g., "fr", "de", "en")
     */
    public String getLocale() {
        return this.locale != null ? this.locale : "fr";
    }

    /**
     * Defines the locale preference for this user's interface language.
     *
     * @param locale the locale code (e.g., "fr", "de", "en")
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Removes the token that allows this user to change her password.
     *
     * @return <code>true</code> if the token has been successfully cleaned
     */
    public final boolean cleanPasswordResetToken() {
        boolean isModified = false;

        if (this.getPasswordResetToken() != null) {
            this.setPasswordResetToken(null);
            isModified = true;
        }

        if (this.getTokenExpiration() != null) {
            this.setTokenExpiration(null);
            isModified = true;
        }

        return isModified;
    }



    /**
     * Defines all the necessary info to allow this user to change her password with a token. Note that the
     * token cannot be <code>null</code>. To clear the password reset info please use the
     * {@link #cleanPasswordResetToken()} method instead.
     *
     * @param token the code to enter when resetting the password
     */
    public final void setPasswordResetInfo(final String token) {

        if (StringUtils.isBlank(token)) {
            throw new IllegalArgumentException("The token cannot be empty.");
        }

        Calendar expiration = new GregorianCalendar();
        expiration.add(Calendar.MINUTE, User.TOKEN_VALIDITY_IN_MINUTES);

        this.setPasswordResetToken(token);
        this.setTokenExpiration(expiration);
    }



    /**
     * Obtains the request processes that this user can operate on. This only includes the processes that are directly
     * attached to this user, and doesn't include those attached to a user group that this user is a member of.
     * To obtain all the processes that this user can operate on independently of how they've been set, please use
     * the method {@link #getDistinctProcesses()}.
     *
     * @return the collection of processes
     */
    @XmlTransient
    public Collection<Process> getProcessesCollection() {
        return this.processesCollection;
    }



    /**
     * Defines the request processing that this user can operate on.
     *
     * @param processes a collection that contains the processes operated by this user
     */
    public void setProcessesCollection(final Collection<Process> processes) {
        this.processesCollection = processes;
    }


    /**
     * Obtains the groups that this user is a member of
     *
     * @return a collection that contains the groups that this user is a member of
     */
    public Collection<UserGroup> getUserGroupsCollection() {
        return userGroupsCollection;
    }

    /**
     * Defines the groups that this user is a member of
     *
     * @param userGroupsCollection a collection that contains the groups that this user is a member of
     */
    public void setUserGroupsCollection(Collection<UserGroup> userGroupsCollection) {
        this.userGroupsCollection = userGroupsCollection;
    }



    public Collection<RecoveryCode> getTwoFactorRecoveryCodesCollection() {
        return this.twoFactorRecoveryCodesCollection;
    }



    public void setTwoFactorRecoveryCodesCollection(Collection<RecoveryCode> twoFactorRecoveryCodesCollection) {
        this.twoFactorRecoveryCodesCollection = twoFactorRecoveryCodesCollection;
    }



    public Collection<RememberMeToken> getRememberMeTokensCollection() {
        return this.rememberMeTokensCollection;
    }



    public void setRememberMeTokensCollection(Collection<RememberMeToken> tokensCollection) {
        this.rememberMeTokensCollection = tokensCollection;
    }



    /**
     * Obtains whether processes are explicitly associated to this user. Note that there might be processes that
     * this user can operate on implicity, for instance if she is an administrator or a member of a user group
     * associated to a process.
     *
     * @return <code>true</code> if at least one process is associated to this user
     */
    public final boolean isAssociatedToProcesses() {
        return this.processesCollection != null && !this.processesCollection.isEmpty();
    }


    /**
     * Obtains all the processes that this user can manage, including those defined through a group of users that this
     * user is a member of.
     *
     * @return a collection that contains all the processes that this user can manage, without duplicates
     */
    public final Collection<Process> getDistinctProcesses() {
        List<Process> processes = new ArrayList<>(this.getProcessesCollection());

        for (UserGroup userGroup : this.getUserGroupsCollection()) {

            for (Process groupProcess : userGroup.getProcessesCollection()) {

                if (processes.contains(groupProcess)) {
                    continue;
                }

                processes.add(groupProcess);
            }
        }

        return processes;
    }


    /**
     * Checks whether this user is the last remaining active user in a user group that manages a process.
     *
     * @return <code>true</code> if this user is the last active member of group attached to a process
     */
    public final boolean isLastActiveMemberOfProcessGroup() {

        if (this.getUserGroupsCollection().isEmpty()) {
            return false;
        }

        boolean hasOtherActiveMember = false;

        for (UserGroup group : this.getUserGroupsCollection()) {

            if (!group.isAssociatedToProcesses()) {
                continue;
            }

            if (group.getUsersCollection().size() == 1) {
                return true;
            }

            for (User groupUser : group.getUsersCollection()) {

                if (Objects.equals(groupUser.getId(), this.getId()) || !groupUser.isActive()) {
                    continue;
                }

                hasOtherActiveMember = true;
            }

            if (!hasOtherActiveMember) {
                return true;
            }
        }

        return false;
    }



    @Override
    public final int hashCode() {
        int hash = 0;
        hash += this.id.hashCode();

        return hash;
    }



    @Override
    public final boolean equals(final Object object) {

        if (!(object instanceof User other)) {
            return false;
        }

        return this.id.equals(other.id);
    }



    @Override
    public final String toString() {
        return String.format("ch.asit_asso.extract.User[ idUser=%d ]", this.id);
    }




    public static final User fromLdap(LdapUser ldapUser) {
        User domainUser = new User();
        domainUser.setLogin(ldapUser.getLogin());
        domainUser.setMailActive(false);
        domainUser.setTwoFactorForced(false);
        domainUser.setTwoFactorStatus(User.TwoFactorStatus.INACTIVE);
        domainUser.setUserType(User.UserType.LDAP);
        domainUser.setActive(ldapUser.isActive());
        domainUser.setEmail(ldapUser.getEmail());
        domainUser.setName(ldapUser.getName());
        domainUser.setProfile(ldapUser.getRole());

        return domainUser;
    }
}
