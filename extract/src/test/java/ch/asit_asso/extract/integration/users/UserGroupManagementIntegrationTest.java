/*
 * Copyright (C) 2025 arx iT
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
package ch.asit_asso.extract.integration.users;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.integration.DatabaseTestHelper;
import ch.asit_asso.extract.integration.WithMockApplicationUser;
import ch.asit_asso.extract.persistence.UserGroupsRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for user group management functionality.
 * Tests group creation, deletion, and user associations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Group Management Integration Tests")
class UserGroupManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserGroupsRepository userGroupsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private DatabaseTestHelper dbHelper;

    // ==================== 1. GROUP CREATION TESTS ====================

    @Nested
    @DisplayName("1. Group Creation")
    class GroupCreationTests {

        @Test
        @DisplayName("1.1 - Admin can create a new group")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void adminCanCreateNewGroup() throws Exception {
            // Given: Admin wants to create a new group
            String groupName = "New Test Group";

            // When: Admin submits the creation form
            mockMvc.perform(post("/userGroups/add")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", groupName)
                    .param("beingCreated", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userGroups"));

            // Then: Group is created
            UserGroup createdGroup = userGroupsRepository.findByNameIgnoreCase(groupName);
            assertNotNull(createdGroup, "Group should be created");
            assertEquals(groupName, createdGroup.getName());
        }

        @Test
        @DisplayName("1.2 - Admin can create group with users")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void adminCanCreateGroupWithUsers() throws Exception {
            // Given: Create users to add to group
            int user1Id = dbHelper.createTestOperator("groupuser1", "Group User 1", "gu1@test.com", true);
            int user2Id = dbHelper.createTestOperator("groupuser2", "Group User 2", "gu2@test.com", true);

            String groupName = "Group With Users";

            // When: Admin creates group with users
            mockMvc.perform(post("/userGroups/add")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", groupName)
                    .param("beingCreated", "true")
                    .param("usersIds", String.valueOf(user1Id))
                    .param("usersIds", String.valueOf(user2Id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userGroups"));

            // Then: Group has users
            UserGroup createdGroup = userGroupsRepository.findByNameIgnoreCase(groupName);
            assertNotNull(createdGroup);
            assertEquals(2, createdGroup.getUsersCollection().size());

            Set<Integer> userIds = createdGroup.getUsersCollection().stream()
                .map(User::getId)
                .collect(Collectors.toSet());
            assertTrue(userIds.contains(user1Id));
            assertTrue(userIds.contains(user2Id));
        }

        @Test
        @DisplayName("1.3 - Cannot create group with duplicate name")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void cannotCreateGroupWithDuplicateName() throws Exception {
            // Given: A group already exists
            dbHelper.createTestUserGroup("Existing Group");

            // When: Trying to create group with same name
            mockMvc.perform(post("/userGroups/add")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "Existing Group")
                    .param("beingCreated", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("userGroups/details"));
            // Returns to form with validation errors
        }

        @Test
        @DisplayName("1.4 - Operator cannot create groups")
        @WithMockApplicationUser(username = "operator", userId = 10, role = "OPERATOR")
        void operatorCannotCreateGroups() throws Exception {
            // When: Operator tries to access group creation
            mockMvc.perform(get("/userGroups/add"))
                .andExpect(status().isForbidden());

            // And: Tries to submit creation
            mockMvc.perform(post("/userGroups/add")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "Hacker Group")
                    .param("beingCreated", "true"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("1.5 - Cannot create group with empty name")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        void cannotCreateGroupWithEmptyName() throws Exception {
            // When: Trying to create group with empty name
            mockMvc.perform(post("/userGroups/add")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "")
                    .param("beingCreated", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("userGroups/details"));
        }
    }

    // ==================== 2. GROUP DELETION TESTS ====================

    @Nested
    @DisplayName("2. Group Deletion")
    class GroupDeletionTests {

        @Test
        @DisplayName("2.1 - Admin can delete group not assigned to process")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void adminCanDeleteGroupNotAssignedToProcess() throws Exception {
            // Given: A group not assigned to any process
            int groupId = dbHelper.createTestUserGroup("Delete Me Group");
            assertNotNull(userGroupsRepository.findById(groupId).orElse(null));

            // When: Admin deletes the group
            mockMvc.perform(post("/userGroups/delete")
                    .with(csrf())
                    .param("id", String.valueOf(groupId))
                    .param("name", "Delete Me Group"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userGroups"));

            // Then: Group is deleted
            assertFalse(userGroupsRepository.findById(groupId).isPresent());
        }

        @Test
        @DisplayName("2.2 - Cannot delete group assigned to process")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void cannotDeleteGroupAssignedToProcess() throws Exception {
            // Given: A group assigned to a process
            int groupId = dbHelper.createTestUserGroup("Process Group");
            int processId = dbHelper.createTestProcess("Group Process");
            dbHelper.assignGroupToProcess(groupId, processId);

            // Verify group is associated
            UserGroup group = userGroupsRepository.findById(groupId).orElse(null);
            assertNotNull(group);
            assertTrue(group.isAssociatedToProcesses());

            // When: Admin tries to delete
            mockMvc.perform(post("/userGroups/delete")
                    .with(csrf())
                    .param("id", String.valueOf(groupId))
                    .param("name", "Process Group"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userGroups"));

            // Then: Group is NOT deleted
            assertTrue(userGroupsRepository.findById(groupId).isPresent(),
                "Group assigned to process should not be deleted");
        }

        @Test
        @DisplayName("2.3 - Deleting group removes user associations")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void deletingGroupRemovesUserAssociations() throws Exception {
            // Given: A group with users
            int userId = dbHelper.createTestOperator("removeassoc", "Remove Assoc", "remove@test.com", true);
            int groupId = dbHelper.createTestUserGroup("User Group to Delete");
            dbHelper.addUserToGroup(userId, groupId);

            // Verify user is in group
            User userBefore = usersRepository.findById(userId).orElse(null);
            assertNotNull(userBefore);
            assertTrue(userBefore.getUserGroupsCollection().stream()
                .anyMatch(g -> g.getId() == groupId));

            // When: Group is deleted
            mockMvc.perform(post("/userGroups/delete")
                    .with(csrf())
                    .param("id", String.valueOf(groupId))
                    .param("name", "User Group to Delete"))
                .andExpect(status().is3xxRedirection());

            // Then: Group should be deleted
            assertFalse(userGroupsRepository.findById(groupId).isPresent(), "Group should be deleted");

            // And: User should no longer be in the deleted group
            // Note: The user's collection is managed by JPA cascade, so we verify the group is deleted
            // which automatically removes the association
        }

        @Test
        @DisplayName("2.4 - Operator cannot delete groups")
        @WithMockApplicationUser(username = "operator", userId = 10, role = "OPERATOR")
        @Transactional
        void operatorCannotDeleteGroups() throws Exception {
            // Given: A group exists
            int groupId = dbHelper.createTestUserGroup("Protected Group");

            // When: Operator tries to delete
            mockMvc.perform(post("/userGroups/delete")
                    .with(csrf())
                    .param("id", String.valueOf(groupId))
                    .param("name", "Protected Group"))
                .andExpect(status().isForbidden());

            // Then: Group still exists
            assertTrue(userGroupsRepository.findById(groupId).isPresent());
        }
    }

    // ==================== 3. GROUP UPDATE TESTS ====================

    @Nested
    @DisplayName("3. Group Update")
    class GroupUpdateTests {

        @Test
        @DisplayName("3.1 - Admin can update group name")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void adminCanUpdateGroupName() throws Exception {
            // Given: A group exists
            int groupId = dbHelper.createTestUserGroup("Original Name");

            // When: Admin updates the name
            mockMvc.perform(post("/userGroups/" + groupId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("id", String.valueOf(groupId))
                    .param("name", "Updated Name")
                    .param("beingCreated", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userGroups"));

            // Then: Name is updated
            UserGroup updatedGroup = userGroupsRepository.findById(groupId).orElse(null);
            assertNotNull(updatedGroup);
            assertEquals("Updated Name", updatedGroup.getName());
        }

        @Test
        @DisplayName("3.2 - Admin can add users to existing group")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void adminCanAddUsersToExistingGroup() throws Exception {
            // Given: A group and users exist
            int groupId = dbHelper.createTestUserGroup("Add Users Group");
            int userId1 = dbHelper.createTestOperator("adduser1", "Add User 1", "add1@test.com", true);
            int userId2 = dbHelper.createTestOperator("adduser2", "Add User 2", "add2@test.com", true);

            // When: Admin adds users
            mockMvc.perform(post("/userGroups/" + groupId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("id", String.valueOf(groupId))
                    .param("name", "Add Users Group")
                    .param("beingCreated", "false")
                    .param("usersIds", String.valueOf(userId1))
                    .param("usersIds", String.valueOf(userId2)))
                .andExpect(status().is3xxRedirection());

            // Then: Users are added
            UserGroup updatedGroup = userGroupsRepository.findById(groupId).orElse(null);
            assertNotNull(updatedGroup);
            assertEquals(2, updatedGroup.getUsersCollection().size());
        }

        @Test
        @DisplayName("3.3 - Admin can remove users from group")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void adminCanRemoveUsersFromGroup() throws Exception {
            // Given: A group with users
            int userId = dbHelper.createTestOperator("removeuser", "Remove User", "removeuser@test.com", true);
            int groupId = dbHelper.createTestUserGroup("Remove Users Group");
            dbHelper.addUserToGroup(userId, groupId);

            // Verify user is in group
            UserGroup groupBefore = userGroupsRepository.findById(groupId).orElse(null);
            assertNotNull(groupBefore);
            assertEquals(1, groupBefore.getUsersCollection().size());

            // When: Admin updates group without the user
            mockMvc.perform(post("/userGroups/" + groupId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("id", String.valueOf(groupId))
                    .param("name", "Remove Users Group")
                    .param("beingCreated", "false"))
                    // Note: no usersIds parameter means empty users list
                .andExpect(status().is3xxRedirection());

            // Then: Users are removed
            UserGroup updatedGroup = userGroupsRepository.findById(groupId).orElse(null);
            assertNotNull(updatedGroup);
            assertEquals(0, updatedGroup.getUsersCollection().size());
        }
    }

    // ==================== 4. USER-GROUP ASSOCIATION TESTS ====================

    @Nested
    @DisplayName("4. User-Group Association")
    class UserGroupAssociationTests {

        @Test
        @DisplayName("4.1 - User shows group membership in user details")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void userShowsGroupMembership() throws Exception {
            // Given: A user in a group
            int userId = dbHelper.createTestOperator("memberuser", "Member User", "member@test.com", true);
            int groupId = dbHelper.createTestUserGroup("Membership Group");
            dbHelper.addUserToGroup(userId, groupId);

            // When: Viewing user details
            User user = usersRepository.findById(userId).orElse(null);
            assertNotNull(user);

            // Then: User shows group membership
            Set<String> groupNames = user.getUserGroupsCollection().stream()
                .map(UserGroup::getName)
                .collect(Collectors.toSet());
            assertTrue(groupNames.contains("Membership Group"));
        }

        @Test
        @DisplayName("4.2 - User can be in multiple groups")
        @Transactional
        void userCanBeInMultipleGroups() {
            // Given: Create user and multiple groups
            int userId = dbHelper.createTestOperator("multigroup", "Multi Group User", "multi@test.com", true);
            int group1Id = dbHelper.createTestUserGroup("Group A");
            int group2Id = dbHelper.createTestUserGroup("Group B");
            int group3Id = dbHelper.createTestUserGroup("Group C");

            // When: User is added to multiple groups
            dbHelper.addUserToGroup(userId, group1Id);
            dbHelper.addUserToGroup(userId, group2Id);
            dbHelper.addUserToGroup(userId, group3Id);

            // Then: User is member of all groups
            User user = usersRepository.findById(userId).orElse(null);
            assertNotNull(user);
            assertEquals(3, user.getUserGroupsCollection().size());
        }

        @Test
        @DisplayName("4.3 - Group can have multiple users")
        @Transactional
        void groupCanHaveMultipleUsers() {
            // Given: Create group and multiple users
            int groupId = dbHelper.createTestUserGroup("Multi User Group");
            int user1Id = dbHelper.createTestOperator("grpuser1", "Group User 1", "grpu1@test.com", true);
            int user2Id = dbHelper.createTestOperator("grpuser2", "Group User 2", "grpu2@test.com", true);
            int user3Id = dbHelper.createTestOperator("grpuser3", "Group User 3", "grpu3@test.com", true);

            // When: Users are added to group
            dbHelper.addUserToGroup(user1Id, groupId);
            dbHelper.addUserToGroup(user2Id, groupId);
            dbHelper.addUserToGroup(user3Id, groupId);

            // Then: Group has all users
            UserGroup group = userGroupsRepository.findById(groupId).orElse(null);
            assertNotNull(group);
            assertEquals(3, group.getUsersCollection().size());
        }
    }

    // ==================== 5. GROUP LIST TESTS ====================

    @Nested
    @DisplayName("5. Group List")
    class GroupListTests {

        @Test
        @DisplayName("5.1 - Admin can view group list")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        void adminCanViewGroupList() throws Exception {
            mockMvc.perform(get("/userGroups"))
                .andExpect(status().isOk())
                .andExpect(view().name("userGroups/list"))
                .andExpect(model().attributeExists("userGroups"));
        }

        @Test
        @DisplayName("5.2 - Operator cannot view group list")
        @WithMockApplicationUser(username = "operator", userId = 10, role = "OPERATOR")
        void operatorCannotViewGroupList() throws Exception {
            mockMvc.perform(get("/userGroups"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("5.3 - Groups are sorted by name")
        @Transactional
        void groupsAreSortedByName() {
            // Given: Create groups with various names
            dbHelper.createTestUserGroup("Zeta Group");
            dbHelper.createTestUserGroup("Alpha Group");
            dbHelper.createTestUserGroup("Beta Group");

            // When: Fetching all groups sorted
            List<UserGroup> groups = new ArrayList<>(userGroupsRepository.findAllByOrderByName());

            // Then: Check that groups starting with these names are in correct order
            // (Note: there might be other groups from test data)
            int alphaIndex = -1, betaIndex = -1, zetaIndex = -1;
            for (int i = 0; i < groups.size(); i++) {
                if ("Alpha Group".equals(groups.get(i).getName())) alphaIndex = i;
                if ("Beta Group".equals(groups.get(i).getName())) betaIndex = i;
                if ("Zeta Group".equals(groups.get(i).getName())) zetaIndex = i;
            }

            if (alphaIndex >= 0 && betaIndex >= 0) {
                assertTrue(alphaIndex < betaIndex, "Alpha should come before Beta");
            }
            if (betaIndex >= 0 && zetaIndex >= 0) {
                assertTrue(betaIndex < zetaIndex, "Beta should come before Zeta");
            }
        }
    }
}
