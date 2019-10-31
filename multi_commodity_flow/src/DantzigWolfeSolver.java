import data.Instance;
import ilog.concert.IloException;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DantzigWolfeSolver {

    private RestrictedMasterProblem rmp;
    private List<Subproblem> subproblems;
    private PrintStream log;
    private PrintStream rmpValuesLog;
    private int numCommonConstraints;
    private int numActivities;
    private Instance instance;
    private List<double[]> solution;

    public DantzigWolfeSolver(Instance instance, PrintStream out) throws IloException, IOException {
        rmp = new RestrictedMasterProblem(instance);
        numCommonConstraints = rmp.numCommonConstraints;
        numActivities = rmp.numActivities;
        this.instance = instance;
        subproblems = new ArrayList<>(numActivities);
        log = out;
        rmpValuesLog = new PrintStream(Files.createFile(Path.of("log2.txt")).toFile());
        initializeSubproblems(instance);
    }

    /**
     * Initialize subproblems with the fixed cost vector and fixed constraints
     * only the u vector is variable, and all fixed costs are the same for subproblems
     * @param instance
     * @throws IloException
     */
    private void initializeSubproblems(Instance instance) throws IloException {
        var flowReqs = instance.flowRequirements;
        var arcs = instance.graph.getArcs();
        var numArcs = numCommonConstraints;
        var cj = new double[numArcs];
        for (int i = 0; i < numArcs; i++) {
            cj[i] = arcs.get(i).cost;
        }
        for (var fr: flowReqs) {
            subproblems.add(new Subproblem(fr, instance.graph, cj));
        }
    }

    public void solve() throws IloException {
        // keep track of extreme points and corresponding rmp beta var names
        List<List<Double[]>> subproblemExtremePoints = new ArrayList<>(numActivities);
        for (int i = 0; i < numActivities; i++) {
            subproblemExtremePoints.add(new ArrayList<>());
        }
        int count = 0;
        while (true) {
            log.println("------ITERATION " + count + " --------");
            // solve rmp
//            rmp.model.exportModel(String.format("rmp%d.lp", count));
            rmp.solve();
            var solutionValue = rmp.getSolutionValue();
            log.println("--solution value rmp: " + solutionValue);
            rmpValuesLog.println(solutionValue);
            // get duals and construct u
            var duals = rmp.getDuals();
            log.println("--duals: " + Arrays.toString(duals));
            var u = new Double[numCommonConstraints];
            for (int i = 0; i < numCommonConstraints; i++) {
                u[i] = duals[i];
            }
            log.println("--dual u: " + Arrays.toString(u));
            var addedNewColumn = false;
            for (int j = 0; j < numActivities; j++) {
                var vj = duals[numCommonConstraints + j];
                log.println("--v: " + vj);
                var subproblem = subproblems.get(j);
                // solve subproblem with new u
                subproblem.solve(u);
//                subproblem.model.exportModel(String.format("sub%d_it%d.lp", j, count));
                var z = subproblem.getSolutionValue();
                // if solution value is negative, add new column
                if (-vj - z < 0) {
                    log.println("--found new var with negative reduced cost for subproblem: " + j);
                    log.println("--reduced cost: " + (-vj - z));
                    // calculuate cost and column coefficients
                    var f_hat = subproblem.getSolutionVector();
                    log.println("--corresponding extreme point: " + Arrays.toString(f_hat));
                    var c_j = subproblem.cj;
                    var cost = 0.0;
                    // since Aj = I, column is [fhat 0 ... 0 1 0 ... 0]
                    var column = new double[numCommonConstraints + numActivities];
                    for (int i = 0; i < numCommonConstraints; i++) {
                        cost += (f_hat[i] * c_j[i]);
                        column[i] = f_hat[i];
                    }
                    column[numCommonConstraints + j] = 1.0;
                    var jExtremePoints = subproblemExtremePoints.get(j);
                    var betaIndex = jExtremePoints.size();
                    // track new extreme point
                    jExtremePoints.add(f_hat);
                    // add column with new beta var
                    rmp.addColumn(new RestrictedMasterProblem.Beta(j, betaIndex), cost, column);
                    addedNewColumn = true;
                    break;
                }
            }
            if (!addedNewColumn) {
                break;
            }
            count++;
        }
        // for each activity construct vector F^k
        List<double[]> fVectors = new ArrayList<>(numActivities);
        for (int j = 0; j < numActivities; j++) {
            var fK = new double[numCommonConstraints];
            var jExtremePoints = subproblemExtremePoints.get(j);
            for (int x = 0; x < jExtremePoints.size(); x++) {
                var xPoint = jExtremePoints.get(x);
                var betaVal = rmp.getBetaVarValue(new RestrictedMasterProblem.Beta(j, x));
                for (int i = 0; i < numCommonConstraints; i++) {
                    fK[i] += betaVal * xPoint[i];
                }
            }
            fVectors.add(fK);
        }
        solution = fVectors;
    }


    public List<double[]> getSolution() {
        return solution;
    }
}
