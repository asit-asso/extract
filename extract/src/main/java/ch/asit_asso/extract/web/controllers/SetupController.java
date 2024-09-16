package ch.asit_asso.extract.web.controllers;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.services.AdminUserBuilder;
import ch.asit_asso.extract.services.AppInitializationService;
import ch.asit_asso.extract.services.UserService;
import ch.asit_asso.extract.utils.Secrets;
import ch.asit_asso.extract.web.model.SetupModel;
import ch.asit_asso.extract.web.validators.PasswordValidator;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.json.JsonOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Locale;

@Controller
public class SetupController {

    private static final String DEFAULT_VIEW_NAME = "setup/index";
    private static final String DEFAULT_REDIRECT_URL = "redirect:/login";

    private final Logger logger = LoggerFactory.getLogger(SetupController.class);

    /**
     * Repository that created
     */
    private final UserService userService;


    public SetupController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/setup")
    public ModelAndView showSetupPage(Model model) {
        ModelAndView modelAndView = new ModelAndView(DEFAULT_VIEW_NAME);
        modelAndView.addObject("model", new SetupModel("admin"));
        return modelAndView;
    }

    @PostMapping("/setup")
    public String handleSetup(Model model, @Valid @ModelAttribute("model") SetupModel setupModel, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("model", setupModel);
            return DEFAULT_VIEW_NAME;
        }
        handleUserCreation(setupModel);
        return DEFAULT_REDIRECT_URL;
    }

    private void handleUserCreation(SetupModel setupModel) {
        userService.create(AdminUserBuilder.create()
                .name(setupModel.getName())
                .withLogin(setupModel.getLogin())
                .password(setupModel.getPassword1())
                .email(setupModel.getEmail())
                .build());
        logger.info("Admin user {} was created", setupModel.getName());
    }
}
