/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

public class ChoreographerServiceQueryFormDTO extends ServiceQueryFormDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 4310676705715417602L;
	
	private boolean localCloudOnly = false;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ChoreographerServiceQueryFormDTO() {
		super();
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerServiceQueryFormDTO(final ServiceQueryFormDTO queryForm, final boolean localCloudOnly) {
		super();
		
		this.setServiceDefinitionRequirement(queryForm.getServiceDefinitionRequirement());
		this.setInterfaceRequirements(queryForm.getInterfaceRequirements());
		this.setSecurityRequirements(queryForm.getSecurityRequirements());
		this.setMetadataRequirements(queryForm.getMetadataRequirements());
		this.setVersionRequirement(queryForm.getVersionRequirement());
		this.setMinVersionRequirement(queryForm.getMinVersionRequirement());
		this.setMaxVersionRequirement(queryForm.getMaxVersionRequirement());
		this.setPingProviders(queryForm.getPingProviders());
		this.localCloudOnly = localCloudOnly;
	}

	//-------------------------------------------------------------------------------------------------
	public boolean isLocalCloudOnly() { return localCloudOnly; }

	//-------------------------------------------------------------------------------------------------
	public void setLocalCloudOnly(boolean localCloudOnly) { this.localCloudOnly = localCloudOnly; }
}