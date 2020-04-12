package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.OS;
import eu.arrowhead.core.msvc.database.entities.Target;
import eu.arrowhead.core.msvc.database.view.TargetView;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TargetRepository extends RefreshableRepository<Target, Long> {

    <S extends Target> Optional<S> findByName(final String name);
    TargetView findOneByName(final String name);
    TargetView findOneByNameAndOs(final String name, final OS os);
}
