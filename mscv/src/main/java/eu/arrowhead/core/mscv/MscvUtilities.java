package eu.arrowhead.core.mscv;

import java.util.function.Supplier;

public class MscvUtilities {

    public final static String ID_NOT_NULL = "id must not be null";
    public final static String LIST_NOT_NULL = "list must not be null";
    public final static String TARGET_NOT_NULL = "target must not be null";
    public final static String NAME_NOT_NULL = "name must not be null";

    private static final String NOT_FOUND_ERROR = " not found";

    private MscvUtilities() { throw new UnsupportedOperationException(); }

    public static Supplier<IllegalArgumentException> notFoundException(final String variable) {
        return () -> new IllegalArgumentException(variable + NOT_FOUND_ERROR);
    }
}
