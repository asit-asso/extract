package ch.asit_asso.extract.testutils;

import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;

import ch.asit_asso.extract.utils.Secrets;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

/**
 * Creates deterministic test secret services with real AES-GCM encryption.
 */
public final class TestSecrets {

    private TestSecrets() {
    }

    public static Secrets create() {
        SecretKeySpec key = new SecretKeySpec("1234567890123456".getBytes(StandardCharsets.UTF_8), "AES");
        AesBytesEncryptor encryptor = new AesBytesEncryptor(key, KeyGenerators.secureRandom(12),
                AesBytesEncryptor.CipherAlgorithm.GCM);
        return new Secrets(NoOpPasswordEncoder.getInstance(), encryptor);
    }
}
