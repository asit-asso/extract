package ch.asit_asso.extract.authentication.twofactor;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.domain.User;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TwoFactorCookie {

    private static final String COOKIE_NAME_PREFIX = "2FA_ID";

    private static final String COOKIE_NAME_REGEX = String.format("^%s_([a-zA-z0-9]+)$", TwoFactorCookie.COOKIE_NAME_PREFIX);

    private static final Pattern COOKIE_NAME_REGEX_PATTERN = Pattern.compile(TwoFactorCookie.COOKIE_NAME_REGEX);

    private static final int DAYS_TO_LIVE = 30;

    private static final String PATH = "/extract";

    private final String userHash;

    private final String token;

    private final PasswordEncoder encoder;

    public TwoFactorCookie(@NotNull User user, @NotNull String token, @NotNull PasswordEncoder encoder) {
        this(user.getLogin(), token, encoder);
    }

    public TwoFactorCookie(@NotNull String userName, @NotNull String token, @NotNull PasswordEncoder encoder) {
        this.encoder = encoder;
        this.userHash = this.encoder.encode(userName);
        this.token = token;
    }

    private TwoFactorCookie(@NotNull Cookie cookie, @NotNull PasswordEncoder encoder) {

        if (!TwoFactorCookie.isTwoFactorCookie(cookie)) {
            throw new IllegalArgumentException("The cookie is not a 2FA remember-me cookie.");
        }

        this.encoder = encoder;
        this.userHash = this.parseUserHashFromCookieName(cookie.getName());
        this.token = cookie.getValue();
    }

    public static boolean isTwoFactorCookie(@NotNull Cookie cookie) {

        return cookie.getName().matches(TwoFactorCookie.COOKIE_NAME_REGEX);
    }


    public static TwoFactorCookie fromCookie(Cookie cookie, PasswordEncoder encoder) {

        if (!TwoFactorCookie.isTwoFactorCookie(cookie)) {
            throw new IllegalArgumentException("Cookie is not a 2FA cookie.");
        }

        return new TwoFactorCookie(cookie, encoder);
    }


    public String getToken() { return this.token; }


    public boolean isCookieUser(@NotNull User user) {

        return this.isCookieUser(user.getLogin());
    }


    public boolean isCookieUser(@NotNull String userName) {

        return this.encoder.matches(userName, this.userHash);
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
        //return cookieName.substring(TwoFactorCookie.COOKIE_NAME_PREFIX.length() + 1);
    }


//    private String buildCookieValue() {
//        return String.format(TwoFactorCookie.VALUE_STRING_FORMAT, this.userHash, token);
//    }
}
