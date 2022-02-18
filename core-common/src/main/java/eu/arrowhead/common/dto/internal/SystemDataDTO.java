/********************************************************************************
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SystemDataDTO implements Serializable {

	// =================================================================================================
	// members

	private String data;

	// =================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------
	public SystemDataDTO(@JsonProperty("systemData") final String data) {
		this.data = data;
	}

	// -------------------------------------------------------------------------------------------------
	public String getSystemData() {
		return data;
	}

	// -------------------------------------------------------------------------------------------------
	public void setData(final String data) {
		this.data = data;
	}
}
