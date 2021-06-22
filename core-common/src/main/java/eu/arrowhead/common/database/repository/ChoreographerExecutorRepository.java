package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChoreographerExecutorRepository extends RefreshableRepository<ChoreographerExecutor, Long> {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Query("select ex.id " +
            "from ChoreographerStepDetail sdet " +
            "join ChoreographerExecutorServiceDefinition def " +
            "on sdet.serviceDefinition = def.serviceDefinitionName " +
            "and (sdet.version = def.version or def.version between sdet.minVersion and sdet.maxVersion) " +
            "join ChoreographerExecutorServiceDefinitionConnection conn " +
            "on def.id = conn.serviceDefinitionEntry.id " +
            "join ChoreographerExecutor ex " +
            "on conn.executorEntry.id = ex.id " +
            "where sdet.step.id = ?1 " +
            "group by ex.id " +
            "having count(distinct sdet.id) = (select count(id) from ChoreographerStepDetail where step.id = ?1)")
    public Optional<List<Long>> findExecutorsByStepId(final long stepId);

    //-------------------------------------------------------------------------------------------------
    public Optional<ChoreographerExecutor> findByAddressAndPortAndBaseUri(final String address, final int port, final String baseUri);
}
