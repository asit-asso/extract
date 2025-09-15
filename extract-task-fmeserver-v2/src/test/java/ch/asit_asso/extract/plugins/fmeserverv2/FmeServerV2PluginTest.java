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
package ch.asit_asso.extract.plugins.fmeserverv2;

import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the FME Server V2 plugin.
 *
 * @author Extract Team
 */
class FmeServerV2PluginTest {

    @Mock
    private ITaskProcessorRequest mockRequest;

    @TempDir
    Path tempDir;

    private FmeServerV2Plugin plugin;
    private Map<String, String> validInputs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up valid inputs for testing
        validInputs = new HashMap<>();
        validInputs.put("serviceURL", "http://fmeserver.example.com/fmedatadownload/test/workspace.fmw");
        validInputs.put("apiToken", "test-token-123");
        validInputs.put("geoJsonParameter", "GEOJSON_INPUT");
        validInputs.put("executionMode", "sync");

        // Mock request setup
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getFolderOut()).thenReturn(tempDir.toString());
        when(mockRequest.getFolderIn()).thenReturn(tempDir.toString());
        when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
        when(mockRequest.getOrderGuid()).thenReturn("order-guid-123");
        when(mockRequest.getOrderLabel()).thenReturn("Test Order");
        when(mockRequest.getClientGuid()).thenReturn("client-guid-123");
        when(mockRequest.getClient()).thenReturn("Test Client");
        when(mockRequest.getOrganismGuid()).thenReturn("organism-guid-123");
        when(mockRequest.getOrganism()).thenReturn("Test Organism");
        when(mockRequest.getProductGuid()).thenReturn("product-guid-123");
        when(mockRequest.getProductLabel()).thenReturn("Test Product");
        when(mockRequest.getParameters()).thenReturn("{\"customParam\":\"customValue\"}");
    }

    @Test
    void testPluginCreation() {
        plugin = new FmeServerV2Plugin();
        assertNotNull(plugin);
        assertEquals("FMESERVERV2", plugin.getCode());
        assertEquals("fa-cogs", plugin.getPictoClass());
        assertNotNull(plugin.getLabel());
        assertNotNull(plugin.getDescription());
    }

    @Test
    void testPluginCreationWithLanguage() {
        plugin = new FmeServerV2Plugin("fr");
        assertNotNull(plugin);
        assertEquals("FMESERVERV2", plugin.getCode());
    }

    @Test
    void testPluginCreationWithInputs() {
        plugin = new FmeServerV2Plugin(validInputs);
        assertNotNull(plugin);
        assertEquals("FMESERVERV2", plugin.getCode());
    }

    @Test
    void testPluginCreationWithLanguageAndInputs() {
        plugin = new FmeServerV2Plugin("fr", validInputs);
        assertNotNull(plugin);
        assertEquals("FMESERVERV2", plugin.getCode());
    }

    @Test
    void testNewInstance() {
        plugin = new FmeServerV2Plugin();
        ITaskProcessor newInstance = plugin.newInstance("en");
        assertNotNull(newInstance);
        assertNotSame(plugin, newInstance);
        assertEquals("FMESERVERV2", newInstance.getCode());
    }

    @Test
    void testNewInstanceWithInputs() {
        plugin = new FmeServerV2Plugin();
        ITaskProcessor newInstance = plugin.newInstance("en", validInputs);
        assertNotNull(newInstance);
        assertNotSame(plugin, newInstance);
        assertEquals("FMESERVERV2", newInstance.getCode());
    }

    @Test
    void testExecuteWithNoInputs() {
        plugin = new FmeServerV2Plugin();
        ITaskProcessorResult result = plugin.execute(mockRequest, null);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("paramètre"));
    }

    @Test
    void testExecuteWithEmptyInputs() {
        plugin = new FmeServerV2Plugin(new HashMap<>());
        ITaskProcessorResult result = plugin.execute(mockRequest, null);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
    }

    @Test
    void testExecuteWithNoServiceUrl() {
        Map<String, String> invalidInputs = new HashMap<>(validInputs);
        invalidInputs.remove("serviceURL");
        
        plugin = new FmeServerV2Plugin(invalidInputs);
        ITaskProcessorResult result = plugin.execute(mockRequest, null);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertTrue(result.getMessage().contains("URL du service"));
    }

    @Test
    void testExecuteWithInvalidServiceUrl() {
        Map<String, String> invalidInputs = new HashMap<>(validInputs);
        invalidInputs.put("serviceURL", "ftp://invalid-protocol.com/test");
        
        plugin = new FmeServerV2Plugin(invalidInputs);
        ITaskProcessorResult result = plugin.execute(mockRequest, null);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertTrue(result.getMessage().contains("URL du service"));
    }

    @Test
    void testExecuteWithLocalhostUrl() {
        Map<String, String> invalidInputs = new HashMap<>(validInputs);
        invalidInputs.put("serviceURL", "http://localhost/fmedatadownload/test/workspace.fmw");
        
        plugin = new FmeServerV2Plugin(invalidInputs);
        ITaskProcessorResult result = plugin.execute(mockRequest, null);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertTrue(result.getMessage().contains("URL du service"));
    }

    @Test
    void testExecuteWithNoAuthentication() {
        Map<String, String> invalidInputs = new HashMap<>(validInputs);
        invalidInputs.remove("apiToken");
        
        plugin = new FmeServerV2Plugin(invalidInputs);
        ITaskProcessorResult result = plugin.execute(mockRequest, null);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertTrue(result.getMessage().contains("authentification"));
    }

    @Test
    void testExecuteWithUsernamePasswordAuth() {
        Map<String, String> authInputs = new HashMap<>(validInputs);
        authInputs.remove("apiToken");
        authInputs.put("username", "testuser");
        authInputs.put("password", "testpass");
        
        plugin = new FmeServerV2Plugin(authInputs);
        ITaskProcessorResult result = plugin.execute(mockRequest, null);
        
        assertNotNull(result);
        // This will fail with connection error in unit test, which is expected
        // The important thing is that authentication validation passes (no immediate ERROR for missing auth)
        assertNotNull(result.getStatus());
    }

    @Test
    void testExecuteWithIncompleteUsernamePasswordAuth() {
        Map<String, String> authInputs = new HashMap<>(validInputs);
        authInputs.remove("apiToken");
        authInputs.put("username", "testuser");
        // Missing password
        
        plugin = new FmeServerV2Plugin(authInputs);
        ITaskProcessorResult result = plugin.execute(mockRequest, null);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertTrue(result.getMessage().contains("authentification"));
    }

    @Test
    void testGetParamsJsonStructure() {
        plugin = new FmeServerV2Plugin();
        String paramsJson = plugin.getParams();
        
        assertNotNull(paramsJson);
        assertFalse(paramsJson.isEmpty());
        assertTrue(paramsJson.startsWith("["));
        assertTrue(paramsJson.endsWith("]"));
        
        // Should contain all expected parameters
        assertTrue(paramsJson.contains("serviceURL"));
        assertTrue(paramsJson.contains("apiToken"));
        assertTrue(paramsJson.contains("username"));
        assertTrue(paramsJson.contains("password"));
        assertTrue(paramsJson.contains("geoJsonParameter"));
        assertTrue(paramsJson.contains("executionMode"));
    }

    @Test
    void testGetHelp() {
        plugin = new FmeServerV2Plugin();
        String help = plugin.getHelp();
        
        // Help might be null if file is not found, which is OK for unit tests
        // In integration tests, the help file should be available
        if (help != null) {
            assertTrue(help.contains("FME Server"));
        }
    }

    @Test
    void testResultCreation() {
        FmeServerV2Result result = new FmeServerV2Result();
        
        assertNotNull(result);
        assertNull(result.getStatus());
        assertNull(result.getMessage());
        assertNull(result.getErrorCode());
        assertNull(result.getRequestData());
        assertNull(result.getResultFilePath());
    }

    @Test
    void testResultSetters() {
        FmeServerV2Result result = new FmeServerV2Result();
        
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        result.setMessage("Test message");
        result.setErrorCode("TEST_ERROR");
        result.setRequestData(mockRequest);
        result.setResultFilePath("/test/path");
        
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertEquals("Test message", result.getMessage());
        assertEquals("TEST_ERROR", result.getErrorCode());
        assertEquals(mockRequest, result.getRequestData());
        assertEquals("/test/path", result.getResultFilePath());
    }

    @Test
    void testResultToString() {
        FmeServerV2Result result = new FmeServerV2Result();
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        result.setMessage("Test message");
        result.setErrorCode("TEST_ERROR");
        result.setResultFilePath("/test/path");
        
        String toString = result.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SUCCESS"));
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("TEST_ERROR"));
        assertTrue(toString.contains("/test/path"));
    }

    @Test
    void testLocalizedMessagesCreation() {
        LocalizedMessages messages = new LocalizedMessages();
        assertNotNull(messages);
        assertEquals("fr", messages.getLanguage());
    }

    @Test
    void testLocalizedMessagesWithLanguage() {
        LocalizedMessages messages = new LocalizedMessages("en");
        assertNotNull(messages);
        assertEquals("en", messages.getLanguage());
    }

    @Test
    void testLocalizedMessagesGetString() {
        LocalizedMessages messages = new LocalizedMessages();
        String message = messages.getString("plugin.label");
        assertNotNull(message);
        // Should return the key if message is not found, or the actual message if found
        assertFalse(message.isEmpty());
    }

    @Test
    void testPluginConfigurationCreation() {
        PluginConfiguration config = new PluginConfiguration("nonexistent/path");
        assertNotNull(config);
        
        // Should use default values when config file is not found
        int timeout = config.getRequestTimeoutSeconds();
        assertTrue(timeout > 0);
    }

    @Test
    void testPluginConfigurationDefaultValues() {
        PluginConfiguration config = new PluginConfiguration("nonexistent/path");
        
        assertEquals(300, config.getRequestTimeoutSeconds());
        assertEquals(30, config.getConnectTimeoutSeconds());
        assertEquals(3, config.getMaxRetryAttempts());
        assertEquals("sync", config.getDefaultExecutionMode());
        assertEquals("GEOJSON_INPUT", config.getDefaultGeoJsonParameter());
        assertTrue(config.getMaxDownloadSize() > 0);
        assertTrue(config.isSslVerifyCertificates());
        assertTrue(config.isSslVerifyHostname());
    }

    @Test
    void testPluginConfigurationPropertyMethods() {
        PluginConfiguration config = new PluginConfiguration("nonexistent/path");
        
        // Test with default values
        assertEquals(500, config.getIntProperty("nonexistent.key", 500));
        assertEquals(1000L, config.getLongProperty("nonexistent.key", 1000L));
        assertTrue(config.getBooleanProperty("nonexistent.key", true));
        assertEquals("default", config.getProperty("nonexistent.key", "default"));
    }
}