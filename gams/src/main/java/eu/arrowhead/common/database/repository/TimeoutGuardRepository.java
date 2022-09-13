package eu.arrowhead.common.database.repository;

import java.util.Optional;

import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.entity.TimeoutGuard;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeoutGuardRepository extends RefreshableRepository<TimeoutGuard, Long> {

    <S extends TimeoutGuard> Optional<S> findBySensor(final Sensor sensor);
}
