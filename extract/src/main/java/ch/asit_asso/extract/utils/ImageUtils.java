package ch.asit_asso.extract.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

    /**
     * Pattern to detect SVG content by looking for the svg root element.
     */
    private static final Pattern SVG_PATTERN = Pattern.compile("(<svg[^>]*>|<\\?xml[^>]*>.*<svg[^>]*>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);



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

            ImageUtils.logger.debug("Image read from the URL is null. Checking for SVG format.");
            return ImageUtils.checkSvgUrl(url);

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

        // For SVG data URLs, check the MIME type first
        if ("image/svg+xml".equals(mimeType)) {
            return ImageUtils.checkSvgFromBase64(base64Content);
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

    /**
     * Checks if a URL points to a valid SVG image by attempting to read its content.
     *
     * @param url the URL to check
     * @return true if the URL contains valid SVG content, false otherwise
     */
    private static boolean checkSvgUrl(@NotNull String url) {
        try {
            URL svgUrl = new URL(url);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(svgUrl.openStream(), StandardCharsets.UTF_8))) {
                StringBuilder content = new StringBuilder();
                String line;
                int linesRead = 0;
                
                // Read up to 50 lines to check for SVG content (reasonable limit for headers)
                while ((line = reader.readLine()) != null && linesRead < 50) {
                    content.append(line).append('\n');
                    linesRead++;
                }
                
                boolean isSvg = ImageUtils.SVG_PATTERN.matcher(content.toString()).find();
                ImageUtils.logger.debug("SVG check result for URL: {}", isSvg);
                return isSvg;
            }
        } catch (IOException exception) {
            ImageUtils.logger.debug("Error checking SVG URL: {}", exception.getMessage());
            return false;
        }
    }

    /**
     * Checks if Base64 content represents valid SVG data.
     *
     * @param base64String the Base64 encoded SVG content
     * @return true if the content is valid SVG, false otherwise
     */
    private static boolean checkSvgFromBase64(@NotNull String base64String) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decodedBytes = decoder.decode(base64String);
            String svgContent = new String(decodedBytes, StandardCharsets.UTF_8);
            
            boolean isSvg = ImageUtils.SVG_PATTERN.matcher(svgContent).find();
            ImageUtils.logger.debug("SVG check result for Base64 content: {}", isSvg);
            return isSvg;
            
        } catch (IllegalArgumentException exception) {
            ImageUtils.logger.debug("Invalid Base64 content for SVG check.", exception);
            return false;
        }
    }
}
