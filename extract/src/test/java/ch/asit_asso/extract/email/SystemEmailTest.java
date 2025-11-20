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
package ch.asit_asso.extract.email;

import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for system email notifications.
 * Tests TaskFailedEmail, UnmatchedRequestEmail, and other system emails.
 */
@ExtendWith(MockitoExtension.class)
public class SystemEmailTest {

    private EmailSettings mockEmailSettings;
    private TemplateEngine mockTemplateEngine;
    
    private Request testRequest;
    private Task testTask;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Create mock objects for testing
        SystemParametersRepository mockRepo = Mockito.mock(SystemParametersRepository.class);

        // Create a real TemplateEngine with a StringTemplateResolver for testing
        mockTemplateEngine = new TemplateEngine();
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        mockTemplateEngine.setTemplateResolver(templateResolver);

        // Create mockMessageSource with Answer that returns the message key
        MessageSource mockMessageSource = Mockito.mock(MessageSource.class, invocation -> {
            if ("getMessage".equals(invocation.getMethod().getName())) {
                return invocation.getArgument(0); // Return the message key as the message
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });

        // Mock the repository to return our test email settings
        when(mockRepo.isEmailNotificationEnabled()).thenReturn("true");
        when(mockRepo.getSmtpServer()).thenReturn("smtp.test.com");
        when(mockRepo.getSmtpPort()).thenReturn("587");
        when(mockRepo.getSmtpFromMail()).thenReturn("noreply@extract.test");
        when(mockRepo.getSmtpFromName()).thenReturn("Extract System");
        when(mockRepo.getSmtpUser()).thenReturn(null);
        when(mockRepo.getSmtpPassword()).thenReturn(null);
        when(mockRepo.getSmtpSSL()).thenReturn("NONE");

        // Create EmailSettings with correct constructor parameters
        mockEmailSettings = new EmailSettings(mockRepo, mockTemplateEngine, mockMessageSource, "http://localhost:8080");
        
        // Setup test request with comprehensive data
        testRequest = new Request();
        testRequest.setId(123);
        testRequest.setOrderLabel("ORDER-2024-001");
        testRequest.setProductLabel("Cadastral Data Extract");
        testRequest.setClient("Municipality of Test City");
        testRequest.setClientGuid("client-uuid-123");
        testRequest.setOrganism("Regional Planning Office");
        testRequest.setOrganismGuid("org-uuid-456");
        testRequest.setTiers("Engineering Consultants Ltd");
        testRequest.setSurface(2500.75);
        testRequest.setPerimeter("POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))");
        testRequest.setRemark("Urgent request for construction permit");
        testRequest.setStatus(Request.Status.ONGOING);
        
        // Set dates
        Calendar startDate = new GregorianCalendar(2024, Calendar.MARCH, 1, 9, 0);
        Calendar endDate = new GregorianCalendar(2024, Calendar.MARCH, 5, 17, 30);
        testRequest.setStartDate(startDate);
        testRequest.setEndDate(endDate);
        
        // Set parameters JSON
        String parametersJson = "{\"FORMAT\":\"DXF\",\"PROJECTION\":\"CH1903+\",\"SCALE\":500,\"LAYERS\":\"building,parcel,road\"}";
        testRequest.setParameters(parametersJson);
        
        // Setup connector
        Connector connector = new Connector();
        connector.setName("Test Connector");
        testRequest.setConnector(connector);
        
        // Setup test task
        testTask = new Task();
        testTask.setId(456);
        testTask.setLabel("Data Export Task");
    }
    
    @Test
    public void testTaskFailedEmail_Initialization() {
        // Arrange
        TaskFailedEmail email = new TaskFailedEmail(mockEmailSettings);
        String errorMessage = "Export failed: Invalid coordinate system";
        Calendar failureTime = new GregorianCalendar(2024, Calendar.MARCH, 5, 14, 30);
        String[] recipients = {"admin@test.com", "operator@test.com"};
        
        // Act
        boolean initialized = email.initialize(testTask, testRequest, errorMessage, failureTime, recipients);
        
        // Assert
        assertTrue(initialized);
    }
    
