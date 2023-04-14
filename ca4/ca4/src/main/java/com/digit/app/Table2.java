package com.digit.app;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class Table2 {
    private int round;
    private int bestSolutionFoundIteration;
    private int finalIteration;
    private double lagrangianOptimal;
    private double[] lagrangeMultiplier;
    private double[] optimalX;
    private double[] subgradient;

    private static String doubleArrayToString(double[] toPrint) {
        StringBuilder builder = new StringBuilder();
        for (double value: toPrint) {
            builder.append("%f,".formatted(value));
        }

        return builder.toString();
    }

    public static String prettyPrintTable2(List<Table2> list) {
        StringBuilder builder = new StringBuilder();

        builder.append("|variable|");

        for (Table2 value : list) {
            builder.append("%s|".formatted(value.round));
        }

        builder.append("\n| --- |");
        for (Table2 value : list) {
            builder.append(" --- |");
        }

        builder.append("\n|k_1|");

        for (Table2 table2 : list) {
            builder.append("%s|".formatted(table2.bestSolutionFoundIteration));
        }

        builder.append("\n|k_2|");

        for (Table2 table2 : list) {
            builder.append("%s|".formatted(table2.finalIteration));
        }

        builder.append("\n|Z_LR|");

        for (Table2 table2 : list) {
            builder.append("%s|".formatted(table2.lagrangianOptimal));
        }

        builder.append("\n|L|");

        for (Table2 table2 : list) {
            builder.append("%s|".formatted(Table2.doubleArrayToString(table2.lagrangeMultiplier)));
        }

        builder.append("\n|x|");

        for (Table2 table2 : list) {
            builder.append("%s|".formatted(Table2.doubleArrayToString(table2.optimalX)));
        }

        builder.append("\n|Subgradient|");

        for (Table2 table2 : list) {
            builder.append("%s|".formatted(Table2.doubleArrayToString(table2.subgradient)));
        }

        return builder.toString();
    }
}
