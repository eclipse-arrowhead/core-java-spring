package eu.arrowhead.core.choreographer.executor;

import java.util.List;
import java.util.Map;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;

public interface ExecutorPrioritizationStrategy {

	List<ChoreographerExecutor> priorize(final Map<ChoreographerExecutor,ChoreographerExecutorServiceInfoResponseDTO> executorServiceInfos);
}
