package ch.asit_asso.extract.web.model;

import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/*
 * Copyright (C) 2025 arusakov
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

/**
 *
 * @author arusakov
 */
public class OwnedObjectModel {
    
    /**
     * The operators groups associated to this process.
     */
    private final List<UserGroup> userGroupsList;

    /**
     * An array that contains the identifiers of the operators groups associated with this process.
     */
    private String[] userGroupsIds;

    /**
     * The operators associated to this process.
     */
    private final List<UserModel> usersList;

    /**
     * An array that contains the identifiers of the operators associated with this process.
     */
    private String[] usersIds;
    
    
    public OwnedObjectModel() {
        this.userGroupsList = new ArrayList<>();
        this.usersList = new ArrayList<>();
    }
    
    public OwnedObjectModel(Collection<User> users, Collection<UserGroup> userGroups) {
        this();
        setUsersFromDomainObject(users);
        setUserGroupsFromDomainObject(userGroups);
    }
    /**
     * Defines the users of this process.
     *
     * @param users an array containing the operators directly attached to this process
     */
    public final void setUsers(final UserModel[] users) {
        this.usersList.clear();
        this.usersList.addAll(Arrays.asList(users));

        List<String> list = new ArrayList<>();
        for (UserModel user : this.usersList) {
            list.add(user.getId().toString());
        }
        this.usersIds = list.toArray(String[]::new);
    }



    /**
     * Defines the identifiers of the operators associated with this process.
     *
     * @param joinedUsersIds a string with the operator identifiers separated by commas
     */
    public final void setUsersIds(final String joinedUsersIds) {
        this.usersIds = joinedUsersIds.split(",");
    }
    
    /**
     * Obtains the identifiers of the operators associated to this process.
     *
     * @return a string with the identifiers separated by commas
     */
    public final String getUsersIds() {
        return StringUtils.join(this.usersIds, ',');
    }


    /**
     * Obtains the users of this process.
     *
     * @return an array containing the users that make up this process
     */
    public final UserModel[] getUsers() {
        return this.usersList.toArray(UserModel[]::new);
    }
    
    /**
     * Defines the process operators in this model based on what is in the data source.
     *
     * @param domainProcess the data object for this process
     */
    final void setUsersFromDomainObject(final Collection<User> domainObjectUsers) {
        assert domainObjectUsers != null : "The process data object must not be null.";

        List<String> usersIdsList = new ArrayList<>();
        for (User user : domainObjectUsers) {
            this.usersList.add(new UserModel(user));
            usersIdsList.add(user.getId().toString());
        }
        this.usersIds = usersIdsList.toArray(String[]::new);
    }

    /**
     * Obtains the identifiers of the operators groups associated to this process.
     *
     * @return a string with the identifiers separated by commas
     */
    public final String getUserGroupsIds() {
        return StringUtils.join(this.userGroupsIds, ',');
    }
    
    /**
     * Defines the process operators groups in this model based on what is in the data source.
     *
     * @param domainRequest the data object for this process
     */
    final void setUserGroupsFromDomainObject(Collection<UserGroup> domainObjectUserGroups) {
        assert domainObjectUserGroups != null : "The process data object must not be null.";

        List<String> userGroupsIdsList = new ArrayList<>();

        for (UserGroup userGroup : domainObjectUserGroups) {
            this.userGroupsList.add(userGroup);
            userGroupsIdsList.add(userGroup.getId().toString());
        }
        this.userGroupsIds = userGroupsIdsList.toArray(String[]::new);
    }

    /**
     * Defines the users groups of this process.
     *
     * @param userGroups an array containing the user groups that operate on this process
     */
    public final void setUserGroups(final UserGroup[] userGroups) {
        this.userGroupsList.clear();
        this.userGroupsList.addAll(List.of(userGroups));

        List<String> list = new ArrayList<>();

        for (UserGroup userGroup : this.userGroupsList) {
            list.add(userGroup.getId().toString());
        }
        this.usersIds = list.toArray(String[]::new);
    }



    /**
     * Defines the identifiers of the operators groups associated with this process.
     *
     * @param joinedUserGroupsIds a string with the operator group identifiers separated by commas
     */
    public final void setUserGroupsIds(final String joinedUserGroupsIds) {
        this.userGroupsIds = joinedUserGroupsIds.split(",");
    }
    
}
