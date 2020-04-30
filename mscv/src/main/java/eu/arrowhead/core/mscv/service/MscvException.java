package eu.arrowhead.core.mscv.service;

public class MscvException extends Throwable {
    public MscvException() { super(); }

    public MscvException(final String message) {
        super(message);
    }

    public MscvException(final String message, final Exception cause) {
        super(message, cause);
    }
}
