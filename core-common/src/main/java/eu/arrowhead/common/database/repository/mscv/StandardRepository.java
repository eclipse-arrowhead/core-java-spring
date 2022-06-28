package eu.arrowhead.common.database.repository.mscv;

import java.util.Optional;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.entity.mscv.Standard;
import org.springframework.stereotype.Repository;

@Repository
public interface StandardRepository extends RefreshableRepository<Standard, Long> {
    Optional<Standard> findByIdentification(final String identification);
}
