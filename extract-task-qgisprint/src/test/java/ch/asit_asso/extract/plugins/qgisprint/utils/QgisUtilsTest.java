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

    @Test
    void getIdWithEmptyString() {
        String id = QgisUtils.getIdFromGmlIdString("");

        assertEquals("", id);
    }

    @Test
    void getIdWithOnlyDot() {
        String id = QgisUtils.getIdFromGmlIdString(".");

        assertEquals("", id);
    }

    @Test
    void getIdWithMultipleConsecutiveDotsAtEnd() {
        String id = QgisUtils.getIdFromGmlIdString("feature...");

        assertEquals("", id);
    }

    @Test
    void getIdWithUuidFormat() {
        String id = QgisUtils.getIdFromGmlIdString("layer.550e8400-e29b-41d4-a716-446655440000");

        assertEquals("550e8400-e29b-41d4-a716-446655440000", id);
    }

    @Test
    void getIdWithNumericId() {
        String id = QgisUtils.getIdFromGmlIdString("feature.12345");

        assertEquals("12345", id);
    }

    @Test
    void getIdWithLongPrefix() {
        String id = QgisUtils.getIdFromGmlIdString("very.long.prefix.with.many.dots.finalid");

        assertEquals("finalid", id);
    }

    @Test
    void getIdWithSpecialCharactersInId() {
        String id = QgisUtils.getIdFromGmlIdString("feature.id-with_special");

        assertEquals("id-with_special", id);
    }

    @Test
    void getIdWithSpaceInString() {
        String id = QgisUtils.getIdFromGmlIdString("feature. space id");

        assertEquals(" space id", id);
    }

    @Test
    void getIdWithSingleCharacterAfterDot() {
        String id = QgisUtils.getIdFromGmlIdString("feature.x");

        assertEquals("x", id);
    }

    @Test
    void getIdWithNumbersOnly() {
        String id = QgisUtils.getIdFromGmlIdString("123.456");

        assertEquals("456", id);
    }

    @Test
    void getIdPreservesCase() {
        String id = QgisUtils.getIdFromGmlIdString("Feature.MixedCaseID");

        assertEquals("MixedCaseID", id);
    }

    @Test
    void getIdWithUnicodeCharacters() {
        String id = QgisUtils.getIdFromGmlIdString("feature.valeur");

        assertEquals("valeur", id);
    }

    @Test
    void getIdWithVeryLongId() {
        String longId = "a".repeat(1000);
        String id = QgisUtils.getIdFromGmlIdString("feature." + longId);

        assertEquals(longId, id);
    }

    @Test
    void getIdWithTabCharacter() {
        String id = QgisUtils.getIdFromGmlIdString("feature.id\twith\ttabs");

        assertEquals("id\twith\ttabs", id);
    }

    @Test
    void getIdWithNewlineCharacter() {
        String id = QgisUtils.getIdFromGmlIdString("feature.id\nwith\nnewlines");

        assertEquals("id\nwith\nnewlines", id);
    }

    @Test
    void getIdWithQgisServerFormat() {
        // Common QGIS Server GML ID format
        String id = QgisUtils.getIdFromGmlIdString("countries.1");

        assertEquals("1", id);
    }

    @Test
    void getIdWithComplexQgisFormat() {
        // More complex QGIS format
        String id = QgisUtils.getIdFromGmlIdString("schema.table.123");

        assertEquals("123", id);
    }
}
