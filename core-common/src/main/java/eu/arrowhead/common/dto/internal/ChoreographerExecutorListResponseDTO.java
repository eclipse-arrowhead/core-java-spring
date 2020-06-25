package eu.arrowhead.common.dto.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChoreographerExecutorListResponseDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = -2899097877922460370L;

    private List<ChoreographerExecutorResponseDTO> data;
    private long count;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorListResponseDTO() {
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorListResponseDTO(List<ChoreographerExecutorResponseDTO> data, long count) {
        this.data = data;
        this.count = count;
    }

    //-------------------------------------------------------------------------------------------------
    public List<ChoreographerExecutorResponseDTO> getData() { return data; }
    public long getCount() { return count; }

    //-------------------------------------------------------------------------------------------------
    public void setData(List<ChoreographerExecutorResponseDTO> data) { this.data = data; }
    public void setCount(long count) { this.count = count; }
}
