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
	public void testHasCircle1() throws Exception {
		final List<StepGraph> nonCirculars = GraphExamples.getNonCirculars();
		
		for (int i = 0; i < nonCirculars.size(); ++i) {
			final boolean result = graphUtils.hasCircle(nonCirculars.get(i));
			Assert.assertFalse("Non-existent circle detected in graph[" + i + "]", result);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHasCircle2() throws Exception {
		final List<StepGraph> circulars = GraphExamples.getCirculars();
		
		for (int i = 0; i < circulars.size(); ++i) { 
			final boolean result = graphUtils.hasCircle(circulars.get(i));
			Assert.assertTrue("Circle not detected in graph[" + i + "]", result);
		}
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
