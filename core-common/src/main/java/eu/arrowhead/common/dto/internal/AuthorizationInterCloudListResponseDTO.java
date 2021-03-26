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

public class AuthorizationInterCloudListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 893654899355518416L;
	
	private List<AuthorizationInterCloudResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudListResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudListResponseDTO(final List<AuthorizationInterCloudResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<AuthorizationInterCloudResponseDTO> getData() { return data; }
	public long getCount() { return count; }
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<AuthorizationInterCloudResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }	
}