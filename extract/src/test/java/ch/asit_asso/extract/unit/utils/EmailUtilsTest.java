/*
 * Copyright (C) 2025 arx iT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.unit.utils;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.utils.EmailUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailUtils class.
 *
 * Tests:
 * - isAddressValid method
 * - isAddressInUse methods
 * - isAddressInUseByOtherUser method
 *
 * @author Bruno Alves
 */
@DisplayName("EmailUtils Tests")
class EmailUtilsTest {

    @Mock
    private UsersRepository usersRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== 1. IS ADDRESS VALID TESTS ====================

    @Nested
    @DisplayName("1. isAddressValid Tests")
    class IsAddressValidTests {

        @Test
        @DisplayName("1.1 - Returns true for valid email address")
        void returnsTrueForValidEmail() {
            assertTrue(EmailUtils.isAddressValid("user@example.com"));
        }

        @Test
        @DisplayName("1.2 - Returns true for valid email with subdomain")
        void returnsTrueForValidEmailWithSubdomain() {
            assertTrue(EmailUtils.isAddressValid("user@mail.example.com"));
        }

        @Test
        @DisplayName("1.3 - Returns true for valid email with plus sign")
        void returnsTrueForValidEmailWithPlusSign() {
            assertTrue(EmailUtils.isAddressValid("user+tag@example.com"));
        }

        @Test
        @DisplayName("1.4 - Returns true for valid email with dots in local part")
        void returnsTrueForValidEmailWithDots() {
            assertTrue(EmailUtils.isAddressValid("first.last@example.com"));
        }

        @Test
        @DisplayName("1.5 - Returns true for valid email with numbers")
        void returnsTrueForValidEmailWithNumbers() {
            assertTrue(EmailUtils.isAddressValid("user123@example123.com"));
        }

