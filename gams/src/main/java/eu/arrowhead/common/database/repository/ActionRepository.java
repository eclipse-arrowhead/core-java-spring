package eu.arrowhead.common.database.repository;

import java.util.Optional;

import eu.arrowhead.common.database.entity.AbstractAction;
import eu.arrowhead.common.database.entity.GamsInstance;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository<S extends AbstractAction> extends RefreshableRepository<S, Long> {
    Optional<S> findByInstanceAndName(final GamsInstance instance, final String name);
}
