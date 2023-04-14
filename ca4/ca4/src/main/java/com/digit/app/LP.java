package com.digit.app;

import com.digit.app.data.Data;
import com.google.common.base.Preconditions;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.io.OutputStream;
import java.io.PrintStream;

public class LP implements AutoCloseable {
    private final IloCplex cplex;
    private final IloNumVar[] variables;

    private final IloRange[] constraints;

    /**
     * Get the integer program for the lagrange and the amount that needs to be added to the final result
     */
    public static Pair<LP, Double> createFromLagrange(Data data, double[] lagrangeMultipliers) throws IloException {
        // Create new data based on Lagrange multipliers
        Pair<Data, Double> pair = data.withLagrange(lagrangeMultipliers);

        Data lagrange = pair.getLeft();

        System.out.println(lagrange.getOptimizationFn().toString());

        LP lagrangeIp = LP.integerProgram(lagrange);

        return new Pair<>(lagrangeIp, pair.getRight());
    }

    public static LP integerProgram(Data data) throws IloException {
        return new LP(data, true);
    }

    public static LP linearProgramRelaxation(Data data) throws IloException {
        return new LP(data, false);
    }

    private LP(Data data, boolean integerProgramming) throws IloException {
        Preconditions.checkArgument(!data.getOptimizationFn().isHasLagrangian(),
                "This only handles data that does not have lagrangian optimization. If you want to use that, use" +
                        "the create function");
        // Get CPLEX configured the way we want
        cplex = configureCplex();

        // Add optimization to cplex and get the variables
        if (integerProgramming) {
            variables = data.getOptimizationFn().addIPToCplex(cplex);
        } else {
            variables = data.getOptimizationFn().addLPToCplex(cplex);
        }


        // Add the constraints
        constraints = data.getConstraints().addToCplex(cplex, variables);
    }

    private IloCplex configureCplex() throws IloException {
        IloCplex cplex = new IloCplex();
        // Have it not print stuff out because it is very verbose
        cplex.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {
                //DO NOTHING
            }
        }));

        return cplex;
    }

    /**
     * Get the generic IP solution to the problem.
     */
    public IPSolution getGenericSolution() throws IloException {
        return getLDSolution(0);
    }

    /**
     * Get the LD solution which may include adding a value to the optimization to account for the constant in the
     * Lagrange
     */
    public IPSolution getLDSolution(double addToFinal) throws IloException {
        if (!cplex.solve()) {
            return IPSolution.infeasible();
        }

        // Get the optimal values and slacks
        double[] values = cplex.getValues(variables);
        double optimal = cplex.getObjValue();
        double[] slack = cplex.getSlacks(constraints);

        // The optimal value is really the additional value that needs to be added at the end
        return new IPSolution(optimal + addToFinal, values, slack);
    }

    @Override
    public void close() throws Exception {
        cplex.close();
    }
}
