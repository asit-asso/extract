package ch.asit_asso.extract.ldap;

import java.util.Collection;
import java.util.HashSet;
import ch.asit_asso.extract.domain.User;

public class LdapUsersCollection extends HashSet<LdapUser> {

    public static LdapUsersCollection of(Collection<LdapUser> users) {
        LdapUsersCollection collection = new LdapUsersCollection();
        collection.addAll(users);

        return collection;
    }

    @Override
    public boolean add(LdapUser userToAdd) {

        if (userToAdd == null) {
            throw new IllegalArgumentException("The user to add cannot be null.");
        }

        if (!this.contains(userToAdd)) {
            return super.add(userToAdd);
        }

        if (userToAdd.getRole() == User.Profile.OPERATOR) {
            return false;
        }

        if (!this.removeIf(user -> user.equals(userToAdd) && user.getRole() == User.Profile.OPERATOR)) {
            return false;
        }

        return super.add(userToAdd);
    }

    @Override
    public boolean addAll(Collection<? extends LdapUser> usersToAdd) {
        boolean hasChanged = false;

        for (LdapUser newUser : usersToAdd) {

            if (newUser == null) {
                continue;
            }

            hasChanged |= this.add(newUser);
        }

        return hasChanged;
    }
}
