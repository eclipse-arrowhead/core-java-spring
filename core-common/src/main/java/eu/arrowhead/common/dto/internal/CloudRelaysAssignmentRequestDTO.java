package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class CloudRelaysAssignmentRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -999808999290231598L;
	
	private Long cloudId;
	private List<Long> gatekeeperRelayIds;
	private List<Long> gatewayRelayIds;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Long getCloudId() { return cloudId; }
	public List<Long> getGatekeeperRelayIds() { return gatekeeperRelayIds; }
	public List<Long> getGatewayRelayIds() { return gatewayRelayIds; }
	
	//-------------------------------------------------------------------------------------------------
	public void setCloudId(final Long cloudId) { this.cloudId = cloudId; }
	public void setGatekeeperRelayIds(final List<Long> gatekeeperRelayIds) { this.gatekeeperRelayIds = gatekeeperRelayIds; }
	public void setGatewayRelayIds(final List<Long> gatewayRelayIds) { this.gatewayRelayIds = gatewayRelayIds; }
}