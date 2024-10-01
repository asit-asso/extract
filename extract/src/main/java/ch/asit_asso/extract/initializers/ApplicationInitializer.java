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
package ch.asit_asso.extract.initializers;

import ch.asit_asso.extract.persistence.ApplicationRepositories;
import ch.asit_asso.extract.utils.Secrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;


/**
 * An object that ensures that the application is in an operational state.
 *
 * @author Yves Grasset
 */
@Component
public class ApplicationInitializer {

    /**
     * An ensemble of objects that link the data objects of the application to the data source.
     */
    private final ApplicationRepositories applicationRepositories;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);

    /**
     * The access to the application localized strings.
     */
    private final MessageSource messageSource;

    /**
     * The object that ensures the parameters generic to the application are set.
     */
    private ApplicationParametersInitializer parametersInitializer;

    /**
     * The Spring Security object used by the application to hash passwords.
     */
    private final Secrets secrets;

    private final ServletContext servletContext;

    /**
     * The object that ensures that there are users defined.
     */
    private UsersInitializer usersInitializer;


    public ApplicationInitializer(ApplicationRepositories repositories, MessageSource messageSource,
                                  Secrets secrets, ServletContext servletContext) {
        this.applicationRepositories = repositories;
        this.messageSource = messageSource;
        this.secrets = secrets;
        this.servletContext = servletContext;
        this.ensureInitialized();
    }


    /**
     * Carries the appropriate actions if the application is not operational.
     */
    public synchronized final void ensureInitialized() {
        this.logger.debug("Check that the application data is initialized.");
        this.getUsersInitializer().ensureInitialized();
        this.getParametersInitializer().ensureInitialized();
    }



    /**
     * Obtains the object that ensures that the parameters generic to the application are set.
     *
     * @return the parameters initializer
     */
    private ApplicationParametersInitializer getParametersInitializer() {

        if (this.parametersInitializer == null) {
            this.parametersInitializer
                    = new ApplicationParametersInitializer(this.applicationRepositories.getParametersRepository());
        }

        return this.parametersInitializer;
    }



    /**
     * Obtains the object that ensures that there are users defined.
     *
     * @return the users initializer
     */
    private UsersInitializer getUsersInitializer() {

        if (this.usersInitializer == null) {
            this.usersInitializer = new UsersInitializer(this.applicationRepositories.getUsersRepository(),
                                                         this.secrets, this.messageSource);
        }

        return this.usersInitializer;
    }

}
