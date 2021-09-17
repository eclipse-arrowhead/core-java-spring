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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KeyPairDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 4141153476434846717L;
	
	private String keyAlgorithm;
    private String keyFormat;
    private String publicKey;
    private String privateKey;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public KeyPairDTO() {}

    //-------------------------------------------------------------------------------------------------
	public KeyPairDTO(final String keyAlgorithm, final String keyFormat, final String publicKey, final String privateKey) {
        this.keyAlgorithm = keyAlgorithm;
        this.keyFormat = keyFormat;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    //-------------------------------------------------------------------------------------------------
	public String getKeyAlgorithm() { return keyAlgorithm; }
	public String getKeyFormat() { return keyFormat; }
	public String getPublicKey() { return publicKey; }
	public String getPrivateKey() { return privateKey; }

    //-------------------------------------------------------------------------------------------------
	public void setKeyAlgorithm(final String keyAlgorithm) { this.keyAlgorithm = keyAlgorithm; }
    public void setKeyFormat(final String keyFormat) { this.keyFormat = keyFormat; }
    public void setPublicKey(final String publicKey) { this.publicKey = publicKey; }
    public void setPrivateKey(final String privateKey) { this.privateKey = privateKey; }

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