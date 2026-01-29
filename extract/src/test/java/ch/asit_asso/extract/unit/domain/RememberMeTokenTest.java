package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.RememberMeToken;
import ch.asit_asso.extract.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RememberMeToken Entity Tests")
class RememberMeTokenTest {

    private RememberMeToken token;

    @BeforeEach
    void setUp() {
        token = new RememberMeToken();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null id")
        void defaultConstructor_createsInstanceWithNullId() {
            RememberMeToken newToken = new RememberMeToken();
            assertNull(newToken.getId());
        }

        @Test
        @DisplayName("Constructor with id sets the id correctly")
        void constructorWithId_setsIdCorrectly() {
            Integer expectedId = 42;
            RememberMeToken newToken = new RememberMeToken(expectedId);
            assertEquals(expectedId, newToken.getId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId work correctly")
        void setAndGetId() {
            Integer expectedId = 100;
            token.setId(expectedId);
            assertEquals(expectedId, token.getId());
        }

        @Test
        @DisplayName("setUser and getUser work correctly")
        void setAndGetUser() {
            User expectedUser = new User(1);
            token.setUser(expectedUser);
            assertEquals(expectedUser, token.getUser());
        }

        @Test
        @DisplayName("setToken and getToken work correctly")
        void setAndGetToken() {
            String expectedToken = "remember-me-token-abc123";
            token.setToken(expectedToken);
            assertEquals(expectedToken, token.getToken());
        }

        @Test
        @DisplayName("setTokenExpiration and getTokenExpiration work correctly")
        void setAndGetTokenExpiration() {
            Calendar expectedExpiration = new GregorianCalendar(2024, Calendar.FEBRUARY, 15);
            token.setTokenExpiration(expectedExpiration);
            assertEquals(expectedExpiration, token.getTokenExpiration());
        }
    }

    @Nested
    @DisplayName("Token Tests")
    class TokenTests {

        @Test
        @DisplayName("token can be set to null")
        void token_canBeSetToNull() {
            token.setToken(null);
            assertNull(token.getToken());
        }

        @Test
        @DisplayName("token can be set to empty string")
        void token_canBeSetToEmptyString() {
            token.setToken("");
            assertEquals("", token.getToken());
        }

        @Test
        @DisplayName("token can be set to alphanumeric value")
        void token_canBeSetToAlphanumericValue() {
            String alphanumericToken = "ABC123DEF456GHI789";
            token.setToken(alphanumericToken);
            assertEquals(alphanumericToken, token.getToken());
        }

        @Test
        @DisplayName("token can be set to long value")
        void token_canBeSetToLongValue() {
            String longToken = "A".repeat(100);
            token.setToken(longToken);
            assertEquals(longToken, token.getToken());
        }

        @Test
        @DisplayName("token can be replaced")
        void token_canBeReplaced() {
            token.setToken("original-token");
            assertEquals("original-token", token.getToken());

            token.setToken("new-token");
            assertEquals("new-token", token.getToken());
        }
    }

    @Nested
    @DisplayName("Token Expiration Tests")
    class TokenExpirationTests {

        @Test
        @DisplayName("tokenExpiration can be set to null")
        void tokenExpiration_canBeSetToNull() {
            token.setTokenExpiration(null);
            assertNull(token.getTokenExpiration());
        }

        @Test
        @DisplayName("tokenExpiration can be in the future")
        void tokenExpiration_canBeInTheFuture() {
            Calendar futureDate = new GregorianCalendar();
            futureDate.add(Calendar.DAY_OF_MONTH, 30);

            token.setTokenExpiration(futureDate);

            assertTrue(token.getTokenExpiration().after(new GregorianCalendar()));
        }

        @Test
        @DisplayName("tokenExpiration can be in the past")
        void tokenExpiration_canBeInThePast() {
            Calendar pastDate = new GregorianCalendar();
            pastDate.add(Calendar.DAY_OF_MONTH, -30);

            token.setTokenExpiration(pastDate);

            assertTrue(token.getTokenExpiration().before(new GregorianCalendar()));
        }

        @Test
        @DisplayName("tokenExpiration can be replaced")
        void tokenExpiration_canBeReplaced() {
            Calendar date1 = new GregorianCalendar(2024, Calendar.JANUARY, 1);
            Calendar date2 = new GregorianCalendar(2024, Calendar.DECEMBER, 31);

            token.setTokenExpiration(date1);
            assertEquals(date1, token.getTokenExpiration());

            token.setTokenExpiration(date2);
            assertEquals(date2, token.getTokenExpiration());
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

            token.setUser(user);

            assertEquals(user, token.getUser());
            assertEquals("testuser", token.getUser().getLogin());
        }

        @Test
        @DisplayName("user can be null")
        void user_canBeNull() {
            token.setUser(null);
            assertNull(token.getUser());
        }

        @Test
        @DisplayName("user can be replaced")
        void user_canBeReplaced() {
            User user1 = new User(1);
            User user2 = new User(2);

            token.setUser(user1);
            assertEquals(user1, token.getUser());

            token.setUser(user2);
            assertEquals(user2, token.getUser());
        }
    }

    @Nested
    @DisplayName("Complete RememberMeToken Configuration Tests")
    class CompleteConfigurationTests {

        @Test
        @DisplayName("fully configured token has all attributes")
        void fullyConfiguredToken_hasAllAttributes() {
            Integer id = 1;
            User user = new User(10);
            user.setLogin("testuser");
            String tokenValue = "remember-me-abc-123";
            Calendar expiration = new GregorianCalendar(2024, Calendar.JUNE, 15);

            token.setId(id);
            token.setUser(user);
            token.setToken(tokenValue);
            token.setTokenExpiration(expiration);

            assertEquals(id, token.getId());
            assertEquals(user, token.getUser());
            assertEquals(tokenValue, token.getToken());
            assertEquals(expiration, token.getTokenExpiration());
        }

        @Test
        @DisplayName("token can be used for session persistence")
        void token_canBeUsedForSessionPersistence() {
            User user = new User(1);
            user.setActive(true);

            Calendar validExpiration = new GregorianCalendar();
            validExpiration.add(Calendar.DAY_OF_MONTH, 14);

            token.setId(1);
            token.setUser(user);
            token.setToken("session-persistence-token");
            token.setTokenExpiration(validExpiration);

            assertNotNull(token.getToken());
            assertTrue(token.getUser().isActive());
            assertTrue(token.getTokenExpiration().after(new GregorianCalendar()));
        }
    }

    @Nested
    @DisplayName("Id Tests")
    class IdTests {

        @Test
        @DisplayName("id can be set to zero")
        void id_canBeSetToZero() {
            token.setId(0);
            assertEquals(0, token.getId());
        }

        @Test
        @DisplayName("id can be set to negative value")
        void id_canBeSetToNegativeValue() {
            token.setId(-1);
            assertEquals(-1, token.getId());
        }

        @Test
        @DisplayName("id can be set to large value")
        void id_canBeSetToLargeValue() {
            token.setId(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, token.getId());
        }
    }

    @Nested
    @DisplayName("Token Validity Scenario Tests")
    class TokenValidityScenarioTests {

        @Test
        @DisplayName("valid token has future expiration")
        void validToken_hasFutureExpiration() {
            Calendar futureExpiration = new GregorianCalendar();
            futureExpiration.add(Calendar.DAY_OF_MONTH, 7);

            token.setToken("valid-token");
            token.setTokenExpiration(futureExpiration);

            assertTrue(token.getTokenExpiration().after(new GregorianCalendar()));
        }

        @Test
        @DisplayName("expired token has past expiration")
        void expiredToken_hasPastExpiration() {
            Calendar pastExpiration = new GregorianCalendar();
            pastExpiration.add(Calendar.DAY_OF_MONTH, -1);

            token.setToken("expired-token");
            token.setTokenExpiration(pastExpiration);

            assertTrue(token.getTokenExpiration().before(new GregorianCalendar()));
        }

        @Test
        @DisplayName("multiple tokens can exist for same user")
        void multipleTokens_canExistForSameUser() {
            User user = new User(1);

            RememberMeToken token1 = new RememberMeToken(1);
            token1.setUser(user);
            token1.setToken("token-device-1");

            RememberMeToken token2 = new RememberMeToken(2);
            token2.setUser(user);
            token2.setToken("token-device-2");

            assertEquals(user, token1.getUser());
            assertEquals(user, token2.getUser());
            assertNotEquals(token1.getToken(), token2.getToken());
        }
    }
}
