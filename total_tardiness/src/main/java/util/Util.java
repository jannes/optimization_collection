package util;

import java.util.Arrays;
import java.util.Comparator;

public class Util {
    public static int[][] getJobsNonDecrDDate(int[][] jobs) {
        int[][] sortedJobs = jobs.clone();
        Arrays.sort(sortedJobs, Comparator.comparingInt(a -> a[1]));
        return sortedJobs;
    }

    public static double[][] getJobsNonDecrDDate(double[][] jobs) {
        double[][] sortedJobs = jobs.clone();
        Arrays.sort(sortedJobs, Comparator.comparingDouble(a -> a[1]));
        return sortedJobs;
    }

    public static int[][] getJobsNonDecrPtime(int[][] jobs) {
        int[][] sortedJobs = jobs.clone();
        Arrays.sort(sortedJobs, Comparator.comparingInt(a -> a[0]));
        return sortedJobs;
    }

    public static double[][] getJobsNonDecrPtime(double[][] jobs) {
        double[][] sortedJobs = jobs.clone();
        Arrays.sort(sortedJobs, Comparator.comparingDouble(a -> a[0]));
        return sortedJobs;
    }

    public static int getTotalTardiness(int[][] jobs) {
        int completion = 0;
        int tardiness = 0;
        for (int i = 0; i < jobs.length; i++) {
            completion += jobs[i][0];
            tardiness += Math.max(0, completion - jobs[i][1]);
        }
        return tardiness;
    }

    public static int getTotalTardiness(double[][] jobs) {
        double completion = 0;
        int tardiness = 0;
        for (int i = 0; i < jobs.length; i++) {
            completion += jobs[i][0];
            tardiness += Math.max(0, completion - jobs[i][1]);
        }
        return tardiness;
    }

    public static void switch_jobs(int[][] jobs, int i, int j) {
        int[] tmp = jobs[i];
        jobs[i] = jobs[j];
        jobs[j] = tmp;
    }

    public static void switch_jobs(double[][] jobs, int i, int j) {
        double[] tmp = jobs[i];
        jobs[i] = jobs[j];
        jobs[j] = tmp;
    }
}
