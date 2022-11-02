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

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.persistence.UserGroupsRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.web.model.UserGroupModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * An object that ensure that a model representing a user group contains valid information.
 *
 * @author Yves Grasset
 */
public class UserGroupValidator extends BaseValidator {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(UserGroupValidator.class);

    /**
     * The object that links the user data objects with the data source.
     */
    private final UsersRepository usersRepository;

    /**
     * The object that links the user group data objects with the data source.
     */
    private final UserGroupsRepository userGroupsRepository;



    /**
     * Creates a new instance of this validator.
     *
     * @param userGroupsRepository the object that links user group data objects with the data source
     * @param usersRepository the object that links user data objects with the data source
     */
    public UserGroupValidator(final UserGroupsRepository userGroupsRepository, final UsersRepository usersRepository) {

        if (userGroupsRepository == null) {
            throw new IllegalArgumentException("The user groups repository cannot be null.");
        }

        if (usersRepository == null) {
            throw new IllegalArgumentException("The users repository cannot be null.");
        }

        this.userGroupsRepository = userGroupsRepository;
        this.usersRepository = usersRepository;
    }



    /**
     * Determines if objects of a given type can be checked with this validator.
     *
     * @param type the class of the objects to validate
     * @return <code>true</code> if the type is supported by this validator
     */
    @Override
    public final boolean supports(final Class<?> type) {
        return UserGroupModel.class.equals(type);
    }



    /**
     * Checks the conformity of the user group model information.
     *
     * @param target the object to validate
     * @param errors an object that assembles the validation errors for the object
     */
    @Override
    @Transactional(readOnly = true)
    public void validate(final Object target, final Errors errors) {
        this.logger.debug("Validating the user group model {}.", target);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "userGroupDetails.errors.name.required");

        final UserGroupModel validatingUserGroup = (UserGroupModel) target;
        Integer validatingUserGroupId = null;

        if (!validatingUserGroup.isBeingCreated()) {
            UserGroup savedUserGroup = this.userGroupsRepository.findById(validatingUserGroup.getId())
                .orElseThrow(() -> {
                    return new UnsupportedOperationException("If the user group is not being created, it should be present in"
                            + " the data source.");
                });
            validatingUserGroupId = savedUserGroup.getId();
        }

        String nameErrorMessage = this.validateName(validatingUserGroup.getName(), validatingUserGroupId);

        if (nameErrorMessage != null) {
            errors.rejectValue("name", nameErrorMessage);
        }

        String usersErrorMessage = this.validateUsersList(validatingUserGroup);

        if (usersErrorMessage != null) {
            errors.rejectValue("users", usersErrorMessage);
        }
    }



    /**
     * Ensures that the proposed user group name is acceptable.
     *
     * @param name  the name to validate
     * @param userGroupId the number that identifies the user whose name must be validated
     * @return a string containing the key of the error message triggered, or <code>null</code> if the name is valid
     */
    private String validateName(final String name, final Integer userGroupId) {

        if (StringUtils.isBlank(name)) {
            return "userGroupDetails.errors.name.required";
        }

        final UserGroup userGroupWithSameName = (userGroupId == null)
                ? this.userGroupsRepository.findByNameIgnoreCase(name)
                : this.userGroupsRepository.findByNameIgnoreCaseAndIdNot(name, userGroupId);

        if (userGroupWithSameName != null) {
            return "userGroupDetails.errors.name.inUse";
        }

        return null;
    }



    /**
     * Ensures that the password fields are correctly set.
     *
     * @param userGroupModel the model representing the user group whose members data should be checked.
     */
    private String validateUsersList(final UserGroupModel userGroupModel) {
        Optional<UserGroup> domainUserGroup = this.userGroupsRepository.findById(userGroupModel.getId());
        boolean isAssociatedToProcesses = (domainUserGroup.isPresent() && domainUserGroup.get().isAssociatedToProcesses());
        String userIdsString = userGroupModel.getUsersIds();
        this.logger.debug("Users ids are {} (isEmpty: {}). The user group {} associated to a process.",
                            userIdsString, StringUtils.isEmpty(userIdsString),
                            (isAssociatedToProcesses) ? "is" : "is not");

        if (StringUtils.isEmpty(userIdsString)) {

            if (isAssociatedToProcesses) {
                return "userGroupDetails.errors.users.required";
            }

            return null;
        }

        List<Integer> usersIds = new ArrayList<>();
        boolean hasActiveUsers = false;

        for (String idString : userIdsString.split(",")) {
            Integer userId = NumberUtils.toInt(idString);

            if (userId == null) {
                return "userGroupDetails.errors.users.invalidId";
            }

            if (usersIds.contains(userId)) {
                return "userGroupDetails.errors.users.duplicates";
            }

            Optional<User> member = this.usersRepository.findById(userId);

            if (member.isEmpty()) {
                return "userGroupDetails.errors.users.notFound";
            }

            if (member.get().isActive()) {
                hasActiveUsers = true;
            }

            usersIds.add(userId);
        }

        this.logger.debug("The user group {} associated to a process and {} at least one active user.",
                          (userGroupModel.isAssociatedToProcesses()) ? "is" : "is not",
                          (hasActiveUsers) ? "has" : "has not");

        if (isAssociatedToProcesses && !hasActiveUsers) {
            return "userGroupDetails.errors.users.required";
        }

        return null;
    }

}
