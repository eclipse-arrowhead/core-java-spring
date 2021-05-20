package eu.arrowhead.common.database.repository;

import java.util.Optional;

import eu.arrowhead.common.database.entity.Event;
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

    Long countValid();
}
