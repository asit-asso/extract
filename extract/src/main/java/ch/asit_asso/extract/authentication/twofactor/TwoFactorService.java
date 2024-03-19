package ch.asit_asso.extract.authentication.twofactor;

import java.security.GeneralSecurityException;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class TwoFactorService {

    public boolean check(String base32Token, String code) {

        if (!code.matches("^[\\d ]+$")) {
            return false;
        }

        try {
            return TimeBasedOneTimePasswordUtil.validateCurrentNumber(base32Token, Integer.parseInt(code.replace(" ", "")), 10000);
        }
        catch (GeneralSecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
