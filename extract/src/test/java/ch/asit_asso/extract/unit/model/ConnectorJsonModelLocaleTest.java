package ch.asit_asso.extract.unit.model;

import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.unit.MockEnabledTest;
import ch.asit_asso.extract.web.model.json.ConnectorJsonModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.MessageSource;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DisplayName("ConnectorJsonModel locale support")
class ConnectorJsonModelLocaleTest extends MockEnabledTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private Connector connector;

    @BeforeEach
    void setUp() {
        when(connector.getId()).thenReturn(1);
        when(connector.getName()).thenReturn("Test Connector");
        when(connector.isInError()).thenReturn(false);
    }

    @Nested
    @DisplayName("No import yet")
    class NoImport {

        @BeforeEach
        void setUp() {
            when(connector.getLastImportDate()).thenReturn(null);
        }

        @Test
        @DisplayName("should use French locale for state message")
        void shouldUseFrenchLocale() {
            when(messageSource.getMessage(eq("requestsList.connectors.noImport"), isNull(), eq(Locale.FRENCH)))
                    .thenReturn("Pas encore d'import");

            ConnectorJsonModel model = new ConnectorJsonModel(connector, messageSource, true, Locale.FRENCH);

            assertThat(model.getStateMessage()).isEqualTo("Pas encore d'import");
            verify(messageSource).getMessage(eq("requestsList.connectors.noImport"), isNull(), eq(Locale.FRENCH));
        }

        @Test
        @DisplayName("should use German locale for state message")
        void shouldUseGermanLocale() {
            when(messageSource.getMessage(eq("requestsList.connectors.noImport"), isNull(), eq(Locale.GERMAN)))
                    .thenReturn("Noch kein Import");

            ConnectorJsonModel model = new ConnectorJsonModel(connector, messageSource, true, Locale.GERMAN);

            assertThat(model.getStateMessage()).isEqualTo("Noch kein Import");
            verify(messageSource).getMessage(eq("requestsList.connectors.noImport"), isNull(), eq(Locale.GERMAN));
        }

        @Test
        @DisplayName("should use English locale for state message")
        void shouldUseEnglishLocale() {
            when(messageSource.getMessage(eq("requestsList.connectors.noImport"), isNull(), eq(Locale.ENGLISH)))
                    .thenReturn("No import yet");

            ConnectorJsonModel model = new ConnectorJsonModel(connector, messageSource, true, Locale.ENGLISH);

            assertThat(model.getStateMessage()).isEqualTo("No import yet");
            verify(messageSource).getMessage(eq("requestsList.connectors.noImport"), isNull(), eq(Locale.ENGLISH));
        }
    }

    @Nested
    @DisplayName("Successful import")
    class SuccessfulImport {

        @BeforeEach
        void setUp() {
            Calendar importDate = new GregorianCalendar(2026, Calendar.MARCH, 10, 14, 30);
            when(connector.getLastImportDate()).thenReturn(importDate);
            when(connector.isInError()).thenReturn(false);
        }

        @Test
        @DisplayName("should use German locale for success message")
        void shouldUseGermanLocale() {
            when(messageSource.getMessage(eq("requestsList.connectors.importSuccess"), any(Object[].class),
                    eq(Locale.GERMAN))).thenReturn("Letzter Import um 14:30");

            ConnectorJsonModel model = new ConnectorJsonModel(connector, messageSource, true, Locale.GERMAN);

            assertThat(model.getStateMessage()).isEqualTo("Letzter Import um 14:30");
        }

        @Test
        @DisplayName("should use French locale for success message")
        void shouldUseFrenchLocale() {
            when(messageSource.getMessage(eq("requestsList.connectors.importSuccess"), any(Object[].class),
                    eq(Locale.FRENCH))).thenReturn("Dernier import à 14:30");

            ConnectorJsonModel model = new ConnectorJsonModel(connector, messageSource, true, Locale.FRENCH);

            assertThat(model.getStateMessage()).isEqualTo("Dernier import à 14:30");
        }
    }

    @Nested
    @DisplayName("Import error")
    class ImportError {

        @BeforeEach
        void setUp() {
            Calendar importDate = new GregorianCalendar(2026, Calendar.MARCH, 10, 14, 30);
            when(connector.getLastImportDate()).thenReturn(importDate);
            when(connector.isInError()).thenReturn(true);
            when(connector.getLastImportMessage()).thenReturn("Connection refused");
        }

        @Test
        @DisplayName("should use German locale for error message")
        void shouldUseGermanLocale() {
            when(messageSource.getMessage(eq("requestsList.connectors.importError"), any(Object[].class),
                    eq(Locale.GERMAN))).thenReturn("Fehler beim Import um 14:30: Connection refused");

            ConnectorJsonModel model = new ConnectorJsonModel(connector, messageSource, true, Locale.GERMAN);

            assertThat(model.getStateMessage()).isEqualTo("Fehler beim Import um 14:30: Connection refused");
        }
    }

    @Nested
    @DisplayName("fromConnectorsArray")
    class FromArray {

        @Test
        @DisplayName("should pass locale to all created models")
        void shouldPassLocaleToAllModels() {
            Connector connector2 = org.mockito.Mockito.mock(Connector.class);
            when(connector2.getId()).thenReturn(2);
            when(connector2.getName()).thenReturn("Connector 2");
            when(connector2.isInError()).thenReturn(false);
            when(connector2.getLastImportDate()).thenReturn(null);

            when(connector.getLastImportDate()).thenReturn(null);

            when(messageSource.getMessage(eq("requestsList.connectors.noImport"), isNull(), eq(Locale.GERMAN)))
                    .thenReturn("Noch kein Import");

            ConnectorJsonModel[] models = ConnectorJsonModel.fromConnectorsArray(
                    new Connector[]{connector, connector2}, messageSource, true, Locale.GERMAN);

            assertThat(models).hasSize(2);
            assertThat(models[0].getStateMessage()).isEqualTo("Noch kein Import");
            assertThat(models[1].getStateMessage()).isEqualTo("Noch kein Import");
        }
    }

    @Nested
    @DisplayName("Null locale fallback")
    class NullLocale {

        @Test
        @DisplayName("should fall back to JVM default locale when locale is null")
        void shouldFallbackToDefault() {
            when(connector.getLastImportDate()).thenReturn(null);
            when(messageSource.getMessage(eq("requestsList.connectors.noImport"), isNull(), eq(Locale.getDefault())))
                    .thenReturn("fallback message");

            ConnectorJsonModel model = new ConnectorJsonModel(connector, messageSource, true, null);

            assertThat(model.getStateMessage()).isEqualTo("fallback message");
        }
    }
}
