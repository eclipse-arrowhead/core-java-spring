package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;

@Repository
public interface SubscriptionRepository extends RefreshableRepository<Subscription, Long> {

	public Set<Subscription> findAllByEventType(final EventType validEventType);
	public List<Subscription> findAllBySubscriberSystem(final System system);
	public Optional<Subscription> findByEventTypeAndSubscriberSystem(final EventType eventType, final System subscriberSystem);

}
