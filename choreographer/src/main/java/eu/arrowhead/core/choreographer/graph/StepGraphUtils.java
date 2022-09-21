package eu.arrowhead.core.choreographer.graph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class StepGraphUtils {
	
	//=================================================================================================
	// members
	
	@Autowired
	private StepGraphCircleDetector circleDetector;
	
	@Autowired
	private StepGraphNormalizer normalizer;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean hasCircle(final StepGraph graph) {
		Assert.notNull(circleDetector, "Circle detector is not implemented.");
		
		return circleDetector.hasCircle(graph);
	}
	
	//-------------------------------------------------------------------------------------------------
	public StepGraph normalizeStepGraph(final StepGraph graph) {
		Assert.notNull(normalizer, "Normalizer is not implemented.");
		
		return normalizer.normalizeStepGraph(graph);
	}
}