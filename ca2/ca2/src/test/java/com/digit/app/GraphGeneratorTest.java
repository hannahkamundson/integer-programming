package com.digit.app;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


public class GraphGeneratorTest {
    private final Graph graph = createUnconnectedGraph();

    private static Graph createUnconnectedGraph() {
        // Create a disconnected graph
        Graph graph = new Graph(8);
        // cycle 1
        graph.addEdgeWithRandomWeight(1, 2);
        graph.addEdgeWithRandomWeight(2, 3);
        graph.addEdgeWithRandomWeight(1, 3);

        // subgraph 2
        graph.addEdgeWithRandomWeight(4, 5);
        graph.addEdgeWithRandomWeight(4, 6);
        graph.addEdgeWithRandomWeight(5, 6);
        graph.addEdgeWithRandomWeight(5, 7);
        graph.addEdgeWithRandomWeight(6, 7);
        graph.addEdgeWithRandomWeight(4, 7);
        graph.addEdgeWithRandomWeight(4, 8);
        graph.addEdgeWithRandomWeight(7, 8);

        return graph;
    }

    @Test
    public void testGetUnvisitedNodesCycle1() {
        List<Integer> unvisitedNodes = GraphGenerator.getUnvisitedNodes(1, graph);

        Assertions.assertThat(unvisitedNodes).contains(4, 5, 6, 7, 8);
    }

    @Test
    public void testGetUnvisitedNodesSubgraph2() {
        List<Integer> unvisitedNodes = GraphGenerator.getUnvisitedNodes(4, graph);

        Assertions.assertThat(unvisitedNodes).contains(1, 2, 3);
    }

    @Test
    public void testEnsureGraphIsConnected() {
        Graph connectedGraph = GraphGenerator.ensureGraphIsConnected(graph);
        List<Integer> unvisitedNodes = GraphGenerator.getUnvisitedNodes(1, connectedGraph);

        Assertions.assertThat(unvisitedNodes).isEmpty();;
    }
}
