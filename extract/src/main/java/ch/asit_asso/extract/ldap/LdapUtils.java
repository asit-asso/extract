package ch.asit_asso.extract.ldap;

import java.util.Objects;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.utils.EmailUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LdapUtils {

    private static final Logger logger = LoggerFactory.getLogger(LdapUtils.class);

    private LdapUtils() { throw new UnsupportedOperationException("This class is not instantiable."); }



    // TODO Find a better place
    public static boolean updateFromLdap(@NotNull User domainUser, @NotNull LdapUser ldapUser,
                                         @NotNull UsersRepository usersRepository) {

        if (!Objects.equals(domainUser.getLogin(), ldapUser.getLogin())) {
            throw new IllegalArgumentException("The provided LDAP user and domain user do not match.");
        }

        boolean isModified = false;

        if (!ldapUser.getEmail().equalsIgnoreCase(domainUser.getEmail())) {

            if (EmailUtils.isAddressInUseByOtherUser(ldapUser.getEmail(), ldapUser.getLogin(), usersRepository)) {
                LdapUtils.logger.warn(
                        "User {} e-mail in LDAP is {}, but another user already uses it. The e-mail remains {}.",
                        ldapUser.getLogin(), ldapUser.getEmail(), domainUser.getEmail());
            } else {
                domainUser.setEmail(ldapUser.getEmail());
                isModified = true;
            }
        }

        if (!ldapUser.getName().equalsIgnoreCase(domainUser.getName())) {
            domainUser.setName(ldapUser.getName());
            isModified = true;
        }

        if (ldapUser.isActive() != domainUser.isActive()) {
            domainUser.setActive(ldapUser.isActive());
            isModified = true;
        }

        if (ldapUser.getRole() != domainUser.getProfile()) {
            domainUser.setProfile(ldapUser.getRole());
            isModified = true;
        }

        return isModified;
    }
}
