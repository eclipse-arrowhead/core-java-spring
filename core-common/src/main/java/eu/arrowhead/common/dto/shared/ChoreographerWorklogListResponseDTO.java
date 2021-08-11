package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;

public class ChoreographerWorklogListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 6777984090689985303L;	

	private List<ChoreographerWorklogResponseDTO> data;
    private long count;

    //=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
	public ChoreographerWorklogListResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerWorklogListResponseDTO(final List<ChoreographerWorklogResponseDTO> data, final long count) {
        this.data = data;
        this.count = count;
    }

    //-------------------------------------------------------------------------------------------------
	public List<ChoreographerWorklogResponseDTO> getData() { return data; }
	public long getCount() { return count; }

    //-------------------------------------------------------------------------------------------------
	public void setData(final List<ChoreographerWorklogResponseDTO> data) { this.data = data; }
    public void setCount(final long count) { this.count = count; }
}
