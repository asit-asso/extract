package ch.asit_asso.extract.authentication.ldap;

import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.DefaultTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

public class ExtractLdapAuthenticationProvider implements AuthenticationProvider {
    private final Logger logger = LoggerFactory.getLogger(ExtractLdapAuthenticationProvider.class);

    private final ExtractLdapUserDetailsMapper userDetailsMapper;
    private final LdapSettings ldapSettings;
    private final UsersRepository usersRepository;

    public ExtractLdapAuthenticationProvider(@NotNull LdapSettings ldapSettings,
                                             @NotNull ExtractLdapUserDetailsMapper ldapUserDetailsMapper,
                                             @NotNull UsersRepository usersRepository) {
        this.logger.debug("Instantiating the LDAP authentication provider.");
        this.ldapSettings = ldapSettings;
        this.userDetailsMapper = ldapUserDetailsMapper;
        this.usersRepository = usersRepository;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getPrincipal().toString();
        this.logger.debug("Trying to authenticate user {} against LDAP.", username);
        this.ldapSettings.refresh();

        if (!this.ldapSettings.isEnabled()) {
            this.logger.info("LDAP is disabled. LDAP authentication rejected");
            throw new BadCredentialsException("LDAP disabled.");
        }

        this.logger.debug("Attempting to bind authenticate with the various server URLs and base DN.");
        this.logger.debug("The servers are {}", String.join(";", this.ldapSettings.getServers()));

        String[] serverUrlsArray = this.ldapSettings.getServersUrls();
        this.logger.debug("The servers URLS are {}", String.join(";", serverUrlsArray));

        for (String serverUrl : serverUrlsArray) {

            for (String baseDn : this.ldapSettings.getBaseDn()) {
                Authentication ldapAuthentication = null;

                if (baseDn.matches("[=,]")) {
                    this.logger.debug("Trying to authenticate against LDAP server \"{}\" with base {}.", serverUrl, baseDn);
                    this.logger.warn("Non Active Directory LDAP authentication is not implemented yet.");
                    //            BindAuthenticator authenticator = this.getBindAuthenticator(baseDn, serverUrls, authentication);
                    ////            String userPattern = String.format("%s={0}, ou=Users", this.ldapSettings.getLoginAttribute());
                    ////            this.logger.debug("Pattern DN utilisateur : {}", userPattern);
                    ////            authenticator.setUserDnPatterns(new String[] { userPattern });
                    //            this.logger.debug("Trying LDAP binding authentication.");
                    //
                    //try {
                    //                DirContextOperations result = authenticator.authenticate(authentication);
                    //
                    //                if (result != null) {
                    //                    this.logger.debug("Non null result, binding successful: {}", result);
                    //                    return result;
                    //                }

                } else {
                    this.logger.debug("Base {} is interpreted as an Active Directory domain.", baseDn);
                    this.logger.debug("Trying to authenticate against AD server \"{}\" with domain {}.", serverUrl, baseDn);
                    ldapAuthentication = this.doActiveDirectoryAuthentication(authentication, serverUrl, baseDn);
                }
//
                if (ldapAuthentication == null || !ldapAuthentication.isAuthenticated()) {
                    this.logger.debug("Binding authentication failed.");
                    continue;
                }

                this.logger.debug("LDAP authentication successful.");
                return ldapAuthentication;

//                } catch (Exception exception) {
//                    this.logger.warn(String.format("LDAP binding authentication of user %s caused an error.",
//                                                   authentication.getPrincipal()),
//                                     exception);
//                }

            }
        }

        this.logger.debug("All bindings returned a null result. Authentication unsuccessful.");
        throw new BadCredentialsException("Invalid user or password.");
    }

    private Authentication doActiveDirectoryAuthentication(Authentication authentication, String url, String domain) {

        try {
            ActiveDirectoryLdapAuthenticationProvider provider = this.getActiveDirectoryAuthenticationProvider(domain, url);

            return provider.authenticate(authentication);

        } catch (AuthenticationException authenticationException) {
            this.logger.info("Authentication to server %s (%s) failed with an error: {}",
                             url, domain, authenticationException.getMessage());
            return null;
        }
    }

    private ActiveDirectoryLdapAuthenticationProvider getActiveDirectoryAuthenticationProvider(String domain, String url) {
        this.logger.debug("Domain is {}", domain);
        String[] domainParts = domain.split("\\.");
        String baseDn = String.format("DC=%s", String.join(",DC=", domainParts));
        this.logger.debug("Base DN is {}", baseDn);
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(
                domain, url, baseDn);
        String searchFilter = String.format("(&(objectClass=%s)(%s={1}))", ldapSettings.getUserObjectClass(),
                                            ldapSettings.getLoginAttribute());
        this.logger.debug("Active Directory search filter is {}", searchFilter);
        provider.setSearchFilter(searchFilter);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUserDetailsContextMapper(this.userDetailsMapper);

        return provider;
    }

    private void setSsl() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        X509TrustManager trustManager;

        //if (LdapGlobalConfig.SKIP_ALL_SSL_CERTS_CHECK.value(Boolean.class)) {
        trustManager = new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
//        } else {
//            tm = new X509TrustManager() {
//
//                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
//                }
//
//                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
//                }
//
//                public X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[0];
//                }
//            };
//        }

        sslContext.init(null, new TrustManager[]{trustManager}, null);
        SSLContext.setDefault(sslContext);
    }

    void setTls(LdapContextSource ldapContextSource) {
        // set tls
        logger.debug("Ldap TLS enabled.");
        DefaultTlsDirContextAuthenticationStrategy tls = new DefaultTlsDirContextAuthenticationStrategy();
        tls.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        //tls.setSslSocketFactory(new DummySSLSocketFactory());
        ldapContextSource.setAuthenticationStrategy(tls);
    }


    @NotNull
    private BindAuthenticator getBindAuthenticator(String baseDn, String[] serverUrls, Authentication authentication) {
        BaseLdapPathContextSource contextSource = this.buildContextSource(baseDn, serverUrls, authentication);
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        String searchFilter = String.format("(%s={0})", this.ldapSettings.getLoginAttribute());
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch("ou=users", searchFilter, contextSource);
        userSearch.setReturningAttributes(new String[]{
                "objectClass"
        });
        authenticator.setUserSearch(userSearch);
        return authenticator;
    }


    private BaseLdapPathContextSource buildContextSource(String baseDn, String[] serverUrls, Authentication authentication) {
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(List.of(serverUrls), baseDn);
        contextSource.setUserDn(String.format("%s=%s", this.ldapSettings.getLoginAttribute(), authentication.getPrincipal()));
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


    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
