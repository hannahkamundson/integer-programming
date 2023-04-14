package com.digit.app.data;

import com.digit.app.Pair;
import com.google.common.base.Preconditions;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import lombok.Value;

import java.util.Arrays;

@Value
public class OptimizationFunction {
    /**
     * The number of variables in this program
     */
    private final int numVariables;

    /**
     * The optimization function coefficients
     */
    private final double[] c;

    /**
     * The coefficients for the lagrangian part of the optimization function. This is associated with the variables.
     */
    private final double[][] lagrangianOptimizationCoefficients;

    /**
     * The b value in the lagrangian part of the optimization function
     */
    private final double[] lagrangianOptimizationConstants;

    /**
     * Does it have a lagrangian component?
     */
    private final boolean hasLagrangian;

    public OptimizationFunction(double[] c) {
        this(c, new double[0][c.length], new double[0], false);
    }

    public OptimizationFunction(double[] c, double[][] lagrangianOptimizationCoefficients,
                                double[] lagrangianOptimizationConstants) {
        this(c, lagrangianOptimizationCoefficients, lagrangianOptimizationConstants, true);
    }

    public OptimizationFunction(double[] c, double[][] lagrangianOptimizationCoefficients,
                                double[] lagrangianOptimizationConstants, boolean hasLagrangian) {
        this.numVariables = c.length;
        Preconditions.checkArgument(lagrangianOptimizationCoefficients.length == lagrangianOptimizationConstants.length,
                "You must have the same number of lagrangian constraints in the optimization function");

        if (lagrangianOptimizationCoefficients.length > 0) {
            Preconditions.checkArgument(lagrangianOptimizationCoefficients[0].length == this.numVariables,
                    "You must have the correct number of variables in your lagrangian optimization function");
        }

        this.c = c;
        this.lagrangianOptimizationCoefficients = lagrangianOptimizationCoefficients;
        this.lagrangianOptimizationConstants = lagrangianOptimizationConstants;
        this.hasLagrangian = hasLagrangian;
    }

    /**
     * Return the optimization function for the new lagrangian relaxed function
     */
    public OptimizationFunction relax(int constraintsToAdd, Constraints origConstraints) {
        Preconditions.checkArgument(lagrangianOptimizationCoefficients.length == 0,
                "This function only works on the original optimization function");

        // Create the new variables for the new opt function
        // Note: c will stay the same because the value only changes once we have lagrangian multipliers values
        double[][] newLCoefficients = new double[constraintsToAdd][numVariables];
        double[] newLConstants = new double[constraintsToAdd];

        // For every constraint that we need to remove
        for (int i = 0; i < constraintsToAdd; i++) {
            boolean isALessThanB = origConstraints.getAlessThanb()[i];

            // If it is Ax <= b
            // a1x1+a2x2 <= b becomes l(b + -a1x1 + -a2x2) so that l >= 0
            if (isALessThanB) {
                // b should be the value it is
                newLConstants[i] = origConstraints.getB()[i];
                for (int j = 0; j < numVariables; j++) {
                    // a_ij should be the negative of what it actually is
                    newLCoefficients[i][j] = - origConstraints.getA()[i][j];
                }
            // Otherwise
            // it is Ax >= b
            // a1x1+a2x2 <= b becomes l(-b + a1x1 + a2x2) so that l >= 0
            } else {
                // b should be the negative of the value it is
                newLConstants[i] = - origConstraints.getB()[i];
                for (int j = 0; j < numVariables; j++) {
                    // a_ij should be the value it actually is
                    newLCoefficients[i][j] = - origConstraints.getA()[i][j];
                }
            }
        }

        return new OptimizationFunction(Arrays.copyOf(c, c.length), newLCoefficients, newLConstants);
    }

    public IloNumVar[] addIPToCplex(IloCplex cplex) throws IloException {
        // Create all the variables with an upper bound of 500
        IloNumVar[] variables = cplex.intVarArray(numVariables, 0, 500);

        return addToCplex(cplex, variables);
    }

