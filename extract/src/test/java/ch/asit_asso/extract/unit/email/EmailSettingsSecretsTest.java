package ch.asit_asso.extract.unit.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.testutils.TestSecrets;
import ch.asit_asso.extract.utils.Secrets;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;

@Tag("unit")
class EmailSettingsSecretsTest {

    @Test
    void decryptsPersistedSmtpPassword() {
        SystemParametersRepository repository = mock(SystemParametersRepository.class);
        Secrets secrets = TestSecrets.create();
        when(repository.isEmailNotificationEnabled()).thenReturn("true");
        when(repository.getSmtpServer()).thenReturn("smtp.test.com");
        when(repository.getSmtpPort()).thenReturn("587");
        when(repository.getSmtpFromMail()).thenReturn("noreply@extract.test");
        when(repository.getSmtpFromName()).thenReturn("Extract System");
        when(repository.getSmtpUser()).thenReturn("smtp-user");
        when(repository.getSmtpPassword()).thenReturn(secrets.encrypt("smtp-pass"));
        when(repository.getSmtpSSL()).thenReturn("NONE");

        EmailSettings settings = new EmailSettings(repository, new TemplateEngine(), mock(MessageSource.class),
                "http://localhost:8080", "fr", secrets);

        assertEquals("smtp-pass", settings.getSmtpPassword());
    }
}
