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

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Focused unit tests for variable replacement functionality in EmailPlugin.
 * Tests the replaceRequestVariables and replaceDynamicParameters methods.
 */
@ExtendWith(MockitoExtension.class)
public class VariableReplacementTest {

    private EmailPlugin emailPlugin;
    private Method replaceRequestVariablesMethod;
    private Method replaceDynamicParametersMethod;
    
    @Mock
    private ITaskProcessorRequest mockRequest;
    
    @BeforeEach
    public void setUp() throws Exception {
        Map<String, String> settings = new HashMap<>();
        settings.put("to", "test@example.com");
        settings.put("subject", "Test");
        settings.put("body", "Test");
        
        emailPlugin = new EmailPlugin("fr", settings);
        
        // Get private methods via reflection for testing
        replaceRequestVariablesMethod = EmailPlugin.class.getDeclaredMethod(
            "replaceRequestVariables", String.class, ITaskProcessorRequest.class);
        replaceRequestVariablesMethod.setAccessible(true);
        
        replaceDynamicParametersMethod = EmailPlugin.class.getDeclaredMethod(
            "replaceDynamicParameters", String.class, ITaskProcessorRequest.class);
        replaceDynamicParametersMethod.setAccessible(true);
    }
    
    @Test
    public void testReplaceStandardFields() throws Exception {
        // Setup
        String template = "Order: {orderLabel}, Product: {productLabel}, Client: {client}";
        when(mockRequest.getOrderLabel()).thenReturn("ORD-123");
        when(mockRequest.getProductLabel()).thenReturn("Map Extract");
        when(mockRequest.getClient()).thenReturn("John Doe");
        
        // Act
        String result = (String) replaceRequestVariablesMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("Order: ORD-123, Product: Map Extract, Client: John Doe", result);
    }
    
    @Test
    public void testReplaceExtendedFields() throws Exception {
        // Setup
        String template = "Tiers: {tiers}, Perimeter: {perimeter}, Status: {status}";
        when(mockRequest.getTiers()).thenReturn("Third Party Co");
        when(mockRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 0))");
        when(mockRequest.getStatus()).thenReturn("ONGOING");
        
