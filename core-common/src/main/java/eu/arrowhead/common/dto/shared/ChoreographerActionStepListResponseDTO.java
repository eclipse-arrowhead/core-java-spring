package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionStepListResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -4221321354484821514L;

	private List<ChoreographerActionStepResponseDTO> data;
    private long count;

    //=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionStepListResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionStepListResponseDTO(final List<ChoreographerActionStepResponseDTO> data, final long count) {
        this.data = data;
        this.count = count;
    }

    //-------------------------------------------------------------------------------------------------
	public List<ChoreographerActionStepResponseDTO> getData() { return data; }
	public long getCount() { return count; }

    //-------------------------------------------------------------------------------------------------
	public void setData(final List<ChoreographerActionStepResponseDTO> data) { this.data = data; }
    public void setCount(final long count) { this.count = count; }
}