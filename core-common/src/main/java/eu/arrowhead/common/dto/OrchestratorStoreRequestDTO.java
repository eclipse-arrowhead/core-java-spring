package eu.arrowhead.common.dto;

import java.io.Serializable;

public class OrchestratorStoreRequestDTO implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = 6496923524186210327L;
	
	private ServiceDefinitionRequestDTO serviceDefinitionDTO;
	private SystemRequestDTO consumerSystemDTO;
	private SystemRequestDTO providerSystemDTO;
	private CloudRequestDTO cloudDTO;
	private Integer priority;	
	private String attribute;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreRequestDTO(final ServiceDefinitionRequestDTO serviceDefinitionDTO, final SystemRequestDTO consumerSystemDTO,
			final SystemRequestDTO providerSystemDTO, final CloudRequestDTO cloudDTO, final Integer priority, final String attribute) {
		super();
		this.serviceDefinitionDTO = serviceDefinitionDTO;
		this.consumerSystemDTO = consumerSystemDTO;
		this.providerSystemDTO = providerSystemDTO;
		this.cloudDTO = cloudDTO;
		this.priority = priority;
		this.attribute = attribute;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionRequestDTO getServiceDefinition() {return serviceDefinitionDTO;}
	public SystemRequestDTO getConsumerSystemDTO() {return consumerSystemDTO;}
	public SystemRequestDTO getProviderSystemDTO() {return providerSystemDTO;}
	public CloudRequestDTO getCloudDTO() {return cloudDTO;}
	public Integer getPriority() {return priority;}
	public String getAttribute() {return attribute;}	

	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinitionId(final ServiceDefinitionRequestDTO serviceDefinitionDTO) {this.serviceDefinitionDTO = serviceDefinitionDTO;}
	public void setConsumerSystemId(final SystemRequestDTO consumerSystemDTO) {this.consumerSystemDTO = consumerSystemDTO;}
	public void setProviderSystemId(final SystemRequestDTO providerSystemDTO) {this.providerSystemDTO = providerSystemDTO;}
	public void setCloudId(final CloudRequestDTO cloudDTO) {this.cloudDTO = cloudDTO;}
	public void setPriority(final Integer priority) {this.priority = priority;}
	public void setAttribute(final String attribute) {this.attribute = attribute;}
}
