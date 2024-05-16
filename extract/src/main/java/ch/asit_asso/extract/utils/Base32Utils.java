package ch.asit_asso.extract.utils;

public abstract class Base32Utils {

    private static final char[] BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    private static final int[] BITS_LOOKUP = new int[128];



    static {

        for (int characterIndex = 0; characterIndex < Base32Utils.BASE32_CHARS.length; characterIndex++) {
            Base32Utils.BITS_LOOKUP[Base32Utils.BASE32_CHARS[characterIndex]] = characterIndex;
        }
    }




    public static String encode(String input) {

        StringBuilder encoded = new StringBuilder();
        int buffer = 0;
        int bufferLength = 0;

        for (byte currentByte : input.getBytes()) {
            buffer <<= 8;
            buffer |= currentByte & 0xFF;
            bufferLength += 8;

            while (bufferLength >= 5) {
                int index = (buffer >> (bufferLength - 5)) & 0x1F;
                encoded.append(Base32Utils.BASE32_CHARS[index]);
                bufferLength -= 5;
            }
        }

        while (encoded.length() % 8 != 0) {
            encoded.append('=');
        }

        return encoded.toString();
    }



    public static String decode(String base32) {

        base32 = base32.toUpperCase().replaceAll("=", "");
        StringBuilder decoded = new StringBuilder();
        int buffer = 0;
        int bufferLength = 0;

        for (char c : base32.toCharArray()) {
            buffer <<= 5;
            buffer |= Base32Utils.BITS_LOOKUP[c];
            bufferLength += 5;

            while (bufferLength >= 8) {
                byte b = (byte) (buffer >> (bufferLength - 8));
                decoded.append((char) b);
                bufferLength -= 8;
            }
        }

        return decoded.toString();
    }

}
