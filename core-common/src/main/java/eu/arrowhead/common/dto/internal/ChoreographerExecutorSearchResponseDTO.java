package eu.arrowhead.common.dto.internal;

import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;

import java.io.Serializable;
import java.util.List;

public class ChoreographerExecutorSearchResponseDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 3067385511834702704L;

    private List<ChoreographerExecutorResponseDTO> data;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorSearchResponseDTO() {
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorSearchResponseDTO(List<ChoreographerExecutorResponseDTO> data) {
        this.data = data;
    }

    //-------------------------------------------------------------------------------------------------
    public List<ChoreographerExecutorResponseDTO> getData() { return data; }

    //-------------------------------------------------------------------------------------------------
    public void setData(List<ChoreographerExecutorResponseDTO> data) { this.data = data; }
}
