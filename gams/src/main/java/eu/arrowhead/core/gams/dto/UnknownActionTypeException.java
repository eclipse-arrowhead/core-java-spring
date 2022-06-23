package eu.arrowhead.core.gams.dto;

public class UnknownActionTypeException extends RuntimeException {

    private final static String TEMPLATE = "Unknown ActionType for class %s";

    public UnknownActionTypeException(final Class<?> cls) { super(String.format(TEMPLATE, cls.getSimpleName())); }
}
