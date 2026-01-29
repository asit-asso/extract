package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import ch.asit_asso.extract.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RequestHistoryRecord Entity Tests")
class RequestHistoryRecordTest {

    private RequestHistoryRecord record;

    @BeforeEach
    void setUp() {
        record = new RequestHistoryRecord();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null id")
        void defaultConstructor_createsInstanceWithNullId() {
            RequestHistoryRecord newRecord = new RequestHistoryRecord();
            assertNull(newRecord.getId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId work correctly")
        void setAndGetId() {
            Integer expectedId = 100;
            record.setId(expectedId);
            assertEquals(expectedId, record.getId());
        }

        @Test
        @DisplayName("setTaskLabel and getTaskLabel work correctly")
        void setAndGetTaskLabel() {
            String expectedLabel = "Email Task";
            record.setTaskLabel(expectedLabel);
            assertEquals(expectedLabel, record.getTaskLabel());
        }

        @Test
        @DisplayName("setStatus and getStatus work correctly")
        void setAndGetStatus() {
            record.setStatus(RequestHistoryRecord.Status.ONGOING);
            assertEquals(RequestHistoryRecord.Status.ONGOING, record.getStatus());

            record.setStatus(RequestHistoryRecord.Status.FINISHED);
            assertEquals(RequestHistoryRecord.Status.FINISHED, record.getStatus());
        }

        @Test
        @DisplayName("setStep and getStep work correctly")
        void setAndGetStep() {
            Integer expectedStep = 3;
            record.setStep(expectedStep);
            assertEquals(expectedStep, record.getStep());
        }

        @Test
        @DisplayName("setProcessStep and getProcessStep work correctly")
        void setAndGetProcessStep() {
            Integer expectedProcessStep = 2;
            record.setProcessStep(expectedProcessStep);
            assertEquals(expectedProcessStep, record.getProcessStep());
        }

        @Test
        @DisplayName("setMessage and getMessage work correctly")
        void setAndGetMessage() {
            String expectedMessage = "Task completed successfully";
            record.setMessage(expectedMessage);
            assertEquals(expectedMessage, record.getMessage());
        }

        @Test
        @DisplayName("setStartDate and getStartDate work correctly")
        void setAndGetStartDate() {
            Calendar expectedDate = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 30);
            record.setStartDate(expectedDate);
            assertEquals(expectedDate, record.getStartDate());
        }

        @Test
        @DisplayName("setEndDate and getEndDate work correctly")
        void setAndGetEndDate() {
            Calendar expectedDate = new GregorianCalendar(2024, Calendar.JANUARY, 15, 11, 45);
            record.setEndDate(expectedDate);
            assertEquals(expectedDate, record.getEndDate());
        }

        @Test
        @DisplayName("setRequest and getRequest work correctly")
        void setAndGetRequest() {
            Request expectedRequest = new Request(1);
            record.setRequest(expectedRequest);
            assertEquals(expectedRequest, record.getRequest());
        }

        @Test
        @DisplayName("setUser and getUser work correctly")
        void setAndGetUser() {
            User expectedUser = new User(1);
            record.setUser(expectedUser);
            assertEquals(expectedUser, record.getUser());
        }
    }

    @Nested
    @DisplayName("Status Enum Tests")
    class StatusEnumTests {

        @Test
        @DisplayName("All status values are defined")
        void allStatusValuesAreDefined() {
            RequestHistoryRecord.Status[] statuses = RequestHistoryRecord.Status.values();
            assertEquals(5, statuses.length);
            assertNotNull(RequestHistoryRecord.Status.ONGOING);
            assertNotNull(RequestHistoryRecord.Status.STANDBY);
            assertNotNull(RequestHistoryRecord.Status.ERROR);
            assertNotNull(RequestHistoryRecord.Status.FINISHED);
            assertNotNull(RequestHistoryRecord.Status.SKIPPED);
        }

        @Test
        @DisplayName("valueOf returns correct status")
        void valueOfReturnsCorrectStatus() {
            assertEquals(RequestHistoryRecord.Status.ONGOING, RequestHistoryRecord.Status.valueOf("ONGOING"));
            assertEquals(RequestHistoryRecord.Status.FINISHED, RequestHistoryRecord.Status.valueOf("FINISHED"));
            assertEquals(RequestHistoryRecord.Status.ERROR, RequestHistoryRecord.Status.valueOf("ERROR"));
        }
    }

    @Nested
    @DisplayName("SetToError Tests")
    class SetToErrorTests {

        @Test
        @DisplayName("setToError sets status, end date, and message")
        void setToError_setsStatusEndDateAndMessage() {
            String errorMessage = "Task failed due to network error";
            record.setToError(errorMessage);

            assertEquals(RequestHistoryRecord.Status.ERROR, record.getStatus());
            assertNotNull(record.getEndDate());
            assertEquals(errorMessage, record.getMessage());
        }

        @Test
        @DisplayName("setToError with date sets specified date")
        void setToErrorWithDate_setsSpecifiedDate() {
            String errorMessage = "Task failed";
            Calendar errorDate = new GregorianCalendar(2024, Calendar.JANUARY, 20, 14, 30);

            record.setToError(errorMessage, errorDate);

            assertEquals(RequestHistoryRecord.Status.ERROR, record.getStatus());
            assertEquals(errorDate, record.getEndDate());
            assertEquals(errorMessage, record.getMessage());
        }

        @Test
        @DisplayName("setToError throws exception for empty message")
        void setToError_throwsExceptionForEmptyMessage() {
            assertThrows(IllegalArgumentException.class, () -> record.setToError(""));
            assertThrows(IllegalArgumentException.class, () -> record.setToError("   "));
        }

        @Test
        @DisplayName("setToError with date throws exception for null date")
        void setToErrorWithDate_throwsExceptionForNullDate() {
            assertThrows(IllegalArgumentException.class, () -> record.setToError("Error message", null));
        }
    }

    @Nested
    @DisplayName("Message Truncation Tests")
    class MessageTruncationTests {

        @Test
        @DisplayName("setMessage truncates long messages")
        void setMessage_truncatesLongMessages() {
            String longMessage = "A".repeat(5000);
            record.setMessage(longMessage);

            String message = record.getMessage();
            assertNotNull(message);
            assertTrue(message.length() <= 4000);
        }

        @Test
        @DisplayName("setMessage preserves short messages")
        void setMessage_preservesShortMessages() {
            String shortMessage = "Short message";
            record.setMessage(shortMessage);
            assertEquals(shortMessage, record.getMessage());
        }

        @Test
        @DisplayName("setMessage handles null")
        void setMessage_handlesNull() {
            record.setMessage(null);
            assertNull(record.getMessage());
        }
    }

    @Nested
    @DisplayName("Step Tests")
    class StepTests {

        @Test
        @DisplayName("step can be different from processStep")
        void step_canBeDifferentFromProcessStep() {
            record.setStep(5);
            record.setProcessStep(2);

            assertEquals(5, record.getStep());
            assertEquals(2, record.getProcessStep());
            assertNotEquals(record.getStep(), record.getProcessStep());
        }

        @Test
        @DisplayName("step can be zero")
        void step_canBeZero() {
            record.setStep(0);
            assertEquals(0, record.getStep());
        }

        @Test
        @DisplayName("processStep can be zero")
        void processStep_canBeZero() {
            record.setProcessStep(0);
            assertEquals(0, record.getProcessStep());
        }
    }

    @Nested
    @DisplayName("Equals, HashCode, and ToString Tests")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("equals returns true for same id")
        void equals_returnsTrueForSameId() {
            RequestHistoryRecord record1 = new RequestHistoryRecord();
            record1.setId(1);

            RequestHistoryRecord record2 = new RequestHistoryRecord();
            record2.setId(1);

            assertEquals(record1, record2);
        }

        @Test
        @DisplayName("equals returns false for different id")
        void equals_returnsFalseForDifferentId() {
            RequestHistoryRecord record1 = new RequestHistoryRecord();
            record1.setId(1);

            RequestHistoryRecord record2 = new RequestHistoryRecord();
            record2.setId(2);

            assertNotEquals(record1, record2);
        }

        @Test
        @DisplayName("equals returns false for null")
        void equals_returnsFalseForNull() {
            record.setId(1);
            assertFalse(record.equals(null));
        }

        @Test
        @DisplayName("equals returns false for different type")
        void equals_returnsFalseForDifferentType() {
            record.setId(1);
            assertNotEquals("not a record", record);
        }

        @Test
        @DisplayName("hashCode is consistent for same id")
        void hashCode_isConsistentForSameId() {
            RequestHistoryRecord record1 = new RequestHistoryRecord();
            record1.setId(1);

            RequestHistoryRecord record2 = new RequestHistoryRecord();
            record2.setId(1);

            assertEquals(record1.hashCode(), record2.hashCode());
        }

        @Test
        @DisplayName("toString contains id")
        void toString_containsId() {
            record.setId(42);
            String result = record.toString();
            assertTrue(result.contains("42"));
            assertTrue(result.contains("idEntry"));
        }
    }

