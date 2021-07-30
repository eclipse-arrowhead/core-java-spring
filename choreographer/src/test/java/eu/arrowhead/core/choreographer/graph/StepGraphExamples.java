package eu.arrowhead.core.choreographer.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class StepGraphExamples {

	private static List<StepGraph> nonCirculars = new ArrayList<>();
	private static List<StepGraph> circulars = new ArrayList<>();
	
	private static List<Pair<StepGraph,StepGraph>> normalizables = new ArrayList<>();
	
	static {
		initNonCirculars();
		initCirculars();
		initNormalizables();
	}
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static List<StepGraph> getNonCirculars() { return nonCirculars; }
	public static List<StepGraph> getCirculars() { return circulars; }
	public static List<Pair<StepGraph,StepGraph>> getNormalizables() { return normalizables; }
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private static void initNonCirculars() {
		//------------------------------
		// diamond without circle
		Node n1 = new Node("n1");
		Node n2 = new Node("n2");
		Node n3 = new Node("n3");
		Node n4 = new Node("n4");
		n1.getNextNodes().addAll(Set.of(n2, n3));
		
		n2.getNextNodes().add(n4);
		n2.getPrevNodes().add(n1);
		
		n3.getNextNodes().add(n4);
		n3.getPrevNodes().add(n1);
		
		n4.getPrevNodes().addAll(Set.of(n2, n3));
		
		StepGraph graph = new StepGraph();
		graph.getSteps().addAll(Set.of(n1, n2, n3, n4));
		nonCirculars.add(graph);
		
		//------------------------------
		// independent subgraphs 
		n1 = new Node("n1");
		n2 = new Node("n2");
		n3 = new Node("n3");
		n4 = new Node("n4");
		
		n1.getNextNodes().add(n3);
		n2.getNextNodes().add(n4);
		n3.getPrevNodes().add(n1);
		n4.getPrevNodes().add(n2);
		
		graph = new StepGraph();
		graph.getSteps().addAll(Set.of(n1, n2, n3, n4));
		nonCirculars.add(graph);
		
		//------------------------------
		// complex graph without circle
		n1 = new Node("n1");
		n2 = new Node("n2");
		n3 = new Node("n3");
		n4 = new Node("n4");
		Node n5 = new Node("n5");
		Node n6 = new Node("n6");
		Node n7 = new Node("n7");
		Node n8 = new Node("n8");
		Node n9 = new Node("n9");
		n1.getNextNodes().addAll(Set.of(n2, n3, n4));
		
		n2.getNextNodes().addAll(Set.of(n4, n5, n6));
		n2.getPrevNodes().add(n1);
		
		n3.getNextNodes().add(n6);
		n3.getPrevNodes().add(n1);
		
		n4.getNextNodes().add(n8);
		n4.getPrevNodes().addAll(Set.of(n1, n2));
		
		n5.getPrevNodes().add(n2);
		
		n6.getNextNodes().add(n9);
		n6.getPrevNodes().addAll(Set.of(n2, n3));
		
		n7.getNextNodes().add(n8);
		n7.getPrevNodes().add(n9);
		
		n8.getPrevNodes().addAll(Set.of(n4, n7));
		
		n9.getNextNodes().add(n7);
		n9.getPrevNodes().add(n6);
		
		graph = new StepGraph();
		graph.getSteps().addAll(Set.of(n1, n2, n3, n4, n5, n6, n7, n8, n9));
		nonCirculars.add(graph);		
	}

	//-------------------------------------------------------------------------------------------------
	private static void initCirculars() {
		//------------------------------
		// self neighbor node
		Node n1 = new Node("n1");
		n1.getNextNodes().add(n1);
		n1.getPrevNodes().add(n1);
		
		StepGraph graph = new StepGraph();
		graph.getSteps().add(n1);
		circulars.add(graph);
		
		//------------------------------
		// 2-length circle
		n1 = new Node("n1");
		Node n2 = new Node("n2");
		n1.getNextNodes().add(n2);
		n1.getPrevNodes().add(n2);
		n2.getNextNodes().add(n1);
		n2.getPrevNodes().add(n1);
		
		graph = new StepGraph();
		graph.getSteps().addAll(Set.of(n1, n2));
		circulars.add(graph);
		
		//------------------------------
		// diamond circle
		n1 = new Node("n1");
		n2 = new Node("n2");
		Node n3 = new Node("n3");
		Node n4 = new Node("n4");
		n1.getNextNodes().add(n2);
		n1.getPrevNodes().add(n4);
		n2.getNextNodes().add(n3);
		n2.getPrevNodes().add(n1);
		n3.getNextNodes().add(n4);
		n3.getPrevNodes().add(n2);
		n4.getNextNodes().add(n1);
		n4.getPrevNodes().add(n3);
		
		graph = new StepGraph();
		graph.getSteps().addAll(Set.of(n1, n2, n3, n4));
		circulars.add(graph);
		
		//------------------------------
		// complex graph with circle
		n1 = new Node("n1");
		n2 = new Node("n2");
		n3 = new Node("n3");
		n4 = new Node("n4");
		Node n5 = new Node("n5");
		Node n6 = new Node("n6");
		Node n7 = new Node("n7");
		Node n8 = new Node("n8");
		Node n9 = new Node("n9");
		n1.getNextNodes().addAll(Set.of(n2, n3, n4));
		
		n2.getNextNodes().addAll(Set.of(n4, n5, n6));
		n2.getPrevNodes().add(n1);
		
		n3.getNextNodes().add(n6);
		n3.getPrevNodes().addAll(Set.of(n1, n7));
		
		n4.getNextNodes().add(n8);
		n4.getPrevNodes().addAll(Set.of(n1, n2));
		
		n5.getPrevNodes().add(n2);
		
		n6.getNextNodes().add(n9);
		n6.getPrevNodes().addAll(Set.of(n2, n3));
		
		n7.getNextNodes().addAll(Set.of(n3, n8));
		n7.getPrevNodes().add(n9);
		
		n8.getPrevNodes().addAll(Set.of(n4, n7));
		
		n9.getNextNodes().add(n7);
		n9.getPrevNodes().add(n6);
		
		graph = new StepGraph();
		graph.getSteps().addAll(Set.of(n1, n2, n3, n4, n5, n6, n7, n8, n9));
		circulars.add(graph);
	}
	
	//-------------------------------------------------------------------------------------------------
	private static void initNormalizables() {
		//------------------------------
		// simple - 1 deletion
		Node n1 = new Node("n1");
		Node n2 = new Node("n2");
		Node n3 = new Node("n3");
		Node n4 = new Node("n4");
		n1.getNextNodes().addAll(Set.of(n2, n3));
		
		n2.getNextNodes().add(n3);
		n2.getPrevNodes().add(n1);
		
		n3.getNextNodes().add(n4);
		n3.getPrevNodes().addAll(Set.of(n1, n2));
		
		n4.getPrevNodes().add(n3);
		
		StepGraph graph = new StepGraph();
		graph.getSteps().addAll(Set.of(n1, n2, n3, n4));
		
		n1 = new Node("n1");
		n2 = new Node("n2");
		n3 = new Node("n3");
		n4 = new Node("n4");
		n1.getNextNodes().add(n2);
		
		n2.getNextNodes().add(n3);
		n2.getPrevNodes().add(n1);
		
		n3.getNextNodes().add(n4);
		n3.getPrevNodes().add(n2);
		
		n4.getPrevNodes().add(n3);
		
		StepGraph resultGraph = new StepGraph();
		resultGraph.getSteps().addAll(Set.of(n1, n2, n3, n4));
		
		normalizables.add(Pair.of(graph, resultGraph));
		
		//------------------------------
		// complex - 1 deletion 
		n1 = new Node("n1");
		n2 = new Node("n2");
		n3 = new Node("n3");
		n4 = new Node("n4");
		Node n5 = new Node("n5"); 
		
		n1.getNextNodes().addAll(Set.of(n2, n3));
		
		n2.getNextNodes().addAll(Set.of(n4, n5));
		n2.getPrevNodes().add(n1);
		
		n3.getNextNodes().add(n4);
		n3.getPrevNodes().add(n1);
		
		n4.getNextNodes().add(n5);
		n4.getPrevNodes().addAll(Set.of(n2, n3));

		n5.getPrevNodes().addAll(Set.of(n2, n4));
		graph = new StepGraph();
		graph.getSteps().addAll(Set.of(n1, n2, n3, n4, n5));
		
		
		n1 = new Node("n1");
		n2 = new Node("n2");
		n3 = new Node("n3");
		n4 = new Node("n4");
		n5 = new Node("n5"); 
		
		n1.getNextNodes().addAll(Set.of(n2, n3));
		
		n2.getNextNodes().add(n4);
		n2.getPrevNodes().add(n1);
		
		n3.getNextNodes().add(n4);
		n3.getPrevNodes().add(n1);
		
		n4.getNextNodes().add(n5);
		n4.getPrevNodes().addAll(Set.of(n2, n3));

		n5.getPrevNodes().add(n4);
		
		resultGraph = new StepGraph();
		resultGraph.getSteps().addAll(Set.of(n1, n2, n3, n4, n5));
		
		normalizables.add(Pair.of(graph, resultGraph));
		
		//------------------------------
		// complex - 3 deletion 
		n1 = new Node("n1");
		n2 = new Node("n2");
		n3 = new Node("n3");
		n4 = new Node("n4");
		n5 = new Node("n5"); 
		
		n1.getNextNodes().addAll(Set.of(n2, n3, n4, n5));
		
		n2.getNextNodes().addAll(Set.of(n4, n5));
		n2.getPrevNodes().add(n1);
		
		n3.getNextNodes().add(n4);
		n3.getPrevNodes().add(n1);
		
		n4.getNextNodes().add(n5);
		n4.getPrevNodes().addAll(Set.of(n1, n2, n3));

		n5.getPrevNodes().addAll(Set.of(n1, n2, n4));
		graph = new StepGraph();
		graph.getSteps().addAll(Set.of(n1, n2, n3, n4, n5));
		
		n1 = new Node("n1");
		n2 = new Node("n2");
		n3 = new Node("n3");
		n4 = new Node("n4");
		n5 = new Node("n5"); 
		
		n1.getNextNodes().addAll(Set.of(n2, n3));
		
		n2.getNextNodes().add(n4);
		n2.getPrevNodes().add(n1);
		
		n3.getNextNodes().add(n4);
		n3.getPrevNodes().add(n1);
		
		n4.getNextNodes().add(n5);
		n4.getPrevNodes().addAll(Set.of(n2, n3));

		n5.getPrevNodes().add(n4);
		
		resultGraph = new StepGraph();
		resultGraph.getSteps().addAll(Set.of(n1, n2, n3, n4, n5));
		
		normalizables.add(Pair.of(graph, resultGraph));
		
		//------------------------------
		// diamond without circle
		n1 = new Node("n1");
		n2 = new Node("n2");
		n3 = new Node("n3");
		n4 = new Node("n4");
		n1.getNextNodes().addAll(Set.of(n2, n3));
		
		n2.getNextNodes().add(n4);
		n2.getPrevNodes().add(n1);
		
		n3.getNextNodes().add(n4);
		n3.getPrevNodes().add(n1);
		
		n4.getPrevNodes().addAll(Set.of(n2, n3));
		
		graph = new StepGraph();
		graph.getSteps().addAll(Set.of(n1, n2, n3, n4));
		normalizables.add(Pair.of(graph, graph));
		
		//------------------------------
		// independent subgraphs 
		n1 = new Node("n1");
		n2 = new Node("n2");
		n3 = new Node("n3");
		n4 = new Node("n4");
		
		n1.getNextNodes().add(n3);
		n2.getNextNodes().add(n4);
		n3.getPrevNodes().add(n1);
		n4.getPrevNodes().add(n2);
		
		graph = new StepGraph();
		graph.getSteps().addAll(Set.of(n1, n2, n3, n4));
		normalizables.add(Pair.of(graph, graph));
	}
}