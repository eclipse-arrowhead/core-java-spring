package eu.arrowhead.core.choreographer.graph;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DepthFirstStepGraphCircleDetectorTest {
	
	//=================================================================================================
	// members
	
	private StepGraphCircleDetector detector = new DepthFirstStepGraphCircleDetector();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHasCircle1() {
		final List<StepGraph> nonCirculars = StepGraphExamples.getNonCirculars();
		
		for (int i = 0; i < nonCirculars.size(); ++i) {
			final boolean result = detector.hasCircle(nonCirculars.get(i));
			Assert.assertFalse("Non-existent circle detected in graph[" + i + "]", result);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHasCircle2() {
		final List<StepGraph> circulars = StepGraphExamples.getCirculars();
		
		for (int i = 0; i < circulars.size(); ++i) { 
			final boolean result = detector.hasCircle(circulars.get(i));
			Assert.assertTrue("Circle not detected in graph[" + i + "]", result);
		}
	}
	
}