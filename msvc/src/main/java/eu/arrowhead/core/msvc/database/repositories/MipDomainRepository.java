package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.MipDomain;
import org.springframework.stereotype.Repository;

@Repository
public interface MipDomainRepository extends RefreshableRepository<MipDomain, Long> {
}
