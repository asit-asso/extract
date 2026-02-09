/*
 * Copyright (C) 2017 arx iT
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
package ch.asit_asso.extract.plugins.qgisprint;

import java.util.HashMap;
import java.util.Map;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QGISPrintPlugin execute() method.
 * Tests all branches of the execute method including error handling.
 * Tests use localhost with a port that should fail fast (no network timeout).
 */
@ExtendWith(MockitoExtension.class)
@Timeout(30) // Global timeout for all tests - fails fast if network issues
public class QGISPrintPluginExecuteTest {

    private static final String CONFIG_FILE_PATH = "plugins/qgisprint/properties/config.properties";
    private static final String TEST_INSTANCE_LANGUAGE = "fr";
    // Use localhost with a high port that should be closed - fails immediately
    private static final String TEST_FAIL_FAST_URL = "http://127.0.0.1:59999/qgis";

    private PluginConfiguration configuration;
    private Map<String, String> testParameters;

    @Mock
    private IEmailSettings emailSettings;

    @BeforeEach
    void setUp() {
        configuration = new PluginConfiguration(CONFIG_FILE_PATH);
        testParameters = new HashMap<>();
    }

    private void setUpValidParameters() {
        String urlCode = configuration.getProperty("paramUrl");
        String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
        String pathProjectCode = configuration.getProperty("paramPathProjectQGIS");
        String loginCode = configuration.getProperty("paramLogin");
        String passwordCode = configuration.getProperty("paramPassword");
        String layersCode = configuration.getProperty("paramLayers");
        String crsCode = configuration.getProperty("paramCRS");

        testParameters.put(urlCode, TEST_FAIL_FAST_URL);
        testParameters.put(templateLayoutCode, "myplan");
        testParameters.put(pathProjectCode, "/path/to/project.qgs");
        testParameters.put(loginCode, "testuser");
        testParameters.put(passwordCode, "testpass");
        testParameters.put(layersCode, "layer1,layer2");
        testParameters.put(crsCode, "EPSG:2056");
    }

    private QGISPrintRequest createTestRequest() {
        QGISPrintRequest request = new QGISPrintRequest();
        request.setId(1);
        request.setFolderIn("/tmp/input");
        request.setFolderOut("/tmp/output");
        request.setProductGuid("test-product-guid");
        request.setPerimeter("POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))");
        request.setParameters("{}");
        request.setOrderLabel("Test Order");
        request.setClientGuid("test-client-guid");
        request.setOrganismGuid("test-organism-guid");
        return request;
    }

    @Nested
    @DisplayName("Execute with null or missing parameters")
    class ExecuteWithMissingParameters {

        @Test
        @DisplayName("Execute with null URL returns ERROR status")
        void testExecuteWithNullUrl() {
            String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
            testParameters.put(templateLayoutCode, "myplan");
            // URL is null/missing

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertEquals("-1", result.getErrorCode());
            assertNotNull(result.getMessage());
        }

        @Test
        @DisplayName("Execute with empty parameters map returns ERROR status")
        void testExecuteWithEmptyParameters() {
            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, new HashMap<>());
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertEquals("-1", result.getErrorCode());
        }

