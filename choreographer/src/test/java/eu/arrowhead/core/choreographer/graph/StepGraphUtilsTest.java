package eu.arrowhead.core.choreographer.graph;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.core.choreographer.ChoreographerMain;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChoreographerMain.class)
public class StepGraphUtilsTest {
	
	//=================================================================================================
	// members
	
	@Autowired
	private StepGraphUtils graphUtils;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testHasCircleNullDetector() {
		StepGraphCircleDetector detector = (StepGraphCircleDetector) ReflectionTestUtils.getField(graphUtils, "circleDetector");		
		ReflectionTestUtils.setField(graphUtils, "circleDetector", null);
		try {
			graphUtils.hasCircle(null);
		} catch (final Exception ex) {
			Assert.assertEquals("Circle detector is not implemented.", ex.getMessage());
			ReflectionTestUtils.setField(graphUtils, "circleDetector", detector);
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHasCircle1() {
		final List<StepGraph> nonCirculars = StepGraphExamples.getNonCirculars();
		
		for (int i = 0; i < nonCirculars.size(); ++i) {
			final boolean result = graphUtils.hasCircle(nonCirculars.get(i));
			Assert.assertFalse("Non-existent circle detected in graph[" + i + "]", result);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHasCircle2() {
		final List<StepGraph> circulars = StepGraphExamples.getCirculars();
		
		for (int i = 0; i < circulars.size(); ++i) { 
			final boolean result = graphUtils.hasCircle(circulars.get(i));
			Assert.assertTrue("Circle not detected in graph[" + i + "]", result);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testNormaiizeStepGraphNormalizerNull() {
		final StepGraphNormalizer normalizer = (StepGraphNormalizer) ReflectionTestUtils.getField(graphUtils, "normalizer");		
		ReflectionTestUtils.setField(graphUtils, "normalizer", null);
		try {
			graphUtils.normalizeStepGraph(null);
		} catch (final Exception ex) {
			Assert.assertEquals("Normalizer is not implemented.", ex.getMessage());
			ReflectionTestUtils.setField(graphUtils, "normalizer", normalizer);
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeStepGraph() {
		final List<Pair<StepGraph,StepGraph>> normalizables = StepGraphExamples.getNormalizables();
		
		for (int i = 0; i < normalizables.size(); ++i) {
			final Pair<StepGraph,StepGraph> graphPair = normalizables.get(i);
			final StepGraph result = graphUtils.normalizeStepGraph(graphPair.getLeft());
			Assert.assertEquals(graphPair.getRight(), result);
		}
	}
}