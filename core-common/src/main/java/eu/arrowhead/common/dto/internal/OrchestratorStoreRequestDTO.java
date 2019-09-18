package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.Map;

import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class OrchestratorStoreRequestDTO implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = 6496923524186210327L;
	
	private String serviceDefinitionName;
	private Long consumerSystemId;
	private SystemRequestDTO providerSystemDTO;
	private CloudRequestDTO cloudDTO;
	private String serviceInterfaceName;
	private Integer priority;	
	private Map<String,String> attribute;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreRequestDTO(final String serviceDefinitionName, final Long consumerSystemId, final SystemRequestDTO providerSystemDTO, final CloudRequestDTO cloudDTO,
									   final String serviceInterfaceName, final Integer priority, final Map<String,String> attribute) {
		this.serviceDefinitionName = serviceDefinitionName;
		this.consumerSystemId = consumerSystemId;
		this.providerSystemDTO = providerSystemDTO;
		this.cloudDTO = cloudDTO;
		this.serviceInterfaceName = serviceInterfaceName;
		this.priority = priority;
		this.attribute = attribute;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String getServiceDefinitionName() {return serviceDefinitionName;}
	public Long getConsumerSystemId() {return consumerSystemId;}
	public SystemRequestDTO getProviderSystemDTO() {return providerSystemDTO;}
	public CloudRequestDTO getCloudDTO() {return cloudDTO;}
	public String getServiceInterfaceName() {return serviceInterfaceName;} 
	public Integer getPriority() {return priority;}
	public Map<String,String> getAttribute() {return attribute;}	

	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinitionName(final String serviceDefinitionName) {this.serviceDefinitionName = serviceDefinitionName;}
	public void setConsumerSystemId(final Long consumerSystemId) {this.consumerSystemId = consumerSystemId;}
	public void setProviderSystemDTO(final SystemRequestDTO providerSystemDTO) {this.providerSystemDTO = providerSystemDTO;}
	public void setCloudDTO(final CloudRequestDTO cloudDTO) {this.cloudDTO = cloudDTO;}
	public void setServiceInterfaceName(final String serviceInterfaceName) {this.serviceInterfaceName = serviceInterfaceName; }
	public void setPriority(final Integer priority) {this.priority = priority;}
	public void setAttribute(final Map<String,String> attribute) {this.attribute = attribute;}
}