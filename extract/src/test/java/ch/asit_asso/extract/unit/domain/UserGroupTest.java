package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserGroup Entity Tests")
class UserGroupTest {

    private UserGroup userGroup;

    @BeforeEach
    void setUp() {
        userGroup = new UserGroup();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null id")
        void defaultConstructor_createsInstanceWithNullId() {
            UserGroup newGroup = new UserGroup();
            assertNull(newGroup.getId());
        }

        @Test
        @DisplayName("Constructor with id sets the id correctly")
        void constructorWithId_setsIdCorrectly() {
            Integer expectedId = 42;
            UserGroup newGroup = new UserGroup(expectedId);
            assertEquals(expectedId, newGroup.getId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId work correctly")
        void setAndGetId() {
            Integer expectedId = 100;
            userGroup.setId(expectedId);
            assertEquals(expectedId, userGroup.getId());
        }

        @Test
        @DisplayName("setName and getName work correctly")
        void setAndGetName() {
            String expectedName = "Administrators Group";
            userGroup.setName(expectedName);
            assertEquals(expectedName, userGroup.getName());
        }

        @Test
        @DisplayName("setUsersCollection and getUsersCollection work correctly")
        void setAndGetUsersCollection() {
            Collection<User> users = new ArrayList<>();
            users.add(new User(1));
            users.add(new User(2));

            userGroup.setUsersCollection(users);
            assertEquals(2, userGroup.getUsersCollection().size());
        }

        @Test
        @DisplayName("setProcessesCollection and getProcessesCollection work correctly")
        void setAndGetProcessesCollection() {
            Collection<Process> processes = new ArrayList<>();
            processes.add(new Process(1));
            processes.add(new Process(2));

            userGroup.setProcessesCollection(processes);
            assertEquals(2, userGroup.getProcessesCollection().size());
        }
    }

    @Nested
    @DisplayName("IsAssociatedToProcesses Tests")
    class IsAssociatedToProcessesTests {

        @Test
        @DisplayName("isAssociatedToProcesses returns true when processes exist")
        void isAssociatedToProcesses_returnsTrueWhenProcessesExist() {
            userGroup.setProcessesCollection(List.of(new Process(1)));
            assertTrue(userGroup.isAssociatedToProcesses());
        }

        @Test
        @DisplayName("isAssociatedToProcesses returns false when no processes")
        void isAssociatedToProcesses_returnsFalseWhenNoProcesses() {
            userGroup.setProcessesCollection(new ArrayList<>());
            assertFalse(userGroup.isAssociatedToProcesses());
        }
    }

    @Nested
    @DisplayName("Name Tests")
    class NameTests {

        @Test
        @DisplayName("name can be set to null")
        void name_canBeSetToNull() {
            userGroup.setName(null);
            assertNull(userGroup.getName());
        }

        @Test
        @DisplayName("name can be set to empty string")
        void name_canBeSetToEmptyString() {
            userGroup.setName("");
            assertEquals("", userGroup.getName());
        }

        @Test
        @DisplayName("name can be set to long string")
        void name_canBeSetToLongString() {
            String longName = "A".repeat(50);
            userGroup.setName(longName);
            assertEquals(longName, userGroup.getName());
        }
    }

    @Nested
    @DisplayName("Users Collection Tests")
    class UsersCollectionTests {

        @Test
        @DisplayName("users collection can be empty")
        void usersCollection_canBeEmpty() {
            userGroup.setUsersCollection(new ArrayList<>());
            assertTrue(userGroup.getUsersCollection().isEmpty());
        }

        @Test
        @DisplayName("users collection can contain multiple users")
        void usersCollection_canContainMultipleUsers() {
            List<User> users = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                users.add(new User(i));
            }
            userGroup.setUsersCollection(users);
            assertEquals(10, userGroup.getUsersCollection().size());
        }

        @Test
        @DisplayName("users collection can be replaced")
        void usersCollection_canBeReplaced() {
            userGroup.setUsersCollection(List.of(new User(1)));
            assertEquals(1, userGroup.getUsersCollection().size());

            userGroup.setUsersCollection(List.of(new User(2), new User(3)));
            assertEquals(2, userGroup.getUsersCollection().size());
        }
    }

    @Nested
    @DisplayName("Processes Collection Tests")
    class ProcessesCollectionTests {

        @Test
        @DisplayName("processes collection can be empty")
        void processesCollection_canBeEmpty() {
            userGroup.setProcessesCollection(new ArrayList<>());
            assertTrue(userGroup.getProcessesCollection().isEmpty());
        }

        @Test
        @DisplayName("processes collection can contain multiple processes")
        void processesCollection_canContainMultipleProcesses() {
            List<Process> processes = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                processes.add(new Process(i));
            }
            userGroup.setProcessesCollection(processes);
            assertEquals(5, userGroup.getProcessesCollection().size());
        }

        @Test
        @DisplayName("processes collection can be replaced")
        void processesCollection_canBeReplaced() {
            userGroup.setProcessesCollection(List.of(new Process(1)));
            assertEquals(1, userGroup.getProcessesCollection().size());

            userGroup.setProcessesCollection(List.of(new Process(2), new Process(3)));
            assertEquals(2, userGroup.getProcessesCollection().size());
        }
    }

    @Nested
    @DisplayName("Complete UserGroup Configuration Tests")
    class CompleteConfigurationTests {

        @Test
        @DisplayName("fully configured group has all attributes")
        void fullyConfiguredGroup_hasAllAttributes() {
            Integer id = 1;
            String name = "Test Group";
            List<User> users = List.of(new User(1), new User(2));
            List<Process> processes = List.of(new Process(1));

            userGroup.setId(id);
            userGroup.setName(name);
            userGroup.setUsersCollection(users);
            userGroup.setProcessesCollection(processes);

            assertEquals(id, userGroup.getId());
            assertEquals(name, userGroup.getName());
            assertEquals(2, userGroup.getUsersCollection().size());
            assertEquals(1, userGroup.getProcessesCollection().size());
            assertTrue(userGroup.isAssociatedToProcesses());
        }

        @Test
        @DisplayName("group without processes is not associated to processes")
        void groupWithoutProcesses_isNotAssociatedToProcesses() {
            userGroup.setId(1);
            userGroup.setName("Empty Group");
            userGroup.setUsersCollection(List.of(new User(1)));
            userGroup.setProcessesCollection(new ArrayList<>());

            assertFalse(userGroup.isAssociatedToProcesses());
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("user can be added to group")
        void user_canBeAddedToGroup() {
            User user = new User(1);
            user.setName("Test User");

            List<User> users = new ArrayList<>();
            users.add(user);
            userGroup.setUsersCollection(users);

            assertTrue(userGroup.getUsersCollection().contains(user));
        }

        @Test
        @DisplayName("process can be added to group")
        void process_canBeAddedToGroup() {
            Process process = new Process(1);
            process.setName("Test Process");

            List<Process> processes = new ArrayList<>();
            processes.add(process);
            userGroup.setProcessesCollection(processes);

            assertTrue(userGroup.getProcessesCollection().contains(process));
        }
    }
}
