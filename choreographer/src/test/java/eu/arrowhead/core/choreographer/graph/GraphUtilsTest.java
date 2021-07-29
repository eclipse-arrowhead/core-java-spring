package eu.arrowhead.core.choreographer.graph;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
public class GraphUtilsTest {
	
	//=================================================================================================
	// members
	
	private final GraphUtils graphUtils = new GraphUtils();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCirculars() throws Exception {
		final List<StepGraph> circulars = GraphExamples.getCirculars();
		//TODO implement

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalize() throws Exception {
		final List<Pair<StepGraph, StepGraph>> normalizables = GraphExamples.getNormalizables();
		for (int i = 0; i < normalizables.size(); i++) {
			final StepGraph normalizedGraph = graphUtils.normalizeStepGraph(normalizables.get(i).getLeft());
			Assert.assertTrue("Normalized garph don't match in example[" + i + "]", normalizedGraph.equals(normalizables.get(i).getRight()));
		}
	} 
}
