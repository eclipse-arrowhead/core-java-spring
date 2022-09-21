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

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class LogEntryListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 6116287488543230901L;
	
	private List<LogEntryDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public LogEntryListResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public LogEntryListResponseDTO(final List<LogEntryDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<LogEntryDTO> getData() { return data; }
	public long getCount() { return count; }
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<LogEntryDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }	
}