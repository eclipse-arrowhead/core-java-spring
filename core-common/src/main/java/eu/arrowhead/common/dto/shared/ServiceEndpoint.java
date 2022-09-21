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
import java.net.URI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.core.CoreSystemService;

public class ServiceEndpoint implements Serializable {

	//=================================================================================================
    // members

	private static final long serialVersionUID = -8766267910533700737L;
	
    private CoreSystemService service;
    private URI uri;

    //=================================================================================================
	// methods 
    
    //-------------------------------------------------------------------------------------------------
	public ServiceEndpoint() {}

    //-------------------------------------------------------------------------------------------------
	public ServiceEndpoint(final CoreSystemService service, final URI uri) {
        this.service = service;
        this.uri = uri;
    }

    //-------------------------------------------------------------------------------------------------
	public CoreSystemService getService() { return service; }
	public URI getUri() { return uri; }
    
	//-------------------------------------------------------------------------------------------------
	public void setService(final CoreSystemService service) { this.service = service; }
    public void setUri(final URI uri) { this.uri = uri; }

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