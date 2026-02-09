package ch.asit_asso.extract.unit.authentication;

import ch.asit_asso.extract.authentication.ApplicationUserRole;
import ch.asit_asso.extract.domain.User.Profile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
class ApplicationUserRoleTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Create role with ADMIN profile")
        void createRoleWithAdminProfile() {
            ApplicationUserRole role = new ApplicationUserRole(Profile.ADMIN);

            assertNotNull(role);
        }



        @Test
        @DisplayName("Create role with OPERATOR profile")
        void createRoleWithOperatorProfile() {
            ApplicationUserRole role = new ApplicationUserRole(Profile.OPERATOR);

            assertNotNull(role);
        }



        @Test
        @DisplayName("Create role with null profile throws exception")
        void createRoleWithNullProfileThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new ApplicationUserRole(null));
        }
    }



    @Nested
    @DisplayName("getAuthority Tests")
    class GetAuthorityTests {

        @Test
        @DisplayName("ADMIN profile returns ADMIN authority")
        void adminProfileReturnsAdminAuthority() {
            ApplicationUserRole role = new ApplicationUserRole(Profile.ADMIN);

            String authority = role.getAuthority();

            assertEquals("ADMIN", authority);
        }



        @Test
        @DisplayName("OPERATOR profile returns OPERATOR authority")
        void operatorProfileReturnsOperatorAuthority() {
            ApplicationUserRole role = new ApplicationUserRole(Profile.OPERATOR);

            String authority = role.getAuthority();

            assertEquals("OPERATOR", authority);
        }
    }



    @Nested
    @DisplayName("GrantedAuthority Contract Tests")
    class GrantedAuthorityContractTests {

        @Test
        @DisplayName("Role implements GrantedAuthority correctly")
        void roleImplementsGrantedAuthority() {
            ApplicationUserRole role = new ApplicationUserRole(Profile.ADMIN);

            // GrantedAuthority interface only defines getAuthority()
            String authority = role.getAuthority();

            assertNotNull(authority);
            assertEquals(Profile.ADMIN.name(), authority);
        }
    }
}
