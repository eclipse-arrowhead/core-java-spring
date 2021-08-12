package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerSessionStepListResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -3871716187401370011L;
	
	private List<ChoreographerSessionStepResponseDTO> data;
	private long count;
	
	//=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
	public ChoreographerSessionStepListResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerSessionStepListResponseDTO(final List<ChoreographerSessionStepResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<ChoreographerSessionStepResponseDTO> getData() { return data; }
	public long getCount() { return count; }	
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<ChoreographerSessionStepResponseDTO> data) { this.data = data; }
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
