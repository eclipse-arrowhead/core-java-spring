package eu.arrowhead.core.gateway.service;

import java.io.Serializable;
import java.util.List;

public class ActiveSessionListDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6563407010792596047L;
	
	private List<ActiveSessionDTO> data;
	private long count;
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ActiveSessionListDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ActiveSessionListDTO(final List<ActiveSessionDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<ActiveSessionDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<ActiveSessionDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }	
}
