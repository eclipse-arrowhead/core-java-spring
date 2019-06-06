package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SystemListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -1661484009332215820L;
	
	private List<SystemResponseDTO> data = new ArrayList<>();
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	
	public SystemListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	
	public SystemListResponseDTO(final List<SystemResponseDTO> systemResponeDTOList, final int totalNumberOfSystems) {
		super();
		this.data = systemResponeDTOList;
		this.count = totalNumberOfSystems;
	}
	
	//-------------------------------------------------------------------------------------------------
	
	public List<SystemResponseDTO> getData() {
		return data;
	}

	//-------------------------------------------------------------------------------------------------
	
	public long getCount() {
		return count;
	}

	//-------------------------------------------------------------------------------------------------
	
	public void setData(List<SystemResponseDTO> data) {
		this.data = data;
	}

	//-------------------------------------------------------------------------------------------------
	
	public void setCount(long count) {
		this.count = count;
	}
}
