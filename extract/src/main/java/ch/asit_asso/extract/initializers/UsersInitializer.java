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

import java.util.Locale;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.utils.Secrets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;



/**
 * An object that ensures that there are users defined.
 *
 * @author Yves Grasset
 */
public class UsersInitializer {

    /**
     * The string that identifies the localized name for the default administrator user in the application
     * strings.
     */
    private static final String ADMIN_USER_NAME_KEY = "default.users.administrator.name";

    /**
     * The string that identifies the localized name for the default system user in the application
     * strings.
     */
    private static final String SYSTEM_USER_NAME_KEY = "default.users.system.name";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(UsersInitializer.class);

    /**
     * The access to the application localized strings.
     */
    private final MessageSource messageSource;

    /**
     * The Spring Security object that allows to hash passwords.
     */
    private final Secrets secrets;

    /**
     * The object that links the user data objects with the data source.
     */
    private final UsersRepository repository;



    /**
     * Creates a new instance of the initializer.
     *
     * @param usersRepository        the object that links the user data objects with the data source
     * @param secrets          the password utility bean used by the application to hash passwords
     * @param localizedStringsSource the access to the application messages in the user's language
     */
    public UsersInitializer(final UsersRepository usersRepository, final Secrets secrets,
            final MessageSource localizedStringsSource) {

        if (usersRepository == null) {
            throw new IllegalArgumentException("The users repository cannot be null.");
        }

        if (secrets == null) {
            throw new IllegalArgumentException("The password utility bean cannot be null.");
        }

        if (localizedStringsSource == null) {
            throw new IllegalArgumentException("The message source cannot be null");
        }

        this.messageSource = localizedStringsSource;
        this.secrets = secrets;
        this.repository = usersRepository;
    }



    /**
     * Creates the default users if there none exist in the data source.
     */
    public final void ensureInitialized() {
        this.logger.debug("Checking if users are initialized.");

        if (this.repository.count() > 0) {
            this.logger.debug("Users have been found.");
            return;
        }

        this.createSystemUser();
        this.createDefaultAdministrator();
        this.createDefaultOperator();
    }



    /**
     * Creates the hidden user that is used to denote system operations.
     */
    private void createSystemUser() {
        this.logger.debug("Creating the system user.");

        User systemUser = new User();
        systemUser.setActive(false);
        systemUser.setLogin(User.SYSTEM_USER_LOGIN);
        systemUser.setName(this.getMessageString(UsersInitializer.SYSTEM_USER_NAME_KEY));
        systemUser.setPassword(".|}@;;bJXY5-#Fu$a}hNtpQ{");
        systemUser.setEmail("system@monmail.com");

        this.repository.save(systemUser);
        this.logger.info("The system user has been created.");
    }



    /**
     * Creates a user with administrator privileges.
     */
    private void createDefaultAdministrator() {
        this.logger.debug("Creating the default administrator.");

        User adminUser = new User();
        adminUser.setActive(true);
        adminUser.setLogin("admin");
        adminUser.setName(this.getMessageString(UsersInitializer.ADMIN_USER_NAME_KEY));
        adminUser.setPassword(this.secrets.hash("motdepasse21"));
        adminUser.setProfile(Profile.ADMIN);
        adminUser.setEmail("monadmin@monmail.com");

        this.repository.save(adminUser);
        this.logger.info("The default administrator has been created. Please log in and change its password.");
    }



    /**
     * Creates a user with operator privileges.
     */
    // TODO Remove when the user management part of the website is implemented
    private void createDefaultOperator() {
        this.logger.debug("Creating the default operator.");
        User operatorUser = new User();
        operatorUser.setActive(true);
        operatorUser.setLogin("operator");
        operatorUser.setName("Operator");
        operatorUser.setPassword(this.secrets.hash("motdepasse21"));
        operatorUser.setProfile(Profile.OPERATOR);
        operatorUser.setEmail("monoperateur@monmail.com");

        this.repository.save(operatorUser);
        this.logger.info("The default operator has been created. Please log in  with an administrator user and change"
                + " its password.");
    }



    /**
     * Obtains a localized string.
     *
     * @param messageKey the string that identifies the desired message
     * @return the message for the application locale
     */
    private String getMessageString(final String messageKey) {
        assert !StringUtils.isBlank(messageKey) : "The message key must not be empty.";

        return this.messageSource.getMessage(messageKey, null, Locale.getDefault());
    }

}
