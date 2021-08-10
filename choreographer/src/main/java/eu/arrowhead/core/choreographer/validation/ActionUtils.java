package eu.arrowhead.core.choreographer.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerStepRequestDTO;
import eu.arrowhead.core.choreographer.graph.Node;
import eu.arrowhead.core.choreographer.graph.StepGraph;

@Component
public class ActionUtils {

	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(ActionUtils.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	/* default */ StepGraph createStepGraphFromAction(final ChoreographerActionRequestDTO action) {
		logger.debug("convertActionToStepGraph started...");
		
		if (action == null) {
			return null;
		}
		
		final StepGraph result = new StepGraph();
		final Map<String,Node> name2Node = new HashMap<>(action.getSteps().size());
		
		for (final ChoreographerStepRequestDTO step : action.getSteps()) {
			final Node node = new Node(step.getName());
			name2Node.put(step.getName(), node);
			result.getSteps().add(node);
		}
		
		for (final ChoreographerStepRequestDTO step : action.getSteps()) {
			if (step.getNextStepNames() != null) {
				final Node fromNode = name2Node.get(step.getName());
				for (final String name : step.getNextStepNames()) {
					final Node toNode = name2Node.get(name);
					addEdge(fromNode, toNode);
				}
			}
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	/* default */ ChoreographerActionRequestDTO transformActionWithGraph(final StepGraph graph, final ChoreographerActionRequestDTO action) {
		logger.debug("transformActionWithGraph started...");
		
		if (action == null) {
			return null;
		}
		
		Assert.notNull(graph, "Graph is null.");
		Assert.notNull(action.getSteps(), "Action has no steps.");
		Assert.isTrue(graph.getSteps().size() == action.getSteps().size(), "Graph is incompatible with action.");
		
		resetAction(action);
		
		final List<String> firstStepNames = new ArrayList<>();
		
		for (final Node node : graph.getSteps()) {
			final ChoreographerStepRequestDTO step = findStep(action.getSteps(), node.getName());
			
			if (step == null) {
				throw new IllegalArgumentException("Graph is incompatible with action.");
			}
			
			if (node.getPrevNodes().isEmpty()) { // first step
				firstStepNames.add(node.getName());
			}
			
			for (final Node nextNode : node.getNextNodes()) {
				step.getNextStepNames().add(nextNode.getName());
			}
		}
		
		action.setFirstStepNames(firstStepNames);
		
		return action;
	}

	//=================================================================================================
	// assistant method
	
	//-------------------------------------------------------------------------------------------------
	private void addEdge(final Node fromNode, final Node toNode) {
		logger.debug("addEdge started...");
		
		if (fromNode != null && toNode != null) {
			fromNode.getNextNodes().add(toNode);
			toNode.getPrevNodes().add(fromNode);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void resetAction(final ChoreographerActionRequestDTO action) {
		logger.debug("resetAction started...");
		
		action.setFirstStepNames(null);
		for (final ChoreographerStepRequestDTO step : action.getSteps()) {
			if (step.getNextStepNames() != null) {
				step.setNextStepNames(new ArrayList<>());
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerStepRequestDTO findStep(final List<ChoreographerStepRequestDTO> steps, final String name) {
		logger.debug("findStep started...");
		
		for (final ChoreographerStepRequestDTO step : steps) {
			if (name.equals(step.getName())) {
				return step;
			}
		}
		
		return null;
	}
}