    @Nested
    @DisplayName("Complete Record Configuration Tests")
    class CompleteConfigurationTests {

        @Test
        @DisplayName("fully configured record has all attributes")
        void fullyConfiguredRecord_hasAllAttributes() {
            Integer id = 1;
            String taskLabel = "FME Task";
            RequestHistoryRecord.Status status = RequestHistoryRecord.Status.FINISHED;
            Integer step = 3;
            Integer processStep = 2;
            String message = "Task completed successfully";
            Calendar startDate = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 0);
            Calendar endDate = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 30);
            Request request = new Request(1);
            User user = new User(1);

            record.setId(id);
            record.setTaskLabel(taskLabel);
            record.setStatus(status);
            record.setStep(step);
            record.setProcessStep(processStep);
            record.setMessage(message);
            record.setStartDate(startDate);
            record.setEndDate(endDate);
            record.setRequest(request);
            record.setUser(user);

            assertEquals(id, record.getId());
            assertEquals(taskLabel, record.getTaskLabel());
            assertEquals(status, record.getStatus());
            assertEquals(step, record.getStep());
            assertEquals(processStep, record.getProcessStep());
            assertEquals(message, record.getMessage());
            assertEquals(startDate, record.getStartDate());
            assertEquals(endDate, record.getEndDate());
            assertEquals(request, record.getRequest());
            assertEquals(user, record.getUser());
        }
    }

    @Nested
    @DisplayName("Date Relationship Tests")
    class DateRelationshipTests {

        @Test
        @DisplayName("end date can be after start date")
        void endDate_canBeAfterStartDate() {
            Calendar startDate = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 0);
            Calendar endDate = new GregorianCalendar(2024, Calendar.JANUARY, 15, 11, 0);

            record.setStartDate(startDate);
            record.setEndDate(endDate);

            assertTrue(record.getEndDate().after(record.getStartDate()));
        }

        @Test
        @DisplayName("start date can be null when end date is set")
        void startDate_canBeNullWhenEndDateSet() {
            Calendar endDate = new GregorianCalendar(2024, Calendar.JANUARY, 15, 11, 0);
            record.setEndDate(endDate);

            assertNull(record.getStartDate());
            assertNotNull(record.getEndDate());
        }

        @Test
        @DisplayName("end date can be null when start date is set")
        void endDate_canBeNullWhenStartDateSet() {
            Calendar startDate = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 0);
            record.setStartDate(startDate);

            assertNotNull(record.getStartDate());
            assertNull(record.getEndDate());
        }
    }
}
