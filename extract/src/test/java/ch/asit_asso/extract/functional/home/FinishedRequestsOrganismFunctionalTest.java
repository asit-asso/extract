/*
 * Copyright (C) 2025 asit-asso
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
package ch.asit_asso.extract.functional.home;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.functional.pages.LoginPage;
import ch.asit_asso.extract.persistence.RequestsRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Functional tests for the organism shown in the finished requests table (issue #369).
 *
 * The string the customer cell displays is built when the row is serialized, and the table is paged and
 * filtered by the server. Neither a unit test on the model nor a repository test on the criteria proves
 * that the two meet: only asking the deployed application for a page of the table does. That endpoint
 * also resolves the folder the orders are stored under, which is mounted in the application server and
 * nowhere else, so it cannot be exercised from the container the integration tests run in.
 *
 * The requests are seeded behind a marker, and removed afterwards.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("functional")
@DisplayName("Finished Requests Organism Functional Tests (issue #369)")
public class FinishedRequestsOrganismFunctionalTest {

    private static final String ADMIN_USERNAME = "admin";

    private static final String ADMIN_PASSWORD = "motdepasse21";

    private static final String APPLICATION_URL = "http://127.0.0.1:8080/extract";

    /**
     * The title every page of the application carries. It tells the application apart from the 404 page
     * the server answers while the application is redeploying.
     */
    private static final String APPLICATION_TITLE = "Extract";

    /**
     * Prefixes the seeded values, so that they cannot collide with the test data set.
     */
    private static final String MARKER = "ZZ369";

    /**
     * A customer who placed an order for an organism of their own.
     */
    private static final String CUSTOMER = MARKER + " Alice Martin";

    /**
     * The organism the customer placed the order for.
     */
    private static final String ORGANISM = MARKER + " Ma boîte SA";

    /**
     * A customer who placed an order from a personal account: the organism repeats their name.
     */
    private static final String PERSONAL_ACCOUNT = MARKER + " Bob Dupont";

    @Autowired
    private RequestsRepository requestsRepository;

    private WebDriver driver;

    private final List<Integer> seededIds = new ArrayList<>();


    @BeforeAll
    public static void setUpClass() {
        WebDriverManager.chromedriver().setup();
    }


    @BeforeEach
    public void setUp() {
        this.seedRequests();

        ChromeOptions options = new ChromeOptions();
        options.addArguments(List.of("--disable-gpu", "--window-size=1920,1200",
                                     "--ignore-certificate-errors", "--disable-extensions", "--no-sandbox",
                                     "--disable-dev-shm-usage", "--headless", "--remote-allow-origins=*",
                                     "--disable-logging", "--log-level=OFF"));

        this.driver = new ChromeDriver(options);
        this.driver.manage().timeouts().implicitlyWait(Duration.of(10, ChronoUnit.SECONDS));

        this.waitForTheApplicationToBeDeployed();
        new LoginPage(this.driver).loginAs(FinishedRequestsOrganismFunctionalTest.ADMIN_USERNAME,
                                           FinishedRequestsOrganismFunctionalTest.ADMIN_PASSWORD);
    }


    @AfterEach
    public void tearDown() {

        if (this.driver != null) {
            this.driver.quit();
        }

        this.seededIds.forEach(id -> this.requestsRepository.deleteById(id));
        this.seededIds.clear();
    }


    @Test
    @DisplayName("The organism follows the customer, and the table can be searched by organism (369-1, 369-3)")
    public void theOrganismIsShownAndSearchable() {
        List<String> customers = this.searchTheFinishedRequests(
                FinishedRequestsOrganismFunctionalTest.ORGANISM);

        assertEquals(List.of(String.format("%s (%s)", FinishedRequestsOrganismFunctionalTest.CUSTOMER,
                                           FinishedRequestsOrganismFunctionalTest.ORGANISM)),
                     customers,
                     "Searching the organism must return the order, and show the organism next to the customer");
    }


    @Test
    @DisplayName("An organism that only repeats the customer is not displayed twice (369-2)")
    public void anOrganismRepeatingTheCustomerIsNotShown() {
        List<String> customers = this.searchTheFinishedRequests(
                FinishedRequestsOrganismFunctionalTest.PERSONAL_ACCOUNT);

        assertEquals(List.of(FinishedRequestsOrganismFunctionalTest.PERSONAL_ACCOUNT), customers,
                     "An order placed from a personal account must show the name once");
    }


    /**
     * Types a text in the search box of the finished requests table and reads the customer cells of the
     * rows the server answers with.
     *
     * @param searchText the text to search
     * @return the customer cell of every row of the table
     */
    private List<String> searchTheFinishedRequests(final String searchText) {
        this.driver.get(FinishedRequestsOrganismFunctionalTest.APPLICATION_URL + "/");

        WebElement searchBox = this.driver.findElement(By.id("textFilter"));
        searchBox.clear();
        searchBox.sendKeys(searchText);
        this.driver.findElement(By.id("filterButton")).click();

        // The table asks the server for a page of its own and redraws itself once it answers, which makes
        // the cells read too early go stale. The cells are therefore read again until the redraw settles.
        return new WebDriverWait(this.driver, Duration.of(20, ChronoUnit.SECONDS))
                .ignoring(StaleElementReferenceException.class)
                .until(webDriver -> {
                    List<String> customers
                            = webDriver.findElements(
                                              By.cssSelector("#finishedRequestsTable tbody tr td:nth-child(4)"))
                                       .stream()
                                       .map(cell -> cell.getText().trim())
                                       .filter(text -> text.startsWith(
                                               FinishedRequestsOrganismFunctionalTest.MARKER))
                                       .toList();

                    return customers.isEmpty() ? null : customers;
                });
    }


    /**
     * Opens the application, waiting for it to answer.
     *
     * The build repackages the WAR that the application server deploys, so the application can still be
     * redeploying when the test starts, and it then answers a 404.
     */
    private void waitForTheApplicationToBeDeployed() {
        new WebDriverWait(this.driver, Duration.of(90, ChronoUnit.SECONDS))
                .pollingEvery(Duration.of(2, ChronoUnit.SECONDS))
                .ignoring(WebDriverException.class)
                .until(webDriver -> {
                    webDriver.get(FinishedRequestsOrganismFunctionalTest.APPLICATION_URL);

                    return FinishedRequestsOrganismFunctionalTest.APPLICATION_TITLE.equals(
                            webDriver.getTitle());
                });
    }


    /**
     * Adds the finished requests whose display these tests check.
     */
    private void seedRequests() {
        this.seededIds.add(this.saveFinishedRequest(FinishedRequestsOrganismFunctionalTest.CUSTOMER,
                                                    FinishedRequestsOrganismFunctionalTest.ORGANISM));
        this.seededIds.add(this.saveFinishedRequest(FinishedRequestsOrganismFunctionalTest.PERSONAL_ACCOUNT,
                                                    FinishedRequestsOrganismFunctionalTest.PERSONAL_ACCOUNT));
    }


    /**
     * Adds a finished request placed by a given customer for a given organism.
     *
     * @param customer the name of the customer
     * @param organism the name of the organism
     * @return the identifier of the saved request
     */
    private Integer saveFinishedRequest(final String customer, final String organism) {
        Request request = new Request();
        request.setStatus(Request.Status.FINISHED);
        request.setClient(customer);
        request.setOrganism(organism);
        request.setOrderLabel(FinishedRequestsOrganismFunctionalTest.MARKER + "-order");
        request.setProductLabel(FinishedRequestsOrganismFunctionalTest.MARKER + "-product");
        // The table shows how long ago an order was received, so a request without dates cannot be
        // rendered: the serialization fails and the endpoint answers an empty table.
        request.setStartDate(new GregorianCalendar());
        request.setEndDate(new GregorianCalendar());
        request.setUsersCollection(new ArrayList<>());
        request.setUserGroupsCollection(new ArrayList<>());

        return this.requestsRepository.save(request).getId();
    }
}
