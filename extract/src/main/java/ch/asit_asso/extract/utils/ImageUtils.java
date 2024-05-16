package ch.asit_asso.extract.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ImageUtils {

    private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);

    private static final String DATA_URL_PREFIX = "data:";

    private static final Pattern DATA_URL_REGEX = Pattern.compile("^data:(?<mimeType>[^;]+);(?<encoding>[^,]+),(?<content>.+)$");



    public static boolean checkUrl(@NotNull String url) {
        ImageUtils.logger.debug("Checking image URL {}", url);

        if (url.startsWith(ImageUtils.DATA_URL_PREFIX)) {
            ImageUtils.logger.debug("The URL is a data URL");

            return ImageUtils.checkDataUrl(url);
        }

        try {
            BufferedImage image = ImageIO.read(new URL(url));

            if (image != null) {
                ImageUtils.logger.debug("Image successfully read from the URL.");
                return true;
            }

            ImageUtils.logger.debug("Image read from the URL is null.");
            // TODO Check for SVG
            return false;

        } catch (IOException exception) {
            ImageUtils.logger.debug(String.format("Checking URL %s produced an error.", url), exception);
            return false;
        }
    }



    public static @NotNull String encodeToBase64(BufferedImage image) {

        byte[] bytes;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", outputStream);
            bytes = outputStream.toByteArray();

        } catch (IOException ioException) {
            throw new RuntimeException("The 2FA registration QR code generation failed.", ioException);
        }

        return new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8);
    }




    private static boolean checkDataUrl(@NotNull String url) {
        Matcher dataMatcher = ImageUtils.DATA_URL_REGEX.matcher(url);

        if (!dataMatcher.find()) {
            ImageUtils.logger.debug("The URL passed is not a valid image data URL.");
            return false;
        }

        String mimeType = dataMatcher.group("mimeType");

        if (mimeType == null  || !mimeType.startsWith("image/")) {
            ImageUtils.logger.debug("The MIME type in the data URL is not an image.");
            return false;
        }

        String encoding = dataMatcher.group("encoding");

        if (!"base64".equals(encoding)) {
            ImageUtils.logger.debug("The data in the URL is not Base64 encoded.");
            return false;
        }

        String base64Content = dataMatcher.group("content");

        if (StringUtils.isEmpty(base64Content)) {
            ImageUtils.logger.debug("No content in data URL.");
            return false;
        }

        return ImageUtils.loadImageFromBase64(base64Content);
    }



    private static boolean loadImageFromBase64(@NotNull String base64String) {

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] imageByte;

        try {
            imageByte = decoder.decode(base64String);

        } catch (IllegalArgumentException invalidBase64Exception) {
            ImageUtils.logger.debug("The content is not a valid Base64-encoded string.", invalidBase64Exception);
            return false;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageByte)) {
            BufferedImage image = ImageIO.read(inputStream);

            ImageUtils.logger.debug("Image read from Base64 string is {}null", (image == null) ? "" : "NOT ");

            return (image != null);

        } catch (IOException exception) {
            ImageUtils.logger.debug("Checking data string produced an error.", exception);
            return false;
        }
    }
}
