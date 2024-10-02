package ch.asit_asso.extract.web.controllers;

import ch.asit_asso.extract.services.AdminUserBuilder;
import ch.asit_asso.extract.services.AppInitializationService;
import ch.asit_asso.extract.services.UserService;
import ch.asit_asso.extract.web.model.SetupModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class SetupController {

    /**
     * Default name of the view
     */
    private static final String DEFAULT_VIEW_NAME = "setup/index";

    /**
     * Default redirection URL after successful user creation
     */
    private static final String DEFAULT_REDIRECT_URL = "redirect:/login";

    /**
     * The local logger
     */
    private final Logger logger = LoggerFactory.getLogger(SetupController.class);

    /**
     * Repository that created
     */
    private final UserService userService;

    /**
     * The service that indicates that the admin account was created
     */
    private final AppInitializationService appInitializationService;


    /**
     * The class constructor
     * @param userService The user service
     * @param appInitializationService The application service
     */
    public SetupController(final UserService userService, AppInitializationService appInitializationService) {
        this.userService = userService;
        this.appInitializationService = appInitializationService;
    }

    /**
     * Handles the GET request
     * @param model The model
     * @return A model and view object
     */
    @GetMapping("/setup")
    public ModelAndView showSetupPage(Model model) {
        denyUnlessAdminUserIsNotCreated();
        ModelAndView modelAndView = new ModelAndView(DEFAULT_VIEW_NAME);
        modelAndView.addObject("model", new SetupModel("admin"));
        return modelAndView;
    }

    /**
     * Handles the POST request
     * @param model The model
     * @param setupModel The object
     * @param bindingResult The error list
     * @return A view
     */
    @PostMapping("/setup")
    public String handleSetup(Model model, @Valid @ModelAttribute("model") SetupModel setupModel, BindingResult bindingResult) {
        denyUnlessAdminUserIsNotCreated();
        if (bindingResult.hasErrors()) {
            model.addAttribute("model", setupModel);
            return DEFAULT_VIEW_NAME;
        }
        handleUserCreation(setupModel);
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Creates the administrator user
     * @param setupModel The data model
     */
    private void handleUserCreation(SetupModel setupModel) {
        userService.create(AdminUserBuilder.create()
                .name(setupModel.getName())
                .withLogin(setupModel.getLogin())
                .password(setupModel.getPassword1())
                .email(setupModel.getEmail())
                .build());
        logger.info("Admin user {} was created", setupModel.getName());
    }

    /**
     * Throws an exception if an admin user is already created
     */
    private void denyUnlessAdminUserIsNotCreated() {
        if (appInitializationService.isConfigured()) {
            throw new SecurityException("This page cannot be accessed !");
        }
    }
}
