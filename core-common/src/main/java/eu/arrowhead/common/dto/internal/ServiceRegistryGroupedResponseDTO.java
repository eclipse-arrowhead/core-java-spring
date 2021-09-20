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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class ServiceRegistryGroupedResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 8834608315964460311L;
	
	private List<ServicesGroupedBySystemsResponseDTO> servicesGroupedBySystems;
	private List<ServicesGroupedByServiceDefinitionResponseDTO> servicesGroupedByServiceDefinition;
	private AutoCompleteDataResponseDTO autoCompleteData;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public ServiceRegistryGroupedResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryGroupedResponseDTO(final List<ServicesGroupedBySystemsResponseDTO> servicesGroupedBySystems,
											 final List<ServicesGroupedByServiceDefinitionResponseDTO> servicesGroupedByServiceDefinition, 
											 final AutoCompleteDataResponseDTO autoCompleteData) {
		this.servicesGroupedBySystems = servicesGroupedBySystems;
		this.servicesGroupedByServiceDefinition = servicesGroupedByServiceDefinition;
		this.autoCompleteData = autoCompleteData;
	}
	
	//-------------------------------------------------------------------------------------------------	
	public List<ServicesGroupedBySystemsResponseDTO> getServicesGroupedBySystems() { return servicesGroupedBySystems; }
	public List<ServicesGroupedByServiceDefinitionResponseDTO> getServicesGroupedByServiceDefinition() { return servicesGroupedByServiceDefinition; }
	public AutoCompleteDataResponseDTO getAutoCompleteData() { return autoCompleteData; }

	//-------------------------------------------------------------------------------------------------	
	public void setServicesGroupedBySystems(final List<ServicesGroupedBySystemsResponseDTO> servicesGroupedBySystems) { this.servicesGroupedBySystems = servicesGroupedBySystems; }
	public void setServicesGroupedByServiceDefinition(final List<ServicesGroupedByServiceDefinitionResponseDTO> servicesGroupedByServiceDefinition) { this.servicesGroupedByServiceDefinition = servicesGroupedByServiceDefinition; }
	public void setAutoCompleteData(final AutoCompleteDataResponseDTO autoCompleteData) { this.autoCompleteData = autoCompleteData; }
	
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