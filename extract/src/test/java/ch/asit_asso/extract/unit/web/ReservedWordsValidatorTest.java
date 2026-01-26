package ch.asit_asso.extract.unit.web;

import ch.asit_asso.extract.web.constraints.ReservedWords;
import ch.asit_asso.extract.web.constraints.ReservedWordsValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservedWordsValidator Tests")
class ReservedWordsValidatorTest {

    private ReservedWordsValidator validator;

    @Mock
    private ReservedWords constraintAnnotation;

    @Mock
    private ConstraintValidatorContext context;


    @Nested
    @DisplayName("Default Reserved Words Tests")
    class DefaultReservedWordsTests {

        @BeforeEach
        void setUp() {
            when(constraintAnnotation.words()).thenReturn(new String[]{"system"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);
        }

        @Test
        @DisplayName("Should return false for default reserved word 'system'")
        void shouldReturnFalseForDefaultReservedWord() {
            boolean result = validator.isValid("system", context);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for 'SYSTEM' (case insensitive)")
        void shouldReturnFalseForUppercaseSystem() {
            boolean result = validator.isValid("SYSTEM", context);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for 'System' (mixed case)")
        void shouldReturnFalseForMixedCaseSystem() {
            boolean result = validator.isValid("System", context);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true for non-reserved word")
        void shouldReturnTrueForNonReservedWord() {
            boolean result = validator.isValid("user", context);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true for word containing reserved word")
        void shouldReturnTrueForWordContainingReservedWord() {
            // "systems" contains "system" but is not equal to it
            boolean result = validator.isValid("systems", context);
            assertTrue(result);
        }
    }


    @Nested
    @DisplayName("Custom Reserved Words Tests")
    class CustomReservedWordsTests {

        @Test
        @DisplayName("Should return false for custom reserved words")
        void shouldReturnFalseForCustomReservedWords() {
            when(constraintAnnotation.words()).thenReturn(new String[]{"admin", "root", "superuser"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);

            assertFalse(validator.isValid("admin", context));
            assertFalse(validator.isValid("root", context));
            assertFalse(validator.isValid("superuser", context));
        }

        @Test
        @DisplayName("Should return true for value not in custom reserved words")
        void shouldReturnTrueForNonReservedValue() {
            when(constraintAnnotation.words()).thenReturn(new String[]{"admin", "root"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);

            boolean result = validator.isValid("regularuser", context);
            assertTrue(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"ADMIN", "Admin", "aDmIn", "ROOT", "Root", "rOoT"})
        @DisplayName("Should be case insensitive for custom reserved words")
        void shouldBeCaseInsensitiveForCustomReservedWords(String value) {
            when(constraintAnnotation.words()).thenReturn(new String[]{"admin", "root"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);

            boolean result = validator.isValid(value, context);
            assertFalse(result);
        }
    }


    @Nested
    @DisplayName("Empty Reserved Words List Tests")
    class EmptyReservedWordsListTests {

        @BeforeEach
        void setUp() {
            when(constraintAnnotation.words()).thenReturn(new String[]{});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);
        }

        @Test
        @DisplayName("Should return true for any value when reserved words list is empty")
        void shouldReturnTrueWhenReservedWordsListIsEmpty() {
            assertTrue(validator.isValid("system", context));
            assertTrue(validator.isValid("admin", context));
            assertTrue(validator.isValid("anything", context));
        }
    }


    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should throw NullPointerException for null value")
        void shouldThrowExceptionForNullValue() {
            // Use lenient() because the stub may not be called if the exception is thrown first
            lenient().when(constraintAnnotation.words()).thenReturn(new String[]{"system"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);

            assertThrows(NullPointerException.class, () -> validator.isValid(null, context));
        }

        @Test
        @DisplayName("Should return true for empty string")
        void shouldReturnTrueForEmptyString() {
            when(constraintAnnotation.words()).thenReturn(new String[]{"system"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);

            boolean result = validator.isValid("", context);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true for whitespace only")
        void shouldReturnTrueForWhitespaceOnly() {
            when(constraintAnnotation.words()).thenReturn(new String[]{"system"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);

            boolean result = validator.isValid("   ", context);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle reserved words with spaces")
        void shouldHandleReservedWordsWithSpaces() {
            when(constraintAnnotation.words()).thenReturn(new String[]{"system admin", "super user"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);

            assertFalse(validator.isValid("system admin", context));
            assertFalse(validator.isValid("SYSTEM ADMIN", context));
            assertTrue(validator.isValid("systemadmin", context));
        }

        @Test
        @DisplayName("Should handle reserved word with trailing space")
        void shouldHandleReservedWordWithTrailingSpace() {
            when(constraintAnnotation.words()).thenReturn(new String[]{"admin "});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);

            // "admin" without trailing space should be valid
            assertTrue(validator.isValid("admin", context));
            // "admin " with trailing space should be invalid
            assertFalse(validator.isValid("admin ", context));
        }
    }


    @Nested
    @DisplayName("Special Characters Tests")
    class SpecialCharactersTests {

        @Test
        @DisplayName("Should handle reserved words with special characters")
        void shouldHandleReservedWordsWithSpecialCharacters() {
            when(constraintAnnotation.words()).thenReturn(new String[]{"admin@test", "user#1"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);

            assertFalse(validator.isValid("admin@test", context));
            assertFalse(validator.isValid("ADMIN@TEST", context));
            assertTrue(validator.isValid("admin", context));
        }

        @Test
        @DisplayName("Should handle reserved words with numbers")
        void shouldHandleReservedWordsWithNumbers() {
            when(constraintAnnotation.words()).thenReturn(new String[]{"admin123", "user456"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);

            assertFalse(validator.isValid("admin123", context));
            assertFalse(validator.isValid("ADMIN123", context));
            assertTrue(validator.isValid("admin124", context));
        }

        @Test
        @DisplayName("Should handle unicode characters in reserved words")
        void shouldHandleUnicodeInReservedWords() {
            when(constraintAnnotation.words()).thenReturn(new String[]{"administrateur"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);

            assertFalse(validator.isValid("administrateur", context));
            assertFalse(validator.isValid("ADMINISTRATEUR", context));
        }
    }


    @Nested
    @DisplayName("Multiple Reserved Words Tests")
    class MultipleReservedWordsTests {

        @BeforeEach
        void setUp() {
            when(constraintAnnotation.words()).thenReturn(
                    new String[]{"system", "admin", "root", "superuser", "administrator", "operator"}
            );
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);
        }

        @ParameterizedTest
        @ValueSource(strings = {"system", "admin", "root", "superuser", "administrator", "operator"})
        @DisplayName("Should return false for all reserved words")
        void shouldReturnFalseForAllReservedWords(String value) {
            boolean result = validator.isValid(value, context);
            assertFalse(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"user", "guest", "manager", "developer", "tester"})
        @DisplayName("Should return true for non-reserved words")
        void shouldReturnTrueForNonReservedWords(String value) {
            boolean result = validator.isValid(value, context);
            assertTrue(result);
        }
    }


    @Nested
    @DisplayName("Value Normalization Tests")
    class ValueNormalizationTests {

        @BeforeEach
        void setUp() {
            when(constraintAnnotation.words()).thenReturn(new String[]{"admin"});
            validator = new ReservedWordsValidator();
            validator.initialize(constraintAnnotation);
        }

        @ParameterizedTest
        @ValueSource(strings = {"admin", "ADMIN", "Admin", "aDmIn", "ADmin", "adMIN"})
        @DisplayName("Should normalize input to lowercase before checking")
        void shouldNormalizeInputToLowercase(String value) {
            boolean result = validator.isValid(value, context);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should preserve exact matching after normalization")
        void shouldPreserveExactMatchingAfterNormalization() {
            // "admin" is reserved, "admin1" is not
            assertFalse(validator.isValid("admin", context));
            assertTrue(validator.isValid("admin1", context));
            assertTrue(validator.isValid("1admin", context));
            assertTrue(validator.isValid("_admin", context));
        }
    }
}
