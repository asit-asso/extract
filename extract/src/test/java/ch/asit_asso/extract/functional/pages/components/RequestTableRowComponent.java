package ch.asit_asso.extract.functional.pages.components;

import java.util.List;
import ch.asit_asso.extract.functional.pages.RequestDetailsPage;
import ch.asit_asso.extract.functional.pages.enums.RequestState;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class RequestTableRowComponent {

    private static final List<String> ERROR_ICON_CLASSES = List.of(new String[]{"fa-exclamation-triangle", "text-danger"});
    private static final String ERROR_ROW_CLASS = "error";
    private static final List<String> FINISHED_ICON_CLASSES = List.of(new String[]{"fa-check", "text-muted"});
    private static final String FINISHED_ROW_CLASS = "finished";
    private static final List<String> REJECTED_ICON_CLASSES = List.of(new String[]{"fa-times", "text-muted"});
    private static final String REJECTED_ROW_CLASS = "rejected";
    private static final List<String> RUNNING_ICON_CLASSES = List.of(new String[]{"fa-cog", "text-success"});
    private static final String RUNNING_ROW_CLASS = "running";
    private static final List<String> STANDBY_ICON_CLASSES = List.of(new String[]{"fa-user", "text-warning"});
    private static final String STANDBY_ROW_CLASS = "standby";
    
    private WebDriver driver;
    private WebElement row;



    public RequestTableRowComponent(WebDriver webDriver, WebElement rowElement) {

        if (webDriver == null) {
            throw new IllegalArgumentException("The web driver cannot be null.");
        }

        if (rowElement == null) {
            throw new IllegalArgumentException("The row element representing the request cannot be null.");
        }

        // TODO Check web driver and web element state

        this.driver = webDriver;
        this.row = rowElement;
    }


    public RequestDetailsPage openDetails() {
//        ((JavascriptExecutor) this.driver).executeScript("arguments[0].scrollIntoView();", this.row);
//
//        try {
//            Thread.sleep(500);
//
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        this.row.click();

        return new RequestDetailsPage(this.driver);
    }



    public RequestState getState() {
        RequestState cssState = this.getStateFromCssClasses();
        RequestState iconState = this.getStateFromIcon();

        if (cssState != iconState) {
            return RequestState.INCOHERENT;
        }

        return cssState;
    }



    public RequestState getStateFromCssClasses() {
        List<String> rowClasses = List.of(this.row.getAttribute("class").split("\\s+"));
        boolean hasError = rowClasses.contains(RequestTableRowComponent.ERROR_ROW_CLASS);
        boolean hasFinished = rowClasses.contains(RequestTableRowComponent.FINISHED_ROW_CLASS);
        boolean hasRejected = rowClasses.contains(RequestTableRowComponent.REJECTED_ROW_CLASS);
        boolean hasRunning = rowClasses.contains(RequestTableRowComponent.RUNNING_ROW_CLASS);
        boolean hasStandby = rowClasses.contains(RequestTableRowComponent.STANDBY_ROW_CLASS);

        if (hasError && !hasFinished && !hasRejected && !hasRunning && !hasStandby) {
            return RequestState.ERROR;
        }

        if (hasFinished && !hasError && !hasRejected && !hasRunning && !hasStandby) {
            return RequestState.FINISHED;
        }

        if (hasRejected && !hasError && !hasFinished && !hasRunning && !hasStandby) {
            return RequestState.REJECTED;
        }

        if (hasRunning && !hasError && !hasFinished && !hasRejected && !hasStandby) {
            return RequestState.RUNNING;
        }

        if (hasStandby && !hasError && !hasFinished && !hasRejected && !hasRunning) {
            return RequestState.STANDBY;
        }

        return RequestState.INCOHERENT;
    }

    public RequestState getStateFromIcon() {
        WebElement icon = row.findElement(By.cssSelector("i.fa"));

        List<String> iconClasses = List.of(icon.getAttribute("class").split("\\s+"));
        boolean hasError = iconClasses.containsAll(RequestTableRowComponent.ERROR_ICON_CLASSES);
        boolean hasFinished = iconClasses.containsAll(RequestTableRowComponent.FINISHED_ICON_CLASSES);
        boolean hasRejected = iconClasses.containsAll(RequestTableRowComponent.REJECTED_ICON_CLASSES);
        boolean hasRunning = iconClasses.containsAll(RequestTableRowComponent.RUNNING_ICON_CLASSES);
        boolean hasStandby = iconClasses.containsAll(RequestTableRowComponent.STANDBY_ICON_CLASSES);

        if (hasError && !hasFinished && !hasRejected && !hasRunning && !hasStandby) {
            return RequestState.ERROR;
        }

        if (hasFinished && !hasError && !hasRejected && !hasRunning && !hasStandby) {
            return RequestState.FINISHED;
        }

        if (hasRejected && !hasError && !hasFinished && !hasRunning && !hasStandby) {
            return RequestState.REJECTED;
        }

        if (hasRunning && !hasError && !hasFinished && !hasRejected && !hasStandby) {
            return RequestState.RUNNING;
        }

        if (hasStandby && !hasError && !hasFinished && !hasRejected && !hasRunning) {
            return RequestState.STANDBY;
        }

        return RequestState.INCOHERENT;
    }
}
