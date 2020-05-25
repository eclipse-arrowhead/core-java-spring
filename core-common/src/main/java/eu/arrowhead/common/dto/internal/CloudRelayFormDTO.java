package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

public class CloudRelayFormDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -2722459200530984365L;
	
	private CloudResponseDTO cloud;
	private RelayResponseDTO relay;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudRelayFormDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public CloudRelayFormDTO(final CloudResponseDTO cloud, final RelayResponseDTO relay) {
		this.cloud = cloud;
		this.relay = relay;
	}

	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO getCloud() { return cloud; }
	public RelayResponseDTO getRelay() { return relay; }

	//-------------------------------------------------------------------------------------------------
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setRelay(final RelayResponseDTO relay) { this.relay = relay; }	
}
