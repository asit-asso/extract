package ch.asit_asso.extract.integration;

import ch.asit_asso.extract.authentication.ApplicationUser;
import ch.asit_asso.extract.domain.User;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@TestConfiguration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        return username -> {
            if ("admin".equals(username)) {
                User mockUser = new User();
                mockUser.setId(1);
                mockUser.setLogin("admin");
                mockUser.setName("Test Admin");
                mockUser.setEmail("admin@test.com");
                mockUser.setProfile(User.Profile.ADMIN);
                mockUser.setActive(true);
                mockUser.setPassword("$2a$10$encrypted");
                mockUser.setUserType(User.UserType.LOCAL);

                return new ApplicationUser(mockUser);
            }
            throw new UsernameNotFoundException("User not found: " + username);
        };
    }
}