        @Test
        @DisplayName("1.6 - Returns true for valid email with hyphen in domain")
        void returnsTrueForValidEmailWithHyphenInDomain() {
            assertTrue(EmailUtils.isAddressValid("user@my-domain.com"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("1.7 - Returns false for null or empty address")
        void returnsFalseForNullOrEmpty(String address) {
            assertFalse(EmailUtils.isAddressValid(address));
        }

        @Test
        @DisplayName("1.8 - Returns false for address without @ symbol")
        void returnsFalseForAddressWithoutAtSymbol() {
            assertFalse(EmailUtils.isAddressValid("userexample.com"));
        }

        @Test
        @DisplayName("1.9 - Returns false for address without domain")
        void returnsFalseForAddressWithoutDomain() {
            assertFalse(EmailUtils.isAddressValid("user@"));
        }

        @Test
        @DisplayName("1.10 - Returns false for address without local part")
        void returnsFalseForAddressWithoutLocalPart() {
            assertFalse(EmailUtils.isAddressValid("@example.com"));
        }

        @Test
        @DisplayName("1.11 - Returns false for address with multiple @ symbols")
        void returnsFalseForAddressWithMultipleAtSymbols() {
            assertFalse(EmailUtils.isAddressValid("user@@example.com"));
        }

        @Test
        @DisplayName("1.12 - Returns false for address with spaces")
        void returnsFalseForAddressWithSpaces() {
            assertFalse(EmailUtils.isAddressValid("user @example.com"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "plainaddress",
            "#@%^%#$@#$@#.com",
            "email.example.com",
            "email@example@example.com",
            ".email@example.com",
            "email..email@example.com"
        })
        @DisplayName("1.13 - Returns false for various invalid formats")
        void returnsFalseForInvalidFormats(String invalidEmail) {
            assertFalse(EmailUtils.isAddressValid(invalidEmail));
        }
    }

    // ==================== 2. IS ADDRESS IN USE (WITH USER) TESTS ====================

    @Nested
    @DisplayName("2. isAddressInUse (with User) Tests")
    class IsAddressInUseWithUserTests {

        @Test
        @DisplayName("2.1 - Returns true when email is in use by another user")
        void returnsTrueWhenEmailInUseByOtherUser() {
            // Given: A user with an ID and an email that's in use by someone else
            User currentUser = createUser(1, "currentUser");
            when(usersRepository.countByEmailIgnoreCaseAndLoginNot("taken@example.com", "currentUser")).thenReturn(1);

            // When: Checking if address is in use
            boolean result = EmailUtils.isAddressInUse("taken@example.com", currentUser, usersRepository);

            // Then: Should return true
            assertTrue(result);
            verify(usersRepository).countByEmailIgnoreCaseAndLoginNot("taken@example.com", "currentUser");
        }

        @Test
        @DisplayName("2.2 - Returns false when email is not in use by another user")
        void returnsFalseWhenEmailNotInUseByOtherUser() {
            // Given: A user with an ID and an email that's not in use
            User currentUser = createUser(1, "currentUser");
            when(usersRepository.countByEmailIgnoreCaseAndLoginNot("available@example.com", "currentUser")).thenReturn(0);

            // When: Checking if address is in use
            boolean result = EmailUtils.isAddressInUse("available@example.com", currentUser, usersRepository);

            // Then: Should return false
            assertFalse(result);
        }

        @Test
        @DisplayName("2.3 - Calls general isAddressInUse when user is null")
        void callsGeneralMethodWhenUserIsNull() {
            // Given: Null user
            when(usersRepository.countByEmailIgnoreCase("test@example.com")).thenReturn(1);

            // When: Checking if address is in use
            boolean result = EmailUtils.isAddressInUse("test@example.com", null, usersRepository);

            // Then: Should call general method and return true
            assertTrue(result);
            verify(usersRepository).countByEmailIgnoreCase("test@example.com");
            verify(usersRepository, never()).countByEmailIgnoreCaseAndLoginNot(anyString(), anyString());
        }

        @Test
        @DisplayName("2.4 - Calls general isAddressInUse when user ID is null")
        void callsGeneralMethodWhenUserIdIsNull() {
            // Given: User with null ID
            User userWithNullId = new User();
            userWithNullId.setLogin("testUser");
            when(usersRepository.countByEmailIgnoreCase("test@example.com")).thenReturn(0);

            // When: Checking if address is in use
            boolean result = EmailUtils.isAddressInUse("test@example.com", userWithNullId, usersRepository);

            // Then: Should call general method
            assertFalse(result);
            verify(usersRepository).countByEmailIgnoreCase("test@example.com");
        }
    }

    // ==================== 3. IS ADDRESS IN USE (WITHOUT USER) TESTS ====================

    @Nested
    @DisplayName("3. isAddressInUse (without User) Tests")
    class IsAddressInUseWithoutUserTests {

        @Test
        @DisplayName("3.1 - Returns true when email exists in database")
        void returnsTrueWhenEmailExists() {
            // Given: An email that exists
            when(usersRepository.countByEmailIgnoreCase("existing@example.com")).thenReturn(1);

            // When: Checking if address is in use
            boolean result = EmailUtils.isAddressInUse("existing@example.com", usersRepository);

            // Then: Should return true
            assertTrue(result);
        }

        @Test
        @DisplayName("3.2 - Returns false when email does not exist in database")
        void returnsFalseWhenEmailDoesNotExist() {
            // Given: An email that doesn't exist
            when(usersRepository.countByEmailIgnoreCase("new@example.com")).thenReturn(0);

            // When: Checking if address is in use
            boolean result = EmailUtils.isAddressInUse("new@example.com", usersRepository);

            // Then: Should return false
            assertFalse(result);
        }

        @Test
        @DisplayName("3.3 - Returns true when multiple users have the email")
        void returnsTrueWhenMultipleUsersHaveEmail() {
            // Given: An email used by multiple users (edge case)
            when(usersRepository.countByEmailIgnoreCase("shared@example.com")).thenReturn(3);

            // When: Checking if address is in use
            boolean result = EmailUtils.isAddressInUse("shared@example.com", usersRepository);

            // Then: Should return true
            assertTrue(result);
        }

        @Test
        @DisplayName("3.4 - Checks case insensitively")
        void checksCaseInsensitively() {
            // Given: Repository is set up to count by email ignore case
            when(usersRepository.countByEmailIgnoreCase("TEST@EXAMPLE.COM")).thenReturn(1);

            // When: Checking with uppercase email
            boolean result = EmailUtils.isAddressInUse("TEST@EXAMPLE.COM", usersRepository);

            // Then: Should use case-insensitive method
            assertTrue(result);
            verify(usersRepository).countByEmailIgnoreCase("TEST@EXAMPLE.COM");
        }
    }

    // ==================== 4. IS ADDRESS IN USE BY OTHER USER TESTS ====================

    @Nested
    @DisplayName("4. isAddressInUseByOtherUser Tests")
    class IsAddressInUseByOtherUserTests {

        @Test
        @DisplayName("4.1 - Returns true when email is used by different user")
        void returnsTrueWhenEmailUsedByDifferentUser() {
            // Given: An email used by a different user
            when(usersRepository.countByEmailIgnoreCaseAndLoginNot("taken@example.com", "currentUser")).thenReturn(1);

            // When: Checking if address is in use by other user
            boolean result = EmailUtils.isAddressInUseByOtherUser("taken@example.com", "currentUser", usersRepository);

            // Then: Should return true
            assertTrue(result);
        }

        @Test
        @DisplayName("4.2 - Returns false when email is only used by current user")
        void returnsFalseWhenEmailOnlyUsedByCurrentUser() {
            // Given: An email only used by current user
            when(usersRepository.countByEmailIgnoreCaseAndLoginNot("myemail@example.com", "currentUser")).thenReturn(0);

            // When: Checking if address is in use by other user
            boolean result = EmailUtils.isAddressInUseByOtherUser("myemail@example.com", "currentUser", usersRepository);

            // Then: Should return false
            assertFalse(result);
        }

        @Test
        @DisplayName("4.3 - Returns false when email is not used at all")
        void returnsFalseWhenEmailNotUsed() {
            // Given: An email not used by anyone
            when(usersRepository.countByEmailIgnoreCaseAndLoginNot("new@example.com", "anyUser")).thenReturn(0);

            // When: Checking if address is in use by other user
            boolean result = EmailUtils.isAddressInUseByOtherUser("new@example.com", "anyUser", usersRepository);

            // Then: Should return false
            assertFalse(result);
        }

        @Test
        @DisplayName("4.4 - Excludes current user by login (not by ID)")
        void excludesCurrentUserByLogin() {
            // Given
            when(usersRepository.countByEmailIgnoreCaseAndLoginNot("test@example.com", "specificLogin")).thenReturn(0);

            // When
            EmailUtils.isAddressInUseByOtherUser("test@example.com", "specificLogin", usersRepository);

            // Then: Should query excluding by login
            verify(usersRepository).countByEmailIgnoreCaseAndLoginNot(eq("test@example.com"), eq("specificLogin"));
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a test user with given ID and login.
     */
    private User createUser(Integer id, String login) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        return user;
    }
}
