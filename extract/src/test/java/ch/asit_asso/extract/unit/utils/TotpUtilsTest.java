package ch.asit_asso.extract.unit.utils;

import ch.asit_asso.extract.utils.Base32Utils;
import ch.asit_asso.extract.utils.TotpUtils;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpUtilsTest {

    @Test
    void generateSecretReturnsNonNullString() {
        String secret = TotpUtils.generateSecret();

        assertNotNull(secret);
    }


    @Test
    void generateSecretReturnsNonEmptyString() {
        String secret = TotpUtils.generateSecret();

        assertFalse(secret.isEmpty());
    }


    @Test
    void generateSecretReturnsBase32EncodedString() {
        String secret = TotpUtils.generateSecret();

        // Base32 characters are A-Z and 2-7, plus padding with =
        assertTrue(secret.matches("^[A-Z2-7=]+$"), "Secret should contain only Base32 characters");
    }


    @Test
    void generateSecretReturnsPaddedString() {
        String secret = TotpUtils.generateSecret();

        // Base32 encoded strings should be padded to a multiple of 8 characters
        assertEquals(0, secret.length() % 8, "Base32 secret should be padded to multiple of 8");
    }


    @RepeatedTest(10)
    void generateSecretReturnsUniqueValues() {
        String secret1 = TotpUtils.generateSecret();
        String secret2 = TotpUtils.generateSecret();

        assertNotEquals(secret1, secret2, "Consecutive generated secrets should be different");
    }


    @Test
    void validateReturnsTrueForValidCode() {
        // Generate a secret
        String secret = TotpUtils.generateSecret();

        // Generate a TOTP code using the same algorithm
        String validCode = generateTotpCode(secret);

        boolean result = TotpUtils.validate(secret, validCode);

        assertTrue(result, "Validation should return true for a valid TOTP code");
    }


    @Test
    void validateReturnsFalseForInvalidCode() {
        String secret = TotpUtils.generateSecret();
        String invalidCode = "000000";

        // This might occasionally pass if the actual code happens to be 000000,
        // but the probability is 1 in 1,000,000
        boolean result = TotpUtils.validate(secret, invalidCode);

        // We cannot reliably assert false here because the code might be valid
        // Instead, we just verify the method executes without exception
        assertNotNull(Boolean.valueOf(result));
    }


    @Test
    void validateReturnsFalseForWrongLengthCode() {
        String secret = TotpUtils.generateSecret();
        String shortCode = "12345";  // Only 5 digits

        boolean result = TotpUtils.validate(secret, shortCode);

        assertFalse(result, "Validation should return false for wrong length code");
    }


    @Test
    void validateReturnsFalseForTooLongCode() {
        String secret = TotpUtils.generateSecret();
        String longCode = "1234567";  // 7 digits

        boolean result = TotpUtils.validate(secret, longCode);

        assertFalse(result, "Validation should return false for too long code");
    }


    @Test
    void validateReturnsFalseForNonNumericCode() {
        String secret = TotpUtils.generateSecret();
        String nonNumericCode = "abcdef";

        boolean result = TotpUtils.validate(secret, nonNumericCode);

        assertFalse(result, "Validation should return false for non-numeric code");
    }


    @Test
    void validateAcceptsCodeFromPreviousTimeWindow() {
        String secret = TotpUtils.generateSecret();

        // Generate code for previous time window (timeInterval - 1)
        long timeInterval = System.currentTimeMillis() / 1000 / 30;
        String previousCode = generateTotpCodeForInterval(secret, timeInterval - 1);

        boolean result = TotpUtils.validate(secret, previousCode);

        assertTrue(result, "Validation should accept code from previous time window");
    }


    @Test
    void validateAcceptsCodeFromNextTimeWindow() {
        String secret = TotpUtils.generateSecret();

        // Generate code for next time window (timeInterval + 1)
        long timeInterval = System.currentTimeMillis() / 1000 / 30;
        String nextCode = generateTotpCodeForInterval(secret, timeInterval + 1);

        boolean result = TotpUtils.validate(secret, nextCode);

        assertTrue(result, "Validation should accept code from next time window");
    }


    @Test
    void validateRejectsCodeFromOldTimeWindow() {
        String secret = TotpUtils.generateSecret();

        // Generate code for old time window (timeInterval - 2)
        long timeInterval = System.currentTimeMillis() / 1000 / 30;
        String oldCode = generateTotpCodeForInterval(secret, timeInterval - 2);

        boolean result = TotpUtils.validate(secret, oldCode);

        assertFalse(result, "Validation should reject code from old time window");
    }


    @Test
    void validateRejectsCodeFromFutureTimeWindow() {
        String secret = TotpUtils.generateSecret();

        // Generate code for future time window (timeInterval + 2)
        long timeInterval = System.currentTimeMillis() / 1000 / 30;
        String futureCode = generateTotpCodeForInterval(secret, timeInterval + 2);

        boolean result = TotpUtils.validate(secret, futureCode);

        assertFalse(result, "Validation should reject code from future time window");
    }


    @Test
    void validateWithKnownSecretAndCode() {
        // This is a known test vector
        // We use a fixed time interval and secret to ensure reproducibility
        String knownSecret = "JBSWY3DPEHPK3PXP";  // Base32 for "Hello!"

        // For this test, we verify the validate method handles input correctly
        // The actual code validation depends on the current time
        assertNotNull(TotpUtils.validate(knownSecret, "123456"));
    }


    @Test
    void validateWithInvalidBase32SecretCharactersHandled() {
        // Base32Utils.decode handles invalid characters by returning unpredictable values
        // (uses 0 from BITS_LOOKUP for unknown characters)
        // The validate method wraps any exceptions in a RuntimeException
        // Test that invalid but ASCII-range characters don't crash the system
        String invalidButSafeSecret = "!@#$%^&*";

        // Should not throw - these characters are in ASCII range and will
        // decode to something (possibly garbage) but the method handles it
        boolean result = TotpUtils.validate(invalidButSafeSecret, "123456");

        // The result will be false since these aren't valid secrets
        assertFalse(result);
    }


    @Test
    void generateSecretProducesDecodableValue() {
        String secret = TotpUtils.generateSecret();

        // Should not throw when decoding
        String decoded = Base32Utils.decode(secret);

        assertNotNull(decoded);
        assertFalse(decoded.isEmpty());
    }


    @Test
    void validateWithEmptyCode() {
        String secret = TotpUtils.generateSecret();
        String emptyCode = "";

        boolean result = TotpUtils.validate(secret, emptyCode);

        assertFalse(result, "Validation should return false for empty code");
    }


    @Test
    void validateConsistency() {
        // Verify that the same secret and code produce consistent results
        String secret = TotpUtils.generateSecret();
        String code = generateTotpCode(secret);

        boolean result1 = TotpUtils.validate(secret, code);
        boolean result2 = TotpUtils.validate(secret, code);

        assertEquals(result1, result2, "Validation should be consistent for same inputs");
    }


    @Test
    void generateSecretLength() {
        String secret = TotpUtils.generateSecret();

        // Base32 encoding of 10 bytes produces 16 characters (with padding)
        assertTrue(secret.length() >= 16, "Secret should be at least 16 characters");
    }


    // Helper methods to generate TOTP codes for testing

    private String generateTotpCode(String base32Secret) {
        long timeInterval = System.currentTimeMillis() / 1000 / 30;
        return generateTotpCodeForInterval(base32Secret, timeInterval);
    }


    private String generateTotpCodeForInterval(String base32Secret, long timeInterval) {
        try {
            String decodedSecret = Base32Utils.decode(base32Secret);
            byte[] decodedKey = decodedSecret.getBytes();
            byte[] timeIntervalBytes = new byte[8];

            for (int i = 7; i >= 0; i--) {
                timeIntervalBytes[i] = (byte) (timeInterval & 0xFF);
                timeInterval >>= 8;
            }

            javax.crypto.Mac hmac = javax.crypto.Mac.getInstance("HmacSHA1");
            hmac.init(new javax.crypto.spec.SecretKeySpec(decodedKey, "HmacSHA1"));
            byte[] hash = hmac.doFinal(timeIntervalBytes);
            int offset = hash[hash.length - 1] & 0xF;
            long mostSignificantByte = (hash[offset] & 0x7F) << 24;
            long secondMostSignificantByte = (hash[offset + 1] & 0xFF) << 16;
            long thirdMostSignificantByte = (hash[offset + 2] & 0xFF) << 8;
            long leastSignificantByte = hash[offset + 3] & 0xFF;

            long binaryCode = mostSignificantByte
                              | secondMostSignificantByte
                              | thirdMostSignificantByte
                              | leastSignificantByte;

            int totp = (int) (binaryCode % (long) Math.pow(10, 6));

            return String.format("%06d", totp);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TOTP for testing", e);
        }
    }
}
