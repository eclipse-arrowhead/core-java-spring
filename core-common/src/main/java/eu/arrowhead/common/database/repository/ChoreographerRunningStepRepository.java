package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerRunningStep;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChoreographerRunningStepRepository extends RefreshableRepository<ChoreographerRunningStep, Long> {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public Optional<ChoreographerRunningStep> findByStepIdAndSessionId(final long stepId, final long sessionId);
    public List<ChoreographerRunningStep> findAllBySessionId(final long sessionId);
}
