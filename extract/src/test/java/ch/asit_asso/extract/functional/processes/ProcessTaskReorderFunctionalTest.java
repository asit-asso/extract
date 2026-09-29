/*
 * Copyright (C) 2026 asit-asso
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
package ch.asit_asso.extract.functional.processes;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.functional.pages.LoginPage;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.TasksRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Functional tests for reordering a process's tasks by drag-and-drop (issue #440).
 *
 * Reordering used to make the browser lose the selected value of a boolean task parameter rendered as a
 * Yes/No radio-button pair (e.g. the Remarque fixe plugin's "override the existing remark" option):
 * jQuery UI Sortable's drag helper is a clone of the whole task card, radio inputs included, carrying the
 * very same {@code name} attribute as the live form fields. While the clone follows the pointer, the
 * browser enforces the "only one checked radio per name" rule across both copies and unchecks the real
 * one, so neither Yes nor No stays selected: the parameter is submitted as {@code null}, which is illegal,
 * and saving the process fails with a 500 error.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("functional")
@DisplayName("Process Task Reorder Functional Tests (issue #440)")
public class ProcessTaskReorderFunctionalTest {

    private static final String ADMIN_USERNAME = "admin";

    private static final String ADMIN_PASSWORD = "motdepasse21";

    private static final String APPLICATION_URL = "http://127.0.0.1:8080/extract";

    private static final String APPLICATION_TITLE = "Extract";

    private static final String MARKER = "ZZ440";

    private static final String REMARK_PLUGIN_CODE = "REMARK";

    @Autowired
    private ProcessesRepository processesRepository;

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private UsersRepository usersRepository;

    private WebDriver driver;

    private Integer seededProcessId;



    @BeforeAll
    public static void setUpClass() {
        WebDriverManager.chromedriver().setup();
    }



    @BeforeEach
    public void setUp() {
        this.seedProcessWithRemarkTask();

        ChromeOptions options = new ChromeOptions();
        options.addArguments(List.of("--disable-gpu", "--window-size=1920,1200",
                                     "--ignore-certificate-errors", "--disable-extensions", "--no-sandbox",
                                     "--disable-dev-shm-usage", "--headless", "--remote-allow-origins=*",
                                     "--disable-logging", "--log-level=OFF"));

        this.driver = new ChromeDriver(options);
        this.driver.manage().timeouts().implicitlyWait(Duration.of(10, ChronoUnit.SECONDS));

        this.waitForTheApplicationToBeDeployed();
        new LoginPage(this.driver).loginAs(ProcessTaskReorderFunctionalTest.ADMIN_USERNAME,
                                           ProcessTaskReorderFunctionalTest.ADMIN_PASSWORD);
    }



    /**
     * Opens the application, waiting for it to answer.
     *
     * The build repackages the WAR that the application server deploys, so the application can still be
     * redeploying when the test starts, and it then answers a 404. Rather than assume it is up, the page
     * is requested again until it is served.
     */
    private void waitForTheApplicationToBeDeployed() {
        new WebDriverWait(this.driver, Duration.of(90, ChronoUnit.SECONDS))
                .pollingEvery(Duration.of(2, ChronoUnit.SECONDS))
                .ignoring(WebDriverException.class)
                .until(webDriver -> {
                    webDriver.get(ProcessTaskReorderFunctionalTest.APPLICATION_URL);

                    return ProcessTaskReorderFunctionalTest.APPLICATION_TITLE.equals(webDriver.getTitle());
                });
    }



    @AfterEach
    public void tearDown() {
        if (this.driver != null) {
            this.driver.quit();
        }

        if (this.seededProcessId != null) {
            this.processesRepository.deleteById(this.seededProcessId);
        }
    }



    @Test
    @DisplayName("A boolean task parameter keeps its value after the task is dragged")
    public void booleanTaskParameterSurvivesDragAndDropReorder() {
        this.driver.get(String.format("%s/processes/%d", ProcessTaskReorderFunctionalTest.APPLICATION_URL,
                                      this.seededProcessId));

        WebElement noRadio = this.driver.findElement(By.cssSelector("input[type='radio'][value='false']"));
        assertTrue(noRadio.isSelected(),
                  "The seeded task must start with \"No\" selected for its boolean parameter.");

        WebElement dragHandle = this.driver.findElement(
                By.cssSelector(".extract-proc-tasks .taskcard .card-header"));
        new Actions(this.driver)
                .clickAndHold(dragHandle)
                .moveByOffset(0, 40)
                .pause(Duration.ofMillis(150))
                .moveByOffset(0, -40)
                .pause(Duration.ofMillis(150))
                .release()
                .perform();

        noRadio = this.driver.findElement(By.cssSelector("input[type='radio'][value='false']"));
        assertTrue(noRadio.isSelected(),
                  "Dragging the task must not clear the selection of its boolean parameter.");

        this.driver.findElement(By.id("processSaveButton")).click();

        new WebDriverWait(this.driver, Duration.ofSeconds(10))
                .until(webDriver -> !webDriver.getCurrentUrl().endsWith("/" + this.seededProcessId)
                        || "Extract".equals(webDriver.getTitle()));

        Task reloadedTask = this.tasksRepository.findByProcessOrderByPosition(
                this.processesRepository.findById(this.seededProcessId).orElseThrow())[0];

        assertEquals("false", reloadedTask.getParametersValues().get("overwrite"),
                    "The boolean parameter must still be \"false\" after the reorder and the save, not null.");
    }



    /**
     * Seeds a process with a single Remarque fixe task, so that its boolean parameter can be dragged.
     */
    private void seedProcessWithRemarkTask() {
        User admin = this.usersRepository.findById(2).orElseThrow();

        Process process = new Process();
        process.setName(ProcessTaskReorderFunctionalTest.MARKER + " process");
        process.setUsersCollection(List.of(admin));
        process.setUserGroupsCollection(new ArrayList<>());
        process.setTasksCollection(new ArrayList<>());
        process = this.processesRepository.save(process);
        this.seededProcessId = process.getId();

        Task task = new Task();
        task.setCode(ProcessTaskReorderFunctionalTest.REMARK_PLUGIN_CODE);
        task.setLabel("Remarque fixe");
        task.setPosition(1);
        task.setProcess(process);

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("remark", ProcessTaskReorderFunctionalTest.MARKER + " remark");
        parameters.put("overwrite", "false");
        task.setParametersValues(parameters);

        this.tasksRepository.save(task);
    }
}
