package ch.asit_asso.extract.unit.web.model;

import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import ch.asit_asso.extract.persistence.RequestHistoryRepository;
import ch.asit_asso.extract.web.model.RequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RequestModel class, specifically testing null handling for outputFolderPath and outputFiles.
 * This addresses issue #333: Requests without geographical perimeter (IMPORTFAIL status) 
 * should be handled gracefully.
 * This also addresses issue #337: Cancelled requests without matching rules should render correctly.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RequestModelTest {

    @Mock
    private Request mockRequest;
    
    @Mock
    private RequestHistoryRepository mockHistoryRepository;
    
    @Mock
    private MessageSource mockMessageSource;
    
    @Mock
    private Connector mockConnector;
    
    private Path basePath;
    private RequestHistoryRecord[] emptyHistory;
    private String[] validationFocusProperties;
    
    @BeforeEach
    public void setUp() {
        basePath = Paths.get("/var/extract/data");
        emptyHistory = new RequestHistoryRecord[0];
        validationFocusProperties = new String[]{"property1", "property2"};
        
        // Setup default mock behavior
        when(mockRequest.getId()).thenReturn(1);
        when(mockRequest.getProductLabel()).thenReturn("Test Product");
        when(mockRequest.getOrderLabel()).thenReturn("Test Order");
        when(mockRequest.getConnector()).thenReturn(mockConnector);
        when(mockConnector.getName()).thenReturn("Test Connector");
    }
    
    /**
     * Test that RequestModel handles null folderOut correctly.
     * This is the main fix for issue #333.
     */
    @Test
    public void testRequestModelWithNullFolderOut() {
        // Given: A request with null folderOut (typical for IMPORTFAIL status)
        when(mockRequest.getFolderOut()).thenReturn(null);
        when(mockRequest.getStatus()).thenReturn(Request.Status.IMPORTFAIL);
        
        // When: Creating a RequestModel
        RequestModel model = new RequestModel(mockRequest, emptyHistory, basePath, mockMessageSource, validationFocusProperties);
        
        // Then: The model should be created successfully and getOutputFolderPath should return null
        assertNotNull(model, "RequestModel should be created even with null folderOut");
        assertNull(model.getOutputFolderPath(), "getOutputFolderPath should return null when folderOut is null");
    }
    
    /**
     * Test that RequestModel handles valid folderOut correctly.
     */
    @Test
    public void testRequestModelWithValidFolderOut() {
        // Given: A request with valid folderOut
        String folderOut = "request123/output";
        when(mockRequest.getFolderOut()).thenReturn(folderOut);
        when(mockRequest.getStatus()).thenReturn(Request.Status.ONGOING);
        
        // When: Creating a RequestModel
        RequestModel model = new RequestModel(mockRequest, emptyHistory, basePath, mockMessageSource, validationFocusProperties);
        
        // Then: The model should be created and getOutputFolderPath should return the full path
        assertNotNull(model, "RequestModel should be created with valid folderOut");
        assertNotNull(model.getOutputFolderPath(), "getOutputFolderPath should not be null with valid folderOut");
        assertTrue(model.getOutputFolderPath().contains(folderOut.replace("/", System.getProperty("file.separator"))),
                   "Output path should contain the folderOut");
    }
    
    /**
     * Test that RequestModel.fromDomainRequestsCollection handles requests with null folderOut.
     */
    @Test
    public void testFromDomainRequestsCollectionWithNullFolderOut() {
        // Given: A collection with a request having null folderOut
        List<Request> requests = new ArrayList<>();
        when(mockRequest.getFolderOut()).thenReturn(null);
        when(mockRequest.getStatus()).thenReturn(Request.Status.IMPORTFAIL);
        requests.add(mockRequest);
        
        // Mock the history repository
        List<RequestHistoryRecord> historyList = new ArrayList<>();
        when(mockHistoryRepository.findByRequestOrderByStep(any(Request.class))).thenReturn(historyList);
        
        // When: Creating RequestModels from the collection
        // Note: We need to use an existing path or mock the file system
        String testBasePath = System.getProperty("java.io.tmpdir");
        RequestModel[] models = RequestModel.fromDomainRequestsCollection(
            requests, mockHistoryRepository, testBasePath, mockMessageSource, validationFocusProperties
        );
        
        // Then: The models should be created successfully
        assertNotNull(models, "Models array should not be null");
        assertEquals(1, models.length, "Should have one model");
        assertNull(models[0].getOutputFolderPath(), "Model should have null outputFolderPath");
    }
    
    /**
     * Test that output files handling works correctly with null outputFolderPath.
     */
    @Test
    public void testGetOutputFilesWithNullFolderPath() {
        // Given: A request with null folderOut
        when(mockRequest.getFolderOut()).thenReturn(null);
        when(mockRequest.getStatus()).thenReturn(Request.Status.IMPORTFAIL);
        
        // When: Creating a RequestModel and getting output files
        RequestModel model = new RequestModel(mockRequest, emptyHistory, basePath, mockMessageSource, validationFocusProperties);
        
        // Then: getOutputFiles should return empty array, not throw exception
        assertNotNull(model.getOutputFiles(), "getOutputFiles should not return null");
        assertEquals(0, model.getOutputFiles().length, "getOutputFiles should return empty array when folderOut is null");
    }
    
    /**
     * Test that isImportFail correctly identifies IMPORTFAIL status.
     */
    @Test
    public void testIsImportFailStatus() {
        // Given: A request with IMPORTFAIL status
        when(mockRequest.getFolderOut()).thenReturn(null);
        when(mockRequest.getStatus()).thenReturn(Request.Status.IMPORTFAIL);
        
        // When: Creating a RequestModel
        RequestModel model = new RequestModel(mockRequest, emptyHistory, basePath, mockMessageSource, validationFocusProperties);
        
        // Then: isImportFail should return true
        assertTrue(model.isImportFail(), "isImportFail should return true for IMPORTFAIL status");
        assertTrue(model.isInError(), "isInError should return true for IMPORTFAIL status");
    }
    
    /**
     * Test that isWaitingIntervention works correctly with IMPORTFAIL status and null folderOut.
     */
    @Test
    public void testIsWaitingInterventionWithImportFail() {
        // Given: A request with IMPORTFAIL status and no history
        when(mockRequest.getFolderOut()).thenReturn(null);
        when(mockRequest.getStatus()).thenReturn(Request.Status.IMPORTFAIL);
        
        // When: Creating a RequestModel
        RequestModel model = new RequestModel(mockRequest, emptyHistory, basePath, mockMessageSource, validationFocusProperties);
        
        // Then: isWaitingIntervention should return true
        assertTrue(model.isWaitingIntervention(),
                   "isWaitingIntervention should return true for IMPORTFAIL with no history");
    }
    
    /**
     * Test that getOutputFiles returns empty array when outputFolderPath is null.
     * This addresses issue #337: Template should handle null outputFiles gracefully.
     */
    @Test
    @DisplayName("getOutputFiles should return empty array when outputFolderPath is null")
    public void testGetOutputFilesWithNullFolderPathIssue337() {
        // Given: A request with null folderOut (common in UNMATCHED or IMPORTFAIL status)
        when(mockRequest.getFolderOut()).thenReturn(null);
        when(mockRequest.getStatus()).thenReturn(Request.Status.UNMATCHED);
        
        // When: Creating a RequestModel and getting output files
        RequestModel model = new RequestModel(mockRequest, emptyHistory, basePath, mockMessageSource, validationFocusProperties);
        Object[] outputFiles = model.getOutputFiles();
        
        // Then: outputFiles should be empty array, not null
        assertNotNull(outputFiles, "outputFiles should never be null");
        assertEquals(0, outputFiles.length, "outputFiles should be empty when folderOut is null");
    }
    
    /**
     * Test that RequestModel handles UNMATCHED status with null outputFolderPath correctly.
     * This addresses issue #337: Cancelled requests without rules should not cause NPE.
     */
    @Test
    @DisplayName("RequestModel should handle UNMATCHED status with null folder correctly")
    public void testUnmatchedStatusWithNullFolder() {
        // Given: An UNMATCHED request (no matching rules found)
        when(mockRequest.getFolderOut()).thenReturn(null);
        when(mockRequest.getStatus()).thenReturn(Request.Status.UNMATCHED);
        when(mockRequest.getRemark()).thenReturn("No matching rules found");
        when(mockRequest.getConnector()).thenReturn(mockConnector);
        when(mockConnector.getName()).thenReturn("Test Connector");
        
        // When: Creating a RequestModel
        RequestModel model = new RequestModel(mockRequest, emptyHistory, basePath, mockMessageSource, validationFocusProperties);
        
        // Then: Model should handle nulls gracefully
        assertNull(model.getOutputFolderPath(), "outputFolderPath should be null");
        assertNotNull(model.getOutputFiles(), "outputFiles should not be null");
        assertEquals(0, model.getOutputFiles().length, "outputFiles should be empty");
        assertTrue(model.isUnmatched(), "isUnmatched should be true");
        assertEquals("No matching rules found", model.getRemark());
    }
    
    /**
     * Test that RequestModel handles ERROR status with null outputFiles correctly.
     * This ensures the template can safely check array operations.
     */
    @Test
    @DisplayName("RequestModel should handle ERROR status with null output correctly")  
    public void testErrorStatusWithNullOutput() {
        // Given: A request in ERROR status with no output
        when(mockRequest.getFolderOut()).thenReturn(null);
        when(mockRequest.getStatus()).thenReturn(Request.Status.ERROR);
        when(mockRequest.getRemark()).thenReturn("Processing failed");
        
        // When: Creating a RequestModel
        RequestModel model = new RequestModel(mockRequest, emptyHistory, basePath, mockMessageSource, validationFocusProperties);
        
        // Then: Should handle all null scenarios
        assertNull(model.getOutputFolderPath(), "outputFolderPath should be null");
        assertNotNull(model.getOutputFiles(), "outputFiles should never be null to prevent template errors");
        assertEquals(0, model.getOutputFiles().length, "outputFiles should be empty array");
        assertTrue(model.isInError(), "isInError should be true");
    }
    
}