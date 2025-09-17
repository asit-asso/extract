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
package ch.asit_asso.extract.plugins.validation;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ValidationPlugin
 * 
 * @author Extract Team
 */
public class ValidationPluginTest {
    
    private static final String EXPECTED_PLUGIN_CODE = "VALIDATION";
    private static final String EXPECTED_ICON_CLASS = "fa-eye";
    private static final String TEST_INSTANCE_LANGUAGE = "fr";
    private static final String LABEL_STRING_IDENTIFIER = "plugin.label";
    private static final String DESCRIPTION_STRING_IDENTIFIER = "plugin.description";
    private static final String HELP_FILE_NAME = "validationHelp.html";
    private static final int PARAMETERS_NUMBER = 2;
    private static final String[] VALID_PARAMETER_TYPES = new String[] {"email", "pass", "multitext", "text", "numeric", "boolean", "list_msgs"};
    
    private final Logger logger = LoggerFactory.getLogger(ValidationPluginTest.class);
    
    @Mock
    private ITaskProcessorRequest mockRequest;
    
    @Mock
    private IEmailSettings mockEmailSettings;
    
    @TempDir
    Path tempDir;
    
    private LocalizedMessages messages;
    private ObjectMapper parameterMapper;
    private Map<String, String> testParameters;
    private ValidationPlugin plugin;
    private PluginConfiguration config;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.messages = new LocalizedMessages(TEST_INSTANCE_LANGUAGE);
        this.parameterMapper = new ObjectMapper();
        this.config = new PluginConfiguration("plugins/validation/properties/config.properties");
        
