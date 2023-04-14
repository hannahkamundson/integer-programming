package com.digit.app;

import com.digit.app.data.Data;

import java.util.Random;

public class DataGenerator {
    private static final double DENSITY_A = 0.4;
    private static final int MIN_A = -10;
    private static final int MAX_A = 30;
    private static final int MIN_B = 0;
    private static final int MAX_B = 10;
    private static final int MIN_C = -5;
    private static final int MAX_C = 10;
    private static final double PROB_A_LESS_THAN_B = 0.7;

    public static Data create(int numConstraints, int numVariables) {
        int[][] A = new int[numConstraints][numVariables];
        int[] b = new int[numConstraints];
        double[] c = new double[numVariables];
        boolean[] AlessThanb = new boolean[numConstraints];
        
        Random randomGenerator = new Random();
//        int seed = randomGenerator.nextInt();
//        int seed = 1492842449;
        int seed = -1549335653;
        System.out.printf("Seed: %s\n", seed);
        randomGenerator.setSeed(seed);

        // Generate the cost variable
        for (int i = 0; i < numConstraints; i++) {
            // Generate the constraint b side
            b[i] = randomGenerator.nextInt((DataGenerator.MAX_B - DataGenerator.MIN_B) + 1) + DataGenerator.MIN_B;

            // If we generate a number less than above, set the variable to true. Otherwise, keep it to false because it should be greater than
            if (randomGenerator.nextDouble() < PROB_A_LESS_THAN_B) {
                AlessThanb[i] = true;
            }

            for (int j = 0; j < numVariables; j++) {
                // On the first round, generate c as well
                if (i == 0) {
                    c[j] = randomGenerator.nextInt((DataGenerator.MAX_C - DataGenerator.MIN_C) + 1) + DataGenerator.MIN_C;
                }

                // Create a matrix with density equivalent to what was selected within the given range
                if (randomGenerator.nextDouble() < DENSITY_A) {
                    A[i][j] = randomGenerator.nextInt((DataGenerator.MAX_A - DataGenerator.MIN_A) + 1) + DataGenerator.MIN_A;
                }               
            }
        }

        return Data.create(A, b, AlessThanb, c);
    }
}
