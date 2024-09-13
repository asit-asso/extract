package ch.asit_asso.extract.web.controllers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import ch.asit_asso.extract.authentication.ApplicationUser;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorApplication;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorAuthentication;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorAuthenticationHandler;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorBackupCodes;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorRememberMe;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorService;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.RecoveryCodeRepository;
import ch.asit_asso.extract.persistence.RememberMeTokenRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.utils.Secrets;
import ch.asit_asso.extract.utils.UrlUtils;
import ch.asit_asso.extract.web.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Scope("session")
@RequestMapping("/2fa")
public class TwoFactorController extends BaseController {

    private static final String AUTHENTICATE_VIEW = "2fa/authenticate";

    private static final String CONFIRMATION_VIEW = "2fa/confirm";

    private static final String RECOVERY_VIEW = "2fa/recovery";

    private static final String REDIRECT_TO_AUTHENTICATE_VIEW = "redirect:/2fa/authenticate";

    private static final String REDIRECT_TO_CONFIRMATION_VIEW = "redirect:/2fa/confirm";

    private static final String REDIRECT_TO_REGISTER_VIEW = "redirect:/2fa/register";

    private static final String REDIRECT_TO_USERS_LIST = "redirect:/users";

    private static final String REGISTER_VIEW = "2fa/register";

    private final String applicationPath;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(TwoFactorController.class);

    private final TwoFactorService twoFactorService;

    private final AuthenticationFailureHandler failureHandler;

    private final Secrets secrets;

    private final RecoveryCodeRepository recoveryCodeRepository;

    private final RememberMeTokenRepository rememberMeRepository;

    private final UsersRepository usersRepository;

    public TwoFactorController(TwoFactorService service, Secrets secrets,
                               RecoveryCodeRepository recoveryCodeRepository, UsersRepository usersRepository,
                               RememberMeTokenRepository rememberMeTokenRepository,
                               AuthenticationFailureHandler failureHandler, Environment environment) {

        this.twoFactorService = service;
        this.failureHandler = failureHandler;
        this.secrets = secrets;
        this.recoveryCodeRepository = recoveryCodeRepository;
        this.rememberMeRepository = rememberMeTokenRepository;
        this.usersRepository = usersRepository;
        this.applicationPath = UrlUtils.getApplicationPath(environment.getProperty("application.external.url"));
    }

    @GetMapping("authenticate")
    public String requestTwoFactorAuthentication(Authentication authentication) {

        if (!(authentication instanceof TwoFactorAuthentication)) {
            this.logger.warn("User {} requested the 2FA authentication page while being already authenticated.", authentication.getName());
            //TODO : Better error management
            throw new IllegalStateException("Cannot display the recovery page for an authenticated user");
        }

        if (authentication.getPrincipal() instanceof ApplicationUser user) {

            if (user.getTwoFactorStatus() != User.TwoFactorStatus.ACTIVE) {
                this.logger.warn("User {} requested the 2FA authentication page while 2FA not being active for them.", user.getUsername());
                //TODO : Better error management
                throw new IllegalStateException("The user's 2FA status is not valid for the authentication page.");
            }

            this.logger.debug("Displaying the 2FA authentication page for user {}.", user.getUsername());
            return TwoFactorController.AUTHENTICATE_VIEW;
        }

        this.logger.warn("User {} requested the 2FA authentication page while not being an application user.", authentication.getName());
        //TODO : Better error management
        throw new IllegalStateException("TOTP authentication without prior login.");
    }

