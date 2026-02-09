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
package ch.asit_asso.extract.plugins.validation;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationRequest
 */
class ValidationRequestTest {

    private ValidationRequest request;

    @BeforeEach
    void setUp() {
        request = new ValidationRequest();
    }

    @Test
    @DisplayName("New request has null default values")
    void testNewRequestHasNullDefaults() {
        ValidationRequest newRequest = new ValidationRequest();
        assertEquals(0, newRequest.getId());
        assertNull(newRequest.getFolderIn());
        assertNull(newRequest.getFolderOut());
        assertNull(newRequest.getClient());
        assertNull(newRequest.getClientGuid());
        assertNull(newRequest.getOrderGuid());
        assertNull(newRequest.getOrderLabel());
        assertNull(newRequest.getOrganism());
        assertNull(newRequest.getOrganismGuid());
        assertNull(newRequest.getParameters());
        assertNull(newRequest.getPerimeter());
        assertNull(newRequest.getProductGuid());
        assertNull(newRequest.getProductLabel());
        assertNull(newRequest.getTiers());
        assertNull(newRequest.getRemark());
        assertNull(newRequest.getStatus());
        assertNull(newRequest.getStartDate());
        assertNull(newRequest.getEndDate());
        assertNull(newRequest.getSurface());
        assertFalse(newRequest.isRejected());
    }

