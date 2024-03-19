package ch.asit_asso.extract.functional.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {

    private static final String EXPECTED_PAGE_TITLE = "Extract";
    private static final By LOGIN_BUTTON_LOCATOR = By.id("loginButton");
    private static final By PASSWORD_LOCATOR = By.name("password");
    private static final By USERNAME_LOCATOR = By.name("username");

    private WebDriver driver;

    public LoginPage(WebDriver webDriver) {

        if (webDriver == null) {
            throw new IllegalArgumentException("The web driver cannot be null.");
        }

        if (!LoginPage.EXPECTED_PAGE_TITLE.equals(webDriver.getTitle())) {
            throw new IllegalStateException(String.format(
                    "The web driver does not seem to point toward the login page: %s", webDriver.getTitle()));
        }

        this.driver = webDriver;
    }

    public HomePage loginAs(String username, String password) {
        this.typeUsername(username);
        this.typePassword(password);

        return this.submitLogin();
    }



    private HomePage submitLogin() {
        this.driver.findElement(LoginPage.LOGIN_BUTTON_LOCATOR).click();

        return new HomePage(this.driver);
    }



    private void typePassword(String password) {
        this.driver.findElement(LoginPage.PASSWORD_LOCATOR).sendKeys(password);
    }



    private void typeUsername(String username) {
        this.driver.findElement(LoginPage.USERNAME_LOCATOR).sendKeys(username);
    }

}
