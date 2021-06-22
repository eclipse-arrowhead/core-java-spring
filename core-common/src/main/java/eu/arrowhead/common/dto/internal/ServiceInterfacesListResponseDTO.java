package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;

@JsonInclude(Include.NON_NULL)
public class ServiceInterfacesListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 6737069652359698446L;
	
	private List<ServiceInterfaceResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ServiceInterfacesListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public ServiceInterfacesListResponseDTO(final List<ServiceInterfaceResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<ServiceInterfaceResponseDTO> getData() { return data; }
	public long getCount() { return count; }
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<ServiceInterfaceResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }
}