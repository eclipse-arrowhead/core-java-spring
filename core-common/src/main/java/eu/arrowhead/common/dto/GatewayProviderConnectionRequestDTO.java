package eu.arrowhead.common.dto;

import java.io.Serializable;

public class GatewayProviderConnectionRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -5914515159810300399L;
	
	private RelayRequestDTO relay;
	private SystemRequestDTO consumer;
	private SystemRequestDTO provider;
	private CloudRequestDTO consumerCloud;
	private CloudRequestDTO providerCloud;
	private String serviceDefinition;
	private boolean isSecure;
	private int timeout;
	private String consumerGWPublicKey;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public GatewayProviderConnectionRequestDTO() {
	}
	
	//-------------------------------------------------------------------------------------------------
	public GatewayProviderConnectionRequestDTO(final RelayRequestDTO relay, final SystemRequestDTO consumer, final SystemRequestDTO provider, final CloudRequestDTO consumerCloud, final CloudRequestDTO providerCloud,
											   final String serviceDefinition, final boolean isSecure, final int timeout, final String consumerGWPublicKey) {
		this.relay = relay;
		this.consumer = consumer;
		this.provider = provider;
		this.consumerCloud = consumerCloud;
		this.providerCloud = providerCloud;
		this.serviceDefinition = serviceDefinition;
		this.isSecure = isSecure;
		this.timeout = timeout;
		this.consumerGWPublicKey = consumerGWPublicKey;
	}

	//-------------------------------------------------------------------------------------------------
	public RelayRequestDTO getRelay() { return relay; }
	public SystemRequestDTO getConsumer() { return consumer; }
	public SystemRequestDTO getProvider() { return provider; }
	public CloudRequestDTO getConsumerCloud() { return consumerCloud; }
	public CloudRequestDTO getProviderCloud() { return providerCloud; }
	public String getServiceDefinition() { return serviceDefinition; }
	public boolean isSecure() { return isSecure; }
	public int getTimeout() { return timeout; }
	public String getConsumerGWPublicKey() { return consumerGWPublicKey; }

	public void setRelay(final RelayRequestDTO relay) { this.relay = relay; }
	public void setConsumer(final SystemRequestDTO consumer) { this.consumer = consumer; }
	public void setProvider(final SystemRequestDTO provider) { this.provider = provider; }
	public void setConsumerCloud(final CloudRequestDTO consumerCloud) { this.consumerCloud = consumerCloud; }
	public void setProviderCloud(final CloudRequestDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setSecure(final boolean isSecure) { this.isSecure = isSecure; }
	public void setTimeout(final int timeout) { this.timeout = timeout; }
	public void setConsumerGWPublicKey(final String consumerGWPublicKey) { this.consumerGWPublicKey = consumerGWPublicKey; }	
}
