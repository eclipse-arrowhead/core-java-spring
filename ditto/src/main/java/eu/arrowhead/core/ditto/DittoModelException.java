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

package eu.arrowhead.core.ditto;

public class DittoModelException extends Exception {

	//=================================================================================================
	// members

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public DittoModelException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	//-------------------------------------------------------------------------------------------------
	public DittoModelException(final String msg) {
		super(msg);
	}

}
