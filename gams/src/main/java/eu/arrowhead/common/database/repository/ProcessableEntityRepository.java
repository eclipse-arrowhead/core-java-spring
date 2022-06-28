package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ProcessableEntity;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ProcessableEntityRepository<T extends ProcessableEntity> extends RefreshableRepository<T, Long> {

}
