package ch.asit_asso.extract.web.validators;

import ch.asit_asso.extract.utils.CommonPasswords;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.Errors;

import java.util.regex.Pattern;

/**
 * The class implements a strong password validation
 *
 * @author Bruno Alves
 */
public class PasswordValidator {

    /**
     * Should we test for uppercase chars ?
     */
    private boolean uppercase = true;

    /**
     * Should we test for lowercase chars ?
     */
    private boolean lowercase = true;

    /**
     * Should we test for digits ?
     */
    private boolean digits = true;

    /**
     * Should we test for special chars ?
     */
    private boolean specialChars = true;

    /**
     * Should we check whether the password is too common
     */
    private boolean common = true;

    /**
     * Show we check for sequences of chars (eg. abc, 123)
     */
    private boolean sequential = true;

    /**
     * Shall we stop on first validation error ?
     */
    private boolean stopOnFirstError = false;

    /**
     * Minimum password length
     */
    private int minLength = 8;

    /**
     * Maximum password length
     */
    private int maxLength = 24;

    /**
     * Pattern tprivate boolean numbers = true;
    hat seeks for at least an uppercase
     */
    private static final Pattern UPPER_CASE = Pattern.compile("[A-Z]");

    /**
     * Pattern that seeks for at least a lowercase
     */
    private static final Pattern LOWER_CASE = Pattern.compile("[a-z]");

    /**
     * Pattern that seeks for at least a digit
     */
    private static final Pattern DIGIT = Pattern.compile("\\d");

    /**
     * Pattern that seeks for at least a special character
     */
    private static final Pattern SPECIAL_CHARACTER = Pattern.compile("[#_\\-$!@%&:]");

    private PasswordValidator() {}

    public static PasswordValidator create() {
        return new PasswordValidator();
    }

    public PasswordValidator withDigits(boolean digits) {
        this.digits = digits;
        return this;
    }

    public PasswordValidator withUppercase(boolean uppercase) {
        this.uppercase = uppercase;
        return this;
    }

    public PasswordValidator withLowerCase(boolean lowercase) {
        this.lowercase = lowercase;
        return this;
    }

    public PasswordValidator withSpecialChars(boolean specialChars) {
        this.specialChars = specialChars;
        return this;
    }

    public PasswordValidator withCommon(boolean common) {
        this.common = common;
        return this;
    }

    public PasswordValidator withSequential(boolean sequential) {
        this.sequential = sequential;
        return this;
    }

    public PasswordValidator withStopOnFirstError(boolean stopOnFirstError) {
        this.stopOnFirstError = stopOnFirstError;
        return this;
    }

    public PasswordValidator withLength(int minLength, int maxLength) {
        this.minLength = Math.max(4, Math.min(minLength, maxLength));
        this.maxLength = Math.min(24, Math.max(minLength, maxLength));
        return this;
    }

    public void validate(@NotNull Object target, @NotNull Errors errors) {
        String password = (String) target;

        if (password.length() < minLength || password.length() > maxLength) {
            errors.reject("setup.fields.password.size", new Object[]{minLength, maxLength},
                    "Le mot de passe doit avoir entre " + minLength + " et " + maxLength + " caractères");
            if (stopOnFirstError) return;
        }

        if (uppercase && !UPPER_CASE.matcher(password).find()) {
            errors.reject("setup.fields.password.uppercase", "Le mot de passe doit contenir au moins une lettre majuscule");
            if (stopOnFirstError) return;
        }

        if (lowercase && !LOWER_CASE.matcher(password).find()) {
            errors.reject("setup.fields.password.lowercase", "Le mot de passe doit contenir au moins une lettre minuscule");
            if (stopOnFirstError) return;
        }

        if (digits && !DIGIT.matcher(password).find()) {
            errors.reject("setup.fields.password.digit", "Le mot de passe doit contenir au moins un chiffre");
            if (stopOnFirstError) return;
        }

        if (specialChars && !SPECIAL_CHARACTER.matcher(password).find()) {
            errors.reject("setup.fields.password.special", "Le mot de passe doit contenir au moins un caractère spécial");
            if (stopOnFirstError) return;
        }

        if (common && CommonPasswords.isCommon(password)) {
            errors.reject("setup.fields.password.common", "Le mot de passe est trop commun");
            if (stopOnFirstError) return;
        }

        if (sequential && hasSequentialOrRepeatedChars(password)) {
            errors.reject("setup.fields.password.sequential", "Le mot de passe ne doit pas contenir de séquences ou de caractères répétés");
            if (stopOnFirstError) return;
        }
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

    public void validateField(String fieldName, String password, Errors errors) {

        if (password.length() < minLength || password.length() > maxLength) {
            errors.rejectValue(fieldName, "setup.fields.password.size", new Object[]{minLength, maxLength},
                    "Le mot de passe doit avoir entre " + minLength + " et " + maxLength + " caractères");
            if (stopOnFirstError) return;
        }

        if (uppercase && !UPPER_CASE.matcher(password).find()) {
            errors.rejectValue(fieldName, "setup.fields.password.uppercase", "Le mot de passe doit contenir au moins une lettre majuscule");
            if (stopOnFirstError) return;
        }

        if (lowercase && !LOWER_CASE.matcher(password).find()) {
            errors.rejectValue(fieldName, "setup.fields.password.lowercase", "Le mot de passe doit contenir au moins une lettre minuscule");
            if (stopOnFirstError) return;
        }

        if (digits && !DIGIT.matcher(password).find()) {
            errors.rejectValue(fieldName, "setup.fields.password.digit", "Le mot de passe doit contenir au moins un chiffre");
            if (stopOnFirstError) return;
        }

        if (specialChars && !SPECIAL_CHARACTER.matcher(password).find()) {
            errors.rejectValue(fieldName, "setup.fields.password.special", "Le mot de passe doit contenir au moins un caractère spécial");
            if (stopOnFirstError) return;
        }

        if (common && CommonPasswords.isCommon(password)) {
            errors.rejectValue(fieldName, "setup.fields.password.common", "Le mot de passe est trop commun");
            if (stopOnFirstError) return;
        }

        if (sequential && hasSequentialOrRepeatedChars(password)) {
            errors.rejectValue(fieldName, "setup.fields.password.sequential", "Le mot de passe ne doit pas contenir de séquences ou de caractères répétés");
            if (stopOnFirstError) return;
        }
    }
}
