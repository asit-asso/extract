package ch.asit_asso.extract.functional.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the connectors list page.
 */
public class ConnectorsPage {
    private static final By TEXT_FILTER_LOCATOR = By.id("textFilter");
    private static final By TYPE_FILTER_LOCATOR = By.id("typeFilter");
    private static final By FILTER_BUTTON_LOCATOR = By.id("filterButton");
    private static final By CONNECTORS_TABLE_LOCATOR = By.cssSelector("table.dataTables");
    private static final By FILTER_FORM_LOCATOR = By.cssSelector("form.filter-form-inline");

    private final WebDriver driver;

    public ConnectorsPage(WebDriver webDriver) {
        if (webDriver == null) {
            throw new IllegalArgumentException("The web driver cannot be null.");
        }

        if (!webDriver.getTitle().contains("Connecteurs")) {
            throw new IllegalStateException(String.format("The web driver does not seem to point toward the connectors page: %s",
                    webDriver.getTitle()));
        }

        this.driver = webDriver;
    }

    public boolean hasTextFilter() {
        return !driver.findElements(TEXT_FILTER_LOCATOR).isEmpty();
    }

    public boolean hasTypeFilter() {
        return !driver.findElements(TYPE_FILTER_LOCATOR).isEmpty();
    }

    public boolean hasFilterButton() {
        return !driver.findElements(FILTER_BUTTON_LOCATOR).isEmpty();
    }

    public boolean hasDataTable() {
        return !driver.findElements(CONNECTORS_TABLE_LOCATOR).isEmpty();
    }

    public boolean hasFilterForm() {
        return !driver.findElements(FILTER_FORM_LOCATOR).isEmpty();
    }

    public WebElement getTextFilter() {
        return driver.findElement(TEXT_FILTER_LOCATOR);
    }

    public WebElement getTypeFilter() {
        return driver.findElement(TYPE_FILTER_LOCATOR);
    }

    public WebElement getConnectorsTable() {
        return driver.findElement(CONNECTORS_TABLE_LOCATOR);
    }

    public String getPageSource() {
        return driver.getPageSource();
    }

    public boolean pageContains(String text) {
        return driver.getPageSource().contains(text);
    }
}
