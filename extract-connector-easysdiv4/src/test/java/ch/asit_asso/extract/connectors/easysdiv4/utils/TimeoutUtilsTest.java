package ch.asit_asso.extract.connectors.easysdiv4.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeoutUtilsTest {

    @Test
    @DisplayName("Test with NULL value")
    public void testWithNullValueYieldsDefaultTimeout() {
        assertEquals(TimeoutUtils.DEFAULT_TIMEOUT_MILLIS, TimeoutUtils.parseTimeout(null));
    }

    @Test
    @DisplayName("Test with invalid value")
    public void testWithInvalidValueYieldsDefaultTimeout() {
        assertEquals(TimeoutUtils.DEFAULT_TIMEOUT_MILLIS, TimeoutUtils.parseTimeout("nonoisnothere"));
    }

    @Test
    @DisplayName("Test with low value")
    public void testWithLowValueYieldsDefaultTimeout() {
        assertEquals(TimeoutUtils.DEFAULT_TIMEOUT_MILLIS, TimeoutUtils.parseTimeout("11"));
    }
    @Test
    @DisplayName("Test with high value")
    public void testWithHighValueYieldsDefaultTimeout() {
        assertEquals(TimeoutUtils.DEFAULT_TIMEOUT_MILLIS, TimeoutUtils.parseTimeout("1424444"));
    }
    @Test
    @DisplayName("Test with value in range")
    public void testWithValueInRangeYieldsValue() {
        assertEquals(4502, TimeoutUtils.parseTimeout("4502"));
    }
}