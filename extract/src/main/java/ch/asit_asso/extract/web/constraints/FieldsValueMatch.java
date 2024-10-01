package ch.asit_asso.extract.web.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Checks if two fields have the same values. Adapted from
 *
 * @author Bruno Alves
 *
 * @see <a href="https://github.com/yiminyangguang520/spring-boot-tutorials">Spring Boot Tutorials</a>
 */
@Documented
@Constraint(validatedBy = FieldsValueMatchValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface FieldsValueMatch {

    String message() default "Fields values don't match!";

    String field();

    String fieldMatch();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}