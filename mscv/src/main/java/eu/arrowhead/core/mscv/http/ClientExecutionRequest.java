package eu.arrowhead.core.mscv.http;

import java.io.Serializable;
import java.util.StringJoiner;

import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;

public class ClientExecutionRequest implements Serializable {

    private SshTargetDto target;
    private Layer layer;

    public ClientExecutionRequest() {
        super();
    }

    public ClientExecutionRequest(final SshTargetDto target, final Layer layer) {
        super();
        this.target = target;
        this.layer = layer;
    }

    public SshTargetDto getTarget() {
        return target;
    }

    public void setTarget(final SshTargetDto target) {
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
