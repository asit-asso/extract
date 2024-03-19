package ch.asit_asso.extract.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class ActiveDirectoryServer extends LdapServer {

    private static final Pattern ACTIVE_DIRECTORY_DOMAIN_PATTERN
            = Pattern.compile("^(?:((?!-)[a-z0-9-]+(?!-))\\.)+([a-z]{3,6})$", Pattern.CASE_INSENSITIVE);

    private static final int MAXIMUM_DOMAIN_BYTES = 64;

    private final String domain;

    private final Logger logger = LoggerFactory.getLogger(ActiveDirectoryServer.class);



    public ActiveDirectoryServer(@NotNull String serverInfo, @NotNull String domain, LdapSettings.EncryptionType encryption,
                                 String username, String password, LdapSettings settings) {

        super(serverInfo, ActiveDirectoryServer.buildBaseFromDomain(domain), encryption, username, password, settings);

        this.domain = domain;
    }



    @Override
    public Authentication authenticate(Authentication authentication, UserDetailsContextMapper userDetailsMapper) {
        this.logger.debug("Trying to authenticate against AD server \"{}\" with domain {}.", this.url, this.base);
        AbstractLdapAuthenticationProvider provider = this.getAuthenticationProvider(userDetailsMapper);

        try {
            return provider.authenticate(authentication);

        } catch (AuthenticationException authenticationException) {
            this.logger.info("Authentication to server {} ({}) failed with an error: {}",
                             this.url, this.domain, authenticationException.getMessage());
            return null;
        }
    }



    public static boolean isDomain(@NotNull String base) {
        return ActiveDirectoryServer.ACTIVE_DIRECTORY_DOMAIN_PATTERN.asMatchPredicate().test(base)
               && base.getBytes().length <= ActiveDirectoryServer.MAXIMUM_DOMAIN_BYTES;
    }



    private ActiveDirectoryLdapAuthenticationProvider getAuthenticationProvider(UserDetailsContextMapper userDetailsMapper) {
        this.logger.debug("Domain is {}", this.domain);
//        String[] domainParts = domain.split("\\.");
//        String baseDn = String.format("DC=%s", String.join(",DC=", domainParts));
        this.logger.debug("Base DN is {}", this.base);
        ActiveDirectoryLdapAuthenticationProvider provider
                = new ActiveDirectoryLdapAuthenticationProvider(this.domain, this.url, this.base);
        String searchFilter = String.format("(&(objectClass=%s)(%s={1}))", this.settings.getUserObjectClass(),
                                            this.settings.getLoginAttribute());
        this.logger.debug("Active Directory search filter is {}", searchFilter);
        provider.setSearchFilter(searchFilter);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUserDetailsContextMapper(userDetailsMapper);

        return provider;
    }



    private static String buildBaseFromDomain(@NotNull String domain) {

        if (!ActiveDirectoryServer.isDomain(domain)) {
            throw new IllegalArgumentException("The domain is not valid.");
        }

        return String.format("DC=%s", String.join(",DC=", ActiveDirectoryServer.getDomainParts(domain)));
    }



    private static String[] getDomainParts(@NotNull String domain) {
        Matcher domainMatcher = ActiveDirectoryServer.ACTIVE_DIRECTORY_DOMAIN_PATTERN.matcher(domain);

        if (!domainMatcher.matches() || domainMatcher.groupCount() < 1) {
            throw new IllegalArgumentException("The string is not a valid domain name.");
        }

        List<String> parts = new ArrayList<>();

        for (int groupIndex = 1; groupIndex <= domainMatcher.groupCount(); groupIndex++) {
            String group = domainMatcher.group(groupIndex);

            if (group == null) {
                continue;
            }

            parts.add(group);
        }

        return parts.toArray(String[]::new);
    }
}
