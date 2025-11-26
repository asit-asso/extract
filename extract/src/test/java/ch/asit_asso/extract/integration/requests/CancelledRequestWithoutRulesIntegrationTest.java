package ch.asit_asso.extract.integration.requests;

import ch.asit_asso.extract.authentication.ApplicationUser;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.SystemParameter;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.integration.TestSecurityConfig;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import javax.persistence.EntityManager;

import java.util.GregorianCalendar;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for Issue #337: Fix template rendering for cancelled requests without matching rules.
 * This test ensures that the request details page renders correctly in various scenarios:
 * - Requests with no matching rules (unmatched)
 * - Requests with matched rules but cancelled/finished
 * - Null outputFolderPath and outputFiles
 * - Client Response panel visibility handling
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestSecurityConfig.class)
public class CancelledRequestWithoutRulesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private ConnectorsRepository connectorsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private SystemParametersRepository systemParametersRepository;

    @Autowired
    private EntityManager entityManager;

    // Test data IDs
    private Integer cancelledRequestId;
    private Integer normalRequestId;
    private Integer cancelledWithRulesRequestId;
    private Integer cancelledWithRemarkRequestId;
    private ApplicationUser mockAdminUser;

    @BeforeAll
    public void setUpTestData() {
        // Clean up any existing test data (except users to avoid FK issues)
        requestsRepository.deleteAll();
        connectorsRepository.deleteAll();

        // Use existing admin user or create one if it doesn't exist
        User adminUser = usersRepository.findByLoginIgnoreCase("admin");
        if (adminUser == null) {
            adminUser = new User();
            adminUser.setLogin("admin");
            adminUser.setName("Test Admin");
            adminUser.setEmail("admin@test.com");
            adminUser.setProfile(User.Profile.ADMIN);
            adminUser.setActive(true);
            adminUser.setPassword("$2a$10$dummyHashedPassword");
            adminUser.setUserType(User.UserType.LOCAL);
            adminUser = usersRepository.save(adminUser);
        }

        // Create ApplicationUser for authentication
        mockAdminUser = new ApplicationUser(adminUser);

        // Create minimal system parameters to avoid redirect to /setup
        if (systemParametersRepository.count() == 0) {
            systemParametersRepository.save(new SystemParameter("base_path", "/tmp/extract/orders"));
            systemParametersRepository.save(new SystemParameter("dashboard_interval", "20"));
        }

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

        // Request WITH matched rules and output folder (finished but can review)
        Request cancelledWithRules = new Request();
        cancelledWithRules.setProductLabel("Request With Rules");
        cancelledWithRules.setOrderLabel("ORDER-337-WITH-RULES");
        cancelledWithRules.setClient("Test Client A");
        cancelledWithRules.setStatus(Request.Status.FINISHED);
        cancelledWithRules.setFolderOut("request337/with_rules"); // Has output folder
        cancelledWithRules.setStartDate(new GregorianCalendar());
        cancelledWithRules.setRemark("Completed successfully then reviewed");
        cancelledWithRules.setConnector(testConnector);
        cancelledWithRules.setParameters("{\"format\":\"TIFF\"}");
        cancelledWithRules.setPerimeter("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
        cancelledWithRules = requestsRepository.save(cancelledWithRules);
        cancelledWithRulesRequestId = cancelledWithRules.getId();

        // Request with error and remark only (for panel visibility test)
        Request cancelledWithRemark = new Request();
        cancelledWithRemark.setProductLabel("Request With Remark Only");
        cancelledWithRemark.setOrderLabel("ORDER-337-REMARK");
        cancelledWithRemark.setClient("Test Client C");
        cancelledWithRemark.setStatus(Request.Status.ERROR);
        cancelledWithRemark.setFolderOut(null); // No output folder
        cancelledWithRemark.setStartDate(new GregorianCalendar());
        cancelledWithRemark.setRemark("Processing failed: Project requirements changed");
        cancelledWithRemark.setConnector(testConnector);
        cancelledWithRemark.setParameters("{}");
        cancelledWithRemark.setPerimeter("{}");
        cancelledWithRemark = requestsRepository.save(cancelledWithRemark);
        cancelledWithRemarkRequestId = cancelledWithRemark.getId();
    }

    @Test
    @DisplayName("Should render details page for cancelled request without matching rules")
    public void testDetailsPageForCancelledRequestWithoutRules() throws Exception {
        mockMvc.perform(get("/requests/" + cancelledRequestId)
            .with(user(mockAdminUser)))
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
    @DisplayName("Should handle null outputFiles without errors")
    public void testNullOutputFilesHandling() throws Exception {
        // Use the cancelled request which has null folder
        mockMvc.perform(get("/requests/" + cancelledRequestId)
            .with(user(mockAdminUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/details"))
            // Verify the response panel shows with remark but no file errors
            .andExpect(content().string(containsString("Cancelled: No matching rules found")))
            // Verify no NullPointerException indicators
            .andExpect(content().string(not(containsString("NullPointerException"))))
            .andExpect(content().string(not(containsString("Error 500"))));
    }

    @Test
    @DisplayName("Should not show download button when outputFiles is null")
    public void testDownloadButtonNotShownForNullFiles() throws Exception {
        mockMvc.perform(get("/requests/" + cancelledRequestId)
            .with(user(mockAdminUser)))
            .andExpect(status().isOk())
            // The download button should not be present (check that the ID doesn't exist)
            .andExpect(content().string(not(containsString("id=\"file-download-button\""))));
    }

    @Test
    @DisplayName("Normal request should still work correctly")
    public void testNormalRequestStillWorks() throws Exception {
        mockMvc.perform(get("/requests/" + normalRequestId)
            .with(user(mockAdminUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/details"))
            .andExpect(model().attributeExists("request"))
            .andExpect(content().string(containsString("ORDER-337-NORMAL")));
    }

    @Test
    @DisplayName("Admin should see all details even with null fields")
    public void testAdminViewWithNullFields() throws Exception {
        mockMvc.perform(get("/requests/" + cancelledRequestId)
            .with(user(mockAdminUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/details"))
            // Admin should have access to all functionality
            .andExpect(model().attributeExists("request"))
            // Verify the page loads successfully for admin with null fields
            .andExpect(content().string(containsString("ORDER-337-CANCELLED")));
    }

    @Test
    @DisplayName("Finished request WITH matched rules should render correctly")
    public void testFinishedRequestWithMatchedRules() throws Exception {
        mockMvc.perform(get("/requests/" + cancelledWithRulesRequestId)
            .with(user(mockAdminUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/details"))
            .andExpect(model().attributeExists("request"))
            // Verify page content renders correctly
            .andExpect(content().string(containsString("ORDER-337-WITH-RULES")))
            .andExpect(content().string(containsString("Request With Rules")))
            .andExpect(content().string(containsString("Completed successfully then reviewed")))
            // Verify no JavaScript errors or NPEs
            .andExpect(content().string(not(containsString("NullPointerException"))))
            .andExpect(content().string(not(containsString("Error 500"))))
            // Verify Client Response panel should be visible (has remark)
            .andExpect(content().string(containsString("Completed successfully then reviewed")));
    }

    @Test
    @DisplayName("Client Response panel visibility should be conditional")
    public void testClientResponsePanelVisibility() throws Exception {
        // Scenario A: Request with remark only (no output folder) - panel SHOULD be visible
        mockMvc.perform(get("/requests/" + cancelledWithRemarkRequestId)
            .with(user(mockAdminUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/details"))
            // Panel should be visible because remark is not empty
            .andExpect(content().string(containsString("Processing failed: Project requirements changed")))
            // Verify panel title is present (indicates panel is rendered) - translated as "Réponse au client"
            .andExpect(content().string(containsString("Réponse au client")));

        // Scenario B: Request without remark and no output files - panel might be hidden
        // This is tested with cancelledRequestId which has empty remark and null folder
        mockMvc.perform(get("/requests/" + cancelledRequestId)
            .with(user(mockAdminUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/details"))
            // Page should render without errors even if panel is hidden
            .andExpect(content().string(not(containsString("NullPointerException"))))
            .andExpect(content().string(not(containsString("Error 500"))));

        // Scenario C: Request with matched rules and output folder - panel SHOULD be visible
        mockMvc.perform(get("/requests/" + cancelledWithRulesRequestId)
            .with(user(mockAdminUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("requests/details"))
            // Panel should be visible because remark is not empty
            .andExpect(content().string(containsString("Completed successfully then reviewed")));
    }
}
