package eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PdStoreExceptionTest {

    @Test
    public void shouldCreateExceptionFromMessage() {
        final String errorMessage = "Lorem Ipsum";
        final PdStoreException e = new PdStoreException(errorMessage);
        assertEquals(errorMessage, e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void shouldCreateExceptionFromThrowable() {
        final RuntimeException cause = new RuntimeException("ABC");
        final PdStoreException e = new PdStoreException(cause);
        assertEquals("java.lang.RuntimeException: ABC", e.getMessage());
    }

    @Test
    public void shouldCreateExceptionFromMessageAndThrowable() {
        final String errorMessage = "Lorem Ipsum";
        final String causeMessage = "ABC";
        final RuntimeException cause = new RuntimeException(causeMessage);
        final PdStoreException e = new PdStoreException(errorMessage, cause);
        assertEquals(errorMessage, e.getMessage());
        assertEquals(causeMessage, e.getCause().getMessage());
    }

}