    @PostMapping("authenticate")
    public void processTwoFactorAuthentication(@RequestParam("code") String code,
                                               @RequestParam(name = "rememberMe", required = false) String rememberMe,
                                               TwoFactorAuthentication authentication, HttpServletRequest request,
                                               HttpServletResponse response) throws Exception {

        if (authentication == null) {
            //TODO : Better error management
            throw new IllegalStateException("Cannot display the recovery page for an authenticated user");
        }

        if (authentication.getPrincipal() instanceof ApplicationUser user) {

            if (user.getTwoFactorStatus() != User.TwoFactorStatus.ACTIVE) {
                //TODO : Better error management
                throw new IllegalStateException("The user's 2FA status is not valid for the recovery page.");
            }

            User domainUser = this.usersRepository.findById(user.getUserId()).orElseThrow();
            TwoFactorAuthenticationHandler handler = new TwoFactorAuthenticationHandler(null);
            TwoFactorApplication twoFactorApplication
                    = new TwoFactorApplication(domainUser, this.secrets, this.twoFactorService);

            if (twoFactorApplication.authenticate(code)) {

                if (rememberMe != null) {
                    this.logger.debug("The user asked to be remembered for 2FA authentication for 30 days.");
                    TwoFactorRememberMe rememberMeUser = new TwoFactorRememberMe(domainUser, this.rememberMeRepository,
                                                                                 this.secrets, this.applicationPath);
                    rememberMeUser.enable(response);
                }

                SecurityContextHolder.getContext().setAuthentication(authentication.getFirst());
                handler.onAuthenticationSuccess(request, response, authentication.getFirst());

            } else {
                this.failureHandler.onAuthenticationFailure(request, response,
                                                            new BadCredentialsException("bad credentials"));
            }

        } else {
            //TODO : Better error management
            throw new IllegalStateException("TOTP authentication without prior login.");
        }
    }

    @GetMapping("recovery")
    public String requestRecovery(Authentication authentication) {

        if (!(authentication instanceof TwoFactorAuthentication)) {
            //TODO : Better error management
            throw new IllegalStateException("Cannot display the recovery page for an authenticated user");
        }

        if (authentication.getPrincipal() instanceof ApplicationUser user) {

            if (user.getTwoFactorStatus() != User.TwoFactorStatus.ACTIVE) {
                //TODO : Better error management
                throw new IllegalStateException("The user's 2FA status is not valid for the recovery page.");
            }

            return TwoFactorController.RECOVERY_VIEW;
        }

        //TODO : Better error management
        throw new IllegalStateException("Cannot display the recovery page before the user logged.");
    }

    @PostMapping("recovery")
    public void processRecovery(@RequestParam("code") String code, TwoFactorAuthentication authentication,
                                   HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (authentication == null) {
            //TODO : Better error management
            throw new IllegalStateException("Cannot display the recovery page for an authenticated user");
        }

        if (authentication.getPrincipal() instanceof ApplicationUser user) {

            if (user.getTwoFactorStatus() != User.TwoFactorStatus.ACTIVE) {
                //TODO : Better error management
                throw new IllegalStateException("The user's 2FA status is not valid for the recovery page.");
            }

            User domainUser = this.usersRepository.findById(user.getUserId()).orElseThrow();
            TwoFactorAuthenticationHandler handler = new TwoFactorAuthenticationHandler(null);
            TwoFactorBackupCodes userBackupCodes = new TwoFactorBackupCodes(domainUser, this.recoveryCodeRepository,
                                                                            this.secrets);

            if (userBackupCodes.submitCode(code)) {
                handler.onAuthenticationSuccess(request, response, authentication.getFirst());

            } else {
                this.failureHandler.onAuthenticationFailure(request, response,
                                                            new BadCredentialsException("bad credentials"));
            }

        } else {
            //TODO : Better error management
            throw new IllegalStateException("Cannot display the recovery page before the user logged.");
        }
    }


    @GetMapping("register")
    public String requestRegister(ModelMap model, Authentication authentication, HttpServletRequest request) {

        if (authentication.getPrincipal() instanceof ApplicationUser applicationUser) {

            if (!this.isRegistrationAllowed(request)) {
                this.logger.warn("User {} attempted to display the 2FA registration wizard outside of the allowed workflow.",
                                 applicationUser.getUsername());
                return BaseController.REDIRECT_TO_ACCESS_DENIED;
            }

            this.logger.debug("Application user is {} (ID: {})", applicationUser.getUsername(),
                              applicationUser.getUserId());
            User user = this.usersRepository.findById(applicationUser.getUserId()).orElseThrow();
            this.logger.debug("Domain user is {} (ID: {})", user.getLogin(),
                              user.getId());

            if (user.getTwoFactorStatus() != User.TwoFactorStatus.STANDBY) {
                //TODO : Better error management
                throw new IllegalStateException("The user's 2FA status is invalid for the registration wizard.");
            }

            TwoFactorApplication twoFactorApplicationUser
                    = new TwoFactorApplication(user, this.secrets, this.twoFactorService);
            model.addAttribute("isForced", user.isTwoFactorForced());
            model.addAttribute("token", twoFactorApplicationUser.getStandbyToken());
            model.addAttribute("qrCodeUrl", twoFactorApplicationUser.getQrCodeUrl());

            return TwoFactorController.REGISTER_VIEW;
        }

        //TODO : Better error management
        return null;
    }


