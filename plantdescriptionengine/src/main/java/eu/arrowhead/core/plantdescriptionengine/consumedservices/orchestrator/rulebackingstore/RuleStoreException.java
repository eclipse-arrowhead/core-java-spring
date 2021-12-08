package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import java.util.Objects;

/**
 * Signifies a failure of a BackingStore instance in communicating with its
 * underlying storage.
 */
public class RuleStoreException extends Exception {
    private static final long serialVersionUID = 2925941409432190728L;

    public RuleStoreException(final String errorMessage) {
        super(errorMessage);
        Objects.requireNonNull(errorMessage, "Expected error message.");
    }

    public RuleStoreException(final Throwable cause) {
        super(cause);
        Objects.requireNonNull(cause, "Expected cause.");
    }

    public RuleStoreException(final String errorMessage, final Throwable cause) {
        super(errorMessage, cause);
        Objects.requireNonNull(errorMessage, "Expected error message.");
        Objects.requireNonNull(cause, "Expected cause.");
    }
}