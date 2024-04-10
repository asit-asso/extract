package ch.asit_asso.extract.authentication.ldap;

import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.ldap.LdapPool;
import ch.asit_asso.extract.ldap.LdapSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.support.DefaultTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class ExtractLdapAuthenticationProvider implements AuthenticationProvider {
    private final Logger logger = LoggerFactory.getLogger(ExtractLdapAuthenticationProvider.class);

    private final ExtractLdapUserDetailsMapper userDetailsMapper;
    private final LdapSettings ldapSettings;

    public ExtractLdapAuthenticationProvider(@NotNull LdapSettings ldapSettings,
                                             @NotNull ExtractLdapUserDetailsMapper ldapUserDetailsMapper) {
        this.logger.debug("Instantiating the LDAP authentication provider.");
        this.ldapSettings = ldapSettings;
        this.userDetailsMapper = ldapUserDetailsMapper;
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
        LdapPool serversPool = LdapPool.fromLdapSettings(this.ldapSettings);
        Authentication ldapAuthentication = serversPool.authenticate(authentication, this.userDetailsMapper);

        if (ldapAuthentication == null) {
            this.logger.debug("All bindings returned a null result. Authentication unsuccessful.");
            throw new BadCredentialsException("Invalid user or password.");
        }

        return ldapAuthentication;
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
        this.logger.debug("Ldap TLS enabled.");
        DefaultTlsDirContextAuthenticationStrategy tls = new DefaultTlsDirContextAuthenticationStrategy();
        tls.setHostnameVerifier((hostname, session) -> true);
        //tls.setSslSocketFactory(new DummySSLSocketFactory());
        ldapContextSource.setAuthenticationStrategy(tls);
    }



    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
