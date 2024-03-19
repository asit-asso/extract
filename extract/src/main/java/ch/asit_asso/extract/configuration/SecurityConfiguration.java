package ch.asit_asso.extract.configuration;

import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import ch.asit_asso.extract.authentication.DatabaseUserDetailsService;
import ch.asit_asso.extract.authentication.ExtractAuthenticationSuccessHandler;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorAuthentication;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorAuthenticationHandler;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorTrustResolver;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.persistence.RememberMeTokenRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.access.ExceptionTranslationFilter;
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
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RememberMeTokenRepository rememberMeTokenRepository;

    /**
     * The object that links the user data objects with the data source.
     */
    private final UsersRepository usersRepository;

    @Value("${database.encryption.secret}")
    private String encryptionPassword;

    @Value("${database.encryption.salt}")
    private String encryptionSalt;

    public SecurityConfiguration(RememberMeTokenRepository tokenRepository, UsersRepository usersRepository) {
        this.rememberMeTokenRepository = tokenRepository;
        this.usersRepository = usersRepository;
    }


    /**
     * Sets which URLs are accessible to whom and which ones are used for authentication operations.
     *
     * @param httpSecurity the Spring Security object that allows to configure the web part of the application
     *                     security
     * @throws Exception if an error occurred during the configuration
     */
    @Override
    protected final void configure(final HttpSecurity httpSecurity) throws Exception {

        this.logger.debug("Configuring the security of the application.");
        TwoFactorAuthenticationHandler twoFactorAuthenticationHandler = new TwoFactorAuthenticationHandler("/2fa/authenticate");
        httpSecurity.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
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
                                 new AntPathRequestMatcher("/2fa/confirm")).permitAll()
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
                    //.loginPage("/login")
                    .failureUrl("/login/error")
                    .permitAll()
                    .defaultSuccessUrl("/")
                    .successHandler(successHandler())
            )
            .logout().logoutUrl("/login/disconnect").logoutSuccessUrl("/login/disconnect").permitAll()
            .and()
            .exceptionHandling((exceptions) -> exceptions
                    .withObjectPostProcessor(new ObjectPostProcessor<ExceptionTranslationFilter>() {

                        @Override
                        public <O extends ExceptionTranslationFilter> O postProcess(O filter) {
                            filter.setAuthenticationTrustResolver(new TwoFactorTrustResolver());
                            return filter;
                        }
                    })
                    .accessDeniedPage("/forbidden")
            );

        //return httpSecurity.build();
    }



    /**
     * Sets how the authentication is managed at the application level.
     *
     * @param authenticationManagerBuilder the Spring Security builder object that allows to configure the
     *                                     authentication mechanism
     * @throws Exception if an error occurred during the configuration
     */
    @Override
    public final void configure(final AuthenticationManagerBuilder authenticationManagerBuilder)
            throws Exception {
        this.logger.debug("Configuring the user details service.");
        authenticationManagerBuilder.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }



    /**
     * Creates the object that will attempt to fetch the data for a user concerned by an authentication
     * attempt.
     *
     * @return the service object
     */
    @Override
    public final UserDetailsService userDetailsService() {
        return new DatabaseUserDetailsService(this.usersRepository);
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

    // for the second-factor
    @Bean
    AesBytesEncryptor encryptor() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(this.encryptionPassword.toCharArray(), this.encryptionSalt.getBytes(), 65536, 256);
        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        return new AesBytesEncryptor(key, KeyGenerators.secureRandom(12), AesBytesEncryptor.CipherAlgorithm.GCM);
    }

    @Bean
    AuthenticationSuccessHandler successHandler() {
        return new ExtractAuthenticationSuccessHandler(passwordEncoder(), this.rememberMeTokenRepository,
                                                       this.usersRepository);
    }

    @Bean
    AuthenticationFailureHandler failureHandler() {
        return new SimpleUrlAuthenticationFailureHandler("/login/error");
    }

}
