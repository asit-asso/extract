package ch.asit_asso.extract.functional.lists;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import ch.asit_asso.extract.functional.pages.ConnectorsPage;
import ch.asit_asso.extract.functional.pages.LoginPage;
import ch.asit_asso.extract.functional.pages.ProcessesPage;
import ch.asit_asso.extract.functional.pages.UsersPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Functional tests for list filtering functionality (Issue #344).
 * Tests that processes, connectors, and users list pages contain filtering elements.
 *
 * @author Bruno Alves
 */
@Tag("functional")
public class ListFilteringFunctionalTest {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "motdepasse21";
    private static final String APPLICATION_URL = "http://127.0.0.1:8080/extract";

    private WebDriver driver;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setUp() throws InterruptedException {
        Thread.sleep(500);

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
        this.driver.manage().window().maximize();
        this.driver.manage().timeouts().implicitlyWait(Duration.of(10, ChronoUnit.SECONDS));

        // Login
        this.driver.get(APPLICATION_URL);
        LoginPage loginPage = new LoginPage(this.driver);
        loginPage.loginAs(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    @AfterEach
    public void tearDown() {
        if (this.driver != null) {
            this.driver.quit();
        }
    }

    @Nested
    @DisplayName("Processes page filtering tests")
    class ProcessesFilteringTests {

        @Test
        @DisplayName("Processes page contains text filter input")
        public void testProcessesPageContainsTextFilterInput() {
            driver.get(APPLICATION_URL + "/processes");
            ProcessesPage processesPage = new ProcessesPage(driver);
            assertTrue(processesPage.hasTextFilter(), "Processes page should have text filter input");
        }

        @Test
        @DisplayName("Processes page contains filter button")
        public void testProcessesPageContainsFilterButton() {
            driver.get(APPLICATION_URL + "/processes");
            ProcessesPage processesPage = new ProcessesPage(driver);
            assertTrue(processesPage.hasFilterButton(), "Processes page should have filter button");
        }

        @Test
        @DisplayName("Processes page contains DataTables")
        public void testProcessesPageContainsDataTables() {
            driver.get(APPLICATION_URL + "/processes");
            ProcessesPage processesPage = new ProcessesPage(driver);
            assertTrue(processesPage.hasDataTable(), "Processes page should have DataTables");
        }

        @Test
        @DisplayName("Processes page contains filter form")
        public void testProcessesPageContainsFilterForm() {
            driver.get(APPLICATION_URL + "/processes");
            ProcessesPage processesPage = new ProcessesPage(driver);
            assertTrue(processesPage.hasFilterForm(), "Processes page should have filter form");
        }

        @Test
        @DisplayName("Processes page contains JavaScript for list management")
        public void testProcessesPageContainsJavaScript() {
            driver.get(APPLICATION_URL + "/processes");
            ProcessesPage processesPage = new ProcessesPage(driver);
            assertTrue(processesPage.pageContains("dataTablesProperties"),
                    "Processes page should contain DataTables JavaScript configuration");
        }
    }

    @Nested
    @DisplayName("Connectors page filtering tests")
    class ConnectorsFilteringTests {

        @Test
        @DisplayName("Connectors page contains text filter input")
        public void testConnectorsPageContainsTextFilterInput() {
            driver.get(APPLICATION_URL + "/connectors");
            ConnectorsPage connectorsPage = new ConnectorsPage(driver);
            assertTrue(connectorsPage.hasTextFilter(), "Connectors page should have text filter input");
        }

        @Test
        @DisplayName("Connectors page contains type dropdown filter")
        public void testConnectorsPageContainsTypeDropdownFilter() {
            driver.get(APPLICATION_URL + "/connectors");
            ConnectorsPage connectorsPage = new ConnectorsPage(driver);
            assertTrue(connectorsPage.hasTypeFilter(), "Connectors page should have type dropdown filter");
        }

        @Test
        @DisplayName("Connectors page contains filter button")
        public void testConnectorsPageContainsFilterButton() {
            driver.get(APPLICATION_URL + "/connectors");
            ConnectorsPage connectorsPage = new ConnectorsPage(driver);
            assertTrue(connectorsPage.hasFilterButton(), "Connectors page should have filter button");
        }

        @Test
        @DisplayName("Connectors page contains DataTables")
        public void testConnectorsPageContainsDataTables() {
            driver.get(APPLICATION_URL + "/connectors");
            ConnectorsPage connectorsPage = new ConnectorsPage(driver);
            assertTrue(connectorsPage.hasDataTable(), "Connectors page should have DataTables");
        }

        @Test
        @DisplayName("Connectors page contains filter form")
        public void testConnectorsPageContainsFilterForm() {
            driver.get(APPLICATION_URL + "/connectors");
            ConnectorsPage connectorsPage = new ConnectorsPage(driver);
            assertTrue(connectorsPage.hasFilterForm(), "Connectors page should have filter form");
        }
    }

    @Nested
    @DisplayName("Users page filtering tests")
    class UsersFilteringTests {

        @Test
        @DisplayName("Users page contains text filter input")
        public void testUsersPageContainsTextFilterInput() {
            driver.get(APPLICATION_URL + "/users");
            UsersPage usersPage = new UsersPage(driver);
            assertTrue(usersPage.hasTextFilter(), "Users page should have text filter input");
        }

        @Test
        @DisplayName("Users page contains role dropdown filter")
        public void testUsersPageContainsRoleDropdownFilter() {
            driver.get(APPLICATION_URL + "/users");
            UsersPage usersPage = new UsersPage(driver);
            assertTrue(usersPage.hasRoleFilter(), "Users page should have role dropdown filter");
        }

        @Test
        @DisplayName("Users page contains state dropdown filter")
        public void testUsersPageContainsStateDropdownFilter() {
            driver.get(APPLICATION_URL + "/users");
            UsersPage usersPage = new UsersPage(driver);
            assertTrue(usersPage.hasStateFilter(), "Users page should have state dropdown filter");
        }

        @Test
        @DisplayName("Users page contains notifications dropdown filter")
        public void testUsersPageContainsNotificationsDropdownFilter() {
            driver.get(APPLICATION_URL + "/users");
            UsersPage usersPage = new UsersPage(driver);
            assertTrue(usersPage.hasNotificationsFilter(), "Users page should have notifications dropdown filter");
        }

        @Test
        @DisplayName("Users page contains two-factor auth dropdown filter")
        public void testUsersPageContainsTwoFactorDropdownFilter() {
            driver.get(APPLICATION_URL + "/users");
            UsersPage usersPage = new UsersPage(driver);
            assertTrue(usersPage.hasTwoFactorFilter(), "Users page should have two-factor auth dropdown filter");
        }

        @Test
        @DisplayName("Users page contains filter button")
        public void testUsersPageContainsFilterButton() {
            driver.get(APPLICATION_URL + "/users");
            UsersPage usersPage = new UsersPage(driver);
            assertTrue(usersPage.hasFilterButton(), "Users page should have filter button");
        }

        @Test
        @DisplayName("Users page contains DataTables")
        public void testUsersPageContainsDataTables() {
            driver.get(APPLICATION_URL + "/users");
            UsersPage usersPage = new UsersPage(driver);
            assertTrue(usersPage.hasDataTable(), "Users page should have DataTables");
        }

        @Test
        @DisplayName("Users page contains filter form")
        public void testUsersPageContainsFilterForm() {
            driver.get(APPLICATION_URL + "/users");
            UsersPage usersPage = new UsersPage(driver);
            assertTrue(usersPage.hasFilterForm(), "Users page should have filter form");
        }
    }
}
