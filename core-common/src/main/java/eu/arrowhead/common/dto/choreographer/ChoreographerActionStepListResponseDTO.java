package eu.arrowhead.common.dto.choreographer;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionStepListResponseDTO implements Serializable {

    private List<ChoreographerActionStepResponseDTO> data;

    private long count;

    public ChoreographerActionStepListResponseDTO() {}

    public ChoreographerActionStepListResponseDTO(List<ChoreographerActionStepResponseDTO> data, long count) {
        this.data = data;
        this.count = count;
    }

    public List<ChoreographerActionStepResponseDTO> getData() {
        return data;
    }

    public void setData(List<ChoreographerActionStepResponseDTO> data) {
        this.data = data;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
