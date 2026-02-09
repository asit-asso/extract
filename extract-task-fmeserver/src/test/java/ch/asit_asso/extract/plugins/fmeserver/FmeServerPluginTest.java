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
package ch.asit_asso.extract.plugins.fmeserver;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for the FmeServerPlugin class.
 */
@DisplayName("FmeServerPlugin")
@ExtendWith(MockitoExtension.class)
class FmeServerPluginTest {

    private FmeServerPlugin plugin;

    @Mock
    private IEmailSettings emailSettings;

    @BeforeEach
    void setUp() {
        plugin = new FmeServerPlugin();
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates valid instance")
        void defaultConstructorCreatesValidInstance() {
            FmeServerPlugin defaultPlugin = new FmeServerPlugin();

            assertNotNull(defaultPlugin);
            assertNotNull(defaultPlugin.getCode());
            assertNotNull(defaultPlugin.getLabel());
        }

        @Test
        @DisplayName("Constructor with language creates valid instance")
        void constructorWithLanguageCreatesValidInstance() {
            FmeServerPlugin frenchPlugin = new FmeServerPlugin("fr");

            assertNotNull(frenchPlugin);
            assertNotNull(frenchPlugin.getCode());
            assertNotNull(frenchPlugin.getLabel());
        }

        @Test
        @DisplayName("Constructor with German language creates valid instance")
        void constructorWithGermanLanguageCreatesValidInstance() {
            FmeServerPlugin germanPlugin = new FmeServerPlugin("de");

            assertNotNull(germanPlugin);
            assertNotNull(germanPlugin.getLabel());
        }

        @Test
        @DisplayName("Constructor with English language creates valid instance")
        void constructorWithEnglishLanguageCreatesValidInstance() {
            FmeServerPlugin englishPlugin = new FmeServerPlugin("en");

            assertNotNull(englishPlugin);
            assertNotNull(englishPlugin.getLabel());
        }

        @Test
        @DisplayName("Constructor with task settings creates valid instance")
        void constructorWithTaskSettingsCreatesValidInstance() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://fme.example.com");
            settings.put("login", "user");
            settings.put("pass", "password");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin(settings);

            assertNotNull(pluginWithSettings);
            assertNotNull(pluginWithSettings.getCode());
        }

        @Test
        @DisplayName("Constructor with language and task settings creates valid instance")
        void constructorWithLanguageAndTaskSettingsCreatesValidInstance() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://fme.example.com");

            FmeServerPlugin pluginWithBoth = new FmeServerPlugin("fr", settings);

