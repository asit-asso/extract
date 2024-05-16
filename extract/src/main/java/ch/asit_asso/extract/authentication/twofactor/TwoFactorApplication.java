package ch.asit_asso.extract.authentication.twofactor;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.TwoFactorStatus;
import ch.asit_asso.extract.utils.ImageUtils;
import ch.asit_asso.extract.utils.Secrets;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwoFactorApplication {

    private static final String REGISTRATION_URL_FORMAT
            = "otpauth://totp/§§ISSUER§§:§§USER§§?secret=§§SECRET§§&issuer=§§ISSUER§§";

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
            this.logger.debug(
                    "2FA registration canceled. No active 2FA token for the user so 2FA status returned to INACTIVE.");
            newStatus = TwoFactorStatus.INACTIVE;

        } else {
            this.logger.debug(
                    "2FA registration canceled. There is an active 2FA token for the user so 2FA status returned to ACTIVE.");
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
        String standbyToken = this.service.generateSecret();
        String encryptedStandbyToken = this.secrets.encrypt(standbyToken);
        this.user.setTwoFactorStandbyToken(encryptedStandbyToken);
    }



    public String getQrCodeUrl() throws RuntimeException {

        BufferedImage image = this.generateQrCodeImage();
        String base64bytes = ImageUtils.encodeToBase64(image);

        return "data:image/png;base64," + base64bytes;
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



    private @NotNull BufferedImage generateQrCodeImage() {

        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix matrix;

        try {
            matrix = writer.encode(this.getRegistrationUrl(), BarcodeFormat.QR_CODE, 300, 300, hintMap);

        } catch (WriterException writerException) {
            throw new RuntimeException("The 2FA registration QR code generation failed.", writerException);
        }

        return MatrixToImageWriter.toBufferedImage(matrix);
    }



    private String getRegistrationUrl() {

        return TwoFactorApplication.REGISTRATION_URL_FORMAT.replaceAll("§§ISSUER§§", "Extract")
                                                           .replaceAll("§§USER§§", this.user.getLogin())
                                                           .replaceAll("§§SECRET§§", this.getToken(TokenType.STANDBY));
    }



    private String getToken(TokenType secretType) {

        this.logger.debug("Getting {} token for user {}", secretType.name(), this.user.getLogin());
        String encryptedToken = (secretType == TokenType.STANDBY) ? this.user.getTwoFactorStandbyToken()
                                                                  : this.user.getTwoFactorToken();

        return this.secrets.decrypt(encryptedToken);
    }
}
