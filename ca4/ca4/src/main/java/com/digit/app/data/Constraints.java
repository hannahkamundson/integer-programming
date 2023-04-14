package com.digit.app.data;

import com.google.common.base.Preconditions;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import lombok.Value;

@Value
public class Constraints {
    /**
     * The number of constraints in this program
     */
    private final int numConstraints;

    /**
     * The constraint matrix
     */
    private final int[][] A;

    /**
     * The constraint RHS
     */
    private final int[] b;

    /**
     * Whether a constraint is Ax <= b or the opposite
     */
    private final boolean[] AlessThanb;

    public Constraints(int[][] A, int[] b, boolean[] AlessThanB) {
        this.numConstraints = b.length;

        Preconditions.checkArgument(A.length == this.numConstraints,
                "A rows needs to be the same size as b but they differ. A: %s b: %s".formatted(A.length, this.numConstraints));
        Preconditions.checkArgument(AlessThanB.length == this.numConstraints,
                "A rows needs to be the same size as b but they differ. A: %s b: %s".formatted(A.length, this.numConstraints));

        this.A = A;
        this.b = b;
        this.AlessThanb = AlessThanB;
    }

    public void assertCorrectNumberOfVariables(int numVariables) {
        if (A.length > 0) {
            Preconditions.checkArgument(A[0].length == numVariables,
                    "A columns needs to be the same size as c but they differ. A: %s b: %s".formatted(A[0].length, numVariables));
        }
    }

    /**
     * Get the density of the matrix A. ie, the number nonzero entries over the total number of entries
     */
    public double getDensityOfMatrix() {
        int total = 0;
        int nonZero = 0;
        for (int[] row: A) {
            for (int val: row) {
                total++;

                if (val != 0) {
                    nonZero++;
                }
            }
        }

        return ((double) nonZero)/ total;
    }

    /**
     * Return the constraints for the new lagrangian relaxed version. This will remove the original constraints
     */
    public Constraints relax(int constraintsToRemove) {
        Preconditions.checkArgument(constraintsToRemove <= numConstraints,
                "You can only %s constraints".formatted(numConstraints));
        int newConstraints = numConstraints - constraintsToRemove;
        int numVariables = A[0].length;

        // Create the new values for the new constraint
        int[][] newA = new int[newConstraints][numVariables];
        int[] newB = new int[newConstraints];
        boolean[] newALessThanB = new boolean[newConstraints];

        for (int origConstraint = constraintsToRemove; origConstraint < numConstraints; origConstraint++) {
            int newConstraintNum = origConstraint - constraintsToRemove;
            // Add the old b to the new b
            newB[newConstraintNum] = b[origConstraint];
            // Add the old constraint expression to the new one
            newA[newConstraintNum] = A[origConstraint];
            // Get the correct less than/equal to
            newALessThanB[newConstraintNum] = AlessThanb[origConstraint];
        }

        return new Constraints(newA, newB, newALessThanB);
    }

    /**
     * Add the constraints to CPLEX
     */
    public IloRange[] addToCplex(IloCplex cplex, IloNumVar[] variables) throws IloException {
        // Create the number of constraints
        IloRange[] constraints = new IloRange[numConstraints];

        // For each constraint
        for (int i = 0; i < numConstraints; i++) {
            // Create the constraint by looping through each variable per constraint
            IloLinearNumExpr constraint = cplex.linearNumExpr();
            for (int j = 0; j < variables.length; j++) {
                constraint.addTerm(A[i][j], variables[j]);
            }
            // If it is Ax <= b
            if (AlessThanb[i]) {
                constraints[i] = cplex.addLe(constraint, b[i]);
            // Otherwise it is Ax >= b
            } else {
                constraints[i] = cplex.addGe(constraint, b[i]);
            }
        }

        // Return the constraints we made so we can keep track of it
        return constraints;
    }

    /**
     * Make the output pretty so that we can actually see what the function looks like
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\n\nConstraints:\n");

        if (A.length == 0) {
            builder.append("No constraints");
            return builder.toString();
        }

        builder.append("%5s ".formatted(""));

        // Create description above
        for (int i = 0; i < A[0].length; i++) {
            builder.append("%5s ".formatted("x" + (i + 1)));
        }
        builder.append("\n");

        // Create labels for the rows
        for (int i = 0; i < numConstraints; i++) {
            builder.append("%4s) ".formatted("c" + (i + 1)));
            int[] row = A[i];
            for (int value: row) {
                builder.append("%5s ".formatted(value));
            }

            if (AlessThanb[i]) {
                builder.append("%5s ".formatted("<="));
            } else {
                builder.append("%5s ".formatted(">="));
            }
            builder.append("%5s ".formatted(b[i]));
            builder.append("\n");
        }

        return builder.toString();
    }
}
