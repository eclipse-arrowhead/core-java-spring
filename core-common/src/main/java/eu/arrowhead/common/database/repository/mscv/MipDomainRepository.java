package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.entity.mscv.MipDomain;
import org.springframework.stereotype.Repository;

@Repository
public interface MipDomainRepository extends RefreshableRepository<MipDomain, Long> {
}
