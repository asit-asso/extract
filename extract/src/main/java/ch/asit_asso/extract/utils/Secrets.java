package ch.asit_asso.extract.utils;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Secrets {
    /**
     * The placeholder used to mask a password value.
     */
    private static final String DUMMY_PASSWORD = "*****";

    private final PasswordEncoder encoder;

    private final BytesEncryptor encryptor;



    public Secrets(PasswordEncoder encoder, BytesEncryptor encryptor) {
        this.encoder = encoder;
        this.encryptor = encryptor;
    }


    public static boolean isGenericPasswordString(String value) {
        return Secrets.DUMMY_PASSWORD.equals(value);
    }


    public static String getGenericPasswordString() {
        return Secrets.DUMMY_PASSWORD;
    }



    public boolean check(String input, String hash) {
        return this.encoder.matches(input, hash);
    }



    public String decrypt(String encryptedValue) {

        if (encryptedValue == null) {
            throw new IllegalArgumentException("The value to decrypt cannot be null.");
        }

        return new String(this.encryptor.decrypt(Hex.decode(encryptedValue)));
    }



    public String encrypt(String clearValue) {

        if (clearValue == null) {
            throw new IllegalArgumentException("The value to encrypt cannot be null.");
        }

        return new String(Hex.encode(this.encryptor.encrypt(clearValue.getBytes())));
    }


    public String hash(String clearValue) {

        if (clearValue == null) {
            throw new IllegalArgumentException("The value to hash cannot be null.");
        }

        return this.encoder.encode(clearValue);
    }
}
