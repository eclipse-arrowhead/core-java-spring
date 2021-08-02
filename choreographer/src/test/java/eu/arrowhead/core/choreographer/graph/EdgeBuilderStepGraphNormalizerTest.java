package eu.arrowhead.core.choreographer.graph;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class EdgeBuilderStepGraphNormalizerTest {
	
	//=================================================================================================
	// members
	
	private StepGraphNormalizer normalizer = new EdgeBuilderStepGraphNormalizer();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeStepGraph() {
		final List<Pair<StepGraph,StepGraph>> normalizables = StepGraphExamples.getNormalizables();
		
		for (int i = 0; i < normalizables.size(); ++i) {
			final Pair<StepGraph,StepGraph> graphPair = normalizables.get(i);
			final StepGraph result = normalizer.normalizeStepGraph(graphPair.getLeft());
			Assert.assertEquals(graphPair.getRight(), result);
		}
	}
}