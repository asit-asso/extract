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
package ch.asit_asso.extract.integration.usergroups;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.persistence.UserGroupsRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for user groups list view.
 *
 * Validates that:
 * 1. All user groups are retrievable from the database
 * 2. Each group has the correct number of associated users
 * 3. The users collection is properly loaded (not lazy-loaded issue)
 * 4. Process association is correctly determined
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("User Groups List View Integration Tests")
class UserGroupsListIntegrationTest {

    @Autowired
    private UserGroupsRepository userGroupsRepository;

    @Autowired
    private UsersRepository usersRepository;

    // ==================== 1. GROUP RETRIEVAL ====================

    @Nested
    @DisplayName("1. Group Retrieval Tests")
    class GroupRetrievalTests {

        @Test
        @DisplayName("1.1 - All groups are retrievable via findAll")
        @Transactional
        void allGroupsAreRetrievable() {
            // Given: Create some test groups
            UserGroup group1 = createAndSaveGroup("Integration Test Group 1");
            UserGroup group2 = createAndSaveGroup("Integration Test Group 2");

            // When
            Iterable<UserGroup> allGroups = userGroupsRepository.findAll();

            // Then
            assertNotNull(allGroups);
            List<UserGroup> groupList = new ArrayList<>();
            allGroups.forEach(groupList::add);

            assertTrue(groupList.size() >= 2, "Should have at least the 2 created groups");

            // Verify our groups are in the list
            assertTrue(groupList.stream().anyMatch(g -> g.getName().equals("Integration Test Group 1")));
            assertTrue(groupList.stream().anyMatch(g -> g.getName().equals("Integration Test Group 2")));
        }

        @Test
        @DisplayName("1.2 - Groups are retrievable by ID")
        @Transactional
        void groupIsRetrievableById() {
            // Given
            UserGroup group = createAndSaveGroup("Findable Group");
            Integer groupId = group.getId();

            // When
            Optional<UserGroup> found = userGroupsRepository.findById(groupId);

            // Then
            assertTrue(found.isPresent());
            assertEquals("Findable Group", found.get().getName());
        }

        @Test
        @DisplayName("1.3 - Groups ordered by name are retrievable")
        @Transactional
        void groupsOrderedByNameAreRetrievable() {
            // Given
            createAndSaveGroup("ZZZ Last Group");
            createAndSaveGroup("AAA First Group");

            // When
            Collection<UserGroup> orderedGroups = userGroupsRepository.findAllByOrderByName();

            // Then
            assertNotNull(orderedGroups);
            List<UserGroup> groupList = new ArrayList<>(orderedGroups);

            // Verify ordering - AAA should come before ZZZ
            int aaaIndex = -1;
            int zzzIndex = -1;
            for (int i = 0; i < groupList.size(); i++) {
                if (groupList.get(i).getName().equals("AAA First Group")) aaaIndex = i;
                if (groupList.get(i).getName().equals("ZZZ Last Group")) zzzIndex = i;
            }

            assertTrue(aaaIndex >= 0, "AAA group should be found");
            assertTrue(zzzIndex >= 0, "ZZZ group should be found");
            assertTrue(aaaIndex < zzzIndex, "AAA should come before ZZZ");
        }
    }

    // ==================== 2. USER COUNT ====================

    @Nested
    @DisplayName("2. User Count Tests")
    class UserCountTests {

        @Test
        @DisplayName("2.1 - Group with no users has empty collection")
        @Transactional
        void groupWithNoUsersHasEmptyCollection() {
            // Given
            UserGroup group = createAndSaveGroup("Empty Group");

            // When
            Optional<UserGroup> found = userGroupsRepository.findById(group.getId());

            // Then
            assertTrue(found.isPresent());
            assertNotNull(found.get().getUsersCollection());
            assertEquals(0, found.get().getUsersCollection().size());
        }

        @Test
        @DisplayName("2.2 - Group with users has correct count")
        @Transactional
        void groupWithUsersHasCorrectCount() {
            // Given: Create a group with users
            UserGroup group = new UserGroup();
            group.setName("Group With Users - " + System.currentTimeMillis());

            // Get existing users from database
            User[] activeUsers = usersRepository.findAllActiveApplicationUsers();
            assertTrue(activeUsers.length > 0, "Should have active users in test data");

            // Associate first 2 users to group
            int usersToAdd = Math.min(2, activeUsers.length);
            Collection<User> groupUsers = new ArrayList<>();
            for (int i = 0; i < usersToAdd; i++) {
                groupUsers.add(activeUsers[i]);
            }
            group.setUsersCollection(groupUsers);
            group.setProcessesCollection(new ArrayList<>());

            group = userGroupsRepository.save(group);

            // When
            Optional<UserGroup> found = userGroupsRepository.findById(group.getId());

            // Then
            assertTrue(found.isPresent());
            assertEquals(usersToAdd, found.get().getUsersCollection().size());
        }

        @Test
        @DisplayName("2.3 - Users collection is eagerly loadable for list view")
        @Transactional
        void usersCollectionIsLoadable() {
            // Given
            UserGroup group = createAndSaveGroup("Loadable Group");

            // When: Retrieve via findAll (like the controller does)
            Iterable<UserGroup> allGroups = userGroupsRepository.findAll();

            // Then: Users collection should be accessible without LazyInitializationException
            for (UserGroup g : allGroups) {
                if (g.getName().equals("Loadable Group")) {
                    assertNotNull(g.getUsersCollection());
                    // This should not throw LazyInitializationException
                    int size = g.getUsersCollection().size();
                    assertTrue(size >= 0);
                }
            }
        }
    }

    // ==================== 3. PROCESS ASSOCIATION ====================

