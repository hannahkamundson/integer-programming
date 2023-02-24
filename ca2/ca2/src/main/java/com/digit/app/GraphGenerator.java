package com.digit.app;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.util.*;

public class GraphGenerator {

    /**
     * Create a graph that meets all our criteria.
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static Graph create(int numberOfNodes, double density) {
        Preconditions.checkArgument(density > 0 && density <= 1,
                "The density must be in the range (0,1] but you chose %s".formatted(density));

        // Generate an initial graph based on the algorithm given
        Graph initialGraph = generateInitialGraph(numberOfNodes, density);

        // Check that each node is connected to at least 2 other nodes
        Graph minDegree2Graph = ensureDegree2(initialGraph);

        // Check that the graph is connected
        Graph connectedGraph = ensureGraphIsConnected(minDegree2Graph);

        return connectedGraph;
    }

    /**
     * Create the initial graph with a certain number of nodes and a certain density. This is based on the original
     * algorithm and does not guarantee a connected graph or a minimum degree of 2.
     */
    private static Graph generateInitialGraph(int numberOfNodes, double density) {
        // Create an empty graph and the random number generator
        Graph graph = new Graph(numberOfNodes);
        Random randomGenerator = new Random();

        // For each node number
        for (int node1 = 1; node1 <= numberOfNodes; node1++) {
            // Iterate through every node that is larger than it
            for (int node2 = node1 + 1; node2 <= numberOfNodes; node2++) {
                // Grab a random number between [0, density]
                double probability = randomGenerator.nextDouble();

                if (probability < density && probability > 0) {
                    // Add the edge with the weight to the graph
                    // Note: we don't need to worry about order of nodes because the internal method takes care of it
                    graph.addEdgeWithRandomWeight(node1, node2);
                }
            }
        }

        return graph;
    }

    /**
     * Ensure there is a degree 2 for each node. If there isn't, generate a random edge until it is degree 2.
     */
    private static Graph ensureDegree2(Graph graph) {
        Random random = new Random();;
        int numberOfNodes = graph.getNumberOfNodes();
        // For every node
        for (int node = 1; node <= numberOfNodes; node++) {
            // Get the degree of the node
            int degree = graph.nodeDegree(node);

            // While the node has a degree less than 2, add an edge
            while (degree < 2) {
                // Set the defaults
                // We are not allowed to make an edge yet because we haven't determined a node
                boolean canMakeEdge = false;
                int nodeToConnect = 0;

                // Find an edge we are allowed to make
                while (!canMakeEdge) {
                    // Generate a random number between 1 and the number of nodes
                    nodeToConnect = random.nextInt(numberOfNodes) + 1;

                    // Check if we can use this to make an edge
                    canMakeEdge = graph.canMakeEdge(node, nodeToConnect);
                }

                graph.addEdgeWithRandomWeight(node, nodeToConnect);

                // We added a degree so increment it
                degree++;
            }
        }

        return graph;
    }

    /**
     * Ensure the graph is connected using depth first search. If it isn't connected, add edges until it is connected.
     */
    @VisibleForTesting
    static Graph ensureGraphIsConnected(Graph graph) {
        int numberOfNodes = graph.getNumberOfNodes();
        Random randomGenerator = new Random();
        List<Integer> unconnectedNodes = getUnvisitedNodes(1, graph);
        int numberOfUnconnectedNodes = unconnectedNodes.size();

        // Until all the graph is fully connected, keep connecting the disconnected part to the connected part
        while (numberOfUnconnectedNodes > 0) {
            // Select two indexes randomly in the list
            int unconnectedIndex1 = randomGenerator.nextInt(numberOfUnconnectedNodes);
            int unconnectedIndex2 = randomGenerator.nextInt(numberOfUnconnectedNodes);

            // Make sure they are different nodes. If they aren't keep regenerating the second one until we get
            // a different node
            while (unconnectedIndex1 == unconnectedIndex2) {
                unconnectedIndex2 = randomGenerator.nextInt(numberOfUnconnectedNodes);
            }

            // Get the nodes associated with the index
            int unconnectedNode1 = unconnectedNodes.get(unconnectedIndex1);
            int unconnectedNode2 = unconnectedNodes.get(unconnectedIndex2);

            // Select two nodes at random in the connected bit
            Set<Integer> unusedNodesSet = new HashSet<>(unconnectedNodes);
            int connectedNode1 = selectNodeNotInSet(randomGenerator, unusedNodesSet, numberOfNodes);
            int connectedNode2 = selectNodeNotInSet(randomGenerator, unusedNodesSet, numberOfNodes);

            // Make sure they are different nodes
            while (connectedNode1 == connectedNode2) {
                connectedNode2 = selectNodeNotInSet(randomGenerator, unusedNodesSet, numberOfNodes);
            }

            // Add edges to the nodes
            graph.addEdgeWithRandomWeight(unconnectedNode1, connectedNode1);
            graph.addEdgeWithRandomWeight(unconnectedNode2, connectedNode2);

            // Get the unconnected nodes and store their size
            unconnectedNodes = getUnvisitedNodes(1, graph);
            numberOfUnconnectedNodes = unconnectedNodes.size();
        };

        return graph;
    }

    /**
     * Select a random node that isn't in the set passed in
     */
    private static int selectNodeNotInSet(Random randomGenerator, Set<Integer> alreadyUsedNodes, int numberOfNodes) {
        int newNode = randomGenerator.nextInt(numberOfNodes) + 1;

        while (alreadyUsedNodes.contains(newNode)) {
            newNode = randomGenerator.nextInt(numberOfNodes) + 1;
        }

        return newNode;
    }

    /**
     * Run a depth first search on a node and return a list of nodes that weren't visited
     */
    @SuppressWarnings("SameParameterValue")
    @VisibleForTesting
    static List<Integer> getUnvisitedNodes(int startingNode, Graph graph) {
        int numberOfNodes = graph.getNumberOfNodes();
        // An array that keeps track of whether a node has been visited
        boolean[] visited = new boolean[numberOfNodes];
        // A stack that keeps track of upcoming nodes to visit
        Stack<Integer> upcomingNodes = new Stack<>();

         upcomingNodes.add(startingNode);

        while (!upcomingNodes.isEmpty()) {
            // Get the latest node to be added
            int node = upcomingNodes.pop();

            // Set it to visited so that we don't run through it again
            visited[node - 1] = true;

            // Get adjacent nodes to the current one and filter out any that have already been visited
            List<Integer> unvisitedAdjacentNodes = graph.getAdjacent(node).stream()
                    .filter(i -> !visited[i - 1])
                    .toList();

            // Add the unvisited adjacent nodes to the stack
            upcomingNodes.addAll(unvisitedAdjacentNodes);
        }

        // We now have an array of nodes that were visited. Return a list of any nodes that weren't
        // visited
        List<Integer> unvisitedNodes = new ArrayList<>();
        for (int i = 0; i < visited.length; i++) {
            // If it wasn't visited, add the node number to the unvisited list
            if (!visited[i]) {
                unvisitedNodes.add(i + 1);
            }
        }

        return unvisitedNodes;
    }
}
