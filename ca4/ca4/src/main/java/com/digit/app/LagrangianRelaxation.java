package com.digit.app;

import com.google.common.base.Preconditions;

import java.util.Arrays;

public class LagrangianRelaxation {

    public static double stepValue(double e, double[] subgradient, double optimalValueFromIteration, double lowestOriginalUB) {
        Preconditions.checkArgument(e <= 2, "Epsilon has to be less than 2");
        Preconditions.checkArgument(e > 0, "Epsilon has to be positive");
        double euclideanNorm = euclideanNorm(subgradient);
        double denominator = euclideanNorm * euclideanNorm;;
        double numerator = optimalValueFromIteration - lowestOriginalUB;
        double value = e*numerator/denominator;

        System.out.printf("Calculating: %f * (%f - %f)/(%f^2) = %f", e, optimalValueFromIteration, lowestOriginalUB, euclideanNorm, value);

        return value;
    }

    public static double violationSquared(double[] subgradient) {
        double euclideanNorm = euclideanNorm(subgradient);
        return euclideanNorm * euclideanNorm;
    }

    public static double originalIPOptimalValue(double[] originalC, double[] variables) {
        Preconditions.checkArgument(originalC.length == variables.length, "The lengths of C and variables need to be the same");
        double optimalValue = 0;
        for (int i = 0; i < originalC.length; i++) {
            optimalValue = optimalValue + originalC[i] * variables[i];
        }

        return optimalValue;
    }

    public static double[] newLagrangianMultipliers(double[] origU, double step, double[] subgradient) {
        Preconditions.checkArgument(origU.length == subgradient.length, "The lengths of subgradient and u need to be the same");
        double[] newU = Arrays.copyOf(origU, origU.length);
        for (int i = 0; i < origU.length; i++) {
            double newVal = origU[i] + step * subgradient[i];

            newU[i] = Math.max(newVal, 0);
        }

        return newU;
    }

    private static double euclideanNorm(double[] vector) {
        double value = 0;

        for (double num: vector) {
            value = value + num * num;
        }

        return Math.sqrt(value);
    }
}
