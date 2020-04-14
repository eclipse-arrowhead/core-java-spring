package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.view.mscv.TargetView;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TargetRepository extends RefreshableRepository<Target, Long> {

    <S extends Target> Optional<S> findByName(final String name);
    TargetView findOneByName(final String name);
    TargetView findOneByNameAndOs(final String name, final OS os);
}
