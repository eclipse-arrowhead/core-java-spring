package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerActionStep;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChoreographerActionStepRepository extends RefreshableRepository<ChoreographerActionStep, Long> {

    public Optional<ChoreographerActionStep> findByName(final String name);
}
