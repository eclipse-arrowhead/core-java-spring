package eu.arrowhead.common.dto;

import java.io.Serializable;

public class GatewayConsumerConnectionRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 8982448541331553443L;
	
	private RelayRequestDTO relay;
	private String queueName;
	private String controlQueueName;
	private SystemRequestDTO consumer;
	private SystemRequestDTO provider;
	private CloudRequestDTO consumerCloud;
	private CloudRequestDTO providerCloud;
	private String serviceDefinition;
	private boolean isSecure;
	private int timeout;
	private String providerGWPublicKey;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public GatewayConsumerConnectionRequestDTO() {}

	//-------------------------------------------------------------------------------------------------
	public GatewayConsumerConnectionRequestDTO(final RelayRequestDTO relay, final String queueName, final String controlQueueName, final SystemRequestDTO consumer, final SystemRequestDTO provider, final CloudRequestDTO consumerCloud, 
											   final CloudRequestDTO providerCloud, final String serviceDefinition, final boolean isSecure, final int timeout, final String providerGWPublicKey) {
		this.relay = relay;
		this.queueName = queueName;
		this.controlQueueName = controlQueueName;
		this.consumer = consumer;
		this.provider = provider;
		this.consumerCloud = consumerCloud;
		this.providerCloud = providerCloud;
		this.serviceDefinition = serviceDefinition;
		this.isSecure = isSecure;
		this.timeout = timeout;
		this.providerGWPublicKey = providerGWPublicKey;
	}

	//-------------------------------------------------------------------------------------------------
	public RelayRequestDTO getRelay() { return relay; }
	public String getQueueName() { return queueName; }
	public String getControlQueueName() { return controlQueueName; }
	public SystemRequestDTO getConsumer() { return consumer; }
	public SystemRequestDTO getProvider() { return provider; }
	public CloudRequestDTO getConsumerCloud() { return consumerCloud; }
	public CloudRequestDTO getProviderCloud() { return providerCloud; }
	public String getServiceDefinition() { return serviceDefinition; }
	public boolean isSecure() { return isSecure; }
	public int getTimeout() { return timeout; }
	public String getProviderGWPublicKey() { return providerGWPublicKey; }

	//-------------------------------------------------------------------------------------------------
	public void setRelay(final RelayRequestDTO relay) { this.relay = relay; }
	public void setQueueName(final String queueName) { this.queueName = queueName; }
	public void setControlQueueName(final String controlQueueName) { this.controlQueueName = controlQueueName; }
	public void setConsumer(final SystemRequestDTO consumer) { this.consumer = consumer; }
	public void setProvider(final SystemRequestDTO provider) { this.provider = provider; }
	public void setConsumerCloud(final CloudRequestDTO consumerCloud) { this.consumerCloud = consumerCloud; }
	public void setProviderCloud(final CloudRequestDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setSecure(final boolean isSecure) { this.isSecure = isSecure; }
	public void setTimeout(final int timeout) { this.timeout = timeout; }
	public void setProviderGWPublicKey(final String providerGWPublicKey) { this.providerGWPublicKey = providerGWPublicKey; }	
}