    @PostMapping("register")
    @Transactional
    public String processRegister(@RequestParam("registrationCode") String code, Authentication authentication,
                                   HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

        if (authentication.getPrincipal() instanceof ApplicationUser currentApplicationUser) {

            if (!this.isRegistrationAllowed(request)) {
                this.logger.warn("User {} attempted to submit a 2FA registration outside of the allowed workflow.",
                                 currentApplicationUser.getUsername());
                return BaseController.REDIRECT_TO_ACCESS_DENIED;
            }

            User currentUser = this.usersRepository.findById(currentApplicationUser.getUserId()).orElseThrow();

            if (currentUser.getTwoFactorStatus() != User.TwoFactorStatus.STANDBY) {
                this.logger.warn("The user {} submitted 2FA registration while their status was not in standby ({}).",
                                 currentUser.getLogin(), currentUser.getTwoFactorStatus().name());
                this.processRegistrationStep(false, request);
                return BaseController.REDIRECT_TO_ACCESS_DENIED;
            }

            TwoFactorApplication twoFactorApplication
                    = new TwoFactorApplication(currentUser, this.secrets, this.twoFactorService);

            if (!twoFactorApplication.validateRegistration(code)) {

                if (!this.isCurrentUserAuthenticated()) {
                    this.failureHandler.onAuthenticationFailure(request, response,
                                                                new BadCredentialsException("bad credentials"));
                    this.processRegistrationStep(false, request);
                    return BaseController.REDIRECT_TO_ACCESS_DENIED;

                } else {
                    this.addStatusMessage(model, "userDetails.errors.user.update.failed", Message.MessageType.ERROR);

                    return TwoFactorController.REDIRECT_TO_REGISTER_VIEW;
                }
            }

            User registeredUser = this.usersRepository.save(currentUser);
            TwoFactorRememberMe rememberMeUser = new TwoFactorRememberMe(registeredUser, this.rememberMeRepository,
                                                                         this.secrets, this.applicationPath);
            rememberMeUser.disable(request, response);
            this.processRegistrationStep(true, request);

            return TwoFactorController.REDIRECT_TO_CONFIRMATION_VIEW;
        }

        this.logger.warn("2FA registration data submitted by a user not authenticated through the login form.");
        return BaseController.REDIRECT_TO_ACCESS_DENIED;
    }



    @PostMapping("cancelRegistration")
    public String processRegistrationCancellation(Authentication authentication, HttpServletRequest request,
                                                  HttpServletResponse response) throws ServletException, IOException {

        this.logger.debug("Registration cancellation requested");

        if (authentication.getPrincipal() instanceof ApplicationUser currentApplicationUser) {

            if (!this.isRegistrationAllowed(request)) {
                this.logger.warn("User {} attempted to submit a 2FA registration outside of the allowed workflow.",
                                 currentApplicationUser.getUsername());
                return BaseController.REDIRECT_TO_ACCESS_DENIED;
            }

            this.logger.debug("Registration cancellation was requested by user {}", currentApplicationUser.getUserId());
            User domainUser = this.usersRepository.findById(currentApplicationUser.getUserId()).orElseThrow();

            if (domainUser.getTwoFactorStatus() != User.TwoFactorStatus.STANDBY) {
                this.logger.warn("The user {} cancelled 2FA registration while their status was not in standby ({}).",
                                 domainUser.getLogin(), domainUser.getTwoFactorStatus().name());
                return BaseController.REDIRECT_TO_ACCESS_DENIED;
            }

            if (domainUser.isTwoFactorForced()) {
                this.logger.warn("User {} attempted to cancel their 2FA registration that was required by an administrator. Cancellation rejected.",
                                 currentApplicationUser.getUserId());
                return BaseController.REDIRECT_TO_ACCESS_DENIED;
            }

            TwoFactorApplication twoFactorApplication = new TwoFactorApplication(domainUser, this.secrets,
                                                                                 this.twoFactorService);
            User.TwoFactorStatus newStatus = twoFactorApplication.cancelEnabling();
            domainUser = this.usersRepository.save(domainUser);
            this.processRegistrationStep(false, request);

            HttpSession currentSession = request.getSession();


            if ("AUTHENTICATION".equals(currentSession.getAttribute("2faProcess"))) {
                currentSession.removeAttribute("2faProcess");

                String nextUrl;
                Authentication baseAuthentication;

                if (newStatus == User.TwoFactorStatus.INACTIVE) {
                    nextUrl = null;
                    baseAuthentication = ((TwoFactorAuthentication) authentication).getFirst();
                } else {
                    nextUrl = String.format("/%s", TwoFactorController.AUTHENTICATE_VIEW);
                    ApplicationUser refreshedUser = new ApplicationUser(domainUser);
                    baseAuthentication = new UsernamePasswordAuthenticationToken(refreshedUser, domainUser.getPassword(),
                                                                                 refreshedUser.getAuthorities());
                    this.logger.debug("Refreshed user authorities : [{}]", String.join(", ",
                              baseAuthentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()));
                }

                TwoFactorAuthenticationHandler handler = new TwoFactorAuthenticationHandler(nextUrl);
                SecurityContextHolder.getContext().setAuthentication(baseAuthentication);
                handler.onAuthenticationSuccess(request, response, baseAuthentication);

                return nextUrl;
            }

            return (this.isCurrentUserAdmin()) ? TwoFactorController.REDIRECT_TO_USERS_LIST
                                               : BaseController.REDIRECT_TO_HOME;
        }

        this.logger.warn("Cancellation of the 2FA registration submitted by a user that wasn't authenticated with the login form.");

        return BaseController.REDIRECT_TO_ACCESS_DENIED;
    }



