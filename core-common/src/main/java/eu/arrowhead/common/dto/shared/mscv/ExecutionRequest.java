package eu.arrowhead.common.dto.shared.mscv;

import java.io.Serializable;

public class ExecutionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long executionListId;
    private Long targetId;

    public ExecutionRequest() {
        super();
    }

    public ExecutionRequest(final Long executionListId, final Long targetId) {
        super();
        this.executionListId = executionListId;
        this.targetId = targetId;
    }

    public Long getExecutionListId() {
        return executionListId;
    }

    public void setExecutionListId(final Long executionListId) {
        this.executionListId = executionListId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(final Long targetId) {
        this.targetId = targetId;
    }
}
