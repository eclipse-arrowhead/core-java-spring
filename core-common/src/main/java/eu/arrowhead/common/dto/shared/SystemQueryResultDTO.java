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
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@JsonInclude(Include.NON_NULL)
public class SystemQueryResultDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -1822444510232108526L;

	private List<SystemRegistryResponseDTO> systemQueryData = new ArrayList<>();
	private int unfilteredHits = 0;


	//=================================================================================================
	// constructors
	public SystemQueryResultDTO(final List<SystemRegistryResponseDTO> systemQueryData, final int unfilteredHits)
	{
		this.systemQueryData = systemQueryData;
		this.unfilteredHits = unfilteredHits;
	}


	//=================================================================================================
	// methods
	//-------------------------------------------------------------------------------------------------

	public List<SystemRegistryResponseDTO> getSystemQueryData() { return systemQueryData; }
	public int getUnfilteredHits() { return unfilteredHits; }
	//-------------------------------------------------------------------------------------------------

	public void setSystemQueryData(final List<SystemRegistryResponseDTO> systemQueryData) { this.systemQueryData = systemQueryData; }
	public void setUnfilteredHits(final int unfilteredHits) { this.unfilteredHits = unfilteredHits; }

	@Override
	public String toString() {
		return new StringJoiner(", ", SystemQueryResultDTO.class.getSimpleName() + "[", "]")
				.add("systemQueryData=" + systemQueryData)
				.add("unfilteredHits=" + unfilteredHits)
				.toString();
	}
}