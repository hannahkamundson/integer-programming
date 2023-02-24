package com.digit.app;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CycleDetectionTest {

    @Test
    public void pattonFundamentalSetOfCyclesTest() {
        Graph graph = new Graph(8);

        graph.addEdgeWithRandomWeight(1, 2);
        graph.addEdgeWithRandomWeight(1, 5);
        graph.addEdgeWithRandomWeight(2, 4);
        graph.addEdgeWithRandomWeight(3, 4);
        graph.addEdgeWithRandomWeight(3, 5);
        graph.addEdgeWithRandomWeight(3, 6);
        graph.addEdgeWithRandomWeight(3, 7);
        graph.addEdgeWithRandomWeight(4, 6);
        graph.addEdgeWithRandomWeight(4, 7);
        graph.addEdgeWithRandomWeight(5, 6);
        graph.addEdgeWithRandomWeight(5, 7);
        graph.addEdgeWithRandomWeight(6, 7);

        List<List<Integer>> cycleBasis = CycleDetection.pattonFundamentalSetOfCycles(graph);
        Assertions.assertThat(cycleBasis).containsExactly(
                List.of(3, 5, 7),
                List.of(6, 5, 7),
                List.of(2, 1, 5, 7, 4),
                List.of(3, 5, 7, 4),
                List.of(6, 5, 7, 4),
                List.of(3, 5, 6)
        );
    }

    @Test
    public void cycleToIncidenceVectorTest() {
        List<Integer> cycle = List.of(1, 3, 5, 9);
        byte[] expected = new byte[] {
                0b1,
                0b0,
                0b1,
                0b0,
                0b1,
                0b0,
                0b0,
                0b0,
                0b1,
                0b0
        };

        byte[] actual = CycleDetection.cycleToIncidenceVector(cycle, 10).getInner();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

}
