package eu.arrowhead.common.dto.shared.mscv;

import java.io.Serializable;
import java.util.StringJoiner;

public class ClientExecutionRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private TargetDto target;
    private Layer layer;

    public ClientExecutionRequest() {
        super();
    }

    public ClientExecutionRequest(final TargetDto target, final Layer layer) {
        super();
        this.target = target;
        this.layer = layer;
    }

    public TargetDto getTarget() {
        return target;
    }

    public void setTarget(final TargetDto target) {
        this.target = target;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(final Layer layer) {
        this.layer = layer;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ClientExecutionRequest.class.getSimpleName() + "[", "]")
                .add("target=" + target)
                .add("layer=" + layer)
                .toString();
    }
}
