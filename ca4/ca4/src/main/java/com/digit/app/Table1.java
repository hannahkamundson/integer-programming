package com.digit.app;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class Table1 {
    private final int round;
    private final int iteration;
    private final double[] lagrangeMultiplier;
    private final double lagrangeOptimal;
    private final double stepSize;
    private final double e;
    private final double violationSquared;
    private final double originalProblem;

    public static String title() {

        return "%s|".formatted("k") +
                "%s|".formatted("mu") +
                "%s|".formatted("e") +
                "%s|".formatted("vs") +
                "%s|".formatted("Z_IP") +
                "%s|".formatted("Z_LR") +
                "L1|L2|L3|L4|L5\n| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |";
    }

    @Override
    public String toString() {

        return "|%s|".formatted(iteration) +
                "%f|".formatted(stepSize) +
                "%f|".formatted(e) +
                "%f|".formatted(violationSquared) +
                "%f|".formatted(originalProblem) +
                "%f|".formatted(lagrangeOptimal) +
                Table1.doubleArrayToString(lagrangeMultiplier);
    }

    private static String doubleArrayToString(double[] toPrint) {
        StringBuilder builder = new StringBuilder();
        for (double value: toPrint) {
            builder.append("%.5f|".formatted(value));
        }

        return builder.toString();
    }
}
