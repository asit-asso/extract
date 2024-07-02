package ch.asit_asso.extract.unit.ldap;

import ch.asit_asso.extract.ldap.ActiveDirectoryServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActiveDirectoryServerTest {

    private static final String[] INVALID_DOMAINS = new String[] {
          "example.c",
          "-mon-domaine.ch",
          "titi-.toto",
          "mon_dom@ine,invalide!.net",
          "l'autre(domaine)invalide?.info",
          "faux&invalide.domain",
          "mon\"domaine\".fr",
          "test",
          ".ch"
    };

    private static final String[] VALID_DOMAINS = new String[] {
            "example.com",
            "mon-domaine.ch",
            "3333-22222.net",
            "news.info",
            "MoNdOmAiNeVaLiDe.CoM"
    };

    @Test
    @DisplayName("Test invalid domain string")
    void isDomainInvalid() {

        for (String invalidDomain : ActiveDirectoryServerTest.INVALID_DOMAINS) {
            Assertions.assertFalse(ActiveDirectoryServer.isDomain(invalidDomain),
                                   String.format("The domain %s is invalid but was accepted.", invalidDomain));
        }
    }



    @Test
    @DisplayName("Test valid domain string")
    void isDomainValid() {
        for (String validDomain : ActiveDirectoryServerTest.VALID_DOMAINS) {
            assertTrue(ActiveDirectoryServer.isDomain(validDomain),
                       String.format("The domain %s is valid but was refused.", validDomain));
        }
    }
}
