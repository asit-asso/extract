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
package org.easysdi.extract.web.controllers;

import org.easysdi.extract.web.Message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



/**
 * A controller that processes the request related to the authentication status.
 * <p>
 * <b>Note:</b> The authentication process itself is managed by the security configuration, not by this controller.
 *
 * @author Yves Grasset
 */
@Controller
@RequestMapping("/login")
public class LoginController extends BaseController {

    /**
     * The string that identifies the view to display the authentication form.
     */
    private static final String LOGIN_VIEW = "login";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(LoginController.class);



    /**
     * Displays the login page.
     *
     * @param redirectAttributes the data that may be passed by a page redirecting to this one
     * @return the string that identifies the view to display
     */
    @GetMapping
    public final String viewForm(final RedirectAttributes redirectAttributes) {

        if (this.isCurrentUserApplicationUser()) {
            return LoginController.REDIRECT_TO_HOME;
        }

        return LoginController.LOGIN_VIEW;
    }



    /**
     * Handles a login attempt that failed.
     *
     * @param redirectAttributes the data to pass to the page that user is redirected to
     * @return the string that identifies the view to display
     */
    @GetMapping("error")
    public final String processLoginError(final RedirectAttributes redirectAttributes) {
        this.addStatusMessage(redirectAttributes, "login.errors.badLogin", MessageType.ERROR);

        return LoginController.REDIRECT_TO_LOGIN;
    }



    /**
     * Handles a request to end the current user session.
     *
     * @param redirectAttributes the data to pass to the page that the user is redirected to
     * @return the string that identifies the view to display
     */
    @GetMapping("disconnect")
    public final String processLogout(final RedirectAttributes redirectAttributes) {
        this.addStatusMessage(redirectAttributes, "login.logout.success", MessageType.SUCCESS);

        return LoginController.REDIRECT_TO_LOGIN;
    }

}
