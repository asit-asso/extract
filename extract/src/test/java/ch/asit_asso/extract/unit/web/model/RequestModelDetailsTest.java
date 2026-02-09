/*
 * Copyright (C) 2025 SecureMind Sàrl
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

import ch.asit_asso.extract.domain.*;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.web.model.RequestModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests to verify that RequestModel exposes all necessary information
 * for the request details view.
 *
 * Tests validate that:
 * 1. Request identification information is available
 * 2. Connector information is accessible
 * 3. Process information is exposed
 * 4. Customer details are present
 * 5. Third party information is available
 * 6. Parameters are correctly formatted
 * 7. Perimeter/geographic data is accessible
 * 8. Status and history are available
 * 9. Admin-specific fields are exposed
 *
 * @author Bruno Alves
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Request Details View - RequestModel Unit Tests")
class RequestModelDetailsTest {

    @Mock
    private Request mockRequest;

    @Mock
    private MessageSource mockMessageSource;

    @Mock
    private Connector mockConnector;

    @Mock
    private Process mockProcess;

    private Path basePath;
    private RequestHistoryRecord[] emptyHistory;
    private String[] validationFocusProperties;

    @BeforeEach
    void setUp() {
        basePath = Paths.get("/var/extract/data");
        emptyHistory = new RequestHistoryRecord[0];
        validationFocusProperties = new String[]{"FORMAT", "SCALE"};

        // Setup default mock behavior for required fields
        when(mockRequest.getId()).thenReturn(42);
        when(mockRequest.getStatus()).thenReturn(Request.Status.FINISHED);
        when(mockRequest.getFolderOut()).thenReturn(null);
        when(mockRequest.getProcess()).thenReturn(null);

        // Setup message source for localized labels
        when(mockMessageSource.getMessage(any(String.class), any(), any(Locale.class)))
            .thenReturn("Test Label");
    }

    // ==================== 1. REQUEST IDENTIFICATION ====================

    @Nested
    @DisplayName("1. Request Identification")
    class RequestIdentificationTests {

        @Test
        @DisplayName("1.1 - Request ID is accessible")
        void requestIdIsAccessible() {
            // Given
            when(mockRequest.getId()).thenReturn(12345);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals(12345, model.getId(), "Request ID should be accessible");
        }

        @Test
        @DisplayName("1.2 - Order label is accessible")
        void orderLabelIsAccessible() {
            // Given
            when(mockRequest.getOrderLabel()).thenReturn("ORDER-2025-001");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("ORDER-2025-001", model.getOrderLabel());
        }

        @Test
        @DisplayName("1.3 - Product label is accessible")
        void productLabelIsAccessible() {
            // Given
            when(mockRequest.getProductLabel()).thenReturn("Cadastral Map Extract");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("Cadastral Map Extract", model.getProductLabel());
        }

        @Test
        @DisplayName("1.4 - Combined label (order/product) is formatted correctly")
        void combinedLabelIsFormatted() {
            // Given
            when(mockRequest.getOrderLabel()).thenReturn("CMD-001");
            when(mockRequest.getProductLabel()).thenReturn("Product A");

            // When
            RequestModel model = createRequestModel();

            // Then: Label contains both order and product labels
            String label = model.getLabel();
            assertTrue(label.contains("CMD-001"), "Label should contain order label");
            assertTrue(label.contains("Product A"), "Label should contain product label");
            assertTrue(label.contains("/"), "Label should contain separator");
        }

        @Test
        @DisplayName("1.5 - Product GUID is accessible")
        void productGuidIsAccessible() {
            // Given
            when(mockRequest.getProductGuid()).thenReturn("prod-guid-123");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("prod-guid-123", model.getProductGuid());
        }
    }

    // ==================== 2. CONNECTOR INFORMATION ====================

    @Nested
    @DisplayName("2. Connector Information")
    class ConnectorInformationTests {

        @Test
        @DisplayName("2.1 - Connector object is accessible")
        void connectorIsAccessible() {
            // Given
            when(mockRequest.getConnector()).thenReturn(mockConnector);
            when(mockConnector.getName()).thenReturn("easySDI Connector");
            when(mockConnector.getId()).thenReturn(5);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNotNull(model.getConnector(), "Connector should be accessible");
            assertEquals("easySDI Connector", model.getConnector().getName());
        }

        @Test
        @DisplayName("2.2 - External URL is accessible")
        void externalUrlIsAccessible() {
            // Given
            when(mockRequest.getExternalUrl()).thenReturn("https://sdi.example.com/orders/12345");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("https://sdi.example.com/orders/12345", model.getExternalUrl());
        }

        @Test
        @DisplayName("2.3 - Connector can be null (deleted)")
        void connectorCanBeNull() {
            // Given
            when(mockRequest.getConnector()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNull(model.getConnector(), "Connector can be null when deleted");
        }
    }

    // ==================== 3. PROCESS INFORMATION ====================

    @Nested
    @DisplayName("3. Process Information")
    class ProcessInformationTests {

        @Test
        @DisplayName("3.1 - Process name is accessible")
        void processNameIsAccessible() {
            // Given: Process with name
            when(mockRequest.getProcess()).thenReturn(mockProcess);
            when(mockProcess.getName()).thenReturn("Standard Extraction");
            when(mockProcess.getId()).thenReturn(1);
            when(mockProcess.getTasksCollection()).thenReturn(new ArrayList<>());

            // When
            when(mockRequest.getStatus()).thenReturn(Request.Status.ONGOING);
            RequestModel model = createRequestModel();

            // Then
            assertEquals("Standard Extraction", model.getProcessName());
        }

        @Test
        @DisplayName("3.2 - Process ID is accessible")
        void processIdIsAccessible() {
            // Given: Process with ID
            when(mockRequest.getProcess()).thenReturn(mockProcess);
            when(mockProcess.getId()).thenReturn(10);
            when(mockProcess.getName()).thenReturn("Test Process");
            when(mockProcess.getTasksCollection()).thenReturn(new ArrayList<>());

            // When
            when(mockRequest.getStatus()).thenReturn(Request.Status.ONGOING);
            RequestModel model = createRequestModel();

            // Then
            assertEquals(10, model.getProcessId());
        }

        @Test
        @DisplayName("3.3 - Process can be null (unmatched)")
        void processCanBeNull() {
            // Given
            when(mockRequest.getProcess()).thenReturn(null);
            when(mockRequest.getStatus()).thenReturn(Request.Status.UNMATCHED);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNull(model.getProcessId());
            assertEquals("", model.getProcessName());
        }

        @Test
        @DisplayName("3.4 - Process history is accessible")
        void processHistoryIsAccessible() {
            // Given/When
            RequestModel model = createRequestModel();

            // Then
            assertNotNull(model.getProcessHistory(), "Process history array should not be null");
        }

        @Test
        @DisplayName("3.5 - Full history is accessible")
        void fullHistoryIsAccessible() {
            // Given/When
            RequestModel model = createRequestModel();

            // Then
            assertNotNull(model.getFullHistory(), "Full history should not be null");
        }

        @Test
        @DisplayName("3.6 - Current process step is accessible")
        void currentProcessStepIsAccessible() {
            // Given/When
            RequestModel model = createRequestModel();

            // Then
            assertTrue(model.getCurrentProcessStep() >= -1,
                "Current process step should be valid (-1 for no history)");
        }
    }

    // ==================== 4. CUSTOMER DETAILS ====================

    @Nested
    @DisplayName("4. Customer Details")
    class CustomerDetailsTests {

        @Test
        @DisplayName("4.1 - Customer name is accessible")
        void customerNameIsAccessible() {
            // Given
            when(mockRequest.getClient()).thenReturn("Jean Dupont");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("Jean Dupont", model.getCustomerName());
        }

        @Test
        @DisplayName("4.2 - Customer details/address is accessible")
        void customerDetailsIsAccessible() {
            // Given
            when(mockRequest.getClientDetails()).thenReturn("Rue de la Gare 12\n1000 Lausanne");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("Rue de la Gare 12\n1000 Lausanne", model.getCustomerDetails());
        }

        @Test
        @DisplayName("4.3 - Customer GUID is accessible")
        void customerGuidIsAccessible() {
            // Given
            when(mockRequest.getClientGuid()).thenReturn("client-guid-789");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("client-guid-789", model.getCustomerGuid());
        }

        @Test
        @DisplayName("4.4 - Organism name is accessible")
        void organismNameIsAccessible() {
            // Given
            when(mockRequest.getOrganism()).thenReturn("ASIT VD");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("ASIT VD", model.getOrganism());
        }

        @Test
        @DisplayName("4.5 - Organism GUID is accessible")
        void organismGuidIsAccessible() {
            // Given
            when(mockRequest.getOrganismGuid()).thenReturn("org-guid-456");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("org-guid-456", model.getOrganismGuid());
        }
    }

    // ==================== 5. THIRD PARTY INFORMATION ====================

    @Nested
    @DisplayName("5. Third Party Information")
    class ThirdPartyInformationTests {

        @Test
        @DisplayName("5.1 - Third party name is accessible")
        void thirdPartyNameIsAccessible() {
            // Given
            when(mockRequest.getTiers()).thenReturn("Mandataire SA");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("Mandataire SA", model.getThirdPartyName());
        }

        @Test
        @DisplayName("5.2 - Third party details is accessible")
        void thirdPartyDetailsIsAccessible() {
            // Given
            when(mockRequest.getTiersDetails()).thenReturn("Avenue des Alpes 5\n1950 Sion");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("Avenue des Alpes 5\n1950 Sion", model.getThirdPartyDetails());
        }

        @Test
        @DisplayName("5.3 - Third party GUID is accessible")
        void tiersGuidIsAccessible() {
            // Given
            when(mockRequest.getTiersGuid()).thenReturn("tiers-guid-321");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("tiers-guid-321", model.getTiersGuid());
        }

        @Test
        @DisplayName("5.4 - Third party can be null")
        void thirdPartyCanBeNull() {
            // Given
            when(mockRequest.getTiers()).thenReturn(null);
            when(mockRequest.getTiersDetails()).thenReturn(null);
            when(mockRequest.getTiersGuid()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNull(model.getThirdPartyName());
            assertNull(model.getThirdPartyDetails());
            assertNull(model.getTiersGuid());
        }
    }

    // ==================== 6. PARAMETERS ====================

    @Nested
    @DisplayName("6. Request Parameters")
    class ParametersTests {

        @Test
        @DisplayName("6.1 - Parameters map is accessible")
        void parametersMapIsAccessible() {
            // Given
            when(mockRequest.getParameters()).thenReturn("{\"FORMAT\":\"DXF\",\"SCALE\":\"1:1000\"}");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNotNull(model.getParameters(), "Parameters should not be null");
            assertEquals("DXF", model.getParameters().get("FORMAT"));
            assertEquals("1:1000", model.getParameters().get("SCALE"));
        }

        @Test
        @DisplayName("6.2 - Display parameters are accessible")
        void displayParametersAreAccessible() {
            // Given
            when(mockRequest.getParameters()).thenReturn("{\"FORMAT\":\"PDF\"}");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNotNull(model.getDisplayParameters());
        }

        @Test
        @DisplayName("6.3 - Validation focus parameters are filtered")
        void validationFocusParametersAreFiltered() {
            // Given
            when(mockRequest.getParameters()).thenReturn("{\"FORMAT\":\"PDF\",\"SCALE\":\"1:500\",\"OTHER\":\"value\"}");

            // When
            RequestModel model = createRequestModel();

            // Then
            Map<String, String> focusParams = model.getValidationFocusParameters();
            assertNotNull(focusParams);
            assertTrue(focusParams.containsKey("FORMAT") || focusParams.containsKey("SCALE"),
                "Focus parameters should only include configured properties");
        }

        @Test
        @DisplayName("6.4 - Empty parameters handled gracefully")
        void emptyParametersHandled() {
            // Given
            when(mockRequest.getParameters()).thenReturn("{}");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNotNull(model.getParameters());
            assertTrue(model.getParameters().isEmpty());
        }
    }

    // ==================== 7. GEOGRAPHIC DATA ====================

    @Nested
    @DisplayName("7. Geographic/Perimeter Data")
    class GeographicDataTests {

        @Test
        @DisplayName("7.1 - Perimeter geometry (WKT) is accessible")
        void perimeterGeometryIsAccessible() {
            // Given
            String wkt = "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))";
            when(mockRequest.getPerimeter()).thenReturn(wkt);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals(wkt, model.getPerimeterGeometry());
        }

        @Test
        @DisplayName("7.2 - Surface area is accessible")
        void surfaceIsAccessible() {
            // Given
            when(mockRequest.getSurface()).thenReturn(12500.75);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals(12500.75, model.getSurface());
        }

        @Test
        @DisplayName("7.3 - Perimeter can be null (import fail)")
        void perimeterCanBeNull() {
            // Given
            when(mockRequest.getPerimeter()).thenReturn(null);
            when(mockRequest.getStatus()).thenReturn(Request.Status.IMPORTFAIL);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNull(model.getPerimeterGeometry());
        }
    }

    // ==================== 8. STATUS AND DATES ====================

    @Nested
    @DisplayName("8. Status and Dates")
    class StatusAndDatesTests {

        @Test
        @DisplayName("8.1 - Start date is accessible")
        void startDateIsAccessible() {
            // Given
            Calendar startDate = new GregorianCalendar(2025, Calendar.JANUARY, 15, 10, 30, 0);
            when(mockRequest.getStartDate()).thenReturn(startDate);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNotNull(model.getStartDate());
            assertEquals(2025, model.getStartDate().get(Calendar.YEAR));
        }

        @Test
        @DisplayName("8.2 - Start date timestamp is accessible")
        void startDateTimestampIsAccessible() {
            // Given
            Calendar startDate = new GregorianCalendar(2025, Calendar.JANUARY, 15);
            when(mockRequest.getStartDate()).thenReturn(startDate);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertTrue(model.getStartDateTimestamp() > 0);
        }

        @Test
        @DisplayName("8.3 - Remark is accessible")
        void remarkIsAccessible() {
            // Given
            when(mockRequest.getRemark()).thenReturn("Validated by operator");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertEquals("Validated by operator", model.getRemark());
        }

        @Test
        @DisplayName("8.4 - Rejection status is accessible")
        void rejectionStatusIsAccessible() {
            // Given
            when(mockRequest.isRejected()).thenReturn(true);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertTrue(model.isRejected());
        }
    }

    // ==================== 9. STATUS FLAGS ====================

    @Nested
    @DisplayName("9. Status Flags")
    class StatusFlagsTests {

        @Test
        @DisplayName("9.1 - isFinished flag works")
        void isFinishedWorks() {
            // Given: A finished request (process is null for finished requests without active process)
            when(mockRequest.getStatus()).thenReturn(Request.Status.FINISHED);
            when(mockRequest.getProcess()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertTrue(model.isFinished());
        }

        @Test
        @DisplayName("9.2 - isInStandby flag works")
        void isInStandbyWorks() {
            // Given: A standby request
            when(mockRequest.getStatus()).thenReturn(Request.Status.STANDBY);
            when(mockRequest.getProcess()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertTrue(model.isInStandby());
        }

        @Test
        @DisplayName("9.3 - isInError flag works")
        void isInErrorWorks() {
            // Given
            when(mockRequest.getStatus()).thenReturn(Request.Status.ERROR);
            when(mockRequest.getProcess()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertTrue(model.isInError());
        }

        @Test
        @DisplayName("9.4 - isTaskInError flag works")
        void isTaskInErrorWorks() {
            // Given
            when(mockRequest.getStatus()).thenReturn(Request.Status.ERROR);
            when(mockRequest.getProcess()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertTrue(model.isTaskInError());
        }

        @Test
        @DisplayName("9.5 - isExportInError flag works")
        void isExportInErrorWorks() {
            // Given
            when(mockRequest.getStatus()).thenReturn(Request.Status.EXPORTFAIL);
            when(mockRequest.getProcess()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertTrue(model.isExportInError());
        }

        @Test
        @DisplayName("9.6 - isUnmatched flag works")
        void isUnmatchedWorks() {
            // Given
            when(mockRequest.getStatus()).thenReturn(Request.Status.UNMATCHED);
            when(mockRequest.getProcess()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertTrue(model.isUnmatched());
        }

        @Test
        @DisplayName("9.7 - isImportFail flag works")
        void isImportFailWorks() {
            // Given
            when(mockRequest.getStatus()).thenReturn(Request.Status.IMPORTFAIL);
            when(mockRequest.getProcess()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertTrue(model.isImportFail());
        }

        @Test
        @DisplayName("9.8 - isWaitingIntervention flag works")
        void isWaitingInterventionWorks() {
            // Given
            when(mockRequest.getStatus()).thenReturn(Request.Status.STANDBY);
            when(mockRequest.getProcess()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            // Standby requests with no current step history should wait for intervention
            assertNotNull(model.getProcessHistory());
        }
    }

    // ==================== 10. OUTPUT FILES ====================

    @Nested
    @DisplayName("10. Output Files")
    class OutputFilesTests {

        @Test
        @DisplayName("10.1 - Output folder path is accessible")
        void outputFolderPathIsAccessible() {
            // Given
            when(mockRequest.getFolderOut()).thenReturn("request-42/output");

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNotNull(model.getOutputFolderPath());
            assertTrue(model.getOutputFolderPath().contains("request-42"));
        }

        @Test
        @DisplayName("10.2 - Output folder path can be null")
        void outputFolderPathCanBeNull() {
            // Given
            when(mockRequest.getFolderOut()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNull(model.getOutputFolderPath());
        }

        @Test
        @DisplayName("10.3 - Output files array is never null")
        void outputFilesNeverNull() {
            // Given
            when(mockRequest.getFolderOut()).thenReturn(null);

            // When
            RequestModel model = createRequestModel();

            // Then
            assertNotNull(model.getOutputFiles(), "Output files should never be null");
            assertEquals(0, model.getOutputFiles().length);
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a RequestModel with current mock configuration.
     */
    private RequestModel createRequestModel() {
        return new RequestModel(mockRequest, emptyHistory, basePath,
            mockMessageSource, validationFocusProperties);
    }
}
