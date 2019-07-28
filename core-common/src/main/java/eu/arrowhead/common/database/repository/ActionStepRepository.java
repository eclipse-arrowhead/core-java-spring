package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ActionStep;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionStepRepository extends RefreshableRepository<ActionStep, Long> {
}
