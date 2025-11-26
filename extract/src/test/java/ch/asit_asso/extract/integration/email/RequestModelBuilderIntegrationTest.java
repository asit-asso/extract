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

import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.email.RequestModelBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.context.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RequestModelBuilder (Issue #323).
 * Tests email placeholder functionality with all request fields and dynamic parameters.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
public class RequestModelBuilderIntegrationTest {

    private Request testRequest;
    private Context context;

    @BeforeEach
    public void setUp() {
        testRequest = new Request();
        context = new Context();

        // Setup complete request with all fields
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
        testRequest.setTiersDetails("Contact: John Smith, Phone: +41 21 123 45 67");
        testRequest.setClientDetails("Email: contact@testcity.ch, Department: Urban Planning");
        testRequest.setSurface(2500.75);
        testRequest.setPerimeter("POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))");
        testRequest.setRemark("Urgent request for construction permit - delivery required before March 31st");
        testRequest.setStatus(Request.Status.ONGOING);
        testRequest.setRejected(false);

        // Set dates
        Calendar startDate = new GregorianCalendar(2024, Calendar.MARCH, 1, 9, 0, 0);
        Calendar endDate = new GregorianCalendar(2024, Calendar.MARCH, 15, 17, 30, 0);
        testRequest.setStartDate(startDate);
        testRequest.setEndDate(endDate);
    }

    @Test
    @DisplayName("All basic request fields added to model")
    public void testAllBasicRequestFieldsAddedToModel() {
        // When: Adding request variables to context
        RequestModelBuilder.addRequestVariables(context, testRequest);

        // Then: All basic fields should be present
        assertEquals("ORDER-2024-TEST-001", context.getVariable("orderLabel"),
            "Order label should be present");
        assertEquals("Cadastral Data Extract - Complete", context.getVariable("productLabel"),
            "Product label should be present");
        assertEquals("Municipality of Test City", context.getVariable("client"),
            "Client name should be present");
        assertEquals("Municipality of Test City", context.getVariable("clientName"),
            "Client name alias should be present");
        assertEquals("Regional Planning Office", context.getVariable("organism"),
            "Organism should be present");
        assertEquals("Regional Planning Office", context.getVariable("organisationName"),
            "Organisation name alias should be present");

        // Verify dates are formatted
        assertNotNull(context.getVariable("startDate"), "Start date should be present");
        assertNotNull(context.getVariable("startDateISO"), "Start date ISO should be present");
        String startDateISO = (String) context.getVariable("startDateISO");
        assertTrue(startDateISO.contains("2024-03"), "Start date ISO should contain correct date");

        assertNotNull(context.getVariable("endDate"), "End date should be present");
        assertNotNull(context.getVariable("endDateISO"), "End date ISO should be present");
        String endDateISO = (String) context.getVariable("endDateISO");
        assertTrue(endDateISO.contains("2024-03"), "End date ISO should contain correct date");

        // Verify geographic fields
        assertEquals("2500.75", context.getVariable("surface"),
            "Surface should be present and formatted");
        assertEquals("POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))",
            context.getVariable("perimeter"),
            "Perimeter WKT should be present");

        // Verify status and remarks
        assertEquals("ONGOING", context.getVariable("status"),
            "Status should be present");
        assertEquals("Urgent request for construction permit - delivery required before March 31st",
            context.getVariable("remark"),
            "Remark should be present");
        assertEquals("Urgent request for construction permit - delivery required before March 31st",
            context.getVariable("clientRemark"),
            "Client remark alias should be present");
        assertFalse((Boolean) context.getVariable("rejected"),
            "Rejected flag should be present and false");
    }

    @Test
    @DisplayName("Dynamic parameters parsing from JSON")
    public void testDynamicParametersParsingFromJson() {
        // Given: Request with JSON parameters
        String parametersJson = "{\"format\":\"DXF\",\"projection\":\"EPSG:2056\",\"communes\":\"Lausanne,Morges\",\"SCALE\":500,\"LAYERS\":\"building,parcel,road\"}";
        testRequest.setParameters(parametersJson);

        // When: Adding request variables to context
        RequestModelBuilder.addRequestVariables(context, testRequest);

        // Then: Raw JSON should be available
        assertEquals(parametersJson, context.getVariable("parametersJson"),
            "Parameters JSON should be available as raw string");

        // And: Parameters should be parsed and available as map
        @SuppressWarnings("unchecked")
        Map<String, Object> parametersMap = (Map<String, Object>) context.getVariable("parametersMap");
        assertNotNull(parametersMap, "Parameters map should be created");
        assertEquals("DXF", parametersMap.get("format"), "Format parameter should be in map");
        assertEquals("EPSG:2056", parametersMap.get("projection"), "Projection parameter should be in map");
        assertEquals("Lausanne,Morges", parametersMap.get("communes"), "Communes parameter should be in map");

        // And: Parameters should be available via lowercase keys
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) context.getVariable("parameters");
        assertNotNull(parameters, "Parameters object should be created");
        assertEquals("DXF", parameters.get("format"), "Format should be accessible with lowercase key");
        assertEquals("EPSG:2056", parameters.get("projection"), "Projection should be accessible with lowercase key");
        assertEquals("Lausanne,Morges", parameters.get("communes"), "Communes should be accessible with lowercase key");
        assertEquals("500", parameters.get("scale"), "Scale should be accessible with lowercase key");
        assertEquals("building,parcel,road", parameters.get("layers"), "Layers should be accessible with lowercase key");

        // And: Parameters should be available as individual variables with param_ prefix
        assertEquals("DXF", context.getVariable("param_format"),
            "Format parameter with param_ prefix should be available");
        assertEquals("EPSG:2056", context.getVariable("param_projection"),
            "Projection parameter with param_ prefix should be available");
        assertEquals("Lausanne,Morges", context.getVariable("param_communes"),
            "Communes parameter with param_ prefix should be available");

        // And: Parameters with uppercase keys in JSON should be available with original case
        assertEquals("500", context.getVariable("param_SCALE"),
            "SCALE parameter with original case should be available");
        assertEquals("building,parcel,road", context.getVariable("param_LAYERS"),
            "LAYERS parameter with original case should be available");

        // And: Parameters with uppercase keys should also be available in lowercase
        assertEquals("500", context.getVariable("param_scale"),
            "SCALE parameter should also be available in lowercase");
        assertEquals("building,parcel,road", context.getVariable("param_layers"),
            "LAYERS parameter should also be available in lowercase");
    }

    @Test
    @DisplayName("Dynamic parameters with missing or empty values")
    public void testDynamicParametersWithMissingOrEmptyValues() {
        // Given: Request with JSON containing empty and null values
        String parametersJsonWithEmptyValues = "{\"format\":\"DXF\",\"projection\":\"\",\"communes\":null}";
        testRequest.setParameters(parametersJsonWithEmptyValues);

        // When: Adding request variables to context
        RequestModelBuilder.addRequestVariables(context, testRequest);

        // Then: Should not throw exception
        assertNotNull(context.getVariable("parameters"),
            "Parameters map should be created even with empty/null values");

        // And: Empty/null values should be handled gracefully
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) context.getVariable("parameters");
        assertEquals("DXF", parameters.get("format"), "Non-empty format should be present");
        assertEquals("", parameters.get("projection"), "Empty projection should be empty string");
        assertEquals("", parameters.get("communes"), "Null communes should be empty string");

        // Test accessing non-existent parameter (should return null safely)
        assertNull(parameters.get("nonExistentParameter"),
            "Non-existent parameter should return null");

        // Test with completely empty JSON
        testRequest.setParameters("{}");
        Context emptyContext = new Context();
        RequestModelBuilder.addRequestVariables(emptyContext, testRequest);

        @SuppressWarnings("unchecked")
        Map<String, Object> emptyParameters = (Map<String, Object>) emptyContext.getVariable("parameters");
        assertNotNull(emptyParameters, "Parameters map should be created for empty JSON");
        assertTrue(emptyParameters.isEmpty(), "Parameters map should be empty for empty JSON");

        // Test with null parameters
        testRequest.setParameters(null);
        Context nullContext = new Context();
        RequestModelBuilder.addRequestVariables(nullContext, testRequest);

        assertEquals("{}", nullContext.getVariable("parametersJson"),
            "Null parameters should default to empty JSON object");
        @SuppressWarnings("unchecked")
        Map<String, Object> nullParameters = (Map<String, Object>) nullContext.getVariable("parameters");
        assertNotNull(nullParameters, "Parameters map should be created even when parameters are null");
    }

    @Test
    @DisplayName("GUIDs added to email model")
    public void testGuidsAddedToEmailModel() {
        // When: Adding request variables to context
        RequestModelBuilder.addRequestVariables(context, testRequest);

        // Then: All GUIDs should be present
        assertEquals("client-guid-abc123", context.getVariable("clientGuid"),
            "Client GUID should be present");
        assertEquals("organism-guid-def456", context.getVariable("organismGuid"),
            "Organism GUID should be present");
        assertEquals("tiers-guid-ghi789", context.getVariable("tiersGuid"),
            "Tiers GUID should be present (if applicable)");

        // Verify productLabel is present (note: productGuid is not currently added by RequestModelBuilder)
        assertEquals("Cadastral Data Extract - Complete", context.getVariable("productLabel"),
            "Product label should be present");

        // Verify additional detail fields
        assertEquals("Contact: John Smith, Phone: +41 21 123 45 67",
            context.getVariable("tiersDetails"),
            "Tiers details should be present");

        // Test with request having null GUIDs
        Request requestWithoutGuids = new Request();
        requestWithoutGuids.setOrderLabel("ORDER-NO-GUIDS");
        requestWithoutGuids.setProductLabel("Product Without GUIDs");
        requestWithoutGuids.setClient("Test Client");

        Context contextWithoutGuids = new Context();
        RequestModelBuilder.addRequestVariables(contextWithoutGuids, requestWithoutGuids);

        // GUIDs should be empty strings when null
        assertEquals("", contextWithoutGuids.getVariable("clientGuid"),
            "Null client GUID should be empty string");
        assertEquals("", contextWithoutGuids.getVariable("organismGuid"),
            "Null organism GUID should be empty string");
        assertEquals("", contextWithoutGuids.getVariable("tiersGuid"),
            "Null tiers GUID should be empty string");
    }

    @Test
    @DisplayName("Backward compatibility with alias variables")
    public void testBackwardCompatibilityWithAliasVariables() {
        // When: Adding request variables to context
        RequestModelBuilder.addRequestVariables(context, testRequest);

        // Then: Both original and alias variables should return the same value
        String clientValue = (String) context.getVariable("client");
        String clientNameValue = (String) context.getVariable("clientName");
        assertEquals(clientValue, clientNameValue,
            "client and clientName should return the same value");
        assertEquals("Municipality of Test City", clientValue,
            "Client value should match expected value");

        String organismValue = (String) context.getVariable("organism");
        String organisationNameValue = (String) context.getVariable("organisationName");
        assertEquals(organismValue, organisationNameValue,
            "organism and organisationName should return the same value");
        assertEquals("Regional Planning Office", organismValue,
            "Organism value should match expected value");

        String remarkValue = (String) context.getVariable("remark");
        String clientRemarkValue = (String) context.getVariable("clientRemark");
        assertEquals(remarkValue, clientRemarkValue,
            "remark and clientRemark should return the same value");
        assertEquals("Urgent request for construction permit - delivery required before March 31st",
            remarkValue,
            "Remark value should match expected value");

        // Test that old templates using these aliases continue to work
        assertNotNull(context.getVariable("clientName"),
            "Legacy clientName variable should be accessible");
        assertNotNull(context.getVariable("organisationName"),
            "Legacy organisationName variable should be accessible");
        assertNotNull(context.getVariable("clientRemark"),
            "Legacy clientRemark variable should be accessible");
    }

    @Test
    @DisplayName("Context creation convenience method")
    public void testContextCreationConvenienceMethod() {
        // Given: Request with parameters
        String parametersJson = "{\"OUTPUT_FORMAT\":\"PDF\",\"QUALITY\":\"high\"}";
        testRequest.setParameters(parametersJson);

        // When: Creating context with request using convenience method
        Context newContext = RequestModelBuilder.createContextWithRequest(testRequest);

        // Then: Context should be populated with all request variables
        assertNotNull(newContext, "Created context should not be null");
        assertEquals("ORDER-2024-TEST-001", newContext.getVariable("orderLabel"),
            "Order label should be present in created context");
        assertEquals("Municipality of Test City", newContext.getVariable("client"),
            "Client should be present in created context");
        assertEquals("Regional Planning Office", newContext.getVariable("organism"),
            "Organism should be present in created context");

        // And: Parameters should be parsed
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) newContext.getVariable("parameters");
        assertNotNull(params, "Parameters should be parsed");
        assertEquals("PDF", params.get("output_format"),
            "Output format parameter should be accessible");
        assertEquals("high", params.get("quality"),
            "Quality parameter should be accessible");
    }
}
