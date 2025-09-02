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

import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Request.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RequestModelBuilder utility class.
 * Tests the proper extraction and formatting of request variables for email templates.
 */
public class RequestModelBuilderTest {

    private Request request;
    private Context context;
    
    @BeforeEach
    public void setUp() {
        request = new Request();
        context = new Context();
        
        // Set up basic request data
        request.setOrderLabel("TEST-ORDER-123");
        request.setProductLabel("Product ABC");
        request.setClient("John Doe");
        request.setClientGuid("client-123");
        request.setOrganism("ACME Corp");
        request.setOrganismGuid("org-456");
        request.setTiers("Third Party LLC");
        request.setSurface(150.5);
        request.setPerimeter("POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
        request.setRemark("Test remark");
        request.setStatus(Status.FINISHED);
        
        Calendar startDate = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 30);
        Calendar endDate = new GregorianCalendar(2024, Calendar.JANUARY, 20, 14, 45);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
    }
    
    @Test
    public void testAddRequestVariables_BasicFields() {
        // Act
        RequestModelBuilder.addRequestVariables(context, request);
        
        // Assert basic fields
        assertEquals("TEST-ORDER-123", context.getVariable("orderLabel"));
        assertEquals("Product ABC", context.getVariable("productLabel"));
        assertEquals("John Doe", context.getVariable("client"));
        assertEquals("John Doe", context.getVariable("clientName")); // Alias
        assertEquals("client-123", context.getVariable("clientGuid"));
        assertEquals("ACME Corp", context.getVariable("organism"));
        assertEquals("ACME Corp", context.getVariable("organisationName")); // Alias
        assertEquals("org-456", context.getVariable("organismGuid"));
    }
    
    @Test
    public void testAddRequestVariables_ExtendedFields() {
        // Act
        RequestModelBuilder.addRequestVariables(context, request);
        
        // Assert extended fields
        assertEquals("Third Party LLC", context.getVariable("tiers"));
        assertEquals("150.5", context.getVariable("surface"));
        assertEquals("POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))", context.getVariable("perimeter"));
        assertEquals("Test remark", context.getVariable("remark"));
        assertEquals("Test remark", context.getVariable("clientRemark")); // Alias
        assertEquals("FINISHED", context.getVariable("status"));
        assertFalse((Boolean) context.getVariable("rejected"));
    }
    
    @Test
    public void testAddRequestVariables_DateFields() {
        // Act
        RequestModelBuilder.addRequestVariables(context, request);
        
        // Assert date fields are present and formatted
        assertNotNull(context.getVariable("startDate"));
        assertNotNull(context.getVariable("startDateISO"));
        assertNotNull(context.getVariable("endDate"));
        assertNotNull(context.getVariable("endDateISO"));
        
        // Check that dates contain expected parts
        String startDateStr = (String) context.getVariable("startDate");
        assertTrue(startDateStr.contains("2024"));
        
        String startDateISO = (String) context.getVariable("startDateISO");
        assertTrue(startDateISO.contains("2024-01"));
    }
    
    @Test
    public void testAddRequestVariables_NullFields() {
        // Setup request with null values
        Request nullRequest = new Request();
        nullRequest.setOrderLabel("ORDER-1");
        nullRequest.setProductLabel("Product");
        
        // Act
        RequestModelBuilder.addRequestVariables(context, nullRequest);
        
        // Assert null fields are handled as empty strings
        assertEquals("", context.getVariable("client"));
        assertEquals("", context.getVariable("organism"));
        assertEquals("", context.getVariable("tiers"));
        assertEquals("", context.getVariable("surface"));
        assertEquals("", context.getVariable("perimeter"));
        assertEquals("", context.getVariable("startDate"));
        assertEquals("", context.getVariable("endDate"));
    }
    
    @Test
    public void testAddRequestVariables_DynamicParametersAsJSON() {
        // Setup JSON parameters
        String jsonParams = "{\"FORMAT\":\"pdf\",\"PROJECTION\":\"2056\",\"SCALE\":1000}";
        request.setParameters(jsonParams);
        
        // Act
        RequestModelBuilder.addRequestVariables(context, request);
        
        // Assert raw JSON is available
        assertEquals(jsonParams, context.getVariable("parametersJson"));
        
        // Assert parameters map is created
        @SuppressWarnings("unchecked")
        Map<String, Object> parametersMap = (Map<String, Object>) context.getVariable("parameters");
        assertNotNull(parametersMap);
        assertEquals("pdf", parametersMap.get("format")); // Lowercase key
        assertEquals("2056", parametersMap.get("projection")); // Lowercase key
        assertEquals("1000", parametersMap.get("scale")); // Lowercase key
    }
    
