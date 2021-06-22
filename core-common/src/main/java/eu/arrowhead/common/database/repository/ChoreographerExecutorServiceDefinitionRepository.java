package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerExecutorServiceDefinition;

import java.util.Optional;

public interface ChoreographerExecutorServiceDefinitionRepository extends RefreshableRepository<ChoreographerExecutorServiceDefinition, Long>{

    //=================================================================================================
    // methods

    public Optional<ChoreographerExecutorServiceDefinition> findByServiceDefinitionNameAndVersion(final String serviceDefinitionName, final Integer version);
}
