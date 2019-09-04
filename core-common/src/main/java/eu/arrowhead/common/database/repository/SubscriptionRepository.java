package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Subscription;

@Repository
public interface SubscriptionRepository extends RefreshableRepository<Subscription, Long> {

}