    @Test
    public void testAddRequestVariables_DynamicParametersAsVariables() {
        // Setup JSON parameters
        String jsonParams = "{\"FORMAT\":\"geotiff\",\"CRS\":\"EPSG:4326\",\"Resolution\":10}";
        request.setParameters(jsonParams);
        
        // Act
        RequestModelBuilder.addRequestVariables(context, request);
        
        // Assert individual parameter variables are created
        assertEquals("geotiff", context.getVariable("param_FORMAT")); // Original case
        assertEquals("geotiff", context.getVariable("param_format")); // Lowercase
        assertEquals("EPSG:4326", context.getVariable("param_CRS")); // Original case
        assertEquals("EPSG:4326", context.getVariable("param_crs")); // Lowercase
        assertEquals("10", context.getVariable("param_Resolution")); // Original case
        assertEquals("10", context.getVariable("param_resolution")); // Lowercase
    }
    
    @Test
    public void testAddRequestVariables_InvalidJSON() {
        // Setup invalid JSON
        request.setParameters("not a valid json");
        
        // Act
        RequestModelBuilder.addRequestVariables(context, request);
        
        // Assert graceful handling
        assertEquals("not a valid json", context.getVariable("parametersJson"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> parametersMap = (Map<String, Object>) context.getVariable("parameters");
        assertNotNull(parametersMap);
        assertTrue(parametersMap.isEmpty());
    }
    
    @Test
    public void testAddRequestVariables_EmptyParameters() {
        // Setup empty parameters
        request.setParameters("");
        
        // Act
        RequestModelBuilder.addRequestVariables(context, request);
        
        // Assert
        assertEquals("", context.getVariable("parametersJson"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> parametersMap = (Map<String, Object>) context.getVariable("parameters");
        assertNotNull(parametersMap);
        assertTrue(parametersMap.isEmpty());
    }
    
    @Test
    public void testAddRequestVariables_NullContext() {
        // Act - should not throw exception
        RequestModelBuilder.addRequestVariables(null, request);
        // Assert - method handles null gracefully (logged warning)
    }
    
    @Test
    public void testAddRequestVariables_NullRequest() {
        // Act - should not throw exception
        RequestModelBuilder.addRequestVariables(context, null);
        // Assert - method handles null gracefully (logged warning)
        assertTrue(context.getVariableNames().isEmpty());
    }
    
    @Test
    public void testCreateContextWithRequest() {
        // Setup JSON parameters for comprehensive test
        String jsonParams = "{\"QUALITY\":\"high\",\"COMPRESS\":true}";
        request.setParameters(jsonParams);
        
        // Act
        Context newContext = RequestModelBuilder.createContextWithRequest(request);
        
        // Assert context is created with all variables
        assertNotNull(newContext);
        assertEquals("TEST-ORDER-123", newContext.getVariable("orderLabel"));
        assertEquals("Product ABC", newContext.getVariable("productLabel"));
        assertEquals("Third Party LLC", newContext.getVariable("tiers"));
        
        // Check dynamic parameters
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) newContext.getVariable("parameters");
        assertEquals("high", params.get("quality"));
        assertEquals("true", params.get("compress"));
    }
    
    @Test
    public void testAddRequestVariables_SpecialCharactersInParameters() {
        // Setup parameters with special characters
        String jsonParams = "{\"FILE_NAME\":\"test@file#2024.pdf\",\"DESCRIPTION\":\"Test avec caractères spéciaux: é à ç\"}";
        request.setParameters(jsonParams);
        
        // Act
        RequestModelBuilder.addRequestVariables(context, request);
        
        // Assert special characters are preserved
        assertEquals("test@file#2024.pdf", context.getVariable("param_file_name"));
        assertEquals("Test avec caractères spéciaux: é à ç", context.getVariable("param_description"));
    }
}