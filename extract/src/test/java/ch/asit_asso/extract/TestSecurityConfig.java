package ch.asit_asso.extract;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration that allows MockMvc tests to bypass Spring Security restrictions.
 * This configuration is only loaded during tests with @ActiveProfiles("test").
 *
 * @author Bruno Alves
 */
@Configuration
@EnableWebSecurity
@Profile("test")
@Order(1)
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().permitAll()
            .and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable();

        return http.build();
    }
}
