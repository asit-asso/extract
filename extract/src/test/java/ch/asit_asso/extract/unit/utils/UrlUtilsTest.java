package ch.asit_asso.extract.unit.utils;

import ch.asit_asso.extract.utils.UrlUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrlUtilsTest {

    private static final String EXPECTED_APPLICATION_PATH = "/extract-dev";

    private static final String EXPECTED_ROOT_PATH = "/";

    private static final String NOT_A_URL = "Ceci n'est pas du tout une URL";

    private static final String RELATIVE_URL = "/extract-dev/";

    private static final String URL_WITH_ONLY_APPLICATION_PATH = "https://tomcat.myserver.com:8080/extract-dev";

    private static final String URL_WITH_ONLY_ROOT_APPLICATION_PATH = "https://tomcat.myserver.com:8080/";

    private static final String URL_WITH_ROOT_APPLICATION_PATH_NO_TRAILING_SLASH = "https://tomcat.myserver.com:8080";

    private static final String URL_WITH_TRAILING_SLASH = "https://tomcat.myserver.com:8080/extract-dev/";

    private static final String URL_WITH_ADDITIONAL_PATH_INFO = "https://tomcat.myserver.com:8080/extract-dev/titi/toto.html";



    @Test
    void testApplicationPath() {
        String applicationPath = UrlUtils.getApplicationPath(UrlUtilsTest.URL_WITH_ONLY_APPLICATION_PATH);

        assertEquals(UrlUtilsTest.EXPECTED_APPLICATION_PATH, applicationPath);
    }



    @Test
    void testApplicationPathWithTrailingSlash() {
        String applicationPath = UrlUtils.getApplicationPath(UrlUtilsTest.URL_WITH_TRAILING_SLASH);

        assertEquals(UrlUtilsTest.EXPECTED_APPLICATION_PATH, applicationPath);
    }



    @Test
    void testApplicationPathWithAdditionalPathInfo() {
        String applicationPath = UrlUtils.getApplicationPath(UrlUtilsTest.URL_WITH_ADDITIONAL_PATH_INFO);

        assertEquals(UrlUtilsTest.EXPECTED_APPLICATION_PATH, applicationPath);
    }



    @Test
    void testRootApplicationPath() {
        String applicationPath = UrlUtils.getApplicationPath(UrlUtilsTest.URL_WITH_ONLY_ROOT_APPLICATION_PATH);

        assertEquals(UrlUtilsTest.EXPECTED_ROOT_PATH, applicationPath);
    }



    @Test
    void testRootApplicationPathWithoutTrailingSlash() {
        String applicationPath = UrlUtils.getApplicationPath(UrlUtilsTest.URL_WITH_ROOT_APPLICATION_PATH_NO_TRAILING_SLASH);

        assertEquals(UrlUtilsTest.EXPECTED_ROOT_PATH, applicationPath);
    }



    @Test
    void testApplicationPathWithInvalidUrl() {
        assertThrows(IllegalArgumentException.class, () -> UrlUtils.getApplicationPath(UrlUtilsTest.NOT_A_URL));
    }



    @Test
    void testApplicationPathWithRelativeUrl() {
        assertThrows(IllegalArgumentException.class, () -> UrlUtils.getApplicationPath(UrlUtilsTest.RELATIVE_URL));
    }
}
