package ch.asit_asso.extract.unit.authentication;

import java.util.Collection;
import ch.asit_asso.extract.authentication.ApplicationUser;
import ch.asit_asso.extract.authentication.ApplicationUserRole;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.domain.User.TwoFactorStatus;
import ch.asit_asso.extract.domain.User.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
class ApplicationUserTest {

    private User domainUser;


    @BeforeEach
    void setUp() {
        this.domainUser = new User(1);
        this.domainUser.setLogin("testUser");
        this.domainUser.setName("Test User");
        this.domainUser.setPassword("hashedPassword");
        this.domainUser.setActive(true);
        this.domainUser.setProfile(Profile.OPERATOR);
        this.domainUser.setTwoFactorStatus(TwoFactorStatus.INACTIVE);
        this.domainUser.setTwoFactorForced(false);
        this.domainUser.setUserType(UserType.LOCAL);
    }



    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Create ApplicationUser from valid domain user")
        void createFromValidDomainUser() {
            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertNotNull(appUser);
            assertEquals(domainUser.getLogin(), appUser.getUsername());
            assertEquals(domainUser.getName(), appUser.getFullName());
            assertEquals(domainUser.getId(), appUser.getUserId());
            assertEquals(domainUser.getPassword(), appUser.getPassword());
        }



