package ch.asit_asso.extract.authentication.twofactor;

import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.TwoFactorStatus;
import ch.asit_asso.extract.utils.Secrets;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwoFactorApplication {

    private final Logger logger = LoggerFactory.getLogger(TwoFactorApplication.class);

    private final Secrets secrets;

    private final TwoFactorService service;

    private final User user;

    private enum TokenType {
        ACTIVE,
        STANDBY
    }


    public TwoFactorApplication(@NotNull User user, @NotNull Secrets secrets,
                                @NotNull TwoFactorService twoFactorService) {
        this.secrets = secrets;
        this.service = twoFactorService;
        this.user = user;
    }



    public boolean authenticate(@NotNull String code) {

        return this.service.check(this.getToken(TokenType.ACTIVE), code);
    }



    public TwoFactorStatus cancelEnabling() {
        this.user.setTwoFactorStandbyToken(null);
        TwoFactorStatus newStatus;

        if (this.user.getTwoFactorToken() == null) {
            this.logger.debug("2FA registration canceled. No active 2FA token for the user so 2FA status returned to INACTIVE.");
            newStatus = TwoFactorStatus.INACTIVE;

        } else {
            this.logger.debug("2FA registration canceled. There is an active 2FA token for the user so 2FA status returned to ACTIVE.");
            newStatus = TwoFactorStatus.ACTIVE;
        }

        this.user.setTwoFactorStatus(newStatus);
        return newStatus;
    }


    public void disable() {
        assert this.user.getTwoFactorStatus() != TwoFactorStatus.INACTIVE
                : "Two-factor authentication must be enabled before disabling it.";

        this.user.setTwoFactorStatus(TwoFactorStatus.INACTIVE);
        this.user.setTwoFactorToken(null);
        this.user.setTwoFactorStandbyToken(null);
        this.user.setTwoFactorForced(false);
    }



    public void enable() {
        assert this.user.getTwoFactorStatus() == TwoFactorStatus.INACTIVE
                : "Can only enable two-factor authentication if it isn't already active";

        this.user.setTwoFactorStatus(TwoFactorStatus.STANDBY);
        String standbyToken = TimeBasedOneTimePasswordUtil.generateBase32Secret();
        String encryptedStandbyToken = this.secrets.encrypt(standbyToken);
        this.user.setTwoFactorStandbyToken(encryptedStandbyToken);
    }



    public String getQrCodeUrl() {

        return TimeBasedOneTimePasswordUtil.qrImageUrl(String.format("Extract:%s", this.user.getLogin()),
                                                       this.getToken(TokenType.STANDBY));
    }



    public String getStandbyToken() {

        return this.getToken(TokenType.STANDBY);
    }



    public boolean validateRegistration(@NotNull String code) {

        if (!this.service.check(this.getToken(TokenType.STANDBY), code)) {
            return false;
        }

        this.user.setTwoFactorToken(this.user.getTwoFactorStandbyToken());
        this.user.setTwoFactorStandbyToken(null);
        this.user.setTwoFactorStatus(TwoFactorStatus.ACTIVE);

        return true;
    }



    private String getToken(TokenType secretType) {
        this.logger.debug("Getting {} token for user {}", secretType.name(), this.user.getLogin());
        String encryptedToken = (secretType == TokenType.STANDBY) ? this.user.getTwoFactorStandbyToken()
                : this.user.getTwoFactorToken();
        return this.secrets.decrypt(encryptedToken);
    }
}
