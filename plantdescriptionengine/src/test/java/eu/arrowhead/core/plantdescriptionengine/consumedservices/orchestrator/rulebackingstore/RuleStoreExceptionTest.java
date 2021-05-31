package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RuleStoreExceptionTest {

    @Test
    public void shouldCreateExceptionFromMessage() {
        final String errorMessage = "Lorem Ipsum";
        final RuleStoreException e = new RuleStoreException(errorMessage);
        assertEquals(errorMessage, e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void shouldCreateExceptionFromThrowable() {
        final RuntimeException cause = new RuntimeException("ABC");
        final RuleStoreException e = new RuleStoreException(cause);
        assertEquals("java.lang.RuntimeException: ABC", e.getMessage());
    }

    @Test
    public void shouldCreateExceptionFromMessageAndThrowable() {
        final String errorMessage = "Lorem Ipsum";
        final String causeMessage = "ABC";
        final RuntimeException cause = new RuntimeException(causeMessage);
        final RuleStoreException e = new RuleStoreException(errorMessage, cause);
        assertEquals(errorMessage, e.getMessage());
        assertEquals(causeMessage, e.getCause().getMessage());
    }

}