        @Test
        @DisplayName("Create ApplicationUser from null throws exception")
        void createFromNullThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new ApplicationUser(null));
        }
    }



    @Nested
    @DisplayName("UserDetails Implementation Tests")
    class UserDetailsTests {

        @Test
        @DisplayName("Active user has non-expired account")
        void activeUserHasNonExpiredAccount() {
            domainUser.setActive(true);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertTrue(appUser.isAccountNonExpired());
        }



        @Test
        @DisplayName("Inactive user has expired account")
        void inactiveUserHasExpiredAccount() {
            domainUser.setActive(false);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertFalse(appUser.isAccountNonExpired());
        }



        @Test
        @DisplayName("Active user has non-locked account")
        void activeUserHasNonLockedAccount() {
            domainUser.setActive(true);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertTrue(appUser.isAccountNonLocked());
        }



        @Test
        @DisplayName("Inactive user has locked account")
        void inactiveUserHasLockedAccount() {
            domainUser.setActive(false);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertFalse(appUser.isAccountNonLocked());
        }



        @Test
        @DisplayName("Active user has non-expired credentials")
        void activeUserHasNonExpiredCredentials() {
            domainUser.setActive(true);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertTrue(appUser.isCredentialsNonExpired());
        }



        @Test
        @DisplayName("Active user is enabled")
        void activeUserIsEnabled() {
            domainUser.setActive(true);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertTrue(appUser.isEnabled());
        }



        @Test
        @DisplayName("Inactive user is not enabled")
        void inactiveUserIsNotEnabled() {
            domainUser.setActive(false);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertFalse(appUser.isEnabled());
        }
    }



    @Nested
    @DisplayName("Authorities Tests")
    class AuthoritiesTests {

        @Test
        @DisplayName("Admin user has ADMIN authority")
        void adminUserHasAdminAuthority() {
            domainUser.setProfile(Profile.ADMIN);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertTrue(appUser.hasAuthority("ADMIN"));
        }



        @Test
        @DisplayName("Operator user has OPERATOR authority")
        void operatorUserHasOperatorAuthority() {
            domainUser.setProfile(Profile.OPERATOR);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertTrue(appUser.hasAuthority("OPERATOR"));
        }



        @Test
        @DisplayName("User without profile has no profile authority")
        void userWithoutProfileHasNoAuthority() {
            domainUser.setProfile(null);

            ApplicationUser appUser = new ApplicationUser(domainUser);
            Collection<? extends GrantedAuthority> authorities = appUser.getAuthorities();

            assertTrue(authorities.isEmpty());
        }



        @Test
        @DisplayName("hasAuthority throws exception for blank authority")
        void hasAuthorityThrowsExceptionForBlank() {
            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertThrows(IllegalArgumentException.class, () -> appUser.hasAuthority(""));
            assertThrows(IllegalArgumentException.class, () -> appUser.hasAuthority("   "));
            assertThrows(IllegalArgumentException.class, () -> appUser.hasAuthority(null));
        }



        @Test
        @DisplayName("hasAnyAuthority returns true for matching authority")
        void hasAnyAuthorityReturnsTrue() {
            domainUser.setProfile(Profile.ADMIN);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertTrue(appUser.hasAnyAuthority(Profile.values()));
        }



        @Test
        @DisplayName("hasAnyAuthority throws exception for empty array")
        void hasAnyAuthorityThrowsForEmpty() {
            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertThrows(IllegalArgumentException.class, () -> appUser.hasAnyAuthority(new Profile[0]));
        }
    }



    @Nested
    @DisplayName("Two Factor Status Tests")
    class TwoFactorStatusTests {

        @Test
        @DisplayName("Active 2FA user has CAN_AUTHENTICATE_2FA authority")
        void active2FAUserHasAuthenticateAuthority() {
            domainUser.setTwoFactorStatus(TwoFactorStatus.ACTIVE);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertTrue(appUser.hasAuthority("CAN_AUTHENTICATE_2FA"));
        }



        @Test
        @DisplayName("Standby 2FA user has CAN_REGISTER_2FA authority")
        void standby2FAUserHasRegisterAuthority() {
            domainUser.setTwoFactorStatus(TwoFactorStatus.STANDBY);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertTrue(appUser.hasAuthority("CAN_REGISTER_2FA"));
        }



        @Test
        @DisplayName("Forced 2FA user has CAN_REGISTER_2FA authority")
        void forced2FAUserHasRegisterAuthority() {
            domainUser.setTwoFactorStatus(TwoFactorStatus.INACTIVE);
            domainUser.setTwoFactorForced(true);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertTrue(appUser.hasAuthority("CAN_REGISTER_2FA"));
        }



        @Test
        @DisplayName("Inactive 2FA user without force has no 2FA authority")
        void inactive2FAUserHasNo2FAAuthority() {
            domainUser.setTwoFactorStatus(TwoFactorStatus.INACTIVE);
            domainUser.setTwoFactorForced(false);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertFalse(appUser.hasAuthority("CAN_AUTHENTICATE_2FA"));
            assertFalse(appUser.hasAuthority("CAN_REGISTER_2FA"));
        }



        @Test
        @DisplayName("Get two factor status")
        void getTwoFactorStatus() {
            domainUser.setTwoFactorStatus(TwoFactorStatus.ACTIVE);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertEquals(TwoFactorStatus.ACTIVE, appUser.getTwoFactorStatus());
        }



        @Test
        @DisplayName("Get two factor forced flag")
        void getTwoFactorForced() {
            domainUser.setTwoFactorForced(true);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertTrue(appUser.isTwoFactorForced());
        }



        @Test
        @DisplayName("Get two factor active token")
        void getTwoFactorActiveToken() {
            String token = "activeToken123";
            domainUser.setTwoFactorToken(token);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertEquals(token, appUser.getTwoFactorActiveToken());
        }



        @Test
        @DisplayName("Get two factor standby token")
        void getTwoFactorStandbyToken() {
            String token = "standbyToken456";
            domainUser.setTwoFactorStandbyToken(token);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertEquals(token, appUser.getTwoFactorStandbyToken());
        }
    }



    @Nested
    @DisplayName("User Type Tests")
    class UserTypeTests {

        @Test
        @DisplayName("Get local user type")
        void getLocalUserType() {
            domainUser.setUserType(UserType.LOCAL);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertEquals(UserType.LOCAL, appUser.getUserType());
        }



        @Test
        @DisplayName("Get LDAP user type")
        void getLdapUserType() {
            domainUser.setUserType(UserType.LDAP);

            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertEquals(UserType.LDAP, appUser.getUserType());
        }
    }



    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Get username")
        void getUsername() {
            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertEquals(domainUser.getLogin(), appUser.getUsername());
        }



        @Test
        @DisplayName("Get full name")
        void getFullName() {
            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertEquals(domainUser.getName(), appUser.getFullName());
        }



        @Test
        @DisplayName("Get user ID")
        void getUserId() {
            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertEquals(domainUser.getId().intValue(), appUser.getUserId());
        }



        @Test
        @DisplayName("Get password")
        void getPassword() {
            ApplicationUser appUser = new ApplicationUser(domainUser);

            assertEquals(domainUser.getPassword(), appUser.getPassword());
        }



        @Test
        @DisplayName("Get authorities returns unmodifiable collection")
        void getAuthoritiesReturnsUnmodifiable() {
            ApplicationUser appUser = new ApplicationUser(domainUser);

            Collection<? extends GrantedAuthority> authorities = appUser.getAuthorities();

            assertThrows(UnsupportedOperationException.class, () ->
                ((Collection<GrantedAuthority>) authorities).add(new ApplicationUserRole(Profile.ADMIN))
            );
        }
    }
}
