package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class ServiceDefinitionsListResponseDTO implements Serializable {

	private static final long serialVersionUID = 6737069652359698446L;
	
	//=================================================================================================
	// members
	
	private List<ServiceDefinitionResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionsListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionsListResponseDTO(final List<ServiceDefinitionResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<ServiceDefinitionResponseDTO> getData() { return data; }
	public long getCount() { return count; }
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<ServiceDefinitionResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }
}