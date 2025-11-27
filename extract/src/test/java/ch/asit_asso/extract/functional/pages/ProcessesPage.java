package ch.asit_asso.extract.functional.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the processes list page.
 */
public class ProcessesPage {
    private static final By TEXT_FILTER_LOCATOR = By.id("textFilter");
    private static final By FILTER_BUTTON_LOCATOR = By.id("filterButton");
    private static final By PROCESSES_TABLE_LOCATOR = By.cssSelector("table.dataTables");
    private static final By FILTER_FORM_LOCATOR = By.cssSelector("form.filter-form-inline");

    private final WebDriver driver;

    public ProcessesPage(WebDriver webDriver) {
        if (webDriver == null) {
            throw new IllegalArgumentException("The web driver cannot be null.");
        }

        // Check for either French "Traitements" or English "Processes"
        String title = webDriver.getTitle();
        if (!title.contains("Traitements") && !title.contains("Processes")) {
            throw new IllegalStateException(String.format(
                    "The web driver does not seem to point toward the processes page: %s", title));
        }

        this.driver = webDriver;
    }

    public boolean hasTextFilter() {
        return !driver.findElements(TEXT_FILTER_LOCATOR).isEmpty();
    }

    public boolean hasFilterButton() {
        return !driver.findElements(FILTER_BUTTON_LOCATOR).isEmpty();
    }

    public boolean hasDataTable() {
        return !driver.findElements(PROCESSES_TABLE_LOCATOR).isEmpty();
    }

    public boolean hasFilterForm() {
        return !driver.findElements(FILTER_FORM_LOCATOR).isEmpty();
    }

    public WebElement getTextFilter() {
        return driver.findElement(TEXT_FILTER_LOCATOR);
    }

    public WebElement getProcessesTable() {
        return driver.findElement(PROCESSES_TABLE_LOCATOR);
    }

    public String getPageSource() {
        return driver.getPageSource();
    }

    public boolean pageContains(String text) {
        return driver.getPageSource().contains(text);
    }
}
