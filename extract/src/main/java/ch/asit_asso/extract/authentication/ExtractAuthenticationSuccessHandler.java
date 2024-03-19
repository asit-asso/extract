/*
 * Copyright (C) 2017 arx iT
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
package ch.asit_asso.extract.authentication;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorAuthenticationHandler;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorRememberMe;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.RememberMeTokenRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.utils.Secrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.stereotype.Component;


/**
 * Interceptor that carries actions after a successful authentication to the Extract application.
 *
 * @author Yves Grasset
 */
@Component
public class ExtractAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ExtractAuthenticationSuccessHandler.class);

    private final Secrets secrets;

    private final RememberMeTokenRepository rememberMeRepository;

    private final UsersRepository usersRepository;



    public ExtractAuthenticationSuccessHandler(Secrets secrets, RememberMeTokenRepository tokenRepository,
                                               UsersRepository usersRepository) {
        this.secrets = secrets;
        this.rememberMeRepository = tokenRepository;
        this.usersRepository = usersRepository;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
        final Authentication authentication) throws ServletException, IOException {
        this.logger.debug("Successful username / login authentication.");
        ApplicationUser user = (ApplicationUser) authentication.getPrincipal();
        Integer userId = user.getUserId();
        this.logger.debug("Looking for Extract user with ID {}.", userId);
        this.logger.debug("Users repository is {}", this.usersRepository);
        Optional<User> domainUserResult = this.usersRepository.findById(userId);

        if (domainUserResult.isEmpty()) {
            this.logger.error("Could not find domain user with ID {}, even though user / password succeeded. This is not normal.", userId);
            throw new IllegalStateException(String.format("No user found in repository with ID %d", userId));
        }

        User domainUser = domainUserResult.get();
        String userName = domainUser.getLogin();
        this.logger.debug("Found user {}.", userName);
        TwoFactorRememberMe rememberMeUser = new TwoFactorRememberMe(domainUser, this.rememberMeRepository,
                                                                     this.secrets);
        rememberMeUser.cleanUp();

        switch (user.getTwoFactorStatus()) {

            case INACTIVE -> {
                this.logger.debug("2FA for user {} is inactive. Processing to Extract.", userName);
                this.goToApplication(request, response, authentication);
            }

            case STANDBY -> {
                this.logger.debug("2FA for user {} is in standby. Processing to the 2FA registration page.", userName);
                request.getSession().setAttribute("2faStep", "REGISTER");
                request.getSession().setAttribute("2faProcess", "AUTHENTICATION");
//                new SimpleUrlAuthenticationSuccessHandler("/2fa/register")
//                        .onAuthenticationSuccess(request, response, authentication);
                new TwoFactorAuthenticationHandler("/2fa/register")
                        .onAuthenticationSuccess(request, response, authentication);

            }

            case ACTIVE -> {
                this.logger.debug("2FA for user {} is active.", userName);

                if (rememberMeUser.hasValidToken(request)) {
                    this.logger.debug("The 2FA token found in the cookie is valid. Bypassing 2FA authentication and processing to Extract.");
                    this.goToApplication(request, response, authentication);
                    break;
                }

                this.logger.debug("No valid remember-me cookie found for the user. Processing to the 2FA authentication page.");
                new TwoFactorAuthenticationHandler("/2fa/authenticate")
                        .onAuthenticationSuccess(request, response, authentication);
            }

            default ->
                    throw new IllegalStateException(String.format("Invalid 2FA status: %s", user.getTwoFactorStatus()));
        }
    }

    private void goToApplication(final HttpServletRequest request, final HttpServletResponse response,
                                 final Authentication authentication) throws IOException, ServletException {
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
