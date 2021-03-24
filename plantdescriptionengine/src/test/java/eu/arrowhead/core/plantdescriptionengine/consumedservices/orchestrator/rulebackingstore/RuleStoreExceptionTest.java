package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RuleStoreExceptionTest {

    @Test
    public void shouldCreateExceptionFromMessage() {
        String errorMessage = "Lorem Ipsum";
        final var e = new RuleStoreException(errorMessage);
        assertEquals(errorMessage, e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void shouldCreateExceptionFromThrowable() {
        var cause = new RuntimeException("ABC");
        final var e = new RuleStoreException(cause);
        assertEquals("java.lang.RuntimeException: ABC", e.getMessage());
    }

    @Test
    public void shouldCreateExceptionFromMessageAndThrowable() {
        String errorMessage = "Lorem Ipsum";
        String causeMessage = "ABC";
        var cause = new RuntimeException(causeMessage);
        final var e = new RuleStoreException(errorMessage, cause);
        assertEquals(errorMessage, e.getMessage());
        assertEquals(causeMessage, e.getCause().getMessage());
    }

}
