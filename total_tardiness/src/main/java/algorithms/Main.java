package algorithms;

import util.InstanceReader;
import util.ProblemInstance;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please provide epsilon and filename in that order");
            System.exit(1);
        }

        String filename = args[1];
        String epsilonString = args[0];
        double epsilon = Double.valueOf(epsilonString);
        ProblemInstance instance = InstanceReader.readInstance(filename);
        if (instance == null) {
            System.out.println("could not read instance file");
            System.exit(1);
        }
        ExactSchedule exact = new ExactSchedule(instance);
        int optimum = exact.getMinimumTardiness(0);
        FPTAS fptas = new FPTAS(instance);
        int approx = fptas.getApproxMinTard(epsilon);
        System.out.println(String.format("%d %d",
                optimum, approx));
    }
}
