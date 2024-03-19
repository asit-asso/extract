package ch.asit_asso.extract.authentication.ldap;

import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.authentication.ApplicationUser;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class ExtractLdapUserDetailsMapper  implements UserDetailsContextMapper {
    private final LdapSettings ldapSettings;

    private final Logger logger = LoggerFactory.getLogger(ExtractLdapUserDetailsMapper.class);

    private final UsersRepository usersRepository;

    public ExtractLdapUserDetailsMapper(@NotNull LdapSettings ldapSettings, @NotNull UsersRepository usersRepository)
    {
        this.ldapSettings = ldapSettings;
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations context, String username,
                                          Collection<? extends GrantedAuthority> authorities) {

        User domainUser = this.usersRepository.findByLoginIgnoreCase(username);

        if (domainUser != null) {

            if (domainUser.getUserType() != User.UserType.LDAP) {
                this.logger.info("A non-LDAP account already exists with login {}. Access denied.", username);
                throw new BadCredentialsException(String.format("A non-LDAP account already exists with login %s.",
                                                                username));
            }

            if (!domainUser.isActive()) {
                this.logger.debug("User {} is inactive. Access denied.", username);
                throw new BadCredentialsException(String.format("User %s is inactive.", username));
            }

            this.updateDomainUserObjectFromContext(domainUser, context);

        } else {
            this.logger.debug("Creating domain user for {}", username);
            domainUser = this.createDomainUserObject(context);
        }


        try {
            domainUser = this.usersRepository.save(domainUser);

        } catch (Exception userException) {
            this.logger.error(String.format("An error occurred when domain user %s was saved.", username),
                              userException);
            domainUser = null;
        }


        if (domainUser == null) {
            this.logger.warn("Could not create update the domain user for {}", username);
            return null;
        }

        return new ApplicationUser(domainUser);
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new UnsupportedOperationException("Context cannot be modified from the application.");
    }


    private User.Profile getProfileFromContext(DirContextOperations userData, String username) {

        List<String> membershipsList = List.of(userData.getStringAttributes("memberOf"));
        this.logger.debug("User is a member of the following groups:\n{}", String.join("\n", membershipsList));

        if (membershipsList.contains(this.ldapSettings.getAdminsGroup())) {
            return User.Profile.ADMIN;
        }

        if (membershipsList.contains(this.ldapSettings.getOperatorsGroup())) {
            return User.Profile.OPERATOR;
        }

        this.logger.debug("All searches for an application role to map user {} to failed.", username);
        throw new BadCredentialsException(String.format("Could not map user %s to any application role.", username));
    }

    private User createDomainUserObject(DirContextOperations context) {
        User domainUser = new User();
        domainUser.setLogin(context.getStringAttribute(this.ldapSettings.getLoginAttribute()));
        domainUser.setActive(true);
        domainUser.setMailActive(false);
        domainUser.setTwoFactorForced(false);
        domainUser.setTwoFactorStatus(User.TwoFactorStatus.INACTIVE);
        domainUser.setUserType(User.UserType.LDAP);

        return this.updateDomainUserObjectFromContext(domainUser, context);
    }

    private User updateDomainUserObjectFromContext(User domainUser, DirContextOperations context) {
        domainUser.setPassword(null);
        domainUser.setName(context.getStringAttribute(this.ldapSettings.getUserNameAttribute()));

        String contextEmail = context.getStringAttribute(this.ldapSettings.getMailAttribute());
        User otherWithEmail = this.usersRepository.findByEmailIgnoreCaseAndIdNot(contextEmail, domainUser.getId());

        if (otherWithEmail != null) {

            if (domainUser.getId() == null) {
                this.logger.info("Could not create LDAP user {} because another user uses the e-mail address {}.",
                                 domainUser.getLogin(), contextEmail);
                throw new BadCredentialsException("Another user already uses the same e-mail address");
            }

            this.logger.warn("Another account already uses the e-mail address {}. The address for account {} has not been updated.",
                             contextEmail, domainUser.getLogin());

        } else {
            domainUser.setEmail(contextEmail);
        }

        domainUser.setProfile(this.getProfileFromContext(context, domainUser.getLogin()));

        return domainUser;
    }
}
