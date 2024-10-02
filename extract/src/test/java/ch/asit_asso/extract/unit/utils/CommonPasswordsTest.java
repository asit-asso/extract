package ch.asit_asso.extract.unit.utils;

import ch.asit_asso.extract.utils.CommonPasswords;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommonPasswordsTest {

    @Test
    @DisplayName("Check if a password is common")
    void isCommonPassword() {
        Assertions.assertTrue(CommonPasswords.isCommon("password1"));
        assertTrue(CommonPasswords.isCommon("qwerty123"));
        assertTrue(CommonPasswords.isCommon("sunshine"));
        assertTrue(CommonPasswords.isCommon("qwerty1"));
        assertFalse(CommonPasswords.isCommon("IamAhUgePasssWord"));
        assertFalse(CommonPasswords.isCommon("ILuvQwetz"));
    }
}