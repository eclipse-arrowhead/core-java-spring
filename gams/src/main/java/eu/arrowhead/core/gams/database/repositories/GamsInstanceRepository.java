package eu.arrowhead.core.gams.database.repositories;

import java.util.Optional;
import java.util.UUID;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.gams.database.entities.GamsInstance;

public interface GamsInstanceRepository extends RefreshableRepository<GamsInstance, Long> {

    <S extends GamsInstance> Optional<S> findByUid(final UUID uid);

    <S extends GamsInstance> Optional<S> findByName(final String name);
}
