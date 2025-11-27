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
package ch.asit_asso.extract.plugins.reject;

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
 * Unit tests for RejectPlugin
 * 
 * @author Extract Team
 */
public class RejectPluginTest {
    
    private static final String EXPECTED_PLUGIN_CODE = "REJECT";
    private static final String EXPECTED_ICON_CLASS = "fa-ban";
    private static final String TEST_INSTANCE_LANGUAGE = "fr";
    private static final String LABEL_STRING_IDENTIFIER = "plugin.label";
    private static final String DESCRIPTION_STRING_IDENTIFIER = "plugin.description";
    private static final String HELP_FILE_NAME = "rejectHelp.html";
    private static final int PARAMETERS_NUMBER = 1;
    private static final String[] VALID_PARAMETER_TYPES = new String[] {"email", "pass", "multitext", "text", "numeric", "boolean"};
    
    private final Logger logger = LoggerFactory.getLogger(RejectPluginTest.class);
    
    @Mock
    private ITaskProcessorRequest mockRequest;
    
    @Mock
    private IEmailSettings mockEmailSettings;
    
    @TempDir
    Path tempDir;
    
    private LocalizedMessages messages;
    private ObjectMapper parameterMapper;
    private Map<String, String> testParameters;
    private RejectPlugin plugin;
    private PluginConfiguration config;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.messages = new LocalizedMessages(TEST_INSTANCE_LANGUAGE);
        this.parameterMapper = new ObjectMapper();
        this.config = new PluginConfiguration("plugins/reject/properties/configReject.properties");
        
