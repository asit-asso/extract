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
import java.util.UUID;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.domain.User.TwoFactorStatus;
import ch.asit_asso.extract.domain.User.UserType;
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
        this.logger.info("Checking if users are initialized.");

        if (this.repository.count() > 0) {
            this.logger.info("Users have been found.");
            return;
        }

        this.createSystemUser();
    }



    /**
     * Creates the hidden user that is used to denote system operations.
     */
    private void createSystemUser() {
        this.logger.info("Creating the system user.");

        final String password = this.generateSystemPassword();

        User systemUser = new User();
        systemUser.setActive(false);
        systemUser.setLogin(User.SYSTEM_USER_LOGIN);
        systemUser.setName(this.getMessageString(UsersInitializer.SYSTEM_USER_NAME_KEY));
        systemUser.setPassword(this.secrets.hash(password));
        systemUser.setEmail("extract@asit-asso.ch");
        systemUser.setMailActive(false);
        systemUser.setUserType(UserType.LOCAL);
        systemUser.setTwoFactorStatus(TwoFactorStatus.INACTIVE);
        systemUser.setTwoFactorForced(false);

        logger.warn("\n");
        logger.warn("-".repeat(80));
        logger.warn("   SYSTEM USER PASSWORD: {}", password);
        logger.warn("-".repeat(80));
        logger.warn("\n");

        this.repository.save(systemUser);
        this.logger.info("The system user has been created.");
    }

    /**
     * Generate a totally random password
     * @return a totally random password
     */
    protected String generateSystemPassword() {
        return UUID.randomUUID().toString();
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
