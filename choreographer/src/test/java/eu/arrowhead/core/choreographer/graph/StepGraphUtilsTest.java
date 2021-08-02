package eu.arrowhead.core.choreographer.graph;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
	@Test
	public void testNormalizeStepGraph() {
		final List<Pair<StepGraph,StepGraph>> normalizables = StepGraphExamples.getNormalizables();
		
		for (int i = 0; i < normalizables.size(); ++i) {
			final Pair<StepGraph,StepGraph> graphPair = normalizables.get(i);
			final StepGraph result = graphUtils.normalizeStepGraph(graphPair.getLeft());
			System.out.println("---------------------"); //TODO remove this line
			Assert.assertEquals(graphPair.getRight(), result);
		}
	}

}
