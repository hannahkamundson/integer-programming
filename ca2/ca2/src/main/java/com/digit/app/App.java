package com.digit.app;


import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.List;

public class App {
    public static void main(String[] args) throws ArgumentParserException {
        // Create an arg parser for desired size and density
        ArgumentParser parser = ArgumentParsers.newFor("ca2").build()
                .defaultHelp(true)
                .description("Create a connected, undirected graph with minimum degree 2");

        parser.addArgument("--size")
                .type(Integer.class)
                .required(true)
                .help("The number of nodes you want");
        parser.addArgument("--density")
                .type(Double.class)
                .required(true)
                .help("The minimum edge density required");

        Namespace ns = parser.parseArgs(args);

        // Generate the graph with the parameters passed in from the command line
        Graph generatedGraph = GraphGenerator.create(ns.getInt("size"), ns.getDouble("density"));

        // Write the graph out
        System.out.print(generatedGraph);

        // Find the cycles
        List<List<Integer>> cycles = CycleDetection.findCycles(generatedGraph);

        System.out.print(CycleDetection.toPrettyString(cycles));
    }
}
