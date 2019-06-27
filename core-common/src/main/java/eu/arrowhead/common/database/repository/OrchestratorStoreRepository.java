package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.OrchestratorStore;

@Repository
public interface OrchestratorStoreRepository extends RefreshableRepository<OrchestratorStore, Long> {

	Optional<OrchestratorStore> findById(Long id);
}
