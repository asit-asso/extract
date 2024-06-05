package ch.asit_asso.extract.plugins.qgisprint.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QgisUtilsTest {

    private static final String GMLID_ENDING_WITH_DOT = "titi.toto.1985.";
    private static final String GMLID_STARTING_WITH_DOT = ".1985";
    private static final String GMLID_WITH_CONSECUTIVE_DOTS = "titi.toto..1985";
    private static final String GMLID_WITH_MULTIPLE_DOTS = "titi.toto.1985";
    private static final String GMLID_WITH_ONE_DOT = "toto.1985";
    private static final String GMLID_WITHOUT_DOT = "toto1985";



    @Test
    void getIdWithEndingDot() {
        String id = QgisUtils.getIdFromGmlIdString(QgisUtilsTest.GMLID_ENDING_WITH_DOT);

        assertEquals("", id);
    }



    @Test
    void getIdWithConsecutiveDots() {
        String id = QgisUtils.getIdFromGmlIdString(QgisUtilsTest.GMLID_WITH_CONSECUTIVE_DOTS);

        assertEquals("1985", id);
    }



    @Test
    void getIdWithMultipleDots() {
        String id = QgisUtils.getIdFromGmlIdString(QgisUtilsTest.GMLID_WITH_MULTIPLE_DOTS);

        assertEquals("1985", id);
    }



    @Test
    void getIdWithOneDot() {
        String id = QgisUtils.getIdFromGmlIdString(QgisUtilsTest.GMLID_WITH_ONE_DOT);

        assertEquals("1985", id);
    }



    @Test
    void getIdWithoutDot() {
        String id = QgisUtils.getIdFromGmlIdString(QgisUtilsTest.GMLID_WITHOUT_DOT);

        assertEquals("toto1985", id);
    }



    @Test
    void getIdWithStartingDot() {
        String id = QgisUtils.getIdFromGmlIdString(QgisUtilsTest.GMLID_STARTING_WITH_DOT);

        assertEquals("1985", id);
    }
}
