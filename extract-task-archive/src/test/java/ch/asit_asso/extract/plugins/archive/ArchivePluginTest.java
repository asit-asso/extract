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
package ch.asit_asso.extract.plugins.archive;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ArchivePlugin
 * 
 * @author Extract Team
 */
public class ArchivePluginTest {
    
    private static final String EXPECTED_PLUGIN_CODE = "ARCHIVE";
    private static final String EXPECTED_ICON_CLASS = "fa-folder-open-o";
    private static final String TEST_INSTANCE_LANGUAGE = "fr";
    private static final String LABEL_STRING_IDENTIFIER = "plugin.label";
    private static final String DESCRIPTION_STRING_IDENTIFIER = "plugin.description";
    private static final String HELP_FILE_NAME = "archivageHelp.html";
    
    private final Logger logger = LoggerFactory.getLogger(ArchivePluginTest.class);
    
    @Mock
    private ITaskProcessorRequest mockRequest;
    
    @Mock
    private IEmailSettings mockEmailSettings;
    
    @TempDir
    Path tempDir;
    
    private LocalizedMessages messages;
    private ObjectMapper parameterMapper;
    private Map<String, String> testParameters;
    private ArchivePlugin plugin;
    private PluginConfiguration config;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.messages = new LocalizedMessages(TEST_INSTANCE_LANGUAGE);
        this.parameterMapper = new ObjectMapper();
        this.config = new PluginConfiguration("plugins/archivage/properties/configArchivage.properties");
        
