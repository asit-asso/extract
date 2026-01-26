package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.RecoveryCode;
import ch.asit_asso.extract.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecoveryCode Entity Tests")
class RecoveryCodeTest {

    private RecoveryCode recoveryCode;

    @BeforeEach
    void setUp() {
        recoveryCode = new RecoveryCode();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null id")
        void defaultConstructor_createsInstanceWithNullId() {
            RecoveryCode newCode = new RecoveryCode();
            assertNull(newCode.getId());
        }

        @Test
        @DisplayName("Constructor with id sets the id correctly")
        void constructorWithId_setsIdCorrectly() {
            Integer expectedId = 42;
            RecoveryCode newCode = new RecoveryCode(expectedId);
            assertEquals(expectedId, newCode.getId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId work correctly")
        void setAndGetId() {
            Integer expectedId = 100;
            recoveryCode.setId(expectedId);
            assertEquals(expectedId, recoveryCode.getId());
        }

        @Test
        @DisplayName("setUser and getUser work correctly")
        void setAndGetUser() {
            User expectedUser = new User(1);
            recoveryCode.setUser(expectedUser);
            assertEquals(expectedUser, recoveryCode.getUser());
        }

        @Test
        @DisplayName("setToken and getToken work correctly")
        void setAndGetToken() {
            String expectedToken = "recovery-code-abc123";
            recoveryCode.setToken(expectedToken);
            assertEquals(expectedToken, recoveryCode.getToken());
        }
    }

    @Nested
    @DisplayName("Token Tests")
    class TokenTests {

        @Test
        @DisplayName("token can be set to null")
        void token_canBeSetToNull() {
            recoveryCode.setToken(null);
            assertNull(recoveryCode.getToken());
        }

        @Test
        @DisplayName("token can be set to empty string")
        void token_canBeSetToEmptyString() {
            recoveryCode.setToken("");
            assertEquals("", recoveryCode.getToken());
        }

        @Test
        @DisplayName("token can be set to alphanumeric value")
        void token_canBeSetToAlphanumericValue() {
            String alphanumericToken = "ABC123DEF456";
            recoveryCode.setToken(alphanumericToken);
            assertEquals(alphanumericToken, recoveryCode.getToken());
        }

        @Test
        @DisplayName("token can be set to long value")
        void token_canBeSetToLongValue() {
            String longToken = "A".repeat(100);
            recoveryCode.setToken(longToken);
            assertEquals(longToken, recoveryCode.getToken());
        }

        @Test
        @DisplayName("token can contain special characters")
        void token_canContainSpecialCharacters() {
            String specialToken = "code-with_special.chars!";
            recoveryCode.setToken(specialToken);
            assertEquals(specialToken, recoveryCode.getToken());
        }

        @Test
        @DisplayName("token can be replaced")
        void token_canBeReplaced() {
            recoveryCode.setToken("original-token");
            assertEquals("original-token", recoveryCode.getToken());

            recoveryCode.setToken("new-token");
            assertEquals("new-token", recoveryCode.getToken());
        }
    }

    @Nested
    @DisplayName("User Relationship Tests")
    class UserRelationshipTests {

        @Test
        @DisplayName("user can be set")
        void user_canBeSet() {
            User user = new User(1);
            user.setLogin("testuser");

            recoveryCode.setUser(user);

            assertEquals(user, recoveryCode.getUser());
            assertEquals("testuser", recoveryCode.getUser().getLogin());
        }

        @Test
        @DisplayName("user can be null")
        void user_canBeNull() {
            recoveryCode.setUser(null);
            assertNull(recoveryCode.getUser());
        }

        @Test
        @DisplayName("user can be replaced")
        void user_canBeReplaced() {
            User user1 = new User(1);
            User user2 = new User(2);

            recoveryCode.setUser(user1);
            assertEquals(user1, recoveryCode.getUser());

            recoveryCode.setUser(user2);
            assertEquals(user2, recoveryCode.getUser());
        }
    }

    @Nested
    @DisplayName("Complete RecoveryCode Configuration Tests")
    class CompleteConfigurationTests {

        @Test
        @DisplayName("fully configured recovery code has all attributes")
        void fullyConfiguredRecoveryCode_hasAllAttributes() {
            Integer id = 1;
            User user = new User(10);
            user.setLogin("testuser");
            String token = "recovery-abc-123";

            recoveryCode.setId(id);
            recoveryCode.setUser(user);
            recoveryCode.setToken(token);

            assertEquals(id, recoveryCode.getId());
            assertEquals(user, recoveryCode.getUser());
            assertEquals(token, recoveryCode.getToken());
        }

        @Test
        @DisplayName("recovery code can be used for 2FA recovery")
        void recoveryCode_canBeUsedFor2FARecovery() {
            User user = new User(1);
            user.setTwoFactorForced(true);

            recoveryCode.setId(1);
            recoveryCode.setUser(user);
            recoveryCode.setToken("BACKUP-CODE-1234");

            assertNotNull(recoveryCode.getToken());
            assertTrue(recoveryCode.getUser().isTwoFactorForced());
        }
    }

    @Nested
    @DisplayName("Id Tests")
    class IdTests {

        @Test
        @DisplayName("id can be set to zero")
        void id_canBeSetToZero() {
            recoveryCode.setId(0);
            assertEquals(0, recoveryCode.getId());
        }

        @Test
        @DisplayName("id can be set to negative value")
        void id_canBeSetToNegativeValue() {
            recoveryCode.setId(-1);
            assertEquals(-1, recoveryCode.getId());
        }

        @Test
        @DisplayName("id can be set to large value")
        void id_canBeSetToLargeValue() {
            recoveryCode.setId(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, recoveryCode.getId());
        }
    }

    @Nested
    @DisplayName("Multiple Recovery Codes Scenario Tests")
    class MultipleCodesScenarioTests {

        @Test
        @DisplayName("multiple recovery codes can be created for same user")
        void multipleRecoveryCodes_canBeCreatedForSameUser() {
            User user = new User(1);

            RecoveryCode code1 = new RecoveryCode(1);
            code1.setUser(user);
            code1.setToken("CODE-001");

            RecoveryCode code2 = new RecoveryCode(2);
            code2.setUser(user);
            code2.setToken("CODE-002");

            RecoveryCode code3 = new RecoveryCode(3);
            code3.setUser(user);
            code3.setToken("CODE-003");

            assertEquals(user, code1.getUser());
            assertEquals(user, code2.getUser());
            assertEquals(user, code3.getUser());
            assertNotEquals(code1.getToken(), code2.getToken());
            assertNotEquals(code2.getToken(), code3.getToken());
        }
    }
}
