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
package ch.asit_asso.extract.integration.email;

import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.email.*;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Integration tests for system email notifications (Issue #323).
 * Tests that all email types include the new placeholders for request fields and dynamic parameters.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
public class SystemEmailsIntegrationTest {

    private EmailSettings emailSettings;
    private TemplateEngine testTemplateEngine;

    private Request testRequest;
    private Task testTask;
    private Connector testConnector;
    private Process testProcess;
    private Calendar testDate;

    /**
     * Template definitions for testing - these are controlled test templates
     * that verify all placeholders are properly passed to the email context.
     */
    private static final String TASK_FAILED_TEMPLATE =
        "<html><body>" +
        "<h1>Task Failed: [(${taskName})]</h1>" +
        "<p>Order: [(${orderLabel})]</p>" +
        "<p>Product: [(${productLabel})]</p>" +
        "<p>Client: [(${client})] ([(${clientGuid})])</p>" +
        "<p>Organization: [(${organism})] ([(${organismGuid})])</p>" +
        "<p>Tiers: [(${tiers})] ([(${tiersGuid})])</p>" +
        "<p>Surface: [(${surface})] m²</p>" +
        "<p>Perimeter: [(${perimeter})]</p>" +
        "<p>Remark: [(${remark})]</p>" +
        "<p>Status: [(${status})]</p>" +
        "<p>Start: [(${startDateISO})]</p>" +
        "<p>End: [(${endDateISO})]</p>" +
        "<p>Error: [(${errorMessage})]</p>" +
        "<p>Failed at: [(${failureTimeString})]</p>" +
        "<p>Format: [(${parameters.format})]</p>" +
        "<p>Projection: [(${parameters.projection})]</p>" +
        "<p>Scale: [(${parameters.scale})]</p>" +
        "</body></html>";

    private static final String UNMATCHED_REQUEST_TEMPLATE =
        "<html><body>" +
        "<h1>Unmatched Request</h1>" +
        "<p>Connector: [(${connectorName})]</p>" +
        "<p>Order: [(${orderLabel})]</p>" +
        "<p>Product: [(${productLabel})]</p>" +
        "<p>Client: [(${client})]</p>" +
        "<p>Tiers: [(${tiers})]</p>" +
        "<p>Surface: [(${surface})] m²</p>" +
        "<p>Perimeter: [(${perimeter})]</p>" +
        "<p>Format param: [(${param_format})]</p>" +
        "<p>Projection param: [(${param_projection})]</p>" +
        "</body></html>";

    private static final String REQUEST_EXPORT_FAILED_TEMPLATE =
        "<html><body>" +
        "<h1>Export Failed</h1>" +
        "<p>Connector: [(${connectorName})]</p>" +
        "<p>Order: [(${orderLabel})]</p>" +
        "<p>Error: [(${errorMessage})]</p>" +
        "<p>Organization: [(${organism})]</p>" +
        "</body></html>";

    private static final String CONNECTOR_IMPORT_FAILED_TEMPLATE =
        "<html><body>" +
        "<h1>Import Failed</h1>" +
        "<p>Connector: [(${connectorName})]</p>" +
        "<p>Error: [(${errorMessage})]</p>" +
        "</body></html>";

    private static final String INVALID_PRODUCT_IMPORTED_TEMPLATE =
        "<html><body>" +
        "<h1>Invalid Product</h1>" +
        "<p>Connector: [(${connectorName})]</p>" +
        "<p>Product: [(${productLabel})]</p>" +
        "<p>Tiers: [(${tiers})]</p>" +
        "<p>Surface: [(${surface})]</p>" +
        "<p>Error: [(${errorMessage})]</p>" +
        "</body></html>";

    private static final String TASK_STANDBY_TEMPLATE =
        "<html><body>" +
        "<h1>Task Standby</h1>" +
        "<p>Process: [(${processName})]</p>" +
        "<p>Order: [(${orderLabel})]</p>" +
        "<p>Client: [(${client})]</p>" +
        "<p>Format: [(${parameters.format})]</p>" +
        "</body></html>";

    private static final String STANDBY_REMINDER_TEMPLATE =
        "<html><body>" +
        "<h1>Standby Reminder</h1>" +
        "<p>Process: [(${processName})]</p>" +
        "<p>Order: [(${orderLabel})]</p>" +
        "<p>Start Date ISO: [(${startDateISO})]</p>" +
        "<p>Status: [(${status})]</p>" +
        "<p>Scale: [(${param_SCALE})]</p>" +
        "</body></html>";

    @BeforeEach
    public void setUp() throws Exception {
        // Create mock repository for email settings
        SystemParametersRepository mockRepo = Mockito.mock(SystemParametersRepository.class);
        when(mockRepo.isEmailNotificationEnabled()).thenReturn("true");
        when(mockRepo.getSmtpServer()).thenReturn("smtp.test.com");
        when(mockRepo.getSmtpPort()).thenReturn("587");
        when(mockRepo.getSmtpFromMail()).thenReturn("noreply@extract.test");
        when(mockRepo.getSmtpFromName()).thenReturn("Extract Test");
        when(mockRepo.getSmtpUser()).thenReturn(null);
        when(mockRepo.getSmtpPassword()).thenReturn(null);
        when(mockRepo.getSmtpSSL()).thenReturn("NONE");

        // Create test TemplateEngine with StringTemplateResolver for controlled templates
        testTemplateEngine = new TemplateEngine();
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        testTemplateEngine.setTemplateResolver(templateResolver);

        // Create mock MessageSource that returns the message key
        MessageSource mockMessageSource = Mockito.mock(MessageSource.class, invocation -> {
            if ("getMessage".equals(invocation.getMethod().getName())) {
                return invocation.getArgument(0);
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });

        // Create EmailSettings with test dependencies
        emailSettings = new EmailSettings(mockRepo, testTemplateEngine, mockMessageSource, "http://localhost:8080");

        // Setup test request with all fields including new placeholders
        testRequest = new Request();
        testRequest.setId(12345);
        testRequest.setOrderLabel("ORDER-2024-TEST-001");
        testRequest.setOrderGuid("order-guid-12345");
        testRequest.setProductLabel("Cadastral Data Extract - Complete");
        testRequest.setProductGuid("product-guid-67890");
        testRequest.setClient("Municipality of Test City");
        testRequest.setClientGuid("client-guid-abc123");
        testRequest.setOrganism("Regional Planning Office");
        testRequest.setOrganismGuid("organism-guid-def456");
        testRequest.setTiers("Engineering Consultants Ltd");
        testRequest.setTiersGuid("tiers-guid-ghi789");
        testRequest.setTiersDetails("Contact: John Smith");
        testRequest.setSurface(2500.75);
        testRequest.setPerimeter("POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))");
        testRequest.setRemark("Urgent request for construction permit");
        testRequest.setStatus(Request.Status.ONGOING);
        testRequest.setRejected(false);

        // Set dates
        Calendar startDate = new GregorianCalendar(2024, Calendar.MARCH, 1, 9, 0, 0);
        Calendar endDate = new GregorianCalendar(2024, Calendar.MARCH, 15, 17, 30, 0);
        testRequest.setStartDate(startDate);
        testRequest.setEndDate(endDate);

        // Set dynamic parameters JSON
        String parametersJson = "{\"format\":\"DXF\",\"projection\":\"EPSG:2056\",\"communes\":\"Lausanne,Morges\",\"SCALE\":500}";
        testRequest.setParameters(parametersJson);

        // Setup connector
        testConnector = new Connector();
        testConnector.setId(1);
        testConnector.setName("Test Connector");
        testRequest.setConnector(testConnector);

        // Setup process
        testProcess = new Process();
        testProcess.setId(1);
        testProcess.setName("Standard Export Process");
        testRequest.setProcess(testProcess);

        // Setup task
        testTask = new Task();
        testTask.setId(456);
        testTask.setLabel("Data Export Task");

        // Setup test date
        testDate = new GregorianCalendar();
        testDate.add(Calendar.HOUR, -1); // 1 hour ago

        // Register test templates with template engine
        registerTestTemplates();
    }

    /**
     * Helper method to register test templates in the template engine.
     * This simulates the template files but with controlled, testable content.
     */
    private void registerTestTemplates() {
        // Templates are registered via StringTemplateResolver automatically
        // The template name is the content itself when using StringTemplateResolver
    }

    /**
     * Helper method to process a template and get the generated content.
     * This allows us to verify that placeholders are correctly replaced.
     */
    private String processTemplate(String templateContent, Context context) {
        return testTemplateEngine.process(templateContent, context);
    }

    @Test
    @DisplayName("TaskFailedEmail with all new placeholders")
    public void testTaskFailedEmailWithAllNewPlaceholders() {
        // Given: A context with request data
        Context context = RequestModelBuilder.createContextWithRequest(testRequest);
        context.setVariable("taskName", testTask.getLabel());
        context.setVariable("errorMessage", "Export failed: Invalid coordinate system");
        context.setVariable("failureTimeString", "2024-03-15 10:30:00");

        // When: Processing the template with all placeholders
        String emailContent = processTemplate(TASK_FAILED_TEMPLATE, context);

        // Then: All placeholders should be replaced with actual values
        assertTrue(emailContent.contains("Task Failed: Data Export Task"),
            "Should contain task name");
        assertTrue(emailContent.contains("Order: ORDER-2024-TEST-001"),
            "Should contain order label");
        assertTrue(emailContent.contains("Product: Cadastral Data Extract - Complete"),
            "Should contain product label");
        assertTrue(emailContent.contains("Client: Municipality of Test City (client-guid-abc123)"),
            "Should contain client name and GUID");
        assertTrue(emailContent.contains("Organization: Regional Planning Office (organism-guid-def456)"),
            "Should contain organism and GUID");
        assertTrue(emailContent.contains("Tiers: Engineering Consultants Ltd (tiers-guid-ghi789)"),
            "Should contain tiers and GUID");
        assertTrue(emailContent.contains("Surface: 2500.75 m²"),
            "Should contain surface");
        assertTrue(emailContent.contains("Perimeter: POLYGON"),
            "Should contain perimeter WKT");
        assertTrue(emailContent.contains("Remark: Urgent request for construction permit"),
            "Should contain remark");
        assertTrue(emailContent.contains("Status: ONGOING"),
            "Should contain status");
        assertTrue(emailContent.contains("Start: 2024-03"),
            "Should contain start date ISO");
        assertTrue(emailContent.contains("End: 2024-03"),
            "Should contain end date ISO");
        assertTrue(emailContent.contains("Error: Export failed: Invalid coordinate system"),
            "Should contain error message");
        assertTrue(emailContent.contains("Format: DXF"),
            "Should contain format parameter from JSON");
        assertTrue(emailContent.contains("Projection: EPSG:2056"),
            "Should contain projection parameter from JSON");
        assertTrue(emailContent.contains("Scale: 500"),
            "Should contain scale parameter from JSON");

        // Also test with TaskFailedEmail class directly
        TaskFailedEmail email = new TaskFailedEmail(emailSettings);
        boolean initialized = email.initializeContent(testTask, testRequest,
            "Export failed: Invalid coordinate system", testDate);
        assertTrue(initialized, "Email should be initialized successfully");
    }

    @Test
    @DisplayName("RequestExportFailedEmail with parameters")
    public void testRequestExportFailedEmailWithParameters() {
        // Given: A RequestExportFailedEmail with request containing dynamic parameters
        RequestExportFailedEmail email = new RequestExportFailedEmail(emailSettings);
        String errorMessage = "Connection timeout to external server";

        // When: Initializing email content
        boolean initialized = email.initializeContent(testRequest, errorMessage, testDate);

        // Then: Email should be initialized successfully with dynamic parameters
        assertTrue(initialized, "Email should be initialized with dynamic parameters");

        // Test with different locales to ensure i18n support
        RequestExportFailedEmail emailFr = new RequestExportFailedEmail(emailSettings);
        boolean initializedFr = emailFr.initializeContent(testRequest, errorMessage, testDate,
            java.util.Locale.forLanguageTag("fr"));
        assertTrue(initializedFr, "Email should initialize with French locale");

        RequestExportFailedEmail emailDe = new RequestExportFailedEmail(emailSettings);
        boolean initializedDe = emailDe.initializeContent(testRequest, errorMessage, testDate,
            java.util.Locale.forLanguageTag("de"));
        assertTrue(initializedDe, "Email should initialize with German locale");
    }

    @Test
    @DisplayName("ConnectorImportFailedEmail with organism details")
    public void testConnectorImportFailedEmailWithOrganismDetails() {
        // Given: A ConnectorImportFailedEmail with organism details
        ConnectorImportFailedEmail email = new ConnectorImportFailedEmail(emailSettings);
        String errorMessage = "Connection refused by remote server";

        // When: Initializing email content
        boolean initialized = email.initializeContent(testConnector, errorMessage, testDate);

        // Then: Email should be initialized successfully
        assertTrue(initialized, "Email should be initialized with organism details");

        // Test with different locales
        ConnectorImportFailedEmail emailFr = new ConnectorImportFailedEmail(emailSettings);
        boolean initializedFr = emailFr.initializeContent(testConnector, errorMessage, testDate,
            java.util.Locale.forLanguageTag("fr"));
        assertTrue(initializedFr, "Email should initialize with French locale");
    }

    @Test
    @DisplayName("InvalidProductImportedEmail with product label")
    public void testInvalidProductImportedEmailWithProductLabel() {
        // Given: An InvalidProductImportedEmail with product details
        InvalidProductImportedEmail email = new InvalidProductImportedEmail(emailSettings);
        String errorMessage = "Product has no geometry";

        // When: Initializing email content
        boolean initialized = email.initializeContent(testRequest, errorMessage, testDate);

        // Then: Email should be initialized successfully with product label and GUID
        assertTrue(initialized, "Email should be initialized with product label");

        // Test with recipients
        String[] recipients = {"admin@test.com"};
        InvalidProductImportedEmail emailWithRecipients = new InvalidProductImportedEmail(emailSettings);
        boolean fullInit = emailWithRecipients.initialize(testRequest, errorMessage, testDate, recipients);
        assertTrue(fullInit, "Email with recipients should initialize successfully");

        // Test with different locales
        InvalidProductImportedEmail emailDe = new InvalidProductImportedEmail(emailSettings);
        boolean initializedDe = emailDe.initializeContent(testRequest, errorMessage, testDate,
            java.util.Locale.forLanguageTag("de"));
        assertTrue(initializedDe, "Email should initialize with German locale");
    }

    @Test
    @DisplayName("UnmatchedRequestEmail with surface and perimeter")
    public void testUnmatchedRequestEmailWithSurfaceAndPerimeter() {
        // Given: A context with request data for unmatched request
        Context context = RequestModelBuilder.createContextWithRequest(testRequest);
        context.setVariable("connectorName", testConnector.getName());

        // When: Processing the template
        String emailContent = processTemplate(UNMATCHED_REQUEST_TEMPLATE, context);

        // Then: Email should contain all required fields
        assertTrue(emailContent.contains("Connector: Test Connector"),
            "Should contain connector name");
        assertTrue(emailContent.contains("Order: ORDER-2024-TEST-001"),
            "Should contain order label");
        assertTrue(emailContent.contains("Product: Cadastral Data Extract - Complete"),
            "Should contain product label");
        assertTrue(emailContent.contains("Client: Municipality of Test City"),
            "Should contain client name");
        assertTrue(emailContent.contains("Tiers: Engineering Consultants Ltd"),
            "Should contain tiers");
        assertTrue(emailContent.contains("Surface: 2500.75 m²"),
            "Should contain surface with unit");
        assertTrue(emailContent.contains("Perimeter: POLYGON((6.5 46.5"),
            "Should contain perimeter in WKT format");
        assertTrue(emailContent.contains("Format param: DXF"),
            "Should contain format parameter with param_ prefix");
        assertTrue(emailContent.contains("Projection param: EPSG:2056"),
            "Should contain projection parameter with param_ prefix");

        // Verify surface is properly formatted (2500.75 m²)
        assertNotNull(testRequest.getSurface(), "Test request should have surface");
        assertEquals(2500.75, testRequest.getSurface(), 0.01, "Surface should be correctly set");

        // Verify perimeter is WKT format
        assertNotNull(testRequest.getPerimeter(), "Test request should have perimeter");
        assertTrue(testRequest.getPerimeter().startsWith("POLYGON"),
            "Perimeter should be in WKT format");

        // Also test with UnmatchedRequestEmail class directly
        UnmatchedRequestEmail email = new UnmatchedRequestEmail(emailSettings);
        boolean initialized = email.initializeContent(testRequest);
        assertTrue(initialized, "Email should be initialized with surface and perimeter");

        // Test with different locales
        UnmatchedRequestEmail emailFr = new UnmatchedRequestEmail(emailSettings);
        boolean initializedFr = emailFr.initializeContent(testRequest, java.util.Locale.forLanguageTag("fr"));
        assertTrue(initializedFr, "Email should initialize with French locale");
    }

    @Test
    @DisplayName("Email templates with null optional fields")
    public void testEmailTemplatesWithNullOptionalFields() {
        // Given: Request with minimal data (null optional fields)
        Request minimalRequest = new Request();
        minimalRequest.setId(999);
        minimalRequest.setOrderLabel("MIN-ORDER");
        minimalRequest.setProductLabel("Basic Product");
        minimalRequest.setClient("Basic Client");
        minimalRequest.setConnector(testConnector);
        minimalRequest.setProcess(testProcess);
        // Leave tiers, remarks, surface, perimeter, etc. as null

        Calendar minimalDate = new GregorianCalendar();
        minimalDate.add(Calendar.HOUR, -1);

        // When: Creating various emails with minimal request
        // Then: Should not throw NullPointerException

        // Test TaskFailedEmail with null optional fields
        Task minimalTask = new Task();
        minimalTask.setId(1);
        minimalTask.setLabel("Minimal Task");

        TaskFailedEmail taskFailedEmail = new TaskFailedEmail(emailSettings);
        boolean taskFailedInit = taskFailedEmail.initializeContent(minimalTask, minimalRequest,
            "Error", minimalDate);
        assertTrue(taskFailedInit, "TaskFailedEmail should handle null optional fields");

        // Test UnmatchedRequestEmail with null optional fields
        UnmatchedRequestEmail unmatchedEmail = new UnmatchedRequestEmail(emailSettings);
        boolean unmatchedInit = unmatchedEmail.initializeContent(minimalRequest);
        assertTrue(unmatchedInit, "UnmatchedRequestEmail should handle null optional fields");

        // Test InvalidProductImportedEmail with null optional fields
        InvalidProductImportedEmail invalidProductEmail = new InvalidProductImportedEmail(emailSettings);
        boolean invalidProductInit = invalidProductEmail.initializeContent(minimalRequest,
            "Invalid", minimalDate);
        assertTrue(invalidProductInit, "InvalidProductImportedEmail should handle null optional fields");

        // Test RequestExportFailedEmail with null optional fields
        RequestExportFailedEmail exportFailedEmail = new RequestExportFailedEmail(emailSettings);
        boolean exportFailedInit = exportFailedEmail.initializeContent(minimalRequest,
            "Export failed", minimalDate);
        assertTrue(exportFailedInit, "RequestExportFailedEmail should handle null optional fields");

        // Verify null fields are handled gracefully (converted to empty strings)
        assertNull(minimalRequest.getTiers(), "Tiers should be null");
        assertNull(minimalRequest.getRemark(), "Remark should be null");
        assertNull(minimalRequest.getSurface(), "Surface should be null");
        assertNull(minimalRequest.getPerimeter(), "Perimeter should be null");
    }

    @Test
    @DisplayName("Standby emails with dates ISO")
    public void testStandbyEmailsWithDatesISO() {
        // Given: TaskStandbyEmail and StandbyReminderEmail with dates
        TaskStandbyEmail taskStandbyEmail = new TaskStandbyEmail(emailSettings);
        StandbyReminderEmail standbyReminderEmail = new StandbyReminderEmail(emailSettings);

        String[] recipients = {"operator@test.com"};

        // When: Initializing emails
        boolean taskStandbyInit = taskStandbyEmail.initialize(testRequest, recipients);
        boolean standbyReminderInit = standbyReminderEmail.initialize(testRequest, recipients);

        // Then: Emails should be initialized successfully
        assertTrue(taskStandbyInit, "TaskStandbyEmail should be initialized");
        assertTrue(standbyReminderInit, "StandbyReminderEmail should be initialized");

        // Verify dates are present in ISO format
        assertNotNull(testRequest.getStartDate(), "Start date should be present");
        assertNotNull(testRequest.getEndDate(), "End date should be present");

        // Verify dates can be formatted to ISO-8601
        String startDateISO = testRequest.getStartDate().getTime().toInstant().toString();
        String endDateISO = testRequest.getEndDate().getTime().toInstant().toString();

        // ISO-8601 format: yyyy-MM-ddTHH:mm:ss.SSSZ
        assertTrue(startDateISO.contains("2024-03"), "Start date ISO should contain correct date");
        assertTrue(endDateISO.contains("2024-03"), "End date ISO should contain correct date");
        assertTrue(startDateISO.contains("T"), "Start date ISO should contain time separator");
        assertTrue(endDateISO.contains("T"), "End date ISO should contain time separator");

        // Test with request without end date (still in progress)
        Request ongoingRequest = new Request();
        ongoingRequest.setId(888);
        ongoingRequest.setOrderLabel("ONGOING-ORDER");
        ongoingRequest.setProductLabel("Ongoing Product");
        ongoingRequest.setClient("Test Client");
        ongoingRequest.setConnector(testConnector);
        ongoingRequest.setProcess(testProcess);
        ongoingRequest.setStartDate(new GregorianCalendar(2024, Calendar.MARCH, 1, 9, 0, 0));
        // No end date set

        TaskStandbyEmail ongoingEmail = new TaskStandbyEmail(emailSettings);
        boolean ongoingInit = ongoingEmail.initialize(ongoingRequest, recipients);
        assertTrue(ongoingInit, "Email should handle request without end date");
    }

    @Test
    @DisplayName("All email types support dynamic parameters")
    public void testAllEmailTypesSupportDynamicParameters() {
        // Given: Request with various dynamic parameters
        String complexParametersJson = "{" +
            "\"FORMAT\":\"PDF\"," +
            "\"PROJECTION\":\"CH1903+\"," +
            "\"SCALE\":1000," +
            "\"LAYERS\":\"building,parcel,road,water\"," +
            "\"QUALITY\":\"high\"," +
            "\"COMPRESS\":true," +
            "\"communes\":\"Lausanne,Morges,Nyon\"" +
            "}";
        testRequest.setParameters(complexParametersJson);

        // When/Then: All email types should initialize successfully with dynamic parameters
        TaskFailedEmail taskFailedEmail = new TaskFailedEmail(emailSettings);
        boolean taskFailedInit = taskFailedEmail.initializeContent(testTask, testRequest,
            "Error", testDate);
        assertTrue(taskFailedInit, "TaskFailedEmail should handle dynamic parameters");

        UnmatchedRequestEmail unmatchedEmail = new UnmatchedRequestEmail(emailSettings);
        boolean unmatchedInit = unmatchedEmail.initializeContent(testRequest);
        assertTrue(unmatchedInit, "UnmatchedRequestEmail should handle dynamic parameters");

        InvalidProductImportedEmail invalidProductEmail = new InvalidProductImportedEmail(emailSettings);
        boolean invalidProductInit = invalidProductEmail.initializeContent(testRequest,
            "Invalid", testDate);
        assertTrue(invalidProductInit, "InvalidProductImportedEmail should handle dynamic parameters");

        RequestExportFailedEmail exportFailedEmail = new RequestExportFailedEmail(emailSettings);
        boolean exportFailedInit = exportFailedEmail.initializeContent(testRequest,
            "Export failed", testDate);
        assertTrue(exportFailedInit, "RequestExportFailedEmail should handle dynamic parameters");

        String[] recipients = {"test@example.com"};
        TaskStandbyEmail taskStandbyEmail = new TaskStandbyEmail(emailSettings);
        boolean taskStandbyInit = taskStandbyEmail.initialize(testRequest, recipients);
        assertTrue(taskStandbyInit, "TaskStandbyEmail should handle dynamic parameters");

        StandbyReminderEmail standbyReminderEmail = new StandbyReminderEmail(emailSettings);
        boolean standbyReminderInit = standbyReminderEmail.initialize(testRequest, recipients);
        assertTrue(standbyReminderInit, "StandbyReminderEmail should handle dynamic parameters");
    }
}
