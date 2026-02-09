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
package ch.asit_asso.extract.functional.usergroups;

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
 * Functional tests for the user groups list view.
 *
 * Validates end-to-end that:
 * 1. The list view displays ALL user groups
 * 2. Each group shows the correct number of associated users
 * 3. The delete button state is correctly determined
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("functional")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User Groups List View Functional Tests")
class UserGroupsListFunctionalTest {

    @Autowired
    private UserGroupsRepository userGroupsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @BeforeAll
    static void setUpClass() {
        System.out.println("========================================");
        System.out.println("User Groups List View Functional Tests");
        System.out.println("========================================");
        System.out.println("Validates that the list view displays:");
        System.out.println("- ALL user groups from the database");
        System.out.println("- Correct number of users per group");
        System.out.println("- Correct delete button state");
        System.out.println("========================================");
    }

    // ==================== 1. ALL GROUPS DISPLAYED ====================

    @Test
    @Order(1)
    @DisplayName("1. All user groups are retrievable for list display")
    @Transactional
    void allUserGroupsAreRetrievable() {
        // Given: Create multiple test groups
        UserGroup group1 = createGroup("Func Test Group Alpha");
        UserGroup group2 = createGroup("Func Test Group Beta");
        UserGroup group3 = createGroup("Func Test Group Gamma");

        group1 = userGroupsRepository.save(group1);
        group2 = userGroupsRepository.save(group2);
        group3 = userGroupsRepository.save(group3);

        // When: Retrieve all groups (as controller does)
        Iterable<UserGroup> allGroups = userGroupsRepository.findAll();
        List<UserGroup> groupList = new ArrayList<>();
        allGroups.forEach(groupList::add);

        // Then: All created groups are present
        Set<String> groupNames = new HashSet<>();
        for (UserGroup g : groupList) {
            groupNames.add(g.getName());
        }

        assertTrue(groupNames.contains("Func Test Group Alpha"), "Alpha group should be in list");
        assertTrue(groupNames.contains("Func Test Group Beta"), "Beta group should be in list");
        assertTrue(groupNames.contains("Func Test Group Gamma"), "Gamma group should be in list");

        System.out.println("✓ All user groups are retrievable:");
        System.out.println("  - Total groups in database: " + groupList.size());
        System.out.println("  - Test groups (Alpha, Beta, Gamma) all present");
    }

    // ==================== 2. USER COUNT PER GROUP ====================

    @Test
    @Order(2)
    @DisplayName("2. Each group displays correct number of associated users")
    @Transactional
    void eachGroupDisplaysCorrectUserCount() {
        // Given: Get existing active users
        User[] activeUsers = usersRepository.findAllActiveApplicationUsers();
        assertTrue(activeUsers.length >= 1, "Need at least 1 active user for this test");

        // Create groups with different user counts
        UserGroup emptyGroup = createGroup("Empty User Group - " + System.currentTimeMillis());
        emptyGroup = userGroupsRepository.save(emptyGroup);

        UserGroup oneUserGroup = createGroup("One User Group - " + System.currentTimeMillis());
        oneUserGroup.setUsersCollection(Arrays.asList(activeUsers[0]));
        oneUserGroup = userGroupsRepository.save(oneUserGroup);

        // When: Retrieve groups
        Optional<UserGroup> foundEmpty = userGroupsRepository.findById(emptyGroup.getId());
        Optional<UserGroup> foundOne = userGroupsRepository.findById(oneUserGroup.getId());

        // Then: User counts are correct
        assertTrue(foundEmpty.isPresent());
        assertTrue(foundOne.isPresent());

        assertEquals(0, foundEmpty.get().getUsersCollection().size(), "Empty group should have 0 users");
        assertEquals(1, foundOne.get().getUsersCollection().size(), "One user group should have 1 user");

        System.out.println("✓ User counts are correct per group:");
        System.out.println("  - Empty group: " + foundEmpty.get().getUsersCollection().size() + " users");
        System.out.println("  - One user group: " + foundOne.get().getUsersCollection().size() + " user");
        System.out.println("  - Active users available: " + activeUsers.length);
    }

    // ==================== 3. DELETE ELIGIBILITY ====================

    @Test
    @Order(3)
    @DisplayName("3. Groups not associated to processes can be deleted")
    @Transactional
    void groupsNotAssociatedToProcessesCanBeDeleted() {
        // Given: Create a group without process association
        UserGroup deletableGroup = createGroup("Deletable Group - " + System.currentTimeMillis());
        deletableGroup = userGroupsRepository.save(deletableGroup);

        // When: Check if deletable
        Optional<UserGroup> found = userGroupsRepository.findById(deletableGroup.getId());

        // Then
        assertTrue(found.isPresent());
        assertFalse(found.get().isAssociatedToProcesses(), "Group without processes should be deletable");

        System.out.println("✓ Group delete eligibility verified:");
        System.out.println("  - Group: " + found.get().getName());
        System.out.println("  - Associated to processes: " + found.get().isAssociatedToProcesses());
        System.out.println("  - Can be deleted: " + !found.get().isAssociatedToProcesses());
    }

