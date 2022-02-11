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

public class InventoryIdDTO implements Serializable {

	// =================================================================================================
	// members

	private String inventoryId;

	// =================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------
	public InventoryIdDTO(final String inventoryId) {
		this.inventoryId = inventoryId;
	}

	// -------------------------------------------------------------------------------------------------
	public String getInventoryId() {
		return inventoryId;
	}

	// -------------------------------------------------------------------------------------------------
	public void setInventoryId(final String inventoryId) {
		this.inventoryId = inventoryId;
	}
}
