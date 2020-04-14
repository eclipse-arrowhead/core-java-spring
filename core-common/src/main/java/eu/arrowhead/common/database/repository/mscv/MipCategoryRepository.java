package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.entity.mscv.MipCategory;
import org.springframework.stereotype.Repository;

@Repository
public interface MipCategoryRepository extends RefreshableRepository<MipCategory, Long> {
}
