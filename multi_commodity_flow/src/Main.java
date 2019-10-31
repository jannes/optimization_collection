import ilog.concert.IloException;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static final String smallInstancePath = "example_inputs/MCFInstanceSmall.txt";
    public static final String largeInstancePath = "example_inputs/MCFInstanceLarge.txt";
    public static void main(String[] args) {
        try {
            var smallInstance = InstanceReader.parseInstance(Path.of(largeInstancePath));
            System.out.println(smallInstance.numCommodities);
            var log = System.out;
            var solver = new DantzigWolfeSolver(smallInstance, log);
            solver.solve();
//            var sol = solver.getSolution();
//            for (int i = 0; i < 3; i++) {
//                System.out.println("solution vector for activity " + i);
//                System.out.println(Arrays.toString(sol.get(i)));
//            }
        } catch (IOException | IloException e) {
            e.printStackTrace();
        }
    }
}
