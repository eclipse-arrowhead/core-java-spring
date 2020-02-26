package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.CloudRequestDTO;

public class QoSRelayTestProposalRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -5314341474633023883L;
	
	private CloudRequestDTO targetCloud;
	private RelayRequestDTO relay;
	private String senderQoSMonitorPublicKey;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalRequestDTO(final CloudRequestDTO targetCloud, final RelayRequestDTO relay) {
		Assert.notNull(targetCloud, "Target cloud is null.");
		Assert.notNull(relay, "relay is null.");
		
		this.targetCloud = targetCloud;
		this.relay = relay;
	}
	
	//-------------------------------------------------------------------------------------------------
	public CloudRequestDTO getTargetCloud() { return targetCloud; }
	public RelayRequestDTO getRelay() { return relay; }
	public String getSenderQoSMonitorPublicKey() { return senderQoSMonitorPublicKey; }
	
	//-------------------------------------------------------------------------------------------------
	public void setTargetCloud(final CloudRequestDTO targetCloud) { this.targetCloud = targetCloud; }
	public void setRelay(final RelayRequestDTO relay) { this.relay = relay; }
	public void setSenderQoSMonitorPublicKey(final String senderQoSMonitorPublicKey) { this.senderQoSMonitorPublicKey = senderQoSMonitorPublicKey; }
}