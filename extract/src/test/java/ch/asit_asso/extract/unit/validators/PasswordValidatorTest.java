package ch.asit_asso.extract.unit.validators;

import ch.asit_asso.extract.web.validators.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.*;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    private PasswordValidator validator;
    private MapBindingResult errors;

    @BeforeEach
    public void setUp() {
        validator = PasswordValidator.create();
        errors = new MapBindingResult(new HashMap<>(), "password");
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe valide ne génère pas d'erreurs")
    public void testValidPassword() {
        String validPassword = "Valid1Pas$word!";
        validator.validate(validPassword, errors);
        assertFalse(errors.hasErrors(), "Should not have errors for a valid password");
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe vide génère une erreur avec le message approprié")
    public void testEmptyPassword() {
        String emptyPassword = "";
        validator.validate(emptyPassword, errors);
        assertTrue(errors.hasErrors(), "Should have errors for empty password");
        assertTrue(errors.getAllErrors().stream().anyMatch(objectError -> "Le mot de passe doit avoir entre 8 et 24 caractères".equals(objectError.getDefaultMessage())));
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe trop court génère une erreur sur la longueur")
    public void testShortPassword() {
        String shortPassword = "Short1!";
        validator.validate(shortPassword, errors);
        assertTrue(errors.hasErrors(), "Should have errors for short password");
        assertTrue(errors.getAllErrors().stream().anyMatch(objectError -> "Le mot de passe doit avoir entre 8 et 24 caractères".equals(objectError.getDefaultMessage())));
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe trop long génère une erreur sur la longueur")
    public void testLongPassword() {
        String longPassword = "ThisPasswordIsWayTooLong1234!";
        validator.validate(longPassword, errors);
        assertTrue(errors.hasErrors(), "Should have errors for long password");
        assertTrue(errors.getAllErrors().stream().anyMatch(objectError -> "Le mot de passe doit avoir entre 8 et 24 caractères".equals(objectError.getDefaultMessage())));
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe sans lettre majuscule génère une erreur")
    public void testMissingUpperCase() {
        String password = "missinguppercase1!";
        validator.validate(password, errors);
        assertTrue(errors.hasErrors(), "Should have errors for missing uppercase letter");
        assertTrue(errors.getAllErrors().stream().anyMatch(objectError -> "Le mot de passe doit contenir au moins une lettre majuscule".equals(objectError.getDefaultMessage())));
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe sans lettre minuscule génère une erreur")
    public void testMissingLowerCase() {
        String password = "MISSINGLOWERCASE1!";
        validator.validate(password, errors);
        assertTrue(errors.hasErrors(), "Should have errors for missing lowercase letter");
        assertTrue(errors.getAllErrors().stream().anyMatch(objectError -> "Le mot de passe doit contenir au moins une lettre minuscule".equals(objectError.getDefaultMessage())));
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe sans chiffre génère une erreur")
    public void testMissingDigit() {
        String password = "MissingDigit!";
        validator.validate(password, errors);
        assertTrue(errors.hasErrors(), "Should have errors for missing digit");
        assertTrue(errors.getAllErrors().stream().anyMatch(objectError -> "Le mot de passe doit contenir au moins un chiffre".equals(objectError.getDefaultMessage())));
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe sans caractère spécial génère une erreur")
    public void testMissingSpecialCharacter() {
        String password = "MissingSpecial1";
        validator.validate(password, errors);
        assertTrue(errors.hasErrors(), "Should have errors for missing special character");
        assertTrue(errors.getAllErrors().stream().anyMatch(objectError -> "Le mot de passe doit contenir au moins un caractère spécial".equals(objectError.getDefaultMessage())));
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe trop commun génère une erreur")
    public void testCommonPassword() {
        String commonPassword = "password";
        validator.validate(commonPassword, errors);
        assertTrue(errors.hasErrors(), "Should have errors for common password");
        assertTrue(errors.getAllErrors().stream().anyMatch(objectError -> "Le mot de passe est trop commun".equals(objectError.getDefaultMessage())));
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe avec des caractères séquentiels génère une erreur")
    public void testSequentialChars() {
        String password = "abcd1234";
        validator.validate(password, errors);
        assertTrue(errors.hasErrors(), "Should have errors for sequential characters");
        assertTrue(errors.getAllErrors().stream().anyMatch(objectError -> "Le mot de passe ne doit pas contenir de séquences ou de caractères répétés".equals(objectError.getDefaultMessage())));
    }

    @Test
    @DisplayName("Vérifie qu'un mot de passe avec des caractères répétés génère une erreur")
    public void testRepeatedChars() {
        String password = "aabbccdd";
        validator.validate(password, errors);
        assertTrue(errors.hasErrors(), "Should have errors for repeated characters");
        assertTrue(errors.getAllErrors().stream().anyMatch(objectError -> "Le mot de passe ne doit pas contenir de séquences ou de caractères répétés".equals(objectError.getDefaultMessage())));
    }

    // Tests spécifiques sur validateField()
    @Test
    public void testValidPasswordForSpecificField() {
        validator.validateField("password", "Valid1Pas$word!", errors);
        assertFalse(errors.hasErrors(), "Le mot de passe valide ne doit pas générer d'erreurs pour un champ spécifique");
    }

    @Test
    public void testFieldPasswordTooShort() {
        validator.withLength(8, 24);
        validator.validateField("password", "Short1!", errors);
        assertTrue(errors.hasErrors(), "Un mot de passe trop court dans un champ spécifique devrait générer une erreur");
    }

    @Test
    public void testFieldMissingUppercase() {
        validator.withUppercase(true);
        validator.validateField("password", "lowercase123!", errors);
        assertTrue(errors.hasErrors(), "Un mot de passe sans majuscule dans un champ spécifique devrait générer une erreur");
    }

    @Test
    public void testFieldMissingSpecialCharacter() {
        validator.withSpecialChars(true);
        validator.validateField("password", "NoSpecialChar123", errors);
        assertTrue(errors.hasErrors(), "Un mot de passe sans caractères spéciaux dans un champ spécifique devrait générer une erreur");
    }

    // Test de stopOnFirstError = true sur validateField()
    @Test
    public void testStopOnFirstErrorForField() {
        validator.withStopOnFirstError(true);
        validator.withUppercase(true).withDigits(true);
        validator.validateField("password", "lowercase!", errors);
        assertEquals(1, errors.getErrorCount(), "La validation dans un champ spécifique devrait s'arrêter à la première erreur");
    }

    // Test de stopOnFirstError = false sur validateField()
    @Test
    public void testDoNotStopOnFirstErrorForField() {
        validator.withStopOnFirstError(false);
        validator.withUppercase(true).withDigits(true);
        validator.validateField("password", "lowercase!", errors);
        assertTrue(errors.getErrorCount() > 1, "La validation dans un champ spécifique ne doit pas s'arrêter à la première erreur");
    }

    // Tests pour stopOnFirstError = true sur validate()
    @Test
    public void testStopOnFirstErrorForValidate() {
        validator.withStopOnFirstError(true);
        validator.withUppercase(true).withDigits(true);
        validator.validate("lowercase!", errors);
        assertEquals(1, errors.getErrorCount(), "La validation générale devrait s'arrêter à la première erreur");
    }

    // Tests pour stopOnFirstError = false sur validate()
    @Test
    public void testDoNotStopOnFirstErrorForValidate() {
        validator.withStopOnFirstError(false);
        validator.withUppercase(true).withDigits(true);
        validator.validate("lowercase!", errors);
        assertTrue(errors.getErrorCount() > 1, "La validation générale ne doit pas s'arrêter à la première erreur");
    }

    // Test des erreurs multiples sur validateField()
    @Test
    public void testFieldMultipleErrors() {
        validator.withUppercase(true).withDigits(true).withSpecialChars(true);
        validator.validateField("password", "lowercase", errors);
        assertTrue(errors.getErrorCount() > 1, "Le champ spécifique doit générer plusieurs erreurs si plusieurs conditions échouent");
    }
}