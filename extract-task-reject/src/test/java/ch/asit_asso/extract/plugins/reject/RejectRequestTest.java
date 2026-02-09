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
package ch.asit_asso.extract.plugins.reject;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RejectRequest
 */
class RejectRequestTest {

    @Mock
    private ITaskProcessorRequest mockOriginalRequest;

    private RejectRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new RejectRequest();
    }

    @Test
    @DisplayName("Default constructor creates empty request")
    void testDefaultConstructor() {
        RejectRequest newRequest = new RejectRequest();
        assertNotNull(newRequest);
        assertEquals(0, newRequest.getId());
        assertNull(newRequest.getClient());
        assertNull(newRequest.getOrderGuid());
        assertFalse(newRequest.isRejected());
    }

    @Test
    @DisplayName("Copy constructor copies all fields from original request")
    void testCopyConstructor() {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        when(mockOriginalRequest.getId()).thenReturn(123);
        when(mockOriginalRequest.getClient()).thenReturn("Test Client");
        when(mockOriginalRequest.getClientGuid()).thenReturn("client-guid-456");
        when(mockOriginalRequest.getEndDate()).thenReturn(endDate);
        when(mockOriginalRequest.getFolderIn()).thenReturn("/input/folder");
        when(mockOriginalRequest.getFolderOut()).thenReturn("/output/folder");
        when(mockOriginalRequest.getOrderGuid()).thenReturn("order-guid-789");
        when(mockOriginalRequest.getOrderLabel()).thenReturn("Test Order");
        when(mockOriginalRequest.getOrganism()).thenReturn("Test Organism");
        when(mockOriginalRequest.getOrganismGuid()).thenReturn("organism-guid-012");
        when(mockOriginalRequest.getParameters()).thenReturn("{\"param\":\"value\"}");
        when(mockOriginalRequest.getPerimeter()).thenReturn("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
        when(mockOriginalRequest.getProductGuid()).thenReturn("product-guid-345");
        when(mockOriginalRequest.getProductLabel()).thenReturn("Test Product");
        when(mockOriginalRequest.isRejected()).thenReturn(false);
        when(mockOriginalRequest.getRemark()).thenReturn("Original remark");
        when(mockOriginalRequest.getStartDate()).thenReturn(startDate);
        when(mockOriginalRequest.getStatus()).thenReturn("TOEXPORT");
        when(mockOriginalRequest.getSurface()).thenReturn("1000.5");
        when(mockOriginalRequest.getTiers()).thenReturn("Third Party");

        RejectRequest copiedRequest = new RejectRequest(mockOriginalRequest);

        assertEquals(123, copiedRequest.getId());
        assertEquals("Test Client", copiedRequest.getClient());
        assertEquals("client-guid-456", copiedRequest.getClientGuid());
        assertSame(endDate, copiedRequest.getEndDate());
        assertEquals("/input/folder", copiedRequest.getFolderIn());
        assertEquals("/output/folder", copiedRequest.getFolderOut());
        assertEquals("order-guid-789", copiedRequest.getOrderGuid());
        assertEquals("Test Order", copiedRequest.getOrderLabel());
        assertEquals("Test Organism", copiedRequest.getOrganism());
        assertEquals("organism-guid-012", copiedRequest.getOrganismGuid());
        assertEquals("{\"param\":\"value\"}", copiedRequest.getParameters());
        assertEquals("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", copiedRequest.getPerimeter());
        assertEquals("product-guid-345", copiedRequest.getProductGuid());
        assertEquals("Test Product", copiedRequest.getProductLabel());
        assertFalse(copiedRequest.isRejected());
        assertEquals("Original remark", copiedRequest.getRemark());
        assertSame(startDate, copiedRequest.getStartDate());
        assertEquals("TOEXPORT", copiedRequest.getStatus());
        assertEquals("1000.5", copiedRequest.getSurface());
        assertEquals("Third Party", copiedRequest.getTiers());
    }

    @Test
    @DisplayName("setId and getId work correctly")
    void testSetAndGetId() {
        request.setId(42);
        assertEquals(42, request.getId());

        request.setId(0);
        assertEquals(0, request.getId());

        request.setId(-1);
        assertEquals(-1, request.getId());
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
        request.setClientGuid("client-123");
        assertEquals("client-123", request.getClientGuid());

        request.setClientGuid(null);
        assertNull(request.getClientGuid());
    }

    @Test
    @DisplayName("setOrderGuid and getOrderGuid work correctly")
    void testSetAndGetOrderGuid() {
        request.setOrderGuid("order-456");
        assertEquals("order-456", request.getOrderGuid());

        request.setOrderGuid(null);
        assertNull(request.getOrderGuid());
    }

    @Test
    @DisplayName("setOrderLabel and getOrderLabel work correctly")
    void testSetAndGetOrderLabel() {
        request.setOrderLabel("Test Order Label");
        assertEquals("Test Order Label", request.getOrderLabel());

        request.setOrderLabel(null);
        assertNull(request.getOrderLabel());
    }

    @Test
    @DisplayName("setProductGuid and getProductGuid work correctly")
    void testSetAndGetProductGuid() {
        request.setProductGuid("product-789");
        assertEquals("product-789", request.getProductGuid());

        request.setProductGuid(null);
        assertNull(request.getProductGuid());
    }

    @Test
    @DisplayName("setProductLabel and getProductLabel work correctly")
    void testSetAndGetProductLabel() {
        request.setProductLabel("Test Product Label");
        assertEquals("Test Product Label", request.getProductLabel());

        request.setProductLabel(null);
        assertNull(request.getProductLabel());
    }

    @Test
    @DisplayName("setOrganism and getOrganism work correctly")
    void testSetAndGetOrganism() {
        request.setOrganism("Test Organization");
        assertEquals("Test Organization", request.getOrganism());

        request.setOrganism(null);
        assertNull(request.getOrganism());
    }

    @Test
    @DisplayName("setOrganismGuid and getOrganismGuid work correctly")
    void testSetAndGetOrganismGuid() {
        request.setOrganismGuid("organism-012");
        assertEquals("organism-012", request.getOrganismGuid());

        request.setOrganismGuid(null);
        assertNull(request.getOrganismGuid());
    }

    @Test
    @DisplayName("setFolderIn and getFolderIn work correctly")
    void testSetAndGetFolderIn() {
        request.setFolderIn("/path/to/input");
        assertEquals("/path/to/input", request.getFolderIn());

        request.setFolderIn(null);
        assertNull(request.getFolderIn());
    }

    @Test
    @DisplayName("setFolderOut and getFolderOut work correctly")
    void testSetAndGetFolderOut() {
        request.setFolderOut("/path/to/output");
        assertEquals("/path/to/output", request.getFolderOut());

        request.setFolderOut(null);
        assertNull(request.getFolderOut());
    }

    @Test
    @DisplayName("setParameters and getParameters work correctly")
    void testSetAndGetParameters() {
        String jsonParams = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        request.setParameters(jsonParams);
        assertEquals(jsonParams, request.getParameters());

        request.setParameters(null);
        assertNull(request.getParameters());
    }

    @Test
    @DisplayName("setPerimeter and getPerimeter work correctly")
    void testSetAndGetPerimeter() {
        String wktPerimeter = "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))";
        request.setPerimeter(wktPerimeter);
        assertEquals(wktPerimeter, request.getPerimeter());

        request.setPerimeter(null);
        assertNull(request.getPerimeter());
    }

    @Test
    @DisplayName("setRemark and getRemark work correctly")
    void testSetAndGetRemark() {
        request.setRemark("This is a test remark");
        assertEquals("This is a test remark", request.getRemark());

        request.setRemark(null);
        assertNull(request.getRemark());

        request.setRemark("");
        assertEquals("", request.getRemark());
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
        Calendar date = Calendar.getInstance();
        request.setStartDate(date);
        assertSame(date, request.getStartDate());

        request.setStartDate(null);
        assertNull(request.getStartDate());
    }

    @Test
    @DisplayName("setEndDate and getEndDate work correctly")
    void testSetAndGetEndDate() {
        Calendar date = Calendar.getInstance();
        request.setEndDate(date);
        assertSame(date, request.getEndDate());

        request.setEndDate(null);
        assertNull(request.getEndDate());
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
    @DisplayName("setSurface and getSurface work correctly")
    void testSetAndGetSurface() {
        request.setSurface("12345.67");
        assertEquals("12345.67", request.getSurface());

        request.setSurface(null);
        assertNull(request.getSurface());
    }

    @Test
    @DisplayName("Request implements ITaskProcessorRequest interface")
    void testImplementsInterface() {
        assertTrue(request instanceof ITaskProcessorRequest);
    }

    @Test
    @DisplayName("Copy constructor with null values handles gracefully")
    void testCopyConstructorWithNullValues() {
        when(mockOriginalRequest.getId()).thenReturn(0);
        when(mockOriginalRequest.getClient()).thenReturn(null);
        when(mockOriginalRequest.getClientGuid()).thenReturn(null);
        when(mockOriginalRequest.getEndDate()).thenReturn(null);
        when(mockOriginalRequest.getFolderIn()).thenReturn(null);
        when(mockOriginalRequest.getFolderOut()).thenReturn(null);
        when(mockOriginalRequest.getOrderGuid()).thenReturn(null);
        when(mockOriginalRequest.getOrderLabel()).thenReturn(null);
        when(mockOriginalRequest.getOrganism()).thenReturn(null);
        when(mockOriginalRequest.getOrganismGuid()).thenReturn(null);
        when(mockOriginalRequest.getParameters()).thenReturn(null);
        when(mockOriginalRequest.getPerimeter()).thenReturn(null);
        when(mockOriginalRequest.getProductGuid()).thenReturn(null);
        when(mockOriginalRequest.getProductLabel()).thenReturn(null);
        when(mockOriginalRequest.isRejected()).thenReturn(false);
        when(mockOriginalRequest.getRemark()).thenReturn(null);
        when(mockOriginalRequest.getStartDate()).thenReturn(null);
        when(mockOriginalRequest.getStatus()).thenReturn(null);
        when(mockOriginalRequest.getSurface()).thenReturn(null);
        when(mockOriginalRequest.getTiers()).thenReturn(null);

        RejectRequest copiedRequest = new RejectRequest(mockOriginalRequest);

        assertEquals(0, copiedRequest.getId());
        assertNull(copiedRequest.getClient());
        assertNull(copiedRequest.getClientGuid());
        assertNull(copiedRequest.getEndDate());
        assertNull(copiedRequest.getFolderIn());
        assertNull(copiedRequest.getFolderOut());
        assertNull(copiedRequest.getOrderGuid());
        assertNull(copiedRequest.getOrderLabel());
        assertNull(copiedRequest.getOrganism());
        assertNull(copiedRequest.getOrganismGuid());
        assertNull(copiedRequest.getParameters());
        assertNull(copiedRequest.getPerimeter());
        assertNull(copiedRequest.getProductGuid());
        assertNull(copiedRequest.getProductLabel());
        assertFalse(copiedRequest.isRejected());
        assertNull(copiedRequest.getRemark());
        assertNull(copiedRequest.getStartDate());
        assertNull(copiedRequest.getStatus());
        assertNull(copiedRequest.getSurface());
        assertNull(copiedRequest.getTiers());
    }

    @Test
    @DisplayName("Special characters in string fields are preserved")
    void testSpecialCharactersPreserved() {
        String specialChars = "Test with special chars: äöü éèà ñç 漢字 <>&\"'";

        request.setClient(specialChars);
        assertEquals(specialChars, request.getClient());

        request.setRemark(specialChars);
        assertEquals(specialChars, request.getRemark());

        request.setOrderLabel(specialChars);
        assertEquals(specialChars, request.getOrderLabel());
    }
}
