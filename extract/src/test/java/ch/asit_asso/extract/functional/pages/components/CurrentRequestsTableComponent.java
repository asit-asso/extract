package ch.asit_asso.extract.functional.pages.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CurrentRequestsTableComponent {
    private static final String REQUEST_ROW_CSS_SELECTOR = "tr[data-href=\"requests/%d\"]";

    private WebDriver driver;
    private WebElement table;


    public CurrentRequestsTableComponent(WebDriver webDriver, WebElement tableElement) {

        if (webDriver == null) {
            throw new IllegalArgumentException("The web driver cannot be null.");
        }

        if (tableElement == null) {
            throw new IllegalArgumentException(("The finished requests table element cannot be null."));
        }

        // TODO Check web driver and table element state

        this.driver = webDriver;
        this.table = tableElement;
    }



    public RequestTableRowComponent getRequestRow(int requestId) {
        WebElement requestElement
                = this.table.findElement(
                By.cssSelector(String.format(CurrentRequestsTableComponent.REQUEST_ROW_CSS_SELECTOR,
                                             requestId)));

        if (requestElement == null || !requestElement.isDisplayed()) {
            return null;
        }

        return new RequestTableRowComponent(this.driver, requestElement);
    }
}
