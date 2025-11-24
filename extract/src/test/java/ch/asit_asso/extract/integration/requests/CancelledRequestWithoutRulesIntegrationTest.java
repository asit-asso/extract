package ch.asit_asso.extract.integration.requests;

import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.integration.TestSecurityConfig;
import ch.asit_asso.extract.integration.WithMockApplicationUser;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import javax.persistence.EntityManager;

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
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CancelledRequestWithoutRulesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private ConnectorsRepository connectorsRepository;

    @Autowired
    private EntityManager entityManager;

    // Test data IDs
    private Integer cancelledRequestId;
    private Integer normalRequestId;

    @BeforeAll
    public void setUpTestData() {
        // Clean up any existing test data
        requestsRepository.deleteAll();
        connectorsRepository.deleteAll();

        // Create a test connector
        Connector testConnector = new Connector();
        testConnector.setName("Test Connector for #337");
        testConnector.setActive(Boolean.TRUE);
        testConnector = connectorsRepository.save(testConnector);

        // Create a cancelled request without matching rules (unmatched)
        Request cancelledRequest = new Request();
        cancelledRequest.setProductLabel("Cancelled Request Without Rules");
        cancelledRequest.setOrderLabel("ORDER-337-CANCELLED");
        cancelledRequest.setClient("Test Client #337");
        cancelledRequest.setStatus(Request.Status.UNMATCHED);
        cancelledRequest.setFolderOut(null); // No output folder
        cancelledRequest.setStartDate(new GregorianCalendar());
        cancelledRequest.setRemark("Cancelled: No matching rules found");
        cancelledRequest.setConnector(testConnector);
        cancelledRequest.setParameters("{}"); // Empty JSON object
        cancelledRequest.setPerimeter("{}"); // Empty JSON object
        cancelledRequest = requestsRepository.save(cancelledRequest);
        cancelledRequestId = cancelledRequest.getId();

        // Create a normal request for comparison
        Request normalRequest = new Request();
        normalRequest.setProductLabel("Normal Request");
        normalRequest.setOrderLabel("ORDER-337-NORMAL");
        normalRequest.setClient("Test Client");
        normalRequest.setStatus(Request.Status.ONGOING);
        normalRequest.setFolderOut("request337/output");
        normalRequest.setStartDate(new GregorianCalendar());
        normalRequest.setConnector(testConnector);
        normalRequest.setParameters("{}"); // Empty JSON object
        normalRequest.setPerimeter("{}"); // Empty JSON object
        normalRequest = requestsRepository.save(normalRequest);
        normalRequestId = normalRequest.getId();
    }

    @Test
    @WithMockApplicationUser
    @DisplayName("Should render details page for cancelled request without matching rules")
    public void testDetailsPageForCancelledRequestWithoutRules() throws Exception {
        mockMvc.perform(get("/requests/" + cancelledRequestId))
            .andDo(result -> {
                if (result.getResponse().getStatus() != 200) {
                    System.err.println("Response status: " + result.getResponse().getStatus());
                    System.err.println("Response content: " + result.getResponse().getContentAsString());
                }
            })
            .andExpect(status().isOk())
            .andExpect(view().name("requests/details"))
            .andExpect(model().attributeExists("request"))
            // Verify the page renders without errors
            .andExpect(content().string(containsString("ORDER-337-CANCELLED")))
            .andExpect(content().string(containsString("Cancelled: No matching rules found")))
            // Verify action buttons are present (using CSS selectors instead of XPath)
            .andExpect(content().string(containsString("id=\"errorRetryMatchingButton\"")))
            .andExpect(content().string(containsString("id=\"errorCancelButton\"")));
    }

    @Test
    @WithMockApplicationUser
    @DisplayName("Should handle null outputFiles without errors")
    public void testNullOutputFilesHandling() throws Exception {
        // Use the cancelled request which has null folder
        mockMvc.perform(get("/requests/" + cancelledRequestId))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/details"))
            // Verify the response panel shows with remark but no file errors
            .andExpect(content().string(containsString("Cancelled: No matching rules found")))
            // Verify no NullPointerException indicators
            .andExpect(content().string(not(containsString("NullPointerException"))))
            .andExpect(content().string(not(containsString("Error 500"))));
    }

    @Test
    @WithMockApplicationUser
    @DisplayName("Should not show download button when outputFiles is null")
    public void testDownloadButtonNotShownForNullFiles() throws Exception {
        mockMvc.perform(get("/requests/" + cancelledRequestId))
            .andExpect(status().isOk())
            // The download button should not be present (check that the ID doesn't exist)
            .andExpect(content().string(not(containsString("id=\"file-download-button\""))));
    }

    @Test
    @WithMockApplicationUser
    @DisplayName("Normal request should still work correctly")
    public void testNormalRequestStillWorks() throws Exception {
        mockMvc.perform(get("/requests/" + normalRequestId))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/details"))
            .andExpect(model().attributeExists("request"))
            .andExpect(content().string(containsString("ORDER-337-NORMAL")));
    }

    @Test
    @WithMockApplicationUser
    @DisplayName("Admin should see all details even with null fields")
    public void testAdminViewWithNullFields() throws Exception {
        mockMvc.perform(get("/requests/" + cancelledRequestId))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/details"))
            // Admin should have access to all functionality
            .andExpect(model().attributeExists("request"))
            // Verify the page loads successfully for admin with null fields
            .andExpect(content().string(containsString("ORDER-337-CANCELLED")));
    }
}
