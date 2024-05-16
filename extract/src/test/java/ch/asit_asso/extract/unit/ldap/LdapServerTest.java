package ch.asit_asso.extract.unit.ldap;

import java.util.NoSuchElementException;
import ch.asit_asso.extract.ldap.ActiveDirectoryServer;
import ch.asit_asso.extract.ldap.LdapServer;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.ldap.LdapSettings.EncryptionType;
import ch.asit_asso.extract.ldap.OpenLdapServer;
import ch.asit_asso.extract.unit.MockEnabledTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LdapServerTest extends MockEnabledTest {
    private static final String DOMAIN_BASE = "example.com";
    private static final String EXPECTED_BASE_FOR_DOMAIN = "DC=example,DC=com";
    private static final String INVALID_URL = "ldap%server&test";
    private static final String LDAP_BASE = "OU=Users,DC=example,DC=com";
    private static final String LDAP_URL = "ldap://monserveur.com";

    @Mock
    private static LdapSettings settings;

    @Test
    @DisplayName("Creating an instance with Active Directory configuration")
    void buildActiveDirectory() {

        LdapServer adServer = LdapServer.build(LdapServerTest.LDAP_URL, LdapServerTest.DOMAIN_BASE,
                                               EncryptionType.STARTTLS, null, null,
                                               LdapServerTest.settings);
        assertInstanceOf(ActiveDirectoryServer.class, adServer);
        assertEquals(LdapServerTest.EXPECTED_BASE_FOR_DOMAIN, adServer.getBase());
    }


    @Test
    @DisplayName("Creating an instance with Open LDAP configuration")
    void buildOpenLdap() {

        LdapServer openLdapServer = LdapServer.build(LdapServerTest.LDAP_URL, LdapServerTest.LDAP_BASE,
                                                     EncryptionType.STARTTLS, null, null,
                                                     LdapServerTest.settings);
        assertInstanceOf(OpenLdapServer.class, openLdapServer);
        assertEquals(LdapServerTest.LDAP_BASE, openLdapServer.getBase());
    }


    @Test
    @DisplayName("Creating an instance with an invalid URL")
    void buildInvalid() {
        assertThrows(NoSuchElementException.class, () -> {
            LdapServer.build(LdapServerTest.INVALID_URL, LdapServerTest.LDAP_BASE, EncryptionType.STARTTLS,
                             null, null, LdapServerTest.settings);
        });
    }



    @Test
    @DisplayName("Test anonymous instantiation")
    void isAnonymous() {
        LdapServer server = LdapServer.build(LdapServerTest.LDAP_URL, LdapServerTest.LDAP_BASE,
                                             EncryptionType.STARTTLS, null, null,
                                             LdapServerTest.settings);
        assertTrue(server.isAnonymous());
    }

    @Test
    @DisplayName("Test instantiation with only user name")
    void isAnonymousWithUser() {

        LdapServer server = LdapServer.build(LdapServerTest.LDAP_URL, LdapServerTest.LDAP_BASE,
                                  EncryptionType.STARTTLS, "titi", null,
                                  LdapServerTest.settings);
        assertTrue(server.isAnonymous());
    }

    @Test
    @DisplayName("Test anonymous with only password")
    void isAnonymousWithPassword() {

        LdapServer server = LdapServer.build(LdapServerTest.LDAP_URL, LdapServerTest.LDAP_BASE,
                                  EncryptionType.STARTTLS, null, "toto",
                                  LdapServerTest.settings);
        assertTrue(server.isAnonymous());
    }

    @Test
    @DisplayName("Test instantiation with full credentials")
    void hasCredentials() {
        LdapServer server = LdapServer.build(LdapServerTest.LDAP_URL, LdapServerTest.LDAP_BASE,
                                  EncryptionType.STARTTLS, "test", "toto",
                                  LdapServerTest.settings);
        assertFalse(server.isAnonymous());
    }
}
