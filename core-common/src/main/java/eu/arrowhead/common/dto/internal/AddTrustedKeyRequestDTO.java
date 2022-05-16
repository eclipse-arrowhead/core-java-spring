/********************************************************************************
 * Copyright (c) 2020 Evopro
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Evopro - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.internal;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

public class AddTrustedKeyRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -1358728410825823717L;

	@NotBlank(message = "The publicKey is mandatory")
    private String publicKey;

    @NotBlank(message = "The description is mandatory")
    private String description;

    @NotBlank(message = "The validAfter field is mandatory")
    private String validAfter;

    @NotBlank(message = "The validBefore field is mandatory")
    private String validBefore;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public AddTrustedKeyRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public AddTrustedKeyRequestDTO(final String publicKey, final String description, final String validAfter, final String validBefore) {
        this.publicKey = publicKey;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
        this.description = description;
    }

    //-------------------------------------------------------------------------------------------------
	public String getPublicKey() { return publicKey; }
	public String getValidBefore() { return validBefore; }
	public String getValidAfter() { return validAfter; }
	public String getDescription() { return description; }

    //-------------------------------------------------------------------------------------------------
	public void setPublicKey(final String publicKey) { this.publicKey = publicKey; }
    public void setValidBefore(final String validBefore) { this.validBefore = validBefore; }
    public void setValidAfter(final String validAfter) { this.validAfter = validAfter; }
    public void setDescription(final String description) { this.description = description; }
    
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