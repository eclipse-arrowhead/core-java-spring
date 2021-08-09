package eu.arrowhead.core.choreographer.executor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;


public class RandomExecutorPrioritizationStrategy implements ExecutorPrioritizationStrategy {

	//=================================================================================================
	// members
	
	private Random rng;
	
	private static final Logger logger = LogManager.getLogger(ExecutorPrioritizationStrategy.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<ChoreographerExecutor> priorize(final Map<ChoreographerExecutor,ChoreographerExecutorServiceInfoResponseDTO> executorServiceInfos) {
		logger.debug("priorize started");
		
		if (executorServiceInfos.isEmpty()) {
			return new ArrayList<>();
		}
		
		if (rng == null) {
			rng = new Random(System.currentTimeMillis());
		}
		
		final List<ChoreographerExecutor> potentials = new ArrayList<>(executorServiceInfos.keySet());
		Collections.shuffle(potentials, rng);
		return potentials;
	}

}
