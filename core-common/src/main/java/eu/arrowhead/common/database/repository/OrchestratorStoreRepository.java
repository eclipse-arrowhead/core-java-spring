package eu.arrowhead.common.database.repository;

import java.util.Optional;

import eu.arrowhead.common.database.entity.OrchestratorStore;

public interface OrchestratorStoreRepository extends RefreshableRepository<OrchestratorStore, Long> {

	Optional<OrchestratorStore> findById(Long id);
}
