package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.entity.mscv.Mip;
import eu.arrowhead.common.database.view.mscv.MipView;
import org.springframework.stereotype.Repository;

@Repository
public interface MipRepository extends RefreshableRepository<Mip, Long> {

    MipView findViewById(final Long id);

}
