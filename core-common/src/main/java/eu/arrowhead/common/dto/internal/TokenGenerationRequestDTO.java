package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class TokenGenerationRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 1323986110293510298L;
	
	private SystemRequestDTO consumer;
	private CloudRequestDTO consumerCloud;
	private List<TokenGenerationProviderDTO> providers = new ArrayList<>();
	private String service;
	private Integer duration; // in minutes
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public TokenGenerationRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationRequestDTO(final SystemRequestDTO consumer, final CloudRequestDTO consumerCloud, final List<TokenGenerationProviderDTO> providers, final String service,
									 final Integer duration) {
		Assert.notNull(consumer, "Consumer is null.");
		Assert.isTrue(providers != null && !providers.isEmpty(), "Provider list is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(service), "Service is null or blank.");
		
		this.consumer = consumer;
		this.consumerCloud = consumerCloud;
		this.providers = providers;
		this.service = service;
		this.duration = duration;
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getConsumer() { return consumer; }
	public CloudRequestDTO getConsumerCloud() { return consumerCloud; }
	public List<TokenGenerationProviderDTO> getProviders() { return providers; }
	public String getService() { return service; }
	public Integer getDuration() { return duration; }
	
	//-------------------------------------------------------------------------------------------------
	public void setConsumer(final SystemRequestDTO consumer) { this.consumer = consumer; }
	public void setConsumerCloud(final CloudRequestDTO consumerCloud) { this.consumerCloud = consumerCloud; }
	public void setProviders(final List<TokenGenerationProviderDTO> providers) { this.providers = providers; }
	public void setService(final String service) { this.service = service; }
	public void setDuration(final Integer duration) { this.duration = duration; }
}