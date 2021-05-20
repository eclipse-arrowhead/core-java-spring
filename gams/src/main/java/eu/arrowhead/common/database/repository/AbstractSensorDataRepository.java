package eu.arrowhead.common.database.repository;

import java.time.ZonedDateTime;
import java.util.Optional;

import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.core.gams.dto.ProcessingState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface AbstractSensorDataRepository<S extends AbstractSensorData> extends ProcessableEntityRepository<S> {

    Optional<S> findTopBySensorOrderByValidTillDesc(final Sensor sensor);

    Optional<S> findTopBySensorAndCreatedAtAfterOrderByValidTillDesc(final Sensor sensor, final ZonedDateTime from);

    Long countBySensorAndStateAndCreatedAtAfter(final Sensor sensor, final ProcessingState state, final ZonedDateTime from);

    Page<S> findBySensorAndStateOrderByValidTillDesc(final Sensor sensor, final ProcessingState state, final Pageable page);

    Page<S> findBySensorAndCreatedAtAfterOrderByValidTillDesc(final Sensor sensor, final ZonedDateTime from, final Pageable page);
}
