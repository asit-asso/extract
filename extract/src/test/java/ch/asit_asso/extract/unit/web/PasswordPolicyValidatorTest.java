package ch.asit_asso.extract.unit.web;

import ch.asit_asso.extract.web.constraints.PasswordPolicy;
import ch.asit_asso.extract.web.constraints.PasswordPolicyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordPolicyValidator Tests")
class PasswordPolicyValidatorTest {

    private PasswordPolicyValidator validator;

    @Mock
    private PasswordPolicy constraintAnnotation;

    @Mock
    private ConstraintValidatorContext context;

    private void setupDefaultConstraint() {
        lenient().when(constraintAnnotation.minLength()).thenReturn(8);
        lenient().when(constraintAnnotation.maxLength()).thenReturn(24);
        lenient().when(constraintAnnotation.uppercase()).thenReturn(true);
        lenient().when(constraintAnnotation.lowercase()).thenReturn(true);
        lenient().when(constraintAnnotation.digit()).thenReturn(true);
        lenient().when(constraintAnnotation.special()).thenReturn(true);
        lenient().when(constraintAnnotation.common()).thenReturn(true);
        lenient().when(constraintAnnotation.sequential()).thenReturn(true);
    }


    @Nested
    @DisplayName("Null Password Tests")
    class NullPasswordTests {

        @BeforeEach
        void setUp() {
            setupDefaultConstraint();
            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should return false for null password")
        void shouldReturnFalseForNullPassword(String password) {
            boolean result = validator.isValid(password, context);
            assertFalse(result);
        }
    }


    @Nested
    @DisplayName("Length Validation Tests")
    class LengthValidationTests {

        @Test
        @DisplayName("Should return false when password is too short")
        void shouldReturnFalseWhenPasswordTooShort() {
            lenient().when(constraintAnnotation.minLength()).thenReturn(8);
            lenient().when(constraintAnnotation.maxLength()).thenReturn(24);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(false);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);

            boolean result = validator.isValid("Short1!", context);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when password is too long")
        void shouldReturnFalseWhenPasswordTooLong() {
            when(constraintAnnotation.minLength()).thenReturn(8);
            when(constraintAnnotation.maxLength()).thenReturn(24);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(false);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);

            String longPassword = "a".repeat(25);
            boolean result = validator.isValid(longPassword, context);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true when password length is at minimum")
        void shouldReturnTrueWhenPasswordAtMinLength() {
            when(constraintAnnotation.minLength()).thenReturn(8);
            when(constraintAnnotation.maxLength()).thenReturn(24);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(false);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);

            String password = "abcdefgh"; // 8 chars
            boolean result = validator.isValid(password, context);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when password length is at maximum")
        void shouldReturnTrueWhenPasswordAtMaxLength() {
            when(constraintAnnotation.minLength()).thenReturn(8);
            when(constraintAnnotation.maxLength()).thenReturn(24);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(false);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);

            String password = "a".repeat(24);
            boolean result = validator.isValid(password, context);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false for empty password")
        void shouldReturnFalseForEmptyPassword() {
            lenient().when(constraintAnnotation.minLength()).thenReturn(8);
            lenient().when(constraintAnnotation.maxLength()).thenReturn(24);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(false);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);

            boolean result = validator.isValid("", context);

            assertFalse(result);
        }
    }


    @Nested
    @DisplayName("Uppercase Validation Tests")
    class UppercaseValidationTests {

        @BeforeEach
        void setUp() {
            lenient().when(constraintAnnotation.minLength()).thenReturn(0);
            lenient().when(constraintAnnotation.maxLength()).thenReturn(Integer.MAX_VALUE);
            when(constraintAnnotation.uppercase()).thenReturn(true);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(false);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);
        }

