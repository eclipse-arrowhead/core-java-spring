package eu.arrowhead.common.dto;

import java.io.Serializable;

public class GatewayProviderConnectionResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -4211429949859969009L;
	
	private String queueName;
	private String controlQueueName;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public GatewayProviderConnectionResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GatewayProviderConnectionResponseDTO(final String queueName, final String controlQueueName) {
		this.queueName = queueName;
		this.controlQueueName = controlQueueName;
	}

	//-------------------------------------------------------------------------------------------------
	public String getQueueName() { return queueName; }
	public String getControlQueueName() { return controlQueueName; }

	//-------------------------------------------------------------------------------------------------
	public void setQueueName(final String queueName) { this.queueName = queueName; }
	public void setControlQueueName(final String controlQueueName) { this.controlQueueName = controlQueueName; }		
}