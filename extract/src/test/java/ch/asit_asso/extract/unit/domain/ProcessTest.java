package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Rule;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.persistence.RequestsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("Process Entity Tests")
@ExtendWith(MockitoExtension.class)
class ProcessTest {

    private Process process;

    @Mock
    private RequestsRepository requestsRepository;

    @BeforeEach
    void setUp() {
        process = new Process();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null id")
        void defaultConstructor_createsInstanceWithNullId() {
            Process newProcess = new Process();
            assertNull(newProcess.getId());
        }

        @Test
        @DisplayName("Constructor with id sets the id correctly")
        void constructorWithId_setsIdCorrectly() {
            Integer expectedId = 42;
            Process newProcess = new Process(expectedId);
            assertEquals(expectedId, newProcess.getId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId work correctly")
        void setAndGetId() {
            Integer expectedId = 100;
            process.setId(expectedId);
            assertEquals(expectedId, process.getId());
        }

        @Test
        @DisplayName("setName and getName work correctly")
        void setAndGetName() {
            String expectedName = "Test Process";
            process.setName(expectedName);
            assertEquals(expectedName, process.getName());
        }

        @Test
        @DisplayName("setUsersCollection and getUsersCollection work correctly")
        void setAndGetUsersCollection() {
            Collection<User> users = new ArrayList<>();
            users.add(new User(1));
            users.add(new User(2));

            process.setUsersCollection(users);
            assertEquals(2, process.getUsersCollection().size());
        }

        @Test
        @DisplayName("setUserGroupsCollection and getUserGroupsCollection work correctly")
        void setAndGetUserGroupsCollection() {
            Collection<UserGroup> groups = new ArrayList<>();
            groups.add(new UserGroup(1));
            groups.add(new UserGroup(2));

            process.setUserGroupsCollection(groups);
            assertEquals(2, process.getUserGroupsCollection().size());
        }

        @Test
        @DisplayName("setTasksCollection and getTasksCollection work correctly")
        void setAndGetTasksCollection() {
            Collection<Task> tasks = new ArrayList<>();
            tasks.add(new Task(1));
            tasks.add(new Task(2));

            process.setTasksCollection(tasks);
            assertEquals(2, process.getTasksCollection().size());
        }

        @Test
        @DisplayName("setRequestsCollection and getRequestsCollection work correctly")
        void setAndGetRequestsCollection() {
            Collection<Request> requests = new ArrayList<>();
            requests.add(new Request(1));
            requests.add(new Request(2));

            process.setRequestsCollection(requests);
            assertEquals(2, process.getRequestsCollection().size());
        }

        @Test
        @DisplayName("setRulesCollection and getRulesCollection work correctly")
        void setAndGetRulesCollection() {
            Collection<Rule> rules = new ArrayList<>();
            rules.add(new Rule(1));
            rules.add(new Rule(2));

            process.setRulesCollection(rules);
            assertEquals(2, process.getRulesCollection().size());
        }
    }

    @Nested
    @DisplayName("HasActiveRequests Tests")
    class HasActiveRequestsTests {

        @Test
        @DisplayName("hasActiveRequests returns true when active request exists")
        void hasActiveRequests_returnsTrueWhenActiveRequestExists() {
            Request activeRequest = new Request(1);
            activeRequest.setStatus(Request.Status.ONGOING);

            List<Request> requests = new ArrayList<>();
            requests.add(activeRequest);
            process.setRequestsCollection(requests);

            assertTrue(process.hasActiveRequests());
        }

        @Test
        @DisplayName("hasActiveRequests returns false when all requests are finished")
        void hasActiveRequests_returnsFalseWhenAllFinished() {
            Request finishedRequest = new Request(1);
            finishedRequest.setStatus(Request.Status.FINISHED);

            List<Request> requests = new ArrayList<>();
            requests.add(finishedRequest);
            process.setRequestsCollection(requests);

            assertFalse(process.hasActiveRequests());
        }

        @Test
        @DisplayName("hasActiveRequests with repository returns true when active request exists")
        void hasActiveRequestsWithRepo_returnsTrueWhenActiveExists() {
            process.setId(1);
            List<Request> activeRequests = new ArrayList<>();
            activeRequests.add(new Request(1));

            when(requestsRepository.findByStatusNotAndProcessIn(eq(Request.Status.FINISHED), any()))
                    .thenReturn(activeRequests);

            assertTrue(process.hasActiveRequests(requestsRepository));
        }

        @Test
        @DisplayName("hasActiveRequests with repository returns false when no active request")
        void hasActiveRequestsWithRepo_returnsFalseWhenNoActive() {
            process.setId(1);

            when(requestsRepository.findByStatusNotAndProcessIn(eq(Request.Status.FINISHED), any()))
                    .thenReturn(Collections.emptyList());

            assertFalse(process.hasActiveRequests(requestsRepository));
        }

        @Test
        @DisplayName("hasActiveRequests with null repository throws exception")
        void hasActiveRequestsWithRepo_throwsExceptionForNullRepo() {
            assertThrows(IllegalArgumentException.class, () -> process.hasActiveRequests(null));
        }
    }

    @Nested
    @DisplayName("HasOngoingRequests Tests")
    class HasOngoingRequestsTests {

        @Test
        @DisplayName("hasOngoingRequests returns true when ongoing request exists")
        void hasOngoingRequests_returnsTrueWhenOngoingExists() {
            Request ongoingRequest = new Request(1);
            ongoingRequest.setStatus(Request.Status.ONGOING);

            List<Request> requests = new ArrayList<>();
            requests.add(ongoingRequest);
            process.setRequestsCollection(requests);

            assertTrue(process.hasOngoingRequests());
        }

        @Test
        @DisplayName("hasOngoingRequests returns false when no ongoing requests")
        void hasOngoingRequests_returnsFalseWhenNoOngoing() {
            Request errorRequest = new Request(1);
            errorRequest.setStatus(Request.Status.ERROR);

            List<Request> requests = new ArrayList<>();
            requests.add(errorRequest);
            process.setRequestsCollection(requests);

            assertFalse(process.hasOngoingRequests());
        }

        @Test
        @DisplayName("hasOngoingRequests with repository returns true when ongoing exists")
        void hasOngoingRequestsWithRepo_returnsTrueWhenOngoingExists() {
            process.setId(1);
            List<Request> ongoingRequests = new ArrayList<>();
            ongoingRequests.add(new Request(1));

            when(requestsRepository.findByStatusAndProcessIn(eq(Request.Status.ONGOING), any()))
                    .thenReturn(ongoingRequests);

            assertTrue(process.hasOngoingRequests(requestsRepository));
        }

        @Test
        @DisplayName("hasOngoingRequests with repository returns false when no ongoing")
        void hasOngoingRequestsWithRepo_returnsFalseWhenNoOngoing() {
            process.setId(1);

            when(requestsRepository.findByStatusAndProcessIn(eq(Request.Status.ONGOING), any()))
                    .thenReturn(Collections.emptyList());

            assertFalse(process.hasOngoingRequests(requestsRepository));
        }

        @Test
        @DisplayName("hasOngoingRequests with null repository throws exception")
        void hasOngoingRequestsWithRepo_throwsExceptionForNullRepo() {
            assertThrows(IllegalArgumentException.class, () -> process.hasOngoingRequests(null));
        }
    }

    @Nested
    @DisplayName("HasRulesAssigned Tests")
    class HasRulesAssignedTests {

        @Test
        @DisplayName("hasRulesAssigned returns true when rules exist")
        void hasRulesAssigned_returnsTrueWhenRulesExist() {
            List<Rule> rules = new ArrayList<>();
            rules.add(new Rule(1));
            process.setRulesCollection(rules);

            assertTrue(process.hasRulesAssigned());
        }

        @Test
        @DisplayName("hasRulesAssigned returns false when no rules")
        void hasRulesAssigned_returnsFalseWhenNoRules() {
            process.setRulesCollection(new ArrayList<>());
            assertFalse(process.hasRulesAssigned());
        }

        @Test
        @DisplayName("hasRulesAssigned returns false when rules is null")
        void hasRulesAssigned_returnsFalseWhenNull() {
            process.setRulesCollection(null);
            assertFalse(process.hasRulesAssigned());
        }
    }

    @Nested
    @DisplayName("CanBeDeleted Tests")
    class CanBeDeletedTests {

        @Test
        @DisplayName("canBeDeleted returns true when no active requests and no rules")
        void canBeDeleted_returnsTrueWhenNoActiveAndNoRules() {
            Request finishedRequest = new Request(1);
            finishedRequest.setStatus(Request.Status.FINISHED);

            process.setRequestsCollection(List.of(finishedRequest));
            process.setRulesCollection(new ArrayList<>());

            assertTrue(process.canBeDeleted());
        }

        @Test
        @DisplayName("canBeDeleted returns false when active requests exist")
        void canBeDeleted_returnsFalseWhenActiveRequestsExist() {
            Request activeRequest = new Request(1);
            activeRequest.setStatus(Request.Status.ONGOING);

            process.setRequestsCollection(List.of(activeRequest));
            process.setRulesCollection(new ArrayList<>());

            assertFalse(process.canBeDeleted());
        }

        @Test
        @DisplayName("canBeDeleted returns false when rules exist")
        void canBeDeleted_returnsFalseWhenRulesExist() {
            Request finishedRequest = new Request(1);
            finishedRequest.setStatus(Request.Status.FINISHED);

            process.setRequestsCollection(List.of(finishedRequest));
            process.setRulesCollection(List.of(new Rule(1)));

            assertFalse(process.canBeDeleted());
        }

        @Test
        @DisplayName("canBeDeleted with repository works correctly")
        void canBeDeletedWithRepo_worksCorrectly() {
            process.setId(1);
            process.setRulesCollection(new ArrayList<>());

            when(requestsRepository.findByStatusNotAndProcessIn(eq(Request.Status.FINISHED), any()))
                    .thenReturn(Collections.emptyList());

            assertTrue(process.canBeDeleted(requestsRepository));
        }

        @Test
        @DisplayName("canBeDeleted with null repository throws exception")
        void canBeDeletedWithRepo_throwsExceptionForNullRepo() {
            assertThrows(IllegalArgumentException.class, () -> process.canBeDeleted(null));
        }
    }

    @Nested
    @DisplayName("CanBeEdited Tests")
    class CanBeEditedTests {

        @Test
        @DisplayName("canBeEdited returns true when no ongoing requests")
        void canBeEdited_returnsTrueWhenNoOngoingRequests() {
            Request errorRequest = new Request(1);
            errorRequest.setStatus(Request.Status.ERROR);

            process.setRequestsCollection(List.of(errorRequest));

            assertTrue(process.canBeEdited());
        }

        @Test
        @DisplayName("canBeEdited returns false when ongoing requests exist")
        void canBeEdited_returnsFalseWhenOngoingRequestsExist() {
            Request ongoingRequest = new Request(1);
            ongoingRequest.setStatus(Request.Status.ONGOING);

            process.setRequestsCollection(List.of(ongoingRequest));

            assertFalse(process.canBeEdited());
        }

        @Test
        @DisplayName("canBeEdited with repository works correctly")
        void canBeEditedWithRepo_worksCorrectly() {
            process.setId(1);

            when(requestsRepository.findByStatusAndProcessIn(eq(Request.Status.ONGOING), any()))
                    .thenReturn(Collections.emptyList());

            assertTrue(process.canBeEdited(requestsRepository));
        }

        @Test
        @DisplayName("canBeEdited with null repository throws exception")
        void canBeEditedWithRepo_throwsExceptionForNullRepo() {
            assertThrows(IllegalArgumentException.class, () -> process.canBeEdited(null));
        }
    }

    @Nested
    @DisplayName("GetDistinctOperators Tests")
    class GetDistinctOperatorsTests {

        @Test
        @DisplayName("getDistinctOperators returns users from direct assignment")
        void getDistinctOperators_returnsUsersFromDirectAssignment() {
            User user1 = new User(1);
            User user2 = new User(2);

            process.setUsersCollection(List.of(user1, user2));
            process.setUserGroupsCollection(new ArrayList<>());

            Collection<User> operators = process.getDistinctOperators();
            assertEquals(2, operators.size());
        }

        @Test
        @DisplayName("getDistinctOperators returns users from groups")
        void getDistinctOperators_returnsUsersFromGroups() {
            User user1 = new User(1);
            User user2 = new User(2);

            UserGroup group = new UserGroup(1);
            group.setUsersCollection(List.of(user2));

            process.setUsersCollection(List.of(user1));
            process.setUserGroupsCollection(List.of(group));

            Collection<User> operators = process.getDistinctOperators();
            assertEquals(2, operators.size());
        }

        @Test
        @DisplayName("getDistinctOperators removes duplicates")
        void getDistinctOperators_removesDuplicates() {
            User user1 = new User(1);

            UserGroup group = new UserGroup(1);
            group.setUsersCollection(List.of(user1));

            process.setUsersCollection(List.of(user1));
            process.setUserGroupsCollection(List.of(group));

            Collection<User> operators = process.getDistinctOperators();
            assertEquals(1, operators.size());
        }
    }

    @Nested
    @DisplayName("CreateCopy Tests")
    class CreateCopyTests {

        @Test
        @DisplayName("createCopy creates copy with modified name")
        void createCopy_createsCopyWithModifiedName() {
            process.setName("Original Process");
            process.setUsersCollection(new ArrayList<>());
            process.setUserGroupsCollection(new ArrayList<>());

            Process copy = process.createCopy();

            assertEquals("Original Process - Copie", copy.getName());
        }

        @Test
        @DisplayName("createCopy copies users collection")
        void createCopy_copiesUsersCollection() {
            User user = new User(1);
            process.setName("Test");
            process.setUsersCollection(List.of(user));
            process.setUserGroupsCollection(new ArrayList<>());

            Process copy = process.createCopy();

            assertEquals(1, copy.getUsersCollection().size());
        }

        @Test
        @DisplayName("createCopy copies user groups collection")
        void createCopy_copiesUserGroupsCollection() {
            UserGroup group = new UserGroup(1);
            process.setName("Test");
            process.setUsersCollection(new ArrayList<>());
            process.setUserGroupsCollection(List.of(group));

            Process copy = process.createCopy();

            assertEquals(1, copy.getUserGroupsCollection().size());
        }

        @Test
        @DisplayName("createCopy does not copy id")
        void createCopy_doesNotCopyId() {
            process.setId(42);
            process.setName("Test");
            process.setUsersCollection(new ArrayList<>());
            process.setUserGroupsCollection(new ArrayList<>());

            Process copy = process.createCopy();

            assertNull(copy.getId());
        }
    }

    @Nested
    @DisplayName("CreateTasksCopy Tests")
    class CreateTasksCopyTests {

        @Test
        @DisplayName("createTasksCopy creates copies of all tasks")
        void createTasksCopy_createsCopiesOfAllTasks() {
            Task task1 = new Task(1);
            task1.setCode("CODE1");
            task1.setPosition(1);

            Task task2 = new Task(2);
            task2.setCode("CODE2");
            task2.setPosition(2);

            process.setTasksCollection(List.of(task1, task2));

            Process targetProcess = new Process(10);
            Collection<Task> copiedTasks = process.createTasksCopy(targetProcess);

            assertEquals(2, copiedTasks.size());
            for (Task copiedTask : copiedTasks) {
                assertEquals(targetProcess, copiedTask.getProcess());
                assertNull(copiedTask.getId());
            }
        }
    }

    @Nested
    @DisplayName("Equals, HashCode, and ToString Tests")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("equals returns true for same id")
        void equals_returnsTrueForSameId() {
            Process process1 = new Process(1);
            Process process2 = new Process(1);
            assertEquals(process1, process2);
        }

        @Test
        @DisplayName("equals returns false for different id")
        void equals_returnsFalseForDifferentId() {
            Process process1 = new Process(1);
            Process process2 = new Process(2);
            assertNotEquals(process1, process2);
        }

        @Test
        @DisplayName("equals returns false for null")
        void equals_returnsFalseForNull() {
            Process process1 = new Process(1);
            assertNotEquals(null, process1);
        }

        @Test
        @DisplayName("equals returns false for different type")
        void equals_returnsFalseForDifferentType() {
            Process process1 = new Process(1);
            assertNotEquals("not a process", process1);
        }

        @Test
        @DisplayName("hashCode is consistent for same id")
        void hashCode_isConsistentForSameId() {
            Process process1 = new Process(1);
            Process process2 = new Process(1);
            assertEquals(process1.hashCode(), process2.hashCode());
        }

        @Test
        @DisplayName("toString contains id")
        void toString_containsId() {
            Process process1 = new Process(42);
            String result = process1.toString();
            assertTrue(result.contains("42"));
            assertTrue(result.contains("idProcess"));
        }
    }
}
