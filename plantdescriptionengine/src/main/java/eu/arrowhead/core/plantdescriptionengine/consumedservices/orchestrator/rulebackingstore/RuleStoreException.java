package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

/**
 * Signifies the failure for a BackingStore instance to communicate with its
 * underlying storage.
 */
public class RuleStoreException extends Exception {
    private static final long serialVersionUID = 2925941409432190728L;

    public RuleStoreException(String errorMessage) {
        super(errorMessage);
    }

    public RuleStoreException(Throwable throwable) {
        super(throwable);
    }

    public RuleStoreException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }
}