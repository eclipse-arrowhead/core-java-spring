package eu.arrowhead.core.gams.dto;

public class PhaseResult<T> {

    private final T data;

    public PhaseResult(T data) {
        this.data = data;
    }
}
