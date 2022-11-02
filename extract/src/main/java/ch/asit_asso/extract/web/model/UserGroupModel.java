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
package ch.asit_asso.extract.web.model;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.persistence.UserGroupsRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * The representation of a user group for a view.
 *
 * @author Yves Grasset
 */
public class UserGroupModel {

    /**
     * The number that uniquely identifies this user group.
     */
    private Integer id;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Whether this user group is a new one.
     */
    private boolean beingCreated;

    /**
     * The user-friendly name of this group.
     */
    private String name;

    /**
     * The userGroupes that this user group operates on.
     */
    private final List<Process> processesList;
    
    /**
     * The members of this group.
     */
    private final List<UserModel> usersList;

    /**
     * An array that contains the identifiers of the members of this group.
     */
    private String[] usersIds;



    /**
     * Obtains whether this user is a new one.
     *
     * @return <code>true</code> if this user has not been persisted yet
     */
    public final boolean isBeingCreated() {
        return this.beingCreated;
    }



    /**
     * Defines whether this user group is a new one.
     *
     * @param isNew <code>true</code> if this user group has yet to be persisted
     */
    public final void setBeingCreated(final boolean isNew) {
        this.beingCreated = isNew;
    }



    /**
     * Obtains the number that uniquely identifies this user group.
     *
     * @return the identifier
     */
    public final Integer getId() {
        return id;
    }



    /**
     * Defines the number that uniquely identifies this user group.
     *
     * @param userGroupId the identifier
     */
    public final void setId(final Integer userGroupId) {
        this.id = userGroupId;
    }



    /**
     * Obtains the user-friendly name of this user group.
     *
     * @return the name
     */
    public final String getName() {
        return this.name;
    }



    /**
     * Defines the user-friendly name of this user group.
     *
     * @param userGroupName the name
     */
    public final void setName(final String userGroupName) {
        this.name = userGroupName;
    }



    /**
     * Obtains the steps of this userGroup.
     *
     * @return an array containing the tasks that make up this userGroup
     */
    public final Process[] getProcesses() {
        return this.processesList.toArray(new Process[]{});
    }



    /**
     * Obtains the steps of this userGroup.
     *
     * @return an array containing the tasks that make up this userGroup
     */
    public final void setProcesses(Collection<Process> processes) {
        this.processesList.clear();
        this.processesList.addAll(processes);
    }


    /**
     * Obtains the users of this userGroup.
     *
     * @return an array containing the users that make up this userGroup
     */
    public final UserModel[] getUsers() {
        return this.usersList.toArray(new UserModel[]{});
    }



    /**
     * Obtains the identifiers of the operators associated to this userGroup.
     *
     * @return a string with the identifiers separated by commas
     */
    public final String getUsersIds() {
        return StringUtils.join(this.usersIds, ',');
    }



    /**
     * Defines the users of this userGroup.
     *
     * @param users an array containing the users that make up this userGroup
     */
    public final void setUsers(final Collection<UserModel> users) {
        this.usersList.clear();
        this.usersList.addAll(users);

        List<String> list = new ArrayList<>();
        for (UserModel user : this.usersList) {
            list.add(user.getId().toString());
        }
        this.usersIds = list.toArray(new String[]{});
    }



    /**
     * Defines the identifiers of the operators associated with this userGroup.
     *
     * @param joinedUsersIds a string with the operator identifiers separated by commas
     */
    public final void setUsersIds(final String joinedUsersIds) {
        this.usersIds = joinedUsersIds.split(",");
    }



    /**
     * Obtains whether this userGroup can be removed from the data source.
     *
     * @return <code>true</code> if this userGroup is in a state that allows its deletion
     */
    public final boolean isAssociatedToProcesses() {
        return this.processesList.size() > 0;
    }



    /**
     * Creates a new instance of this model.
     */
    public UserGroupModel() {
        this.beingCreated = true;
        this.processesList = new ArrayList<>();
        this.usersList = new ArrayList<>();
    }



    /**
     * Creates a new instance of this model.
     *
     * @param userGroupId   the number that uniquely identifies the user group that this model represents
     * @param userGroupName the name of the user group that this model represents
     */
    public UserGroupModel(final int userGroupId, final String userGroupName) {
        this();

        this.setId(userGroupId);
        this.setName(userGroupName);
    }



