package ch.asit_asso.extract.authentication.twofactor;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.domain.RememberMeToken;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.RememberMeTokenRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TwoFactorRememberMe {
    private final Logger logger = LoggerFactory.getLogger(TwoFactorRememberMe.class);

    private final PasswordEncoder encoder;

    private RememberMeTokenRepository repository;

    private final User user;

    public TwoFactorRememberMe(@NotNull User user, @NotNull RememberMeTokenRepository tokenRepository,
                               @NotNull PasswordEncoder passwordEncoder) {
        this.encoder = passwordEncoder;
        this.repository = tokenRepository;
        this.user = user;
    }



    public void cleanUp() {
        this.logger.debug("Clearing the expired tokens for user {}.", this.user.getLogin());
        Collection<RememberMeToken> expiredTokens = this.repository.getExpiredTokens(this.user);

        if (expiredTokens == null || expiredTokens.isEmpty()) {
            this.logger.debug("No expired tokens found.");
            return;
        }

        this.logger.debug("{} expired tokens to delete.", expiredTokens.size());
        this.repository.deleteAll(expiredTokens);
    }



    public void disable(HttpServletRequest request, HttpServletResponse response) {
        this.expireCookie(request, response);
        this.removeEntries();
    }



    public void enable(HttpServletResponse response) {
        String token = RandomStringUtils.randomAlphanumeric(64);
        this.createEntry(token);
        this.setCookie(token, response);
    }



    public boolean hasValidToken(HttpServletRequest request) {
        this.logger.debug("Fetching 2FA remember-me cookie for user {} if present.", this.user.getLogin());
        Optional<String> cookieToken = this.getCookieToken(request);

        if (cookieToken.isEmpty()) {
            this.logger.debug("No 2FA remember-me cookie found.");
            return false;
        }

        this.logger.debug("A 2FA remember-me cookie has been found for the user. Checking its validity in the database.");
        return this.repository.getValidTokens(this.user)
                              .stream()
                              .anyMatch((entry) -> this.encoder.matches(cookieToken.get(),
                                                                        entry.getToken()));
    }



    private void createEntry(String token) {
        RememberMeToken entry = new RememberMeToken();
        entry.setToken(this.encoder.encode(token));
        Calendar expiration = Calendar.getInstance();
        expiration.setTime(Date.from(ZonedDateTime.now().plusDays(30).toInstant()));
        entry.setTokenExpiration(expiration);
        entry.setUser(this.user);
        this.repository.save(entry);
    }



    private void expireCookie(HttpServletRequest request, HttpServletResponse response) {
        TwoFactorCookie twoFactorCookie = Arrays.stream(request.getCookies())
                                                .filter(TwoFactorCookie::isTwoFactorCookie)
                                                .map((cookie) -> TwoFactorCookie.fromCookie(cookie, this.encoder))
                                                .filter((cookie) -> cookie.isCookieUser(this.user.getLogin()))
                                                .findFirst().orElse(null);

        if (twoFactorCookie == null) {
            return;
        }

        response.addCookie(twoFactorCookie.toCookie(true));
    }



    private Optional<String> getCookieToken(HttpServletRequest request) {

        return Arrays.stream(request.getCookies())
                     .filter(TwoFactorCookie::isTwoFactorCookie)
                     .map((cookie) -> TwoFactorCookie.fromCookie(cookie, this.encoder))
                     .filter((cookie) -> cookie.isCookieUser(this.user.getLogin()))
                     .map(TwoFactorCookie::getToken)
                     .findFirst();
    }


    private void removeEntries() {
        this.repository.deleteByUser(this.user);
    }



    private void setCookie(String token, HttpServletResponse response) {
        TwoFactorCookie twoFactorCookie = new TwoFactorCookie(this.user.getLogin(), token, this.encoder);
        response.addCookie(twoFactorCookie.toCookie());
    }

}