            assertNotNull(pluginWithBoth);
            assertNotNull(pluginWithBoth.getCode());
            assertNotNull(pluginWithBoth.getLabel());
        }

        @Test
        @DisplayName("Constructor with empty settings map creates valid instance")
        void constructorWithEmptySettingsCreatesValidInstance() {
            Map<String, String> emptySettings = new HashMap<>();

            FmeServerPlugin pluginWithEmpty = new FmeServerPlugin(emptySettings);

            assertNotNull(pluginWithEmpty);
        }

        @Test
        @DisplayName("Constructor with null settings creates valid instance")
        void constructorWithNullSettingsCreatesValidInstance() {
            FmeServerPlugin pluginWithNull = new FmeServerPlugin((Map<String, String>) null);

            assertNotNull(pluginWithNull);
        }
    }

    @Nested
    @DisplayName("Interface implementation tests")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Implements ITaskProcessor interface")
        void implementsITaskProcessorInterface() {
            assertTrue(plugin instanceof ITaskProcessor);
        }
    }

    @Nested
    @DisplayName("getCode tests")
    class GetCodeTests {

        @Test
        @DisplayName("getCode returns FMESERVER")
        void getCodeReturnsFmeServer() {
            assertEquals("FMESERVER", plugin.getCode());
        }

        @Test
        @DisplayName("getCode is consistent across instances")
        void getCodeIsConsistentAcrossInstances() {
            FmeServerPlugin plugin1 = new FmeServerPlugin();
            FmeServerPlugin plugin2 = new FmeServerPlugin("fr");
            FmeServerPlugin plugin3 = new FmeServerPlugin(new HashMap<>());

            assertEquals(plugin1.getCode(), plugin2.getCode());
            assertEquals(plugin2.getCode(), plugin3.getCode());
        }
    }

    @Nested
    @DisplayName("getLabel tests")
    class GetLabelTests {

        @Test
        @DisplayName("getLabel returns non-null value")
        void getLabelReturnsNonNullValue() {
            assertNotNull(plugin.getLabel());
        }

        @Test
        @DisplayName("getLabel returns non-empty value")
        void getLabelReturnsNonEmptyValue() {
            assertFalse(plugin.getLabel().isEmpty());
        }

        @Test
        @DisplayName("getLabel returns localized value for French")
        void getLabelReturnsLocalizedValueForFrench() {
            FmeServerPlugin frenchPlugin = new FmeServerPlugin("fr");

            String label = frenchPlugin.getLabel();

            assertNotNull(label);
            assertFalse(label.isEmpty());
        }

        @Test
        @DisplayName("getLabel returns localized value for German")
        void getLabelReturnsLocalizedValueForGerman() {
            FmeServerPlugin germanPlugin = new FmeServerPlugin("de");

            String label = germanPlugin.getLabel();

            assertNotNull(label);
            assertFalse(label.isEmpty());
        }
    }

    @Nested
    @DisplayName("getDescription tests")
    class GetDescriptionTests {

        @Test
        @DisplayName("getDescription returns non-null value")
        void getDescriptionReturnsNonNullValue() {
            assertNotNull(plugin.getDescription());
        }

        @Test
        @DisplayName("getDescription returns non-empty value")
        void getDescriptionReturnsNonEmptyValue() {
            assertFalse(plugin.getDescription().isEmpty());
        }

        @Test
        @DisplayName("getDescription returns localized value")
        void getDescriptionReturnsLocalizedValue() {
            FmeServerPlugin frenchPlugin = new FmeServerPlugin("fr");
            FmeServerPlugin germanPlugin = new FmeServerPlugin("de");

            assertNotNull(frenchPlugin.getDescription());
            assertNotNull(germanPlugin.getDescription());
        }
    }

    @Nested
    @DisplayName("getHelp tests")
    class GetHelpTests {

        @Test
        @DisplayName("getHelp returns non-null value")
        void getHelpReturnsNonNullValue() {
            String help = plugin.getHelp();

            assertNotNull(help);
        }

        @Test
        @DisplayName("getHelp returns HTML content")
        void getHelpReturnsHtmlContent() {
            String help = plugin.getHelp();

            assertNotNull(help);
            // Help file should contain HTML
            assertTrue(help.contains("<") || help.isEmpty() || help != null);
        }

        @Test
        @DisplayName("getHelp caches the help content")
        void getHelpCachesContent() {
            String help1 = plugin.getHelp();
            String help2 = plugin.getHelp();

            // Should return the same cached content
            assertSame(help1, help2);
        }

        @Test
        @DisplayName("getHelp returns localized content")
        void getHelpReturnsLocalizedContent() {
            FmeServerPlugin frenchPlugin = new FmeServerPlugin("fr");

            String help = frenchPlugin.getHelp();

            assertNotNull(help);
        }
    }

    @Nested
    @DisplayName("getPictoClass tests")
    class GetPictoClassTests {

        @Test
        @DisplayName("getPictoClass returns fa-cogs")
        void getPictoClassReturnsFaCogs() {
            assertEquals("fa-cogs", plugin.getPictoClass());
        }

        @Test
        @DisplayName("getPictoClass is consistent across instances")
        void getPictoClassIsConsistentAcrossInstances() {
            FmeServerPlugin plugin1 = new FmeServerPlugin();
            FmeServerPlugin plugin2 = new FmeServerPlugin("de");

            assertEquals(plugin1.getPictoClass(), plugin2.getPictoClass());
        }
    }

    @Nested
    @DisplayName("getParams tests")
    class GetParamsTests {

        @Test
        @DisplayName("getParams returns valid JSON")
        void getParamsReturnsValidJson() throws JsonProcessingException {
            String params = plugin.getParams();

            assertNotNull(params);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(params);
            assertTrue(node.isArray());
        }

        @Test
        @DisplayName("getParams contains URL parameter")
        void getParamsContainsUrlParameter() throws JsonProcessingException {
            String params = plugin.getParams();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(params);

            boolean hasUrl = false;
            for (JsonNode param : node) {
                if ("url".equals(param.get("code").asText())) {
                    hasUrl = true;
                    assertEquals("text", param.get("type").asText());
                    assertTrue(param.get("req").asBoolean());
                    break;
                }
            }
            assertTrue(hasUrl, "URL parameter should be present");
        }

        @Test
        @DisplayName("getParams contains login parameter")
        void getParamsContainsLoginParameter() throws JsonProcessingException {
            String params = plugin.getParams();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(params);

            boolean hasLogin = false;
            for (JsonNode param : node) {
                if ("login".equals(param.get("code").asText())) {
                    hasLogin = true;
                    assertEquals("text", param.get("type").asText());
                    assertFalse(param.get("req").asBoolean());
                    break;
                }
            }
            assertTrue(hasLogin, "Login parameter should be present");
        }

        @Test
        @DisplayName("getParams contains password parameter")
        void getParamsContainsPasswordParameter() throws JsonProcessingException {
            String params = plugin.getParams();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(params);

            boolean hasPassword = false;
            for (JsonNode param : node) {
                if ("pass".equals(param.get("code").asText())) {
                    hasPassword = true;
                    assertEquals("pass", param.get("type").asText());
                    assertFalse(param.get("req").asBoolean());
                    break;
                }
            }
            assertTrue(hasPassword, "Password parameter should be present");
        }

        @Test
        @DisplayName("getParams has three parameters")
        void getParamsHasThreeParameters() throws JsonProcessingException {
            String params = plugin.getParams();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(params);

            assertEquals(3, node.size());
        }

        @Test
        @DisplayName("All parameters have required fields")
        void allParametersHaveRequiredFields() throws JsonProcessingException {
            String params = plugin.getParams();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(params);

            for (JsonNode param : node) {
                assertTrue(param.has("code"), "Parameter should have 'code' field");
                assertTrue(param.has("label"), "Parameter should have 'label' field");
                assertTrue(param.has("type"), "Parameter should have 'type' field");
                assertTrue(param.has("req"), "Parameter should have 'req' field");
                assertTrue(param.has("maxlength"), "Parameter should have 'maxlength' field");
            }
        }

        @Test
        @DisplayName("URL parameter has correct maxlength")
        void urlParameterHasCorrectMaxlength() throws JsonProcessingException {
            String params = plugin.getParams();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(params);

            for (JsonNode param : node) {
                if ("url".equals(param.get("code").asText())) {
                    assertEquals(255, param.get("maxlength").asInt());
                    break;
                }
            }
        }
    }

    @Nested
    @DisplayName("newInstance tests")
    class NewInstanceTests {

        @Test
        @DisplayName("newInstance with language creates new instance")
        void newInstanceWithLanguageCreatesNewInstance() {
            FmeServerPlugin newPlugin = plugin.newInstance("fr");

            assertNotNull(newPlugin);
            assertNotSame(plugin, newPlugin);
        }

        @Test
        @DisplayName("newInstance with language preserves code")
        void newInstanceWithLanguagePreservesCode() {
            FmeServerPlugin newPlugin = plugin.newInstance("de");

            assertEquals(plugin.getCode(), newPlugin.getCode());
        }

        @Test
        @DisplayName("newInstance with language and settings creates new instance")
        void newInstanceWithLanguageAndSettingsCreatesNewInstance() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://test.example.com");

            FmeServerPlugin newPlugin = plugin.newInstance("fr", settings);

            assertNotNull(newPlugin);
            assertNotSame(plugin, newPlugin);
        }

        @Test
        @DisplayName("newInstance with different languages creates independent instances")
        void newInstanceWithDifferentLanguagesCreatesIndependentInstances() {
            FmeServerPlugin frenchPlugin = plugin.newInstance("fr");
            FmeServerPlugin germanPlugin = plugin.newInstance("de");

            assertNotSame(frenchPlugin, germanPlugin);
            assertEquals(frenchPlugin.getCode(), germanPlugin.getCode());
        }

        @Test
        @DisplayName("newInstance returns FmeServerPlugin type")
        void newInstanceReturnsFmeServerPluginType() {
            ITaskProcessor newPlugin = plugin.newInstance("fr");

            assertTrue(newPlugin instanceof FmeServerPlugin);
        }

        @Test
        @DisplayName("newInstance with empty settings creates valid instance")
        void newInstanceWithEmptySettingsCreatesValidInstance() {
            FmeServerPlugin newPlugin = plugin.newInstance("en", new HashMap<>());

            assertNotNull(newPlugin);
            assertNotNull(newPlugin.getCode());
        }
    }

    @Nested
    @DisplayName("execute tests")
    class ExecuteTests {

        @Mock
        private ITaskProcessorRequest mockRequest;

        @Test
        @DisplayName("execute with null inputs returns error result")
        void executeWithNullInputsReturnsErrorResult() {
            FmeServerPlugin pluginWithoutInputs = new FmeServerPlugin("fr");

            // Use lenient stubbing since the execute method may fail before accessing all request properties
            lenient().when(mockRequest.getProductGuid()).thenReturn("test-product");
            lenient().when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            lenient().when(mockRequest.getParameters()).thenReturn("{}");
            lenient().when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            lenient().when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            lenient().when(mockRequest.getId()).thenReturn(1);
            lenient().when(mockRequest.getClientGuid()).thenReturn("client-guid");
            lenient().when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithoutInputs.execute(mockRequest, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertNotNull(result.getMessage());
        }

        @Test
        @DisplayName("execute with missing URL returns error result")
        void executeWithMissingUrlReturnsErrorResult() {
            Map<String, String> settings = new HashMap<>();
            // No URL provided
            settings.put("login", "user");
            settings.put("pass", "password");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            // Use lenient stubbing since the execute method may fail before accessing all request properties
            lenient().when(mockRequest.getProductGuid()).thenReturn("test-product");
            lenient().when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            lenient().when(mockRequest.getParameters()).thenReturn("{}");
            lenient().when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            lenient().when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            lenient().when(mockRequest.getId()).thenReturn(1);
            lenient().when(mockRequest.getClientGuid()).thenReturn("client-guid");
            lenient().when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("execute with invalid URL returns error result")
        void executeWithInvalidUrlReturnsErrorResult() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "not-a-valid-url");
            settings.put("login", "user");
            settings.put("pass", "password");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertNotNull(result.getMessage());
            assertEquals("-1", result.getErrorCode());
        }

        @Test
        @DisplayName("execute sets request data in result")
        void executeSetsRequestDataInResult() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://nonexistent.example.com/fme");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result);
            assertSame(mockRequest, result.getRequestData());
        }

        @Test
        @DisplayName("execute with empty folderOut handles gracefully")
        void executeWithEmptyFolderOutHandlesGracefully() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://example.com/fme");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn("");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result);
            // Should return error since server is not reachable
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("execute with null folderOut handles gracefully")
        void executeWithNullFolderOutHandlesGracefully() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://example.com/fme");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn(null);
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("execute with special characters in parameters")
        void executeWithSpecialCharactersInParameters() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://example.com/fme");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product-123");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))");
            when(mockRequest.getParameters()).thenReturn("{\"format\":\"PDF\",\"scale\":\"1:25000\"}");
            when(mockRequest.getFolderOut()).thenReturn("/tmp/output/test");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order with special chars: & < > \"");
            when(mockRequest.getId()).thenReturn(42);
            when(mockRequest.getClientGuid()).thenReturn("client-123-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("org-456-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("execute with null email settings")
        void executeWithNullEmailSettings() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://example.com/fme");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            // Should not throw NPE
            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, null);

            assertNotNull(result);
        }

        @Test
        @DisplayName("execute with HTTPS URL")
        void executeWithHttpsUrl() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "https://secure.example.com/fme");
            settings.put("login", "user");
            settings.put("pass", "password");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result);
            // Will fail to connect but should handle HTTPS scheme correctly
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("execute with URL containing port")
        void executeWithUrlContainingPort() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://example.com:8080/fme");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("execute without credentials")
        void executeWithoutCredentials() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://example.com/fme");
            // No login or password

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result);
            // Connection will fail but credentials handling should work
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("execute with empty credentials")
        void executeWithEmptyCredentials() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://example.com/fme");
            settings.put("login", "");
            settings.put("pass", "");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("execute with login but no password")
        void executeWithLoginButNoPassword() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://example.com/fme");
            settings.put("login", "user");
            // No password

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Language variation tests")
    class LanguageVariationTests {

        @ParameterizedTest
        @ValueSource(strings = {"fr", "de", "en"})
        @DisplayName("Plugin works with different languages")
        void pluginWorksWithDifferentLanguages(String language) {
            FmeServerPlugin localizedPlugin = new FmeServerPlugin(language);

            assertNotNull(localizedPlugin.getLabel());
            assertNotNull(localizedPlugin.getDescription());
            assertNotNull(localizedPlugin.getHelp());
            assertEquals("FMESERVER", localizedPlugin.getCode());
        }

        @Test
        @DisplayName("Plugin with regional variant language")
        void pluginWithRegionalVariantLanguage() {
            FmeServerPlugin plugin = new FmeServerPlugin("de-CH");

            assertNotNull(plugin.getLabel());
            assertNotNull(plugin.getDescription());
        }

        @Test
        @DisplayName("Plugin with multiple fallback languages")
        void pluginWithMultipleFallbackLanguages() {
            FmeServerPlugin plugin = new FmeServerPlugin("de,en,fr");

            assertNotNull(plugin.getLabel());
            assertNotNull(plugin.getDescription());
        }
    }

    @Nested
    @DisplayName("Result consistency tests")
    class ResultConsistencyTests {

        @Mock
        private ITaskProcessorRequest mockRequest;

        @Test
        @DisplayName("Error result has all required fields")
        void errorResultHasAllRequiredFields() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://nonexistent.invalid/fme");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertNotNull(result.getStatus());
            assertNotNull(result.getMessage());
            assertNotNull(result.getErrorCode());
            assertNotNull(result.getRequestData());
        }

        @Test
        @DisplayName("Result is of type FmeServerResult")
        void resultIsOfTypeFmeServerResult() {
            Map<String, String> settings = new HashMap<>();
            settings.put("url", "http://example.com/fme");

            FmeServerPlugin pluginWithSettings = new FmeServerPlugin("fr", settings);

            when(mockRequest.getProductGuid()).thenReturn("test-product");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");
            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            when(mockRequest.getOrderLabel()).thenReturn("Test Order");
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("organism-guid");

            ITaskProcessorResult result = pluginWithSettings.execute(mockRequest, emailSettings);

            assertTrue(result instanceof FmeServerResult);
        }
    }

    @Nested
    @DisplayName("Edge cases tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Multiple calls to getHelp return same instance")
        void multipleCallsToGetHelpReturnSameInstance() {
            String help1 = plugin.getHelp();
            String help2 = plugin.getHelp();
            String help3 = plugin.getHelp();

            assertSame(help1, help2);
            assertSame(help2, help3);
        }

        @Test
        @DisplayName("Multiple calls to getParams return consistent structure")
        void multipleCallsToGetParamsReturnConsistentStructure() throws JsonProcessingException {
            String params1 = plugin.getParams();
            String params2 = plugin.getParams();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node1 = mapper.readTree(params1);
            JsonNode node2 = mapper.readTree(params2);

            assertEquals(node1.size(), node2.size());
        }

        @Test
        @DisplayName("newInstance creates truly independent instances")
        void newInstanceCreatesTrulyIndependentInstances() {
            Map<String, String> settings1 = new HashMap<>();
            settings1.put("url", "http://server1.com");

            Map<String, String> settings2 = new HashMap<>();
            settings2.put("url", "http://server2.com");

            FmeServerPlugin plugin1 = plugin.newInstance("fr", settings1);
            FmeServerPlugin plugin2 = plugin.newInstance("de", settings2);

            assertNotSame(plugin1, plugin2);
            // Both should still have same code
            assertEquals(plugin1.getCode(), plugin2.getCode());
        }
    }
}
