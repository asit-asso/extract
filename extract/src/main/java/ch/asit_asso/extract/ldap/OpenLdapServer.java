package ch.asit_asso.extract.ldap;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class OpenLdapServer extends LdapServer {

    private final Logger logger = LoggerFactory.getLogger(OpenLdapServer.class);



    public OpenLdapServer(@NotNull String serverInfo, @NotNull String base, LdapSettings.EncryptionType encryption,
                          String username, String password, LdapSettings settings) {
        super(serverInfo, base, encryption, username, password, settings);
    }



    @Override
    public Authentication authenticate(Authentication authentication, UserDetailsContextMapper userDetailsMapper) {
        this.logger.debug("Trying to authenticate against LDAP server \"{}\" with base {}.", this.url, this.base);
        this.logger.debug("Trying LDAP binding authentication.");

        AbstractLdapAuthenticationProvider provider = this.getAuthenticationProvider(authentication, userDetailsMapper);

        try {
            return provider.authenticate(authentication);

        } catch (AuthenticationException authenticationException) {
            this.logger.info("Authentication to server {} ({}) failed with an error: {}",
                             this.url, this.base, authenticationException.getMessage());
            return null;
        }
    }



    private LdapAuthenticationProvider getAuthenticationProvider(Authentication authentication,
                                                                 UserDetailsContextMapper userDetailsMapper) {
        BindAuthenticator authenticator = this.getBindAuthenticator(this.base, this.url, authentication);
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator);
        provider.setUserDetailsContextMapper(userDetailsMapper);

        return provider;
    }




    @NotNull
    private BindAuthenticator getBindAuthenticator(String baseDn, String serverUrl, Authentication authentication) {
        BaseLdapPathContextSource contextSource = this.buildContextSource(baseDn, serverUrl, authentication);
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        String searchFilter = String.format("(%s={0})", this.settings.getLoginAttribute());
        String userPattern = String.format("%s={0}, ou=Users", this.settings.getLoginAttribute());
        this.logger.debug("Pattern DN utilisateur : {}", userPattern);
        authenticator.setUserDnPatterns(new String[] { userPattern });
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch("ou=users", searchFilter, contextSource);
        authenticator.setUserSearch(userSearch);
        authenticator.setUserAttributes(new String[]{
                "objectClass", this.settings.getLoginAttribute(), this.settings.getMailAttribute(),
                this.settings.getUserNameAttribute(), "memberOf"
        });

        return authenticator;
    }


    private BaseLdapPathContextSource buildContextSource(String baseDn, String serverUrl, Authentication authentication) {
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(List.of(serverUrl), baseDn);
        contextSource.setUserDn(String.format("%s=%s", this.settings.getLoginAttribute(), authentication.getPrincipal()));
        contextSource.setPassword(authentication.getCredentials().toString());
        contextSource.setDirObjectFactory(DefaultDirObjectFactory.class);
        contextSource.setCacheEnvironmentProperties(false);
        contextSource.setPooled(false);
        contextSource.setReferral("follow");

        try {
            contextSource.afterPropertiesSet();

        } catch (Exception exception) {
            this.logger.error("Failed to load the LDAP context source.", exception);
            throw exception;
        }

        return contextSource;
    }

}
