package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChoreographerExecutorRepository extends RefreshableRepository<ChoreographerExecutor, Long> {

    //=================================================================================================
    // methods

    public Optional<ChoreographerExecutor> findByServiceDefinitionNameAndVersion(final String serviceDefinitionName, final Integer version);
}
