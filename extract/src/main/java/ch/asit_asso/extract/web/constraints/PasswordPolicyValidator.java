package ch.asit_asso.extract.web.constraints;

import ch.asit_asso.extract.utils.CommonPasswords;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {

    private static final Pattern UPPER_CASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWER_CASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHARACTER = Pattern.compile("[^a-zA-Z0-9]");

    private PasswordPolicy constraint;

    @Override
    public void initialize(PasswordPolicy constraintAnnotation) {
        this.constraint = constraintAnnotation;
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        if (password.length() < constraint.minLength() || password.length() > constraint.maxLength()) {
            return false;
        }

        if (constraint.uppercase() && !UPPER_CASE.matcher(password).find()) {
            return false;
        }

        if (constraint.lowercase() && !LOWER_CASE.matcher(password).find()) {
            return false;
        }

        if (constraint.digit() && !DIGIT.matcher(password).find()) {
            return false;
        }

        if (constraint.special() && !SPECIAL_CHARACTER.matcher(password).find()) {
            return false;
        }

        if (constraint.common() && CommonPasswords.isCommon(password)) {
            return false;
        }

        if (constraint.sequential() && hasSequentialOrRepeatedChars(password)) {
            return false;
        }

        return true;
    }


    private boolean hasSequentialOrRepeatedChars(String password) {
        char[] chars = password.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == chars[i + 1]) {
                return true; // Caractère répété trouvé
            }

            if (i < chars.length - 2 && chars[i] == chars[i + 1] - 1 && chars[i] == chars[i + 2] - 2) {
                return true; // Séquence trouvée (ex. "abc", "123")
            }
        }
        return false;
    }
}