    @Test
    @DisplayName("setId and getId work correctly")
    void testSetAndGetId() {
        request.setId(123);
        assertEquals(123, request.getId());

        request.setId(0);
        assertEquals(0, request.getId());

        request.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, request.getId());
    }

    @Test
    @DisplayName("setFolderIn and getFolderIn work correctly")
    void testSetAndGetFolderIn() {
        request.setFolderIn("/data/input/request123");
        assertEquals("/data/input/request123", request.getFolderIn());

        request.setFolderIn(null);
        assertNull(request.getFolderIn());

        request.setFolderIn("");
        assertEquals("", request.getFolderIn());
    }

    @Test
    @DisplayName("setFolderOut and getFolderOut work correctly")
    void testSetAndGetFolderOut() {
        request.setFolderOut("/data/output/request123");
        assertEquals("/data/output/request123", request.getFolderOut());

        request.setFolderOut(null);
        assertNull(request.getFolderOut());

        request.setFolderOut("");
        assertEquals("", request.getFolderOut());
    }

    @Test
    @DisplayName("setClient and getClient work correctly")
    void testSetAndGetClient() {
        request.setClient("John Doe");
        assertEquals("John Doe", request.getClient());

        request.setClient(null);
        assertNull(request.getClient());

        request.setClient("");
        assertEquals("", request.getClient());
    }

    @Test
    @DisplayName("setClientGuid and getClientGuid work correctly")
    void testSetAndGetClientGuid() {
        request.setClientGuid("client-guid-abc123");
        assertEquals("client-guid-abc123", request.getClientGuid());

        request.setClientGuid(null);
        assertNull(request.getClientGuid());
    }

    @Test
    @DisplayName("setOrderGuid and getOrderGuid work correctly")
    void testSetAndGetOrderGuid() {
        request.setOrderGuid("order-guid-xyz789");
        assertEquals("order-guid-xyz789", request.getOrderGuid());

        request.setOrderGuid(null);
        assertNull(request.getOrderGuid());
    }

    @Test
    @DisplayName("setOrderLabel and getOrderLabel work correctly")
    void testSetAndGetOrderLabel() {
        request.setOrderLabel("Extraction Order #456");
        assertEquals("Extraction Order #456", request.getOrderLabel());

        request.setOrderLabel(null);
        assertNull(request.getOrderLabel());
    }

    @Test
    @DisplayName("setOrganism and getOrganism work correctly")
    void testSetAndGetOrganism() {
        request.setOrganism("ACME Corporation");
        assertEquals("ACME Corporation", request.getOrganism());

        request.setOrganism(null);
        assertNull(request.getOrganism());
    }

    @Test
    @DisplayName("setOrganismGuid and getOrganismGuid work correctly")
    void testSetAndGetOrganismGuid() {
        request.setOrganismGuid("org-guid-def456");
        assertEquals("org-guid-def456", request.getOrganismGuid());

        request.setOrganismGuid(null);
        assertNull(request.getOrganismGuid());
    }

    @Test
    @DisplayName("setParameters and getParameters work correctly")
    void testSetAndGetParameters() {
        String jsonParams = "{\"format\":\"geojson\",\"crs\":\"EPSG:2056\"}";
        request.setParameters(jsonParams);
        assertEquals(jsonParams, request.getParameters());

        request.setParameters(null);
        assertNull(request.getParameters());
    }

    @Test
    @DisplayName("setPerimeter and getPerimeter work correctly")
    void testSetAndGetPerimeter() {
        String wktPerimeter = "POLYGON((6.1 46.2, 6.2 46.2, 6.2 46.3, 6.1 46.3, 6.1 46.2))";
        request.setPerimeter(wktPerimeter);
        assertEquals(wktPerimeter, request.getPerimeter());

        request.setPerimeter(null);
        assertNull(request.getPerimeter());
    }

    @Test
    @DisplayName("setProductGuid and getProductGuid work correctly")
    void testSetAndGetProductGuid() {
        request.setProductGuid("product-guid-ghi789");
        assertEquals("product-guid-ghi789", request.getProductGuid());

        request.setProductGuid(null);
        assertNull(request.getProductGuid());
    }

    @Test
    @DisplayName("setProductLabel and getProductLabel work correctly")
    void testSetAndGetProductLabel() {
        request.setProductLabel("Cadastral Data 2024");
        assertEquals("Cadastral Data 2024", request.getProductLabel());

        request.setProductLabel(null);
        assertNull(request.getProductLabel());
    }

    @Test
    @DisplayName("setTiers and getTiers work correctly")
    void testSetAndGetTiers() {
        request.setTiers("Third Party Name");
        assertEquals("Third Party Name", request.getTiers());

        request.setTiers(null);
        assertNull(request.getTiers());
    }

    @Test
    @DisplayName("setRemark and getRemark work correctly")
    void testSetAndGetRemark() {
        request.setRemark("Please process urgently");
        assertEquals("Please process urgently", request.getRemark());

        request.setRemark(null);
        assertNull(request.getRemark());
    }

    @Test
    @DisplayName("setRejected and isRejected work correctly")
    void testSetAndIsRejected() {
        request.setRejected(true);
        assertTrue(request.isRejected());

        request.setRejected(false);
        assertFalse(request.isRejected());
    }

    @Test
    @DisplayName("setStatus and getStatus work correctly")
    void testSetAndGetStatus() {
        request.setStatus("TOEXPORT");
        assertEquals("TOEXPORT", request.getStatus());

        request.setStatus("PROCESSING");
        assertEquals("PROCESSING", request.getStatus());

        request.setStatus(null);
        assertNull(request.getStatus());
    }

    @Test
    @DisplayName("setStartDate and getStartDate work correctly")
    void testSetAndGetStartDate() {
        Calendar startDate = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 30, 0);
        request.setStartDate(startDate);
        assertEquals(startDate, request.getStartDate());

        request.setStartDate(null);
        assertNull(request.getStartDate());
    }

    @Test
    @DisplayName("setEndDate and getEndDate work correctly")
    void testSetAndGetEndDate() {
        Calendar endDate = new GregorianCalendar(2024, Calendar.JANUARY, 16, 14, 45, 0);
        request.setEndDate(endDate);
        assertEquals(endDate, request.getEndDate());

        request.setEndDate(null);
        assertNull(request.getEndDate());
    }

    @Test
    @DisplayName("setSurface and getSurface work correctly")
    void testSetAndGetSurface() {
        request.setSurface("1500.50");
        assertEquals("1500.50", request.getSurface());

        request.setSurface(null);
        assertNull(request.getSurface());

        request.setSurface("0");
        assertEquals("0", request.getSurface());
    }

    @Test
    @DisplayName("Request implements ITaskProcessorRequest interface")
    void testImplementsInterface() {
        assertTrue(request instanceof ITaskProcessorRequest);
    }

    @Test
    @DisplayName("Complete request with all fields set")
    void testCompleteRequest() {
        Calendar startDate = new GregorianCalendar(2024, Calendar.MARCH, 10);
        Calendar endDate = new GregorianCalendar(2024, Calendar.MARCH, 11);

        request.setId(999);
        request.setFolderIn("/input/999");
        request.setFolderOut("/output/999");
        request.setClient("Test Client");
        request.setClientGuid("client-999");
        request.setOrderGuid("order-999");
        request.setOrderLabel("Order 999");
        request.setOrganism("Test Org");
        request.setOrganismGuid("org-999");
        request.setParameters("{\"key\":\"value\"}");
        request.setPerimeter("POINT(6.5 46.5)");
        request.setProductGuid("product-999");
        request.setProductLabel("Test Product");
        request.setTiers("Test Tiers");
        request.setRemark("Test remark");
        request.setRejected(false);
        request.setStatus("COMPLETED");
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setSurface("2500.00");

        assertEquals(999, request.getId());
        assertEquals("/input/999", request.getFolderIn());
        assertEquals("/output/999", request.getFolderOut());
        assertEquals("Test Client", request.getClient());
        assertEquals("client-999", request.getClientGuid());
        assertEquals("order-999", request.getOrderGuid());
        assertEquals("Order 999", request.getOrderLabel());
        assertEquals("Test Org", request.getOrganism());
        assertEquals("org-999", request.getOrganismGuid());
        assertEquals("{\"key\":\"value\"}", request.getParameters());
        assertEquals("POINT(6.5 46.5)", request.getPerimeter());
        assertEquals("product-999", request.getProductGuid());
        assertEquals("Test Product", request.getProductLabel());
        assertEquals("Test Tiers", request.getTiers());
        assertEquals("Test remark", request.getRemark());
        assertFalse(request.isRejected());
        assertEquals("COMPLETED", request.getStatus());
        assertEquals(startDate, request.getStartDate());
        assertEquals(endDate, request.getEndDate());
        assertEquals("2500.00", request.getSurface());
    }

    @Test
    @DisplayName("Special characters in text fields")
    void testSpecialCharactersInTextFields() {
        String specialText = "Test with special chars: accentue, umlauts oua, chinese hanzi, symbols <>&";

        request.setClient(specialText);
        assertEquals(specialText, request.getClient());

        request.setRemark(specialText);
        assertEquals(specialText, request.getRemark());

        request.setProductLabel(specialText);
        assertEquals(specialText, request.getProductLabel());
    }

    @Test
    @DisplayName("Long text values")
    void testLongTextValues() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            longText.append("Long text content. ");
        }
        String longString = longText.toString();

        request.setRemark(longString);
        assertEquals(longString, request.getRemark());

        request.setParameters(longString);
        assertEquals(longString, request.getParameters());
    }

    @Test
    @DisplayName("Negative id value")
    void testNegativeIdValue() {
        request.setId(-1);
        assertEquals(-1, request.getId());
    }
}
