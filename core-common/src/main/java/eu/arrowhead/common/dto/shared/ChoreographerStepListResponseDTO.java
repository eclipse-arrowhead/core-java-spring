package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;

public class ChoreographerStepListResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -4221321354484821514L;

	private List<ChoreographerStepResponseDTO> data;
    private long count;

    //=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepListResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepListResponseDTO(final List<ChoreographerStepResponseDTO> data, final long count) {
        this.data = data;
        this.count = count;
    }

    //-------------------------------------------------------------------------------------------------
	public List<ChoreographerStepResponseDTO> getData() { return data; }
	public long getCount() { return count; }

    //-------------------------------------------------------------------------------------------------
	public void setData(final List<ChoreographerStepResponseDTO> data) { this.data = data; }
    public void setCount(final long count) { this.count = count; }
}