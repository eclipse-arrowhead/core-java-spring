package eu.arrowhead.core.gams.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.gams.database.entities.GamsInstance;
import eu.arrowhead.core.gams.database.entities.Sensor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SensorRepository extends RefreshableRepository<Sensor, Long> {

    <S extends Sensor> Optional<S> findByUid(final UUID uid);

    <S extends Sensor> Optional<S> findByInstanceAndName(final GamsInstance instance, final String name);
}
