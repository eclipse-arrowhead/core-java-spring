package eu.arrowhead.core.choreographer.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

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
			List<List<String>> routes = generateRoutes(node);
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
			result.getSteps().add(new Node(node.getName()));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<List<String>> generateRoutes(final Node node) {
		if (node.getPrevNodes().isEmpty()) {
			return List.of(List.of(node.getName()));
		}
		
		final List<List<String>> result = new ArrayList<>();
		for (final Node prevNode : node.getPrevNodes()) {
			final List<String> route = new ArrayList<>();
			route.add(node.getName());
			generateARoute(prevNode, route, result);
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private void generateARoute(final Node node, final List<String> routePrefix, final List<List<String>> routes) {
		routePrefix.add(node.getName());
		
		if (node.getPrevNodes().isEmpty()) {
			routes.add(routePrefix);
		}
		
		for (final Node prevNode : node.getPrevNodes()) {
			final List<String> routePrefixCopy = copyRoute(routePrefix);
			generateARoute(prevNode, routePrefixCopy, routes);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<String> copyRoute(final List<String> route) {
		final List<String> result = new ArrayList<>(route.size());
		for (final String nodeName : route) {
			result.add(nodeName);
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private void sortRoutesByLengthDescending(final List<List<String>> routes) {
		Collections.sort(routes, new Comparator<List<String>>() {

			//-------------------------------------------------------------------------------------------------
			@Override
			public int compare(final List<String> route1, final List<String> route2) {
				// we need descending order
				return Integer.compare(route2.size(), route1.size());
			}
			
		});
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<List<String>> removeRedundantRoutes(final List<List<String>> routes) {
		final List<List<String>> result = new ArrayList<>();
		result.add(routes.get(0)); // we always keep the first one (longest one)
		
		for (int i = 1; i < routes.size(); ++i) {
			final List<String> candidateRoute = routes.get(i);
			if (!isRedundantRoute(candidateRoute, result)) {
				result.add(candidateRoute);
			}
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean isRedundantRoute(final List<String> candidateRoute, final List<List<String>> verifiedRoutes) {
		for (final List<String> route : verifiedRoutes) {
			if (isRedundantRouteImpl(candidateRoute, route)) {
				return true;
			}
		}
		return false;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean isRedundantRouteImpl(final List<String> candidateRoute, final List<String> verifiedRoute) {
		int lastIndex = -1;
		for (int i = 0; i < candidateRoute.size(); ++i) {
			final String node = candidateRoute.get(i);
			int index = verifiedRoute.indexOf(node);
			if (index <= lastIndex) {
				return false;
			}
			lastIndex = index;
		}
		
		return true;
	}

	//-------------------------------------------------------------------------------------------------
	private void addEdgesFromRoutes(final StepGraph graph, final List<List<String>> routes) {
		for (final List<String> route : routes) {
			if (route.size() > 1) {
				final Node toNode = findNode(graph, route.get(0));
				final Node fromNode = findNode(graph, route.get(1));
				addEdge(fromNode, toNode);
				// all other edges were or will be added when this method was or will be called with a prefix route
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
}