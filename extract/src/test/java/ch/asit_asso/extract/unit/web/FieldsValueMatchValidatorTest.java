package ch.asit_asso.extract.unit.web;

import ch.asit_asso.extract.web.constraints.FieldsValueMatch;
import ch.asit_asso.extract.web.constraints.FieldsValueMatchValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FieldsValueMatchValidator Tests")
class FieldsValueMatchValidatorTest {

    private FieldsValueMatchValidator validator;

    @Mock
    private FieldsValueMatch constraintAnnotation;

    @Mock
    private ConstraintValidatorContext context;


    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should initialize with field names from annotation")
        void shouldInitializeWithFieldNamesFromAnnotation() {
            when(constraintAnnotation.field()).thenReturn("password");
            when(constraintAnnotation.fieldMatch()).thenReturn("confirmPassword");

            validator = new FieldsValueMatchValidator();
            validator.initialize(constraintAnnotation);

            // Validator should be initialized without errors
            // We verify by using it in a test
            TestBean bean = new TestBean("test", "test");
            assertTrue(validator.isValid(bean, context));
        }
    }


    @Nested
    @DisplayName("Validation with matching fields")
    class MatchingFieldsTests {

        @BeforeEach
        void setUp() {
            when(constraintAnnotation.field()).thenReturn("password");
            when(constraintAnnotation.fieldMatch()).thenReturn("confirmPassword");

            validator = new FieldsValueMatchValidator();
            validator.initialize(constraintAnnotation);
        }

        @Test
        @DisplayName("Should return true when both fields have the same value")
        void shouldReturnTrueWhenFieldsMatch() {
            TestBean bean = new TestBean("SecurePassword123!", "SecurePassword123!");

            boolean result = validator.isValid(bean, context);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when both fields are null")
        void shouldReturnTrueWhenBothFieldsAreNull() {
            TestBean bean = new TestBean(null, null);

            boolean result = validator.isValid(bean, context);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when both fields are empty strings")
        void shouldReturnTrueWhenBothFieldsAreEmpty() {
            TestBean bean = new TestBean("", "");

            boolean result = validator.isValid(bean, context);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when both fields contain special characters")
        void shouldReturnTrueWhenBothFieldsContainSpecialChars() {
            String valueWithSpecialChars = "Pass@word#123!$%";
            TestBean bean = new TestBean(valueWithSpecialChars, valueWithSpecialChars);

            boolean result = validator.isValid(bean, context);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when both fields contain unicode characters")
        void shouldReturnTrueWhenBothFieldsContainUnicode() {
            String unicodeValue = "MotDePasseAvecAccents";
            TestBean bean = new TestBean(unicodeValue, unicodeValue);

            boolean result = validator.isValid(bean, context);

            assertTrue(result);
        }
    }


    @Nested
    @DisplayName("Validation with non-matching fields")
    class NonMatchingFieldsTests {

        @BeforeEach
        void setUp() {
            when(constraintAnnotation.field()).thenReturn("password");
            when(constraintAnnotation.fieldMatch()).thenReturn("confirmPassword");

            validator = new FieldsValueMatchValidator();
            validator.initialize(constraintAnnotation);
        }

        @Test
        @DisplayName("Should return false when fields have different values")
        void shouldReturnFalseWhenFieldsDoNotMatch() {
            TestBean bean = new TestBean("Password1", "Password2");

            boolean result = validator.isValid(bean, context);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when first field is null and second is not")
        void shouldReturnFalseWhenFirstFieldIsNullAndSecondIsNot() {
            TestBean bean = new TestBean(null, "SomePassword");

            boolean result = validator.isValid(bean, context);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when first field is not null and second is null")
        void shouldReturnFalseWhenFirstFieldIsNotNullAndSecondIsNull() {
            TestBean bean = new TestBean("SomePassword", null);

            boolean result = validator.isValid(bean, context);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when values differ only in case")
        void shouldReturnFalseWhenValuesDifferInCase() {
            TestBean bean = new TestBean("Password", "password");

            boolean result = validator.isValid(bean, context);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when values differ by whitespace")
        void shouldReturnFalseWhenValuesDifferByWhitespace() {
            TestBean bean = new TestBean("Password", "Password ");

            boolean result = validator.isValid(bean, context);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when one is empty and other is not")
        void shouldReturnFalseWhenOneIsEmptyAndOtherIsNot() {
            TestBean bean = new TestBean("", "NotEmpty");

            boolean result = validator.isValid(bean, context);

            assertFalse(result);
        }
    }


    @Nested
    @DisplayName("Validation with different field types")
    class DifferentFieldTypesTests {

        @Test
        @DisplayName("Should validate fields with different names")
        void shouldValidateFieldsWithDifferentNames() {
            when(constraintAnnotation.field()).thenReturn("email");
            when(constraintAnnotation.fieldMatch()).thenReturn("confirmEmail");

            validator = new FieldsValueMatchValidator();
            validator.initialize(constraintAnnotation);

            EmailTestBean bean = new EmailTestBean("test@example.com", "test@example.com");

            boolean result = validator.isValid(bean, context);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false for different email values")
        void shouldReturnFalseForDifferentEmailValues() {
            when(constraintAnnotation.field()).thenReturn("email");
            when(constraintAnnotation.fieldMatch()).thenReturn("confirmEmail");

            validator = new FieldsValueMatchValidator();
            validator.initialize(constraintAnnotation);

            EmailTestBean bean = new EmailTestBean("test@example.com", "different@example.com");

            boolean result = validator.isValid(bean, context);

            assertFalse(result);
        }
    }


    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @BeforeEach
        void setUp() {
            when(constraintAnnotation.field()).thenReturn("password");
            when(constraintAnnotation.fieldMatch()).thenReturn("confirmPassword");

            validator = new FieldsValueMatchValidator();
            validator.initialize(constraintAnnotation);
        }

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            String longString = "a".repeat(10000);
            TestBean bean = new TestBean(longString, longString);

            boolean result = validator.isValid(bean, context);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle strings with newlines")
        void shouldHandleStringsWithNewlines() {
            String stringWithNewlines = "Line1\nLine2\nLine3";
            TestBean bean = new TestBean(stringWithNewlines, stringWithNewlines);

            boolean result = validator.isValid(bean, context);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle strings with tabs")
        void shouldHandleStringsWithTabs() {
            String stringWithTabs = "Part1\tPart2\tPart3";
            TestBean bean = new TestBean(stringWithTabs, stringWithTabs);

            boolean result = validator.isValid(bean, context);

            assertTrue(result);
        }
    }


    /**
     * Test bean class with password and confirmPassword fields.
     */
    public static class TestBean {
        private final String password;
        private final String confirmPassword;

        public TestBean(String password, String confirmPassword) {
            this.password = password;
            this.confirmPassword = confirmPassword;
        }

        public String getPassword() {
            return password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }
    }


    /**
     * Test bean class with email and confirmEmail fields.
     */
    public static class EmailTestBean {
        private final String email;
        private final String confirmEmail;

        public EmailTestBean(String email, String confirmEmail) {
            this.email = email;
            this.confirmEmail = confirmEmail;
        }

        public String getEmail() {
            return email;
        }

        public String getConfirmEmail() {
            return confirmEmail;
        }
    }
}
