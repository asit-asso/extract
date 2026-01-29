package ch.asit_asso.extract.unit.authentication.twofactor;

import java.util.concurrent.atomic.AtomicReference;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorApplication;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorService;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.TwoFactorStatus;
import ch.asit_asso.extract.unit.MockEnabledTest;
import ch.asit_asso.extract.utils.ImageUtils;
import ch.asit_asso.extract.utils.Secrets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Tag("unit")
public class TwoFactorApplicationTest extends MockEnabledTest {

    private TwoFactorApplication application;

    @Mock
    private Secrets secrets;

    @Mock
    private TwoFactorService service;

    private User user;

    @BeforeEach
    public void setUp() {
        this.user = new User(1);
        this.user.setLogin("testUser");

        Mockito.when(this.secrets.encrypt(anyString())).thenAnswer(
                (Answer<String>) invocationOnMock -> invocationOnMock.getArgument(0)
        );

        Mockito.when(this.secrets.decrypt(anyString())).thenAnswer(
                (Answer<String>) invocationOnMock -> invocationOnMock.getArgument(0)
        );

        Mockito.doCallRealMethod().when(this.service).generateSecret();

        this.application = new TwoFactorApplication(this.user, this.secrets, this.service);
    }

    @Nested
    @DisplayName("QR Code Generation Tests")
    class QrCodeTests {

        @Test
        @DisplayName("Generate QR code URL")
        void getQrCodeUrl() {
            user.setTwoFactorStatus(TwoFactorStatus.INACTIVE);
            application.enable();

            AtomicReference<String> url = new AtomicReference<>();

            assertDoesNotThrow(() -> url.set(application.getQrCodeUrl()));

            verify(secrets, times(1)).decrypt(anyString());
            assertNotNull(url.get());
            assertTrue(ImageUtils.checkUrl(url.get()), "The resulting URL could not be loaded as an image.");
        }
    }



    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Authenticate with valid code")
        void authenticateWithValidCode() {
            String validCode = "123456";
            String activeToken = "encryptedActiveToken";
            user.setTwoFactorToken(activeToken);
            Mockito.when(service.check(anyString(), eq(validCode))).thenReturn(true);

            boolean result = application.authenticate(validCode);

            assertTrue(result);
            verify(secrets, times(1)).decrypt(activeToken);
            verify(service, times(1)).check(anyString(), eq(validCode));
        }