    public IloNumVar[] addLPToCplex(IloCplex cplex) throws IloException {
        // Create all the variables with an upper bound of 500
        IloNumVar[] variables = cplex.numVarArray(numVariables, 0, 500);

        return addToCplex(cplex, variables);
    }

    /**
     * Take the c vector and add it to cplex optimization
     */
    private IloNumVar[] addToCplex(IloCplex cplex, IloNumVar[] variables) throws IloException {
        Preconditions.checkArgument(!hasLagrangian,
                "This only handles data that does not have lagrangian optimization.");
        // Create the optimization function (maximize)
        IloLinearNumExpr optimizationFunction = cplex.linearNumExpr();
        // For every value of c, add it to the optimization function
        for (int i = 0; i < numVariables; i++) {
            optimizationFunction.addTerm(c[i], variables[i]);
        }

        cplex.addMaximize(optimizationFunction);

        return variables;
    }

    /**
     * Simplify the value with a lagrange multiplier to just have a C
     */
    public Pair<OptimizationFunction, Double> withLagrange(double[] lagrangeMultiplier) {
        double[] newC = Arrays.copyOf(c, c.length);
        double additionalValueToOpt = 0;

        // For each lagrangian constraint
        for (int constraint = 0; constraint < lagrangeMultiplier.length; constraint++) {
            // We already have it in the correct positive/negative situation. We now just need to add it and multiply
            // by the lagrange multiplier
            double lagrange = lagrangeMultiplier[constraint];
            // Add the b to what needs to be added to optimization function
            additionalValueToOpt = additionalValueToOpt + lagrange * lagrangianOptimizationConstants[constraint];
            for (int variable = 0; variable < lagrangianOptimizationCoefficients[0].length; variable++) {
                // Add the lagrangian coefficient times the lagrange multiplier
                newC[variable] = newC[variable] + lagrange * lagrangianOptimizationCoefficients[constraint][variable];
            }
        }

        return new Pair<>(new OptimizationFunction(newC), additionalValueToOpt);
    }

    public double[] getSubgradient(double[] variables) {
        double[] subgradient = new double[lagrangianOptimizationConstants.length];

        // For each lagrange opt. function
        for (int constraint = 0; constraint < subgradient.length; constraint++) {
            double ax = 0;
            // Calculate Ax^t
            for (int variable = 0; variable < lagrangianOptimizationCoefficients[0].length; variable++) {
                ax = ax + lagrangianOptimizationCoefficients[constraint][variable]*variables[variable];
            }
            // Calculate b - Ax^t
            subgradient[constraint] = lagrangianOptimizationConstants[constraint] - ax;
        }

        return subgradient;
    }

    /**
     * Make the output pretty so that we can actually see what the function looks like
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\nOptimization Function:\n");
        builder.append("%5s ".formatted(""));

        // Create description above
        for (int i = 0; i < c.length; i++) {
            builder.append("%5s ".formatted("x" + (i + 1)));
        }

        if (hasLagrangian) {
            builder.append("%5s ".formatted("b"));
        }

        builder.append("\n");
        builder.append("%4s) ".formatted("z"));

        // Create labels for the rows
        for (double value: c) {
            builder.append("%5s ".formatted(value));
        }

        if (hasLagrangian) {
            builder.append("\n");
            for (int i = 0; i < lagrangianOptimizationCoefficients.length; i++) {
                builder.append("%4s) ".formatted("l" + (i + 1)));

                for (int j = 0; j < lagrangianOptimizationCoefficients[0].length; j++) {
                    builder.append("%5s ".formatted(lagrangianOptimizationCoefficients[i][j]));
                }
                builder.append("%5s ".formatted(lagrangianOptimizationConstants[i]));
                builder.append("\n");
            }

        }

        return builder.toString();
    }
}
