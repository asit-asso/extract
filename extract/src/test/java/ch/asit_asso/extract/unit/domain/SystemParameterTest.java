package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.SystemParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SystemParameter Entity Tests")
class SystemParameterTest {

    private SystemParameter parameter;

    @BeforeEach
    void setUp() {
        parameter = new SystemParameter();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null key")
        void defaultConstructor_createsInstanceWithNullKey() {
            SystemParameter newParam = new SystemParameter();
            assertNull(newParam.getKey());
        }

        @Test
        @DisplayName("Constructor with key sets the key correctly")
        void constructorWithKey_setsKeyCorrectly() {
            String expectedKey = "smtp_server";
            SystemParameter newParam = new SystemParameter(expectedKey);
            assertEquals(expectedKey, newParam.getKey());
        }

        @Test
        @DisplayName("Constructor with key and value sets both correctly")
        void constructorWithKeyAndValue_setsBothCorrectly() {
            String expectedKey = "smtp_port";
            String expectedValue = "587";

            SystemParameter newParam = new SystemParameter(expectedKey, expectedValue);

            assertEquals(expectedKey, newParam.getKey());
            assertEquals(expectedValue, newParam.getValue());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setKey and getKey work correctly")
        void setAndGetKey() {
            String expectedKey = "base_path";
            parameter.setKey(expectedKey);
            assertEquals(expectedKey, parameter.getKey());
        }

        @Test
        @DisplayName("setValue and getValue work correctly")
        void setAndGetValue() {
            String expectedValue = "/var/data/extract";
            parameter.setValue(expectedValue);
            assertEquals(expectedValue, parameter.getValue());
        }
    }

    @Nested
    @DisplayName("Key Tests")
    class KeyTests {

        @Test
        @DisplayName("key can be set to null")
        void key_canBeSetToNull() {
            parameter.setKey(null);
            assertNull(parameter.getKey());
        }

        @Test
        @DisplayName("key can be set to empty string")
        void key_canBeSetToEmptyString() {
            parameter.setKey("");
            assertEquals("", parameter.getKey());
        }

        @Test
        @DisplayName("key can be set to max length")
        void key_canBeSetToMaxLength() {
            String maxLengthKey = "A".repeat(50);
            parameter.setKey(maxLengthKey);
            assertEquals(maxLengthKey, parameter.getKey());
        }

        @Test
        @DisplayName("key can contain underscores")
        void key_canContainUnderscores() {
            String underscoreKey = "smtp_from_mail";
            parameter.setKey(underscoreKey);
            assertEquals(underscoreKey, parameter.getKey());
        }

        @Test
        @DisplayName("key can be replaced")
        void key_canBeReplaced() {
            parameter.setKey("original_key");
            assertEquals("original_key", parameter.getKey());

            parameter.setKey("new_key");
            assertEquals("new_key", parameter.getKey());
        }
    }

    @Nested
    @DisplayName("Value Tests")
    class ValueTests {

        @Test
        @DisplayName("value can be set to null")
        void value_canBeSetToNull() {
            parameter.setValue(null);
            assertNull(parameter.getValue());
        }

        @Test
        @DisplayName("value can be set to empty string")
        void value_canBeSetToEmptyString() {
            parameter.setValue("");
            assertEquals("", parameter.getValue());
        }

        @Test
        @DisplayName("value can be set to long text")
        void value_canBeSetToLongText() {
            String longValue = "A".repeat(65000);
            parameter.setValue(longValue);
            assertEquals(longValue, parameter.getValue());
        }

        @Test
        @DisplayName("value can contain special characters")
        void value_canContainSpecialCharacters() {
            String specialValue = "smtp.example.com:587@ssl";
            parameter.setValue(specialValue);
            assertEquals(specialValue, parameter.getValue());
        }

        @Test
        @DisplayName("value can be replaced")
        void value_canBeReplaced() {
            parameter.setValue("original_value");
            assertEquals("original_value", parameter.getValue());

            parameter.setValue("new_value");
            assertEquals("new_value", parameter.getValue());
        }

        @Test
        @DisplayName("value can be numeric string")
        void value_canBeNumericString() {
            parameter.setValue("300");
            assertEquals("300", parameter.getValue());
        }

        @Test
        @DisplayName("value can be boolean string")
        void value_canBeBooleanString() {
            parameter.setValue("true");
            assertEquals("true", parameter.getValue());

            parameter.setValue("false");
            assertEquals("false", parameter.getValue());
        }

        @Test
        @DisplayName("value can be JSON string")
        void value_canBeJsonString() {
            String jsonValue = "{\"key\": \"value\", \"array\": [1, 2, 3]}";
            parameter.setValue(jsonValue);
            assertEquals(jsonValue, parameter.getValue());
        }
    }

    @Nested
    @DisplayName("Known System Parameters Tests")
    class KnownParametersTests {

        @Test
        @DisplayName("base_path parameter can be created")
        void basePathParameter_canBeCreated() {
            SystemParameter basePath = new SystemParameter("base_path", "/var/extract/data");
            assertEquals("base_path", basePath.getKey());
            assertEquals("/var/extract/data", basePath.getValue());
        }

        @Test
        @DisplayName("smtp_server parameter can be created")
        void smtpServerParameter_canBeCreated() {
            SystemParameter smtpServer = new SystemParameter("smtp_server", "mail.example.com");
            assertEquals("smtp_server", smtpServer.getKey());
            assertEquals("mail.example.com", smtpServer.getValue());
        }

        @Test
        @DisplayName("smtp_port parameter can be created")
        void smtpPortParameter_canBeCreated() {
            SystemParameter smtpPort = new SystemParameter("smtp_port", "587");
            assertEquals("smtp_port", smtpPort.getKey());
            assertEquals("587", smtpPort.getValue());
        }

        @Test
        @DisplayName("mails_enable parameter can be created")
        void mailsEnableParameter_canBeCreated() {
            SystemParameter mailsEnable = new SystemParameter("mails_enable", "true");
            assertEquals("mails_enable", mailsEnable.getKey());
            assertEquals("true", mailsEnable.getValue());
        }

        @Test
        @DisplayName("freq_scheduler_sec parameter can be created")
        void freqSchedulerSecParameter_canBeCreated() {
            SystemParameter freqScheduler = new SystemParameter("freq_scheduler_sec", "60");
            assertEquals("freq_scheduler_sec", freqScheduler.getKey());
            assertEquals("60", freqScheduler.getValue());
        }

        @Test
        @DisplayName("ldap_on parameter can be created")
        void ldapOnParameter_canBeCreated() {
            SystemParameter ldapOn = new SystemParameter("ldap_on", "false");
            assertEquals("ldap_on", ldapOn.getKey());
            assertEquals("false", ldapOn.getValue());
        }

        @Test
        @DisplayName("standby_reminder_days parameter can be created")
        void standbyReminderDaysParameter_canBeCreated() {
            SystemParameter standbyReminder = new SystemParameter("standby_reminder_days", "7");
            assertEquals("standby_reminder_days", standbyReminder.getKey());
            assertEquals("7", standbyReminder.getValue());
        }

        @Test
        @DisplayName("dashboard_interval parameter can be created")
        void dashboardIntervalParameter_canBeCreated() {
            SystemParameter dashboardInterval = new SystemParameter("dashboard_interval", "30000");
            assertEquals("dashboard_interval", dashboardInterval.getKey());
            assertEquals("30000", dashboardInterval.getValue());
        }
    }

    @Nested
    @DisplayName("Equals, HashCode, and ToString Tests")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("equals returns true for same key")
        void equals_returnsTrueForSameKey() {
            SystemParameter param1 = new SystemParameter("smtp_server", "mail1.example.com");
            SystemParameter param2 = new SystemParameter("smtp_server", "mail2.example.com");
            assertEquals(param1, param2);
        }

        @Test
        @DisplayName("equals returns false for different key")
        void equals_returnsFalseForDifferentKey() {
            SystemParameter param1 = new SystemParameter("smtp_server");
            SystemParameter param2 = new SystemParameter("smtp_port");
            assertNotEquals(param1, param2);
        }

        @Test
        @DisplayName("equals returns false for null")
        void equals_returnsFalseForNull() {
            SystemParameter param1 = new SystemParameter("smtp_server");
            assertNotEquals(null, param1);
        }

        @Test
        @DisplayName("equals returns false for different type")
        void equals_returnsFalseForDifferentType() {
            SystemParameter param1 = new SystemParameter("smtp_server");
            assertNotEquals("not a parameter", param1);
        }

        @Test
        @DisplayName("hashCode is consistent for same key")
        void hashCode_isConsistentForSameKey() {
            SystemParameter param1 = new SystemParameter("smtp_server", "value1");
            SystemParameter param2 = new SystemParameter("smtp_server", "value2");
            assertEquals(param1.hashCode(), param2.hashCode());
        }

        @Test
        @DisplayName("hashCode differs for different key")
        void hashCode_differsForDifferentKey() {
            SystemParameter param1 = new SystemParameter("key1");
            SystemParameter param2 = new SystemParameter("key2");
            assertNotEquals(param1.hashCode(), param2.hashCode());
        }

        @Test
        @DisplayName("toString contains key")
        void toString_containsKey() {
            SystemParameter param = new SystemParameter("test_key", "test_value");
            String result = param.toString();
            assertTrue(result.contains("test_key"));
            assertTrue(result.contains("key"));
        }
    }