        @Test
        @DisplayName("Authenticate with invalid code")
        void authenticateWithInvalidCode() {
            String invalidCode = "000000";
            String activeToken = "encryptedActiveToken";
            user.setTwoFactorToken(activeToken);
            Mockito.when(service.check(anyString(), eq(invalidCode))).thenReturn(false);

            boolean result = application.authenticate(invalidCode);

            assertFalse(result);
            verify(service, times(1)).check(anyString(), eq(invalidCode));
        }
    }



    @Nested
    @DisplayName("Cancel Enabling Tests")
    class CancelEnablingTests {

        @Test
        @DisplayName("Cancel enabling when no active token exists")
        void cancelEnablingNoActiveToken() {
            user.setTwoFactorStatus(TwoFactorStatus.STANDBY);
            user.setTwoFactorStandbyToken("standbyToken");
            user.setTwoFactorToken(null);

            TwoFactorStatus result = application.cancelEnabling();

            assertEquals(TwoFactorStatus.INACTIVE, result);
            assertEquals(TwoFactorStatus.INACTIVE, user.getTwoFactorStatus());
            assertNull(user.getTwoFactorStandbyToken());
        }



        @Test
        @DisplayName("Cancel enabling when active token exists")
        void cancelEnablingWithActiveToken() {
            user.setTwoFactorStatus(TwoFactorStatus.STANDBY);
            user.setTwoFactorStandbyToken("standbyToken");
            user.setTwoFactorToken("activeToken");

            TwoFactorStatus result = application.cancelEnabling();

            assertEquals(TwoFactorStatus.ACTIVE, result);
            assertEquals(TwoFactorStatus.ACTIVE, user.getTwoFactorStatus());
            assertNull(user.getTwoFactorStandbyToken());
        }
    }



    @Nested
    @DisplayName("Disable Tests")
    class DisableTests {

        @Test
        @DisplayName("Disable 2FA when active")
        void disableWhenActive() {
            user.setTwoFactorStatus(TwoFactorStatus.ACTIVE);
            user.setTwoFactorToken("activeToken");
            user.setTwoFactorStandbyToken("standbyToken");
            user.setTwoFactorForced(true);

            application.disable();

            assertEquals(TwoFactorStatus.INACTIVE, user.getTwoFactorStatus());
            assertNull(user.getTwoFactorToken());
            assertNull(user.getTwoFactorStandbyToken());
            assertFalse(user.isTwoFactorForced());
        }



        @Test
        @DisplayName("Disable 2FA when in standby")
        void disableWhenStandby() {
            user.setTwoFactorStatus(TwoFactorStatus.STANDBY);
            user.setTwoFactorStandbyToken("standbyToken");
            user.setTwoFactorForced(false);

            application.disable();

            assertEquals(TwoFactorStatus.INACTIVE, user.getTwoFactorStatus());
            assertNull(user.getTwoFactorToken());
            assertNull(user.getTwoFactorStandbyToken());
        }
    }



    @Nested
    @DisplayName("Enable Tests")
    class EnableTests {

        @Test
        @DisplayName("Enable 2FA when inactive")
        void enableWhenInactive() {
            user.setTwoFactorStatus(TwoFactorStatus.INACTIVE);

            application.enable();

            assertEquals(TwoFactorStatus.STANDBY, user.getTwoFactorStatus());
            assertNotNull(user.getTwoFactorStandbyToken());
            verify(secrets, times(1)).encrypt(anyString());
        }



        @Test
        @DisplayName("Reset 2FA when already active")
        void enableWhenActive() {
            user.setTwoFactorStatus(TwoFactorStatus.ACTIVE);
            user.setTwoFactorToken("existingToken");

            application.enable();

            assertEquals(TwoFactorStatus.STANDBY, user.getTwoFactorStatus());
            assertNotNull(user.getTwoFactorStandbyToken());
            // Original token should still be present
            assertEquals("existingToken", user.getTwoFactorToken());
        }
    }



    @Nested
    @DisplayName("Get Standby Token Tests")
    class GetStandbyTokenTests {

        @Test
        @DisplayName("Get standby token")
        void getStandbyToken() {
            String encryptedToken = "encryptedStandbyToken";
            user.setTwoFactorStandbyToken(encryptedToken);

            String result = application.getStandbyToken();

            verify(secrets, times(1)).decrypt(encryptedToken);
            // Since decrypt is stubbed to return input, result should equal the encrypted token
            assertEquals(encryptedToken, result);
        }
    }



    @Nested
    @DisplayName("Validate Registration Tests")
    class ValidateRegistrationTests {

        @Test
        @DisplayName("Validate registration with valid code")
        void validateRegistrationWithValidCode() {
            String validCode = "123456";
            String standbyToken = "standbyToken";
            user.setTwoFactorStatus(TwoFactorStatus.STANDBY);
            user.setTwoFactorStandbyToken(standbyToken);
            Mockito.when(service.check(anyString(), eq(validCode))).thenReturn(true);

            boolean result = application.validateRegistration(validCode);

            assertTrue(result);
            assertEquals(TwoFactorStatus.ACTIVE, user.getTwoFactorStatus());
            assertEquals(standbyToken, user.getTwoFactorToken());
            assertNull(user.getTwoFactorStandbyToken());
        }



        @Test
        @DisplayName("Validate registration with invalid code")
        void validateRegistrationWithInvalidCode() {
            String invalidCode = "000000";
            String standbyToken = "standbyToken";
            user.setTwoFactorStatus(TwoFactorStatus.STANDBY);
            user.setTwoFactorStandbyToken(standbyToken);
            Mockito.when(service.check(anyString(), eq(invalidCode))).thenReturn(false);

            boolean result = application.validateRegistration(invalidCode);

            assertFalse(result);
            assertEquals(TwoFactorStatus.STANDBY, user.getTwoFactorStatus());
            assertEquals(standbyToken, user.getTwoFactorStandbyToken());
            assertNull(user.getTwoFactorToken());
        }
    }
}