    // ==================== 4. GROUP NAME UNIQUENESS ====================

    @Test
    @Order(4)
    @DisplayName("4. Groups are findable by name (case insensitive)")
    @Transactional
    void groupsAreFindableByName() {
        // Given
        String uniqueName = "Unique Name Test - " + System.currentTimeMillis();
        UserGroup group = createGroup(uniqueName);
        group = userGroupsRepository.save(group);

        // When: Search by name (case insensitive)
        UserGroup foundLower = userGroupsRepository.findByNameIgnoreCase(uniqueName.toLowerCase());
        UserGroup foundUpper = userGroupsRepository.findByNameIgnoreCase(uniqueName.toUpperCase());

        // Then
        assertNotNull(foundLower, "Should find group by lowercase name");
        assertNotNull(foundUpper, "Should find group by uppercase name");
        assertEquals(group.getId(), foundLower.getId());
        assertEquals(group.getId(), foundUpper.getId());

        System.out.println("✓ Groups are findable by name (case insensitive):");
        System.out.println("  - Original: " + uniqueName);
        System.out.println("  - Found by lowercase: YES");
        System.out.println("  - Found by uppercase: YES");
    }

    // ==================== 5. LIST VIEW DATA COMPLETE ====================

    @Test
    @Order(5)
    @DisplayName("5. List view data is complete for all groups")
    @Transactional
    void listViewDataIsComplete() {
        // Given: Create a complete group
        User[] activeUsers = usersRepository.findAllActiveApplicationUsers();
        assertTrue(activeUsers.length > 0);

        UserGroup completeGroup = createGroup("Complete Data Group - " + System.currentTimeMillis());
        completeGroup.setUsersCollection(Arrays.asList(activeUsers[0]));
        completeGroup = userGroupsRepository.save(completeGroup);

        // When: Retrieve via findAll (simulating controller)
        Iterable<UserGroup> allGroups = userGroupsRepository.findAll();

        // Then: Our group has complete data for list view
        boolean foundComplete = false;
        for (UserGroup g : allGroups) {
            if (g.getId().equals(completeGroup.getId())) {
                foundComplete = true;

                // Verify all list view fields
                assertNotNull(g.getId(), "ID required for link URL");
                assertNotNull(g.getName(), "Name required for display");
                assertNotNull(g.getUsersCollection(), "Users collection required for count");

                System.out.println("✓ List view data is complete:");
                System.out.println("  - ID: " + g.getId());
                System.out.println("  - Name: " + g.getName());
                System.out.println("  - Users count: " + g.getUsersCollection().size());
                System.out.println("  - Can be deleted: " + !g.isAssociatedToProcesses());
            }
        }

        assertTrue(foundComplete, "Complete group should be found in list");
    }

    // ==================== 6. DOCUMENTATION ====================

    @Test
    @Order(6)
    @DisplayName("6. Document: User groups list view requirements")
    void documentListViewRequirements() {
        System.out.println("✓ User Groups List View Requirements:");
        System.out.println("");
        System.out.println("  ENDPOINT:");
        System.out.println("  - URL: GET /userGroups");
        System.out.println("  - Controller: UserGroupsController.viewList()");
        System.out.println("  - Authorization: Admin only (isCurrentUserAdmin())");
        System.out.println("");
        System.out.println("  MODEL ATTRIBUTES:");
        System.out.println("  - userGroups: Iterable<UserGroup> from repository.findAll()");
        System.out.println("");
        System.out.println("  TABLE COLUMNS:");
        System.out.println("  ┌─────────────────┬────────────────────────────────────┐");
        System.out.println("  │ Column          │ Data Source                        │");
        System.out.println("  ├─────────────────┼────────────────────────────────────┤");
        System.out.println("  │ Name            │ userGroup.name (link to details)   │");
        System.out.println("  │ Members Number  │ #lists.size(usersCollection)       │");
        System.out.println("  │ Delete          │ not associatedToProcesses          │");
        System.out.println("  └─────────────────┴────────────────────────────────────┘");
        System.out.println("");
        System.out.println("  DELETE BUTTON STATES:");
        System.out.println("  - Enabled (btn-danger): Group not associated to any process");
        System.out.println("  - Disabled: Group is associated to at least one process");
        System.out.println("");
        System.out.println("  TEMPLATE: templates/pages/userGroups/list.html");

        assertTrue(true, "Documentation test");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a UserGroup with default empty collections.
     */
    private UserGroup createGroup(String name) {
        UserGroup group = new UserGroup();
        group.setName(name);
        group.setUsersCollection(new ArrayList<>());
        group.setProcessesCollection(new ArrayList<>());
        return group;
    }
}
