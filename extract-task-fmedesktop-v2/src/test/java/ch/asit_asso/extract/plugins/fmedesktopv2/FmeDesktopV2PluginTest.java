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
package ch.asit_asso.extract.plugins.fmedesktopv2;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FmeDesktopV2Plugin
 * 
 * @author Extract Team
 */
public class FmeDesktopV2PluginTest {
    
    private static final String EXPECTED_PLUGIN_CODE = "FME2017V2";
    private static final String EXPECTED_ICON_CLASS = "fa-cogs";
    private static final String TEST_INSTANCE_LANGUAGE = "fr";
    private static final String LABEL_STRING_IDENTIFIER = "plugin.label";
    private static final String DESCRIPTION_STRING_IDENTIFIER = "plugin.description";
    private static final String HELP_FILE_NAME = "help.html";
    private static final int PARAMETERS_NUMBER = 3;
    private static final String[] VALID_PARAMETER_TYPES = new String[] {"email", "pass", "multitext", "text", "numeric"};
    
    private final Logger logger = LoggerFactory.getLogger(FmeDesktopV2PluginTest.class);
    
    @Mock
    private ITaskProcessorRequest mockRequest;
    
    @Mock
    private IEmailSettings mockEmailSettings;
    
    @TempDir
    Path tempDir;
    
    private LocalizedMessages messages;
    private ObjectMapper parameterMapper;
    private Map<String, String> testParameters;
    private FmeDesktopV2Plugin plugin;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.messages = new LocalizedMessages(TEST_INSTANCE_LANGUAGE);
        this.parameterMapper = new ObjectMapper();
        
