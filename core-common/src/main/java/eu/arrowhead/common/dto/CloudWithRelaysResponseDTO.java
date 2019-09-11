package eu.arrowhead.common.dto;

import java.util.List;

public class CloudWithRelaysResponseDTO extends CloudResponseDTO {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -4289255963612036571L;
	
	private List<RelayResponseDTO> gatekeeperRelays; 
	private List<RelayResponseDTO> gatewayRelays;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysResponseDTO(final long id, final String operator, final String name, final boolean secure, final boolean neighbor, final boolean ownCloud, final String authenticationInfo,
									  final String createdAt, final String updatedAt, final List<RelayResponseDTO> gatekeeperRelays, final List<RelayResponseDTO> gatewayRelays) {
		super(id, operator, name, secure, neighbor, ownCloud, authenticationInfo, createdAt, updatedAt);
		this.gatekeeperRelays = gatekeeperRelays;
		this.gatewayRelays = gatewayRelays;
	}

	//-------------------------------------------------------------------------------------------------
	public List<RelayResponseDTO> getGatekeeperRelays() { return gatekeeperRelays; }
	public List<RelayResponseDTO> getGatewayRelays() { return gatewayRelays; }

	//-------------------------------------------------------------------------------------------------
	public void setGatekeeperRelays(final List<RelayResponseDTO> gatekeeperRelays) { this.gatekeeperRelays = gatekeeperRelays; }
	public void setGatewayRelays(final List<RelayResponseDTO> gatewayRelays) { this.gatewayRelays = gatewayRelays; }	
}