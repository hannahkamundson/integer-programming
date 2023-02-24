package com.digit.app;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

import java.util.*;

public class CycleDetection {

    /**
     * Get all cycles in a graph.
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static List<List<Integer>> findCycles(Graph graph) {
        List<List<Integer>> basisCycles = pattonFundamentalSetOfCycles(graph);
        List<List<Integer>> allCycles = findAllCycles(basisCycles, graph.getNumberOfNodes());

        return allCycles;
    }

    /**
     * Find all cycles by creating all combinations of the basis cycles.
     */
    public static List<List<Integer>> findAllCycles(List<List<Integer>> basisCycles, int numberOfNodes) {
        // Store the basis cycles in a set of all cycles and map it to the incidence vector
        Map<ByteWrapper, List<Integer>> allCycles = new HashMap<>();

        // Convert the basis cycles to their incidence vectors
        for (List<Integer> cycle: basisCycles) {
            allCycles.put(cycleToIncidenceVector(cycle, numberOfNodes), cycle);
        }

        // Get all combos of the pairs and add them to the queue
        Set<Set<ByteWrapper>> allPairs = Sets.combinations(allCycles.keySet(), 2);
        Queue<Set<ByteWrapper>> pairsToExamine = new ArrayDeque<>(allPairs);
        Set<Set<ByteWrapper>> pairsAlreadySeen = new HashSet<>(allPairs);

        // For every pair of cycles
        while (!pairsToExamine.isEmpty()) {
            // Get the unevaluated pair
            List<ByteWrapper> pair = pairsToExamine.poll().stream().toList();

            // If we can create a cycle from it
            if (canCreateCycleWithPair(pair)) {
                // Create a cycle
                ByteWrapper newCycleIncidence = constructCycle(pair);

                // If this cycle doesn't have more than 2 elements, we need to pass through
                if (!isCycle(newCycleIncidence)) {
                    continue;
                }

                // If we haven't yet recorded this cycle
                if (!allCycles.containsKey(newCycleIncidence)) {
                    // Iterate through all cycles that have already been created and add the pair of the two to be
                    // evaluated
                    for (ByteWrapper oldCycleIncidence: allCycles.keySet()) {
                        Set<ByteWrapper> newPair = new HashSet<>();
                        newPair.add(oldCycleIncidence);
                        newPair.add(newCycleIncidence);

                        // If we haven't already examined this pair, add it
                        if (!pairsAlreadySeen.contains(newPair)) {
                            pairsToExamine.add(newPair);
                            pairsAlreadySeen.add(newPair);
                        }
                    }

                    // Get the new cycle and put it in the map
                    allCycles.put(newCycleIncidence, convertFromIncidenceVector(newCycleIncidence));
                }
            }

        }

        return allCycles.values().stream().toList();
    }

    /**
     * Is this byte array actually a cycle?
     */
    private static boolean isCycle(ByteWrapper cycle) {
        int count = 0;

        for (byte node: cycle.getInner()) {
            // If the node is present, add to the count
            if (node > 0) {
                count++;
            }

            // If we have more than 2 nodes, we can have a cycle
            if (count > 2) {
                return true;
            }
        }

        // Otherwise, we didn't find a cycle, and we need to say it shouldn't be a cycle
        return false;
    }

    /**
     * Construct a cycle from an incidence vector.
     */
    private static ByteWrapper constructCycle(List<ByteWrapper> incidenceVectors) {
        // Get the incidence vectors
        byte[] vector1 = incidenceVectors.get(0).getInner();
        byte[] vector2 = incidenceVectors.get(1).getInner();

        // Store the new cycle here
        byte[] newCycle = new byte[vector1.length];

        // XOR them to get the new cycle
        for (int i = 0; i < vector1.length; i++) {
            newCycle[i] = (byte) (vector1[i] ^ vector2[i]);
        }

        return new ByteWrapper(newCycle);
    }

    /**
     * Convert the incidence vector back to the list of nodes in the cycle
     */
    private static List<Integer> convertFromIncidenceVector(ByteWrapper incidenceWrapper) {
        byte[] incidenceVector = incidenceWrapper.getInner();
        List<Integer> converted = new ArrayList<>();
        // For every node
        for (int i = 0; i < incidenceVector.length; i++) {
            // If it is stored in the incidence vector
            if (incidenceVector[i] > 0) {
                // Add the node number to the list
                converted.add(i + 1);
            }
        }

        return converted;
    }

    /**
     * Can these two cycles form a new cycle?
     */
    private static boolean canCreateCycleWithPair(List<ByteWrapper> incidenceVectors) {
        byte[] vector1 = incidenceVectors.get(0).getInner();
        byte[] vector2 = incidenceVectors.get(1).getInner();

        // We can't make a cycle if they are the same
        if (Arrays.equals(vector1, vector2)) {
            return false;
        }

        // See if the pair can match by doing bit AND
        for (int i = 0; i < incidenceVectors.size(); i++) {

            // If at least one edge matches, return true
            if ((vector1[i] & vector2[i]) > 0) {
                return true;
            }
        }

        // Otherwise, they don't share edges, and we cannot make a cycle for it
        return false;
    }


