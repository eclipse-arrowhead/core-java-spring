package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.OrchestratorStore;

@Repository
public interface OrchestratorStoreRepository extends RefreshableRepository<OrchestratorStore, Long> {

}
