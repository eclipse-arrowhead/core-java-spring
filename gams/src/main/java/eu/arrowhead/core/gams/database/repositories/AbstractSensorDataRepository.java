package eu.arrowhead.core.gams.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.gams.database.entities.AbstractSensorData;
import eu.arrowhead.core.gams.database.entities.Sensor;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.ZonedDateTime;
import java.util.Optional;

@NoRepositoryBean
public interface AbstractSensorDataRepository<S extends AbstractSensorData> extends RefreshableRepository<AbstractSensorData, Long> {

    Optional<S> findBySensorOrderByTimestampDesc(final Sensor sensor);

    Optional<S> findBySensorAndTimestampAfterOrderByTimestampDesc(final Sensor sensor, final ZonedDateTime from);
}
