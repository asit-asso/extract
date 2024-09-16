package ch.asit_asso.extract.web.validators;

import ch.asit_asso.extract.utils.CommonPasswords;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.Errors;

import java.util.regex.Pattern;

/**
 * Classe implémentant un validateur avancé de mots de passe
 *
 * @author Bruno Alves
 */
public class PasswordValidator extends BaseValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    private static final Pattern UPPER_CASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWER_CASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHARACTER = Pattern.compile("[^a-zA-Z0-9]");

    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }

    @Override
    public void validate(Object target, @NotNull Errors errors) {
        String password = (String) target;

        if (password == null) {
            // le mot de passe a déjà été validé
            return;
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            errors.reject("setup.fields.password.size", new Object[]{MIN_LENGTH, MAX_LENGTH},
                    "Le mot de passe doit avoir entre " + MIN_LENGTH + " et " + MAX_LENGTH + " caractères");
            return;
        }

        if (!UPPER_CASE.matcher(password).find()) {
            errors.reject("setup.fields.password.uppercase", "Le mot de passe doit contenir au moins une lettre majuscule");
        }

        if (!LOWER_CASE.matcher(password).find()) {
            errors.reject("setup.fields.password.lowercase", "Le mot de passe doit contenir au moins une lettre minuscule");
        }

        if (!DIGIT.matcher(password).find()) {
            errors.reject("setup.fields.password.digit", "Le mot de passe doit contenir au moins un chiffre");
        }

        if (!SPECIAL_CHARACTER.matcher(password).find()) {
            errors.reject("setup.fields.password.special", "Le mot de passe doit contenir au moins un caractère spécial");
        }

        if (CommonPasswords.isCommon(password)) {
            errors.reject("setup.fields.password.common", "Le mot de passe est trop commun");
        }

        if (hasSequentialOrRepeatedChars(password)) {
            errors.reject("setup.fields.password.sequential", "Le mot de passe ne doit pas contenir de séquences ou de caractères répétés");
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

}
