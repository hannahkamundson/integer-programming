package com.digit.app;

import com.digit.app.data.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class App {
    private static final List<Table1> table1 = new ArrayList<>();
    private static final List<Table2> table2 = new ArrayList<>();
    private static final int ITERATIONS_UNTIL_DECREASE = 4;
    private static final double FRACTION_OF_E_IF_NO_DECREASE = 0.5;

    private static final double BREAK_IF_E_LESS_THAN = 0.0005;

    private static final int TOTAL_ROUNDS_TO_TRY = 90000;

    public static void main(String[] args) throws Exception {
        // Create the integer program and print out the info
        Data origData = DataGenerator.create(10, 15);

        LP integerProgram = LP.integerProgram(origData);
        IPSolution ipSol = integerProgram.getGenericSolution();
        while (!ipSol.isFeasible()) {
            origData = DataGenerator.create(10, 15);
            integerProgram = LP.integerProgram(origData);
            ipSol = integerProgram.getGenericSolution();
        }

        integerProgram.close();
        printSubject("Optimal Solution");
        System.out.println(ipSol);
        printSubject("Optimization Problem");
        System.out.println(origData);
        System.out.printf("Density of matrix A: %s", origData.getConstraints().getDensityOfMatrix());

        // For a round
        for (int round = 0; round < 10; round++) {
            App.runRound(round + 1, origData);
        }

        System.out.printf("\nActual optimal: %f\n", ipSol.getOptimizationValue());
        System.out.println("Table 1");
        System.out.println(Table1.title());
        for (Table1 table: table1) {
            System.out.println(table);
        }

        System.out.println("Table 2");
        System.out.println(Table2.prettyPrintTable2(table2));
    }

    private static void runRound(int round, Data origData) throws Exception {
        printSubject("Lagrangian Relaxation round: %s".formatted(round));
        Table2 valueTable2 = new Table2();
        valueTable2.setRound(round);

        // Make epsilon 1.5 because that is what the research says is a good way to start
        double e = 1.5;

        // Choose a first upper bound and lagrangian multipliers
        // The first upper bound will be the linear relaxation of the original problem
        double upperBound;
        try (LP lp = LP.linearProgramRelaxation(origData)) {
            upperBound = lp.getGenericSolution().getOptimizationValue();
        }
        double[] lagrangianMultipliers = new double[round];
        Arrays.fill(lagrangianMultipliers, 1);

        int iterationsSinceImprovement = 0;

        // Get the L(u) function
        Data lagrangianData = origData.relax(round);
        System.out.println(lagrangianData);

        double lastOptimalZDual = Integer.MAX_VALUE;

        for (int iteration = 0; iteration <= TOTAL_ROUNDS_TO_TRY; iteration++) {
            iterationsSinceImprovement++;
            printSubject("Round %s Iteration: %s".formatted(round, iteration + 1));
            System.out.printf("Upper Bound: %f\n", upperBound);
            System.out.printf("Lagrangian multipliers: %s\n", doubleArrayToString(lagrangianMultipliers));

            // Run the integer program for L(lagrangianMultipliers) for the round
            // Get the new optimal value and the value of subgradient b-Ax
            // For the new optimal value, take the lagrangian data and turn it into an IP
            // Then, run it and get the solution
            Pair<LP, Double> pair = LP.createFromLagrange(lagrangianData, lagrangianMultipliers);
            LP lip = pair.getLeft();
            Double amountToAdd = pair.getRight();
            // The value for this one is listed here
            IPSolution solution = lip.getLDSolution(amountToAdd);
            lip.close();
            printSubject("LR Solution");
            System.out.println(solution);

            // Now get the subgradient
            double[] subgradient = lagrangianData.getOptimizationFn().getSubgradient(solution.getVariables());

            boolean allZero = true;

            for (double value: subgradient) {
                if (value != 0) {
                    allZero = false;
                    break;
                }
            }

            // If the subgradient is 0, exit out because we are done
            if (allZero) {
                valueTable2.setFinalIteration(iteration);
                System.out.println("The subgradient was 0");
                break;
            }

            // Update the lowest upper bound of the original problem if needed
            double valueForOrigProblem = LagrangianRelaxation.originalIPOptimalValue(origData.getOptimizationFn().getC(), solution.getVariables());
            System.out.printf("Value for original problem: %f", valueForOrigProblem);
            if (valueForOrigProblem < upperBound) {
                System.out.printf("\nUpdating the upper bound from %f to %f\n", upperBound, valueForOrigProblem);
                upperBound = valueForOrigProblem;
            }

            // Calculate the step function
            // Get the value of the original problem with the given x values
            printSubject("Step function calculation");
            double stepValue = LagrangianRelaxation.stepValue(e, subgradient, solution.getOptimizationValue(), upperBound);

            // Store table 1 data for round 5
            if (round == 5) {
                Table1 valueTab1 = new Table1(round, iteration, lagrangianMultipliers, solution.getOptimizationValue(), stepValue, e, LagrangianRelaxation.violationSquared(subgradient), valueForOrigProblem);
                table1.add(valueTab1);
            }

            // If we improved the value, set the iterations for improvement to 0
            // Also, update table 2 because that means this is the first time we are seeing the new data
            // Also do it if it is iteration 0 since we haven't yet added anything to table 2
            if (iteration == 0 || lastOptimalZDual > solution.getOptimizationValue()) {
                iterationsSinceImprovement = 0;
                valueTable2.setBestSolutionFoundIteration(iteration);
                valueTable2.setLagrangianOptimal(solution.getOptimizationValue());
                valueTable2.setLagrangeMultiplier(lagrangianMultipliers);
                valueTable2.setOptimalX(solution.getVariables());
                valueTable2.setSubgradient(subgradient);
            }

            // Update the new lagrangian multipliers based on the step function
            // u^{t+1} = max {0, u + step*subgradient)
            lagrangianMultipliers = LagrangianRelaxation.newLagrangianMultipliers(lagrangianMultipliers, stepValue, subgradient);

            // If we haven't improved in T iterations, half e
            if (iterationsSinceImprovement >= ITERATIONS_UNTIL_DECREASE) {
                iterationsSinceImprovement = 0;
                e = e * FRACTION_OF_E_IF_NO_DECREASE;
            }

            lastOptimalZDual = solution.getOptimizationValue();

            if (e <= BREAK_IF_E_LESS_THAN) {
                valueTable2.setFinalIteration(iteration);
                System.out.printf("Stopping because e is %f which is less than %f\n", e, BREAK_IF_E_LESS_THAN);
                break;
            }
        }

        // If we exited because of number of rounds, set that
        if (valueTable2.getFinalIteration() == 0) {
            valueTable2.setFinalIteration(TOTAL_ROUNDS_TO_TRY);
        }

        table2.add(valueTable2);

        System.out.printf("Ending on optimal integer value %f", upperBound);
    }

    private static String doubleArrayToString(double[] toPrint) {
        StringBuilder builder = new StringBuilder();
        for (double value: toPrint) {
            builder.append("%.5f ".formatted(value));
        }

        return builder.toString();
    }

    private static void printSubject(String topic) {
        System.out.printf("\n\n------------ %s ------------ %n", topic);
    }
}
