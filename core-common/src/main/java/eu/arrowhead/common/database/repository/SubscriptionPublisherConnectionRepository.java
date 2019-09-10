package eu.arrowhead.common.database.repository;

import java.util.Set;

import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.SubscriptionPublisherConnection;
import eu.arrowhead.common.database.entity.System;

public interface SubscriptionPublisherConnectionRepository
		extends RefreshableRepository<SubscriptionPublisherConnection, Long> {

	public Set<Subscription> findAllBySystemAndAuthorized(final System validProviderSystem, final boolean authorized);
	public Set<SubscriptionPublisherConnection> findBySubscription(final Subscription subscriptionEntry);

}
