package eu.arrowhead.core.gams.database.repositories;

import eu.arrowhead.core.gams.database.entities.AbstractSensorData;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorDataRepository extends AbstractSensorDataRepository<AbstractSensorData> {
}
