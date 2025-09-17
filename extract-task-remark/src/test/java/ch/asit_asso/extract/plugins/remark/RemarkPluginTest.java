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
package ch.asit_asso.extract.plugins.remark;

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
 * Unit tests for RemarkPlugin
 * 
 * @author Extract Team
 */
public class RemarkPluginTest {
    
    private static final String EXPECTED_PLUGIN_CODE = "REMARK";
    private static final String EXPECTED_ICON_CLASS = "fa-comment-o";
    private static final String TEST_INSTANCE_LANGUAGE = "fr";
    private static final String LABEL_STRING_IDENTIFIER = "plugin.label";
    private static final String DESCRIPTION_STRING_IDENTIFIER = "plugin.description";
    private static final String HELP_FILE_NAME = "remarkHelp.html";
    private static final int PARAMETERS_NUMBER = 2;
    private static final String[] VALID_PARAMETER_TYPES = new String[] {"email", "pass", "multitext", "text", "numeric", "boolean"};
    
    private final Logger logger = LoggerFactory.getLogger(RemarkPluginTest.class);
    
    @Mock
    private ITaskProcessorRequest mockRequest;
    
    @Mock
    private IEmailSettings mockEmailSettings;
    
    @TempDir
    Path tempDir;
    
    private LocalizedMessages messages;
    private ObjectMapper parameterMapper;
    private Map<String, String> testParameters;
    private RemarkPlugin plugin;
    private PluginConfiguration config;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.messages = new LocalizedMessages(TEST_INSTANCE_LANGUAGE);
        this.parameterMapper = new ObjectMapper();
        this.config = new PluginConfiguration("plugins/remark/properties/configRemark.properties");
        