    /**
     * Get the cycle basis by creating a spanning tree. At each new node, look and see if we should add the edge to
     * the tree or if it creates a cycle.
     * <p>
     * Algorithm here: https://dl-acm-org.ezproxy.lib.utexas.edu/doi/abs/10.1145/363219.363232
     */
    @VisibleForTesting
    static List<List<Integer>> pattonFundamentalSetOfCycles(Graph graph) {
        List<List<Integer>> cycleBasis = new ArrayList<>();
        // We know it is connected, so we should be able to reach everything from a random starting root
        int rootNode = 1;
        // This holds the parent of a given node. Note, index = node - 1
        int[] parent = new int[graph.getNumberOfNodes()];
        // This holds whether a given node has already been looked at. Note, index = node - 1
        boolean[] visited = new boolean[graph.getNumberOfNodes()];
        // This holds nodes we haven't yet looked at
        Stack<Integer> nodesToLookAt = new Stack<>();
        // Set the parent of the root node to itself that way we don't try to find a parent for it
        parent[rootNode - 1] = rootNode;
        // Set root node as visited
        visited[rootNode - 1] = true;

        // For every node adjacent to our root node
        for (int adjacent: graph.getAdjacent(rootNode)) {
            // Set the parent of the adjacent node to be the root node
            parent[adjacent - 1] = rootNode;

            // Add these new nodes to the stack of nodes to look at
            nodesToLookAt.add(adjacent);
        }

        while (!nodesToLookAt.isEmpty()) {
            // Get the most recent node
            int node = nodesToLookAt.pop();

            // For every neighbor of the node we are looking at
            for (int neighbor: graph.getAdjacent(node)) {
                // If we've already looked at a node, don't look at it again
                if (visited[neighbor - 1]) {
                    continue;
                }

                // If the neighbor is already in the spanning tree
                if (parent[neighbor - 1] > 0) {
                    // We know the cycle is the parent from node to root + neighbor to root + node to neighbor
                    List<Integer> neighborPath = new ArrayList<>();
                    int currentNode = neighbor;
                    neighborPath.add(neighbor);

                    // Iterate up from the current node to the root node
                    while (currentNode != rootNode) {
                        currentNode = parent[currentNode - 1];
                        neighborPath.add(currentNode);
                    }

                    // We don't always want to iterate all the way up to the root. We need to iterate up until one of
                    // the values was seen in the other
                    Set<Integer> usedByNeighbor = new HashSet<>(neighborPath);

                    List<Integer> nodePath = new ArrayList<>();
                    currentNode = node;

                    // Keep iterating as long as the current node hasn't been used by the neighbor
                    while (!usedByNeighbor.contains(currentNode)) {
                        nodePath.add(currentNode);
                        currentNode = parent[currentNode - 1];
                    }

                    // If the current node isn't the root node, we need to iterate up in neighbor path and remove
                    if (currentNode != rootNode) {
                        // For the last parts of the path
                        for (int i = neighborPath.size() - 1; i > 0; i--) {
                            // remove it if it isn't the last item of the neighbor
                            if (neighborPath.get(i) != currentNode) {
                                neighborPath.remove(i);
                            }
                        }
                    }

                    // Create the cycle by starting with the neighbor path
                    List<Integer> cycle = new ArrayList<>(neighborPath);
                    // Reverse the node list and add it. That way the root is touching
                    Collections.reverse(nodePath);
                    cycle.addAll(nodePath);

                    // Add the cycle to the list of basis cycles
                    cycleBasis.add(cycle);

                // Otherwise, if the neighbor is not in the spanning tree
                } else {
                    // Set its parent to the node we are on
                    parent[neighbor - 1] = node;

                    // Add it to a node that needs to be looked at
                    nodesToLookAt.add(neighbor);
                }
            }

            // Mark the node as visited since we just looked at all its neighbors
            visited[node - 1] = true;
        }

        return cycleBasis;
    }

    /**
     * Convert a cycle to an incidence vector. Returned vector will have a 1 if the element was in the array and 0
     * otherwise.
     */
    @VisibleForTesting
    static ByteWrapper cycleToIncidenceVector(List<Integer> cycle, int sizeOfArray) {
        byte[] incidenceVector = new byte[sizeOfArray];

        // For every node in the array
        for (Integer node: cycle) {
            incidenceVector[node - 1] = 0b1;
        }

        return new ByteWrapper(incidenceVector);
    }

    public static String toPrettyString(List<List<Integer>> cycles) {
        StringBuilder builder = new StringBuilder();
        builder.append("%s cycles were found\n".formatted(cycles.size()));
        for (List<Integer> cycle: cycles) {
            for (Integer node: cycle) {
                builder.append("%3s ->".formatted(node));
            }
            builder.append("%3s".formatted(cycle.get(0)));
            builder.append("\n");
        }

        return builder.toString();
    }

    /**
     * This wraps the byte array since bytes don't hash normally since they are primitive
     */
    static class ByteWrapper {
        private byte[] inner;

        public ByteWrapper(byte[] inner) {
            this.inner = inner;
        }

        public byte[] getInner() {
            return inner;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ByteWrapper)) {
                return false;
            }
            return Arrays.equals(inner, ((ByteWrapper)other).inner);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(inner);
        }

        @Override
        public String toString() {
            return Arrays.toString(inner);
        }
    }
}
