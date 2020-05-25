package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

public class QoSBestRelayRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -8357593897933589292L;
	
	private CloudResponseDTO cloud;
	private String attribute;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSBestRelayRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSBestRelayRequestDTO(final CloudResponseDTO cloud, final String attribute) {
		this.cloud = cloud;
		this.attribute = attribute;
	}
	
	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO getCloud() { return cloud; }
	public String getAttribute() { return attribute; }

	//-------------------------------------------------------------------------------------------------
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setAttribute(final String attribute) { this.attribute = attribute; }
}
