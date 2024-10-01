package ch.asit_asso.extract.unit.controllers;

import ch.asit_asso.extract.services.AppInitializationService;
import ch.asit_asso.extract.services.UserService;
import ch.asit_asso.extract.unit.MockEnabledTest;
import ch.asit_asso.extract.web.controllers.SetupController;
import ch.asit_asso.extract.web.model.SetupModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class SetupControllerTest extends MockEnabledTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private AppInitializationService appInitializationService;

    @InjectMocks
    private SetupController setupController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(setupController).build();
    }

    @Test
    @DisplayName("Tests that the page displays correctly when application is not initialized")
    void testShowSetupPage() throws Exception {

        when(appInitializationService.isConfigured()).thenReturn(false);
        MvcResult result = mockMvc.perform(get("/setup"))
                .andExpect(status().isOk())
                .andExpect(view().name("setup/index"))
                .andExpect(model().attributeExists("model"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        SetupModel setupModel = (SetupModel) model.get("model");
        assertThat(setupModel).isNotNull();
        assertThat(setupModel.getLogin()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Tests that the submission controller handles errors gracefully")
    void testHandleSetupWithErrors() throws Exception {
        when(appInitializationService.isConfigured()).thenReturn(false);
        SetupModel setupModel = new SetupModel("admin");
        setupModel.setLogin("admin");
        setupModel.setPassword1("password");
        setupModel.setEmail("admin@example.com");

        mockMvc.perform(post("/setup")
                        .flashAttr("model", setupModel))
                .andExpect(model().hasErrors())
                .andExpect(status().isOk())
                .andExpect(view().name("setup/index"));
    }

    @Test
    @DisplayName("Test that the submission controller works fine with correct arguments")
    void testHandleSetupWithoutErrors() throws Exception {
        when(appInitializationService.isConfigured()).thenReturn(false);
        SetupModel setupModel = new SetupModel("admin");
        setupModel.setLogin("admin");
        setupModel.setEmail("admin@test.com");
        setupModel.setName("Admin User");
        setupModel.setPassword1("$Pas$word21!");
        setupModel.setPassword2("$Pas$word21!");
        mockMvc.perform(post("/setup")
                        .flashAttr("model", setupModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService, times(1)).create(any());
    }

    @Test
    @DisplayName("Test that the setup page cannot be accessed once the admin is created")
    void testShowSetupPageWithExistingAdmin() throws Exception {
        when(appInitializationService.isConfigured()).thenReturn(true);

        try {
            mockMvc.perform(get("/setup"))
                    .andExpect(status().is4xxClientError());
        }
        catch (Exception e) {
            assertInstanceOf(SecurityException.class, e.getCause());
            assertEquals("This page cannot be accessed !", e.getCause().getMessage());
        }
    }

    @Test
    @DisplayName("Test that the setup submission endpoint cannot be accessed once the admin is created")
    void testHandleSetupWithExistingAdmin() throws Exception {
        when(appInitializationService.isConfigured()).thenReturn(true);
        SetupModel setupModel = new SetupModel("admin");
        setupModel.setLogin("admin");
        setupModel.setEmail("admin@test.com");
        setupModel.setName("Admin User");
        setupModel.setPassword1("$Pas$word21!");
        setupModel.setPassword2("$Pas$word21!");

        try {
            mockMvc.perform(post("/setup")
                            .flashAttr("model", setupModel))
                    .andExpect(status().is4xxClientError());
        }
        catch (Exception e) {
            assertInstanceOf(SecurityException.class, e.getCause());
            assertEquals("This page cannot be accessed !", e.getCause().getMessage());
        }
        verify(userService, never()).create(any());
    }
}