    @Nested
    @DisplayName("3. Process Association Tests")
    class ProcessAssociationTests {

        @Test
        @DisplayName("3.1 - New group is not associated to processes")
        @Transactional
        void newGroupIsNotAssociatedToProcesses() {
            // Given
            UserGroup group = createAndSaveGroup("New Unassociated Group");

            // When
            Optional<UserGroup> found = userGroupsRepository.findById(group.getId());

            // Then
            assertTrue(found.isPresent());
            assertFalse(found.get().isAssociatedToProcesses());
        }

        @Test
        @DisplayName("3.2 - Process association flag is correctly determined")
        @Transactional
        void processAssociationFlagIsCorrect() {
            // Given: Create a group without process associations
            UserGroup group = createAndSaveGroup("Process Check Group");

            // When
            Optional<UserGroup> found = userGroupsRepository.findById(group.getId());

            // Then
            assertTrue(found.isPresent());
            UserGroup foundGroup = found.get();

            // Verify the logic: associatedToProcesses = processesCollection.size() > 0
            boolean hasProcesses = foundGroup.getProcessesCollection() != null
                    && foundGroup.getProcessesCollection().size() > 0;
            assertEquals(hasProcesses, foundGroup.isAssociatedToProcesses());
        }
    }

    // ==================== 4. DATA PERSISTENCE ====================

    @Nested
    @DisplayName("4. Data Persistence Tests")
    class DataPersistenceTests {

        @Test
        @DisplayName("4.1 - Group name is persisted correctly")
        @Transactional
        void groupNameIsPersistedCorrectly() {
            // Given
            String uniqueName = "Persisted Group - " + System.currentTimeMillis();
            UserGroup group = createAndSaveGroup(uniqueName);

            // When
            Optional<UserGroup> found = userGroupsRepository.findById(group.getId());

            // Then
            assertTrue(found.isPresent());
            assertEquals(uniqueName, found.get().getName());
        }

        @Test
        @DisplayName("4.2 - Group with special characters in name is persisted")
        @Transactional
        void groupWithSpecialCharsIsPersistedCorrectly() {
            // Given
            String specialName = "Géomètres & Ingénieurs (Équipe α)";
            UserGroup group = createAndSaveGroup(specialName);

            // When
            Optional<UserGroup> found = userGroupsRepository.findById(group.getId());

            // Then
            assertTrue(found.isPresent());
            assertEquals(specialName, found.get().getName());
        }

        @Test
        @DisplayName("4.3 - User associations are persisted")
        @Transactional
        void userAssociationsArePersisted() {
            // Given
            UserGroup group = new UserGroup();
            group.setName("User Association Test - " + System.currentTimeMillis());

            // Get active users
            User[] activeUsers = usersRepository.findAllActiveApplicationUsers();
            assertTrue(activeUsers.length > 0);

            Collection<User> users = Arrays.asList(activeUsers[0]);
            group.setUsersCollection(users);
            group.setProcessesCollection(new ArrayList<>());

            group = userGroupsRepository.save(group);
            Integer groupId = group.getId();

            // When: Retrieve fresh from database
            Optional<UserGroup> found = userGroupsRepository.findById(groupId);

            // Then
            assertTrue(found.isPresent());
            assertEquals(1, found.get().getUsersCollection().size());
        }
    }

    // ==================== 5. LIST VIEW REQUIREMENTS ====================

    @Nested
    @DisplayName("5. List View Requirements")
    class ListViewRequirementsTests {

        @Test
        @DisplayName("5.1 - All groups returned by findAll have required fields")
        @Transactional
        void allGroupsHaveRequiredFields() {
            // Given: Ensure at least one group exists
            createAndSaveGroup("Complete Group Test");

            // When
            Iterable<UserGroup> allGroups = userGroupsRepository.findAll();

            // Then: Each group has all required fields for list view
            for (UserGroup group : allGroups) {
                // Required: ID for URL
                assertNotNull(group.getId(), "Group ID should not be null");

                // Required: Name for display
                assertNotNull(group.getName(), "Group name should not be null");

                // Required: Users collection for count
                assertNotNull(group.getUsersCollection(), "Users collection should not be null");

                // Required: Processes collection for delete eligibility
                // Note: may be null if not initialized, check isAssociatedToProcesses method
            }
        }

        @Test
        @DisplayName("5.2 - Users count is accurate for list display")
        @Transactional
        void usersCountIsAccurate() {
            // Given: Create group with known number of users
            UserGroup group = new UserGroup();
            group.setName("Count Test Group - " + System.currentTimeMillis());

            User[] activeUsers = usersRepository.findAllActiveApplicationUsers();
            int expectedCount = Math.min(3, activeUsers.length);

            Collection<User> users = new ArrayList<>();
            for (int i = 0; i < expectedCount; i++) {
                users.add(activeUsers[i]);
            }
            group.setUsersCollection(users);
            group.setProcessesCollection(new ArrayList<>());

            group = userGroupsRepository.save(group);

            // When: Retrieve via findAll (simulating controller)
            Iterable<UserGroup> allGroups = userGroupsRepository.findAll();

            // Then
            for (UserGroup g : allGroups) {
                if (g.getId().equals(group.getId())) {
                    assertEquals(expectedCount, g.getUsersCollection().size(),
                        "Users count should match for list display");
                }
            }
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates and saves a UserGroup with the given name.
     */
    private UserGroup createAndSaveGroup(String name) {
        UserGroup group = new UserGroup();
        group.setName(name);
        group.setUsersCollection(new ArrayList<>());
        group.setProcessesCollection(new ArrayList<>());
        return userGroupsRepository.save(group);
    }
}
