package ch.asit_asso.extract.functional.pages;

import java.time.Duration;
import ch.asit_asso.extract.functional.pages.components.CurrentRequestsTableComponent;
import ch.asit_asso.extract.functional.pages.components.FinishedRequestsTableComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePage {
    private static final By CURRENT_REQUESTS_TABLE_LOCATOR = By.id("currentRequestsTable");
    private static final String LOADED_CSS_CLASS = "loaded";
    private static final String EXPECTED_PAGE_TITLE = "Extract – Accueil";
    private static final By FINISHED_REQUESTS_TABLE_LOCATOR = By.id("finishedRequestsTable");


    private final Logger logger = LoggerFactory.getLogger(HomePage.class);

    public WebDriver driver;



    public HomePage(WebDriver webDriver) {

        if (webDriver == null) {
            throw new IllegalArgumentException("The web driver cannot be null.");
        }

        if (!HomePage.EXPECTED_PAGE_TITLE.equals(webDriver.getTitle())) {
            throw new IllegalStateException(String.format("The web driver does not seem to point toward the home page: %s",
                                                          webDriver.getTitle()));
        }

        this.driver = webDriver;
    }


    public CurrentRequestsTableComponent getCurrentRequestsTableWhenLoaded() {
        Wait<WebDriver> homePageWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement currentRequestsTable = driver.findElement(HomePage.CURRENT_REQUESTS_TABLE_LOCATOR);

        try {
            homePageWait.until(webDriver ->
                                       currentRequestsTable.isDisplayed()
                                               && currentRequestsTable.getAttribute("class")
                                                                      .contains(HomePage.LOADED_CSS_CLASS));
        } catch (Exception e) {
            throw new RuntimeException(String.format("The current requests table content did not load in time. Home page:\n%s",
                                                     this.driver.getPageSource()));

        }

        return new CurrentRequestsTableComponent(this.driver, currentRequestsTable);
    }


    public FinishedRequestsTableComponent getFinishedRequestsTableWhenLoaded() {
        Wait<WebDriver> homePageWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement finishedRequestsTable = driver.findElement(HomePage.FINISHED_REQUESTS_TABLE_LOCATOR);

        try {
            homePageWait.until(webDriver ->
                                       finishedRequestsTable.isDisplayed()
                                            && finishedRequestsTable.getAttribute("class")
                                                                    .contains(HomePage.LOADED_CSS_CLASS));
        } catch (TimeoutException e) {
            throw new RuntimeException(String.format("The finished requests table content did not load in time. Home page:\n%s",
                                               this.driver.getPageSource()));
        }

        return new FinishedRequestsTableComponent(this.driver, finishedRequestsTable);
    }
}
