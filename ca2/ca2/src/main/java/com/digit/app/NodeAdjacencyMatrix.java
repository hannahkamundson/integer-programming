package com.digit.app;

import com.google.common.base.Preconditions;

/**
 * This is a wrapper around the node-node adjacency matrix. Since our matrix has rows and columns
 * that numerically don't line up with the indexes, it is easier to wrap this so that we don't have to
 * keep track of the index to node number math.
 */
public class NodeAdjacencyMatrix {
    private final int size;

    /**
     * This is a node-node adjacency matrix. The [row][column] is represented as a weight if j>i and i is connected to j.
     * Otherwise, it is 0.
     * <p>
     * Additionally, rows are 1,...,n-1 and columns are 2,...,n
     */
    private final int[][] nodeAdjacency;

    public NodeAdjacencyMatrix(int size) {
        this.size = size;
        // Array of arrays => [row][column]
        nodeAdjacency = new int[size - 1][size - 1];
    }

    /**
     * Insert an edge by node number
     */
    public void insert(int rowNode, int columnNode, int value) {
        // Get the row and column index where they will be stored
        int row = rowIndexByNodeNumber(rowNode);
        int col = columnIndexByNodeNumber(columnNode);
        nodeAdjacency[row][col] = value;
    }

    /**
     * Get the value of an edge by node numbers
     */
    public int get(int rowNode, int columnNode) {
        int row = rowIndexByNodeNumber(rowNode);
        int col = columnIndexByNodeNumber(columnNode);

        return nodeAdjacency[row][col];
    }

    /**
     * This gets the row index number for a given node.
     */
    private int rowIndexByNodeNumber(int nodeNumber) {
        Preconditions.checkArgument(nodeNumber > 0 && nodeNumber < size,
                "The rows represent nodes %s through %s.".formatted(1, size - 1));

        // Node 1 is stored in index 0, node 2 is stored in index 1, etc
        return nodeNumber - 1;
    }

    /**
     * This gets the column index number for a given node.
     */
    private int columnIndexByNodeNumber(int nodeNumber) {
        Preconditions.checkArgument(nodeNumber > 1 && nodeNumber < size + 1,
                "The columns represent nodes %s through %s.".formatted(2, size));

        // Node 2 is in index 0, Node 3 is in index 1, etc
        return nodeNumber - 2;
    }

    public int getSize() {
        return size;
    }

    /**
     * Output this graph in a clean way. Output the matrix with the labels of each node and space it out so we can
     * clearly see what is going on.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Graph with %s vertices\n".formatted(size));

        builder.append("%4s ".formatted(" "));
        builder.append("%4s ".formatted(" "));

        // Create labels for the rows
        for (int i = 2; i <= size; i++) {
            builder.append("%4s ".formatted(i));
        }

        builder.append("\n");
        builder.append("%4s ".formatted(" "));
        builder.append("%4s ".formatted(" "));

        for (int i = 2; i <= size; i++) {
            builder.append("%4s ".formatted("--"));
        }

        builder.append("\n");


        // Output each element
        for (int i = 0; i < size - 1; i++) {
            int[] row = nodeAdjacency[i];
            builder.append("%4s ".formatted(i + 1));
            builder.append("%4s ".formatted("|"));
            for (int e : row) {
                builder.append("%4s ".formatted(e));
            }
            builder.append("\n");
        }

        return builder.toString();
    }
}
