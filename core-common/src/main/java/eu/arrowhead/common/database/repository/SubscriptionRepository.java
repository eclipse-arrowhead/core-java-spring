package eu.arrowhead.common.database.repository;

import java.util.Set;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;

@Repository
public interface SubscriptionRepository extends RefreshableRepository<Subscription, Long> {

	public Set<Subscription> findAllByEventType(final EventType validEventType);

}
