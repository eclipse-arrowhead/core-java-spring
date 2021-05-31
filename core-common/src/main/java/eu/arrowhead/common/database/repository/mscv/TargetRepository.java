package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.dto.shared.mscv.OS;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TargetRepository extends RefreshableRepository<Target, Long> {

    List<Target> findByName(final String name);

    <S extends Target> Optional<S> findByNameAndOs(final String name, final OS os);
}
