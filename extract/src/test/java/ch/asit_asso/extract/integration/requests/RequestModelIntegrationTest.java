package ch.asit_asso.extract.integration.requests;

import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.RequestHistoryRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.web.model.RequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RequestModel with database, specifically testing handling of IMPORTFAIL requests.
 * This addresses issue #333: Requests without geographical perimeter should be handled gracefully.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
public class RequestModelIntegrationTest {

    @Autowired
    private RequestsRepository requestsRepository;
    
    @Autowired
    private ConnectorsRepository connectorsRepository;
    
    @Autowired
    private RequestHistoryRepository historyRepository;
    
    @Autowired
    private MessageSource messageSource;
    
    private Request testRequestWithNullFolder;
    private Request testRequestWithFolder;
    private Connector testConnector;
    
    @BeforeEach
    public void setUp() {
        // Create a test connector
        testConnector = new Connector();
        testConnector.setName("Test Connector");
        // testConnector.setLogin("test"); // Login is not a field on Connector
        testConnector.setActive(Boolean.TRUE);
        testConnector = connectorsRepository.save(testConnector);
        
        // Create a request with null folder (IMPORTFAIL status)
        testRequestWithNullFolder = new Request();
        testRequestWithNullFolder.setProductLabel("Test Request Without Perimeter");
        testRequestWithNullFolder.setOrderLabel("ORDER-333");
        testRequestWithNullFolder.setClient("Test Client");
        testRequestWithNullFolder.setStatus(Request.Status.IMPORTFAIL);
        testRequestWithNullFolder.setFolderOut(null); // This is the key - null folder
        testRequestWithNullFolder.setStartDate(new GregorianCalendar());
        testRequestWithNullFolder.setConnector(testConnector);
        testRequestWithNullFolder = requestsRepository.save(testRequestWithNullFolder);
        
        // Create a normal request with folder
        testRequestWithFolder = new Request();
        testRequestWithFolder.setProductLabel("Normal Request");
        testRequestWithFolder.setOrderLabel("ORDER-NORMAL");
        testRequestWithFolder.setClient("Test Client");
        testRequestWithFolder.setStatus(Request.Status.ONGOING);
        testRequestWithFolder.setFolderOut("request123/output");
        testRequestWithFolder.setStartDate(new GregorianCalendar());
        testRequestWithFolder.setConnector(testConnector);
        testRequestWithFolder = requestsRepository.save(testRequestWithFolder);
    }
    
    @Test
    @DisplayName("RequestModel should handle IMPORTFAIL request with null folder path")
    public void testRequestModelWithNullFolderPath() {
        // Given: A persisted request with null folder
        assertNotNull(testRequestWithNullFolder);
        assertNull(testRequestWithNullFolder.getFolderOut());
        assertEquals(Request.Status.IMPORTFAIL, testRequestWithNullFolder.getStatus());
        
        // When: Creating a RequestModel from the database entity
        RequestModel model = new RequestModel(
            testRequestWithNullFolder,
            historyRepository.findByRequestOrderByStep(testRequestWithNullFolder).toArray(new ch.asit_asso.extract.domain.RequestHistoryRecord[0]),
            Paths.get("/var/extract/data"),
            messageSource,
            new String[]{}
        );
        
        // Then: The model should be created successfully with null outputFolderPath
        assertNotNull(model, "RequestModel should be created even with null folderOut");
        assertNull(model.getOutputFolderPath(), "OutputFolderPath should be null when folderOut is null");
        assertTrue(model.isImportFail(), "Should be identified as IMPORTFAIL");
        assertTrue(model.isInError(), "Should be identified as in error");
        assertEquals(0, model.getOutputFiles().length, "Should have no output files");
    }
    
    @Test
    @DisplayName("RequestModel should handle normal request with valid folder path")
    public void testRequestModelWithValidFolderPath() {
        // Given: A persisted request with valid folder
        assertNotNull(testRequestWithFolder);
        assertNotNull(testRequestWithFolder.getFolderOut());
        assertEquals(Request.Status.ONGOING, testRequestWithFolder.getStatus());
        
        // When: Creating a RequestModel from the database entity
        RequestModel model = new RequestModel(
            testRequestWithFolder,
            historyRepository.findByRequestOrderByStep(testRequestWithFolder).toArray(new ch.asit_asso.extract.domain.RequestHistoryRecord[0]),
            Paths.get("/var/extract/data"),
            messageSource,
            new String[]{}
        );
        
        // Then: The model should be created with valid outputFolderPath
        assertNotNull(model, "RequestModel should be created with valid folderOut");
        assertNotNull(model.getOutputFolderPath(), "OutputFolderPath should not be null with valid folderOut");
        assertTrue(model.getOutputFolderPath().contains("request123"), "Path should contain the folder name");
        assertFalse(model.isImportFail(), "Should not be identified as IMPORTFAIL");
        assertFalse(model.isInError(), "Should not be identified as in error");
    }
    
    @Test
    @DisplayName("RequestModel.fromDomainRequestsCollection should handle mixed requests")
    public void testFromDomainRequestsCollectionWithMixedRequests() {
        // Given: A collection with both null and valid folder requests
        var requests = requestsRepository.findAll();
        // Verify we have some requests
        var requestList = new java.util.ArrayList<Request>();
        requests.forEach(requestList::add);
        assertTrue(requestList.size() >= 2, "Should have at least 2 requests");
        
        // When: Creating RequestModels from the collection
        RequestModel[] models = RequestModel.fromDomainRequestsCollection(
            requestList,
            historyRepository,
            "/var/extract/data",
            messageSource,
            new String[]{}
        );
        
        // Then: All models should be created successfully
        assertNotNull(models, "Models array should not be null");
        assertEquals(requestList.size(), models.length, "Should create model for each request");
        
        // Verify we have both types
        boolean hasNullPath = false;
        boolean hasValidPath = false;
        for (RequestModel model : models) {
            if (model.getOutputFolderPath() == null) {
                hasNullPath = true;
            } else {
                hasValidPath = true;
            }
        }
        assertTrue(hasNullPath, "Should have at least one model with null path");
        assertTrue(hasValidPath, "Should have at least one model with valid path");
    }
}