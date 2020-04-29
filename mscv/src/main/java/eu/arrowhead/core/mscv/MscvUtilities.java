package eu.arrowhead.core.mscv;

import java.util.function.Supplier;

import eu.arrowhead.common.exception.DataNotFoundException;
import org.apache.http.HttpStatus;

public class MscvUtilities {

    // TODO move this whole class to Validation ?
    private static final String NOT_FOUND_ERROR_MESSAGE = " not found";

    private MscvUtilities() { throw new UnsupportedOperationException(); }

    public static Supplier<DataNotFoundException> notFoundException(final String variable) {
        return notFoundException(variable, null);
    }

    public static Supplier<DataNotFoundException> notFoundException(final String variable, final String origin) {
        return () -> new DataNotFoundException(variable + NOT_FOUND_ERROR_MESSAGE, HttpStatus.SC_NOT_FOUND, origin);
    }
}
