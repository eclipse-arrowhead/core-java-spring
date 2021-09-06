/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementAttributesFormDTO;

public class GSDPollResponseDTO implements Serializable, ErrorWrapperDTO {

	//=================================================================================================
	// members
		
	private static final long serialVersionUID = -6771214774324850151L;
	
	private CloudResponseDTO providerCloud;
	private String requiredServiceDefinition;
	private List<String> availableInterfaces;
	private Integer numOfProviders; 
	private List<QoSMeasurementAttributesFormDTO> qosMeasurements;
	private Map<String,String> serviceMetadata;
	private boolean gatewayIsMandatory;
	
	private Set<RelayResponseDTO> verifiedRelays = new HashSet<>(); // Filled up during Inter-Cloud QoS if necessary
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------	
	public GSDPollResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GSDPollResponseDTO(final CloudResponseDTO providerCloud, final String requiredServiceDefinition, final List<String> availableInterfaces,
							  final Integer numOfProviders, final List<QoSMeasurementAttributesFormDTO> qosMeasurements,
							  final Map<String,String> serviceMetadata, final boolean gatewayIsMandatory) {
		this.providerCloud = providerCloud;
		this.requiredServiceDefinition = requiredServiceDefinition;
		this.availableInterfaces = availableInterfaces;
		this.numOfProviders = numOfProviders;
		this.qosMeasurements = qosMeasurements;
		this.serviceMetadata = serviceMetadata;
		this.gatewayIsMandatory = gatewayIsMandatory;
	}

	//-------------------------------------------------------------------------------------------------	
	public CloudResponseDTO getProviderCloud() { return providerCloud; }
	public String getRequiredServiceDefinition() { return requiredServiceDefinition; }
	public List<String> getAvailableInterfaces() { return availableInterfaces; }	
	public Integer getNumOfProviders() { return numOfProviders; }
	public List<QoSMeasurementAttributesFormDTO> getQosMeasurements() { return qosMeasurements; }
	public Map<String,String> getServiceMetadata() { return serviceMetadata; }
	public boolean isGatewayIsMandatory() { return gatewayIsMandatory; }
	public Set<RelayResponseDTO> getVerifiedRelays() { return verifiedRelays; }

	//-------------------------------------------------------------------------------------------------	
	public void setProviderCloud(final CloudResponseDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setRequiredServiceDefinition(final String requiredServiceDefinition) { this.requiredServiceDefinition = requiredServiceDefinition; }
	public void setAvailableInterfaces(final List<String> availableInterfaces) { this.availableInterfaces = availableInterfaces; } 	
	public void setNumOfProviders(final Integer numOfProviders) { this.numOfProviders = numOfProviders; }	
	public void setQosMeasurements(final List<QoSMeasurementAttributesFormDTO> qosMeasurements) { this.qosMeasurements = qosMeasurements; }
	public void setServiceMetadata(final Map<String,String> serviceMetadata) { this.serviceMetadata = serviceMetadata; }
	public void setGatewayIsMandatory(final boolean gatewayIsMandatory) { this.gatewayIsMandatory = gatewayIsMandatory; }
	public void setVerifiedRelays(final HashSet<RelayResponseDTO> verifiedRelays ) { this.verifiedRelays = verifiedRelays; }

	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	@Override
	public boolean isError() {
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}