    @Nested
    @DisplayName("Complete SystemParameter Configuration Tests")
    class CompleteConfigurationTests {

        @Test
        @DisplayName("fully configured parameter has all attributes")
        void fullyConfiguredParameter_hasAllAttributes() {
            String key = "smtp_from_mail";
            String value = "noreply@example.com";

            parameter.setKey(key);
            parameter.setValue(value);

            assertEquals(key, parameter.getKey());
            assertEquals(value, parameter.getValue());
        }

        @Test
        @DisplayName("parameter with null value is valid")
        void parameterWithNullValue_isValid() {
            parameter.setKey("optional_setting");
            parameter.setValue(null);

            assertEquals("optional_setting", parameter.getKey());
            assertNull(parameter.getValue());
        }
    }

    @Nested
    @DisplayName("LDAP Parameters Tests")
    class LdapParametersTests {

        @Test
        @DisplayName("ldap_servers parameter can hold multiple servers")
        void ldapServersParameter_canHoldMultipleServers() {
            String servers = "ldap1.example.com,ldap2.example.com,ldap3.example.com";
            SystemParameter ldapServers = new SystemParameter("ldap_servers", servers);
            assertEquals(servers, ldapServers.getValue());
        }

        @Test
        @DisplayName("ldap_base_dn parameter can hold DN")
        void ldapBaseDnParameter_canHoldDn() {
            String baseDn = "dc=example,dc=com";
            SystemParameter ldapBaseDn = new SystemParameter("ldap_base_dn", baseDn);
            assertEquals(baseDn, ldapBaseDn.getValue());
        }

        @Test
        @DisplayName("ldap_encryption_type parameter can be set")
        void ldapEncryptionTypeParameter_canBeSet() {
            SystemParameter ldapEncryption = new SystemParameter("ldap_encryption_type", "STARTTLS");
            assertEquals("STARTTLS", ldapEncryption.getValue());
        }
    }
}
