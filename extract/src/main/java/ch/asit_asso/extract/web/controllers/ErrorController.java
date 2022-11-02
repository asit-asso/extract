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
package ch.asit_asso.extract.web.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



/**
 * A controller helper that handles the errors occurring in the web application.
 *
 * @author Yves Grasset
 */
@ControllerAdvice
public class ErrorController {

    /**
     * The string that identifies the view to display an issue with the application.
     */
    private static final String ERROR_VIEW = "error";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ErrorController.class);



    /**
     * Processes an unhandled exception thrown by the web application.
     *
     * @param throwable          the error to handle
     * @param redirectAttributes the data to pass to the view that the user will be redirected to
     * @return the string that identifies the next view to display
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public final String handleException(final Throwable throwable, final RedirectAttributes redirectAttributes) {
        this.logger.error("An exception was thrown during execution of the application", throwable);
        String errorMessage = (throwable != null ? throwable.getMessage() : "Unknown error");
        redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        return ErrorController.ERROR_VIEW;
    }

}
