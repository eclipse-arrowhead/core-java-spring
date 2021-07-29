package eu.arrowhead.core.choreographer.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class GraphUtils {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean hasCircle(final StepGraph graph) {
		Assert.notNull(graph, "'graph' is null.");
		
		//TODO: implement
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	public StepGraph normalizeStepGraph(final StepGraph graph) {
		Assert.notNull(graph, "'graph' is null.");

		if (graph.getSteps().isEmpty()) {
			return graph;
		}
		
		final List<Pair<Node,Node>> removables = new ArrayList<>();
		for (final Node node : graph.getSteps()) {
			if (!node.getPrevNodes().isEmpty()) {
				
				List<List<Node>> routes = new ArrayList<>();
				final List<Node> initial = new ArrayList<>();
				initial.add(node);
				routes.add(initial);
				routes = discoverRoutes(routes);
				removables.addAll(findRemovableRoutes(routes));
			}
		}
		
		return cleanGraph(graph, removables);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private List<List<Node>> discoverRoutes(final List<List<Node>> routes) {
		final List<List<Node>> modifiedRouteList = new ArrayList<>();
		boolean allRouteIsComplete = true;
		
		for (final List<Node> route : routes) {
			final List<Node> updatedRoute = discoverToJunctionOrEnd(route);
			final Node lastNode = updatedRoute.get(route.size() - 1);
			
			if (lastNode.getPrevNodes().isEmpty()) {
				//End of the route
				modifiedRouteList.add(updatedRoute);
			} else {
				//Junction in the route
				allRouteIsComplete = false;
				for (final Node prevNode : lastNode.getPrevNodes()) {
					final List<Node> newRoute = new ArrayList<>();
					newRoute.addAll(updatedRoute);
					newRoute.add(prevNode);
					modifiedRouteList.add(newRoute);
				}				
			}			
		}
		
		if (allRouteIsComplete) {
			return modifiedRouteList;
		} else {
			return discoverRoutes(modifiedRouteList);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Node> discoverToJunctionOrEnd(final List<Node> route) {
		final Node lastNode = route.get(route.size() - 1);
		if (lastNode.getPrevNodes().size() != 1) {
			return route;
		}
		route.add(lastNode.getPrevNodes().iterator().next());
		return discoverToJunctionOrEnd(route);
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Pair<Node,Node>> findRemovableRoutes(final List<List<Node>> routes) {
		// Keeps the longest route to a node and the shorter ones only if it is via a different node (which not part of the longest route)
		if (routes.isEmpty()) {
			return new ArrayList<>();
		}
		routes.sort((final List<Node> l1, final List<Node> l2) -> (l2.size() - l1.size()));
		final Set<Node> longest = new HashSet<>(routes.remove(0));
		
		final List<Pair<Node,Node>> removables = new ArrayList<>();		
		for (final List<Node> route : routes) {
			boolean removable = true;
			final List<Pair<Node,Node>> routeRemovables = new ArrayList<>();
			for (int i = 0; i < route.size(); i++) {
				if (!longest.contains(route.get(i))) {
					removable = false;
					break;
				}
				
				if (i + 1 < routes.size() - 1 ) {
					routeRemovables.add(Pair.of(route.get(i + 1), route.get(i))); //Because route is backward, so the result will Pair(from,to)
				}
			}
			
			if (removable) {
				removables.addAll(routeRemovables);
			}
		}
		
		return removables;
	}
	
	//-------------------------------------------------------------------------------------------------
	private StepGraph cleanGraph(final StepGraph graph, final List<Pair<Node,Node>> removables) {
		if (removables.isEmpty()) {
			return graph;
		}
		
		for (final Pair<Node, Node> link : removables) {
			for (final Node step : graph.getSteps()) {
				final Node from = link.getLeft();
				final Node to = link.getRight();
				if (step.equals(from)) {
					from.getNextNodes().remove(to);
					to.getPrevNodes().remove(from);
					break;
				}
			}
		}
		return graph;
	}
}
