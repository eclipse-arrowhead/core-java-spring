package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class DeviceQueryResultDTO implements Serializable
{

    //=================================================================================================
    // members

    private static final long serialVersionUID = -1822444510232108526L;

    private List<DeviceRegistryResponseDTO> deviceQueryData = null;
    private int unfilteredHits = 0;

    //=================================================================================================
    // constructors

	public DeviceQueryResultDTO()
	{
		this(new ArrayList<>(), 0);
	}

	public DeviceQueryResultDTO(final List<DeviceRegistryResponseDTO> deviceQueryData, final int unfilteredHits)
	{
		this.deviceQueryData = deviceQueryData;
		this.unfilteredHits = unfilteredHits;
	}

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public List<DeviceRegistryResponseDTO> getDeviceQueryData() { return deviceQueryData; }

    //-------------------------------------------------------------------------------------------------
    public void setDeviceQueryData(final List<DeviceRegistryResponseDTO> deviceQueryData) { this.deviceQueryData = deviceQueryData; }

    public int getUnfilteredHits() { return unfilteredHits; }

    public void setUnfilteredHits(final int unfilteredHits) { this.unfilteredHits = unfilteredHits; }
}