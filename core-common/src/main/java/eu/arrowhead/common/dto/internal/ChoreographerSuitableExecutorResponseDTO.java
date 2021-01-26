package eu.arrowhead.common.dto.internal;

import java.util.ArrayList;
import java.util.List;

public class ChoreographerSuitableExecutorResponseDTO {

    //=================================================================================================
    // members

    private List<Long> suitableExecutorIds = new ArrayList<>();

    //=================================================================================================
    // methods


    //-------------------------------------------------------------------------------------------------
    public ChoreographerSuitableExecutorResponseDTO() {
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerSuitableExecutorResponseDTO(List<Long> suitableExecutorIds) {
        this.suitableExecutorIds = suitableExecutorIds;
    }

    //-------------------------------------------------------------------------------------------------
    public List<Long> getSuitableExecutorIds() { return suitableExecutorIds; }

    //-------------------------------------------------------------------------------------------------
    public void setSuitableExecutorIds(List<Long> suitableExecutorIds) { this.suitableExecutorIds = suitableExecutorIds; }
}
