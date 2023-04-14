package com.digit.app;

import lombok.Value;

@Value
public class IPSolution {
    private final boolean feasible;
    private final double optimizationValue;
    private final double[] variables;

    private final double[] slack;

    /**
     * Create a feasible solution
     */
    public IPSolution(double optimizationValue, double[] solution, double[] slack) {
        this.feasible = true;
        this.optimizationValue = optimizationValue;
        this.variables = solution;
        this.slack = slack;
    }

    /**
     * Return a solution that is infeasible
     */
    public static IPSolution infeasible() {
        return new IPSolution(false);
    }

    /**
     * Allow us to create an infeasible solution but make it private
     * so people aren't calling this.
     */
    private IPSolution(boolean feasible) {
        this.feasible = feasible;
        this.optimizationValue = 0;
        this.variables = new double[0];
        this.slack = new double[0];
    }

    @Override
    public String toString() {
        if (!feasible) {
            return "The problem is infeasible";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("The problem is feasible\n");
        builder.append("Objective function value: %s\n".formatted(optimizationValue));
        builder.append("Optimal values:\n");

        builder.append("%5s ".formatted(""));

        // Create description above
        for (int i = 0; i < variables.length; i++) {
            builder.append("%5s ".formatted("x" + (i + 1)));
        }
        builder.append("\n");
        builder.append("%5s ".formatted(""));

        for (double value: variables) {
            builder.append("%.5f ".formatted(value));
        }

        if (slack == null) {
            return builder.toString();
        }

        builder.append("\n");
        builder.append("Slack values:\n");
        builder.append("%5s ".formatted(""));

        for (int i = 0; i < slack.length; i++) {
            builder.append("%5s ".formatted("s" + (i + 1)));
        }

        builder.append("\n");
        builder.append("%5s ".formatted(""));

        for (double value: slack) {
            builder.append("%.5f ".formatted(value));
        }

        return builder.toString();
    }
}
