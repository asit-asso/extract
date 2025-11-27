package ch.asit_asso.extract.unit.utils;

import java.nio.file.Paths;
import ch.asit_asso.extract.utils.ImageUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageUtilsTest {

    private static final String DATA_URL
            = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACsAAAA7CAYAAADii3NbAAABnUlEQVRoQ+1ZsY6CQBAdO7DjQ+jA2ME38BcQY0snSkdNAp/hL0BnwMoPMVRAd3frncYjB4PLguEy2xF23zwew86wb/HxNWAmY0FkR3pTpOxIwgIp+3ZlHceBOI6F87BtG6Io6oXbKw1M04Q0TXsB8kwyDAOSJEGXomSrqoLlctkK5Hke7Pd7NBCbczgcWueVZQmyLHfioGTP5zPout4KwlRhymODKdf1dvI8B03ThpFlIKvVCuMy+H6WZZ2isACoss9kXdcFSZIGE7sD1HUNQRDcLoWTvV6voCiKMLJFUTzwiCwpSzlLH9j37kK7Ae0GlAa0G9BuAEBd10/LSUWBigIVBSoKVBSoKNC5we0/iXqDsXqDWR3MCTuR+wNIeNf178iqqgqWZcHxeITL5dL7+SZXtmlkvGKcTE62aYZgpsez7JOT3Ww2EIbhg8N2u/113ZUTQshibk2TwG63g/V6DafTCXzf752zQtwazAfrzQaZKMQHYzFe+VB4yAtzGO/BZ+Pd8qg1xhrUtBsjKC8mkeVVDltHymIK8d6flbKf0lTXbuhSGWQAAAAASUVORK5CYII=";

    private static final String FILE_URL = Paths.get("src/main/resources/static/images/extract_logo.png")
                                                .toUri()
                                                .toString();

    private static final String INVALID_DATA_URL
            = "data:image/png;base64,totalBullshitContent=";


    private static final String NOT_FOUND_FILE_URL = Paths.get("src/main/resources/static/titi/toto.png")
                                                          .toUri()
                                                          .toString();


    private static final String NOT_FOUND_URL = "https://titi.toto/mon_image.png";

    private static final String NOT_A_URL = "b%çsdfkjhg<fiohfr>fdguon4v+/95hg9duh";

    private static final String NOT_AN_IMAGE_DATA_URL
            = "PCFET0NUWVBFIGh0bWw+CjxodG1sIGxhbmc9ImVuIj4KPGhlYWQ+CiAgICA8dGl0bGU+VGVzdCBwYWdlPC90aXRsZT4KPC9oZWFkPgo8Ym9keT4KICAgIDxoMT5UZXN0IHBhZ2U8L2gxPgogICAgPHA+VGhpcyBpcyBhIHRlc3QgSFRNTCBwYWdlPC9wPgo8L2JvZHk+CjwvaHRtbD4K";

    private static final String NOT_AN_IMAGE_FILE_URL = Paths.get("src/main/resources/static/css/extract.css")
                                                             .toUri()
                                                             .toString();


    private static final String NOT_AN_IMAGE_URL = "https://google.com";

    private static final String PNG_URL
            = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";



    @Test
    @DisplayName("Check local file URL")
    void checkFileUrl() {

//        System.err.println(ImageUtilsTest.FILE_URL);
        boolean isValid = ImageUtils.checkUrl(ImageUtilsTest.FILE_URL);

        assertTrue(isValid);
    }



    @Test
    @DisplayName("Check local file URL that does not contain an image")
    void checkNotAnImageFileUrl() {

        boolean isValid = ImageUtils.checkUrl(ImageUtilsTest.NOT_AN_IMAGE_FILE_URL);

        assertFalse(isValid);
    }



    @Test
    @DisplayName("Check local file URL that does not exist")
    void checkNonExistantFileUrl() {

        boolean isValid = ImageUtils.checkUrl(ImageUtilsTest.NOT_FOUND_FILE_URL);

        assertFalse(isValid);
    }



    @Test
    @DisplayName("Check data URL")
    void checkDataUrl() {

        boolean isValid = ImageUtils.checkUrl(ImageUtilsTest.DATA_URL);

        assertTrue(isValid);
    }



    @Test
    @DisplayName("Check data URL whose content is not an image")
    void checkNotAnImageDataUrl() {

        boolean isValid = ImageUtils.checkUrl(ImageUtilsTest.NOT_AN_IMAGE_DATA_URL);

        assertFalse(isValid);
    }



    @Test
    @DisplayName("Check data URL with invalid content")
    void checkInvalidDataUrl() {

        boolean isValid = ImageUtils.checkUrl(ImageUtilsTest.INVALID_DATA_URL);

        assertFalse(isValid);
    }



    @Test
    @DisplayName("Check PNG URL")
    void checkPngUrl() {

        boolean isValid = ImageUtils.checkUrl(ImageUtilsTest.PNG_URL);

        assertTrue(isValid);
    }



    @Test
    @DisplayName("Check URL that points nowhere")
    void check404Url() {

        boolean isValid = ImageUtils.checkUrl(ImageUtilsTest.NOT_FOUND_URL);

        assertFalse(isValid);
    }



    @Test
    @DisplayName("Check URL that does not point to an image")
    void checkNotImageUrl() {

        boolean isValid = ImageUtils.checkUrl(ImageUtilsTest.NOT_AN_IMAGE_URL);

        assertFalse(isValid);
    }



    @Test
    @DisplayName("Check invalid URL")
    void checkInvalidUrl() {

        boolean isValid = ImageUtils.checkUrl(ImageUtilsTest.NOT_A_URL);

        assertFalse(isValid);
    }
}
