package ch.asit_asso.extract.integration.requests;

import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.GregorianCalendar;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for Issue #337: Fix template rendering for cancelled requests without matching rules.
 * This test ensures that the request details page renders correctly even when:
 * - The request has no matching rules (unmatched)
 * - The outputFolderPath is null
 * - The outputFiles array is null or empty
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Tag("integration")
public class CancelledRequestWithoutRulesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private ConnectorsRepository connectorsRepository;

    private Request cancelledRequestWithoutRules;
    private Request normalRequest;
    private Connector testConnector;

    @BeforeEach
    public void setUp() {
        // Create a test connector
        testConnector = new Connector();
        testConnector.setName("Test Connector for #337");
        testConnector.setActive(Boolean.TRUE);
        testConnector = connectorsRepository.save(testConnector);

        // Create a cancelled request without matching rules (unmatched)
        cancelledRequestWithoutRules = new Request();
        cancelledRequestWithoutRules.setProductLabel("Cancelled Request Without Rules");
        cancelledRequestWithoutRules.setOrderLabel("ORDER-337-CANCELLED");
        cancelledRequestWithoutRules.setClient("Test Client #337");
        cancelledRequestWithoutRules.setStatus(Request.Status.UNMATCHED);
        cancelledRequestWithoutRules.setFolderOut(null); // No output folder
        cancelledRequestWithoutRules.setStartDate(new GregorianCalendar());
        cancelledRequestWithoutRules.setRemark("Cancelled: No matching rules found");
        cancelledRequestWithoutRules.setConnector(testConnector);
        cancelledRequestWithoutRules = requestsRepository.save(cancelledRequestWithoutRules);

        // Create a normal request for comparison
        normalRequest = new Request();
        normalRequest.setProductLabel("Normal Request");
        normalRequest.setOrderLabel("ORDER-337-NORMAL");
        normalRequest.setClient("Test Client");
        normalRequest.setStatus(Request.Status.ONGOING);
        normalRequest.setFolderOut("request337/output");
        normalRequest.setStartDate(new GregorianCalendar());
        normalRequest.setConnector(testConnector);
        normalRequest = requestsRepository.save(normalRequest);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should render details page for cancelled request without matching rules")
    public void testDetailsPageForCancelledRequestWithoutRules() throws Exception {
        mockMvc.perform(get("/requests/details/" + cancelledRequestWithoutRules.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("pages/requests/details"))
            .andExpect(model().attributeExists("request"))
            // Verify the page renders without errors
            .andExpect(content().string(containsString("ORDER-337-CANCELLED")))
            .andExpect(content().string(containsString("Cancelled: No matching rules found")))
            // Verify action buttons are present
            .andExpect(xpath("//button[@id='errorRetryMatchingButton']").exists())
            .andExpect(xpath("//button[@id='errorCancelButton']").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should handle null outputFiles without errors")
    public void testNullOutputFilesHandling() throws Exception {
        // Create request with explicitly null conditions
        Request requestWithNulls = new Request();
        requestWithNulls.setProductLabel("Request with Null Files");
        requestWithNulls.setOrderLabel("ORDER-337-NULL");
        requestWithNulls.setClient("Test Client");
        requestWithNulls.setStatus(Request.Status.ERROR);
        requestWithNulls.setFolderOut(null);
        requestWithNulls.setStartDate(new GregorianCalendar());
        requestWithNulls.setConnector(testConnector);
        requestWithNulls.setRemark("Error: Processing failed");
        requestWithNulls = requestsRepository.save(requestWithNulls);

        mockMvc.perform(get("/requests/details/" + requestWithNulls.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("pages/requests/details"))
            // Verify the response panel shows with remark but no file errors
            .andExpect(content().string(containsString("Error: Processing failed")))
            // Verify no NullPointerException indicators
            .andExpect(content().string(not(containsString("NullPointerException"))))
            .andExpect(content().string(not(containsString("Error 500"))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should not show download button when outputFiles is null")
    public void testDownloadButtonNotShownForNullFiles() throws Exception {
        mockMvc.perform(get("/requests/details/" + cancelledRequestWithoutRules.getId()))
            .andExpect(status().isOk())
            // The download button should not be present
            .andExpect(xpath("//button[@id='file-download-button']").doesNotExist());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Normal request should still work correctly")
    public void testNormalRequestStillWorks() throws Exception {
        mockMvc.perform(get("/requests/details/" + normalRequest.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("pages/requests/details"))
            .andExpect(model().attributeExists("request"))
            .andExpect(content().string(containsString("ORDER-337-NORMAL")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin should see all details even with null fields")
    public void testAdminViewWithNullFields() throws Exception {
        mockMvc.perform(get("/requests/details/" + cancelledRequestWithoutRules.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("pages/requests/details"))
            // Admin should have access to all functionality
            .andExpect(model().attributeExists("request"))
            .andExpect(model().attributeExists("isAdmin"));
    }
}
