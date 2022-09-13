package eu.arrowhead.core.choreographer.graph;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
public class KahnAlgorithmStepGraphCircleDetectorTest {
	
	//=================================================================================================
	// members
	
	private final StepGraphCircleDetector circleDetector= new KahnAlgorithmStepGraphCircleDetector();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHasCircle1() throws Exception {
		final List<StepGraph> nonCirculars = StepGraphExamples.getNonCirculars();
		
		for (int i = 0; i < nonCirculars.size(); ++i) {
			final boolean result = circleDetector.hasCircle(nonCirculars.get(i));
			Assert.assertFalse("Non-existent circle detected in graph[" + i + "]", result);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHasCircle2() throws Exception {
		final List<StepGraph> circulars = StepGraphExamples.getCirculars();
		
		for (int i = 0; i < circulars.size(); ++i) { 
			final boolean result = circleDetector.hasCircle(circulars.get(i));
			Assert.assertTrue("Circle not detected in graph[" + i + "]", result);
		}
	}
}