        this.testParameters = new HashMap<>();
        this.plugin = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, testParameters);
    }
    
    @Test
    @DisplayName("Create a new instance without parameter values")
    public void testNewInstanceWithoutParameters() {
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin();
        FmeDesktopV2Plugin result = (FmeDesktopV2Plugin) instance.newInstance(TEST_INSTANCE_LANGUAGE);
        
        assertNotSame(instance, result);
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Create a new instance with parameter values")
    public void testNewInstanceWithParameters() {
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin();
        FmeDesktopV2Plugin result = (FmeDesktopV2Plugin) instance.newInstance(TEST_INSTANCE_LANGUAGE, testParameters);
        
        assertNotSame(instance, result);
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Check the plugin label")
    public void testGetLabel() {
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE);
        String expectedLabel = messages.getString(LABEL_STRING_IDENTIFIER);
        
        String result = instance.getLabel();
        
        assertEquals(expectedLabel, result);
    }
    
    @Test
    @DisplayName("Check the plugin identifier")
    public void testGetCode() {
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin();
        
        String result = instance.getCode();
        
        assertEquals(EXPECTED_PLUGIN_CODE, result);
    }
    
    @Test
    @DisplayName("Check the plugin description")
    public void testGetDescription() {
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE);
        String expectedDescription = messages.getString(DESCRIPTION_STRING_IDENTIFIER);
        
        String result = instance.getDescription();
        
        assertEquals(expectedDescription, result);
    }
    
    @Test
    @DisplayName("Check the help content")
    public void testGetHelp() {
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE);
        String expectedHelp = messages.getFileContent(HELP_FILE_NAME);
        
        String result = instance.getHelp();
        
        assertEquals(expectedHelp, result);
    }
    
    @Test
    @DisplayName("Check the plugin pictogram")
    public void testGetPictoClass() {
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin();
        
        String result = instance.getPictoClass();
        
        assertEquals(EXPECTED_ICON_CLASS, result);
    }
    
    @Test
    @DisplayName("Check the plugin parameters structure")
    public void testGetParams() throws IOException {
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin();
        ArrayNode parametersArray = null;
        
        String paramsJson = instance.getParams();
        assertNotNull(paramsJson);
        
        parametersArray = parameterMapper.readValue(paramsJson, ArrayNode.class);
        
        assertNotNull(parametersArray);
        assertEquals(PARAMETERS_NUMBER, parametersArray.size());
        
        Set<String> expectedCodes = new HashSet<>(Arrays.asList("workbench", "application", "nbInstances"));
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
            assertTrue(ArrayUtils.contains(VALID_PARAMETER_TYPES, type) || "numeric".equals(type));
            
            assertTrue(param.hasNonNull("req"));
            assertTrue(param.get("req").isBoolean());
            
            if ("nbInstances".equals(code)) {
                assertTrue(param.hasNonNull("min"));
                assertTrue(param.hasNonNull("max"));
                assertEquals(1, param.get("min").intValue());
                assertEquals(8, param.get("max").intValue());
            }
        }
        
        assertEquals(expectedCodes, foundCodes);
    }
    
    @Test
    @DisplayName("Execute with no parameters should return error")
    public void testExecuteNoParameters() {
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    @DisplayName("Execute with missing workspace parameter should return error")
    public void testExecuteMissingWorkspace() {
        Map<String, String> params = new HashMap<>();
        params.put("application", "/path/to/fme.exe");
        params.put("nbInstances", "2");
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertTrue(result.getMessage().contains("workspace") || result.getMessage().contains("workbench"));
    }
    
    @Test
    @DisplayName("Execute with missing application parameter should return error")
    public void testExecuteMissingApplication() {
        Map<String, String> params = new HashMap<>();
        params.put("workbench", "/path/to/workspace.fmw");
        params.put("nbInstances", "2");
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertTrue(result.getMessage().contains("application"));
    }
    
    @Test
    @DisplayName("Execute with non-existent workspace file should return error")
    public void testExecuteNonExistentWorkspace() {
        Map<String, String> params = new HashMap<>();
        params.put("workbench", "/nonexistent/workspace.fmw");
        params.put("application", tempDir.resolve("fme.exe").toString());
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
    }
    
    @Test
    @DisplayName("Execute with non-existent application file should return error")
    public void testExecuteNonExistentApplication() throws IOException {
        Path workspaceFile = tempDir.resolve("workspace.fmw");
        Files.createFile(workspaceFile);
        
        Map<String, String> params = new HashMap<>();
        params.put("workbench", workspaceFile.toString());
        params.put("application", "/nonexistent/fme.exe");
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
    }
    
    @Test
    @DisplayName("Parameters JSON file creation with WKT perimeter")
    public void testParametersFileCreationWithWKT() throws IOException {
        Path workspaceFile = tempDir.resolve("workspace.fmw");
        Path applicationFile = tempDir.resolve("fme.sh");
        Path outputDir = tempDir.resolve("output");
        Files.createFile(workspaceFile);
        Files.createFile(applicationFile);
        Files.createDirectory(outputDir);
        
        Map<String, String> params = new HashMap<>();
        params.put("workbench", workspaceFile.toString());
        params.put("application", applicationFile.toString());
        
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getFolderOut()).thenReturn(outputDir.toString());
        when(mockRequest.getFolderIn()).thenReturn(tempDir.toString());
        when(mockRequest.getOrderGuid()).thenReturn("order-guid-123");
        when(mockRequest.getOrderLabel()).thenReturn("Test Order");
        when(mockRequest.getClientGuid()).thenReturn("client-guid-456");
        when(mockRequest.getClient()).thenReturn("Test Client");
        when(mockRequest.getOrganismGuid()).thenReturn("org-guid-789");
        when(mockRequest.getOrganism()).thenReturn("Test Organism");
        when(mockRequest.getProductGuid()).thenReturn("product-guid-abc");
        when(mockRequest.getProductLabel()).thenReturn("Test Product");
        when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
        when(mockRequest.getParameters()).thenReturn("{\"key1\": \"value1\", \"key2\": \"value2\"}");
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        Path parametersFile = outputDir.resolve("parameters.json");
        assertTrue(Files.exists(parametersFile));
        
        String jsonContent = Files.readString(parametersFile);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonContent);
        
        assertEquals("Feature", root.get("type").textValue());
        assertNotNull(root.get("geometry"));
        assertEquals("Polygon", root.get("geometry").get("type").textValue());
        assertNotNull(root.get("properties"));
        assertEquals(123, root.get("properties").get("RequestId").intValue());
        assertEquals("Test Order", root.get("properties").get("OrderLabel").textValue());
    }
    
    @Test
    @DisplayName("Parameters JSON file creation with MultiPolygon WKT")
    public void testParametersFileCreationWithMultiPolygon() throws IOException {
        Path workspaceFile = tempDir.resolve("workspace.fmw");
        Path applicationFile = tempDir.resolve("fme.sh");
        Path outputDir = tempDir.resolve("output");
        Files.createFile(workspaceFile);
        Files.createFile(applicationFile);
        Files.createDirectory(outputDir);
        
        Map<String, String> params = new HashMap<>();
        params.put("workbench", workspaceFile.toString());
        params.put("application", applicationFile.toString());
        
        when(mockRequest.getId()).thenReturn(456);
        when(mockRequest.getFolderOut()).thenReturn(outputDir.toString());
        when(mockRequest.getFolderIn()).thenReturn(tempDir.toString());
        when(mockRequest.getPerimeter()).thenReturn("MULTIPOLYGON(((0 0, 1 0, 1 1, 0 1, 0 0)), ((2 2, 3 2, 3 3, 2 3, 2 2)))");
        when(mockRequest.getParameters()).thenReturn("{}");
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        Path parametersFile = outputDir.resolve("parameters.json");
        assertTrue(Files.exists(parametersFile));
        
        String jsonContent = Files.readString(parametersFile);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonContent);
        
        assertEquals("Feature", root.get("type").textValue());
        assertNotNull(root.get("geometry"));
        assertEquals("MultiPolygon", root.get("geometry").get("type").textValue());
    }
    
    @Test
    @DisplayName("Parameters JSON file creation with Point WKT")
    public void testParametersFileCreationWithPoint() throws IOException {
        Path workspaceFile = tempDir.resolve("workspace.fmw");
        Path applicationFile = tempDir.resolve("fme.sh");
        Path outputDir = tempDir.resolve("output");
        Files.createFile(workspaceFile);
        Files.createFile(applicationFile);
        Files.createDirectory(outputDir);
        
        Map<String, String> params = new HashMap<>();
        params.put("workbench", workspaceFile.toString());
        params.put("application", applicationFile.toString());
        
        when(mockRequest.getId()).thenReturn(789);
        when(mockRequest.getFolderOut()).thenReturn(outputDir.toString());
        when(mockRequest.getFolderIn()).thenReturn(tempDir.toString());
        when(mockRequest.getPerimeter()).thenReturn("POINT(2.5 3.7)");
        when(mockRequest.getParameters()).thenReturn(null);
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        Path parametersFile = outputDir.resolve("parameters.json");
        assertTrue(Files.exists(parametersFile));
        
        String jsonContent = Files.readString(parametersFile);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonContent);
        
        assertEquals("Feature", root.get("type").textValue());
        assertNotNull(root.get("geometry"));
        assertEquals("Point", root.get("geometry").get("type").textValue());
        ArrayNode coords = (ArrayNode) root.get("geometry").get("coordinates");
        assertEquals(2.5, coords.get(0).doubleValue(), 0.001);
        assertEquals(3.7, coords.get(1).doubleValue(), 0.001);
    }
    
    @Test
    @DisplayName("Parameters JSON file creation with LineString WKT")
    public void testParametersFileCreationWithLineString() throws IOException {
        Path workspaceFile = tempDir.resolve("workspace.fmw");
        Path applicationFile = tempDir.resolve("fme.sh");
        Path outputDir = tempDir.resolve("output");
        Files.createFile(workspaceFile);
        Files.createFile(applicationFile);
        Files.createDirectory(outputDir);
        
        Map<String, String> params = new HashMap<>();
        params.put("workbench", workspaceFile.toString());
        params.put("application", applicationFile.toString());
        
        when(mockRequest.getId()).thenReturn(999);
        when(mockRequest.getFolderOut()).thenReturn(outputDir.toString());
        when(mockRequest.getFolderIn()).thenReturn(tempDir.toString());
        when(mockRequest.getPerimeter()).thenReturn("LINESTRING(0 0, 1 1, 2 1, 2 2)");
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        Path parametersFile = outputDir.resolve("parameters.json");
        assertTrue(Files.exists(parametersFile));
        
        String jsonContent = Files.readString(parametersFile);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonContent);
        
        assertEquals("Feature", root.get("type").textValue());
        assertNotNull(root.get("geometry"));
        assertEquals("LineString", root.get("geometry").get("type").textValue());
    }
    
    @Test
    @DisplayName("Parameters JSON file creation without perimeter")
    public void testParametersFileCreationWithoutPerimeter() throws IOException {
        Path workspaceFile = tempDir.resolve("workspace.fmw");
        Path applicationFile = tempDir.resolve("fme.sh");
        Path outputDir = tempDir.resolve("output");
        Files.createFile(workspaceFile);
        Files.createFile(applicationFile);
        Files.createDirectory(outputDir);
        
        Map<String, String> params = new HashMap<>();
        params.put("workbench", workspaceFile.toString());
        params.put("application", applicationFile.toString());
        
        when(mockRequest.getId()).thenReturn(111);
        when(mockRequest.getFolderOut()).thenReturn(outputDir.toString());
        when(mockRequest.getFolderIn()).thenReturn(tempDir.toString());
        when(mockRequest.getPerimeter()).thenReturn(null);
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        Path parametersFile = outputDir.resolve("parameters.json");
        assertTrue(Files.exists(parametersFile));
        
        String jsonContent = Files.readString(parametersFile);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonContent);
        
        assertEquals("Feature", root.get("type").textValue());
        assertTrue(root.get("geometry").isNull());
    }
    
    @Test
    @DisplayName("Parameters JSON file creation with invalid WKT")
    public void testParametersFileCreationWithInvalidWKT() throws IOException {
        Path workspaceFile = tempDir.resolve("workspace.fmw");
        Path applicationFile = tempDir.resolve("fme.sh");
        Path outputDir = tempDir.resolve("output");
        Files.createFile(workspaceFile);
        Files.createFile(applicationFile);
        Files.createDirectory(outputDir);
        
        Map<String, String> params = new HashMap<>();
        params.put("workbench", workspaceFile.toString());
        params.put("application", applicationFile.toString());
        
        when(mockRequest.getId()).thenReturn(222);
        when(mockRequest.getFolderOut()).thenReturn(outputDir.toString());
        when(mockRequest.getFolderIn()).thenReturn(tempDir.toString());
        when(mockRequest.getPerimeter()).thenReturn("INVALID WKT STRING");
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        Path parametersFile = outputDir.resolve("parameters.json");
        assertTrue(Files.exists(parametersFile));
        
        String jsonContent = Files.readString(parametersFile);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonContent);
        
        assertEquals("Feature", root.get("type").textValue());
        assertTrue(root.get("geometry").isNull());
    }
    
    @Test
    @DisplayName("Parameters JSON with custom parameters as JSON object")
    public void testParametersWithCustomJsonParameters() throws IOException {
        Path workspaceFile = tempDir.resolve("workspace.fmw");
        Path applicationFile = tempDir.resolve("fme.sh");
        Path outputDir = tempDir.resolve("output");
        Files.createFile(workspaceFile);
        Files.createFile(applicationFile);
        Files.createDirectory(outputDir);
        
        Map<String, String> params = new HashMap<>();
        params.put("workbench", workspaceFile.toString());
        params.put("application", applicationFile.toString());
        
        String customParams = "{\"format\": \"shapefile\", \"projection\": \"EPSG:2056\", \"buffer\": 100}";
        
        when(mockRequest.getId()).thenReturn(333);
        when(mockRequest.getFolderOut()).thenReturn(outputDir.toString());
        when(mockRequest.getFolderIn()).thenReturn(tempDir.toString());
        when(mockRequest.getParameters()).thenReturn(customParams);
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        Path parametersFile = outputDir.resolve("parameters.json");
        assertTrue(Files.exists(parametersFile));
        
        String jsonContent = Files.readString(parametersFile);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonContent);
        
        JsonNode properties = root.get("properties");
        assertNotNull(properties.get("Parameters"));
        assertEquals("shapefile", properties.get("Parameters").get("format").textValue());
        assertEquals("EPSG:2056", properties.get("Parameters").get("projection").textValue());
        assertEquals(100, properties.get("Parameters").get("buffer").intValue());
    }
    
    @Test
    @DisplayName("Parameters JSON with custom parameters as plain string")
    public void testParametersWithCustomStringParameters() throws IOException {
        Path workspaceFile = tempDir.resolve("workspace.fmw");
        Path applicationFile = tempDir.resolve("fme.sh");
        Path outputDir = tempDir.resolve("output");
        Files.createFile(workspaceFile);
        Files.createFile(applicationFile);
        Files.createDirectory(outputDir);
        
        Map<String, String> params = new HashMap<>();
        params.put("workbench", workspaceFile.toString());
        params.put("application", applicationFile.toString());
        
        String customParams = "Not a valid JSON string";
        
        when(mockRequest.getId()).thenReturn(444);
        when(mockRequest.getFolderOut()).thenReturn(outputDir.toString());
        when(mockRequest.getFolderIn()).thenReturn(tempDir.toString());
        when(mockRequest.getParameters()).thenReturn(customParams);
        
        FmeDesktopV2Plugin instance = new FmeDesktopV2Plugin(TEST_INSTANCE_LANGUAGE, params);
        
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        Path parametersFile = outputDir.resolve("parameters.json");
        assertTrue(Files.exists(parametersFile));
        
        String jsonContent = Files.readString(parametersFile);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonContent);
        
        JsonNode properties = root.get("properties");
        assertNotNull(properties.get("Parameters"));
        assertEquals(customParams, properties.get("Parameters").textValue());
    }
}