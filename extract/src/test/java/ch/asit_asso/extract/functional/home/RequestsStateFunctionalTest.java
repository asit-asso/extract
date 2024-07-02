package ch.asit_asso.extract.functional.home;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import ch.asit_asso.extract.functional.pages.HomePage;
import ch.asit_asso.extract.functional.pages.LoginPage;
import ch.asit_asso.extract.functional.pages.RequestDetailsPage;
import ch.asit_asso.extract.functional.pages.components.CurrentRequestsTableComponent;
import ch.asit_asso.extract.functional.pages.components.FinishedRequestsTableComponent;
import ch.asit_asso.extract.functional.pages.components.RequestTableRowComponent;
import ch.asit_asso.extract.functional.pages.enums.RequestState;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("functional")
public class RequestsStateFunctionalTest {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "motdepasse21";
    private static final String APPLICATION_URL = "http://127.0.0.1:8080/extract";
    private static final int ERROR_REQUEST_ID = 2;
    private static final int FINISHED_REQUEST_ID = 3;
    private static final int REJECTED_REQUEST_ID = 4;

    private static final int STANDBY_REQUEST_ID = 1;

    private WebDriver driver;
    private HomePage homePage;


    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                List.of("--disable-gpu",
                        "--window-size=1920,1200",
                        "--ignore-certificate-errors",
                        "--disable-extensions",
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--headless",
                        "--remote-allow-origins=*",
                        "--disable-logging",
                        "--log-level=OFF"
                )
        );

        this.driver = new ChromeDriver(options);
        this.driver.get(RequestsStateFunctionalTest.APPLICATION_URL);
        this.driver.manage().window().maximize();
        this.driver.manage().timeouts().implicitlyWait(Duration.of(120, ChronoUnit.MILLIS));
        LoginPage loginPage = new LoginPage(this.driver);
        this.homePage = loginPage.loginAs(RequestsStateFunctionalTest.ADMIN_USERNAME,
                                          RequestsStateFunctionalTest.ADMIN_PASSWORD);
    }

    @Test
    @DisplayName("Request in error displays correctly")
    public void testErrorRequest() {
        CurrentRequestsTableComponent currentRequestsTable = this.homePage.getCurrentRequestsTableWhenLoaded();
        RequestTableRowComponent requestRow
                = currentRequestsTable.getRequestRow(RequestsStateFunctionalTest.ERROR_REQUEST_ID);

        assertNotNull(requestRow);
        assertEquals(RequestState.ERROR, requestRow.getStateFromCssClasses());
        assertEquals(RequestState.ERROR, requestRow.getStateFromIcon());

        RequestDetailsPage detailsPage = requestRow.openDetails();
        assertNotNull(detailsPage);
        assertTrue(detailsPage.isCorrectUrlForRequest(RequestsStateFunctionalTest.APPLICATION_URL,
                                                      RequestsStateFunctionalTest.ERROR_REQUEST_ID));
        assertTrue(detailsPage.hasErrorActionPanel());
        assertFalse(detailsPage.hasValidationActionPanel());
    }

    @Test
    @DisplayName("Request finished with data displays correctly")
    public void testFinishedRequest() {
        FinishedRequestsTableComponent finishedRequestsTable = this.homePage.getFinishedRequestsTableWhenLoaded();
        RequestTableRowComponent requestRow
                = finishedRequestsTable.getRequestRow(RequestsStateFunctionalTest.FINISHED_REQUEST_ID);

        assertNotNull(requestRow);
        assertEquals(RequestState.FINISHED, requestRow.getStateFromCssClasses());
        assertEquals(RequestState.FINISHED, requestRow.getStateFromIcon());

        RequestDetailsPage detailsPage = requestRow.openDetails();
        assertNotNull(detailsPage);
        assertTrue(detailsPage.isCorrectUrlForRequest(RequestsStateFunctionalTest.APPLICATION_URL,
                                                      RequestsStateFunctionalTest.FINISHED_REQUEST_ID));
        assertFalse(detailsPage.hasActionPanel());
    }

    @Test
    @DisplayName("Rejected request displays correctly")
    public void testRejectedRequest() {
        FinishedRequestsTableComponent finishedRequestsTable = this.homePage.getFinishedRequestsTableWhenLoaded();
        RequestTableRowComponent requestRow
                = finishedRequestsTable.getRequestRow(RequestsStateFunctionalTest.REJECTED_REQUEST_ID);

        assertNotNull(requestRow);
        assertEquals(RequestState.REJECTED, requestRow.getStateFromCssClasses());
        assertEquals(RequestState.REJECTED, requestRow.getStateFromIcon());


        RequestDetailsPage detailsPage = requestRow.openDetails();
        assertNotNull(detailsPage);
        assertTrue(detailsPage.isCorrectUrlForRequest(RequestsStateFunctionalTest.APPLICATION_URL,
                                                      RequestsStateFunctionalTest.REJECTED_REQUEST_ID));
        assertFalse(detailsPage.hasActionPanel());
    }

    @Test
    @DisplayName("Request waiting for validation displays correctly")
    public void testStandbyRequest() {
        CurrentRequestsTableComponent currentRequestsTable = this.homePage.getCurrentRequestsTableWhenLoaded();
        RequestTableRowComponent requestRow
                = currentRequestsTable.getRequestRow(RequestsStateFunctionalTest.STANDBY_REQUEST_ID);

        assertNotNull(requestRow);
        assertEquals(RequestState.STANDBY, requestRow.getState());
        assertEquals(RequestState.STANDBY, requestRow.getStateFromCssClasses());
        assertEquals(RequestState.STANDBY, requestRow.getStateFromIcon());

        RequestDetailsPage detailsPage = requestRow.openDetails();
        assertNotNull(detailsPage);
        assertTrue(detailsPage.isCorrectUrlForRequest(RequestsStateFunctionalTest.APPLICATION_URL,
                                                      RequestsStateFunctionalTest.STANDBY_REQUEST_ID));
        assertTrue(detailsPage.hasValidationActionPanel());
        assertFalse(detailsPage.hasErrorActionPanel());

    }

    @AfterEach
    public void tearDown() {
        this.driver.quit();
    }
}
