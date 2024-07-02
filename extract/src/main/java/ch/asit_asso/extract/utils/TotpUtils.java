package ch.asit_asso.extract.utils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.stream.IntStream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public abstract class TotpUtils {
    private static final String HMAC_ALGO = "HmacSHA1";

    private static final int TOTP_LENGTH = 6;

    private static final int TIME_STEP = 30;

    private static final int SECRET_LENGTH = 10;



    public static String generateSecret() {

        String rawKey = TotpUtils.generateRawSecret();
        return Base32Utils.encode(rawKey);
    }



    public static boolean validate(String base32Token, String code) {
        long timeInterval = System.currentTimeMillis() / 1000 / TotpUtils.TIME_STEP;

        return IntStream.of(-1, 0, 1)
                        .anyMatch(intervalIndex -> TotpUtils.generateTOTP(base32Token,
                                                                          timeInterval + intervalIndex)
                                                            .equals(code));
    }



    private static String generateRawSecret() {

        byte[] buf = new byte[TotpUtils.SECRET_LENGTH];
        new SecureRandom().nextBytes(buf);
        String rawSecret = Base64.getEncoder().encodeToString(buf);
        return rawSecret.substring(1, TotpUtils.SECRET_LENGTH + 1);
    }



    private static String generateTOTP(String secretKey, long timeInterval) {

        try {
            String decodedSecret = Base32Utils.decode(secretKey);
            byte[] decodedKey = decodedSecret.getBytes();
            byte[] timeIntervalBytes = new byte[8];

            for (int i = 7; i >= 0; i--) {
                timeIntervalBytes[i] = (byte) (timeInterval & 0xFF);
                timeInterval >>= 8;
            }

            Mac hmac = Mac.getInstance(TotpUtils.HMAC_ALGO);
            hmac.init(new SecretKeySpec(decodedKey, TotpUtils.HMAC_ALGO));
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

            int totp = (int) (binaryCode % Math.pow(10, TotpUtils.TOTP_LENGTH));

            return String.format("%0" + TotpUtils.TOTP_LENGTH + "d", totp);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TOTP", e);
        }
    }
}
