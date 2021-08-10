package eu.arrowhead.core.choreographer.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanRequestDTO;

@Service
public class ActionCircleDetector {
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(ActionCircleDetector.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/* default */ boolean hasCircle(final ChoreographerPlanRequestDTO plan) {
		logger.debug("hasCircle started...");
		
		final Set<String> checkedActions = new HashSet<>();
		ChoreographerActionRequestDTO action = findAction(plan.getActions(), plan.getFirstActionName());
		checkedActions.add(action.getName());
		
		while (!Utilities.isEmpty(action.getNextActionName())) {
			if (checkedActions.contains(action.getNextActionName())) {
				return true;
			}
			checkedActions.add(action.getNextActionName());
			action = findAction(plan.getActions(), action.getNextActionName());
		}
		
		return false;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerActionRequestDTO findAction(final List<ChoreographerActionRequestDTO> actions, final String name) {
		for (final ChoreographerActionRequestDTO action : actions) {
			if (name.equals(action.getName())) {
				return action;
			}
		}
		
		return null;
	}
}
