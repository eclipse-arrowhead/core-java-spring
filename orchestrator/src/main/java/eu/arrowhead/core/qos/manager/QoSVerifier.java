/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos.manager;

import eu.arrowhead.core.qos.manager.impl.QoSVerificationParameters;

public interface QoSVerifier {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean verify(final QoSVerificationParameters parameters, final boolean isPreVerification);
}