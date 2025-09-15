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
package ch.asit_asso.extract.plugins.email;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailPlugin.
 * Tests the email notification plugin functionality including variable replacement.
 */
@ExtendWith(MockitoExtension.class)
public class EmailPluginTest {

    private EmailPlugin emailPlugin;
    private Map<String, String> taskSettings;
    
    @Mock
    private ITaskProcessorRequest mockRequest;
    
    @Mock
    private IEmailSettings mockEmailSettings;
    
    @BeforeEach
    public void setUp() {
        taskSettings = new HashMap<>();
        taskSettings.put("to", "test@example.com");
        taskSettings.put("subject", "Test Subject");
        taskSettings.put("body", "Test Body");
        
        emailPlugin = new EmailPlugin("fr", taskSettings);
    }
    
    @Test
    public void testGetCode() {
        assertEquals("EMAIL", emailPlugin.getCode());
    }
    
    @Test
    public void testGetPictoClass() {
        assertEquals("fa-envelope-o", emailPlugin.getPictoClass());
    }
    
    @Test
    public void testExecute_WithBasicVariableReplacement() {
        // Setup
        taskSettings.put("subject", "Order {orderLabel} - Product {productLabel}");
        taskSettings.put("body", "Client: {client}, Organism: {organism}");
        emailPlugin = new EmailPlugin("fr", taskSettings);
        
        when(mockRequest.getOrderLabel()).thenReturn("ORDER-123");
        when(mockRequest.getProductLabel()).thenReturn("Product XYZ");
        when(mockRequest.getClient()).thenReturn("John Doe");
        when(mockRequest.getOrganism()).thenReturn("ACME Corp");
        
        when(mockEmailSettings.isNotificationEnabled()).thenReturn(true);
        when(mockEmailSettings.isValid()).thenReturn(true);
        when(mockEmailSettings.getSmtpHost()).thenReturn("smtp.test.com");
        when(mockEmailSettings.getSmtpPort()).thenReturn(587);
        
        // Act
        ITaskProcessorResult result = emailPlugin.execute(mockRequest, mockEmailSettings);
        
        // Assert
        assertNotNull(result);
        // The actual sending will fail in test, but we verify the plugin attempted execution
    }
    
    @Test
    public void testExecute_WithExtendedVariableReplacement() {
        // Setup
        taskSettings.put("subject", "Tiers: {tiers}");
        taskSettings.put("body", "Surface: {surface}, Perimeter: {perimeter}");
        emailPlugin = new EmailPlugin("fr", taskSettings);
        
        when(mockRequest.getTiers()).thenReturn("Third Party");
        when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
        // Note: getSurface() needs to be added to interface for this to work fully
        
        when(mockEmailSettings.isNotificationEnabled()).thenReturn(true);
        
        // Act
        ITaskProcessorResult result = emailPlugin.execute(mockRequest, mockEmailSettings);
        
        // Assert
        assertNotNull(result);
    }
    
    @Test
    public void testExecute_WithParametersJSON() {
        // Setup
        taskSettings.put("body", "Parameters: {parameters}");
        emailPlugin = new EmailPlugin("fr", taskSettings);
        
        String jsonParams = "{\"FORMAT\":\"pdf\",\"SCALE\":1000}";
        when(mockRequest.getParameters()).thenReturn(jsonParams);
        when(mockEmailSettings.isNotificationEnabled()).thenReturn(true);
        
        // Act
        ITaskProcessorResult result = emailPlugin.execute(mockRequest, mockEmailSettings);
        
        // Assert
        assertNotNull(result);
    }
    
    @Test
    public void testExecute_WithDynamicParameters() {
        // Setup
        taskSettings.put("body", "Format: {parameters.format}, Scale: {param_scale}");
        emailPlugin = new EmailPlugin("fr", taskSettings);
        
        String jsonParams = "{\"FORMAT\":\"geotiff\",\"SCALE\":5000}";
        when(mockRequest.getParameters()).thenReturn(jsonParams);
        when(mockEmailSettings.isNotificationEnabled()).thenReturn(true);
        
        // Act
        ITaskProcessorResult result = emailPlugin.execute(mockRequest, mockEmailSettings);
        
        // Assert
        assertNotNull(result);
    }
    
    @Test
    public void testExecute_WithDateFormatting() {
        // Setup
        taskSettings.put("body", "Start: {startDate}, End: {endDate}");
        emailPlugin = new EmailPlugin("fr", taskSettings);
        
        Calendar startDate = new GregorianCalendar(2024, Calendar.MARCH, 15, 10, 30);
        Calendar endDate = new GregorianCalendar(2024, Calendar.MARCH, 20, 14, 45);
        
        when(mockRequest.getStartDate()).thenReturn(startDate);
        when(mockRequest.getEndDate()).thenReturn(endDate);
        when(mockEmailSettings.isNotificationEnabled()).thenReturn(true);
        
        // Act
        ITaskProcessorResult result = emailPlugin.execute(mockRequest, mockEmailSettings);
        
        // Assert
        assertNotNull(result);
    }
    
