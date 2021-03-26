package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import java.util.Objects;

/**
 * An instance of this class represents a single query parameter requirement
 * violation, e.g. an invalid value for a given parameter.
 */
public class ParseError extends Exception {

    private static final long serialVersionUID = 8823647431608958886L;

    public ParseError(final String message) {
        super(Objects.requireNonNull(message, "Expected message."));
    }

}