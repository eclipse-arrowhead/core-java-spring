package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;

@JsonInclude(Include.NON_NULL)
public class ServiceRegistryListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3892383727230105100L;
	
	private List<ServiceRegistryResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO(final List<ServiceRegistryResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ServiceRegistryResponseDTO> getData() {return data;}
	public long getCount() {return count;}
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<ServiceRegistryResponseDTO> data) {this.data = data;}
	public void setCount(final long count) {this.count = count;}	
}