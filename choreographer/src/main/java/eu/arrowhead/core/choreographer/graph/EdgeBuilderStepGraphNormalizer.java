package eu.arrowhead.core.choreographer.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;
import eu.arrowhead.core.choreographer.graph.Node;

public class EdgeBuilderStepGraphNormalizer implements StepGraphNormalizer {
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(EdgeBuilderStepGraphNormalizer.class);


	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public StepGraph normalizeStepGraph(final StepGraph graph) {
		Assert.notNull(graph, "'graph' is null.");
		
		if (graph.getSteps().isEmpty()) {
			return graph;
		}
		
		final StepGraph resultGraph = shallowCopyStepGraph(graph);

		for (final Node node : graph.getSteps()) {
			List<List<Node>> routes = generateRoutes(node);
			logger.debug("Calculated routes for {}: {}", node.getName(), routes);
			if (routes.size() > 1) {
				sortRoutesByLengthDescending(routes);
				routes = removeRedundantRoutes(routes);
			}
			
			logger.debug("Normalized routes for {}: {}", node.getName(), routes);
			addEdgesFromRoutes(resultGraph, routes);
		}
		
		return resultGraph;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private StepGraph shallowCopyStepGraph(final StepGraph graph) {
		final StepGraph result = new StepGraph();
		for (final Node node : graph.getSteps()) {
			result.getSteps().add(new Node(node.getName(), node.getStartCondition()));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<List<Node>> generateRoutes(final Node node) {
		if (node.getPrevNodes().isEmpty()) {
			return List.of(List.of(new Node(node.getName(), node.getStartCondition())));
		}
		
		final List<List<Node>> result = new ArrayList<>();
		for (final Node prevNode : node.getPrevNodes()) {
			final List<Node> route = new ArrayList<>();
			route.add(new Node(node.getName(), node.getStartCondition()));
			generateARoute(prevNode, route, result);
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private void generateARoute(final Node node, final List<Node> routePrefix, final List<List<Node>> routes) {
		routePrefix.add(new Node(node.getName(), node.getStartCondition()));
		
		if (node.getPrevNodes().isEmpty()) {
			routes.add(routePrefix);
		}
		
		for (final Node prevNode : node.getPrevNodes()) {
			final List<Node> routePrefixCopy = copyRoute(routePrefix);
			generateARoute(prevNode, routePrefixCopy, routes);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Node> copyRoute(final List<Node> route) {
		final List<Node> result = new ArrayList<>(route.size());
		for (final Node node : route) {
			result.add(node);
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private void sortRoutesByLengthDescending(final List<List<Node>> routes) {
		Collections.sort(routes, new Comparator<List<Node>>() {

			//-------------------------------------------------------------------------------------------------
			@Override
			public int compare(final List<Node> route1, final List<Node> route2) {
				// we need descending order
				return Integer.compare(route2.size(), route1.size());
			}
			
		});
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<List<Node>> removeRedundantRoutes(final List<List<Node>> routes) {
		final List<List<Node>> result = new ArrayList<>();
		result.add(routes.get(0)); // we always keep the first one (longest one)
		
		for (int i = 1; i < routes.size(); ++i) {
			final List<Node> candidateRoute = routes.get(i);
			if (!isRedundantRoute(candidateRoute, result)) {
				result.add(candidateRoute);
			}
			else {/*add edges that ends in node where startCondition is NOT AND */
		for (int j = 0; j < candidateRoute.size(); ++j) {
			final Node node = candidateRoute.get(j);
			if(node.getIsStartConditionAND() && j < candidateRoute.size() - 1) {
				List<Node> mustKeepEdge = new ArrayList<>();
				mustKeepEdge.add(node);
				mustKeepEdge.add(candidateRoute.get(j+1));
				result.add(mustKeepEdge);
			}
		}
			}
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean isRedundantRoute(final List<Node> candidateRoute, final List<List<Node>> verifiedRoutes) {
		for (final List<Node> route : verifiedRoutes) {
			if (isRedundantRouteImpl(candidateRoute, route)) {
				return true;
			}
		}
		return false;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean isRedundantRouteImpl(final List<Node> candidateRoute, final List<Node> verifiedRoute) {
		int lastIndex = -1;
		
		for (int i = 0; i < candidateRoute.size(); ++i) {
			final Node node = candidateRoute.get(i);
			int index = verifiedRoute.indexOf(node);
			if (index <= lastIndex) {
				return false;
			}
			lastIndex = index;
		}
		
		return true;
	}

	//-------------------------------------------------------------------------------------------------
	private void addEdgesFromRoutes(final StepGraph graph, final List<List<Node>> routes) {
		for (final List<Node> route : routes) {
			if (route.size() > 1) {
				final Node toNode = findNode(graph, route.get(0));
				final Node fromNode = findNode(graph, route.get(1));
				addEdge(fromNode, toNode);
				// all other edges were or will be added when this method was or will be called with a prefix route
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private Node findNode(final StepGraph graph, final Node pnode) {
		for (final Node node : graph.getSteps()) {
			if (pnode.getName().equals(node.getName())) {
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
}