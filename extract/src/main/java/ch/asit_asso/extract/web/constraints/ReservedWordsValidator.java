package ch.asit_asso.extract.web.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * Validates fields annotated with @ReservedWords
 *
 * @author Bruno Alves
 */
public class ReservedWordsValidator implements ConstraintValidator<ReservedWords, String> {


    private ReservedWords constraint;

    @Override
    public void initialize(ReservedWords constraintAnnotation) {
        this.constraint = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String normalizedValue = value.toLowerCase();
        return !Arrays.asList(constraint.words()).contains(normalizedValue);
    }
}
