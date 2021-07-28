package eu.arrowhead.core.choreographer.graph;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class GraphUtils {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean hasCircle(final StepGraph graph) {
		Assert.notNull(graph, "'graph' is null.");
		
		//TODO: implement
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	public StepGraph normalizeStepGraph(final StepGraph graph) {
		Assert.notNull(graph, "'graph' is null.");

		// TODO: implement
		
		return graph;
	}
}
