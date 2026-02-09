package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Rule;
import ch.asit_asso.extract.persistence.RequestsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("Connector Entity Tests")
@ExtendWith(MockitoExtension.class)
class ConnectorTest {

    private Connector connector;

    @Mock
    private RequestsRepository requestsRepository;

    @BeforeEach
    void setUp() {
        connector = new Connector();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null id")
        void defaultConstructor_createsInstanceWithNullId() {
            Connector newConnector = new Connector();
            assertNull(newConnector.getId());
        }

        @Test
        @DisplayName("Constructor with id sets the id correctly")
        void constructorWithId_setsIdCorrectly() {
            Integer expectedId = 42;
            Connector newConnector = new Connector(expectedId);
            assertEquals(expectedId, newConnector.getId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId work correctly")
        void setAndGetId() {
            Integer expectedId = 100;
            connector.setId(expectedId);
            assertEquals(expectedId, connector.getId());
        }

        @Test
        @DisplayName("setConnectorCode and getConnectorCode work correctly")
        void setAndGetConnectorCode() {
            String expectedCode = "EASYSDI_V4";
            connector.setConnectorCode(expectedCode);
            assertEquals(expectedCode, connector.getConnectorCode());
        }

        @Test
        @DisplayName("setConnectorLabel and getConnectorLabel work correctly")
        void setAndGetConnectorLabel() {
            String expectedLabel = "EasySDI v4 Connector";
            connector.setConnectorLabel(expectedLabel);
            assertEquals(expectedLabel, connector.getConnectorLabel());
        }

        @Test
        @DisplayName("setName and getName work correctly")
        void setAndGetName() {
            String expectedName = "Production Connector";
            connector.setName(expectedName);
            assertEquals(expectedName, connector.getName());
        }

        @Test
        @DisplayName("setImportFrequency and getImportFrequency work correctly")
        void setAndGetImportFrequency() {
            Integer expectedFrequency = 300;
            connector.setImportFrequency(expectedFrequency);
            assertEquals(expectedFrequency, connector.getImportFrequency());
        }

        @Test
        @DisplayName("setActive and isActive work correctly")
        void setAndIsActive() {
            connector.setActive(true);
            assertTrue(connector.isActive());

            connector.setActive(false);
            assertFalse(connector.isActive());
        }

        @Test
        @DisplayName("setLastImportDate and getLastImportDate work correctly")
        void setAndGetLastImportDate() {
            Calendar expectedDate = new GregorianCalendar(2024, Calendar.JANUARY, 15);
            connector.setLastImportDate(expectedDate);
            assertEquals(expectedDate, connector.getLastImportDate());
        }

        @Test
        @DisplayName("setLastImportMessage and getLastImportMessage work correctly")
        void setAndGetLastImportMessage() {
            String expectedMessage = "Import successful";
            connector.setLastImportMessage(expectedMessage);
            assertEquals(expectedMessage, connector.getLastImportMessage());
        }

        @Test
        @DisplayName("setMaximumRetries and getMaximumRetries work correctly")
        void setAndGetMaximumRetries() {
            connector.setMaximumRetries(5);
            assertEquals(5, connector.getMaximumRetries());
        }

        @Test
        @DisplayName("getMaximumRetries returns 0 when null")
        void getMaximumRetries_returnsZeroWhenNull() {
            assertEquals(0, connector.getMaximumRetries());
        }

        @Test
        @DisplayName("setErrorCount and getErrorCount work correctly")
        void setAndGetErrorCount() {
            connector.setErrorCount(3);
            assertEquals(3, connector.getErrorCount());
        }

        @Test
        @DisplayName("getErrorCount returns 0 when null")
        void getErrorCount_returnsZeroWhenNull() {
            assertEquals(0, connector.getErrorCount());
        }

        @Test
        @DisplayName("setRequestsCollection and getRequestsCollection work correctly")
        void setAndGetRequestsCollection() {
            Collection<Request> requests = new ArrayList<>();
            requests.add(new Request(1));
            requests.add(new Request(2));

            connector.setRequestsCollection(requests);
            assertEquals(2, connector.getRequestsCollection().size());
        }

        @Test
        @DisplayName("setRulesCollection and getRulesCollection work correctly")
        void setAndGetRulesCollection() {
            Collection<Rule> rules = new ArrayList<>();
            rules.add(new Rule(1));
            rules.add(new Rule(2));

            connector.setRulesCollection(rules);
            assertEquals(2, connector.getRulesCollection().size());
        }
    }

    @Nested
    @DisplayName("Parameters Values Tests")
    class ParametersValuesTests {

        @Test
        @DisplayName("setConnectorParametersValues adds parameters")
        void setConnectorParametersValues_addsParameters() {
            HashMap<String, String> params = new HashMap<>();
            params.put("url", "https://example.com");
            params.put("username", "admin");

            connector.setConnectorParametersValues(params);

            HashMap<String, String> result = connector.getConnectorParametersValues();
            assertEquals("https://example.com", result.get("url"));
            assertEquals("admin", result.get("username"));
        }

        @Test
        @DisplayName("setConnectorParametersValues throws exception for null")
        void setConnectorParametersValues_throwsExceptionForNull() {
            assertThrows(IllegalArgumentException.class, () -> connector.setConnectorParametersValues(null));
        }

        @Test
        @DisplayName("setConnectorParametersValues does nothing for empty map")
        void setConnectorParametersValues_doesNothingForEmptyMap() {
            connector.setConnectorParametersValues(new HashMap<>());
            assertNull(connector.getConnectorParametersValues());
        }

        @Test
        @DisplayName("setConnectorParametersValues adds to existing map")
        void setConnectorParametersValues_addsToExistingMap() {
            HashMap<String, String> params1 = new HashMap<>();
            params1.put("key1", "value1");
            connector.setConnectorParametersValues(params1);

            HashMap<String, String> params2 = new HashMap<>();
            params2.put("key2", "value2");
            connector.setConnectorParametersValues(params2);

            HashMap<String, String> result = connector.getConnectorParametersValues();
            assertEquals("value1", result.get("key1"));
            assertEquals("value2", result.get("key2"));
        }

        @Test
        @DisplayName("updateConnectorParametersValues delegates when map is empty")
        void updateConnectorParametersValues_delegatesWhenMapIsEmpty() {
            HashMap<String, String> params = new HashMap<>();
            params.put("key1", "value1");
            connector.updateConnectorParametersValues(params);

            assertEquals("value1", connector.getConnectorParametersValues().get("key1"));
        }

        @Test
        @DisplayName("updateConnectorParametersValues removes old keys")
        void updateConnectorParametersValues_removesOldKeys() {
            HashMap<String, String> params1 = new HashMap<>();
            params1.put("key1", "value1");
            params1.put("key2", "value2");
            connector.setConnectorParametersValues(params1);

            HashMap<String, String> params2 = new HashMap<>();
            params2.put("key1", "newValue1");
            connector.updateConnectorParametersValues(params2);

            HashMap<String, String> result = connector.getConnectorParametersValues();
            assertEquals("newValue1", result.get("key1"));
            assertNull(result.get("key2"));
        }

        @Test
        @DisplayName("updateConnectorParametersValues throws exception for null")
        void updateConnectorParametersValues_throwsExceptionForNull() {
            HashMap<String, String> params = new HashMap<>();
            params.put("key1", "value1");
            connector.setConnectorParametersValues(params);

            assertThrows(IllegalArgumentException.class, () -> connector.updateConnectorParametersValues(null));
        }

        @Test
        @DisplayName("updateConnectorParametersValues does nothing for empty map")
        void updateConnectorParametersValues_doesNothingForEmptyMap() {
            HashMap<String, String> params = new HashMap<>();
            params.put("key1", "value1");
            connector.setConnectorParametersValues(params);

            connector.updateConnectorParametersValues(new HashMap<>());

            assertEquals("value1", connector.getConnectorParametersValues().get("key1"));
        }
    }

    @Nested
    @DisplayName("IsInError Tests")
    class IsInErrorTests {

        @Test
        @DisplayName("isInError returns true when lastImportMessage is not empty")
        void isInError_returnsTrueWhenMessageNotEmpty() {
            connector.setLastImportMessage("Error occurred");
            assertTrue(connector.isInError());
        }

        @Test
        @DisplayName("isInError returns false when lastImportMessage is null")
        void isInError_returnsFalseWhenMessageIsNull() {
            connector.setLastImportMessage(null);
            assertFalse(connector.isInError());
        }

        @Test
        @DisplayName("isInError returns false when lastImportMessage is empty")
        void isInError_returnsFalseWhenMessageIsEmpty() {
            connector.setLastImportMessage("");
            assertFalse(connector.isInError());
        }
    }

    @Nested
    @DisplayName("HasActiveRequests Tests")
    class HasActiveRequestsTests {

        @Test
        @DisplayName("hasActiveRequests returns true when active request exists")
        void hasActiveRequests_returnsTrueWhenActiveExists() {
            Request activeRequest = new Request(1);
            activeRequest.setStatus(Request.Status.ONGOING);

            connector.setRequestsCollection(List.of(activeRequest));

            assertTrue(connector.hasActiveRequests());
        }

        @Test
        @DisplayName("hasActiveRequests returns false when all requests are finished")
        void hasActiveRequests_returnsFalseWhenAllFinished() {
            Request finishedRequest = new Request(1);
            finishedRequest.setStatus(Request.Status.FINISHED);

            connector.setRequestsCollection(List.of(finishedRequest));

            assertFalse(connector.hasActiveRequests());
        }

        @Test
        @DisplayName("hasActiveRequests with repository returns true when active exists")
        void hasActiveRequestsWithRepo_returnsTrueWhenActiveExists() {
            connector.setId(1);
            List<Request> activeRequests = new ArrayList<>();
            activeRequests.add(new Request(1));

            when(requestsRepository.findByConnectorAndStatusNot(eq(connector), eq(Request.Status.FINISHED)))
                    .thenReturn(activeRequests);

            assertTrue(connector.hasActiveRequests(requestsRepository));
        }

        @Test
        @DisplayName("hasActiveRequests with repository returns false when no active")
        void hasActiveRequestsWithRepo_returnsFalseWhenNoActive() {
            connector.setId(1);

            when(requestsRepository.findByConnectorAndStatusNot(eq(connector), eq(Request.Status.FINISHED)))
                    .thenReturn(Collections.emptyList());

            assertFalse(connector.hasActiveRequests(requestsRepository));
        }

        @Test
        @DisplayName("hasActiveRequests with null repository throws exception")
        void hasActiveRequestsWithRepo_throwsExceptionForNullRepo() {
            assertThrows(IllegalArgumentException.class, () -> connector.hasActiveRequests(null));
        }
    }

    @Nested
    @DisplayName("Equals, HashCode, and ToString Tests")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("equals returns true for same id")
        void equals_returnsTrueForSameId() {
            Connector connector1 = new Connector(1);
            Connector connector2 = new Connector(1);
            assertEquals(connector1, connector2);
        }

        @Test
        @DisplayName("equals returns false for different id")
        void equals_returnsFalseForDifferentId() {
            Connector connector1 = new Connector(1);
            Connector connector2 = new Connector(2);
            assertNotEquals(connector1, connector2);
        }

        @Test
        @DisplayName("equals returns false for null")
        void equals_returnsFalseForNull() {
            Connector connector1 = new Connector(1);
            assertNotEquals(null, connector1);
        }

        @Test
        @DisplayName("equals returns false for different type")
        void equals_returnsFalseForDifferentType() {
            Connector connector1 = new Connector(1);
            assertNotEquals("not a connector", connector1);
        }

        @Test
        @DisplayName("hashCode is consistent for same id")
        void hashCode_isConsistentForSameId() {
            Connector connector1 = new Connector(1);
            Connector connector2 = new Connector(1);
            assertEquals(connector1.hashCode(), connector2.hashCode());
        }

        @Test
        @DisplayName("hashCode is 0 for null id")
        void hashCode_isZeroForNullId() {
            Connector connector1 = new Connector();
            assertEquals(0, connector1.hashCode());
        }

        @Test
        @DisplayName("toString contains id")
        void toString_containsId() {
            Connector connector1 = new Connector(42);
            String result = connector1.toString();
            assertTrue(result.contains("42"));
            assertTrue(result.contains("idConnector"));
        }
    }
}
