package ch.asit_asso.extract.authentication.twofactor;

import ch.asit_asso.extract.utils.TotpUtils;
import org.springframework.stereotype.Service;


@Service
public class TwoFactorService {



    public boolean check(String base32Token, String code) {

        if (!code.matches("^[\\d ]+$")) {
            return false;
        }

        return TotpUtils.validate(base32Token, code);
    }



    public String generateSecret() {
        return TotpUtils.generateSecret();
    }

}
