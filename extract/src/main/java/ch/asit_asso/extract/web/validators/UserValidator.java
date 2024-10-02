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
package ch.asit_asso.extract.web.validators;

import ch.asit_asso.extract.authentication.ApplicationUser;
import ch.asit_asso.extract.utils.EmailUtils;
import org.apache.commons.lang3.StringUtils;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.web.model.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;


/**
 * An object that ensure that a model representing a user contains valid information.
 *
 * @author Yves Grasset
 */
public class UserValidator extends BaseValidator {

    /**
     * The minimum number of characters that a password must contain.
     */
    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(UserValidator.class);

    /**
     * The object that links the user data objects with the data source.
     */
    private final UsersRepository usersRepository;

    /**
     * The lock to make the validation thread-safe
     */
    private static final Object lock = new Object();

    /**
     * Creates a new instance of this validator.
     *
     * @param repository the object that links user data objects with the data source
     */
    public UserValidator(final UsersRepository repository) {

        if (repository == null) {
            throw new IllegalArgumentException("The users repository cannot be null.");
        }

        this.usersRepository = repository;
    }



    /**
     * Determines if objects of a given type can be checked with this validator.
     *
     * @param type the class of the objects to validate
     * @return <code>true</code> if the type is supported by this validator
     */
    @Override
    public final boolean supports(final Class<?> type) {
        return UserModel.class.equals(type);
    }



    /**
     * Checks the conformity of the user model information.
     *
     * @param target the object to validate
     * @param errors an object that assembles the validation errors for the object
     */
    @Override
    @Transactional(readOnly = true)
    public void validate(final Object target, final Errors errors) {
        this.logger.debug("Validating the user model {}.", target);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "userDetails.errors.name.required");
        ValidationUtils.rejectIfEmpty(errors, "profile", "userDetails.errors.profile.notSet");

        final UserModel validatingUser = (UserModel) target;
        Integer validatingUserId = null;

        final ApplicationUser currentUser
                = (ApplicationUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!validatingUser.isBeingCreated()) {
            User savedUser = this.usersRepository.findById(validatingUser.getId())
                .orElseThrow(() -> {
                    return new UnsupportedOperationException("If the user is not being created, it should be present in"
                            + " the data source.");
                });
            validatingUserId = savedUser.getId();

            if (validatingUserId == currentUser.getUserId()) {

                if (!validatingUser.isActive()) {
                    errors.rejectValue("active", "userDetails.errors.currentUser.inactive");
                }

                if (validatingUser.getProfile() != savedUser.getProfile()) {
                    errors.rejectValue("profile", "userDetails.errors.currentUser.profile.changed");
                }
            }

            if (!validatingUser.isActive() && savedUser.isAssociatedToProcesses()) {
                errors.rejectValue("active", "userDetails.errors.hasProcesses.inactive");
            }

            if (!validatingUser.isActive() && savedUser.isLastActiveMemberOfProcessGroup()) {
                errors.rejectValue("active", "userDetails.errors.lastActiveMember.inactive");
            }
        }

        String loginErrorMessage = this.validateLogin(validatingUser.getLogin(), validatingUserId);

        if (loginErrorMessage != null) {
            errors.rejectValue("login", loginErrorMessage);
        }

        final String emailErrorMessage = this.validateEmail(validatingUser.getEmail(), validatingUserId);

        if (emailErrorMessage != null) {
            errors.rejectValue("email", emailErrorMessage);
        }

        this.validatePassword(validatingUser, errors);
    }



    /**
     * Ensures that the proposed user identifier is acceptable.
     *
     * @param login  the login to validate
     * @param userId the number that identifies the user whose login must be validated
     * @return a string containing the key of the error message triggered, or <code>null</code> if the login is valid
     */
    private String validateLogin(final String login, final Integer userId) {

        if (StringUtils.isBlank(login)) {
            return "userDetails.errors.login.required";
        }

        final User userWithSameLogin = (userId == null)
                ? this.usersRepository.findByLoginIgnoreCase(login)
                : this.usersRepository.findByLoginIgnoreCaseAndIdNot(login, userId);

        if (userWithSameLogin != null) {
            return "userDetails.errors.login.inUse";
        }

        return null;
    }



    /**
     * Ensures that an e-mail address is correctly formed.
     * <p>
     * <b>Note:</b> Does not check if the address is a real one.
     *
     * @param email  the string containing the e-mail address
     * @param userId the number that identifies the user whose e-mail address must be validated
     * @return the key of the validation error message, or <code>null</code> if the address is valid
     */
    private String validateEmail(final String email, final Integer userId) {

        if (StringUtils.isBlank(email)) {
            return "userDetails.errors.email.required";
        }

        if (!EmailUtils.isAddressValid(email)) {
            return "userDetails.errors.email.invalid";
        }

        final User userWithSameEmail = (userId == null)
                ? this.usersRepository.findByEmailIgnoreCase(email)
                : this.usersRepository.findByEmailIgnoreCaseAndIdNot(email, userId);

        if (userWithSameEmail != null) {
            return "userDetails.errors.email.inUse";
        }

        return null;
    }



    /**
     * Ensures that the password fields are correctly set.
     *
     * @param userModel the model representing the user whose password data should be checked.
     * @param errors    the object that assembles the validation errors for the model
     */
    private void validatePassword(final UserModel userModel, final Errors errors) {

        if (userModel.isPasswordUnchanged()) {
            return;
        }

        if (!userModel.isPasswordDefined()) {
            errors.rejectValue("password", "userDetails.errors.password.required");
            return;
        }

        // validate the password
        PasswordValidator passwordValidator = PasswordValidator.create().withStopOnFirstError(false);
        try {
            synchronized (lock) {
                passwordValidator.validateField("password", userModel.getPassword(), errors);
            }
        } catch (Exception e) {
            errors.rejectValue("password", "userDetails.errors.password.invalid");
        }

        if (errors.hasErrors()) {
            return;
        }

        if (StringUtils.isEmpty(userModel.getPasswordConfirmation())) {
            errors.rejectValue("passwordConfirmation", "userDetails.errors.passwordConfirmation.required");
            return;
        }

        if (!userModel.isPasswordMatch()) {
            errors.rejectValue("passwordConfirmation", "userDetails.errors.password.mismatch");
        }
    }

}
