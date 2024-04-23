package ch.asit_asso.extract.ldap;

import ch.asit_asso.extract.domain.User;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapUser {

    private final boolean active;
    private final String login;
    private final User.Profile role;
    private final String email;
    private final String name;

    private final Logger logger = LoggerFactory.getLogger(LdapUser.class);

    public LdapUser(String login, String name, String email, User.Profile role) {
        this(login, name, email, role, true);
    }

    public LdapUser(String login, String name, String email, User.Profile role, boolean active) {
        this.login = login;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public String getLogin() { return this.login; }

    public String getName() { return this.name; }

    public String getEmail() { return this.email; }

    public User.Profile getRole() { return this.role; }

    public boolean isActive() { return this.active; }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object that) {

        if (!(that instanceof LdapUser thatUser)) {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.login, thatUser.login);

        return builder.isEquals();
    }



    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(this.login);

        return builder.toHashCode();
    }
}
