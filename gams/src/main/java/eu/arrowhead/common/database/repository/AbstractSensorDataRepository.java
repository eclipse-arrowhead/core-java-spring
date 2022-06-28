package eu.arrowhead.common.database.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.core.gams.dto.ProcessingState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface AbstractSensorDataRepository<S extends AbstractSensorData> extends ProcessableEntityRepository<S> {

    Optional<S> findTopBySensorOrderByValidTillDesc(final Sensor sensor);

    Optional<S> findTopBySensorAndCreatedAtAfterOrderByValidTillDesc(final Sensor sensor, final ZonedDateTime from);

    Long countBySensorAndStateAndCreatedAtAfter(final Sensor sensor, final ProcessingState state, final ZonedDateTime from);

    List<S> findBySensorAndStateOrderByValidTillDesc(final Sensor sensor, final ProcessingState state);

    Page<S> findBySensorAndStateOrderByValidTillDesc(final Sensor sensor, final ProcessingState state, final Pageable page);

    List<S> findBySensorAndStateAndCreatedAtAfterOrderByValidTillDesc(final Sensor sensor, final ProcessingState state, final ZonedDateTime from);

    Page<S> findBySensorAndStateAndCreatedAtAfterOrderByValidTillDesc(final Sensor sensor, final ProcessingState state, final ZonedDateTime from,
                                                                      final Pageable page);
}
