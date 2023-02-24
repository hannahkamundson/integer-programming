package com.digit.app;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This represents an undirected graph.
 */
public class Graph {
    /**
     * This holds the node adjacency matrix. Since it is confusing to deal with indexes to node number conversions,
     * we are wrapping this in a class so we can just communicate to it with node numbers.
     */
    private final NodeAdjacencyMatrix nodeAdjacencyMatrix;

    public Graph(int numberOfNodes) {
        // Array of arrays => [row][column]
        nodeAdjacencyMatrix = new NodeAdjacencyMatrix(numberOfNodes);
    }

    /**
     * Add an edge between the vertices with a random weight between 1 and 10.
     */
    public void addEdgeWithRandomWeight(int v1, int v2) {
        Random randomGenerator = new Random();
        // Choose a weight between 1 and 10
        int weight = randomGenerator.nextInt(10) + 1;

        // Add the edge
        addEdge(v1, v2, weight);
    }

    /**
     * Add an edge between two nodes with a given weight. The order of adding the vertices do not matter as it is an
     * undirected graph.
     */
    public void addEdge(int v1, int v2, int weight) {
        // Check to make sure we aren't trying to add a self loop
        Preconditions.checkArgument(v1 != v2, "Self loops are not allowed");

        // Grab the larger and smaller ones. We only allow an edge from larger -> smaller
        int columnNode = Math.max(v1, v2);
        int rowNode = Math.min(v1, v2);

        Preconditions.checkArgument(canMakeEdge(rowNode, columnNode),
                "There is already a weight for the edge (%s, %s)".formatted(rowNode, columnNode));

        // Add an edge to the edge between the nodes
        nodeAdjacencyMatrix.insert(rowNode, columnNode, weight);
    }

    /**
     * Get the nodes that are adjacent to a given node.
     */
    public List<Integer> getAdjacent(int node) {
        List<Integer> adjacent = new ArrayList<>();
        int numberOfNodes = getNumberOfNodes();

        // We need to get all elements in the column up to the node number + then we need to get all elements in the
        // row starting at the node number to the end.


        // Go to the column of the node we want. Iterate through all the rows of that column up to the node itself
        // But we don't want to do this if the node is the first one since the first node isn't listed in the columns
        if (node != 1) {
            // Iterate through all the rows of that column up to the node itself
            for (int i = 1; i < node; i++) {
                // If it has a value, add the node to the stack
                if (nodeAdjacencyMatrix.get(i, node) > 0) {
                    adjacent.add(i);
                }
            }
        }

        // Go to the row of the node we want. Iterate through all the columns of that row starting at the node itself
        // But we don't want to do this if it is the last node since that node is only represented in the columns
        if (node != numberOfNodes) {
            // Iterate through all the columns of that row starting at the node itself
            for (int i = node + 1; i <= numberOfNodes; i++) {
                // If it has a value, add the node to the stack
                if (nodeAdjacencyMatrix.get(node, i) > 0) {
                    adjacent.add(i);
                }
            }
        }

        return adjacent;
    }

    /**
     * Get the degree of a given node
     */
    public int nodeDegree(int node) {
        // Get all adjacent nodes
        List<Integer> adjacent = getAdjacent(node);

        // Return the size of the adjacent nodes
        return adjacent.size();
    }

    /**
     * Are we allowed to make another edge here?
     */
    public boolean canMakeEdge(int v1, int v2) {
        // We can't make an edge if they are the same
        if (v1 == v2) {
            return false;
        }

        // Grab the larger and smaller ones. We only allow an edge from larger -> smaller
        int columnNode = Math.max(v1, v2);
        int rowNode = Math.min(v1, v2);

        // We are allowed to make an edge if we haven't yet made one
        return nodeAdjacencyMatrix.get(rowNode, columnNode) == 0;
    }

    /**
     * Return the total number of nodes
     */
    public int getNumberOfNodes() {
        return nodeAdjacencyMatrix.getSize();
    }

    /**
     * Output the node adjacency matrix
     */
    @Override
    public String toString() {
        return nodeAdjacencyMatrix.toString();
    }
}
