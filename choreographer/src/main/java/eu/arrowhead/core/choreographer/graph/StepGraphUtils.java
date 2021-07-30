package eu.arrowhead.core.choreographer.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class StepGraphUtils {
	
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
	
	//-------------------------------------------------------------------------------------------------
	// TODO: remove sysout lines
	public StepGraph normalizeStepGraph(final StepGraph graph) {
		Assert.notNull(graph, "'graph' is null.");
		
		if (graph.getSteps().isEmpty()) {
			return graph;
		}
		
		final StepGraph resultGraph = shallowCopyStepGraph(graph);

		for (final Node node : graph.getSteps()) {
			List<List<String>> paths = generatePaths(node);
			System.out.print("Calculated paths for " + node.getName() + ": ");
			System.out.println(paths);
			if (paths.size() > 1) {
				sortPathsByLengthDescending(paths);
				paths = removeRedundantPaths(paths);
			}
			
			System.out.print("Normalized paths for " + node.getName() + ": ");
			System.out.println(paths);
			addEdgesFromPaths(resultGraph, paths);
		}
		
		return resultGraph;
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
	
	//-------------------------------------------------------------------------------------------------
	private StepGraph shallowCopyStepGraph(final StepGraph graph) {
		final StepGraph result = new StepGraph();
		for (final Node node : graph.getSteps()) {
			result.getSteps().add(new Node(node.getName()));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<List<String>> generatePaths(final Node node) {
		if (node.getPrevNodes().isEmpty()) {
			return List.of(List.of(node.getName()));
		}
		
		final List<List<String>> result = new ArrayList<>();
		for (final Node prevNode : node.getPrevNodes()) {
			final List<String> path = new ArrayList<>();
			path.add(node.getName());
			generateAPath(prevNode, path, result);
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private void generateAPath(final Node node, final List<String> pathPrefix, final List<List<String>> paths) {
		pathPrefix.add(node.getName());
		
		if (node.getPrevNodes().isEmpty()) {
			paths.add(pathPrefix);
		}
		
		for (final Node prevNode : node.getPrevNodes()) {
			final List<String> pathPrefixCopy = copyPath(pathPrefix);
			generateAPath(prevNode, pathPrefixCopy, paths);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<String> copyPath(final List<String> path) {
		final List<String> result = new ArrayList<>(path.size());
		for (final String nodeName : path) {
			result.add(nodeName);
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private void sortPathsByLengthDescending(final List<List<String>> paths) {
		Collections.sort(paths, new Comparator<List<String>>() {

			//-------------------------------------------------------------------------------------------------
			@Override
			public int compare(final List<String> list1, final List<String> list2) {
				// we need descending order
				return Integer.compare(list2.size(), list1.size());
			}
			
		});
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<List<String>> removeRedundantPaths(final List<List<String>> paths) {
		final List<List<String>> result = new ArrayList<>();
		result.add(paths.get(0)); // we always keep the first one (longest one)
		
		for (int i = 1; i < paths.size(); ++i) {
			final List<String> candidatePath = paths.get(i);
			if (!isRedundantPath(candidatePath, result)) {
				result.add(candidatePath);
			}
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean isRedundantPath(final List<String> candidatePath, final List<List<String>> verifiedPaths) {
		for (final List<String> path : verifiedPaths) {
			if (isRedundantPathImpl(candidatePath, path)) {
				return true;
			}
		}
		return false;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean isRedundantPathImpl(final List<String> candidatePath, final List<String> verifiedPath) {
		int lastIndex = -1;
		for (int i = 0; i < candidatePath.size(); ++i) {
			final String node = candidatePath.get(i);
			int index = verifiedPath.indexOf(node);
			if (index <= lastIndex) {
				return false;
			}
			lastIndex = index;
		}
		
		return true;
	}

	//-------------------------------------------------------------------------------------------------
	private void addEdgesFromPaths(final StepGraph graph, final List<List<String>> paths) {
		for (final List<String> path : paths) {
			if (path.size() > 1) {
				final Node toNode = findNode(graph, path.get(0));
				final Node fromNode = findNode(graph, path.get(1));
				addEdge(fromNode, toNode);
				// all other edges were or will be added when this method was or will be called with a prefix path
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private Node findNode(final StepGraph graph, final String name) {
		for (final Node node : graph.getSteps()) {
			if (name.equals(node.getName())) {
				return node;
			}
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void addEdge(final Node fromNode, final Node toNode) {
		if (fromNode != null && toNode != null) {
			fromNode.getNextNodes().add(toNode);
			toNode.getPrevNodes().add(fromNode);
		}
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