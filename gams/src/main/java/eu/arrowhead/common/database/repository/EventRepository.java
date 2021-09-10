package eu.arrowhead.common.database.repository;

import java.util.Optional;

import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.core.gams.dto.EventType;
import eu.arrowhead.core.gams.dto.GamsPhase;
import eu.arrowhead.core.gams.dto.ProcessingState;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EventRepository extends RefreshableRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Optional<Event> findValidEvent(final ProcessingState previousState, final ProcessingState newState);

    Iterable<Event> findValidEvent(final ProcessingState previousState,
                                   final ProcessingState newState, int limit);

    Iterable<Event> findAllValidEvents(final ProcessingState state);

    void expireEvents();

    long countValid();

    boolean hasValidEvent(final Sensor sensor, final ProcessingState persisted, final GamsPhase monitor,
                          final EventType sensorData);
}
