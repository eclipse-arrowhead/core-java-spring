package eu.arrowhead.common.dto;

import java.io.Serializable;

public class CloudRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 7397917411719621910L;
	
	private String operator;
	private String name;
	private Boolean secure;
	private Boolean neighbor;
	private Boolean ownCloud;
	private String authenticationInfo;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String getOperator() { return operator; }
	public String getName() { return name; }
	public Boolean getSecure() { return secure; }
	public Boolean getNeighbor() { return neighbor; }
	public Boolean getOwnCloud() { return ownCloud; }	
	public String getAuthenticationInfo() { return authenticationInfo; }
	
	//-------------------------------------------------------------------------------------------------
	public void setOperator(final String operator) { this.operator = operator; }
	public void setName(final String name) { this.name = name; }
	public void setSecure(final Boolean secure) { this.secure = secure; }
	public void setNeighbor(final Boolean neighbor) { this.neighbor = neighbor; }
	public void setOwnCloud(final Boolean ownCloud) { this.ownCloud = ownCloud; }
	public void setAuthenticationInfo(String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
}