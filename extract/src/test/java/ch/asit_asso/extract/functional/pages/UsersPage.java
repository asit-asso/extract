package ch.asit_asso.extract.functional.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the users list page.
 */
public class UsersPage {
    private static final By TEXT_FILTER_LOCATOR = By.id("textFilter");
    private static final By ROLE_FILTER_LOCATOR = By.id("roleFilter");
    private static final By STATE_FILTER_LOCATOR = By.id("stateFilter");
    private static final By NOTIFICATIONS_FILTER_LOCATOR = By.id("notificationsFilter");
    private static final By TWO_FACTOR_FILTER_LOCATOR = By.id("2faFilter");
    private static final By FILTER_BUTTON_LOCATOR = By.id("filterButton");
    private static final By USERS_TABLE_LOCATOR = By.cssSelector("table.dataTables");
    private static final By FILTER_FORM_LOCATOR = By.cssSelector("form.filter-form-inline");

    private final WebDriver driver;

    public UsersPage(WebDriver webDriver) {
        if (webDriver == null) {
            throw new IllegalArgumentException("The web driver cannot be null.");
        }

        if (!webDriver.getTitle().contains("Utilisateurs")) {
            throw new IllegalStateException(String.format("The web driver does not seem to point toward the users page: %s",
                    webDriver.getTitle()));
        }

        this.driver = webDriver;
    }

    public boolean hasTextFilter() {
        return !driver.findElements(TEXT_FILTER_LOCATOR).isEmpty();
    }

    public boolean hasRoleFilter() {
        return !driver.findElements(ROLE_FILTER_LOCATOR).isEmpty();
    }

    public boolean hasStateFilter() {
        return !driver.findElements(STATE_FILTER_LOCATOR).isEmpty();
    }

    public boolean hasNotificationsFilter() {
        return !driver.findElements(NOTIFICATIONS_FILTER_LOCATOR).isEmpty();
    }

    public boolean hasTwoFactorFilter() {
        return !driver.findElements(TWO_FACTOR_FILTER_LOCATOR).isEmpty();
    }

    public boolean hasFilterButton() {
        return !driver.findElements(FILTER_BUTTON_LOCATOR).isEmpty();
    }

    public boolean hasDataTable() {
        return !driver.findElements(USERS_TABLE_LOCATOR).isEmpty();
    }

    public boolean hasFilterForm() {
        return !driver.findElements(FILTER_FORM_LOCATOR).isEmpty();
    }

    public WebElement getTextFilter() {
        return driver.findElement(TEXT_FILTER_LOCATOR);
    }

    public WebElement getUsersTable() {
        return driver.findElement(USERS_TABLE_LOCATOR);
    }

    public String getPageSource() {
        return driver.getPageSource();
    }

    public boolean pageContains(String text) {
        return driver.getPageSource().contains(text);
    }
}
