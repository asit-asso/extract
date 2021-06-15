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
package org.easysdi.extract.authentication;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;



/**
 * Interceptor that carries actions after a successful authentication to the Extract application.
 *
 * @author Yves Grasset
 */
public class ExtractAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ExtractAuthenticationSuccessHandler.class);



    /**
     * {@inheritDoc}
     */
    @Override
    public final void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
            final Authentication authentication) throws ServletException, IOException {
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