        @Test
        @DisplayName("Should return false when uppercase is required but missing")
        void shouldReturnFalseWhenUppercaseMissing() {
            boolean result = validator.isValid("lowercase123!", context);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true when uppercase is present")
        void shouldReturnTrueWhenUppercasePresent() {
            boolean result = validator.isValid("Uppercase", context);
            assertTrue(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Atest", "teSt", "tesT", "TEST"})
        @DisplayName("Should return true for various uppercase positions")
        void shouldReturnTrueForVariousUppercasePositions(String password) {
            boolean result = validator.isValid(password, context);
            assertTrue(result);
        }
    }


    @Nested
    @DisplayName("Lowercase Validation Tests")
    class LowercaseValidationTests {

        @BeforeEach
        void setUp() {
            lenient().when(constraintAnnotation.minLength()).thenReturn(0);
            lenient().when(constraintAnnotation.maxLength()).thenReturn(Integer.MAX_VALUE);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            when(constraintAnnotation.lowercase()).thenReturn(true);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(false);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);
        }

        @Test
        @DisplayName("Should return false when lowercase is required but missing")
        void shouldReturnFalseWhenLowercaseMissing() {
            boolean result = validator.isValid("UPPERCASE123!", context);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true when lowercase is present")
        void shouldReturnTrueWhenLowercasePresent() {
            boolean result = validator.isValid("lowercase", context);
            assertTrue(result);
        }
    }


    @Nested
    @DisplayName("Digit Validation Tests")
    class DigitValidationTests {

        @BeforeEach
        void setUp() {
            lenient().when(constraintAnnotation.minLength()).thenReturn(0);
            lenient().when(constraintAnnotation.maxLength()).thenReturn(Integer.MAX_VALUE);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            when(constraintAnnotation.digit()).thenReturn(true);
            lenient().when(constraintAnnotation.special()).thenReturn(false);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);
        }

        @Test
        @DisplayName("Should return false when digit is required but missing")
        void shouldReturnFalseWhenDigitMissing() {
            boolean result = validator.isValid("NoDigitsHere!", context);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true when digit is present")
        void shouldReturnTrueWhenDigitPresent() {
            boolean result = validator.isValid("password1", context);
            assertTrue(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0test", "test5", "te9st", "123"})
        @DisplayName("Should return true for various digit positions")
        void shouldReturnTrueForVariousDigitPositions(String password) {
            boolean result = validator.isValid(password, context);
            assertTrue(result);
        }
    }


    @Nested
    @DisplayName("Special Character Validation Tests")
    class SpecialCharacterValidationTests {

        @BeforeEach
        void setUp() {
            lenient().when(constraintAnnotation.minLength()).thenReturn(0);
            lenient().when(constraintAnnotation.maxLength()).thenReturn(Integer.MAX_VALUE);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            when(constraintAnnotation.special()).thenReturn(true);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);
        }

        @Test
        @DisplayName("Should return false when special character is required but missing")
        void shouldReturnFalseWhenSpecialCharMissing() {
            boolean result = validator.isValid("NoSpecialChars123", context);
            assertFalse(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"pass!", "pass@", "pass#", "pass$", "pass%", "pass^", "pass&", "pass*"})
        @DisplayName("Should return true for various special characters")
        void shouldReturnTrueForVariousSpecialCharacters(String password) {
            boolean result = validator.isValid(password, context);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when special character is present at beginning")
        void shouldReturnTrueWhenSpecialCharAtBeginning() {
            boolean result = validator.isValid("!password", context);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when special character is present at end")
        void shouldReturnTrueWhenSpecialCharAtEnd() {
            boolean result = validator.isValid("password!", context);
            assertTrue(result);
        }
    }


    @Nested
    @DisplayName("Common Password Validation Tests")
    class CommonPasswordValidationTests {

        @BeforeEach
        void setUp() {
            lenient().when(constraintAnnotation.minLength()).thenReturn(0);
            lenient().when(constraintAnnotation.maxLength()).thenReturn(Integer.MAX_VALUE);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(false);
            when(constraintAnnotation.common()).thenReturn(true);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);
        }

        @ParameterizedTest
        @ValueSource(strings = {"password", "123456", "qwerty", "admin", "letmein", "welcome", "extract"})
        @DisplayName("Should return false for common passwords")
        void shouldReturnFalseForCommonPasswords(String password) {
            boolean result = validator.isValid(password, context);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true for non-common password")
        void shouldReturnTrueForNonCommonPassword() {
            boolean result = validator.isValid("UniqueP@ssword", context);
            assertTrue(result);
        }
    }


    @Nested
    @DisplayName("Sequential Characters Validation Tests")
    class SequentialCharactersValidationTests {

        @BeforeEach
        void setUp() {
            lenient().when(constraintAnnotation.minLength()).thenReturn(0);
            lenient().when(constraintAnnotation.maxLength()).thenReturn(Integer.MAX_VALUE);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(false);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            when(constraintAnnotation.sequential()).thenReturn(true);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "xyz", "123", "789"})
        @DisplayName("Should return false for sequential characters")
        void shouldReturnFalseForSequentialCharacters(String password) {
            boolean result = validator.isValid(password, context);
            assertFalse(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"aa", "bb", "11", "!!"})
        @DisplayName("Should return false for repeated characters")
        void shouldReturnFalseForRepeatedCharacters(String password) {
            boolean result = validator.isValid(password, context);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true for password without sequences or repeats")
        void shouldReturnTrueForPasswordWithoutSequencesOrRepeats() {
            boolean result = validator.isValid("AcEgIk", context);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false for password with repeated characters")
        void shouldReturnFalseForPasswordWithRepeatedChars() {
            boolean result = validator.isValid("paassword", context);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for password with sequential characters in middle")
        void shouldReturnFalseForPasswordWithSequenceInMiddle() {
            boolean result = validator.isValid("pabcword", context);
            assertFalse(result);
        }
    }


    @Nested
    @DisplayName("Combined Policy Tests")
    class CombinedPolicyTests {

        @Test
        @DisplayName("Should return true for password meeting all requirements")
        void shouldReturnTrueForValidPassword() {
            setupDefaultConstraint();
            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);

            // Password with: uppercase, lowercase, digit, special char, no common, no sequential
            String validPassword = "SecureP@s5word";
            boolean result = validator.isValid(validPassword, context);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when all policies are disabled")
        void shouldReturnTrueWhenAllPoliciesDisabled() {
            when(constraintAnnotation.minLength()).thenReturn(0);
            when(constraintAnnotation.maxLength()).thenReturn(Integer.MAX_VALUE);
            when(constraintAnnotation.uppercase()).thenReturn(false);
            when(constraintAnnotation.lowercase()).thenReturn(false);
            when(constraintAnnotation.digit()).thenReturn(false);
            when(constraintAnnotation.special()).thenReturn(false);
            when(constraintAnnotation.common()).thenReturn(false);
            when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);

            boolean result = validator.isValid("anypassword", context);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should fail at first unmet requirement")
        void shouldFailAtFirstUnmetRequirement() {
            lenient().when(constraintAnnotation.minLength()).thenReturn(20);
            lenient().when(constraintAnnotation.maxLength()).thenReturn(30);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(true);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(true);
            lenient().when(constraintAnnotation.digit()).thenReturn(true);
            lenient().when(constraintAnnotation.special()).thenReturn(true);
            lenient().when(constraintAnnotation.common()).thenReturn(true);
            lenient().when(constraintAnnotation.sequential()).thenReturn(true);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);

            // This password fails length check first
            boolean result = validator.isValid("Short1!", context);

            assertFalse(result);
        }
    }


    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle password with only spaces")
        void shouldHandlePasswordWithOnlySpaces() {
            when(constraintAnnotation.minLength()).thenReturn(8);
            when(constraintAnnotation.maxLength()).thenReturn(24);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(true); // Space is not alphanumeric
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);

            String spaces = "        ";  // 8 spaces
            boolean result = validator.isValid(spaces, context);

            assertTrue(result);  // Spaces are special characters
        }

        @Test
        @DisplayName("Should handle password with unicode characters")
        void shouldHandlePasswordWithUnicodeCharacters() {
            lenient().when(constraintAnnotation.minLength()).thenReturn(0);
            lenient().when(constraintAnnotation.maxLength()).thenReturn(Integer.MAX_VALUE);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(true);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);

            // The SPECIAL_CHARACTER pattern is [^a-zA-Z0-9], which means
            // accented letters like 'e' (e with acute) ARE considered special
            // because they are NOT in the a-zA-Z range
            // Testing with an actual emoji character which is definitely not alphanumeric
            boolean result = validator.isValid("password\u263A", context);  // password + smiley face

            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle very long password within limits")
        void shouldHandleVeryLongPasswordWithinLimits() {
            when(constraintAnnotation.minLength()).thenReturn(0);
            when(constraintAnnotation.maxLength()).thenReturn(10000);
            lenient().when(constraintAnnotation.uppercase()).thenReturn(false);
            lenient().when(constraintAnnotation.lowercase()).thenReturn(false);
            lenient().when(constraintAnnotation.digit()).thenReturn(false);
            lenient().when(constraintAnnotation.special()).thenReturn(false);
            lenient().when(constraintAnnotation.common()).thenReturn(false);
            lenient().when(constraintAnnotation.sequential()).thenReturn(false);

            validator = new PasswordPolicyValidator();
            validator.initialize(constraintAnnotation);

            String longPassword = "a".repeat(9999);
            boolean result = validator.isValid(longPassword, context);

            assertTrue(result);
        }
    }
}