        this.testParameters = new HashMap<>();
        this.plugin = new ValidationPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
    }
    
    @Test
    @DisplayName("Create a new instance without parameter values")
    public void testNewInstanceWithoutParameters() {
        ValidationPlugin instance = new ValidationPlugin();
        ValidationPlugin result = instance.newInstance(TEST_INSTANCE_LANGUAGE);
        
        assertNotSame(instance, result);
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Create a new instance with parameter values")
    public void testNewInstanceWithParameters() {
        ValidationPlugin instance = new ValidationPlugin();
        ValidationPlugin result = instance.newInstance(TEST_INSTANCE_LANGUAGE, testParameters);
        
        assertNotSame(instance, result);
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Create instance with default constructor")
    public void testDefaultConstructor() {
        ValidationPlugin instance = new ValidationPlugin();
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Create instance with language parameter")
    public void testLanguageConstructor() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Create instance with task settings only")
    public void testTaskSettingsConstructor() {
        Map<String, String> taskSettings = new HashMap<>();
        taskSettings.put("validMessages", "Message 1|Message 2");
        taskSettings.put("rejectMessages", "Reject 1|Reject 2");
        
        ValidationPlugin instance = new ValidationPlugin(taskSettings);
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Create instance with language and task settings")
    public void testFullConstructor() {
        Map<String, String> taskSettings = new HashMap<>();
        taskSettings.put("validMessages", "Valid message");
        taskSettings.put("rejectMessages", "Reject message");
        
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE, taskSettings);
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Check the plugin label")
    public void testGetLabel() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        String expectedLabel = messages.getString(LABEL_STRING_IDENTIFIER);
        
        String result = instance.getLabel();
        
        assertEquals(expectedLabel, result);
    }
    
    @Test
    @DisplayName("Check the plugin identifier")
    public void testGetCode() {
        ValidationPlugin instance = new ValidationPlugin();
        
        String result = instance.getCode();
        
        assertEquals(EXPECTED_PLUGIN_CODE, result);
    }
    
    @Test
    @DisplayName("Check the plugin description")
    public void testGetDescription() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        String expectedDescription = messages.getString(DESCRIPTION_STRING_IDENTIFIER);
        
        String result = instance.getDescription();
        
        assertEquals(expectedDescription, result);
    }
    
    @Test
    @DisplayName("Check the help content")
    public void testGetHelp() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        String expectedHelp = messages.getFileContent(HELP_FILE_NAME);
        
        String result = instance.getHelp();
        
        assertEquals(expectedHelp, result);
        
        // Test that subsequent calls return cached help
        String secondResult = instance.getHelp();
        assertSame(result, secondResult);
    }
    
    @Test
    @DisplayName("Check the plugin pictogram")
    public void testGetPictoClass() {
        ValidationPlugin instance = new ValidationPlugin();
        
        String result = instance.getPictoClass();
        
        assertEquals(EXPECTED_ICON_CLASS, result);
    }
    
    @Test
    @DisplayName("Check the plugin parameters structure")
    public void testGetParams() throws IOException {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        ArrayNode parametersArray = null;
        
        String paramsJson = instance.getParams();
        assertNotNull(paramsJson);
        
        parametersArray = parameterMapper.readValue(paramsJson, ArrayNode.class);
        
        assertNotNull(parametersArray);
        assertEquals(PARAMETERS_NUMBER, parametersArray.size());
        
        Set<String> expectedCodes = new HashSet<>(Arrays.asList(
            config.getProperty("paramValidMessages"), 
            config.getProperty("paramRejectMessages")
        ));
        Set<String> foundCodes = new HashSet<>();
        
        for (int i = 0; i < parametersArray.size(); i++) {
            JsonNode param = parametersArray.get(i);
            
            assertTrue(param.hasNonNull("code"));
            String code = param.get("code").textValue();
            assertNotNull(code);
            foundCodes.add(code);
            
            assertTrue(param.hasNonNull("label"));
            assertNotNull(param.get("label").textValue());
            
            assertTrue(param.hasNonNull("type"));
            String type = param.get("type").textValue();
            assertTrue(ArrayUtils.contains(VALID_PARAMETER_TYPES, type));
            
            if (config.getProperty("paramValidMessages").equals(code)) {
                assertEquals("list_msgs", type);
                assertTrue(param.hasNonNull("req"));
                assertFalse(param.get("req").booleanValue());
            } else if (config.getProperty("paramRejectMessages").equals(code)) {
                assertEquals("list_msgs", type);
                assertTrue(param.hasNonNull("req"));
                assertFalse(param.get("req").booleanValue());
            }
        }
        
        assertEquals(expectedCodes, foundCodes);
    }
    
    @Test
    @DisplayName("Execute should always return STANDBY status")
    public void testExecuteReturnsStandbyStatus() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getOrderGuid()).thenReturn("order-guid-456");
        when(mockRequest.getClient()).thenReturn("Test Client");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    @DisplayName("Execute with valid parameters should return STANDBY status")
    public void testExecuteWithValidParameters() {
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramValidMessages"), "Validated successfully|Data approved");
        params.put(config.getProperty("paramRejectMessages"), "Data rejected|Invalid format");
        
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getOrderGuid()).thenReturn("order-guid-456");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    @DisplayName("Execute without parameters should return STANDBY status")
    public void testExecuteWithoutParameters() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    @DisplayName("Execute preserves original request data unchanged")
    public void testExecutePreservesRequestData() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getOrderGuid()).thenReturn("order-guid-456");
        when(mockRequest.getClient()).thenReturn("Test Client");
        when(mockRequest.getRemark()).thenReturn("Original remark");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        
        // Verify that the original request object is returned unchanged
        ITaskProcessorRequest returnedRequest = result.getRequestData();
        assertSame(mockRequest, returnedRequest);
    }
    
    @Test
    @DisplayName("Execute with null request should handle gracefully")
    public void testExecuteWithNullRequest() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        
        ITaskProcessorResult result = instance.execute(null, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        assertNotNull(result.getMessage());
        assertNull(result.getRequestData());
    }
    
    @Test
    @DisplayName("Execute with null email settings should handle gracefully")
    public void testExecuteWithNullEmailSettings() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        
        when(mockRequest.getId()).thenReturn(123);
        
        ITaskProcessorResult result = instance.execute(mockRequest, null);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        assertNotNull(result.getMessage());
        assertSame(mockRequest, result.getRequestData());
    }
    
    @Test
    @DisplayName("Execute with empty parameters should handle gracefully")
    public void testExecuteWithEmptyParameters() {
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramValidMessages"), "");
        params.put(config.getProperty("paramRejectMessages"), "");
        
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getId()).thenReturn(123);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    @DisplayName("Execute with null parameters should handle gracefully")
    public void testExecuteWithNullParameters() {
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramValidMessages"), null);
        params.put(config.getProperty("paramRejectMessages"), null);
        
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getId()).thenReturn(123);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    @DisplayName("Result should be instance of ValidationResult")
    public void testResultType() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        
        when(mockRequest.getId()).thenReturn(123);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertInstanceOf(ValidationResult.class, result);
    }
    
    @Test
    @DisplayName("Execute should set correct message from localized messages")
    public void testExecuteMessage() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        String expectedMessage = messages.getString("messageValidation");
        
        when(mockRequest.getId()).thenReturn(123);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(expectedMessage, result.getMessage());
    }
    
    @Test
    @DisplayName("Execute should not set error code for successful validation setup")
    public void testExecuteNoErrorCode() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        
        when(mockRequest.getId()).thenReturn(123);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertNull(result.getErrorCode());
    }
    
    @Test
    @DisplayName("Execute with complex parameters should succeed")
    public void testExecuteWithComplexParameters() {
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramValidMessages"), "Data validated successfully|Quality check passed|Format approved");
        params.put(config.getProperty("paramRejectMessages"), "Data format invalid|Missing required fields|Quality check failed|Coordinate system not supported");
        
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getId()).thenReturn(456);
        when(mockRequest.getOrderGuid()).thenReturn("complex-order-789");
        when(mockRequest.getClient()).thenReturn("Complex Test Client");
        when(mockRequest.getProductLabel()).thenReturn("Complex Product");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        assertNotNull(result.getMessage());
        assertSame(mockRequest, result.getRequestData());
    }
    
    @Test
    @DisplayName("Execute should be stateless and repeatable")
    public void testExecuteStatelessness() {
        ValidationPlugin instance = new ValidationPlugin(TEST_INSTANCE_LANGUAGE);
        
        when(mockRequest.getId()).thenReturn(789);
        
        // Execute multiple times
        ITaskProcessorResult result1 = instance.execute(mockRequest, mockEmailSettings);
        ITaskProcessorResult result2 = instance.execute(mockRequest, mockEmailSettings);
        ITaskProcessorResult result3 = instance.execute(mockRequest, mockEmailSettings);
        
        // All results should be identical
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        
        assertEquals(result1.getStatus(), result2.getStatus());
        assertEquals(result1.getStatus(), result3.getStatus());
        assertEquals(result1.getMessage(), result2.getMessage());
        assertEquals(result1.getMessage(), result3.getMessage());
        
        // All should reference the same request
        assertSame(result1.getRequestData(), result2.getRequestData());
        assertSame(result1.getRequestData(), result3.getRequestData());
    }
    
    @Test
    @DisplayName("Multiple instances should work independently")
    public void testMultipleInstancesIndependence() {
        Map<String, String> params1 = new HashMap<>();
        params1.put(config.getProperty("paramValidMessages"), "Instance 1 valid");
        
        Map<String, String> params2 = new HashMap<>();
        params2.put(config.getProperty("paramValidMessages"), "Instance 2 valid");
        
        ValidationPlugin instance1 = new ValidationPlugin(TEST_INSTANCE_LANGUAGE, params1);
        ValidationPlugin instance2 = new ValidationPlugin("en", params2);
        
        when(mockRequest.getId()).thenReturn(123);
        
        ITaskProcessorResult result1 = instance1.execute(mockRequest, mockEmailSettings);
        ITaskProcessorResult result2 = instance2.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result1.getStatus());
        assertEquals(ITaskProcessorResult.Status.STANDBY, result2.getStatus());
        
        // Results should be independent
        assertNotSame(result1, result2);
    }
}