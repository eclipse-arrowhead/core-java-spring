package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class SystemQueryResultDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -1822444510232108526L;

	private List<SystemRegistryResponseDTO> systemQueryData = new ArrayList<>();
	private int unfilteredHits = 0;

	//=================================================================================================
	// constructors

	public SystemQueryResultDTO(final List<SystemRegistryResponseDTO> systemQueryData, final int unfilteredHits)
	{
		this.systemQueryData = systemQueryData;
		this.unfilteredHits = unfilteredHits;
	}

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public List<SystemRegistryResponseDTO> getSystemQueryData() { return systemQueryData; }
	public int getUnfilteredHits() { return unfilteredHits; }

	//-------------------------------------------------------------------------------------------------
	public void setSystemQueryData(final List<SystemRegistryResponseDTO> systemQueryData) { this.systemQueryData = systemQueryData; }
	public void setUnfilteredHits(final int unfilteredHits) { this.unfilteredHits = unfilteredHits; }
}