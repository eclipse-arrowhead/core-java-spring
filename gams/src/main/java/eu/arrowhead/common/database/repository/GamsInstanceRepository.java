package eu.arrowhead.common.database.repository;

import java.util.Optional;
import java.util.UUID;

import eu.arrowhead.common.database.entity.GamsInstance;
import org.springframework.stereotype.Repository;

@Repository
public interface GamsInstanceRepository extends RefreshableRepository<GamsInstance, Long> {

    <S extends GamsInstance> Optional<S> findByUid(final UUID uid);

    <S extends GamsInstance> Optional<S> findByName(final String name);
}
