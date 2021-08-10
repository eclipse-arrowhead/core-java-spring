package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerExecutorServiceDefinition;

import java.util.List;
import java.util.Optional;

public interface ChoreographerExecutorServiceDefinitionRepository extends RefreshableRepository<ChoreographerExecutorServiceDefinition, Long>{

    //=================================================================================================
    // methods

    public Optional<ChoreographerExecutorServiceDefinition> findByExecutorAndServiceDefinition(final ChoreographerExecutor executor, final String serviceDefinition);
    public List<ChoreographerExecutorServiceDefinition> findAllByExecutor(final ChoreographerExecutor executor);
}
