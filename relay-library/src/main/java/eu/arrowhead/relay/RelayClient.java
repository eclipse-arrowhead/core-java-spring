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

package eu.arrowhead.relay;

import javax.jms.JMSException;
import javax.jms.Session;

public interface RelayClient {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Session createConnection(final String host, final int port, final boolean secure) throws JMSException;
	public void closeConnection(final Session session);
	public boolean isConnectionClosed(final Session session);
}