package ch.asit_asso.extract.integration;

import ch.asit_asso.extract.authentication.ApplicationUser;
import ch.asit_asso.extract.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockApplicationUserSecurityContextFactory
    implements WithSecurityContextFactory<WithMockApplicationUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockApplicationUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User mockUser = new User();
        mockUser.setId(annotation.userId());
        mockUser.setLogin(annotation.username());
        mockUser.setName("Test " + annotation.username());
        mockUser.setEmail(annotation.username() + "@test.com");
        mockUser.setProfile(User.Profile.valueOf(annotation.role()));
        mockUser.setActive(true);
        mockUser.setPassword("$2a$10$encrypted");
        mockUser.setUserType(User.UserType.LOCAL);

        ApplicationUser principal = new ApplicationUser(mockUser);

        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);

        return context;
    }
}