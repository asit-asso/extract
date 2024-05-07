package ch.asit_asso.extract.unit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public class MockEnabledTest {

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void openMocks() {
        this.autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        this.autoCloseable.close();
    }
}
