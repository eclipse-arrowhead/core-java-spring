package eu.arrowhead.core.gams.dto;

public class ActionExecutionException extends RuntimeException {
    private static final String TEMPLATE = "Action (%s) failed with '%s': %s";
    private final AbstractActionWrapper action;

    public ActionExecutionException(final AbstractActionWrapper action, final Exception exception) {
        super(String.format(TEMPLATE, action.shortToString(), exception.getClass().getSimpleName(), exception.getMessage()), exception);
        this.action = action;
    }

    public AbstractActionWrapper getAction() {
        return action;
    }
}
