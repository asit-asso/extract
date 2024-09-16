package ch.asit_asso.extract.configuration;

import java.security.spec.KeySpec;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import ch.asit_asso.extract.authentication.DatabaseUserDetailsService;
import ch.asit_asso.extract.authentication.ExtractAuthenticationSuccessHandler;
import ch.asit_asso.extract.authentication.ldap.ExtractLdapAuthenticationProvider;
import ch.asit_asso.extract.authentication.ldap.ExtractLdapUserDetailsMapper;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorAuthentication;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.persistence.RememberMeTokenRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.utils.Secrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;



/**
 * Settings that define the access to the application.
 *
 * @author Yves Grasset
 */
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Value("${database.encryption.secret}")
    private String encryptionPassword;

    @Value("${database.encryption.salt}")
    private String encryptionSalt;

    @Value("application.external.url")
    private String applicationUrl;

    public SecurityConfiguration() {
    }


    /**
     * Sets which URLs are accessible to whom and which ones are used for authentication operations.
     *
     * @throws Exception if an error occurred during the configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, AuthenticationSuccessHandler successHandler,
                                           DaoAuthenticationProvider daoAuthenticationProvider,
                                           ExtractLdapAuthenticationProvider ldapAuthenticationProvider) throws Exception {
        this.logger.debug("Configuring the security of the application.");
        httpSecurity.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    new AntPathRequestMatcher("/setup", "GET"),
                    new AntPathRequestMatcher("/setup", "POST"),
                    new AntPathRequestMatcher("/css/**", "GET"),
                    new AntPathRequestMatcher("/images/**", "GET"),
                    new AntPathRequestMatcher("/lib/**", "GET"),
                    new AntPathRequestMatcher("/js/extract.js", "GET"),
                    new AntPathRequestMatcher("/lang/**", "GET"),
                    new AntPathRequestMatcher("/passwordReset/request", "GET"),
                    new AntPathRequestMatcher("/favicon.ico", "GET"),
                    new AntPathRequestMatcher("/extract_favicon*.png", "GET")
                ).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/2fa/authenticate"),
                                 new AntPathRequestMatcher("/2fa/recovery")).hasAuthority("CAN_AUTHENTICATE_2FA")
                .requestMatchers(new AntPathRequestMatcher("/2fa/register"),
                                 new AntPathRequestMatcher("/js/register2fa.js"),
                                 new AntPathRequestMatcher("/2fa/cancelRegistration"),
                                 new AntPathRequestMatcher("/2fa/confirm")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/passwordReset/request", "POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/passwordReset/reset")).hasAuthority("CAN_RESET_PASSWORD")
                .antMatchers(HttpMethod.GET,
                        "/",
                        "/getActiveConnectors",
                        "/getCurrentRequests",
                        "/getFinishedRequests",
                        "/getWorkingState",
                        "/js/**"
                ).hasAnyAuthority(Profile.OPERATOR.name(), Profile.ADMIN.name())
                .mvcMatchers("").hasAnyAuthority(Profile.OPERATOR.name(), Profile.ADMIN.name())
                .antMatchers(
                        "/requests/**",
                        "/users/**"
                ).hasAnyAuthority(Profile.OPERATOR.name(), Profile.ADMIN.name())
                .antMatchers(
                        "/**"
                ).hasAuthority(Profile.ADMIN.name())
            )
            .formLogin((form) -> form
                    .loginPage("/login")
                    .failureUrl("/login/error")
                    .permitAll()
                    .defaultSuccessUrl("/")
                    .successHandler(successHandler)
                    .loginProcessingUrl("/login")
            )
            .logout().logoutUrl("/login/disconnect").logoutSuccessUrl("/login/disconnect").permitAll()
            .and()
            .exceptionHandling((exceptions) -> exceptions
                    .accessDeniedPage("/forbidden")
            )
            .authenticationManager(new ProviderManager(List.of(daoAuthenticationProvider,
                                                               ldapAuthenticationProvider)));

        return httpSecurity.build();
    }



    @Bean
    public ExtractLdapUserDetailsMapper ldapUserDetailsMapper(LdapSettings ldapSettings, UsersRepository usersRepository)
    {
        return new ExtractLdapUserDetailsMapper(ldapSettings, usersRepository);
    }



    @Bean
    public ExtractLdapAuthenticationProvider ldapAuthenticationProvider(LdapSettings ldapSettings,
                                                             ExtractLdapUserDetailsMapper ldapUserDetailsMapper) {
        return new ExtractLdapAuthenticationProvider(ldapSettings, ldapUserDetailsMapper);
    }



    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService daoUserDetailsService,
                                                               PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(daoUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }



    /**
     * Creates the object that will attempt to fetch the data for a user concerned by an authentication
     * attempt.
     *
     * @return the service object
     */
    @Bean
    public UserDetailsService daoUserDetailsService(UsersRepository usersRepository) {
        return new DatabaseUserDetailsService(usersRepository);
    }



    /**
     * Creates the object that will hash passwords and match a raw password with a hash.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        this.logger.debug("Creating password encoder.");
        return new Pbkdf2PasswordEncoder();
    }



    /**
     * Creates the object that allows the page templates engine to interact with Spring Security.
     *
     * @return the dialect object
     */
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }



    @Bean
    AuthorizationManager<RequestAuthorizationContext> twoFactorAuthorizationManager() {
        return (authentication,
                context) -> new AuthorizationDecision(authentication.get() instanceof TwoFactorAuthentication);
    }



    @Bean
    AesBytesEncryptor encryptor() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(this.encryptionPassword.toCharArray(), this.encryptionSalt.getBytes(), 65536, 256);
        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        return new AesBytesEncryptor(key, KeyGenerators.secureRandom(12), AesBytesEncryptor.CipherAlgorithm.GCM);
    }

    @Bean
    AuthenticationSuccessHandler successHandler(Secrets secrets, Environment environment,
                                                RememberMeTokenRepository rememberMeTokenRepository,
                                                UsersRepository usersRepository) {
        return new ExtractAuthenticationSuccessHandler(secrets, rememberMeTokenRepository,
                                                       usersRepository, environment);
    }

    @Bean
    AuthenticationFailureHandler failureHandler() {
        return new SimpleUrlAuthenticationFailureHandler("/login/error");
    }

}
