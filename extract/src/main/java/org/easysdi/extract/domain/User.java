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
package org.easysdi.extract.domain;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
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
            query = "SELECT u.email FROM User u WHERE u.profile = org.easysdi.extract.domain.User$Profile.ADMIN"
            + " AND u.active = true AND u.email IS NOT NULL"),
    @NamedQuery(name = "User.findAllApplicationUsers",
            query = "SELECT u FROM User u WHERE u.login != " + "'" + User.SYSTEM_USER_LOGIN + "'"),
    @NamedQuery(name = "User.findAllActiveApplicationUsers",
            query = "SELECT u FROM User u WHERE u.login != " + "'" + User.SYSTEM_USER_LOGIN + "' and u.active = true"),
    @NamedQuery(name = "User.getUserAssociatedRequestsByStatusOrderByEndDate",
            query = "SELECT r FROM User u JOIN u.processesCollection p JOIN p.requestsCollection r WHERE u.id = :userId"
            + " AND r.status = :status ORDER BY r.endDate DESC"),
    @NamedQuery(name = "User.getUserAssociatedRequestsByStatusNot",
            query = "SELECT r FROM User u JOIN u.processesCollection p JOIN p.requestsCollection r WHERE u.id = :userId"
            + " AND r.status != :status")

})
public class User implements Serializable {

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
    @Column(name = "tokenpass", nullable = true)
    private String passwordResetToken;

    /**
     * When the password reset token will stop being valid.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "tokenexpire")
    private Calendar tokenExpiration;

    /**
     * The request processings that this user can operate.
     */
    @ManyToMany(mappedBy = "usersCollection")
    private Collection<Process> processesCollection;



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
     * Obtains the request processes that this user can operate on.
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
     * Obtains whether processes are explicitly associated to this user. Note that there might be processes that
     * this user can operate on implicity, for instance if she is an administrator.
     *
     * @return <code>true</code> if at least one process is associated to this user
     */
    public final boolean isAssociatedToProcesses() {
        return this.processesCollection != null && this.processesCollection.size() > 0;
    }



    @Override
    public final int hashCode() {
        int hash = 0;
        hash += this.id.hashCode();

        return hash;
    }



    @Override
    public final boolean equals(final Object object) {

        if (object == null || !(object instanceof User)) {
            return false;
        }

        User other = (User) object;

        return this.id.equals(other.id);
    }



    @Override
    public final String toString() {
        return String.format("org.easysdi.extract.User[ idUser=%d ]", this.id);
    }

}
