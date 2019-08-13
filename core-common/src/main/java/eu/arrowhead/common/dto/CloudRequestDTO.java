package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class CloudRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 7397917411719621910L;
	
	private String operator;
	private String name;
	private Boolean secure;
	private Boolean neighbor;
	private String authenticationInfo;
	private List<Long> gatekeeperRelayIds;
	private List<Long> gatewayRelayIds;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String getOperator() { return operator; }
	public String getName() { return name; }
	public Boolean getSecure() { return secure; }
	public Boolean getNeighbor() { return neighbor; }
	public String getAuthenticationInfo() { return authenticationInfo; }	
	public List<Long> getGatekeeperRelayIds() { return gatekeeperRelayIds; }	
	public List<Long> getGatewayRelayIds() { return gatewayRelayIds; }
	//-------------------------------------------------------------------------------------------------
	public void setOperator(final String operator) { this.operator = operator; }
	public void setName(final String name) { this.name = name; }
	public void setSecure(final Boolean secure) { this.secure = secure; }
	public void setNeighbor(final Boolean neighbor) { this.neighbor = neighbor; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setGatekeeperRelayIds(final List<Long> gatekeeperRelayIds) { this.gatekeeperRelayIds = gatekeeperRelayIds; }
	public void setGatewayRelayIds(final List<Long> gatewayRelayIds) { this.gatewayRelayIds = gatewayRelayIds; }
	
}