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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class OnboardingWithCsrRequestDTO implements Serializable {

	//=================================================================================================
    // members

	private static final long serialVersionUID = -2753603721068210444L;

	private String certificateSigningRequest;

    //=================================================================================================
    // methods
    
	//-------------------------------------------------------------------------------------------------
    public String getCertificateSigningRequest() { return certificateSigningRequest; }

    //-------------------------------------------------------------------------------------------------
    public void setCertificateSigningRequest(final String certificateSigningRequest) { this.certificateSigningRequest = certificateSigningRequest; }

    //-------------------------------------------------------------------------------------------------
    @Override
    public int hashCode() {
        return Objects.hash(certificateSigningRequest);
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OnboardingWithCsrRequestDTO other = (OnboardingWithCsrRequestDTO) obj;

        return Objects.equals(certificateSigningRequest, other.certificateSigningRequest);
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