    /**
     * Creates a new user group model instance.
     *
     * @param domainUserGroup         the data object for the user group to represent
     */
    public UserGroupModel(final UserGroup domainUserGroup) {
        this();

        if (domainUserGroup == null) {
            throw new IllegalArgumentException("The userGroup data object cannot be null.");
        }

        this.setId(domainUserGroup.getId());
        this.setName(domainUserGroup.getName());
        this.setProcesses(domainUserGroup.getProcessesCollection());
        this.setUsersFromDomainObject(domainUserGroup);
        this.setBeingCreated(false);
    }



    public final UserGroup createInDataSource(UserGroupsRepository userGroupsRepository,
                                              UsersRepository usersRepository) {
        UserGroup domainUserGroup = this.createDomainObject(usersRepository);

        return this.saveInDataSource(userGroupsRepository, domainUserGroup);
    }



    private final UserGroup saveInDataSource(UserGroupsRepository userGroupsRepository,
                                             UserGroup domainUserGroup) {
        domainUserGroup = userGroupsRepository.save(domainUserGroup);
        this.setId(domainUserGroup.getId());

        return domainUserGroup;
    }



    public final UserGroup updateInDataSource(UserGroupsRepository userGroupsRepository,
                                              UsersRepository usersRepository,
                                              UserGroup domainUserGroup) {
        domainUserGroup = this.updateDomainObject(domainUserGroup, usersRepository);

        return this.saveInDataSource(userGroupsRepository, domainUserGroup);
    }



    /**
     * Make a new user group data object based on the current values in this model.
     *
     * @param usersRepository the object that links the user data object to the data source
     * @return the new user group data object
     */
    public final UserGroup createDomainObject(UsersRepository usersRepository) {
        final UserGroup domainUserGroup = new UserGroup();
        domainUserGroup.setId(this.getId());

        return this.updateDomainObject(domainUserGroup, usersRepository);
    }



    /**
     * Reports the current values in this model to the data object for the represented user group.
     *
     * @param domainUserGroup the represented user group data object
     * @param usersRepository the object that links the user data object to the data source
     * @return the updated data object
     */
    public final UserGroup updateDomainObject(final UserGroup domainUserGroup,
                                              UsersRepository usersRepository) {

        if (domainUserGroup == null) {
            throw new IllegalArgumentException("The userGroup data object cannot be null.");
        }

        domainUserGroup.setName(this.getName());
        this.copyUsers(domainUserGroup, usersRepository);

        return domainUserGroup;
    }



    /**
     * Defines the user group operators in this model based on what is in the data source.
     *
     * @param domainUserGroup the data object for this user group
     */
    private void setUsersFromDomainObject(final UserGroup domainUserGroup) {
        assert domainUserGroup != null : "The userGroup data object must not be null.";

        List<String> usersIdsList = new ArrayList<>();

        for (User user : domainUserGroup.getUsersCollection()) {
            this.usersList.add(new UserModel(user));
            usersIdsList.add(user.getId().toString());
        }
        this.usersIds = usersIdsList.toArray(String[]::new);
    }



    /**
     * Creates models to represent a collection of userGroup data objects.
     *
     * @param domainObjectsCollection the userGroup data objects to represent
     * @return a collection that contains the models representing the data objects
     */
    public static Collection<UserGroupModel> fromDomainObjectsCollection(
            final Iterable<UserGroup> domainObjectsCollection) {

        if (domainObjectsCollection == null) {
            throw new IllegalArgumentException("The collection of userGroup data objects cannot be null.");
        }

        List<UserGroupModel> modelsList = new ArrayList<>();

        for (UserGroup domainUserGroup : domainObjectsCollection) {
            modelsList.add(new UserGroupModel(domainUserGroup));
        }

        return modelsList;
    }



    private void copyUsers(UserGroup domainUserGroup, UsersRepository usersRepository) {
        final Collection<User> usersCollection = new ArrayList<>();
        final String userIds = this.getUsersIds();

        if (!StringUtils.isEmpty(userIds)) {

            for (String userId : userIds.split(",")) {
                Optional<User> user = usersRepository.findById(NumberUtils.toInt(userId));

                if (!user.isPresent()) {
                    this.logger.warn("Could not find a user to copy with id {}", userId);
                }

                usersCollection.add(user.get());
            }
        }

        domainUserGroup.setUsersCollection(usersCollection);
    }

}
