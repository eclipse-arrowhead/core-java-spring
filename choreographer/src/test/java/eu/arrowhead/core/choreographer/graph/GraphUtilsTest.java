package eu.arrowhead.core.choreographer.graph;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class GraphUtilsTest {

	@Test
	public void testCirculars() throws Exception {
		final List<StepGraph> circulars = GraphExamples.getCirculars();
		//TODO implement

	}

}
