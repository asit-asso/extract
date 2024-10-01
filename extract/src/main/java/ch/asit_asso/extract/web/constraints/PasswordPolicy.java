package ch.asit_asso.extract.web.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Email.List;

/**
 * Used on a password field to enforce the password policy
 *
 * @author Bruno Alves
 */
@Documented
@Constraint(validatedBy = PasswordPolicyValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface PasswordPolicy {

    String message() default "{validation.password.policy}";

    int minLength() default 0;
    int maxLength() default Integer.MAX_VALUE;

    boolean uppercase() default true;

    boolean lowercase() default true;

    boolean digit() default true;

    boolean special() default true;

    boolean common() default true;

    boolean sequential() default true;

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}