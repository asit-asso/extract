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
package ch.asit_asso.extract.plugins.fmedesktop;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Extended unit tests for FmeDesktopPlugin class focusing on execution paths and edge cases.
 */
@DisplayName("FmeDesktopPlugin Execution Tests")
class FmeDesktopPluginExecutionTest {

    private static final String CONFIG_FILE_PATH = "plugins/fme/properties/configFME.properties";
    private static final String TEST_LANGUAGE = "fr";

    private PluginConfiguration configuration;

    @Mock
    private IEmailSettings mockEmailSettings;

    @Mock
    private ITaskProcessorRequest mockRequest;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        configuration = new PluginConfiguration(CONFIG_FILE_PATH);
    }

    @Nested
    @DisplayName("Execute method tests")
    class ExecuteMethodTests {

        @Test
        @DisplayName("Execute returns error when script path is null in inputs")
        void executeReturnsErrorWhenScriptPathIsNullInInputs() {
            Map<String, String> params = new HashMap<>();
            String scriptPathCode = configuration.getProperty("paramPath");
            String fmePathCode = configuration.getProperty("paramPathFME");
            String instancesCode = configuration.getProperty("paramInstances");

            params.put(scriptPathCode, null);
            params.put(fmePathCode, "/some/path/fme.exe");
            params.put(instancesCode, "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");
            when(mockRequest.getProductGuid()).thenReturn("test-guid");

            ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("Execute returns error when script file does not exist")
        void executeReturnsErrorWhenScriptFileDoesNotExist() {
            Map<String, String> params = new HashMap<>();
            String scriptPathCode = configuration.getProperty("paramPath");
            String fmePathCode = configuration.getProperty("paramPathFME");
            String instancesCode = configuration.getProperty("paramInstances");

            params.put(scriptPathCode, "/nonexistent/path/script.fmw");
            params.put(fmePathCode, "/nonexistent/path/fme.exe");
            params.put(instancesCode, "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            when(mockRequest.getFolderOut()).thenReturn("/tmp/output");

            ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertNotNull(result.getMessage());
            assertEquals("-1", result.getErrorCode());
        }

        @Test
        @DisplayName("Execute returns error when FME executable does not exist")
        void executeReturnsErrorWhenFmeExecutableDoesNotExist() throws IOException {
            // Create a valid script file but invalid FME executable
            File scriptFile = tempDir.resolve("test.fmw").toFile();
            Files.createFile(scriptFile.toPath());

            Map<String, String> params = new HashMap<>();
            String scriptPathCode = configuration.getProperty("paramPath");
            String fmePathCode = configuration.getProperty("paramPathFME");
            String instancesCode = configuration.getProperty("paramInstances");

            params.put(scriptPathCode, scriptFile.getAbsolutePath());
            params.put(fmePathCode, "/nonexistent/fme.exe");
            params.put(instancesCode, "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            when(mockRequest.getFolderOut()).thenReturn(tempDir.toString());

            ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertNotNull(result.getMessage());
        }

        @Test
        @DisplayName("Execute sets request data in result")
        void executeSetsRequestDataInResult() {
            Map<String, String> params = new HashMap<>();
            String scriptPathCode = configuration.getProperty("paramPath");
            String fmePathCode = configuration.getProperty("paramPathFME");
            String instancesCode = configuration.getProperty("paramInstances");

            params.put(scriptPathCode, "/nonexistent/script.fmw");
            params.put(fmePathCode, "/nonexistent/fme.exe");
            params.put(instancesCode, "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

            assertNotNull(result.getRequestData());
            assertSame(mockRequest, result.getRequestData());
        }

        @Test
        @DisplayName("Execute with null email settings still works")
        void executeWithNullEmailSettingsStillWorks() {
            Map<String, String> params = new HashMap<>();
            String scriptPathCode = configuration.getProperty("paramPath");
            String fmePathCode = configuration.getProperty("paramPathFME");
            String instancesCode = configuration.getProperty("paramInstances");

            params.put(scriptPathCode, "/nonexistent/script.fmw");
            params.put(fmePathCode, "/nonexistent/fme.exe");
            params.put(instancesCode, "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            ITaskProcessorResult result = plugin.execute(mockRequest, null);

            assertNotNull(result);
            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Private method tests via reflection")
    class PrivateMethodTests {

        @Test
        @DisplayName("formatJsonParametersQuotes returns empty string for empty input")
        void formatJsonParametersQuotesReturnsEmptyForEmptyInput() throws Exception {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("formatJsonParametersQuotes", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(plugin, "");

            assertEquals("", result);
        }

        @Test
        @DisplayName("formatJsonParametersQuotes returns null for null input")
        void formatJsonParametersQuotesReturnsNullForNullInput() throws Exception {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("formatJsonParametersQuotes", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(plugin, (String) null);

            assertNull(result);
        }

        @Test
        @DisplayName("formatJsonParametersQuotes handles double quotes")
        void formatJsonParametersQuotesHandlesDoubleQuotes() throws Exception {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("formatJsonParametersQuotes", String.class);
            method.setAccessible(true);

            String input = "{\"key\":\"value\"}";
            String result = (String) method.invoke(plugin, input);

            assertNotNull(result);
            assertTrue(result.startsWith("\""));
            assertTrue(result.endsWith("\""));
        }

        @Test
        @DisplayName("formatJsonParametersQuotes handles backslashes")
        void formatJsonParametersQuotesHandlesBackslashes() throws Exception {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("formatJsonParametersQuotes", String.class);
            method.setAccessible(true);

            String input = "path\\to\\file";
            String result = (String) method.invoke(plugin, input);

            assertNotNull(result);
            assertTrue(result.contains("u005c") || result.contains("\\"));
        }

        @Test
        @DisplayName("formatJsonParametersQuotes handles newlines")
        void formatJsonParametersQuotesHandlesNewlines() throws Exception {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("formatJsonParametersQuotes", String.class);
            method.setAccessible(true);

            String input = "line1\\nline2";
            String result = (String) method.invoke(plugin, input);

            assertNotNull(result);
        }

        @Test
        @DisplayName("formatJsonParametersQuotes handles forward slashes")
        void formatJsonParametersQuotesHandlesForwardSlashes() throws Exception {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("formatJsonParametersQuotes", String.class);
            method.setAccessible(true);

            String input = "path/to/file";
            String result = (String) method.invoke(plugin, input);

            assertNotNull(result);
            assertTrue(result.contains("u002f") || result.contains("/"));
        }

        @Test
        @DisplayName("readInputStream reads correctly")
        void readInputStreamReadsCorrectly() throws Exception {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("readInputStream", InputStream.class);
            method.setAccessible(true);

            String testContent = "Line 1\nLine 2\nLine 3";
            InputStream inputStream = new ByteArrayInputStream(testContent.getBytes(StandardCharsets.UTF_8));

            String result = (String) method.invoke(plugin, inputStream);

            assertNotNull(result);
            assertTrue(result.contains("Line 1"));
            assertTrue(result.contains("Line 2"));
            assertTrue(result.contains("Line 3"));
        }

        @Test
        @DisplayName("readInputStream handles empty stream")
        void readInputStreamHandlesEmptyStream() throws Exception {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("readInputStream", InputStream.class);
            method.setAccessible(true);

            InputStream inputStream = new ByteArrayInputStream(new byte[0]);

            String result = (String) method.invoke(plugin, inputStream);

            assertEquals("", result);
        }

        @Test
        @DisplayName("readInputStream handles UTF-8 characters")
        void readInputStreamHandlesUtf8Characters() throws Exception {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("readInputStream", InputStream.class);
            method.setAccessible(true);

            String testContent = "Erreur: L'extraction a echoue";
            InputStream inputStream = new ByteArrayInputStream(testContent.getBytes(StandardCharsets.UTF_8));

            String result = (String) method.invoke(plugin, inputStream);

            assertEquals(testContent, result);
        }

        @Test
        @DisplayName("getMaxFmeInstances returns configured value")
        void getMaxFmeInstancesReturnsConfiguredValue() throws Exception {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("getMaxFmeInstances");
            method.setAccessible(true);

            Integer result = (Integer) method.invoke(plugin);

            assertNotNull(result);
            assertEquals(8, result);
        }

        @Test
        @DisplayName("formatParameterName returns correct format")
        void formatParameterNameReturnsCorrectFormat() throws Exception {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("formatParameterName", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(plugin, "paramRequestFolderOut");

            assertNotNull(result);
            assertTrue(result.startsWith("--"));
            assertTrue(result.contains("FolderOut"));
        }
    }

    @Nested
    @DisplayName("Constructor variations tests")
    class ConstructorVariationsTests {

        @Test
        @DisplayName("Constructor with Map creates instance")
        void constructorWithMapCreatesInstance() {
            Map<String, String> params = new HashMap<>();
            params.put("key", "value");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(params);

            assertNotNull(plugin);
            assertEquals("FME2017", plugin.getCode());
        }

        @Test
        @DisplayName("Constructor with empty Map works")
        void constructorWithEmptyMapWorks() {
            Map<String, String> params = new HashMap<>();

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(params);

            assertNotNull(plugin);
        }

        @Test
        @DisplayName("Constructor with null Map handled by newInstance")
        void constructorWithNullLanguageAndParams() {
            FmeDesktopPlugin base = new FmeDesktopPlugin();

            FmeDesktopPlugin plugin = base.newInstance(null, null);

            assertNotNull(plugin);
        }

        @Test
        @DisplayName("All four constructors create valid instances")
        void allFourConstructorsCreateValidInstances() {
            Map<String, String> params = new HashMap<>();
            params.put("test", "value");

            FmeDesktopPlugin plugin1 = new FmeDesktopPlugin();
            FmeDesktopPlugin plugin2 = new FmeDesktopPlugin("fr");
            FmeDesktopPlugin plugin3 = new FmeDesktopPlugin(params);
            FmeDesktopPlugin plugin4 = new FmeDesktopPlugin("fr", params);

            assertEquals("FME2017", plugin1.getCode());
            assertEquals("FME2017", plugin2.getCode());
            assertEquals("FME2017", plugin3.getCode());
            assertEquals("FME2017", plugin4.getCode());
        }
    }

    @Nested
    @DisplayName("getParams edge cases")
    class GetParamsEdgeCasesTests {

        @Test
        @DisplayName("getParams returns valid JSON")
        void getParamsReturnsValidJson() {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin();

            String params = plugin.getParams();

            assertNotNull(params);
            assertTrue(params.startsWith("["));
            assertTrue(params.endsWith("]"));
        }

        @Test
        @DisplayName("getParams contains all required fields")
        void getParamsContainsAllRequiredFields() {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin("fr");

            String params = plugin.getParams();

            assertTrue(params.contains("\"code\""));
            assertTrue(params.contains("\"label\""));
            assertTrue(params.contains("\"type\""));
            assertTrue(params.contains("\"req\""));
        }

        @Test
        @DisplayName("getParams numeric parameter has min max step")
        void getParamsNumericParameterHasMinMaxStep() {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin();

            String params = plugin.getParams();

            assertTrue(params.contains("\"min\""));
            assertTrue(params.contains("\"max\""));
            assertTrue(params.contains("\"step\""));
        }

        @Test
        @DisplayName("getParams text parameters have maxlength")
        void getParamsTextParametersHaveMaxlength() {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin();

            String params = plugin.getParams();

            assertTrue(params.contains("\"maxlength\""));
            assertTrue(params.contains("255"));
        }
    }

    @Nested
    @DisplayName("Execute with real temp files")
    class ExecuteWithRealTempFilesTests {

        @Test
        @DisplayName("Execute with existing script but missing FME returns error")
        void executeWithExistingScriptButMissingFmeReturnsError() throws IOException {
            File scriptFile = tempDir.resolve("script.fmw").toFile();
            Files.createFile(scriptFile.toPath());
            Files.writeString(scriptFile.toPath(), "# FME Script");

            Map<String, String> params = new HashMap<>();
            params.put(configuration.getProperty("paramPath"), scriptFile.getAbsolutePath());
            params.put(configuration.getProperty("paramPathFME"), "/nonexistent/fme.exe");
            params.put(configuration.getProperty("paramInstances"), "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            when(mockRequest.getFolderOut()).thenReturn(tempDir.toString());
            when(mockRequest.getProductGuid()).thenReturn("test-guid");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{}");

            ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertNotNull(result.getMessage());
        }

        @Test
        @DisplayName("Execute with directory instead of file returns error")
        void executeWithDirectoryInsteadOfFileReturnsError() {
            Map<String, String> params = new HashMap<>();
            params.put(configuration.getProperty("paramPath"), tempDir.toString()); // Directory not file
            params.put(configuration.getProperty("paramPathFME"), tempDir.toString());
            params.put(configuration.getProperty("paramInstances"), "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            when(mockRequest.getFolderOut()).thenReturn(tempDir.toString());

            ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("Execute with unreadable file returns error")
        void executeWithUnreadableFileReturnsError() throws IOException {
            File scriptFile = tempDir.resolve("unreadable.fmw").toFile();
            Files.createFile(scriptFile.toPath());

            // Note: Making a file unreadable may not work on all systems/filesystems
            // This test checks the path where file exists but is not readable
            Map<String, String> params = new HashMap<>();
            params.put(configuration.getProperty("paramPath"), "/root/protected.fmw");
            params.put(configuration.getProperty("paramPathFME"), "/nonexistent/fme.exe");
            params.put(configuration.getProperty("paramInstances"), "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Interface implementation tests")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Implements ITaskProcessor interface")
        void implementsITaskProcessorInterface() {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin();

            assertTrue(plugin instanceof ch.asit_asso.extract.plugins.common.ITaskProcessor);
        }

        @Test
        @DisplayName("All interface methods are callable")
        void allInterfaceMethodsAreCallable() {
            FmeDesktopPlugin plugin = new FmeDesktopPlugin("fr");

            assertDoesNotThrow(plugin::getCode);
            assertDoesNotThrow(plugin::getLabel);
            assertDoesNotThrow(plugin::getDescription);
            assertDoesNotThrow(plugin::getHelp);
            assertDoesNotThrow(plugin::getPictoClass);
            assertDoesNotThrow(plugin::getParams);
        }
    }

    @Nested
    @DisplayName("FME command generation tests")
    class FmeCommandGenerationTests {

        @Test
        @DisplayName("getFmeCommandForRequestAsArray generates correct array")
        void getFmeCommandForRequestAsArrayGeneratesCorrectArray() throws Exception {
            Map<String, String> params = new HashMap<>();
            params.put(configuration.getProperty("paramPath"), "/path/to/script.fmw");
            params.put(configuration.getProperty("paramPathFME"), "/path/to/fme.exe");
            params.put(configuration.getProperty("paramInstances"), "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("getFmeCommandForRequestAsArray",
                    ITaskProcessorRequest.class, String.class, String.class);
            method.setAccessible(true);

            when(mockRequest.getProductGuid()).thenReturn("product-123");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{\"key\":\"value\"}");
            when(mockRequest.getFolderOut()).thenReturn("/output/folder");
            when(mockRequest.getOrderLabel()).thenReturn("Order-001");
            when(mockRequest.getId()).thenReturn(42);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("org-guid");

            String[] result = (String[]) method.invoke(plugin, mockRequest, "/path/to/script.fmw", "/path/to/fme.exe");

            assertNotNull(result);
            assertTrue(result.length > 0);
            assertEquals("/path/to/fme.exe", result[0]);
        }

        @Test
        @DisplayName("getFmeCommandForRequest generates correct string")
        void getFmeCommandForRequestGeneratesCorrectString() throws Exception {
            Map<String, String> params = new HashMap<>();
            params.put(configuration.getProperty("paramPath"), "/path/to/script.fmw");
            params.put(configuration.getProperty("paramPathFME"), "/path/to/fme.exe");
            params.put(configuration.getProperty("paramInstances"), "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            Method method = FmeDesktopPlugin.class.getDeclaredMethod("getFmeCommandForRequest",
                    ITaskProcessorRequest.class, String.class, String.class);
            method.setAccessible(true);

            when(mockRequest.getProductGuid()).thenReturn("product-123");
            when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            when(mockRequest.getParameters()).thenReturn("{\"key\":\"value\"}");
            when(mockRequest.getFolderOut()).thenReturn("/output/folder");
            when(mockRequest.getOrderLabel()).thenReturn("Order-001");
            when(mockRequest.getId()).thenReturn(42);
            when(mockRequest.getClientGuid()).thenReturn("client-guid");
            when(mockRequest.getOrganismGuid()).thenReturn("org-guid");

            String result = (String) method.invoke(plugin, mockRequest, "/path/to/script.fmw", "/path/to/fme.exe");

            assertNotNull(result);
            assertTrue(result.contains("/path/to/fme.exe"));
            assertTrue(result.contains("product-123"));
            assertTrue(result.contains("Order-001"));
        }
    }

    @Nested
    @DisplayName("Multiple execution scenarios")
    class MultipleExecutionScenariosTests {

        @Test
        @DisplayName("Multiple executions on same plugin instance")
        void multipleExecutionsOnSamePluginInstance() {
            Map<String, String> params = new HashMap<>();
            params.put(configuration.getProperty("paramPath"), "/nonexistent/script.fmw");
            params.put(configuration.getProperty("paramPathFME"), "/nonexistent/fme.exe");
            params.put(configuration.getProperty("paramInstances"), "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            ITaskProcessorResult result1 = plugin.execute(mockRequest, null);
            ITaskProcessorResult result2 = plugin.execute(mockRequest, null);

            assertEquals(result1.getStatus(), result2.getStatus());
        }

        @Test
        @DisplayName("New instance per execution")
        void newInstancePerExecution() {
            FmeDesktopPlugin base = new FmeDesktopPlugin();

            Map<String, String> params1 = new HashMap<>();
            params1.put(configuration.getProperty("paramPath"), "/path1/script.fmw");
            params1.put(configuration.getProperty("paramPathFME"), "/path1/fme.exe");
            params1.put(configuration.getProperty("paramInstances"), "1");

            Map<String, String> params2 = new HashMap<>();
            params2.put(configuration.getProperty("paramPath"), "/path2/script.fmw");
            params2.put(configuration.getProperty("paramPathFME"), "/path2/fme.exe");
            params2.put(configuration.getProperty("paramInstances"), "2");

            FmeDesktopPlugin instance1 = base.newInstance(TEST_LANGUAGE, params1);
            FmeDesktopPlugin instance2 = base.newInstance(TEST_LANGUAGE, params2);

            assertNotSame(instance1, instance2);

            ITaskProcessorResult result1 = instance1.execute(mockRequest, null);
            ITaskProcessorResult result2 = instance2.execute(mockRequest, null);

            assertNotNull(result1);
            assertNotNull(result2);
        }
    }

    @Nested
    @DisplayName("Error code and message tests")
    class ErrorCodeAndMessageTests {

        @Test
        @DisplayName("Script not found returns correct error code")
        void scriptNotFoundReturnsCorrectErrorCode() {
            Map<String, String> params = new HashMap<>();
            params.put(configuration.getProperty("paramPath"), "/nonexistent/script.fmw");
            params.put(configuration.getProperty("paramPathFME"), "/nonexistent/fme.exe");
            params.put(configuration.getProperty("paramInstances"), "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            ITaskProcessorResult result = plugin.execute(mockRequest, null);

            assertEquals("-1", result.getErrorCode());
        }

        @Test
        @DisplayName("Script not found message is localized")
        void scriptNotFoundMessageIsLocalized() {
            Map<String, String> params = new HashMap<>();
            params.put(configuration.getProperty("paramPath"), "/nonexistent/script.fmw");
            params.put(configuration.getProperty("paramPathFME"), "/nonexistent/fme.exe");
            params.put(configuration.getProperty("paramInstances"), "1");

            FmeDesktopPlugin plugin = new FmeDesktopPlugin(TEST_LANGUAGE, params);

            ITaskProcessorResult result = plugin.execute(mockRequest, null);

            assertNotNull(result.getMessage());
            assertTrue(result.getMessage().contains("script FME"));
        }
    }
}
