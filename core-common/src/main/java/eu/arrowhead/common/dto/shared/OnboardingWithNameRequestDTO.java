/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class OnboardingWithNameRequestDTO implements Serializable {

	//=================================================================================================
    // members

	private static final long serialVersionUID = -4222019369804191344L;

	private CertificateCreationRequestDTO creationRequestDTO;

    //=================================================================================================
    // constructors

    //-------------------------------------------------------------------------------------------------
	public OnboardingWithNameRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public OnboardingWithNameRequestDTO(final CertificateCreationRequestDTO creationRequestDTO) {
        this.creationRequestDTO = creationRequestDTO;
    }

    //-------------------------------------------------------------------------------------------------
    public CertificateCreationRequestDTO getCreationRequestDTO() { return creationRequestDTO; }

    //-------------------------------------------------------------------------------------------------
	public void setCreationRequestDTO(final CertificateCreationRequestDTO creationRequestDTO) { this.creationRequestDTO = creationRequestDTO; }

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