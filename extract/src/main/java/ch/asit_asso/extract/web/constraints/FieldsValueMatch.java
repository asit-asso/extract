package ch.asit_asso.extract.web.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The string has to be a well-formed email address. Exact semantics of what makes up a valid
 * email address are left to Jakarta Bean Validation providers. Accepts {@code CharSequence}.
 * <p>
 * {@code null} elements are considered valid.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 *
 * @since 2.0
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