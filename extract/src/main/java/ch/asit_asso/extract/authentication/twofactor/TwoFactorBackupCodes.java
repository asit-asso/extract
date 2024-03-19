package ch.asit_asso.extract.authentication.twofactor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.domain.RecoveryCode;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.RecoveryCodeRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Base64Utils;

public class TwoFactorBackupCodes {
    private static final int BACKUP_CODES_NUMBER = 6;

    private final Logger logger = LoggerFactory.getLogger(TwoFactorBackupCodes.class);

    private final PasswordEncoder encoder;

    private String[] rawCodesArray;

    private final RecoveryCodeRepository repository;

    private final User user;

    public TwoFactorBackupCodes(@NotNull User user, @NotNull RecoveryCodeRepository recoveryCodeRepository,
                                @NotNull PasswordEncoder passwordEncoder) {
        this.encoder = passwordEncoder;
        this.repository = recoveryCodeRepository;
        this.user = user;
    }


    public String toFileData() {
        StringBuilder fileDataBuilder = new StringBuilder("data:text/plain;charset=UTF-8;base64,");
        String fileContent = String.join("\n", this.rawCodesArray);
        fileDataBuilder.append(Base64Utils.encodeToUrlSafeString(fileContent.getBytes()));

        return fileDataBuilder.toString();
    }



    public void delete() {
        this.repository.deleteAll(user.getTwoFactorRecoveryCodesCollection());
        user.setTwoFactorRecoveryCodesCollection(new ArrayList<>());
    }



    public String[] generate() {
        this.delete();
        this.rawCodesArray = new String[TwoFactorBackupCodes.BACKUP_CODES_NUMBER];

        for (int codeIndex = 0; codeIndex < TwoFactorBackupCodes.BACKUP_CODES_NUMBER; codeIndex++) {
            StringBuilder codeStringBuilder = new StringBuilder(RandomStringUtils.randomAlphanumeric(12).toUpperCase());
            codeStringBuilder.insert(6, "-");
            this.rawCodesArray[codeIndex] = codeStringBuilder.toString();
        }

        this.save();

        return this.rawCodesArray.clone();
    }



    public boolean submitCode(String code) {
        Collection<RecoveryCode> encodedAnswers = this.getActiveCodes();

        if (encodedAnswers != null) {

            for (RecoveryCode encodedCode : encodedAnswers) {

                if (this.encoder.matches(code, encodedCode.getToken())) {
                    this.repository.deleteById(encodedCode.getId());

                    return true;
                }
            }
        }

        return false;
    }



    private Collection<RecoveryCode> createEntries() {
        List<RecoveryCode> objectsCollection = new ArrayList<>();

        for (String rawCode : this.rawCodesArray) {
            RecoveryCode codeObject = new RecoveryCode();
            codeObject.setUser(this.user);
            codeObject.setToken(this.encoder.encode(rawCode));
            objectsCollection.add(codeObject);
        }

        return objectsCollection;
    }




    private Collection<RecoveryCode> getActiveCodes() {
        return this.user.getTwoFactorRecoveryCodesCollection();
    }


    private void save() {
        Collection<RecoveryCode> backupCodesCollection = this.createEntries();
        this.user.setTwoFactorRecoveryCodesCollection(backupCodesCollection);
        this.repository.saveAll(backupCodesCollection);
    }

}