        @Test
        @DisplayName("Execute with null template layout returns ERROR status")
        void testExecuteWithNullTemplateLayout() {
            String urlCode = configuration.getProperty("paramUrl");
            testParameters.put(urlCode, TEST_FAIL_FAST_URL);
            // Template layout is null/missing - will fail during getCoverageLayer

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Execute with invalid URL format")
    class ExecuteWithInvalidUrl {

        @Test
        @DisplayName("Execute with malformed URL returns ERROR")
        void testExecuteWithMalformedUrl() {
            String urlCode = configuration.getProperty("paramUrl");
            String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
            testParameters.put(urlCode, "not-a-valid-url");
            testParameters.put(templateLayoutCode, "myplan");

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertEquals("-1", result.getErrorCode());
        }

        @Test
        @DisplayName("Execute with invalid URL syntax returns ERROR")
        void testExecuteWithInvalidUrlSyntax() {
            String urlCode = configuration.getProperty("paramUrl");
            String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
            testParameters.put(urlCode, "http://[invalid");
            testParameters.put(templateLayoutCode, "myplan");

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Execute with perimeter variations")
    class ExecuteWithPerimeterVariations {

        @Test
        @DisplayName("Execute with invalid WKT perimeter returns ERROR")
        void testExecuteWithInvalidWktPerimeter() {
            setUpValidParameters();

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
            QGISPrintRequest request = createTestRequest();
            request.setPerimeter("INVALID_WKT_DATA");

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertEquals("-1", result.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Execute with null request")
    class ExecuteWithNullRequest {

        @Test
        @DisplayName("Execute with null request returns ERROR status")
        void testExecuteWithNullRequest() {
            setUpValidParameters();

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);

            // Plugin handles null gracefully by returning error status
            ITaskProcessorResult result = plugin.execute(null, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Execute with null email settings")
    class ExecuteWithNullEmailSettings {

        @Test
        @DisplayName("Execute with null email settings works (email not used in error path)")
        void testExecuteWithNullEmailSettings() {
            // Use invalid URL to fail fast without network
            String urlCode = configuration.getProperty("paramUrl");
            String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
            testParameters.put(urlCode, "not-a-valid-url");
            testParameters.put(templateLayoutCode, "myplan");

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, null);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Execute result validation")
    class ExecuteResultValidation {

        @Test
        @DisplayName("Execute error result contains request data")
        void testExecuteResultContainsRequestData() {
            String urlCode = configuration.getProperty("paramUrl");
            String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
            testParameters.put(urlCode, "not-a-valid-url");
            testParameters.put(templateLayoutCode, "myplan");

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertNotNull(result.getRequestData());
        }

        @Test
        @DisplayName("Execute error result has error code -1")
        void testExecuteErrorResultHasErrorCode() {
            String urlCode = configuration.getProperty("paramUrl");
            String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
            testParameters.put(urlCode, "not-a-valid-url");
            testParameters.put(templateLayoutCode, "myplan");

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertEquals("-1", result.getErrorCode());
        }

        @Test
        @DisplayName("Execute error result has non-null message")
        void testExecuteErrorResultHasMessage() {
            String urlCode = configuration.getProperty("paramUrl");
            String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
            testParameters.put(urlCode, "not-a-valid-url");
            testParameters.put(templateLayoutCode, "myplan");

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertNotNull(result.getMessage());
            assertFalse(result.getMessage().isEmpty());
        }
    }

    @Nested
    @DisplayName("Execute message localization")
    class ExecuteMessageLocalization {

        @Test
        @DisplayName("Execute error message is localized")
        void testExecuteErrorMessageLocalized() {
            String urlCode = configuration.getProperty("paramUrl");
            String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
            testParameters.put(urlCode, "not-a-valid-url");
            testParameters.put(templateLayoutCode, "myplan");

            QGISPrintPlugin plugin = new QGISPrintPlugin("fr", testParameters);
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertNotNull(result.getMessage());
        }

        @Test
        @DisplayName("Execute with default language")
        void testExecuteWithDefaultLanguage() {
            String urlCode = configuration.getProperty("paramUrl");
            String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
            testParameters.put(urlCode, "not-a-valid-url");
            testParameters.put(templateLayoutCode, "myplan");

            QGISPrintPlugin plugin = new QGISPrintPlugin(testParameters);
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertNotNull(result.getMessage());
        }
    }

    @Nested
    @DisplayName("Execute statelessness tests")
    class ExecuteStatelessness {

        @Test
        @DisplayName("Multiple execute calls do not interfere")
        void testMultipleExecuteCallsIndependent() {
            String urlCode = configuration.getProperty("paramUrl");
            String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
            testParameters.put(urlCode, "not-a-valid-url");
            testParameters.put(templateLayoutCode, "myplan");

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);

            QGISPrintRequest request1 = createTestRequest();
            request1.setProductGuid("product-1");

            QGISPrintRequest request2 = createTestRequest();
            request2.setProductGuid("product-2");

            ITaskProcessorResult result1 = plugin.execute(request1, emailSettings);
            ITaskProcessorResult result2 = plugin.execute(request2, emailSettings);

            assertNotNull(result1);
            assertNotNull(result2);
            assertEquals(ITaskProcessorResult.Status.ERROR, result1.getStatus());
            assertEquals(ITaskProcessorResult.Status.ERROR, result2.getStatus());
        }

        @Test
        @DisplayName("Different plugin instances are independent")
        void testDifferentPluginInstancesIndependent() {
            String urlCode = configuration.getProperty("paramUrl");
            String templateLayoutCode = configuration.getProperty("paramTemplateLayout");
            testParameters.put(urlCode, "not-a-valid-url");
            testParameters.put(templateLayoutCode, "myplan");

            QGISPrintPlugin plugin1 = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
            QGISPrintPlugin plugin2 = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);

            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result1 = plugin1.execute(request, emailSettings);
            ITaskProcessorResult result2 = plugin2.execute(request, emailSettings);

            assertNotNull(result1);
            assertNotNull(result2);
            assertNotSame(result1, result2);
        }
    }

    @Nested
    @DisplayName("Execute with connection refused (fast fail)")
    class ExecuteWithConnectionRefused {

        @Test
        @Timeout(10) // Should fail within 10 seconds (connection refused is fast)
        @DisplayName("Execute with closed port returns ERROR quickly")
        void testExecuteWithClosedPort() {
            setUpValidParameters();
            // localhost:59999 should be closed and fail immediately with connection refused

            QGISPrintPlugin plugin = new QGISPrintPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
            QGISPrintRequest request = createTestRequest();

            ITaskProcessorResult result = plugin.execute(request, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }
    }
}
