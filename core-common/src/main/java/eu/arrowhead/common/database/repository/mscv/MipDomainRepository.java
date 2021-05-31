package eu.arrowhead.common.database.repository.mscv;

import java.util.Optional;

import eu.arrowhead.common.database.entity.mscv.MipDomain;
import eu.arrowhead.common.database.repository.RefreshableRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MipDomainRepository extends RefreshableRepository<MipDomain, Long> {
    Optional<MipDomain> findByName(String name);
}
