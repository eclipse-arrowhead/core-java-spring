package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerActionStep;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChoreographerActionRepository extends RefreshableRepository<ChoreographerAction, Long> {

    public Optional<ChoreographerAction> findByActionName(final String actionName);
    public Optional<ChoreographerAction> findByActionNameAndNextAction(final String actionName, final ChoreographerAction nextAction);

}
