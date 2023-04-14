package com.digit.app.data;

import com.digit.app.Pair;
import lombok.Value;

@Value
public class Data {
    private final Constraints constraints;
    private final OptimizationFunction optimizationFn;

    public static Data create(int[][] A, int[] b, boolean[] AlessThanb, double[] c) {
        Constraints constraints = new Constraints(A, b, AlessThanb);
        OptimizationFunction optimizationFunction = new OptimizationFunction(c);

        return new Data(constraints, optimizationFunction);
    }

    private Data(Constraints constraints, OptimizationFunction optimizationFn) {
        // Initialize
        this.constraints = constraints;
        this.optimizationFn = optimizationFn;

        // Ensure the A matrix has the correct number of variables
        this.constraints.assertCorrectNumberOfVariables(optimizationFn.getNumVariables());
    }

    /**
     * Return the data for the new lagrangian relaxed function
     */
    public Data relax(int constraintsToMove) {
        // Remove the constraints that we want to move
        Constraints newConstraints = constraints.relax(constraintsToMove);
        // Pass in the original constraints and the number of constraints to add to lagrangian
        OptimizationFunction newOptFn = optimizationFn.relax(constraintsToMove, constraints);

        return new Data(newConstraints, newOptFn);
    }

    /**
     * Simplify the value with a lagrange multiplier to just have a C
     */
    public Pair<Data, Double> withLagrange(double[] lagrangeMultiplier) {
        Pair<OptimizationFunction, Double> pair = optimizationFn.withLagrange(lagrangeMultiplier);
        return new Pair<>(new Data(constraints, pair.getLeft()), pair.getRight());
    }

    /**
     * Make the output pretty so that we can actually see what the function looks like
     */
    @Override
    public String toString() {

        return "Data for %s constraints and %s variables\n".formatted(constraints.getNumConstraints(), optimizationFn.getNumVariables()) +
                optimizationFn +
                constraints;
    }
}
