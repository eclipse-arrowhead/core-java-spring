package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Sensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends RefreshableRepository<Sensor, Long> {

    <S extends Sensor> Optional<S> findByUid(final UUID uid);

    <S extends Sensor> List<S> findAllByInstance(final GamsInstance instance);

    <S extends Sensor> Page<S> findAllByInstance(final GamsInstance instance, final Pageable pageable);

    <S extends Sensor> Optional<S> findByInstanceAndName(final GamsInstance instance, final String name);

    <S extends Sensor> Optional<S> findByInstanceAndAddress(final GamsInstance instance, final String address);
}
