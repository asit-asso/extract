package ch.asit_asso.extract.unit.authentication.twofactor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorCookie;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorRememberMe;
import ch.asit_asso.extract.domain.RememberMeToken;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.RememberMeTokenRepository;
import ch.asit_asso.extract.unit.MockEnabledTest;
import ch.asit_asso.extract.unit.persistance.RememberMeTokenRepositoryStub;
import ch.asit_asso.extract.utils.Secrets;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class TwoFactorRememberMeTest extends MockEnabledTest {

    @Captor
    private ArgumentCaptor<Cookie> cookieCaptor;

    private RememberMeTokenRepository repository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Secrets secrets;

    private TwoFactorRememberMe twoFactorRememberMe;

    private User user;



    @BeforeEach
    public void setUp() {
        this.user = new User(1);
        this.user.setLogin("testUser");
        this.user.setTwoFactorRecoveryCodesCollection(new ArrayList<>());

        this.repository = new RememberMeTokenRepositoryStub();

        Mockito.when(this.secrets.hash(anyString())).thenAnswer(
                (Answer<String>) invocationOnMock -> invocationOnMock.getArgument(0)
        );

        Mockito.when(this.secrets.check(anyString(), anyString())).thenAnswer(
                (Answer<Boolean>) invocationOnMock -> Objects.equals(invocationOnMock.getArgument(0),
                                                                     invocationOnMock.getArgument(1))
        );

        this.twoFactorRememberMe = new TwoFactorRememberMe(this.user, this.repository, this.secrets);

        Mockito.doNothing().when(this.response).addCookie(any());
    }



    @Test
    @DisplayName("Clean up the expired tokens for a user")
    void cleanUp() {
        List<RememberMeToken> tokens = new ArrayList<>();
        User otherUser = new User(2);
        otherUser.setLogin("otherUser");
        tokens.add(this.createToken(this.user, this.getExpiredDate()));
        tokens.add(this.createToken(this.user, this.getValidExpiration()));
        tokens.add(this.createToken(otherUser, this.getExpiredDate()));
        tokens.add(this.createToken(otherUser, this.getValidExpiration()));
        this.repository.saveAll(tokens);

        this.twoFactorRememberMe.cleanUp();

        assertEquals(3, this.repository.count());
        assertEquals(0, this.repository.getExpiredTokens(this.user).size());
        assertEquals(1, this.repository.getValidTokens(this.user).size());
        assertEquals(1, this.repository.getExpiredTokens(otherUser).size());
    }



    @Test
    @DisplayName("Clean up the expired tokens for a user who has no token")
    void cleanUpNoToken() {
        List<RememberMeToken> tokens = new ArrayList<>();
        User otherUser = new User(2);
        otherUser.setLogin("otherUser");
        tokens.add(this.createToken(otherUser, this.getExpiredDate()));
        tokens.add(this.createToken(otherUser, this.getValidExpiration()));
        this.repository.saveAll(tokens);

        assertDoesNotThrow(() ->
            this.twoFactorRememberMe.cleanUp()
        );

        assertEquals(2, this.repository.count());
        assertEquals(0, this.repository.getExpiredTokens(this.user).size());
        assertEquals(1, this.repository.getExpiredTokens(otherUser).size());
    }



    @Test
    @DisplayName("Clean up the expired tokens for a user who has no expired token")
    void cleanUpNoExpired() {
        List<RememberMeToken> tokens = new ArrayList<>();
        User otherUser = new User(2);
        otherUser.setLogin("otherUser");
        tokens.add(this.createToken(this.user, this.getValidExpiration()));
        tokens.add(this.createToken(otherUser, this.getExpiredDate()));
        tokens.add(this.createToken(otherUser, this.getValidExpiration()));
        this.repository.saveAll(tokens);

        assertDoesNotThrow(() ->
            this.twoFactorRememberMe.cleanUp()
        );

        assertEquals(3, this.repository.count());
        assertEquals(0, this.repository.getExpiredTokens(this.user).size());
        assertEquals(1, this.repository.getValidTokens(this.user).size());
        assertEquals(1, this.repository.getExpiredTokens(otherUser).size());
    }



    @Test
    @DisplayName("Clean up the expired tokens for a user when there is no token at all")
    void cleanUpEmpty() {
        assertDoesNotThrow(() ->
            this.twoFactorRememberMe.cleanUp()
        );
    }



    @Test
    @DisplayName("Disable remembering a user")
    void disable() {
        RememberMeToken token = this.createToken(this.user, this.getValidExpiration());
        Cookie[] cookies = new Cookie[] {
                new TwoFactorCookie(this.user, token.getToken(), this.secrets).toCookie()
        };
        Mockito.when(this.request.getCookies()).thenReturn(cookies);
        User otherUser = new User(2);
        List<RememberMeToken> tokensList = new ArrayList<>();
        tokensList.add(token);
        tokensList.add(this.createToken(this.user, this.getValidExpiration()));
        tokensList.add(this.createToken(otherUser, this.getValidExpiration()));
        this.repository.saveAll(tokensList);

        this.twoFactorRememberMe.disable(this.request, this.response);

        Mockito.verify(this.response, Mockito.atLeastOnce()).addCookie(this.cookieCaptor.capture());
        assertEquals(0, this.cookieCaptor.getValue().getMaxAge());
        assertEquals(0, this.repository.getValidTokens(this.user).size());
        assertEquals(1, this.repository.getValidTokens(otherUser).size());
    }



    @Test
    @DisplayName("Disable remembering a user when there is no cookie")
    void disableNoCookie() {
        Mockito.when(this.request.getCookies()).thenReturn(new Cookie[] {});
        User otherUser = new User(2);
        List<RememberMeToken> tokensList = new ArrayList<>();
        tokensList.add(this.createToken(this.user, this.getValidExpiration()));
        tokensList.add(this.createToken(otherUser, this.getValidExpiration()));
        this.repository.saveAll(tokensList);

        assertDoesNotThrow(() ->
            this.twoFactorRememberMe.disable(this.request, this.response)
        );

        Mockito.verify(this.response, Mockito.never()).addCookie(any());
        assertEquals(0, this.repository.getValidTokens(this.user).size());
        assertEquals(1, this.repository.getValidTokens(otherUser).size());
    }



    @Test
    @DisplayName("Enable remembering a user")
    void enable() {
        this.twoFactorRememberMe.enable(this.response);

        Mockito.verify(this.response, Mockito.atLeastOnce()).addCookie(this.cookieCaptor.capture());
        assertTrue(this.cookieCaptor.getValue().getMaxAge() > 0);
        assertEquals(1, this.repository.getValidTokens(this.user).size());
    }



    @Test
    @DisplayName("Check if token in cookie is valid for user")
    void hasValidToken() {
        RememberMeToken token = this.createToken(this.user, this.getValidExpiration());
        Cookie[] cookies = new Cookie[] {
                new TwoFactorCookie(this.user, token.getToken(), this.secrets).toCookie()
        };
        Mockito.when(this.request.getCookies()).thenReturn(cookies);
        this.repository.save(token);

        boolean isValid = this.twoFactorRememberMe.hasValidToken(this.request);

        assertTrue(isValid);
    }



    @Test
    @DisplayName("Check if token in cookie is valid for user when there is no cookie")
    void hasValidTokenWithoutCookie() {
        RememberMeToken token = this.createToken(this.user, this.getValidExpiration());
        Mockito.when(this.request.getCookies()).thenReturn(new Cookie[] {});
        this.repository.save(token);

        AtomicBoolean isValid = new AtomicBoolean(false);

        assertDoesNotThrow(() -> {
            isValid.set(this.twoFactorRememberMe.hasValidToken(this.request));
        });

        assertFalse(isValid.get());
    }



    @Test
    @DisplayName("Check if token in cookie is valid for user when the token is invalid")
    void hasValidTokenWithInvalidToken() {
        RememberMeToken token = this.createToken(this.user, this.getValidExpiration());
        Cookie[] cookies = new Cookie[] {
                new TwoFactorCookie(this.user, "invalidtoken", this.secrets).toCookie()
        };
        Mockito.when(this.request.getCookies()).thenReturn(cookies);
        this.repository.save(token);

        boolean isValid = this.twoFactorRememberMe.hasValidToken(this.request);

        assertFalse(isValid);
    }



    @Test
    @DisplayName("Check if token in cookie is valid for user when the cookie is for a different user")
    void hasValidTokenWithDifferentUser() {
        User otherUser = new User(2);
        RememberMeToken otherUserToken = this.createToken(otherUser, this.getValidExpiration());
        Cookie[] cookies = new Cookie[] {
                new TwoFactorCookie(otherUser, otherUserToken.getToken(), this.secrets).toCookie()
        };
        Mockito.when(this.request.getCookies()).thenReturn(cookies);
        this.repository.save(otherUserToken);

        boolean isValid = this.twoFactorRememberMe.hasValidToken(this.request);

        assertFalse(isValid);
    }



    @Test
    @DisplayName("Check if token in cookie is valid for user with cookies for the user and another one")
    void hasValidTokenWithMixedUsers() {
        RememberMeToken token = this.createToken(this.user, this.getValidExpiration());
        User otherUser = new User(2);
        RememberMeToken otherUserToken = this.createToken(otherUser, this.getValidExpiration());
        Cookie[] cookies = new Cookie[] {
                new TwoFactorCookie(otherUser, otherUserToken.getToken(), this.secrets).toCookie(),
                new TwoFactorCookie(this.user, token.getToken(), this.secrets).toCookie()
        };
        Mockito.when(this.request.getCookies()).thenReturn(cookies);
        List<RememberMeToken> tokensList = new ArrayList<>();
        tokensList.add(token);
        tokensList.add(otherUserToken);
        this.repository.saveAll(tokensList);

        boolean isValid = this.twoFactorRememberMe.hasValidToken(this.request);

        assertTrue(isValid);
    }



    private RememberMeToken createToken(User tokenUser, Calendar expiration) {
        RememberMeToken token = new RememberMeToken();
        token.setUser(tokenUser);
        token.setToken(RandomStringUtils.randomAlphanumeric(64));
        token.setTokenExpiration(expiration);

        return token;
    }


    private Calendar getExpiredDate() {
        Calendar expiredDate = Calendar.getInstance();
        expiredDate.add(Calendar.DAY_OF_MONTH, -1);

        return expiredDate;
    }



    private Calendar getValidExpiration() {
        Calendar validDate = Calendar.getInstance();
        validDate.add(Calendar.DAY_OF_MONTH, 1);

        return validDate;
    }
}
