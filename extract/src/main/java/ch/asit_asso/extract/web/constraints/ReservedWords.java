package ch.asit_asso.extract.web.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to prevent users to insert reserved values in a field.
 *
 * @author Bruno Alves
 */
@Documented
@Constraint(validatedBy = ReservedWordsValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface ReservedWords {

    String message() default "Field contains a reserved word";

    String[] words() default {"system"};

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}