package eu.arrowhead.common.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.OrchestratorStore;

@Repository
public interface OrchestratorStoreRepository extends RefreshableRepository<OrchestratorStore, Long> {
	
	@Query("SELECT entry FROM OrchestratorStore entry WHERE priority = ?1 ")
	public Page<OrchestratorStore> findAllByPriority(final int priority, PageRequest regRequest);
	
}