    @GetMapping("confirm")
    public String requestConfirmation(ModelMap model, Authentication authentication, HttpServletRequest request) {
        ApplicationUser currentApplicationUser = (ApplicationUser) authentication.getPrincipal();

        if (!this.isConfirmationAllowed(request)) {
            this.logger.warn("User {} attempted to display the 2FA registration confirmation outside of the allowed workflow.",
                             currentApplicationUser.getUsername());
            return BaseController.REDIRECT_TO_ACCESS_DENIED;
        }

        User currentUser = this.usersRepository.findById(currentApplicationUser.getUserId()).orElseThrow();
        TwoFactorBackupCodes userBackupCodes = new TwoFactorBackupCodes(currentUser, this.recoveryCodeRepository,
                                                                        this.secrets);
        String[] backupCodes = userBackupCodes.generate();
        String fileData = userBackupCodes.toFileData();

        model.addAttribute("backupCodes", backupCodes);
        model.addAttribute("backupCodesFileData", fileData);

        return TwoFactorController.CONFIRMATION_VIEW;
    }


    @PostMapping("confirm")
    public String processConfirmation(Authentication authentication, HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        if (!this.isConfirmationAllowed(request)) {
            this.logger.warn("User {} attempted to submit a 2FA confirmation outside of the allowed workflow.",
                             this.getCurrentUserLogin());
            return BaseController.REDIRECT_TO_ACCESS_DENIED;
        }

        HttpSession currentSession = request.getSession();
        this.processConfirmationStep(request);

        if ("AUTHENTICATION".equals(currentSession.getAttribute("2faProcess"))) {
            //if (this.isCurrentUserApplicationUser() && this.isCurrentUserAuthenticated()) {
            new TwoFactorAuthenticationHandler(null)
                    .onAuthenticationSuccess(request, response, ((TwoFactorAuthentication) authentication).getFirst());

            return BaseController.REDIRECT_TO_HOME;
        }

        return (this.isCurrentUserAdmin()) ? TwoFactorController.REDIRECT_TO_USERS_LIST
                                           : BaseController.REDIRECT_TO_HOME;
    }



    private void processRegistrationStep(boolean success, HttpServletRequest request) {

        if (success) {
            request.getSession().setAttribute("2faStep", "CONFIRM");
        } else {
            request.getSession().removeAttribute("2faStep");
        }
    }



    private void processConfirmationStep(HttpServletRequest request) {
        request.getSession().removeAttribute("2faStep");
    }



    private boolean isConfirmationAllowed(HttpServletRequest request) {
        String twoFactorStep = (String) request.getSession().getAttribute("2faStep");

        return "CONFIRM".equals(twoFactorStep);
    }


    private boolean isRegistrationAllowed(HttpServletRequest request) {
        String twoFactorStep = (String) request.getSession().getAttribute("2faStep");

        return "REGISTER".equals(twoFactorStep);
    }
}
