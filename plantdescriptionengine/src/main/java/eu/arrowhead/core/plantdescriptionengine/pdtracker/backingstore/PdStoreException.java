package eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore;

/**
 * Signifies the failure for a BackingStore instance to communicate with its
 * underlying storage.
 */
public class PdStoreException extends Exception {
    private static final long serialVersionUID = 2925941409432190728L;

    public PdStoreException(String errorMessage) {
        super(errorMessage);
    }

    public PdStoreException(Throwable throwable) {
        super(throwable);
    }

    public PdStoreException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }
}