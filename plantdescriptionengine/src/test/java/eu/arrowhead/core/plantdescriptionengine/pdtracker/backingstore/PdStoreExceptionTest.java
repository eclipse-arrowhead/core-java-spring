package eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PdStoreExceptionTest {

    @Test
    public void shouldCreateExceptionFromMessage() {
        String errorMessage = "Lorem Ipsum";
        final var e = new PdStoreException(errorMessage);
        assertEquals(errorMessage, e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void shouldCreateExceptionFromThrowable() {
        var cause = new RuntimeException("ABC");
        final var e = new PdStoreException(cause);
        assertEquals("java.lang.RuntimeException: ABC", e.getMessage());
    }

    @Test
    public void shouldCreateExceptionFromMessageAndThrowable() {
        String errorMessage = "Lorem Ipsum";
        String causeMessage = "ABC";
        var cause = new RuntimeException(causeMessage);
        final var e = new PdStoreException(errorMessage, cause);
        assertEquals(errorMessage, e.getMessage());
        assertEquals(causeMessage, e.getCause().getMessage());
    }

}