        this.testParameters = new HashMap<>();
        this.plugin = new ArchivePlugin(TEST_INSTANCE_LANGUAGE, testParameters);
    }
    
    @Test
    @DisplayName("Create a new instance without parameter values")
    public void testNewInstanceWithoutParameters() {
        ArchivePlugin instance = new ArchivePlugin();
        ArchivePlugin result = instance.newInstance(TEST_INSTANCE_LANGUAGE);
        
        assertNotSame(instance, result);
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Create a new instance with parameter values")
    public void testNewInstanceWithParameters() {
        ArchivePlugin instance = new ArchivePlugin();
        Map<String, String> params = new HashMap<>();
        params.put("path", "/archive/path");
        
        ArchivePlugin result = instance.newInstance(TEST_INSTANCE_LANGUAGE, params);
        
        assertNotSame(instance, result);
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Check the plugin label")
    public void testGetLabel() {
        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE);
        String expectedLabel = messages.getString(LABEL_STRING_IDENTIFIER);
        
        String result = instance.getLabel();
        
        assertEquals(expectedLabel, result);
    }
    
    @Test
    @DisplayName("Check the plugin identifier")
    public void testGetCode() {
        ArchivePlugin instance = new ArchivePlugin();
        
        String result = instance.getCode();
        
        assertEquals(EXPECTED_PLUGIN_CODE, result);
    }
    
    @Test
    @DisplayName("Check the plugin description")
    public void testGetDescription() {
        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE);
        String expectedDescription = messages.getString(DESCRIPTION_STRING_IDENTIFIER);
        
        String result = instance.getDescription();
        
        assertEquals(expectedDescription, result);
    }
    
    @Test
    @DisplayName("Check the help content")
    public void testGetHelp() {
        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE);
        String expectedHelp = messages.getFileContent(HELP_FILE_NAME);
        
        String result = instance.getHelp();
        
        assertEquals(expectedHelp, result);
    }
    
    @Test
    @DisplayName("Check the plugin pictogram")
    public void testGetPictoClass() {
        ArchivePlugin instance = new ArchivePlugin();
        
        String result = instance.getPictoClass();
        
        assertEquals(EXPECTED_ICON_CLASS, result);
    }
    
    @Test
    @DisplayName("Check the plugin parameters structure")
    public void testGetParams() throws IOException {
        ArchivePlugin instance = new ArchivePlugin();
        
        String paramsJson = instance.getParams();
        assertNotNull(paramsJson);
        
        ArrayNode parametersArray = parameterMapper.readValue(paramsJson, ArrayNode.class);
        assertNotNull(parametersArray);
        assertEquals(1, parametersArray.size());
        
        JsonNode pathParam = parametersArray.get(0);
        assertTrue(pathParam.hasNonNull("code"));
        assertEquals(config.getProperty("paramPath"), pathParam.get("code").textValue());
        
        assertTrue(pathParam.hasNonNull("label"));
        assertNotNull(pathParam.get("label").textValue());
        
        assertTrue(pathParam.hasNonNull("type"));
        assertEquals("text", pathParam.get("type").textValue());
        
        assertTrue(pathParam.hasNonNull("req"));
        assertTrue(pathParam.get("req").booleanValue());
        
        assertTrue(pathParam.hasNonNull("maxlength"));
        assertEquals(255, pathParam.get("maxlength").intValue());
    }
    
    @Test
    @DisplayName("Execute archive with valid source and destination")
    public void testExecuteSuccess() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("archive");
        Files.createDirectory(sourceDir);
        
        Path testFile1 = sourceDir.resolve("test1.txt");
        Path testFile2 = sourceDir.resolve("test2.txt");
        Path subDir = sourceDir.resolve("subdir");
        Files.createDirectory(subDir);
        Path testFile3 = subDir.resolve("test3.txt");
        
        Files.write(testFile1, "Content 1".getBytes(StandardCharsets.UTF_8));
        Files.write(testFile2, "Content 2".getBytes(StandardCharsets.UTF_8));
        Files.write(testFile3, "Content 3".getBytes(StandardCharsets.UTF_8));
        
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramPath"), destDir.toString());
        
        when(mockRequest.getFolderOut()).thenReturn(sourceDir.toString());
        
        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE, params);
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        
        assertTrue(Files.exists(destDir));
        assertTrue(Files.exists(destDir.resolve("test1.txt")));
        assertTrue(Files.exists(destDir.resolve("test2.txt")));
        assertTrue(Files.exists(destDir.resolve("subdir/test3.txt")));
        
        assertEquals("Content 1", Files.readString(destDir.resolve("test1.txt")));
        assertEquals("Content 2", Files.readString(destDir.resolve("test2.txt")));
        assertEquals("Content 3", Files.readString(destDir.resolve("subdir/test3.txt")));
    }
    
    @Test
    @DisplayName("Execute archive with non-existent source directory")
    public void testExecuteNonExistentSource() {
        Path sourceDir = tempDir.resolve("nonexistent");
        Path destDir = tempDir.resolve("archive");
        
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramPath"), destDir.toString());
        
        when(mockRequest.getFolderOut()).thenReturn(sourceDir.toString());
        
        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE, params);
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertTrue(result.getMessage().contains("notexists") || result.getMessage().contains("n'existe pas"));
    }
    
    @Test
    @DisplayName("Test path building with property placeholders")
    public void testBuildPathWithPropertyValues() {
        String pathTemplate = "/archive/{ORDERLABEL}/{CLIENT}/{PRODUCTLABEL}";

        TestTaskProcessorRequest testRequest = new TestTaskProcessorRequest();
        testRequest.orderLabel = "ORDER-12345";
        testRequest.client = "Test Client";
        testRequest.productLabel = "Test Product";

        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE);
        String result = instance.buildPathWithPropertyValues(pathTemplate, testRequest);

        assertEquals("/archive/ORDER-12345/Test_Client/Test_Product", result);
    }
    
    @Test
    @DisplayName("Test path building with date fields")
    public void testBuildPathWithDateFields() {
        String pathTemplate = "/archive/{STARTDATE}/{ORDERLABEL}";

        TestTaskProcessorRequest testRequest = new TestTaskProcessorRequest();
        testRequest.orderLabel = "ORDER-67890";
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15);
        testRequest.startDate = cal;

        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE);
        String result = instance.buildPathWithPropertyValues(pathTemplate, testRequest);

        assertTrue(result.startsWith("/archive/2024-01-15/ORDER-67890"));
    }
    
    @Test
    @DisplayName("Test path building with special characters sanitization")
    public void testBuildPathWithSpecialCharacters() {
        String pathTemplate = "/archive/{CLIENT}";

        TestTaskProcessorRequest testRequest = new TestTaskProcessorRequest();
        testRequest.client = "Client <with> special*chars/and:spaces";

        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE);
        String result = instance.buildPathWithPropertyValues(pathTemplate, testRequest);

        assertEquals("/archive/Client__with__special_chars_and_spaces", result);
    }
    
    @Test
    @DisplayName("Test path building with accented characters")
    public void testBuildPathWithAccentedCharacters() {
        String pathTemplate = "/archive/{CLIENT}";

        TestTaskProcessorRequest testRequest = new TestTaskProcessorRequest();
        testRequest.client = "Société Générale";

        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE);
        String result = instance.buildPathWithPropertyValues(pathTemplate, testRequest);

        assertEquals("/archive/Societe_Generale", result);
    }
    
    @Test
    @DisplayName("Test path building with null field values")
    public void testBuildPathWithNullFields() {
        String pathTemplate = "/archive/{CLIENT}/{PRODUCTLABEL}";

        TestTaskProcessorRequest testRequest = new TestTaskProcessorRequest();
        testRequest.client = null;
        testRequest.productLabel = "Product";

        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE);
        String result = instance.buildPathWithPropertyValues(pathTemplate, testRequest);

        assertEquals("/archive//Product", result);
    }
    
    @Test
    @DisplayName("Test path building with invalid field names")
    public void testBuildPathWithInvalidFields() {
        String pathTemplate = "/archive/{INVALIDFIELD}/{ORDERLABEL}";

        TestTaskProcessorRequest testRequest = new TestTaskProcessorRequest();
        testRequest.orderLabel = "ORDER-999";

        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE);
        String result = instance.buildPathWithPropertyValues(pathTemplate, testRequest);

        assertEquals("/archive/{INVALIDFIELD}/ORDER-999", result);
    }
    
    @Test
    @DisplayName("Execute archive creates destination directory if not exists")
    public void testExecuteCreatesDestinationDirectory() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("new/archive/path");
        Files.createDirectory(sourceDir);
        
        Path testFile = sourceDir.resolve("test.txt");
        Files.write(testFile, "Test content".getBytes(StandardCharsets.UTF_8));
        
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramPath"), destDir.toString());
        
        when(mockRequest.getFolderOut()).thenReturn(sourceDir.toString());
        
        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE, params);
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertTrue(Files.exists(destDir));
        assertTrue(Files.exists(destDir.resolve("test.txt")));
    }
    
    @Test
    @DisplayName("Execute archive with empty source directory")
    public void testExecuteEmptySourceDirectory() throws IOException {
        Path sourceDir = tempDir.resolve("empty");
        Path destDir = tempDir.resolve("archive");
        Files.createDirectory(sourceDir);
        
        Map<String, String> params = new HashMap<>();
        params.put(config.getProperty("paramPath"), destDir.toString());
        
        when(mockRequest.getFolderOut()).thenReturn(sourceDir.toString());
        
        ArchivePlugin instance = new ArchivePlugin(TEST_INSTANCE_LANGUAGE, params);
        ITaskProcessorResult result = instance.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertTrue(Files.exists(destDir));
        assertEquals(0, Files.list(destDir).count());
    }
    
    /**
     * Test helper class implementing ITaskProcessorRequest for testing field access
     */
    private static class TestTaskProcessorRequest implements ITaskProcessorRequest {
        public String orderLabel;
        public String client;
        public String productLabel;
        public Calendar startDate;

        @Override
        public int getId() { return 0; }
        
        @Override
        public String getFolderOut() { return "/test/out"; }
        
        @Override
        public String getFolderIn() { return "/test/in"; }
        
        @Override
        public String getPerimeter() { return null; }
        
        @Override
        public String getParameters() { return null; }
        
        @Override
        public String getOrderGuid() { return null; }
        
        @Override
        public String getOrderLabel() { return null; }
        
        @Override
        public String getProductGuid() { return null; }
        
        @Override
        public String getProductLabel() { return null; }
        
        @Override
        public String getOrganismGuid() { return null; }
        
        @Override
        public String getOrganism() { return null; }
        
        @Override
        public String getClientGuid() { return null; }
        
        @Override
        public String getClient() { return client; }
        
        @Override
        public String getRemark() { return null; }
        
        @Override
        public String getStatus() { return null; }
        
        @Override
        public Calendar getEndDate() { return null; }
        
        @Override
        public boolean isRejected() { return false; }
        
        @Override
        public Calendar getStartDate() { return startDate; }
        
        @Override
        public String getTiers() { return null; }
        
        @Override
        public String getSurface() { return null; }
    }
}