package eu.arrowhead.common.dto.internal;

import java.util.List;

public class CloudWithRelaysAndPublicRelaysResponseDTO extends CloudResponseDTO {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -7909487804122446800L;

	private List<RelayResponseDTO> gatekeeperRelays;
	private List<RelayResponseDTO> gatewayRelays;
	private List<RelayResponseDTO> publicRelays;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysAndPublicRelaysResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysAndPublicRelaysResponseDTO(final long id, final String operator, final String name, final boolean secure, final boolean neighbor, final boolean ownCloud, final String authenticationInfo,
									  final String createdAt, final String updatedAt, final List<RelayResponseDTO> gatekeeperRelays, final List<RelayResponseDTO> gatewayRelays, final List<RelayResponseDTO> publicRelays) {
		super(id, operator, name, secure, neighbor, ownCloud, authenticationInfo, createdAt, updatedAt);
		this.gatekeeperRelays = gatekeeperRelays;
		this.gatewayRelays = gatewayRelays;
		this.publicRelays = publicRelays;
	}

	//-------------------------------------------------------------------------------------------------
	public List<RelayResponseDTO> getGatekeeperRelays() { return gatekeeperRelays; }
	public List<RelayResponseDTO> getGatewayRelays() { return gatewayRelays; }
	public List<RelayResponseDTO> getPublicRelays() { return publicRelays; }

	//-------------------------------------------------------------------------------------------------
	public void setGatekeeperRelays(final List<RelayResponseDTO> gatekeeperRelays) { this.gatekeeperRelays = gatekeeperRelays; }
	public void setGatewayRelays(final List<RelayResponseDTO> gatewayRelays) { this.gatewayRelays = gatewayRelays; }
	public void setPublicRelays(final List<RelayResponseDTO> publicRelays) { this.publicRelays = publicRelays; }
}