package eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore;

import java.util.Objects;


/**
 * Signifies the failure for a BackingStore instance to communicate with its
 * underlying storage.
 */
public class PdStoreException extends Exception {
    private static final long serialVersionUID = 2925941409432190728L;

    public PdStoreException(final String errorMessage) {
        super(errorMessage);
        Objects.requireNonNull(errorMessage, "Expected error message.");
    }

    public PdStoreException(final Throwable cause) {
        super(cause);
        Objects.requireNonNull(cause, "Expected cause.");
    }

    public PdStoreException(final String errorMessage, final Throwable cause) {
        super(errorMessage, cause);
        Objects.requireNonNull(errorMessage, "Expected error message.");
        Objects.requireNonNull(cause, "Expected cause.");

    }
}