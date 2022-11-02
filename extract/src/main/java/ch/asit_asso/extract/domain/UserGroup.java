/*
 * Copyright (C) 2022 arx iT
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
package ch.asit_asso.extract.domain;

import jakarta.xml.bind.annotation.XmlRootElement;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serial;
import java.util.Collection;


/**
 * An entity used to affect permissions to a set of users.
 */
@Entity
@Table(name="USERGROUP", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "UNQ_USERGROUP_NAME")
})
@XmlRootElement
public class UserGroup {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The number that uniquely identifies this user group.
     */
    @Id
    @Basic(optional = false)
    @GeneratedValue
    @NotNull
    @Column(name = "id_usergroup")
    private Integer id;

    /**
     * The name of this user group.
     */
    @Size(max = 50)
    @Column(name = "name")
    private String name;

    /**
     * The users that are members of this group.
     */
    @JoinTable(name = "users_usergroups",
            joinColumns = {
                    @JoinColumn(name = "id_usergroup", referencedColumnName = "id_usergroup",
                            foreignKey = @ForeignKey(name = "FK_USERGROUPS_USERS_USERGROUP")
                    )
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "id_user", referencedColumnName = "id_user",
                            foreignKey = @ForeignKey(name = "FK_USERGROUPS_USERS_USER")
                    )
            }
    )
    @ManyToMany
    private Collection<User> usersCollection;



    /**
     * The request processings that the user of this group can operate.
     */
    @ManyToMany(mappedBy = "userGroupsCollection")
    private Collection<Process> processesCollection;


    public UserGroup() { }



    public UserGroup(final Integer identifier) { this.id = identifier; }



    public Integer getId() {
        return id;
    }



    public void setId(final Integer id) {
        this.id = id;
    }



    public String getName() {
        return this.name;
    }



    public void setName(final String name) {
        this.name = name;
    }



    public Collection<Process> getProcessesCollection() {
        return this.processesCollection;
    }



    public void setProcessesCollection(final Collection<Process> processesCollection) {
        this.processesCollection = processesCollection;
    }



    public Collection<User> getUsersCollection() {
        return this.usersCollection;
    }



    public void setUsersCollection(final Collection<User> usersCollection) {
        this.usersCollection = usersCollection;
    }

    public boolean isAssociatedToProcesses() {
        return this.processesCollection.size() > 0;
    }
}
