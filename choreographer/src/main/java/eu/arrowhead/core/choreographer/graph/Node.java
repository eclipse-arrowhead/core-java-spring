package eu.arrowhead.core.choreographer.graph;

import java.util.HashSet;
import java.util.Set;

public class Node {
	
	//=================================================================================================
	// members
	
	protected final String name;
	protected final Set<Node> nextNodes = new HashSet<>();
	protected final Set<Node> prevNodes = new HashSet<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Node(final String name) {
		this.name = name;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String getName() { return name; }
	public Set<Node> getNextNodes() { return nextNodes; }
	public Set<Node> getPrevNodes() { return prevNodes; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "Node [name=" + name + "]";
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Node other = (Node) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
}