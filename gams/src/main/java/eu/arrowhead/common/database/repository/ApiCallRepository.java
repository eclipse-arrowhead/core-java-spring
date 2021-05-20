package eu.arrowhead.common.database.repository;

import java.util.Optional;

import eu.arrowhead.common.database.entity.ProcessableAction;
import eu.arrowhead.common.database.entity.GamsInstance;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiCallRepository<S extends ProcessableAction> extends RefreshableRepository<S, Long> {
    Optional<S> findByInstanceAndName(final GamsInstance instance, final String name);
}
