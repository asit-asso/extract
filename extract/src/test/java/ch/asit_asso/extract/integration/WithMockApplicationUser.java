package ch.asit_asso.extract.integration;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@WithSecurityContext(factory = WithMockApplicationUserSecurityContextFactory.class)
public @interface WithMockApplicationUser {
    String username() default "admin";
    int userId() default 1;
    String role() default "ADMIN";
}