package org.easysdi.extract.configuration;

import org.easysdi.extract.authentication.DatabaseUserDetailsService;
import org.easysdi.extract.authentication.ExtractAuthenticationSuccessHandler;
import org.easysdi.extract.domain.User.Profile;
import org.easysdi.extract.persistence.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
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

    /**
     * The object that links the user data objects with the data source.
     */
    @Autowired
    private UsersRepository usersRepository;



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
        httpSecurity.authorizeRequests()
                .antMatchers(HttpMethod.GET,
                        "/css/**",
                        "/images/**",
                        "/lib/**",
                        "/js/extract.js",
                        "/lang/**",
                        "/passwordReset/request",
                        "/favicon.ico",
                        "/extract_favicon*.png"
                ).permitAll()
                .antMatchers("/error").permitAll()
                .antMatchers(HttpMethod.POST, "/passwordReset/request").permitAll()
                .antMatchers("/passwordReset/reset").hasAuthority("CAN_RESET_PASSWORD")
                .antMatchers(HttpMethod.GET,
                        "/",
                        "/getActiveConnectors",
                        "/getCurrentRequests",
                        "/getFinishedRequests",
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
                .and()
                .formLogin().loginPage("/login").failureUrl("/login/error").permitAll().defaultSuccessUrl("/")
                .successHandler(new ExtractAuthenticationSuccessHandler())
                .and()
                .logout().logoutUrl("/login/disconnect").logoutSuccessUrl("/login/disconnect").permitAll()
                .and()
                .exceptionHandling().accessDeniedPage("/forbidden");
    }



    /**
     * Sets how the authentication is managed at the application level.
     *
     * @param authenticationManagerBuilder the Spring Security builder object that allows to configure the
     *                                     authentication mechanism
     * @throws Exception if an error occurred during the configuration
     */
    @Autowired
    public final void configureGlobal(final AuthenticationManagerBuilder authenticationManagerBuilder)
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

}
