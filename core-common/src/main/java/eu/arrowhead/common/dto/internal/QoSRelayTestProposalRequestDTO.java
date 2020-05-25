package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.CloudRequestDTO;

public class QoSRelayTestProposalRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 8484001108736724458L;
	
	private CloudRequestDTO requesterCloud;
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
	public CloudRequestDTO getRequesterCloud() { return requesterCloud; }
	
	//-------------------------------------------------------------------------------------------------
	public void setTargetCloud(final CloudRequestDTO targetCloud) { this.targetCloud = targetCloud; }
	public void setRelay(final RelayRequestDTO relay) { this.relay = relay; }
	public void setSenderQoSMonitorPublicKey(final String senderQoSMonitorPublicKey) { this.senderQoSMonitorPublicKey = senderQoSMonitorPublicKey; }
	public void setRequesterCloud(final CloudRequestDTO requesterCloud) { this.requesterCloud = requesterCloud; }
}