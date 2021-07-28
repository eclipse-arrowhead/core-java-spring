package eu.arrowhead.core.choreographer.graph;

import java.util.HashSet;
import java.util.Set;

public class StepGraph {
	
	//=================================================================================================
	// members
	
	private final Set<Node> firstSteps = new HashSet<>();
	private final Set<Node> steps = new HashSet<>();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Set<Node> getFirstSteps() { 	return firstSteps; }
	public Set<Node> getSteps() { return steps; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "StepGraph [firstSteps=" + firstSteps + ", steps=" + steps + "]";
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((firstSteps == null) ? 0 : firstSteps.hashCode());
		result = prime * result + ((steps == null) ? 0 : steps.hashCode());
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		final StepGraph other = (StepGraph) obj;
		if (!this.steps.equals(other.steps) || !this.firstSteps.equals(other.firstSteps)) {
			return false;
		}
		
		for (final Node node : this.steps) {
			final Node otherNode = findNode(node.getName(), other.steps);
			if (!node.getNextNodes().equals(otherNode.getNextNodes()) || !node.getPrevNodes().equals(otherNode.getPrevNodes())) {
				return false;
			}
			
		}
		
		return true;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Node findNode(final String name, final Set<Node> nodes) {
		for (final Node node : nodes) {
			if (name.equals(node.getName())) {
				return node;
			}
			
		}
		
		return null;
	}
}