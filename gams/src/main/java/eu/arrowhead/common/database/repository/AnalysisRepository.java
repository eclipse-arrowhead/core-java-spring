package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Optional;

import eu.arrowhead.common.database.entity.AbstractEvaluation;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.core.gams.dto.AnalysisType;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisRepository<S extends AbstractEvaluation> extends RefreshableRepository<S, Long> {

    List<S> findBySensor(final Sensor sensor);
    Optional<S> findBySensorAndType(final Sensor sensor, final AnalysisType type);
    List<S> findByType(final AnalysisType type);
}
