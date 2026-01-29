package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Task Entity Tests")
class TaskTest {

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null id")
        void defaultConstructor_createsInstanceWithNullId() {
            Task newTask = new Task();
            assertNull(newTask.getId());
        }

        @Test
        @DisplayName("Constructor with id sets the id correctly")
        void constructorWithId_setsIdCorrectly() {
            Integer expectedId = 42;
            Task newTask = new Task(expectedId);
            assertEquals(expectedId, newTask.getId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId work correctly")
        void setAndGetId() {
            Integer expectedId = 100;
            task.setId(expectedId);
            assertEquals(expectedId, task.getId());
        }

        @Test
        @DisplayName("setCode and getCode work correctly")
        void setAndGetCode() {
            String expectedCode = "EMAIL_TASK";
            task.setCode(expectedCode);
            assertEquals(expectedCode, task.getCode());
        }

        @Test
        @DisplayName("setLabel and getLabel work correctly")
        void setAndGetLabel() {
            String expectedLabel = "Send Email Task";
            task.setLabel(expectedLabel);
            assertEquals(expectedLabel, task.getLabel());
        }

        @Test
        @DisplayName("setPosition and getPosition work correctly")
        void setAndGetPosition() {
            Integer expectedPosition = 5;
            task.setPosition(expectedPosition);
            assertEquals(expectedPosition, task.getPosition());
        }

        @Test
        @DisplayName("setProcess and getProcess work correctly")
        void setAndGetProcess() {
            Process expectedProcess = new Process(1);
            task.setProcess(expectedProcess);
            assertEquals(expectedProcess, task.getProcess());
        }
    }

    @Nested
    @DisplayName("Parameters Values Tests")
    class ParametersValuesTests {

        @Test
        @DisplayName("setParametersValues adds parameters to empty map")
        void setParametersValues_addsParametersToEmptyMap() {
            HashMap<String, String> params = new HashMap<>();
            params.put("key1", "value1");
            params.put("key2", "value2");

            task.setParametersValues(params);

            HashMap<String, String> result = task.getParametersValues();
            assertEquals("value1", result.get("key1"));
            assertEquals("value2", result.get("key2"));
        }

        @Test
        @DisplayName("setParametersValues throws exception for null map")
        void setParametersValues_throwsExceptionForNullMap() {
            assertThrows(IllegalArgumentException.class, () -> task.setParametersValues(null));
        }

        @Test
        @DisplayName("setParametersValues does nothing for empty map")
        void setParametersValues_doesNothingForEmptyMap() {
            HashMap<String, String> emptyParams = new HashMap<>();
            task.setParametersValues(emptyParams);
            assertNull(task.getParametersValues());
        }

        @Test
        @DisplayName("setParametersValues adds to existing map")
        void setParametersValues_addsToExistingMap() {
            HashMap<String, String> params1 = new HashMap<>();
            params1.put("key1", "value1");
            task.setParametersValues(params1);

            HashMap<String, String> params2 = new HashMap<>();
            params2.put("key2", "value2");
            task.setParametersValues(params2);

            HashMap<String, String> result = task.getParametersValues();
            assertEquals("value1", result.get("key1"));
            assertEquals("value2", result.get("key2"));
        }

        @Test
        @DisplayName("updateParametersValues delegates to setParametersValues when map is empty")
        void updateParametersValues_delegatesToSetWhenMapIsEmpty() {
            HashMap<String, String> params = new HashMap<>();
            params.put("key1", "value1");
            task.updateParametersValues(params);

            assertEquals("value1", task.getParametersValues().get("key1"));
        }

        @Test
        @DisplayName("updateParametersValues removes old keys not in new map")
        void updateParametersValues_removesOldKeysNotInNewMap() {
            HashMap<String, String> params1 = new HashMap<>();
            params1.put("key1", "value1");
            params1.put("key2", "value2");
            task.setParametersValues(params1);

            HashMap<String, String> params2 = new HashMap<>();
            params2.put("key1", "newValue1");
            task.updateParametersValues(params2);

            HashMap<String, String> result = task.getParametersValues();
            assertEquals("newValue1", result.get("key1"));
            assertNull(result.get("key2"));
        }

        @Test
        @DisplayName("updateParametersValues throws exception for null map")
        void updateParametersValues_throwsExceptionForNullMap() {
            HashMap<String, String> params = new HashMap<>();
            params.put("key1", "value1");
            task.setParametersValues(params);

            assertThrows(IllegalArgumentException.class, () -> task.updateParametersValues(null));
        }

        @Test
        @DisplayName("updateParametersValues does nothing for empty map")
        void updateParametersValues_doesNothingForEmptyMap() {
            HashMap<String, String> params = new HashMap<>();
            params.put("key1", "value1");
            task.setParametersValues(params);

            task.updateParametersValues(new HashMap<>());

            assertEquals("value1", task.getParametersValues().get("key1"));
        }
    }

    @Nested
    @DisplayName("Messages Templates Tests")
    class MessagesTemplatesTests {

        @Test
        @DisplayName("getValidationMessagesTemplatesIds returns null when no parameters")
        void getValidationMessagesTemplatesIds_returnsNullWhenNoParameters() {
            assertNull(task.getValidationMessagesTemplatesIds());
        }

        @Test
        @DisplayName("getValidationMessagesTemplatesIds returns null when parameter not set")
        void getValidationMessagesTemplatesIds_returnsNullWhenParameterNotSet() {
            HashMap<String, String> params = new HashMap<>();
            params.put("other_key", "value");
            task.setParametersValues(params);

            assertNull(task.getValidationMessagesTemplatesIds());
        }

        @Test
        @DisplayName("getValidationMessagesTemplatesIds parses comma-separated ids")
        void getValidationMessagesTemplatesIds_parsesCommaSeparatedIds() {
            HashMap<String, String> params = new HashMap<>();
            params.put(Task.VALIDATION_MESSAGES_PARAMETER_NAME, "1,2,3");
            task.setParametersValues(params);

            List<Integer> result = task.getValidationMessagesTemplatesIds();
            assertEquals(3, result.size());
            assertEquals(1, result.get(0));
            assertEquals(2, result.get(1));
            assertEquals(3, result.get(2));
        }

        @Test
        @DisplayName("getValidationMessagesTemplatesIds skips invalid ids")
        void getValidationMessagesTemplatesIds_skipsInvalidIds() {
            HashMap<String, String> params = new HashMap<>();
            params.put(Task.VALIDATION_MESSAGES_PARAMETER_NAME, "1,invalid,3,0,-1");
            task.setParametersValues(params);

            List<Integer> result = task.getValidationMessagesTemplatesIds();
            assertEquals(2, result.size());
            assertEquals(1, result.get(0));
            assertEquals(3, result.get(1));
        }

        @Test
        @DisplayName("getRejectionMessagesTemplatesIds returns null when no parameters")
        void getRejectionMessagesTemplatesIds_returnsNullWhenNoParameters() {
            assertNull(task.getRejectionMessagesTemplatesIds());
        }

        @Test
        @DisplayName("getRejectionMessagesTemplatesIds parses comma-separated ids")
        void getRejectionMessagesTemplatesIds_parsesCommaSeparatedIds() {
            HashMap<String, String> params = new HashMap<>();
            params.put(Task.REJECTION_MESSAGES_PARAMETER_NAME, "5,10,15");
            task.setParametersValues(params);

            List<Integer> result = task.getRejectionMessagesTemplatesIds();
            assertEquals(3, result.size());
            assertEquals(5, result.get(0));
            assertEquals(10, result.get(1));
            assertEquals(15, result.get(2));
        }
    }

    @Nested
    @DisplayName("CreateCopy Tests")
    class CreateCopyTests {

        @Test
        @DisplayName("createCopy creates copy with same code")
        void createCopy_createsCopyWithSameCode() {
            task.setCode("TEST_CODE");
            Task copy = task.createCopy();
            assertEquals("TEST_CODE", copy.getCode());
        }

        @Test
        @DisplayName("createCopy creates copy with same label")
        void createCopy_createsCopyWithSameLabel() {
            task.setLabel("Test Label");
            Task copy = task.createCopy();
            assertEquals("Test Label", copy.getLabel());
        }

        @Test
        @DisplayName("createCopy creates copy with same position")
        void createCopy_createsCopyWithSamePosition() {
            task.setPosition(5);
            Task copy = task.createCopy();
            assertEquals(5, copy.getPosition());
        }

        @Test
        @DisplayName("createCopy creates copy with same parameters")
        void createCopy_createsCopyWithSameParameters() {
            HashMap<String, String> params = new HashMap<>();
            params.put("key1", "value1");
            task.setParametersValues(params);

            Task copy = task.createCopy();

            assertEquals("value1", copy.getParametersValues().get("key1"));
        }

        @Test
        @DisplayName("createCopy does not copy id")
        void createCopy_doesNotCopyId() {
            task.setId(42);
            Task copy = task.createCopy();
            assertNull(copy.getId());
        }

        @Test
        @DisplayName("createCopy does not copy process")
        void createCopy_doesNotCopyProcess() {
            task.setProcess(new Process(1));
            Task copy = task.createCopy();
            assertNull(copy.getProcess());
        }

        @Test
        @DisplayName("createCopy handles null parameters")
        void createCopy_handlesNullParameters() {
            task.setCode("TEST");
            Task copy = task.createCopy();
            assertNull(copy.getParametersValues());
        }
    }

    @Nested
    @DisplayName("Equals, HashCode, and ToString Tests")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("equals returns true for same position, code, label, and process")
        void equals_returnsTrueForSameAttributes() {
            Process process = new Process(1);

            Task task1 = new Task();
            task1.setPosition(1);
            task1.setCode("CODE");
            task1.setLabel("Label");
            task1.setProcess(process);

            Task task2 = new Task();
            task2.setPosition(1);
            task2.setCode("CODE");
            task2.setLabel("Label");
            task2.setProcess(process);

            assertEquals(task1, task2);
        }

        @Test
        @DisplayName("equals returns false for different position")
        void equals_returnsFalseForDifferentPosition() {
            Task task1 = new Task();
            task1.setPosition(1);
            task1.setCode("CODE");

            Task task2 = new Task();
            task2.setPosition(2);
            task2.setCode("CODE");

            assertNotEquals(task1, task2);
        }

        @Test
        @DisplayName("equals returns false for different code")
        void equals_returnsFalseForDifferentCode() {
            Task task1 = new Task();
            task1.setPosition(1);
            task1.setCode("CODE1");

            Task task2 = new Task();
            task2.setPosition(1);
            task2.setCode("CODE2");

            assertNotEquals(task1, task2);
        }

        @Test
        @DisplayName("equals returns false for null")
        void equals_returnsFalseForNull() {
            task.setId(1);
            assertNotEquals(null, task);
        }

        @Test
        @DisplayName("equals returns false for different type")
        void equals_returnsFalseForDifferentType() {
            task.setId(1);
            assertNotEquals("not a task", task);
        }

        @Test
        @DisplayName("hashCode is consistent for same attributes")
        void hashCode_isConsistentForSameAttributes() {
            Task task1 = new Task();
            task1.setPosition(1);
            task1.setCode("CODE");
            task1.setLabel("Label");

            Task task2 = new Task();
            task2.setPosition(1);
            task2.setCode("CODE");
            task2.setLabel("Label");

            assertEquals(task1.hashCode(), task2.hashCode());
        }

        @Test
        @DisplayName("toString contains id")
        void toString_containsId() {
            task.setId(42);
            String result = task.toString();
            assertTrue(result.contains("42"));
            assertTrue(result.contains("idTask"));
        }
    }

    @Nested
    @DisplayName("Constants Tests")
    class ConstantsTests {

        @Test
        @DisplayName("VALIDATION_MESSAGES_PARAMETER_NAME is correct")
        void validationMessagesParameterName_isCorrect() {
            assertEquals("valid_msgs", Task.VALIDATION_MESSAGES_PARAMETER_NAME);
        }

        @Test
        @DisplayName("REJECTION_MESSAGES_PARAMETER_NAME is correct")
        void rejectionMessagesParameterName_isCorrect() {
            assertEquals("reject_msgs", Task.REJECTION_MESSAGES_PARAMETER_NAME);
        }
    }
}
