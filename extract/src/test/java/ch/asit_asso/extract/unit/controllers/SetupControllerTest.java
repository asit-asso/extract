package ch.asit_asso.extract.unit.controllers;

import ch.asit_asso.extract.services.UserService;
import ch.asit_asso.extract.unit.MockEnabledTest;
import ch.asit_asso.extract.web.controllers.SetupController;
import ch.asit_asso.extract.web.model.SetupModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;


class SetupControllerTest extends MockEnabledTest {
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private SetupController setupController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(setupController).build();
    }

    @Test
    void testShowSetupPage() throws Exception {
        MvcResult result = mockMvc.perform(get("/setup"))
                .andExpect(status().isOk())
                .andExpect(view().name("setup/index"))
                .andExpect(model().attributeExists("model"))
                .andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();
        SetupModel setupModel = (SetupModel) model.get("model");
        assertThat(setupModel).isNotNull();
        assertThat(setupModel.getLogin()).isEqualTo("admin");
    }

    @Test
    void testHandleSetupWithErrors() throws Exception {
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
    void testHandleSetupWithoutErrors() throws Exception {
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
}