package ch.asit_asso.extract.unit.controllers;

import ch.asit_asso.extract.unit.MockEnabledTest;
import ch.asit_asso.extract.web.controllers.BaseController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@DisplayName("BaseController language @ModelAttribute")
class BaseControllerLanguageTest extends MockEnabledTest {

    private MockMvc mockMvc;
    private TestableController controller;
    private LocaleResolver localeResolver;

    @RestController
    @RequestMapping("/test")
    static class TestableController extends BaseController {

        @GetMapping
        public String index() {
            return "test/index";
        }
    }

    @BeforeEach
    void setUp() {
        controller = new TestableController();
        localeResolver = new LocaleResolver() {
            private Locale locale = Locale.FRENCH;

            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                return locale;
            }

            @Override
            public void setLocale(HttpServletRequest request, javax.servlet.http.HttpServletResponse response,
                                  Locale locale) {
                this.locale = locale;
            }
        };
    }

    private void setupController(String languages, LocaleResolver resolver) {
        ReflectionTestUtils.setField(controller, "applicationLanguage", languages);
        ReflectionTestUtils.setField(controller, "localeResolver", resolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Nested
    @DisplayName("When user locale is German")
    class GermanLocale {

        @BeforeEach
        void setUp() {
            localeResolver = new LocaleResolver() {
                @Override
                public Locale resolveLocale(HttpServletRequest request) {
                    return Locale.GERMAN;
                }

                @Override
                public void setLocale(HttpServletRequest request, javax.servlet.http.HttpServletResponse response,
                                      Locale locale) {
                }
            };
        }

        @Test
        @DisplayName("language model attribute should be 'de'")
        void shouldReturnGerman() throws Exception {
            setupController("de,fr,en", localeResolver);

            mockMvc.perform(get("/test"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("language", "de"));
        }
    }

    @Nested
    @DisplayName("When user locale is English")
    class EnglishLocale {

        @BeforeEach
        void setUp() {
            localeResolver = new LocaleResolver() {
                @Override
                public Locale resolveLocale(HttpServletRequest request) {
                    return Locale.ENGLISH;
                }

                @Override
                public void setLocale(HttpServletRequest request, javax.servlet.http.HttpServletResponse response,
                                      Locale locale) {
                }
            };
        }

        @Test
        @DisplayName("language model attribute should be 'en'")
        void shouldReturnEnglish() throws Exception {
            setupController("de,fr,en", localeResolver);

            mockMvc.perform(get("/test"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("language", "en"));
        }
    }

    @Nested
    @DisplayName("When user locale is French")
    class FrenchLocale {

        @Test
        @DisplayName("language model attribute should be 'fr'")
        void shouldReturnFrench() throws Exception {
            setupController("de,fr,en", localeResolver);

            mockMvc.perform(get("/test"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("language", "fr"));
        }
    }

    @Nested
    @DisplayName("When locale is unsupported")
    class UnsupportedLocale {

        @BeforeEach
        void setUp() {
            localeResolver = new LocaleResolver() {
                @Override
                public Locale resolveLocale(HttpServletRequest request) {
                    return Locale.JAPANESE;
                }

                @Override
                public void setLocale(HttpServletRequest request, javax.servlet.http.HttpServletResponse response,
                                      Locale locale) {
                }
            };
        }

        @Test
        @DisplayName("should fall back to default language")
        void shouldFallbackToDefault() throws Exception {
            setupController("de,fr,en", localeResolver);

            mockMvc.perform(get("/test"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("language", "de"));
        }
    }

    @Nested
    @DisplayName("When no locale resolver is available")
    class NoLocaleResolver {

        @Test
        @DisplayName("should fall back to default language")
        void shouldFallbackToDefault() throws Exception {
            setupController("fr,de,en", null);

            mockMvc.perform(get("/test"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("language", "fr"));
        }
    }
}
