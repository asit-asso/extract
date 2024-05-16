package ch.asit_asso.extract.authentication.twofactor;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.utils.Secrets;
import org.springframework.http.ResponseCookie;

public class TwoFactorCookie {

    private static final String COOKIE_NAME_PREFIX = "2FA_ID";

    private static final String COOKIE_NAME_REGEX = String.format("^%s_([a-zA-z0-9]+)$", TwoFactorCookie.COOKIE_NAME_PREFIX);

    private static final Pattern COOKIE_NAME_REGEX_PATTERN = Pattern.compile(TwoFactorCookie.COOKIE_NAME_REGEX);

    private static final int DAYS_TO_LIVE = 30;

    private static final String PATH = "/extract";

    private final String userHash;

    private final String token;

    private final Secrets secrets;

    public TwoFactorCookie(@NotNull User user, @NotNull String token, @NotNull Secrets secrets) {
        this(user.getLogin(), token, secrets);
    }

    public TwoFactorCookie(@NotNull String userName, @NotNull String token, @NotNull Secrets secrets) {
        this.secrets = secrets;
        this.userHash = this.secrets.hash(userName);
        this.token = token;
    }

    private TwoFactorCookie(@NotNull Cookie cookie, @NotNull Secrets secrets) {

        if (!TwoFactorCookie.isTwoFactorCookie(cookie)) {
            throw new IllegalArgumentException("The cookie is not a 2FA remember-me cookie.");
        }

        this.secrets = secrets;
        this.userHash = this.parseUserHashFromCookieName(cookie.getName());
        this.token = cookie.getValue();
    }

    public static boolean isTwoFactorCookie(@NotNull Cookie cookie) {

        return cookie.getName().matches(TwoFactorCookie.COOKIE_NAME_REGEX);
    }


    public static TwoFactorCookie fromCookie(Cookie cookie, Secrets secrets) {

        if (!TwoFactorCookie.isTwoFactorCookie(cookie)) {
            throw new IllegalArgumentException("Cookie is not a 2FA cookie.");
        }

        return new TwoFactorCookie(cookie, secrets);
    }


    public String getToken() { return this.token; }


    public boolean isCookieUser(@NotNull User user) {

        return this.isCookieUser(user.getLogin());
    }


    public boolean isCookieUser(@NotNull String userName) {

        return this.secrets.check(userName, this.userHash);
    }


    public Cookie toCookie() {
        return this.toCookie(false);
    }


    public Cookie toCookie(boolean expire) {
        Cookie cookie = new Cookie(this.buildCookieName(), (expire) ? null : this.token);
        cookie.setHttpOnly(true);
        cookie.setPath(TwoFactorCookie.PATH);
        cookie.setMaxAge((expire) ? 0 : (int) Duration.ofDays(TwoFactorCookie.DAYS_TO_LIVE).toSeconds());

        return cookie;
    }


    public ResponseCookie toResponseCookie() {
        return this.toResponseCookie(false);
    }



    public ResponseCookie toResponseCookie(boolean expire) {

        return ResponseCookie.from(this.buildCookieName(), (expire) ? "" : this.token)
                             .httpOnly(true)
                             .path(TwoFactorCookie.PATH)
                             .maxAge(Duration.ofDays((expire) ? 0 : TwoFactorCookie.DAYS_TO_LIVE))
                             .build();
    }


    private String buildCookieName() {
        return String.format("%s_%s", TwoFactorCookie.COOKIE_NAME_PREFIX, this.userHash);
    }



    private String parseUserHashFromCookieName(@NotNull String cookieName) {
        Matcher nameMatcher = TwoFactorCookie.COOKIE_NAME_REGEX_PATTERN.matcher(cookieName);

        if (!nameMatcher.matches() || nameMatcher.groupCount() == 0) {
            throw new IllegalArgumentException("The cookie name does not match the 2FA cookie name format.");
        }

        return nameMatcher.group(1);
    }
}
