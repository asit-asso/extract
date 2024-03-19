package ch.asit_asso.extract.authentication.twofactor;

import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.domain.User;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;

public class TwoFactorApplication {

    private final Logger logger = LoggerFactory.getLogger(TwoFactorApplication.class);

    private final BytesEncryptor encryptor;

    private final TwoFactorService service;

    private final User user;

    private enum TokenType {
        ACTIVE,
        STANDBY
    }


    public TwoFactorApplication(@NotNull User user, @NotNull BytesEncryptor bytesEncryptor,
                                @NotNull TwoFactorService twoFactorService) {
        this.encryptor = bytesEncryptor;
        this.service = twoFactorService;
        this.user = user;
    }



    public boolean authenticate(@NotNull String code) {

        return this.service.check(this.getToken(TokenType.ACTIVE), code);
    }



    public User.TwoFactorStatus cancelEnabling() {
        this.user.setTwoFactorStandbyToken(null);
        User.TwoFactorStatus newStatus;

        if (this.user.getTwoFactorToken() == null) {
            this.logger.debug("2FA registration canceled. No active 2FA token for the user so 2FA status returned to INACTIVE.");
            newStatus = User.TwoFactorStatus.INACTIVE;

        } else {
            this.logger.debug("2FA registration canceled. There is an active 2FA token for the user so 2FA status returned to ACTIVE.");
            newStatus = User.TwoFactorStatus.ACTIVE;
        }

        this.user.setTwoFactorStatus(newStatus);
        return newStatus;
    }


    public void disable() {
        this.user.setTwoFactorToken(null);
        this.user.setTwoFactorStandbyToken(null);
    }



    public void enable() {
        String standbyToken = TimeBasedOneTimePasswordUtil.generateBase32Secret();
        String encryptedStandbyToken = new String(Hex.encode(encryptor.encrypt(standbyToken.getBytes())));
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
        this.user.setTwoFactorStatus(User.TwoFactorStatus.ACTIVE);

        return true;
    }



    private String getToken(TokenType secretType) {
        this.logger.debug("Getting {} token for user {}", secretType.name(), user.getLogin());
        String encryptedToken = (secretType == TokenType.STANDBY) ? this.user.getTwoFactorStandbyToken()
                : this.user.getTwoFactorToken();
        byte[] bytes = Hex.decode(encryptedToken);
        return new String(this.encryptor.decrypt(bytes));
    }
}
