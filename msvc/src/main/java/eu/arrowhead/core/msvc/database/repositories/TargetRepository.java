package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.Target;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TargetRepository<I> extends RefreshableRepository<Target, Long> {

    Optional<I> findById(final Long id, final Class<I> clz);

    Optional<I> findByName(final String name, final Class<I> clz);
}
