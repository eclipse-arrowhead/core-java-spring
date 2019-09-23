package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ServiceQueryResultDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -1822444510232108526L;
	
	private List<ServiceRegistryResponseDTO> serviceQueryData = new ArrayList<>();
	private int unfilteredHits = 0;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public List<ServiceRegistryResponseDTO> getServiceQueryData() { return serviceQueryData; }
	public int getUnfilteredHits() { return unfilteredHits; }

	//-------------------------------------------------------------------------------------------------
	public void setServiceQueryData(final List<ServiceRegistryResponseDTO> serviceQueryData) { this.serviceQueryData = serviceQueryData; }
	public void setUnfilteredHits(final int unfilteredHits) { this.unfilteredHits = unfilteredHits; }
}