        this.testParameters = new HashMap<>();
        this.plugin = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
    }
    
    @Test
    @DisplayName("Create a new instance without parameter values")
    public void testNewInstanceWithoutParameters() {
        RemarkPlugin instance = new RemarkPlugin();
        RemarkPlugin result = instance.newInstance(TEST_INSTANCE_LANGUAGE);
        
        assertNotSame(instance, result);
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Create a new instance with parameter values")
    public void testNewInstanceWithParameters() {
        RemarkPlugin instance = new RemarkPlugin();
        RemarkPlugin result = instance.newInstance(TEST_INSTANCE_LANGUAGE, testParameters);
        
        assertNotSame(instance, result);
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Create instance with default constructor")
    public void testDefaultConstructor() {
        RemarkPlugin instance = new RemarkPlugin();
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Create instance with language parameter")
    public void testLanguageConstructor() {
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE);
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Create instance with task settings only")
    public void testTaskSettingsConstructor() {
        Map<String, String> taskSettings = new HashMap<>();
        taskSettings.put("remark", "Automated remark message");
        taskSettings.put("overwrite", "false");
        
        RemarkPlugin instance = new RemarkPlugin(taskSettings);
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Create instance with language and task settings")
    public void testFullConstructor() {
        Map<String, String> taskSettings = new HashMap<>();
        taskSettings.put("remark", "Automated remark message");
        taskSettings.put("overwrite", "true");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, taskSettings);
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Check the plugin label")
    public void testGetLabel() {
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE);
        String expectedLabel = messages.getString(LABEL_STRING_IDENTIFIER);
        
        String result = instance.getLabel();
        
        assertEquals(expectedLabel, result);
    }
    
    @Test
    @DisplayName("Check the plugin identifier")
    public void testGetCode() {
        RemarkPlugin instance = new RemarkPlugin();
        
        String result = instance.getCode();
        
        assertEquals(EXPECTED_PLUGIN_CODE, result);
    }
    
    @Test
    @DisplayName("Check the plugin description")
    public void testGetDescription() {
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE);
        String expectedDescription = messages.getString(DESCRIPTION_STRING_IDENTIFIER);
        
        String result = instance.getDescription();
        
        assertEquals(expectedDescription, result);
    }
    
    @Test
    @DisplayName("Check the help content")
    public void testGetHelp() {
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE);
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
        RemarkPlugin instance = new RemarkPlugin();
        
        String result = instance.getPictoClass();
        
        assertEquals(EXPECTED_ICON_CLASS, result);
    }
    
    @Test
    @DisplayName("Check the plugin parameters structure")
    public void testGetParams() throws IOException {
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE);
        ArrayNode parametersArray = null;
        
        String paramsJson = instance.getParams();
        assertNotNull(paramsJson);
        
        parametersArray = parameterMapper.readValue(paramsJson, ArrayNode.class);
        
        assertNotNull(parametersArray);
        assertEquals(PARAMETERS_NUMBER, parametersArray.size());
        
        Set<String> expectedCodes = new HashSet<>(Arrays.asList(
            config.getProperty("paramRemark"), 
            config.getProperty("paramOverwrite")
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
            
            if (config.getProperty("paramRemark").equals(code)) {
                assertEquals("multitext", type);
                assertTrue(param.hasNonNull("req"));
                assertTrue(param.get("req").booleanValue());
                assertTrue(param.hasNonNull("maxlength"));
                assertEquals(5000, param.get("maxlength").intValue());
            } else if (config.getProperty("paramOverwrite").equals(code)) {
                assertEquals("boolean", type);
                assertTrue(param.hasNonNull("req"));
                assertFalse(param.get("req").booleanValue());
            }
        }
        
        assertEquals(expectedCodes, foundCodes);
    }
    
    @Test
    @DisplayName("Execute with new remark on empty request should succeed")
    public void testExecuteWithNewRemarkOnEmptyRequest() {
        String newRemark = "This is an automated remark";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), newRemark);
        params.put(config.getProperty("paramOverwrite"), "false");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn("");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getMessage());
        assertEquals("", result.getErrorCode());
        
        RemarkRequest updatedRequest = (RemarkRequest) result.getRequestData();
        assertNotNull(updatedRequest);
        assertEquals(newRemark, updatedRequest.getRemark());
    }
    
    @Test
    @DisplayName("Execute with new remark on null request should succeed")
    public void testExecuteWithNewRemarkOnNullRequest() {
        String newRemark = "This is an automated remark";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), newRemark);
        params.put(config.getProperty("paramOverwrite"), "false");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn(null);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RemarkRequest updatedRequest = (RemarkRequest) result.getRequestData();
        assertEquals(newRemark, updatedRequest.getRemark());
    }
    
    @Test
    @DisplayName("Execute with append to existing remark should succeed")
    public void testExecuteWithAppendToExistingRemark() {
        String existingRemark = "Original remark";
        String newRemark = "Additional automated remark";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), newRemark);
        params.put(config.getProperty("paramOverwrite"), "false");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn(existingRemark);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RemarkRequest updatedRequest = (RemarkRequest) result.getRequestData();
        String expectedRemark = String.format("%s\r\n%s", existingRemark, newRemark);
        assertEquals(expectedRemark, updatedRequest.getRemark());
    }
    
    @Test
    @DisplayName("Execute with overwrite existing remark should succeed")
    public void testExecuteWithOverwriteExistingRemark() {
        String existingRemark = "Original remark to be overwritten";
        String newRemark = "Replacement automated remark";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), newRemark);
        params.put(config.getProperty("paramOverwrite"), "true");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn(existingRemark);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RemarkRequest updatedRequest = (RemarkRequest) result.getRequestData();
        assertEquals(newRemark, updatedRequest.getRemark());
    }
    
    @Test
    @DisplayName("Execute with overwrite on empty request should succeed")
    public void testExecuteWithOverwriteOnEmptyRequest() {
        String newRemark = "New remark with overwrite enabled";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), newRemark);
        params.put(config.getProperty("paramOverwrite"), "true");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn("");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RemarkRequest updatedRequest = (RemarkRequest) result.getRequestData();
        assertEquals(newRemark, updatedRequest.getRemark());
    }
    
    @Test
    @DisplayName("Execute without parameters should succeed with null handling")
    public void testExecuteWithoutParameters() {
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE);
        
        when(mockRequest.getRemark()).thenReturn("Existing remark");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        // The plugin should handle gracefully even without parameters
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
    }
    
    @Test
    @DisplayName("Execute with null overwrite parameter defaults to false")
    public void testExecuteWithNullOverwriteParameter() {
        String existingRemark = "Existing remark";
        String newRemark = "Additional remark";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), newRemark);
        params.put(config.getProperty("paramOverwrite"), null);
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn(existingRemark);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RemarkRequest updatedRequest = (RemarkRequest) result.getRequestData();
        // Should append since overwrite defaults to false when null
        String expectedRemark = String.format("%s\r\n%s", existingRemark, newRemark);
        assertEquals(expectedRemark, updatedRequest.getRemark());
    }
    
    @Test
    @DisplayName("Execute with empty overwrite parameter defaults to false")
    public void testExecuteWithEmptyOverwriteParameter() {
        String existingRemark = "Existing remark";
        String newRemark = "Additional remark";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), newRemark);
        params.put(config.getProperty("paramOverwrite"), "");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn(existingRemark);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RemarkRequest updatedRequest = (RemarkRequest) result.getRequestData();
        String expectedRemark = String.format("%s\r\n%s", existingRemark, newRemark);
        assertEquals(expectedRemark, updatedRequest.getRemark());
    }
    
    @Test
    @DisplayName("Execute with long remark should succeed")
    public void testExecuteWithLongRemark() {
        StringBuilder longRemark = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longRemark.append("This is a very long automated remark. ");
        }
        
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), longRemark.toString());
        params.put(config.getProperty("paramOverwrite"), "true");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn("Old remark");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RemarkRequest updatedRequest = (RemarkRequest) result.getRequestData();
        assertEquals(longRemark.toString(), updatedRequest.getRemark());
    }
    
    @Test
    @DisplayName("Execute with special characters in remark should succeed")
    public void testExecuteWithSpecialCharacters() {
        String remarkWithSpecialChars = "Automated remark: äöü éèà ñç 漢字 💬 <>&\"'";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), remarkWithSpecialChars);
        params.put(config.getProperty("paramOverwrite"), "true");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn("Old remark");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RemarkRequest updatedRequest = (RemarkRequest) result.getRequestData();
        assertEquals(remarkWithSpecialChars, updatedRequest.getRemark());
    }
    
    @Test
    @DisplayName("Execute preserves original request data")
    public void testExecutePreservesRequestData() {
        String newRemark = "Automated remark";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), newRemark);
        params.put(config.getProperty("paramOverwrite"), "false");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getOrderGuid()).thenReturn("order-guid-456");
        when(mockRequest.getClient()).thenReturn("Test Client");
        when(mockRequest.getRemark()).thenReturn("Old remark");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RemarkRequest updatedRequest = (RemarkRequest) result.getRequestData();
        assertNotNull(updatedRequest);
        
        // Verify that original request data is preserved
        verify(mockRequest, atLeastOnce()).getId();
        verify(mockRequest, atLeastOnce()).getOrderGuid();
        verify(mockRequest, atLeastOnce()).getClient();
        verify(mockRequest, atLeastOnce()).getRemark();
    }
    
    @Test
    @DisplayName("Result should be instance of RemarkResult")
    public void testResultType() {
        String newRemark = "Test remark";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), newRemark);
        params.put(config.getProperty("paramOverwrite"), "false");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn("");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertInstanceOf(RemarkResult.class, result);
    }
    
    @Test
    @DisplayName("Exception during execution should return error status")
    public void testExecuteWithException() {
        String newRemark = "Test remark";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), newRemark);
        params.put(config.getProperty("paramOverwrite"), "false");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        // Mock request to throw exception
        when(mockRequest.getRemark()).thenThrow(new RuntimeException("Test exception"));
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
        assertEquals("-1", result.getErrorCode());
    }
    
    @Test
    @DisplayName("Execute with multiline remarks should format correctly")
    public void testExecuteWithMultilineRemarks() {
        String existingRemark = "Line 1\nLine 2\nLine 3";
        String newRemark = "New line 1\nNew line 2";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramRemark"), newRemark);
        params.put(config.getProperty("paramOverwrite"), "false");
        
        RemarkPlugin instance = new RemarkPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn(existingRemark);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RemarkRequest updatedRequest = (RemarkRequest) result.getRequestData();
        String expectedRemark = String.format("%s\r\n%s", existingRemark, newRemark);
        assertEquals(expectedRemark, updatedRequest.getRemark());
    }
}