package eu.arrowhead.core.mscv;

import eu.arrowhead.common.exception.DataNotFoundException;
import org.apache.http.HttpStatus;

import java.util.function.Supplier;

public class MscvUtilities {

    public final static String ID_NOT_NULL = "id must not be null";
    public final static String LIST_NOT_NULL = "list must not be null";
    public final static String TARGET_NOT_NULL = "target must not be null";
    public final static String NAME_NOT_NULL = "name must not be null";
    public final static String OS_NOT_NULL = "OS must not be null";
    public final static String ADDRESS_NOT_NULL = "address must not be null";
    public final static String PORT_NOT_NULL = "port must not be null";
    public final static String PAGE_NOT_NULL = "Page must not be null";
    public final static String EXAMPLE_NOT_NULL = "Example must not be null";

    private static final String NOT_FOUND_ERROR = " not found";

    private MscvUtilities() { throw new UnsupportedOperationException(); }

    public static Supplier<DataNotFoundException> notFoundException(final String variable) {
        return notFoundException(variable, null);
    }

    public static Supplier<DataNotFoundException> notFoundException(final String variable, final String origin) {
        return () -> new DataNotFoundException(variable + NOT_FOUND_ERROR, HttpStatus.SC_NOT_FOUND, origin);
    }
}
