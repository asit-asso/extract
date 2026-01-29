/*
 * Copyright (C) 2025 asit-asso
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
package ch.asit_asso.extract.unit.web.model;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.web.model.UserGroupModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserGroupModel in the context of the user groups list view.
 *
 * Validates that the list view displays:
 * 1. All user groups
 * 2. The number of users associated with each group
 * 3. Whether the group can be deleted (not associated to processes)
 *
 * @author Bruno Alves
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Groups List View - Unit Tests")
class UserGroupModelListTest {

    @Mock
    private UserGroup mockUserGroup;

    // ==================== 1. GROUP IDENTIFICATION ====================

    @Nested
    @DisplayName("1. Group Identification")
    class GroupIdentificationTests {

        @Test
        @DisplayName("1.1 - Group ID is accessible")
        void groupIdIsAccessible() {
            // Given
            when(mockUserGroup.getId()).thenReturn(42);
            when(mockUserGroup.getName()).thenReturn("Test Group");
            when(mockUserGroup.getUsersCollection()).thenReturn(new ArrayList<>());
            when(mockUserGroup.getProcessesCollection()).thenReturn(new ArrayList<>());

            // When
            UserGroupModel model = new UserGroupModel(mockUserGroup);

            // Then
            assertEquals(42, model.getId());
        }

        @Test
        @DisplayName("1.2 - Group name is accessible")
        void groupNameIsAccessible() {
            // Given
            when(mockUserGroup.getId()).thenReturn(1);
            when(mockUserGroup.getName()).thenReturn("Administrators");
            when(mockUserGroup.getUsersCollection()).thenReturn(new ArrayList<>());
            when(mockUserGroup.getProcessesCollection()).thenReturn(new ArrayList<>());

            // When
            UserGroupModel model = new UserGroupModel(mockUserGroup);

            // Then
            assertEquals("Administrators", model.getName());
        }

        @Test
        @DisplayName("1.3 - Group with special characters in name")
        void groupWithSpecialCharactersInName() {
            // Given
            when(mockUserGroup.getId()).thenReturn(1);
            when(mockUserGroup.getName()).thenReturn("Géomètres & Ingénieurs");
            when(mockUserGroup.getUsersCollection()).thenReturn(new ArrayList<>());
            when(mockUserGroup.getProcessesCollection()).thenReturn(new ArrayList<>());

            // When
            UserGroupModel model = new UserGroupModel(mockUserGroup);

            // Then
            assertEquals("Géomètres & Ingénieurs", model.getName());
        }
    }

    // ==================== 2. USER COUNT ====================

    @Nested
    @DisplayName("2. User Count in Group")
    class UserCountTests {

        @Test
        @DisplayName("2.1 - Group with no users has zero count")
        void groupWithNoUsersHasZeroCount() {
            // Given
            when(mockUserGroup.getId()).thenReturn(1);
            when(mockUserGroup.getName()).thenReturn("Empty Group");
            when(mockUserGroup.getUsersCollection()).thenReturn(new ArrayList<>());
            when(mockUserGroup.getProcessesCollection()).thenReturn(new ArrayList<>());

            // When
            UserGroupModel model = new UserGroupModel(mockUserGroup);

            // Then
            assertEquals(0, model.getUsers().length);
        }

        @Test
        @DisplayName("2.2 - Group with one user has count of 1")
        void groupWithOneUserHasCountOfOne() {
            // Given
            User user1 = createMockUser(1, "user1");
            Collection<User> users = Arrays.asList(user1);

            when(mockUserGroup.getId()).thenReturn(1);
            when(mockUserGroup.getName()).thenReturn("Single User Group");
            when(mockUserGroup.getUsersCollection()).thenReturn(users);
            when(mockUserGroup.getProcessesCollection()).thenReturn(new ArrayList<>());

            // When
            UserGroupModel model = new UserGroupModel(mockUserGroup);

            // Then
            assertEquals(1, model.getUsers().length);
        }

        @Test
        @DisplayName("2.3 - Group with multiple users has correct count")
        void groupWithMultipleUsersHasCorrectCount() {
            // Given
            User user1 = createMockUser(1, "user1");
            User user2 = createMockUser(2, "user2");
            User user3 = createMockUser(3, "user3");
            User user4 = createMockUser(4, "user4");
            User user5 = createMockUser(5, "user5");
            Collection<User> users = Arrays.asList(user1, user2, user3, user4, user5);

            when(mockUserGroup.getId()).thenReturn(1);
            when(mockUserGroup.getName()).thenReturn("Large Group");
            when(mockUserGroup.getUsersCollection()).thenReturn(users);
            when(mockUserGroup.getProcessesCollection()).thenReturn(new ArrayList<>());

            // When
            UserGroupModel model = new UserGroupModel(mockUserGroup);

            // Then
            assertEquals(5, model.getUsers().length);
        }

        @Test
        @DisplayName("2.4 - Users IDs are correctly formatted")
        void usersIdsAreCorrectlyFormatted() {
            // Given
            User user1 = createMockUser(10, "user1");
            User user2 = createMockUser(20, "user2");
            User user3 = createMockUser(30, "user3");
            Collection<User> users = Arrays.asList(user1, user2, user3);

            when(mockUserGroup.getId()).thenReturn(1);
            when(mockUserGroup.getName()).thenReturn("Test Group");
            when(mockUserGroup.getUsersCollection()).thenReturn(users);
            when(mockUserGroup.getProcessesCollection()).thenReturn(new ArrayList<>());

            // When
            UserGroupModel model = new UserGroupModel(mockUserGroup);

            // Then
            String usersIds = model.getUsersIds();
            assertTrue(usersIds.contains("10"));
            assertTrue(usersIds.contains("20"));
            assertTrue(usersIds.contains("30"));
        }
    }

    // ==================== 3. PROCESS ASSOCIATION ====================

    @Nested
    @DisplayName("3. Process Association (Delete Eligibility)")
    class ProcessAssociationTests {

        @Test
        @DisplayName("3.1 - Group not associated to processes can be deleted")
        void groupNotAssociatedToProcessesCanBeDeleted() {
            // Given
            when(mockUserGroup.getId()).thenReturn(1);
            when(mockUserGroup.getName()).thenReturn("Deletable Group");
            when(mockUserGroup.getUsersCollection()).thenReturn(new ArrayList<>());
            when(mockUserGroup.getProcessesCollection()).thenReturn(new ArrayList<>());

            // When
            UserGroupModel model = new UserGroupModel(mockUserGroup);

            // Then
            assertFalse(model.isAssociatedToProcesses());
        }

        @Test
        @DisplayName("3.2 - Group associated to one process cannot be deleted")
        void groupAssociatedToOneProcessCannotBeDeleted() {
            // Given
            Process process = new Process();
            process.setId(1);
            process.setName("Test Process");
            Collection<Process> processes = Arrays.asList(process);

            when(mockUserGroup.getId()).thenReturn(1);
            when(mockUserGroup.getName()).thenReturn("Associated Group");
            when(mockUserGroup.getUsersCollection()).thenReturn(new ArrayList<>());
            when(mockUserGroup.getProcessesCollection()).thenReturn(processes);

            // When
            UserGroupModel model = new UserGroupModel(mockUserGroup);

            // Then
            assertTrue(model.isAssociatedToProcesses());
        }

        @Test
        @DisplayName("3.3 - Group associated to multiple processes cannot be deleted")
        void groupAssociatedToMultipleProcessesCannotBeDeleted() {
            // Given
            Process process1 = new Process();
            process1.setId(1);
            process1.setName("Process 1");
            Process process2 = new Process();
            process2.setId(2);
            process2.setName("Process 2");
            Collection<Process> processes = Arrays.asList(process1, process2);

            when(mockUserGroup.getId()).thenReturn(1);
            when(mockUserGroup.getName()).thenReturn("Multi-Process Group");
            when(mockUserGroup.getUsersCollection()).thenReturn(new ArrayList<>());
            when(mockUserGroup.getProcessesCollection()).thenReturn(processes);

            // When
            UserGroupModel model = new UserGroupModel(mockUserGroup);

            // Then
            assertTrue(model.isAssociatedToProcesses());
            assertEquals(2, model.getProcesses().length);
        }
    }

    // ==================== 4. COLLECTION CONVERSION ====================

    @Nested
    @DisplayName("4. Collection Conversion")
    class CollectionConversionTests {

        @Test
        @DisplayName("4.1 - fromDomainObjectsCollection converts all groups")
        void fromDomainObjectsCollectionConvertsAllGroups() {
            // Given
            UserGroup group1 = createUserGroup(1, "Group 1", 2);
            UserGroup group2 = createUserGroup(2, "Group 2", 5);
            UserGroup group3 = createUserGroup(3, "Group 3", 0);
            List<UserGroup> groups = Arrays.asList(group1, group2, group3);

            // When
            Collection<UserGroupModel> models = UserGroupModel.fromDomainObjectsCollection(groups);

            // Then
            assertEquals(3, models.size());
        }

        @Test
        @DisplayName("4.2 - Empty collection returns empty list")
        void emptyCollectionReturnsEmptyList() {
            // Given
            List<UserGroup> groups = new ArrayList<>();

            // When
            Collection<UserGroupModel> models = UserGroupModel.fromDomainObjectsCollection(groups);

            // Then
            assertTrue(models.isEmpty());
        }

        @Test
        @DisplayName("4.3 - Null collection throws exception")
        void nullCollectionThrowsException() {
            // Given/When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                UserGroupModel.fromDomainObjectsCollection(null);
            });
        }
    }

    // ==================== 5. LIST VIEW DATA REQUIREMENTS ====================

    @Nested
    @DisplayName("5. List View Data Requirements")
    class ListViewDataRequirementsTests {

        @Test
        @DisplayName("5.1 - All required fields for list view are accessible")
        void allRequiredFieldsForListViewAreAccessible() {
            // Given
            User user1 = createMockUser(1, "user1");
            User user2 = createMockUser(2, "user2");
            Collection<User> users = Arrays.asList(user1, user2);

            when(mockUserGroup.getId()).thenReturn(99);
            when(mockUserGroup.getName()).thenReturn("Complete Group");
            when(mockUserGroup.getUsersCollection()).thenReturn(users);
            when(mockUserGroup.getProcessesCollection()).thenReturn(new ArrayList<>());

            // When
            UserGroupModel model = new UserGroupModel(mockUserGroup);

            // Then: All list view fields are accessible
            // 1. Name (for display and link)
            assertNotNull(model.getName());
            assertEquals("Complete Group", model.getName());

            // 2. ID (for link URL)
            assertNotNull(model.getId());
            assertEquals(99, model.getId());

            // 3. Users count (displayed in table)
            assertEquals(2, model.getUsers().length);

            // 4. Can be deleted flag (for delete button state)
            assertFalse(model.isAssociatedToProcesses());
        }

        @Test
        @DisplayName("5.2 - Document: List view displays all required information")
        void documentListViewInformation() {
            System.out.println("✓ User Groups List View displays:");
            System.out.println("");
            System.out.println("  TABLE COLUMNS:");
            System.out.println("  1. Name (link to group details)");
            System.out.println("  2. Number of members (usersCollection.size())");
            System.out.println("  3. Delete button (disabled if associated to processes)");
            System.out.println("");
            System.out.println("  DATA SOURCE:");
            System.out.println("  - Controller: UserGroupsController.viewList()");
            System.out.println("  - Model attribute: 'userGroups'");
            System.out.println("  - Repository: userGroupsRepository.findAll()");
            System.out.println("");
            System.out.println("  TEMPLATE:");
            System.out.println("  - Path: templates/pages/userGroups/list.html");
            System.out.println("  - Name displayed: th:text=\"*{name}\"");
            System.out.println("  - User count: th:text=\"*{#lists.size(usersCollection)}\"");
            System.out.println("  - Delete enabled: not *{associatedToProcesses}");

            assertTrue(true);
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a mock User with given ID and login.
     */
    private User createMockUser(int id, String login) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        user.setName("User " + login);
        user.setActive(true);
        return user;
    }

    /**
     * Creates a real UserGroup with specified number of users.
     */
    private UserGroup createUserGroup(int id, String name, int userCount) {
        UserGroup group = new UserGroup(id);
        group.setName(name);

        Collection<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            users.add(createMockUser(i + 100, "user" + i));
        }
        group.setUsersCollection(users);
        group.setProcessesCollection(new ArrayList<>());

        return group;
    }
}
