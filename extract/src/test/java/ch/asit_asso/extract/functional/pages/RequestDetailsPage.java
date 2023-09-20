package ch.asit_asso.extract.functional.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class RequestDetailsPage {

    private static final By ANY_ACTION_PANEL_LOCATOR = By.cssSelector("div.current-step");
    private static final By ERROR_PANEL_LOCATOR = By.cssSelector("div.current-step.current-step-error");
    private static final String EXPECTED_PAGE_TITLE_START = "Extract - Détails de la demande";
    private static final String EXPECTED_URL_FORMAT = "%s/requests/%d";
    private static final By VALIDATION_PANEL_LOCATOR = By.cssSelector("div.current-step.current-step-standby");

    private WebDriver driver;

    public RequestDetailsPage(WebDriver webDriver) {

        if (webDriver == null) {
            throw new IllegalArgumentException("The web driver cannot be null.");
        }

        if (!webDriver.getTitle().startsWith(RequestDetailsPage.EXPECTED_PAGE_TITLE_START)) {
            throw new IllegalStateException(String.format(
                    "The web driver does not seem to point toward the request details page: %s", webDriver.getTitle()));
        }

        this.driver = webDriver;
    }


    public boolean hasErrorActionPanel() {
        WebElement actionPane = this.driver.findElement(RequestDetailsPage.ERROR_PANEL_LOCATOR);

        return actionPane != null && actionPane.isDisplayed();
    }


    public boolean hasValidationActionPanel() {
        WebElement actionPane = this.driver.findElement(RequestDetailsPage.VALIDATION_PANEL_LOCATOR);

        return actionPane != null && actionPane.isDisplayed();
    }



    public boolean hasActionPanel() {
        List<WebElement> actionPanes = this.driver.findElements(RequestDetailsPage.ANY_ACTION_PANEL_LOCATOR);

        return !actionPanes.isEmpty();
    }


    public boolean isCorrectUrlForRequest(String applicationUrl, int requestId) {
        return String.format(RequestDetailsPage.EXPECTED_URL_FORMAT,
                             applicationUrl, requestId).equals(this.driver.getCurrentUrl());
    }

}
