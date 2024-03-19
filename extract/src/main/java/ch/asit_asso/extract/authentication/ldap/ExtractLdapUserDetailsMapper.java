package ch.asit_asso.extract.authentication.ldap;

import java.util.Collection;
import javax.naming.NamingException;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.authentication.ApplicationUser;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.ldap.LdapUser;
import ch.asit_asso.extract.ldap.LdapUserAttributesMapper;
import ch.asit_asso.extract.ldap.LdapUtils;
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

        this.logger.debug("Looking for existing application user {}", username);
        User domainUser = this.getApplicationUser(username, context);
        this.logger.debug("Updating existing LDAP user.");
        boolean isUpdated;

        try {
            isUpdated = this.updateDomainUserObjectFromContext(domainUser, context);

        } catch (NamingException namingException) {
            this.logger.error(String.format("An error occurred when the user information for %s were read from LDAP."
                                            + " This is likely a configuration error.", username), namingException);
            throw new BadCredentialsException("Could not read user information in LDAP.", namingException);
        }


        if (isUpdated) {

            if (!this.saveDomainUser(domainUser)) {
                throw new BadCredentialsException("Could not save updated user information.");
            }
        }

        return new ApplicationUser(domainUser);
    }



    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new UnsupportedOperationException("Context cannot be modified from the application.");
    }
//
//
//    private User.Profile getProfileFromContext(DirContextOperations userData, String username) {
//        List<Attribute> attributesList = ListUtils.castList(Attribute.class, Collections.list(userData.getAttributes().getAll()));
//        this.logger.debug("Attributes found : {}",
//                          String.join(", ", attributesList.stream().map(Attribute::getID).toList()));
//        String[] membershipsArray = userData.getStringAttributes("memberOf");
//
//        if (membershipsArray == null || membershipsArray.length == 0) {
//            this.logger.debug("User {} doesn't belong to any group.", username);
//            throw new BadCredentialsException(String.format("Could not map user %s to any application role.", username));
//        }
//
//        List<String> membershipsList = List.of(membershipsArray);
//        this.logger.debug("User is a member of the following groups:\n{}", String.join("\n", membershipsList));
//
//        if (membershipsList.contains(this.ldapSettings.getAdminsGroup())) {
//            return User.Profile.ADMIN;
//        }
//
//        if (membershipsList.contains(this.ldapSettings.getOperatorsGroup())) {
//            return User.Profile.OPERATOR;
//        }
//
//        this.logger.debug("All searches for an application role to map user {} to failed.", username);
//        throw new BadCredentialsException(String.format("Could not map user %s to any application role.", username));
//    }

    private User createDomainUserObject(DirContextOperations context) {
        this.logger.debug("Creating user account.");
        User domainUser = new User();
        domainUser.setLogin(context.getStringAttribute(this.ldapSettings.getLoginAttribute()));
        domainUser.setActive(true);
        domainUser.setMailActive(false);
        domainUser.setTwoFactorForced(false);
        domainUser.setTwoFactorStatus(User.TwoFactorStatus.INACTIVE);
        domainUser.setUserType(User.UserType.LDAP);

        return domainUser;
    }


    private User getApplicationUser(String username, DirContextOperations context) {
        User domainUser = this.usersRepository.findByLoginIgnoreCase(username);

        if (domainUser != null) {
            this.logger.debug("Checking existing LDAP user.");

            if (domainUser.getUserType() != User.UserType.LDAP) {
                this.logger.info("A non-LDAP account already exists with login {}. Access denied.", username);
                throw new BadCredentialsException(String.format("A non-LDAP account already exists with login %s.",
                                                                username));
            }

            if (!domainUser.isActive()) {
                this.logger.debug("User {} is inactive. Access denied.", username);
                throw new BadCredentialsException(String.format("User %s is inactive.", username));
            }

            return domainUser;

        }

        this.logger.debug("Creating domain user for {}", username);
        return this.createDomainUserObject(context);
    }



    private boolean saveDomainUser(User domainUser) {
        String username = domainUser.getLogin();

        try {
            domainUser = this.usersRepository.save(domainUser);

        } catch (Exception userException) {
            this.logger.error(String.format("An error occurred when domain user %s was saved.", username),
                              userException);
            domainUser = null;
        }


        if (domainUser == null) {
            this.logger.warn("Could not create update the domain user for {}", username);
            return false;
        }

        return true;
    }




    private boolean updateDomainUserObjectFromContext(User domainUser, DirContextOperations context) throws NamingException {
        this.logger.debug("Updating user account properties from LDAP.");
        LdapUserAttributesMapper attributesMapper = new LdapUserAttributesMapper(this.ldapSettings);
        LdapUser ldapUser = attributesMapper.mapFromAttributes(context.getAttributes());

        return LdapUtils.updateFromLdap(domainUser, ldapUser, this.usersRepository);

//
//        try {
//            this.logger.debug("Setting null password.");
//            domainUser.setPassword(null);
//
//            this.logger.debug("Setting name.");
//            domainUser.setName(context.getStringAttribute(this.ldapSettings.getUserNameAttribute()));
//
//            this.logger.debug("Setting e-mail.");
//            String contextEmail = context.getStringAttribute(this.ldapSettings.getMailAttribute());
//
//            this.logger.debug("Checking if e-mail is taken.");
//
//            if (EmailUtils.isAddressInUse(contextEmail, domainUser, this.usersRepository)) {
//
//                if (domainUser.getId() == null) {
//                    this.logger.info("Could not create LDAP user {} because another user uses the e-mail address {}.",
//                                     domainUser.getLogin(), contextEmail);
//                    throw new BadCredentialsException("Another user already uses the same e-mail address");
//                }
//
//                this.logger.warn(
//                        "Another account already uses the e-mail address {}. The address for account {} has not been updated.",
//                        contextEmail, domainUser.getLogin());
//
//            } else {
//                domainUser.setEmail(contextEmail);
//            }
//
//            this.logger.debug("Setting profile");
//            domainUser.setProfile(this.getProfileFromContext(context, domainUser.getLogin()));
//
//            return domainUser;
//
//        } catch (Exception exception) {
//            this.logger.error("An error occurred when the user properties were updated.", exception);
//            throw new BadCredentialsException("Login procedure failed.");
//        }
    }
}
