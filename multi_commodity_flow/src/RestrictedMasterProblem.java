import data.Arc;
import data.Instance;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;

public class RestrictedMasterProblem {

    public IloCplex model;
    public final int numCommonConstraints;
    public final int numActivities;
    public final int numConstraints;
    private List<Arc> arcList;
    private List<IloNumVar> sVars;
    private List<IloNumVar> tVars;
    private Map<Beta, IloNumVar> betaVars;
    private List<IloRange> commonConstraints;
    private List<IloRange> activityConstraints;
    private List<IloRange> allConstraints;
    private IloObjective objective;
    private Instance originalInstance;

    public RestrictedMasterProblem(Instance instance) throws IloException {
        originalInstance = instance;
        model = new IloCplex();
        numCommonConstraints = instance.graph.numArcs();
        numActivities = instance.flowRequirements.size();
        numConstraints = numCommonConstraints + numActivities;
        arcList = new ArrayList<>(instance.graph.getArcs());
        sVars = new ArrayList<>(numCommonConstraints);
        tVars = new ArrayList<>(numActivities);
        betaVars = new HashMap<>();
        initVarsConstraints();
        initObjective();
    }

    public void solve() throws IloException {
        model.solve();
    }

    public double getSolutionValue() throws IloException {
        return model.getObjValue();
    }

    public double getBetaVarValue(Beta b) throws IloException {
        return model.getValue(betaVars.get(b));
    }

    public double[] getDuals() throws IloException {
        return model.getDuals(allConstraints.toArray(new IloRange[numConstraints]));
    }

    public void addColumn(Beta beta, double cost, double[] columnCoefficients) throws IloException {
        System.out.println(String.format("-rmp- adding new var beta with (activity, index): (%d, %d)",
                beta.activity, beta.superscript));
        System.out.println("-rmp- column coeffs: " + Arrays.toString(columnCoefficients));
        System.out.println("-rmp- cost coeff: " + cost);
        // create new variable and column
        IloColumn column = model.column(objective, cost);
        // fill the column with the right coefficients
        for (int i = 0; i < numConstraints; i++) {
            var row = allConstraints.get(i);
            var coefficient = model.column(row, columnCoefficients[i]);
            column = column.and(coefficient);
        }
        var newVar = model.numVar(column, 0, Double.POSITIVE_INFINITY, beta.toString());
        betaVars.put(beta, newVar);
    }

    private void initObjective() throws IloException {
        IloNumExpr objectiveTerm = model.constant(0);
        for (int i = 0; i < numActivities; i++) {
            objectiveTerm = model.sum(objectiveTerm, model.prod(1000000, tVars.get(i)));
        }
        objective = model.addMinimize(objectiveTerm);
    }

    private void initVarsConstraints() throws IloException {
        commonConstraints = new ArrayList<>(numCommonConstraints);
        activityConstraints = new ArrayList<>(numActivities);
        for (int i = 0; i < numCommonConstraints; i++) {
            sVars.add(model.numVar(0, Double.POSITIVE_INFINITY, String.format("s%d", i)));
            int capacity = arcList.get(i).capacity;
            commonConstraints.add(model.addEq(sVars.get(i), capacity, String.format("com%d", i)));
        }
        for (int i = 0; i < numActivities; i++) {
            tVars.add(model.numVar(0, Double.POSITIVE_INFINITY, String.format("t%d", i)));
            activityConstraints.add(model.addEq(tVars.get(i), 1.0, String.format("act%d", i)));
        }
        allConstraints = new ArrayList<>(commonConstraints);
        allConstraints.addAll(activityConstraints);
    }

    public static class Beta {
        final int activity;
        final int superscript;

        Beta(int activity, int superscript) {
            this.activity = activity;
            this.superscript = superscript;
        }

        @Override
        public String toString() {
            return String.format("&_k%di%d", activity, superscript);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Beta beta = (Beta) o;
            return activity == beta.activity &&
                    superscript == beta.superscript;
        }

        @Override
        public int hashCode() {
            return Objects.hash(activity, superscript);
        }
    }
}
