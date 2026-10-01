package ch.asit_asso.extract.utils;

import java.nio.charset.StandardCharsets;
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

    private static final String ENCRYPTED_PREFIX = "enc:v1:";

    private final PasswordEncoder encoder;

    private final BytesEncryptor encryptor;



    public Secrets(PasswordEncoder encoder, BytesEncryptor encryptor) {
        this.encoder = encoder;
        this.encryptor = encryptor;
    }


    public static boolean isGenericPasswordString(String value) {
        return Secrets.DUMMY_PASSWORD.equals(value);
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
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

        String encodedValue = encryptedValue.startsWith(ENCRYPTED_PREFIX)
                ? encryptedValue.substring(ENCRYPTED_PREFIX.length()) : encryptedValue;
        return new String(this.encryptor.decrypt(Hex.decode(encodedValue)), StandardCharsets.UTF_8);
    }

    /**
     * Decrypts a value, accepting legacy clear-text values for one migration cycle.
     * Values that look like ciphertext fail loudly when they cannot be decrypted.
     *
     * @param value an encrypted value or a legacy clear-text value
     * @return the clear-text value
     */
    public String decryptLegacy(String value) {
        if (value == null) {
            return null;
        }

        if (value.startsWith(ENCRYPTED_PREFIX) || looksLikeLegacyCiphertext(value)) {
            return this.decrypt(value);
        }

        return value;
    }


    public String encryptIfNeeded(String value) {
        if (value == null || isBlank(value) || isGenericPasswordString(value)) {
            return value;
        }

        if (value.startsWith(ENCRYPTED_PREFIX)) {
            return value;
        }

        if (looksLikeLegacyCiphertext(value)) {
            this.decrypt(value);
            return ENCRYPTED_PREFIX + value;
        }

        return this.encrypt(value);
    }



    public String encrypt(String clearValue) {

        if (clearValue == null) {
            throw new IllegalArgumentException("The value to encrypt cannot be null.");
        }

        String encodedValue = new String(Hex.encode(this.encryptor.encrypt(clearValue.getBytes(StandardCharsets.UTF_8))));
        return ENCRYPTED_PREFIX + encodedValue;
    }


    public String hash(String clearValue) {

        if (clearValue == null) {
            throw new IllegalArgumentException("The value to hash cannot be null.");
        }

        return this.encoder.encode(clearValue);
    }

    private static boolean looksLikeLegacyCiphertext(String value) {
        if (value.length() < 56 || value.length() % 2 != 0) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (Character.digit(character, 16) < 0) {
                return false;
            }
        }
        return true;
    }
}