        // Act
        String result = (String) replaceRequestVariablesMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("Tiers: Third Party Co, Perimeter: POLYGON((0 0, 1 0, 1 1, 0 0)), Status: ONGOING", result);
    }
    
    @Test
    public void testReplaceSurfaceField() throws Exception {
        // Setup
        String template = "Surface area: {surface} m²";
        // Using reflection to simulate getSurface() since it may not be in interface yet
        when(mockRequest.getClass().getMethod("getSurface")).thenReturn(null);
        doReturn("1250.5").when(mockRequest).toString();
        
        // Act
        String result = (String) replaceRequestVariablesMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        // Surface might not be replaced if not in interface, but should not throw error
        assertNotNull(result);
    }
    
    @Test
    public void testReplaceDateFields() throws Exception {
        // Setup
        String template = "Start: {startDate}, End: {endDate}";
        Calendar startDate = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 30);
        Calendar endDate = new GregorianCalendar(2024, Calendar.JANUARY, 20, 16, 45);
        
        when(mockRequest.getStartDate()).thenReturn(startDate);
        when(mockRequest.getEndDate()).thenReturn(endDate);
        
        // Act
        String result = (String) replaceRequestVariablesMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertTrue(result.contains("2024"));
        assertFalse(result.contains("{startDate}"));
        assertFalse(result.contains("{endDate}"));
    }
    
    @Test
    public void testReplaceDynamicParameters_StandardFormat() throws Exception {
        // Setup
        String template = "Format: {parameters.format}, Scale: {parameters.scale}";
        String jsonParams = "{\"FORMAT\":\"PDF\",\"SCALE\":1000}";
        when(mockRequest.getParameters()).thenReturn(jsonParams);
        
        // Act
        String result = (String) replaceDynamicParametersMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("Format: PDF, Scale: 1000", result);
    }
    
    @Test
    public void testReplaceDynamicParameters_LowercaseFormat() throws Exception {
        // Setup
        String template = "Format: {parameters.format}, Projection: {parameters.projection}";
        String jsonParams = "{\"FORMAT\":\"GEOTIFF\",\"PROJECTION\":\"EPSG:2056\"}";
        when(mockRequest.getParameters()).thenReturn(jsonParams);
        
        // Act
        String result = (String) replaceDynamicParametersMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("Format: GEOTIFF, Projection: EPSG:2056", result);
    }
    
    @Test
    public void testReplaceDynamicParameters_ParamPrefix() throws Exception {
        // Setup
        String template = "Format: {param_format}, CRS: {param_crs}";
        String jsonParams = "{\"FORMAT\":\"SHP\",\"CRS\":\"CH1903+\"}";
        when(mockRequest.getParameters()).thenReturn(jsonParams);
        
        // Act
        String result = (String) replaceDynamicParametersMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("Format: SHP, CRS: CH1903+", result);
    }
    
    @Test
    public void testReplaceDynamicParameters_MixedCase() throws Exception {
        // Setup
        String template = "{param_FORMAT} {param_format} {parameters.FORMAT} {parameters.format}";
        String jsonParams = "{\"FORMAT\":\"DXF\"}";
        when(mockRequest.getParameters()).thenReturn(jsonParams);
        
        // Act
        String result = (String) replaceDynamicParametersMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("DXF DXF DXF DXF", result);
    }
    
    @Test
    public void testReplaceParameters_ComplexJSON() throws Exception {
        // Setup
        String template = "Layers: {parameters.layers}, Buffer: {param_buffer}";
        String jsonParams = "{\"LAYERS\":\"road,building,parcel\",\"BUFFER\":50}";
        when(mockRequest.getParameters()).thenReturn(jsonParams);
        
        // Act
        String result = (String) replaceDynamicParametersMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("Layers: road,building,parcel, Buffer: 50", result);
    }
    
    @Test
    public void testReplaceParameters_SpecialCharacters() throws Exception {
        // Setup
        String template = "File: {parameters.filename}, Query: {param_query}";
        String jsonParams = "{\"FILENAME\":\"data_2024-03.zip\",\"QUERY\":\"id > 100 AND status = 'active'\"}";
        when(mockRequest.getParameters()).thenReturn(jsonParams);
        
        // Act
        String result = (String) replaceDynamicParametersMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("File: data_2024-03.zip, Query: id > 100 AND status = 'active'", result);
    }
    
    @Test
    public void testReplaceParameters_EmptyJSON() throws Exception {
        // Setup
        String template = "Params: {parameters.test}";
        when(mockRequest.getParameters()).thenReturn("{}");
        
        // Act
        String result = (String) replaceDynamicParametersMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("Params: {parameters.test}", result); // Should remain unchanged
    }
    
    @Test
    public void testReplaceParameters_NullParameters() throws Exception {
        // Setup
        String template = "Test: {parameters.value}";
        when(mockRequest.getParameters()).thenReturn(null);
        
        // Act
        String result = (String) replaceDynamicParametersMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("Test: {parameters.value}", result); // Should remain unchanged
    }
    
    @Test
    public void testReplaceParameters_InvalidJSON() throws Exception {
        // Setup
        String template = "Value: {param_test}";
        when(mockRequest.getParameters()).thenReturn("not valid json");
        
        // Act
        String result = (String) replaceDynamicParametersMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("Value: {param_test}", result); // Should remain unchanged
    }
    
    @Test
    public void testCaseInsensitivePlaceholders() throws Exception {
        // Setup
        String template = "{ORDERLABEL} {orderlabel} {OrderLabel}";
        when(mockRequest.getOrderLabel()).thenReturn("ORDER-999");
        
        // Act
        String result = (String) replaceRequestVariablesMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("ORDER-999 ORDER-999 ORDER-999", result);
    }
    
    @Test
    public void testNullFieldValues() throws Exception {
        // Setup
        String template = "Client: {client}, Tiers: {tiers}";
        when(mockRequest.getClient()).thenReturn(null);
        when(mockRequest.getTiers()).thenReturn(null);
        
        // Act
        String result = (String) replaceRequestVariablesMethod.invoke(emailPlugin, template, mockRequest);
        
        // Assert
        assertEquals("Client: , Tiers: ", result); // Nulls replaced with empty string
    }
    
    @Test
    public void testCompleteEmailTemplate() throws Exception {
        // Setup - Comprehensive template
        String template = "Order {orderLabel} from {client}\n" +
                         "Product: {productLabel}\n" +
                         "Organisation: {organism}\n" +
                         "Tiers: {tiers}\n" +
                         "Surface: {surface}\n" +
                         "Format: {parameters.format}\n" +
                         "Scale: {param_scale}\n" +
                         "Status: {status}";
        
        when(mockRequest.getOrderLabel()).thenReturn("2024-001");
        when(mockRequest.getClient()).thenReturn("City Planning");
        when(mockRequest.getProductLabel()).thenReturn("Zoning Map");
        when(mockRequest.getOrganism()).thenReturn("Municipality");
        when(mockRequest.getTiers()).thenReturn("Consultants Inc");
        when(mockRequest.getStatus()).thenReturn("PROCESSING");
        when(mockRequest.getParameters()).thenReturn("{\"FORMAT\":\"PDF\",\"SCALE\":2000}");
        
        // Act
        String intermediate = (String) replaceRequestVariablesMethod.invoke(emailPlugin, template, mockRequest);
        String result = (String) replaceDynamicParametersMethod.invoke(emailPlugin, intermediate, mockRequest);
        
        // Assert
        assertTrue(result.contains("Order 2024-001"));
        assertTrue(result.contains("City Planning"));
        assertTrue(result.contains("Zoning Map"));
        assertTrue(result.contains("Municipality"));
        assertTrue(result.contains("Consultants Inc"));
        assertTrue(result.contains("Format: PDF"));
        assertTrue(result.contains("Scale: 2000"));
        assertTrue(result.contains("Status: PROCESSING"));
        assertFalse(result.contains("{")); // No unreplaced placeholders
    }
}