package ch.asit_asso.extract.unit.validators;

import ch.asit_asso.extract.web.validators.PasswordValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    @Test
    @DisplayName("Vérifie qu'un mot de passe valide ne génère pas d'erreurs")
    public void testValidPassword() {
        String validPassword = "Valid1Password!";
        Errors errors = new BindException(validPassword, "password");
        validator.validate(validPassword, errors);
        assertFalse(errors.hasErrors(), "Should not have errors for a valid password");
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe null génère une erreur avec le message approprié")
    public void testNullPassword() {
        String nullPassword = null;
        Errors errors = new BindException(nullPassword, "password");
        validator.validate(nullPassword, errors);
        assertTrue(errors.hasFieldErrors("password"), "Should have errors for null password");
        assertEquals("Le mot de passe est requis", errors.getFieldError("password").getDefaultMessage());
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe vide génère une erreur avec le message approprié")
    public void testEmptyPassword() {
        String emptyPassword = "";
        Errors errors = new BindException(emptyPassword, "password");
        validator.validate(emptyPassword, errors);
        assertTrue(errors.hasFieldErrors("password"), "Should have errors for empty password");
        assertEquals("Le mot de passe est requis", errors.getFieldError("password").getDefaultMessage());
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe trop court génère une erreur sur la longueur")
    public void testShortPassword() {
        String shortPassword = "Short1!";
        Errors errors = new BindException(shortPassword, "password");
        validator.validate(shortPassword, errors);
        assertTrue(errors.hasFieldErrors("password"), "Should have errors for short password");
        assertEquals("Le mot de passe doit avoir entre 8 et 128 caractères", errors.getFieldError("password").getDefaultMessage());
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe trop long génère une erreur sur la longueur")
    public void testLongPassword() {
        String longPassword = "ThisPasswordIsWayTooLong1234!";
        Errors errors = new BindException(longPassword, "password");
        validator.validate(longPassword, errors);
        assertTrue(errors.hasFieldErrors("password"), "Should have errors for long password");
        assertEquals("Le mot de passe doit avoir entre 8 et 128 caractères", errors.getFieldError("password").getDefaultMessage());
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe sans lettre majuscule génère une erreur")
    public void testMissingUpperCase() {
        String password = "missinguppercase1!";
        Errors errors = new BindException(password, "password");
        validator.validate(password, errors);
        assertTrue(errors.hasFieldErrors("password"), "Should have errors for missing uppercase letter");
        assertEquals("Le mot de passe doit contenir au moins une lettre majuscule", errors.getFieldError("password").getDefaultMessage());
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe sans lettre minuscule génère une erreur")
    public void testMissingLowerCase() {
        String password = "MISSINGLOWERCASE1!";
        Errors errors = new BindException(password, "password");
        validator.validate(password, errors);
        assertTrue(errors.hasFieldErrors("password"), "Should have errors for missing lowercase letter");
        assertEquals("Le mot de passe doit contenir au moins une lettre minuscule", errors.getFieldError("password").getDefaultMessage());
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe sans chiffre génère une erreur")
    public void testMissingDigit() {
        String password = "MissingDigit!";
        Errors errors = new BindException(password, "password");
        validator.validate(password, errors);
        assertTrue(errors.hasFieldErrors("password"), "Should have errors for missing digit");
        assertEquals("Le mot de passe doit contenir au moins un chiffre", errors.getFieldError("password").getDefaultMessage());
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe sans caractère spécial génère une erreur")
    public void testMissingSpecialCharacter() {
        String password = "MissingSpecial1";
        Errors errors = new BindException(password, "password");
        validator.validate(password, errors);
        assertTrue(errors.hasFieldErrors("password"), "Should have errors for missing special character");
        assertEquals("Le mot de passe doit contenir au moins un caractère spécial", errors.getFieldError("password").getDefaultMessage());
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe trop commun génère une erreur")
    public void testCommonPassword() {
        String commonPassword = "password";
        Errors errors = new BindException(commonPassword, "password");
        validator.validate(commonPassword, errors);
        assertTrue(errors.hasFieldErrors("password"), "Should have errors for common password");
        assertEquals("Le mot de passe est trop commun", errors.getFieldError("password").getDefaultMessage());
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe avec des caractères séquentiels génère une erreur")
    public void testSequentialChars() {
        String password = "abc123";
        Errors errors = new BindException(password, "password");
        validator.validate(password, errors);
        assertTrue(errors.hasFieldErrors("password"), "Should have errors for sequential characters");
        assertEquals("Le mot de passe ne doit pas contenir de séquences ou de caractères répétés", errors.getFieldError("password").getDefaultMessage());
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe avec des caractères répétés génère une erreur")
    public void testRepeatedChars() {
        String password = "aabbcc";
        Errors errors = new BindException(password, "password");
        validator.validate(password, errors);
        assertTrue(errors.hasFieldErrors("password"), "Should have errors for repeated characters");
        assertEquals("Le mot de passe ne doit pas contenir de séquences ou de caractères répétés", errors.getFieldError("password").getDefaultMessage());
    }
}