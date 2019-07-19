package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.CloudGatekeeper;

@Repository
public interface CloudGatekeeperRepository extends RefreshableRepository<CloudGatekeeper,Long> {
}