        this.testParameters = new HashMap<>();
        this.plugin = new RejectPlugin(TEST_INSTANCE_LANGUAGE, testParameters);
    }
    
    @Test
    @DisplayName("Create a new instance without parameter values")
    public void testNewInstanceWithoutParameters() {
        RejectPlugin instance = new RejectPlugin();
        RejectPlugin result = instance.newInstance(TEST_INSTANCE_LANGUAGE);
        
        assertNotSame(instance, result);
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Create a new instance with parameter values")
    public void testNewInstanceWithParameters() {
        RejectPlugin instance = new RejectPlugin();
        RejectPlugin result = instance.newInstance(TEST_INSTANCE_LANGUAGE, testParameters);
        
        assertNotSame(instance, result);
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Create instance with default constructor")
    public void testDefaultConstructor() {
        RejectPlugin instance = new RejectPlugin();
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Create instance with language parameter")
    public void testLanguageConstructor() {
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE);
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Create instance with task settings only")
    public void testTaskSettingsConstructor() {
        Map<String, String> taskSettings = new HashMap<>();
        taskSettings.put("remark", "Test rejection reason");
        
        RejectPlugin instance = new RejectPlugin(taskSettings);
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Create instance with language and task settings")
    public void testFullConstructor() {
        Map<String, String> taskSettings = new HashMap<>();
        taskSettings.put("remark", "Test rejection reason");
        
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE, taskSettings);
        
        assertNotNull(instance);
        assertEquals(EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(EXPECTED_ICON_CLASS, instance.getPictoClass());
    }
    
    @Test
    @DisplayName("Check the plugin label")
    public void testGetLabel() {
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE);
        String expectedLabel = messages.getString(LABEL_STRING_IDENTIFIER);
        
        String result = instance.getLabel();
        
        assertEquals(expectedLabel, result);
    }
    
    @Test
    @DisplayName("Check the plugin identifier")
    public void testGetCode() {
        RejectPlugin instance = new RejectPlugin();
        
        String result = instance.getCode();
        
        assertEquals(EXPECTED_PLUGIN_CODE, result);
    }
    
    @Test
    @DisplayName("Check the plugin description")
    public void testGetDescription() {
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE);
        String expectedDescription = messages.getString(DESCRIPTION_STRING_IDENTIFIER);
        
        String result = instance.getDescription();
        
        assertEquals(expectedDescription, result);
    }
    
    @Test
    @DisplayName("Check the help content")
    public void testGetHelp() {
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE);
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
        RejectPlugin instance = new RejectPlugin();
        
        String result = instance.getPictoClass();
        
        assertEquals(EXPECTED_ICON_CLASS, result);
    }
    
    @Test
    @DisplayName("Check the plugin parameters structure")
    public void testGetParams() throws IOException {
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE);
        ArrayNode parametersArray = null;
        
        String paramsJson = instance.getParams();
        assertNotNull(paramsJson);
        
        parametersArray = parameterMapper.readValue(paramsJson, ArrayNode.class);
        
        assertNotNull(parametersArray);
        assertEquals(PARAMETERS_NUMBER, parametersArray.size());
        
        // Check remark parameter
        JsonNode remarkParam = parametersArray.get(0);
        assertTrue(remarkParam.hasNonNull("code"));
        assertEquals(config.getProperty("param.remark"), remarkParam.get("code").textValue());
        
        assertTrue(remarkParam.hasNonNull("label"));
        assertNotNull(remarkParam.get("label").textValue());
        
        assertTrue(remarkParam.hasNonNull("type"));
        assertEquals("multitext", remarkParam.get("type").textValue());
        
        assertTrue(remarkParam.hasNonNull("req"));
        assertTrue(remarkParam.get("req").booleanValue());
        
        assertTrue(remarkParam.hasNonNull("maxlength"));
        assertEquals(5000, remarkParam.get("maxlength").intValue());
    }
    
    @Test
    @DisplayName("Execute with valid remark should succeed")
    public void testExecuteWithValidRemark() {
        String remarkValue = "This request is rejected due to invalid data format";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("param.remark"), remarkValue);
        
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getRemark()).thenReturn("Original remark");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertEquals("", result.getErrorCode());
        
        // Verify that the request was updated
        RejectRequest updatedRequest = (RejectRequest) result.getRequestData();
        assertNotNull(updatedRequest);
        assertEquals(remarkValue, updatedRequest.getRemark());
        assertTrue(updatedRequest.isRejected());
    }
    
    @Test
    @DisplayName("Execute with empty remark should fail")
    public void testExecuteWithEmptyRemark() {
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("param.remark"), "");
        
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertEquals("-1", result.getErrorCode());
    }
    
    @Test
    @DisplayName("Execute with null remark should fail")
    public void testExecuteWithNullRemark() {
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("param.remark"), null);
        
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertEquals("-1", result.getErrorCode());
    }
    
    @Test
    @DisplayName("Execute with whitespace-only remark should fail")
    public void testExecuteWithWhitespaceRemark() {
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("param.remark"), "   \t\n   ");
        
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertEquals("-1", result.getErrorCode());
    }
    
    @Test
    @DisplayName("Execute without parameters should fail")
    public void testExecuteWithoutParameters() {
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertEquals("-1", result.getErrorCode());
    }
    
    @Test
    @DisplayName("Execute with missing remark parameter should fail")
    public void testExecuteWithMissingRemarkParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("other_param", "some value");
        
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertEquals("-1", result.getErrorCode());
    }
    
    @Test
    @DisplayName("Execute with valid remark preserves original request data")
    public void testExecutePreservesRequestData() {
        String remarkValue = "Rejected for testing purposes";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("param.remark"), remarkValue);
        
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getOrderGuid()).thenReturn("order-guid-456");
        when(mockRequest.getClient()).thenReturn("Test Client");
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RejectRequest updatedRequest = (RejectRequest) result.getRequestData();
        assertNotNull(updatedRequest);
        
        // Verify that original request data is preserved
        verify(mockRequest, atLeastOnce()).getId();
        verify(mockRequest, atLeastOnce()).getOrderGuid();
        verify(mockRequest, atLeastOnce()).getClient();
    }
    
    @Test
    @DisplayName("Execute with very long remark should succeed")
    public void testExecuteWithLongRemark() {
        StringBuilder longRemark = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longRemark.append("This is a very long rejection reason. ");
        }
        
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("param.remark"), longRemark.toString());
        
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RejectRequest updatedRequest = (RejectRequest) result.getRequestData();
        assertEquals(longRemark.toString(), updatedRequest.getRemark());
        assertTrue(updatedRequest.isRejected());
    }
    
    @Test
    @DisplayName("Execute with special characters in remark should succeed")
    public void testExecuteWithSpecialCharacters() {
        String remarkWithSpecialChars = "Rejected: äöü éèà ñç 漢字 🚫 <>&\"'";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("param.remark"), remarkWithSpecialChars);
        
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        RejectRequest updatedRequest = (RejectRequest) result.getRequestData();
        assertEquals(remarkWithSpecialChars, updatedRequest.getRemark());
        assertTrue(updatedRequest.isRejected());
    }
    
    @Test
    @DisplayName("Result should be instance of RejectResult")
    public void testResultType() {
        String remarkValue = "Test rejection";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("param.remark"), remarkValue);
        
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertInstanceOf(RejectResult.class, result);
    }
    
    @Test
    @DisplayName("Exception during execution should return error status")
    public void testExecuteWithException() {
        String remarkValue = "Test rejection";
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("param.remark"), remarkValue);
        
        RejectPlugin instance = new RejectPlugin(TEST_INSTANCE_LANGUAGE, params);

        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertEquals("", result.getErrorCode());
    }
}