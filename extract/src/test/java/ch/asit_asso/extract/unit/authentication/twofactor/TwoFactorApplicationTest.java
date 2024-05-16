package ch.asit_asso.extract.unit.authentication.twofactor;

import java.util.concurrent.atomic.AtomicReference;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorApplication;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorService;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.unit.MockEnabledTest;
import ch.asit_asso.extract.utils.ImageUtils;
import ch.asit_asso.extract.utils.Secrets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

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

    @Test
    @DisplayName("Generate QR code")
    public void getQrCodeUrl() {
        this.user.setTwoFactorStatus(User.TwoFactorStatus.INACTIVE);
        this.application.enable();

        AtomicReference<String> url = new AtomicReference<>();

        assertDoesNotThrow(() -> url.set(this.application.getQrCodeUrl()));

        Mockito.verify(this.secrets, times(1)).decrypt(anyString());
        assertNotNull(url);
        assertTrue(ImageUtils.checkUrl(url.get()), "The resulting URL could not be loaded as an image.");
    }
}
