/*
 * Copyright (C) 2026 arx iT
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
package ch.asit_asso.extract.integration.email;

import java.util.GregorianCalendar;
import java.util.Locale;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.email.NewRequestWatcherEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;



/**
 * Ensures that the message sent to the watchers of a process when a new request is attached to it
 * carries the expected information (issue #359).
 *
 * @author Extract
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("New request watcher e-mail integration tests (issue #359)")
public class NewRequestWatcherEmailIntegrationTest {

    /**
     * The name given to the process that the request is attached to.
     */
    private static final String PROCESS_NAME = "Traitement observé 359";

    /**
     * The application settings required to build a message.
     */
    @Autowired
    private EmailSettings emailSettings;

    /**
     * The process that the test request is attached to.
     */
    private Process process;

    /**
     * The request whose import must be notified to the watchers.
     */
    private Request request;



    /**
     * Creates the data used by every test.
     */
    @BeforeEach
    public final void setUp() {
        this.process = new Process();
        this.process.setId(4359);
        this.process.setName(NewRequestWatcherEmailIntegrationTest.PROCESS_NAME);

        this.request = new Request();
        this.request.setId(4360);
        this.request.setOrderLabel("CMD-359");
        this.request.setProductLabel("Produit observé");
        this.request.setClient("Client 359");
        this.request.setStatus(Request.Status.ONGOING);
        this.request.setStartDate(new GregorianCalendar());
        this.request.setParameters("{}");
        this.request.setProcess(this.process);
    }



    @Test
    @DisplayName("1. The subject and the body name the watched process")
    public final void theMessageNamesTheWatchedProcess() {
        final NewRequestWatcherEmail message = new NewRequestWatcherEmail(this.emailSettings);

        assertTrue(message.initializeContent(this.request, this.process, Locale.FRENCH),
                "The message content could not be initialized.");

        assertTrue(message.getSubject().contains(NewRequestWatcherEmailIntegrationTest.PROCESS_NAME),
                String.format("The subject does not name the watched process: %s", message.getSubject()));
        assertTrue(message.getContent().contains(NewRequestWatcherEmailIntegrationTest.PROCESS_NAME),
                "The message body does not name the watched process.");
    }



    @Test
    @DisplayName("2. The body links to the details of the imported request")
    public final void theMessageLinksToTheRequest() {
        final NewRequestWatcherEmail message = new NewRequestWatcherEmail(this.emailSettings);
        message.initializeContent(this.request, this.process, Locale.FRENCH);

        assertTrue(message.getContent().contains(String.format("/requests/%d", this.request.getId())),
                "The message body does not link to the details of the imported request.");
    }



    @Test
    @DisplayName("3. The parameterized footer is formatted rather than left with its placeholder")
    public final void theFooterIsFormatted() {
        final NewRequestWatcherEmail message = new NewRequestWatcherEmail(this.emailSettings);
        message.initializeContent(this.request, this.process, Locale.FRENCH);
        final String content = message.getContent();

        assertFalse(content.contains("{0}"),
                "The message body contains an unresolved message format placeholder.");
        assertTrue(content.contains("observateur du traitement"),
                "The message body does not explain why the watcher receives it.");
        assertTrue(content.contains("d&#39;observateur") || content.contains("d'observateur"),
                "The apostrophes of the footer have been swallowed by the message formatter.");
    }



    @Test
    @DisplayName("4. The message is translated in the locale of the watcher")
    public final void theMessageIsTranslated() {
        final NewRequestWatcherEmail frenchMessage = new NewRequestWatcherEmail(this.emailSettings);
        frenchMessage.initializeContent(this.request, this.process, Locale.FRENCH);

        final NewRequestWatcherEmail germanMessage = new NewRequestWatcherEmail(this.emailSettings);
        germanMessage.initializeContent(this.request, this.process, Locale.GERMAN);

        assertTrue(frenchMessage.getSubject().contains("Nouvelle commande"),
                String.format("The French subject is not translated: %s", frenchMessage.getSubject()));
        assertTrue(germanMessage.getSubject().contains("Neue Bestellung"),
                String.format("The German subject is not translated: %s", germanMessage.getSubject()));
    }
}
