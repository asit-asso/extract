package ch.asit_asso.extract.authentication.twofactor;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

public class TwoFactorAuthenticationHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(TwoFactorAuthenticationHandler.class);

    private final AuthenticationSuccessHandler successHandler;

    public TwoFactorAuthenticationHandler(String url) {
        this.logger.debug("URL to redirect in case of success is {}", url);

        if (url != null) {
            SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler(url);
            successHandler.setAlwaysUseDefaultTargetUrl(true);
            this.successHandler = successHandler;

        } else {
            this.successHandler = null;
        }
    }

//    @Override
//    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
//                                        AuthenticationException exception) throws IOException, ServletException {
//        Authentication anonymous = new AnonymousAuthenticationToken("key", "anonymousUser",
//                                                                    AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
//        saveTwoFactorAuthentication(request, response, new TwoFactorAuthentication(anonymous));
//    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        this.logger.debug("Authentication success, saving state.");
        this.logger.debug("Authentication object passed is of type {}", authentication.getClass().getCanonicalName());
        this.saveTwoFactorAuthentication(request, response, authentication);
    }

    private void saveTwoFactorAuthentication(HttpServletRequest request, HttpServletResponse response,
                                             Authentication authentication) throws IOException, ServletException {
        this.logger.debug("Setting 2FA authentication.");
        SecurityContextHolder.getContext().setAuthentication(new TwoFactorAuthentication(authentication));

        if (this.successHandler != null) {
            this.logger.debug("Redirecting.");
            this.successHandler.onAuthenticationSuccess(request, response, authentication);

            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        this.goToApplication(request, response, authentication);
    }


    private void goToApplication(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        DefaultSavedRequest savedRequest
                = (DefaultSavedRequest) new HttpSessionRequestCache().getRequest(request, response);

        if (savedRequest == null) {
            this.logger.debug("No request found. Redirecting to the default URL : {}.", this.getDefaultTargetUrl());
            this.getRedirectStrategy().sendRedirect(request, response, this.getDefaultTargetUrl());
            return;
        }

        this.logger.debug("The saved request URI is {}.", savedRequest.getRequestURI());

        if (request.getContextPath().equals(savedRequest.getRequestURI())) {
            this.logger.debug("The saved request is the application base URL with no trailing slash. Redirecting to"
                                      + " the home page.");
            this.getRedirectStrategy().sendRedirect(request, response, this.getDefaultTargetUrl());
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
