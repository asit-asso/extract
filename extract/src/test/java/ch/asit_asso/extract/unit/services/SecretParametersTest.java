package ch.asit_asso.extract.unit.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Set;
import ch.asit_asso.extract.services.SecretParameters;
import ch.asit_asso.extract.testutils.TestSecrets;
import ch.asit_asso.extract.utils.Secrets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecretParametersTest {

    private Secrets secrets;

    private SecretParameters secretParameters;

    @BeforeEach
    void setUp() throws Exception {
        this.secrets = TestSecrets.create();
        this.secretParameters = new SecretParameters(this.secrets);
    }

    @Test
    void encryptsOnlyPasswordLikePluginParametersAndDecryptsThem() {
        HashMap<String, String> values = new HashMap<>();
        values.put("password", "deadbeef");
        values.put("url", "https://example.test");

        HashMap<String, String> encrypted = this.secretParameters.encrypt(values,
                "[{\"code\":\"password\",\"type\":\"pass\"},"
                        + "{\"code\":\"url\",\"type\":\"text\"}]");

        assertTrue(encrypted.get("password").startsWith("enc:v1:"));
        assertEquals(values.get("url"), encrypted.get("url"));
        assertNotEquals(values.get("password"), encrypted.get("password"));
        assertEquals(values, this.secretParameters.decrypt(encrypted,
                "[{\"code\":\"password\",\"type\":\"pass\"},"
                        + "{\"code\":\"url\",\"type\":\"text\"}]"));
    }

    @Test
    void acceptsLegacyCiphertextPlaintextAndMasksWhileKeepingBlankValuesUnencrypted() {
        String prefixed = this.secrets.encrypt("legacy-password");
        String legacy = prefixed.substring("enc:v1:".length());

        assertEquals("legacy-password", this.secrets.decryptLegacy(legacy));
        assertEquals("plain-password", this.secrets.decryptLegacy("plain-password"));
        assertEquals("deadbeef", this.secrets.decryptLegacy("deadbeef"));
        assertEquals("enc:v1:" + legacy, this.secrets.encryptIfNeeded(legacy));
        assertEquals("", this.secrets.encryptIfNeeded(""));

        HashMap<String, String> masked = new HashMap<>();
        masked.put("password", Secrets.getGenericPasswordString());
        assertEquals(Secrets.getGenericPasswordString(),
                this.secretParameters.encrypt(masked, Set.of("password")).get("password"));
    }
}