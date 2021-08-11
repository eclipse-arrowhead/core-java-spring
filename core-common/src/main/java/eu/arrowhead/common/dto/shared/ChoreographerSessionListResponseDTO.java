package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerSessionListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7507499084467118434L;
	
	private List<ChoreographerSessionResponseDTO> data;
	private long count;
	
	//=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
	public ChoreographerSessionListResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerSessionListResponseDTO(final List<ChoreographerSessionResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<ChoreographerSessionResponseDTO> getData() { return data; }
	public long getCount() { return count; }	
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<ChoreographerSessionResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }
	
	//-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
    	try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
    }
}
