package algorithms;

import util.ProblemInstance;

import static util.Util.getJobsNonDecrDDate;
import static util.Util.getTotalTardiness;

public class FPTAS {

    private int[][] unscaledJobs;
    private int n;

    public FPTAS(ProblemInstance instance) {
        unscaledJobs = getJobsNonDecrDDate(instance.getJobs());
        n = unscaledJobs.length;
    }

    public double getK(double epsilon) {
        int tMax = getMaxTardiness(unscaledJobs);
        if (tMax == 0)
            return 0;
        return getK(epsilon, tMax, n);
    }

    public int getApproxMinTard(double epsilon) {
        int tMax = getMaxTardiness(unscaledJobs);
        if (tMax == 0)
            return 0;
        double K = getK(epsilon, tMax, n);
        double[][] scaledJobs = getScaledJobs(unscaledJobs, K);
        // get schedule using exact algo
        ExactScheduleContinuous exact = new ExactScheduleContinuous(scaledJobs, K);
        double[][] scaledJobsOptimalOrder = exact.getOptimalSchedule(0);
        // schedule index: position, value: id of job at position
        int[] schedule = new int[n];
        for (int i = 0; i < n; i++) {
           schedule[i] = (int) Math.round(scaledJobsOptimalOrder[i][2]);
        }
        // order the unscaled jobs according to schedule
        int[][] reorderedJobs = new int[n][];
        for (int i = 0; i < n; i++) {
            reorderedJobs[i] = unscaledJobs[schedule[i]];
        }
        int approxMinTard = getTotalTardiness(reorderedJobs);
        return approxMinTard;
    }

    public static double[][] getScaledJobs(int[][] jobs, double K) {
        double[][] scaledJobs = new double[jobs.length][2];
        for (int i = 0; i < jobs.length; i++) {
            // round down
            int p_div_floor = (int) (jobs[i][0] / K);
            scaledJobs[i][0] = p_div_floor;
            // get precise result of division
            scaledJobs[i][1] = (double) jobs[i][1] / K;
        }
        return scaledJobs;
    }

    public static int getMaxTardiness(int[][] jobs) {
        int completion = 0;
        int tardiness = 0;
        for (int i = 0; i < jobs.length; i++) {
            completion += jobs[i][0];
            tardiness = Math.max(tardiness, completion - jobs[i][1]);
        }
        return tardiness;
    }

    public static double getK(double e, int tMax, int n) {
        return (double) (2 * e * tMax) / (double) (n * (n + 1));
    }

}
