package eu.arrowhead.core.choreographer.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.springframework.util.Assert;

public class KahnAlgorithmStepGraphCircleDetector implements StepGraphCircleDetector {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean hasCircle(final StepGraph graph) { 
		Assert.notNull(graph, "'graph' is null.");
		
		//Edge case: First step(s) with incoming edge
		for (final Node node : graph.getFirstSteps()) {
			if (!node.getPrevNodes().isEmpty()) {
				return true;
			}
		}
		
		//Based on Khan's (Topology) Algorithm https://en.wikipedia.org/wiki/Topological_sorting#Kahn's_algorithm
		final StepGraph deepCopy = graph.deepCopy();		
		final Set<Node> workingGraph = deepCopy.getSteps();
		final Queue<Node> independentNodes = new LinkedList<>(deepCopy.getFirstSteps()); //Nodes without incoming edges in the actual workingGraph
		
		while (!independentNodes.isEmpty()) {
			final Node node = independentNodes.poll();
			if (node.getNextNodes().contains(node)) {
				return true;
			}
			
			final Set<Node> nextNodes = new HashSet<>(node.getNextNodes());
			for (final Node nextNode : nextNodes) {
				removeEdge(node, nextNode);
				if (nextNode.getPrevNodes().isEmpty()) {
					independentNodes.add(nextNode);
				}
			}
		}
		
		return hasEdges(workingGraph);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void removeEdge(final Node from, final Node to) {
		from.getNextNodes().remove(to);
		to.getPrevNodes().remove(from);		
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean hasEdges(final Set<Node> graph) {
		for (final Node node : graph) {
			if (!node.getPrevNodes().isEmpty() || !node.getNextNodes().isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
