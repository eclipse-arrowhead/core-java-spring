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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.StringJoiner;

@JsonInclude(Include.NON_NULL)
public class OnboardingWithNameRequestDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 1L;

    private CertificateCreationRequestDTO creationRequestDTO;

    //=================================================================================================
    // constructors

    public OnboardingWithNameRequestDTO() {
    }

    public OnboardingWithNameRequestDTO(CertificateCreationRequestDTO creationRequestDTO) {
        this.creationRequestDTO = creationRequestDTO;
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------

    public CertificateCreationRequestDTO getCreationRequestDTO() {
        return creationRequestDTO;
    }

    public void setCreationRequestDTO(CertificateCreationRequestDTO creationRequestDTO) {
        this.creationRequestDTO = creationRequestDTO;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OnboardingWithNameRequestDTO.class.getSimpleName() + "[", "]")
                .add("creationRequestDTO=" + creationRequestDTO)
                .add("parent=" + super.toString())
                .toString();
    }
}