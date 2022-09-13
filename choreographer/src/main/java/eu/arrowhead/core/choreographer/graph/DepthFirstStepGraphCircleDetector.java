package eu.arrowhead.core.choreographer.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;

public class DepthFirstStepGraphCircleDetector implements StepGraphCircleDetector {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean hasCircle(final StepGraph graph) {
		Assert.notNull(graph, "'graph' is null.");
		
		final Map<Node,NodeData> nodesData = new HashMap<>(graph.getSteps().size());
		for (final Node node : graph.getSteps()) {
			nodesData.put(node, new NodeData());
		}
		
		performDepthFirstTraverse(graph.getSteps(), nodesData);
		
		for (final Node node : graph.getSteps()) {
			if (nodesData.get(node).detectedCircle) {
				return true;
			}
		}
		
		return false;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void performDepthFirstTraverse(final Set<Node> stepNodes, final Map<Node,NodeData> nodesData) {
		for (final Node node : stepNodes) {
			final NodeData nodeData = nodesData.get(node);
			if (NodeColor.WHITE == nodeData.color) {
				depthFirstTraverseImpl(node, nodesData);
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void depthFirstTraverseImpl(final Node node, final Map<Node,NodeData> nodesData) {
		final NodeData nodeData = nodesData.get(node);
		nodeData.color = NodeColor.GREY;
		
		for (final Node neighbor : node.getNextNodes()) {
			final NodeData neighborData = nodesData.get(neighbor);

			if (NodeColor.WHITE == neighborData.color) {
				depthFirstTraverseImpl(neighbor, nodesData);
			} else if (NodeColor.GREY == neighborData.color) {
				neighborData.detectedCircle = true;
			}
		}
		
		nodeData.color = NodeColor.BLACK;
	}

	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	private static enum NodeColor { WHITE, GREY, BLACK }
	
	//-------------------------------------------------------------------------------------------------
	private static class NodeData {
		
		//=================================================================================================
		// members
		
		NodeColor color = NodeColor.WHITE;
		boolean detectedCircle = false;
		
		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		@Override
		public String toString() {
			return "NodeData [color=" + color + ", detectedCircle=" + detectedCircle + "]";
		}
	}
}