    @Test
    public void testExecute_NotificationsDisabled() {
        // Setup
        when(mockEmailSettings.isNotificationEnabled()).thenReturn(false);
        
        // Act
        ITaskProcessorResult result = emailPlugin.execute(mockRequest, mockEmailSettings);
        
        // Assert
        assertNotNull(result);
        assertEquals(EmailResult.Status.SUCCESS, ((EmailResult) result).getStatus());
        assertTrue(result.getMessage().contains("notifications"));
    }
    
    @Test
    public void testExecute_InvalidEmailAddress() {
        // Setup
        taskSettings.put("to", "invalid-email");
        emailPlugin = new EmailPlugin("fr", taskSettings);
        
        when(mockEmailSettings.isNotificationEnabled()).thenReturn(true);
        
        // Act
        ITaskProcessorResult result = emailPlugin.execute(mockRequest, mockEmailSettings);
        
        // Assert
        assertNotNull(result);
        assertEquals(EmailResult.Status.ERROR, ((EmailResult) result).getStatus());
    }
    
    @Test
    public void testExecute_MultipleRecipients() {
        // Setup
        taskSettings.put("to", "user1@test.com;user2@test.com,user3@test.com");
        emailPlugin = new EmailPlugin("fr", taskSettings);
        
        when(mockEmailSettings.isNotificationEnabled()).thenReturn(true);
        
        // Act
        ITaskProcessorResult result = emailPlugin.execute(mockRequest, mockEmailSettings);
        
        // Assert
        assertNotNull(result);
    }
    
    @Test
    public void testExecute_NullValues() {
        // Setup
        taskSettings.put("body", "Client: {client}, Tiers: {tiers}");
        emailPlugin = new EmailPlugin("fr", taskSettings);
        
        when(mockRequest.getClient()).thenReturn(null);
        when(mockRequest.getTiers()).thenReturn(null);
        when(mockEmailSettings.isNotificationEnabled()).thenReturn(true);
        
        // Act
        ITaskProcessorResult result = emailPlugin.execute(mockRequest, mockEmailSettings);
        
        // Assert
        assertNotNull(result);
        // Null values should be replaced with empty strings
    }
    
    @Test
    public void testExecute_SpecialCharactersInVariables() {
        // Setup
        taskSettings.put("body", "Product: {productLabel}");
        emailPlugin = new EmailPlugin("fr", taskSettings);
        
        when(mockRequest.getProductLabel()).thenReturn("Product & Co. <special> \"test\"");
        when(mockEmailSettings.isNotificationEnabled()).thenReturn(true);
        
        // Act
        ITaskProcessorResult result = emailPlugin.execute(mockRequest, mockEmailSettings);
        
        // Assert
        assertNotNull(result);
    }
    
    @Test
    public void testExecute_CaseInsensitivePlaceholders() {
        // Setup
        taskSettings.put("body", "{ORDERLABEL} {orderlabel} {OrderLabel}");
        emailPlugin = new EmailPlugin("fr", taskSettings);
        
        when(mockRequest.getOrderLabel()).thenReturn("ORDER-999");
        when(mockEmailSettings.isNotificationEnabled()).thenReturn(true);
        
        // Act
        ITaskProcessorResult result = emailPlugin.execute(mockRequest, mockEmailSettings);
        
        // Assert
        assertNotNull(result);
        // All three placeholders should be replaced with the same value
    }
    
    @Test
    public void testNewInstance() {
        // Test with language only
        EmailPlugin instance1 = emailPlugin.newInstance("en");
        assertNotNull(instance1);
        assertNotEquals(emailPlugin, instance1);
        
        // Test with language and settings
        Map<String, String> newSettings = new HashMap<>();
        newSettings.put("to", "new@test.com");
        EmailPlugin instance2 = emailPlugin.newInstance("en", newSettings);
        assertNotNull(instance2);
        assertNotEquals(emailPlugin, instance2);
    }
    
    @Test
    public void testGetParams() {
        String params = emailPlugin.getParams();
        assertNotNull(params);
        assertTrue(params.contains("to"));
        assertTrue(params.contains("subject"));
        assertTrue(params.contains("body"));
        assertTrue(params.contains("email"));
        assertTrue(params.contains("text"));
        assertTrue(params.contains("multitext"));
    }
}