import data.Arc;
import data.FlowRequirement;
import data.Graph;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Subproblem {

    public IloCplex model;
    private List<IloNumVar> fVars;
    private Map<Arc, IloNumVar> fVarMap;
    public final double[] cj;
    private int numVars;
    private int numConstraints;
    private FlowRequirement flowReq;
    private Graph graph;
    private IloObjective objective;

    public Subproblem(FlowRequirement flowReq, Graph graph, double[] cost) throws IloException {
        model = new IloCplex();
        numVars = graph.numArcs();
        numConstraints = graph.getNodes().size();
        this.flowReq = flowReq;
        this.graph = graph;
        cj = cost;
        initVarsConstraints();
    }

    public void solve(Double[] u) throws IloException {
        // compute uTAj-cjT
        // Aj is I so this simplifies to uT-cjT
        Double[] costVector = new Double[numVars];
        for (int i = 0; i < numVars; i++) {
            costVector[i] = u[i] - cj[i];
        }
        var objExp = model.constant(0.0);
        for (int i = 0; i < numVars; i++) {
            objExp = model.sum(objExp, model.prod(costVector[i], fVars.get(i)));
        }
        if (objective != null) {
            System.out.println("old obj: " + model.getObjective().toString());
            objective.clearExpr();
            objective.setExpr(objExp);
            System.out.println("new obj: " + model.getObjective().toString());
        }
        else {
            objective = model.addMaximize(objExp);
            System.out.println("new obj: " + model.getObjective().toString());
        }
        model.solve();
    }

    public Double[] getSolutionVector() throws IloException {
        var result = new Double[numVars];
        var arcs = graph.getArcs();
        for (int i = 0; i < numVars; i++) {
            result[i] = (model.getValue(fVars.get(i)));
        }
        return result;
    }

    public double getSolutionValue() throws IloException {
        return model.getObjValue();
    }

    public void initVarsConstraints() throws IloException {
        var arcs = graph.getArcs();
        var nodes = graph.getNodes();
        fVars = new ArrayList<>(numVars);
        fVarMap = new HashMap<>(numVars);
        // variables
        for (var arc : arcs) {
            var numVar = model.numVar(0, Double.POSITIVE_INFINITY, String.format("f_%d,%d", arc.from, arc.to));
            fVarMap.put(arc, numVar);
            fVars.add(numVar);
        }
        // constraints
        for (var node : nodes) {
            var outArcs = graph.getOutArcs(node);
            var inArcs = graph.getInArcs(node);
            // construct first sum of outgoing arc flows
            var outArcsSum = model.constant(0.0);
            for (var out : outArcs) {
                outArcsSum = model.sum(outArcsSum, fVarMap.get(out));
            }
            // construct second sum of incoming arc flows
            var inArcsSum = model.constant(0.0);
            for (var in : inArcs) {
                inArcsSum = model.sum(inArcsSum, fVarMap.get(in));
            }
            var leftHandSide = model.diff(outArcsSum, inArcsSum);
            var rightHandSide = 0;
            if (node == flowReq.from) {
                rightHandSide = flowReq.requiredFlow;
            } else if (node == flowReq.to) {
                rightHandSide = -flowReq.requiredFlow;
            }
            model.addEq(leftHandSide, rightHandSide);
        }
    }
}