    @Test
    public void testTaskFailedEmail_ModelVariables() {
        // Arrange
        TaskFailedEmail email = new TaskFailedEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Verify the context contains all expected variables
                assertNotNull(context.getVariable("taskName"));
                assertEquals("Data Export Task", context.getVariable("taskName"));
                
                assertNotNull(context.getVariable("errorMessage"));
                
                // Check RequestModelBuilder variables
                assertEquals("ORDER-2024-001", context.getVariable("orderLabel"));
                assertEquals("Cadastral Data Extract", context.getVariable("productLabel"));
                assertEquals("Municipality of Test City", context.getVariable("client"));
                assertEquals("Regional Planning Office", context.getVariable("organism"));
                assertEquals("Engineering Consultants Ltd", context.getVariable("tiers"));
                assertEquals("2500.75", context.getVariable("surface"));
                
                // Check dynamic parameters
                assertNotNull(context.getVariable("parameters"));
                assertNotNull(context.getVariable("parametersJson"));
                
                // Parameters should be accessible with lowercase keys
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> params = (java.util.Map<String, Object>) context.getVariable("parameters");
                assertEquals("DXF", params.get("format"));
                assertEquals("CH1903+", params.get("projection"));
                assertEquals("500", params.get("scale"));
            }
        };
        
        String errorMessage = "Processing failed";
        Calendar failureTime = new GregorianCalendar(2024, Calendar.MARCH, 5, 14, 30);

        // Act
        email.initializeContent(testTask, testRequest, errorMessage, failureTime);
    }
    
    @Test
    public void testUnmatchedRequestEmail_Initialization() {
        // Arrange
        UnmatchedRequestEmail email = new UnmatchedRequestEmail(mockEmailSettings);
        String[] recipients = {"admin@test.com"};
        
        // Act
        boolean initialized = email.initialize(testRequest, recipients);
        
        // Assert
        assertTrue(initialized);
    }
    
    @Test
    public void testUnmatchedRequestEmail_ModelVariables() {
        // Arrange
        UnmatchedRequestEmail email = new UnmatchedRequestEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Verify all RequestModelBuilder variables are present
                assertEquals("Test Connector", context.getVariable("connectorName"));
                assertEquals("ORDER-2024-001", context.getVariable("orderLabel"));
                assertEquals("Cadastral Data Extract", context.getVariable("productLabel"));
                assertEquals("Municipality of Test City", context.getVariable("client"));
                assertEquals("Engineering Consultants Ltd", context.getVariable("tiers"));
                assertNotNull(context.getVariable("parameters"));
                
                // Check parameter variables
                assertNotNull(context.getVariable("param_FORMAT"));
                assertNotNull(context.getVariable("param_format"));
                assertEquals("DXF", context.getVariable("param_FORMAT"));
                assertEquals("DXF", context.getVariable("param_format"));
            }
        };
        
        String[] recipients = {"admin@test.com"};
        
        // Act
        email.initialize(testRequest, recipients);
    }
    
    @Test
    public void testRequestExportFailedEmail_Initialization() {
        // Arrange
        RequestExportFailedEmail email = new RequestExportFailedEmail(mockEmailSettings);
        String errorMessage = "Connection timeout to external server";
        Calendar exportTime = new GregorianCalendar(2024, Calendar.MARCH, 6, 10, 15);
        String[] recipients = {"admin@test.com", "support@test.com"};

        // Act
        boolean initialized = email.initialize(testRequest, errorMessage, exportTime, recipients);

        // Assert
        assertTrue(initialized);
    }

    @Test
    public void testRequestExportFailedEmail_InitializeContent_WithoutLocale() {
        // Arrange
        RequestExportFailedEmail email = new RequestExportFailedEmail(mockEmailSettings);
        String errorMessage = "Connection timeout to external server";
        Calendar exportTime = new GregorianCalendar(2024, Calendar.MARCH, 6, 10, 15);

        // Act
        boolean initialized = email.initializeContent(testRequest, errorMessage, exportTime);

        // Assert
        assertTrue(initialized, "Email should be initialized without locale");
    }

    @Test
    public void testRequestExportFailedEmail_InitializeContent_WithFrenchLocale() {
        // Arrange
        RequestExportFailedEmail email = new RequestExportFailedEmail(mockEmailSettings);
        String errorMessage = "Timeout de connexion au serveur externe";
        Calendar exportTime = new GregorianCalendar(2024, Calendar.MARCH, 6, 10, 15);
        java.util.Locale frenchLocale = java.util.Locale.forLanguageTag("fr");

        // Act
        boolean initialized = email.initializeContent(testRequest, errorMessage, exportTime, frenchLocale);

        // Assert
        assertTrue(initialized, "Email should be initialized with French locale");
    }

    @Test
    public void testRequestExportFailedEmail_InitializeContent_WithGermanLocale() {
        // Arrange
        RequestExportFailedEmail email = new RequestExportFailedEmail(mockEmailSettings);
        String errorMessage = "Verbindungs-Timeout zum externen Server";
        Calendar exportTime = new GregorianCalendar(2024, Calendar.MARCH, 6, 10, 15);
        java.util.Locale germanLocale = java.util.Locale.forLanguageTag("de");

        // Act
        boolean initialized = email.initializeContent(testRequest, errorMessage, exportTime, germanLocale);

        // Assert
        assertTrue(initialized, "Email should be initialized with German locale");
    }

    @Test
    public void testRequestExportFailedEmail_ModelVariablesWithLocale() {
        // Arrange
        java.util.Locale germanLocale = java.util.Locale.forLanguageTag("de");
        RequestExportFailedEmail email = new RequestExportFailedEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Verify locale is set in context
                assertEquals(germanLocale, context.getLocale());

                // Verify RequestModelBuilder variables are present
                assertEquals("Test Connector", context.getVariable("connectorName"));
                assertEquals("ORDER-2024-001", context.getVariable("orderLabel"));
                assertEquals("Cadastral Data Extract", context.getVariable("productLabel"));
                assertNotNull(context.getVariable("errorMessage"));
                assertNotNull(context.getVariable("failureTimeString"));
            }
        };

        String errorMessage = "Test error";
        Calendar exportTime = new GregorianCalendar(2024, Calendar.MARCH, 6, 10, 15);

        // Act
        email.initializeContent(testRequest, errorMessage, exportTime, germanLocale);
    }

    @Test
    public void testConnectorImportFailedEmail_InitializeContent_WithoutLocale() {
        // Arrange
        Connector testConnector = new Connector();
        testConnector.setName("Test Connector");
        ConnectorImportFailedEmail email = new ConnectorImportFailedEmail(mockEmailSettings);
        String errorMessage = "Connection refused by remote server";
        Calendar importTime = new GregorianCalendar(2024, Calendar.MARCH, 10, 8, 30);

        // Act
        boolean initialized = email.initializeContent(testConnector, errorMessage, importTime);

        // Assert
        assertTrue(initialized, "Email should be initialized without locale");
    }

    @Test
    public void testConnectorImportFailedEmail_InitializeContent_WithFrenchLocale() {
        // Arrange
        Connector testConnector = new Connector();
        testConnector.setName("Connecteur de Test");
        ConnectorImportFailedEmail email = new ConnectorImportFailedEmail(mockEmailSettings);
        String errorMessage = "Connexion refusée par le serveur distant";
        Calendar importTime = new GregorianCalendar(2024, Calendar.MARCH, 10, 8, 30);
        java.util.Locale frenchLocale = java.util.Locale.forLanguageTag("fr");

        // Act
        boolean initialized = email.initializeContent(testConnector, errorMessage, importTime, frenchLocale);

        // Assert
        assertTrue(initialized, "Email should be initialized with French locale");
    }

    @Test
    public void testConnectorImportFailedEmail_InitializeContent_WithGermanLocale() {
        // Arrange
        Connector testConnector = new Connector();
        testConnector.setName("Test-Konnektor");
        ConnectorImportFailedEmail email = new ConnectorImportFailedEmail(mockEmailSettings);
        String errorMessage = "Verbindung vom entfernten Server abgelehnt";
        Calendar importTime = new GregorianCalendar(2024, Calendar.MARCH, 10, 8, 30);
        java.util.Locale germanLocale = java.util.Locale.forLanguageTag("de");

        // Act
        boolean initialized = email.initializeContent(testConnector, errorMessage, importTime, germanLocale);

        // Assert
        assertTrue(initialized, "Email should be initialized with German locale");
    }

    @Test
    public void testConnectorImportFailedEmail_ModelVariablesWithLocale() {
        // Arrange
        Connector testConnector = new Connector();
        testConnector.setName("Integration Connector");
        java.util.Locale germanLocale = java.util.Locale.forLanguageTag("de");
        ConnectorImportFailedEmail email = new ConnectorImportFailedEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Verify locale is set in context
                assertEquals(germanLocale, context.getLocale());

                // Verify connector variables are present
                assertEquals("Integration Connector", context.getVariable("connectorName"));
                assertNotNull(context.getVariable("errorMessage"));
                assertNotNull(context.getVariable("failureTimeString"));
                assertNotNull(context.getVariable("dashboardUrl"));
            }
        };

        String errorMessage = "Test import error";
        Calendar importTime = new GregorianCalendar(2024, Calendar.MARCH, 10, 8, 30);

        // Act
        email.initializeContent(testConnector, errorMessage, importTime, germanLocale);
    }

    @Test
    public void testUnmatchedRequestEmail_InitializeContent_WithoutLocale() {
        // Arrange
        UnmatchedRequestEmail email = new UnmatchedRequestEmail(mockEmailSettings);

        // Act
        boolean initialized = email.initializeContent(testRequest);

        // Assert
        assertTrue(initialized, "Email should be initialized without locale");
    }

    @Test
    public void testUnmatchedRequestEmail_InitializeContent_WithFrenchLocale() {
        // Arrange
        UnmatchedRequestEmail email = new UnmatchedRequestEmail(mockEmailSettings);
        java.util.Locale frenchLocale = java.util.Locale.forLanguageTag("fr");

        // Act
        boolean initialized = email.initializeContent(testRequest, frenchLocale);

        // Assert
        assertTrue(initialized, "Email should be initialized with French locale");
    }

    @Test
    public void testUnmatchedRequestEmail_InitializeContent_WithGermanLocale() {
        // Arrange
        UnmatchedRequestEmail email = new UnmatchedRequestEmail(mockEmailSettings);
        java.util.Locale germanLocale = java.util.Locale.forLanguageTag("de");

        // Act
        boolean initialized = email.initializeContent(testRequest, germanLocale);

        // Assert
        assertTrue(initialized, "Email should be initialized with German locale");
    }

    @Test
    public void testUnmatchedRequestEmail_ModelVariablesWithLocale() {
        // Arrange
        java.util.Locale germanLocale = java.util.Locale.forLanguageTag("de");
        UnmatchedRequestEmail email = new UnmatchedRequestEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Verify locale is set in context
                assertEquals(germanLocale, context.getLocale());

                // Verify all RequestModelBuilder variables are present
                assertEquals("Test Connector", context.getVariable("connectorName"));
                assertEquals("ORDER-2024-001", context.getVariable("orderLabel"));
                assertEquals("Cadastral Data Extract", context.getVariable("productLabel"));
                assertEquals("Municipality of Test City", context.getVariable("client"));
                assertEquals("Engineering Consultants Ltd", context.getVariable("tiers"));
                assertNotNull(context.getVariable("parameters"));
                assertNotNull(context.getVariable("dashboardItemUrl"));
            }
        };

        // Act
        email.initializeContent(testRequest, germanLocale);
    }

    @Test
    public void testInvalidProductImportedEmail_InitializeContent_WithoutLocale() {
        // Arrange
        InvalidProductImportedEmail email = new InvalidProductImportedEmail(mockEmailSettings);
        String errorMessage = "Product has no geometry";
        Calendar importTime = new GregorianCalendar(2024, Calendar.MARCH, 12, 11, 45);

        // Act
        boolean initialized = email.initializeContent(testRequest, errorMessage, importTime);

        // Assert
        assertTrue(initialized, "Email should be initialized without locale");
    }

    @Test
    public void testInvalidProductImportedEmail_InitializeContent_WithFrenchLocale() {
        // Arrange
        InvalidProductImportedEmail email = new InvalidProductImportedEmail(mockEmailSettings);
        String errorMessage = "Le produit n'a pas de géométrie";
        Calendar importTime = new GregorianCalendar(2024, Calendar.MARCH, 12, 11, 45);
        java.util.Locale frenchLocale = java.util.Locale.forLanguageTag("fr");

        // Act
        boolean initialized = email.initializeContent(testRequest, errorMessage, importTime, frenchLocale);

        // Assert
        assertTrue(initialized, "Email should be initialized with French locale");
    }

    @Test
    public void testInvalidProductImportedEmail_InitializeContent_WithGermanLocale() {
        // Arrange
        InvalidProductImportedEmail email = new InvalidProductImportedEmail(mockEmailSettings);
        String errorMessage = "Das Produkt hat keine Geometrie";
        Calendar importTime = new GregorianCalendar(2024, Calendar.MARCH, 12, 11, 45);
        java.util.Locale germanLocale = java.util.Locale.forLanguageTag("de");

        // Act
        boolean initialized = email.initializeContent(testRequest, errorMessage, importTime, germanLocale);

        // Assert
        assertTrue(initialized, "Email should be initialized with German locale");
    }

    @Test
    public void testInvalidProductImportedEmail_ModelVariablesWithLocale() {
        // Arrange
        java.util.Locale germanLocale = java.util.Locale.forLanguageTag("de");
        InvalidProductImportedEmail email = new InvalidProductImportedEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Verify locale is set in context
                assertEquals(germanLocale, context.getLocale());

                // Verify RequestModelBuilder integration
                assertEquals("ORDER-2024-001", context.getVariable("orderLabel"));
                assertEquals("Cadastral Data Extract", context.getVariable("productLabel"));
                assertEquals("Test Connector", context.getVariable("connectorName"));
                assertEquals("Engineering Consultants Ltd", context.getVariable("tiers"));
                assertEquals("2500.75", context.getVariable("surface"));
                assertNotNull(context.getVariable("errorMessage"));
                assertNotNull(context.getVariable("failureTimeString"));
                assertNotNull(context.getVariable("dashboardItemUrl"));
            }
        };

        String errorMessage = "Test product error";
        Calendar importTime = new GregorianCalendar(2024, Calendar.MARCH, 12, 11, 45);

        // Act
        email.initializeContent(testRequest, errorMessage, importTime, germanLocale);
    }

    @Test
    public void testInvalidProductImportedEmail_ModelVariables() {
        // Arrange
        InvalidProductImportedEmail email = new InvalidProductImportedEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Verify RequestModelBuilder integration
                assertEquals("ORDER-2024-001", context.getVariable("orderLabel"));
                assertEquals("Cadastral Data Extract", context.getVariable("productLabel"));
                assertEquals("Test Connector", context.getVariable("connectorName"));
                
                // Verify all extended fields
                assertEquals("Engineering Consultants Ltd", context.getVariable("tiers"));
                assertEquals("2500.75", context.getVariable("surface"));
                assertNotNull(context.getVariable("startDate"));
                assertNotNull(context.getVariable("endDate"));
                
                // Check aliases for backward compatibility
                assertEquals("Municipality of Test City", context.getVariable("clientName"));
                assertEquals("Regional Planning Office", context.getVariable("organisationName"));
            }
        };
        
        String errorMessage = "Product configuration invalid";
        Calendar importTime = new GregorianCalendar(2024, Calendar.MARCH, 12, 11, 45);
        String[] recipients = {"admin@test.com"};

        // Act
        email.initialize(testRequest, errorMessage, importTime, recipients);
    }
    
    @Test
    public void testTaskStandbyEmail_ModelVariables() {
        // Arrange
        ch.asit_asso.extract.domain.Process process = new ch.asit_asso.extract.domain.Process();
        process.setName("Standard Export Process");
        testRequest.setProcess(process);

        TaskStandbyEmail email = new TaskStandbyEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Verify process-specific variable
                assertEquals("Standard Export Process", context.getVariable("processName"));
                
                // Verify all RequestModelBuilder variables
                assertEquals("ORDER-2024-001", context.getVariable("orderLabel"));
                assertEquals("Municipality of Test City", context.getVariable("client"));
                assertEquals("Engineering Consultants Ltd", context.getVariable("tiers"));
                
                // Verify dynamic parameters are accessible
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> params = (java.util.Map<String, Object>) context.getVariable("parameters");
                assertNotNull(params);
                assertEquals("dxf", params.get("format")); // Should be lowercase
                assertEquals("ch1903+", params.get("projection")); // Should be lowercase
            }
        };
        
        String[] recipients = {"operator@test.com"};
        
        // Act
        email.initialize(testRequest, recipients);
    }
    
    @Test
    public void testStandbyReminderEmail_WithAllVariables() {
        // Arrange
        ch.asit_asso.extract.domain.Process process = new ch.asit_asso.extract.domain.Process();
        process.setName("Manual Validation Process");
        testRequest.setProcess(process);

        StandbyReminderEmail email = new StandbyReminderEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Verify all variables including new ones from issue #323
                assertEquals("Manual Validation Process", context.getVariable("processName"));
                assertEquals("ORDER-2024-001", context.getVariable("orderLabel"));
                assertEquals("Cadastral Data Extract", context.getVariable("productLabel"));
                assertEquals("Municipality of Test City", context.getVariable("client"));
                assertEquals("Regional Planning Office", context.getVariable("organism"));
                assertEquals("Engineering Consultants Ltd", context.getVariable("tiers"));
                assertEquals("2500.75", context.getVariable("surface"));
                assertEquals("POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))", context.getVariable("perimeter"));
                
                // Check date formatting
                assertNotNull(context.getVariable("startDate"));
                assertNotNull(context.getVariable("startDateISO"));
                String startDateISO = (String) context.getVariable("startDateISO");
                assertTrue(startDateISO.contains("2024-03"));
                
                // Verify status
                assertEquals("ONGOING", context.getVariable("status"));
                
                // Verify parameters JSON and map
                String jsonStr = (String) context.getVariable("parametersJson");
                assertTrue(jsonStr.contains("DXF"));
                assertTrue(jsonStr.contains("CH1903+"));
                
                // Individual parameter variables
                assertEquals("DXF", context.getVariable("param_FORMAT"));
                assertEquals("DXF", context.getVariable("param_format"));
                assertEquals("CH1903+", context.getVariable("param_PROJECTION"));
                assertEquals("CH1903+", context.getVariable("param_projection"));
                assertEquals("500", context.getVariable("param_SCALE"));
                assertEquals("500", context.getVariable("param_scale"));
            }
        };
        
        String[] recipients = {"manager@test.com"};
        
        // Act
        email.initialize(testRequest, recipients);
    }
    
    @Test
    public void testEmailWithNullRequestValues() {
        // Arrange - Create request with minimal data
        Request minimalRequest = new Request();
        minimalRequest.setOrderLabel("MIN-ORDER");
        minimalRequest.setProductLabel("Basic Product");
        
        TaskFailedEmail email = new TaskFailedEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Verify null values are handled as empty strings
                assertEquals("MIN-ORDER", context.getVariable("orderLabel"));
                assertEquals("Basic Product", context.getVariable("productLabel"));
                assertEquals("", context.getVariable("client"));
                assertEquals("", context.getVariable("organism"));
                assertEquals("", context.getVariable("tiers"));
                assertEquals("", context.getVariable("surface"));
                assertEquals("", context.getVariable("perimeter"));
                assertEquals("", context.getVariable("startDate"));
                assertEquals("", context.getVariable("endDate"));
                
                // Parameters should be empty map when null
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> params = (java.util.Map<String, Object>) context.getVariable("parameters");
                assertNotNull(params);
                assertTrue(params.isEmpty());
            }
        };
        
        Connector connector = new Connector();
        connector.setName("Test");
        minimalRequest.setConnector(connector);
        
        Task task = new Task();
        task.setLabel("Test Task");
        Calendar failureTime = new GregorianCalendar(2024, Calendar.MARCH, 5, 14, 30);

        // Act
        email.initializeContent(task, minimalRequest, "Error", failureTime);
    }
    
    @Test
    public void testEmailWithSpecialCharactersInParameters() {
        // Arrange
        String jsonWithSpecialChars = "{\"FILE_NAME\":\"données_2024.pdf\",\"QUERY\":\"SELECT * FROM table WHERE id > 10\"}";
        testRequest.setParameters(jsonWithSpecialChars);
        
        UnmatchedRequestEmail email = new UnmatchedRequestEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Verify special characters are preserved
                assertEquals("données_2024.pdf", context.getVariable("param_file_name"));
                assertEquals("SELECT * FROM table WHERE id > 10", context.getVariable("param_query"));
            }
        };
        
        // Act
        email.initialize(testRequest, new String[]{"test@example.com"});
    }
    
    @Test
    public void testEmailWithInvalidJSON() {
        // Arrange
        testRequest.setParameters("not valid json {");
        
        TaskFailedEmail email = new TaskFailedEmail(mockEmailSettings) {
            protected void setContentFromTemplate(String template, Context context) {
                // Should handle invalid JSON gracefully
                assertEquals("not valid json {", context.getVariable("parametersJson"));
                
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> params = (java.util.Map<String, Object>) context.getVariable("parameters");
                assertNotNull(params);
                assertTrue(params.isEmpty());
            }
        };
        
        Task task = new Task();
        task.setLabel("Task");
        Calendar failureTime = new GregorianCalendar(2024, Calendar.MARCH, 5, 14, 30);

        // Act
        email.initializeContent(task, testRequest, "Error", failureTime);
    }
}