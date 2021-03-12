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

package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Set;

import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.SubscriptionPublisherConnection;
import eu.arrowhead.common.database.entity.System;

public interface SubscriptionPublisherConnectionRepository extends RefreshableRepository<SubscriptionPublisherConnection,Long> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public List<SubscriptionPublisherConnection> findAllBySystemAndAuthorized(final System providerSystem, final boolean authorized);
	public Set<SubscriptionPublisherConnection> findBySubscriptionEntry(final Subscription subscriptionEntry);
}