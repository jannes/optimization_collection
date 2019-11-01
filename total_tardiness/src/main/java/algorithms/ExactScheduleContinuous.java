package algorithms;

import util.Util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExactScheduleContinuous {
    private int n;
    private double[][] jobs;
    private Map<List<Integer>, List<Integer>> setSMap;
    private Map<List<Integer>, Double> minTMap;
    // [j][0]: j's ptime, [j][1]: j's dtime, [j][2]: j's id
    private Map<List<Integer>, Integer> deltaMap;
    private int[] deltas;
    private int scale;

    /**
     * IMPORTANT: jobs must be assigned id's in edd order with id 0 for first one
     * @param instance
     */
    public ExactScheduleContinuous(double[][] instance, double scale) {
        // given the K value that the instance was scaled with
        // we choose a higher value 10^n by which we can scale time values for the keys
        this.scale = 10;
        while (this.scale < scale)
            this.scale *= 10;
        n = instance.length;
        jobs = new double[n][3];
        double[][] jobs_without_id = Util.getJobsNonDecrDDate(instance);
        for (int i = 0; i < n; i++) {
            jobs[i][0] = jobs_without_id[i][0];
            jobs[i][1] = jobs_without_id[i][1];
            jobs[i][2] = i;
        }
        setSMap = new HashMap<>();
        minTMap = new HashMap<>();
        deltaMap = new HashMap<>();
        deltas = new int[n];
    }

    public double getMinimumTardiness(int t) {
        if (n == 0)
            return 0;
//        return minT(0, n - 1, -1, t);
        return Util.getTotalTardiness(getOptimalSchedule(t));
    }

    public double[][] getOptimalSchedule(int t) {
        if (n == 0)
            return null;
        minT(0, n-1, -1, t);
        computeDeltas(0, n-1, -1, t);
        orderJobsOptimally();
        return jobs.clone();
    }

    private void computeDeltas(int i, int j, int k, double t) {
        List<Integer> setS;
        if (k == -1) {
            setS = IntStream.rangeClosed(0, n - 1).boxed()
                    .collect(Collectors.toList());
        } else {
            setS = setS(i, j, k);
        }
        if (setS.isEmpty())
            return;
        int tScaled = (int) t * scale;
        List<Integer> key = Collections.unmodifiableList(Arrays.asList(i,j,k,tScaled));
        int delta = deltaMap.get(key);
        int k2 = pick_k2(setS);
        deltas[k2] = delta;
        computeDeltas(i, k2 + delta, k2, t);
        computeDeltas(k2 + delta + 1, j, k2, completion(t, i, k2, delta));
    }

    private void orderJobsOptimally() {
        double[][] orderedByPTime = Util.getJobsNonDecrPtime(jobs);
        // from highest p_time (first k) to lowest
        for (int i = n - 1; i >= 0; i--) {
            int k = (int) Math.round(orderedByPTime[i][2]);
            int delta = deltas[k];
            int currentIndexJobK = 0;
            // find current position of job k in schedule
            for (int l = 0; l < n; l++) {
                if (jobs[l][2] == k)
                    currentIndexJobK = l;
            }
            // bubble up job k in schedule past job k + delta
            for (int j = 1; j <= delta; j++) {
                Util.switch_jobs(jobs, currentIndexJobK + j, currentIndexJobK + j - 1);
                // check if job k is positioned after k + delta now
                if (jobs[currentIndexJobK + j - 1][2] == k + delta)
                    break;
            }
        }
    }

    private double minT(int i, int j, int k, double t) {
        List<Integer> setS;
        int tScaled = (int) t * scale;
        List<Integer> key = Collections.unmodifiableList(Arrays.asList(i,j,k,tScaled));
        // check if this is initial call and construct set S from all indices if so
        if (k == -1) {
            // List[0...n-1] - initial set S including k
            setS = IntStream.rangeClosed(0, n - 1).boxed().collect(Collectors.toList());
        }
        // if not check if memoized and construct setS otherwise
        else {
            if (minTMap.containsKey(key))
                return minTMap.get(key);
            setS = setS(i, j, k);
        }

        if (setS.isEmpty()) return 0;
        if (setS.size() == 1) {
            int job = setS.get(0);
            deltaMap.put(key, 0);
            return Math.max(0, t + jobs[job][0] - jobs[job][1]);
        }

        int k2 = pick_k2(setS);
        double result = Integer.MAX_VALUE;
        int best_delta = 0;
        // delta + k up to n or j???
        for (int delta = 0; delta <= j - k2; delta++) {
            double minT_lower = minT(i, k2 + delta, k2, t);
            double k2T = Math.max(0, completion(t, i, k2, delta) - jobs[k2][1]);
            double minT_higher = minT(k2 + delta + 1, j, k2, completion(t, i, k2, delta));
            double minT = minT_lower + k2T + minT_higher;
            if (minT < result) {
                result = minT;
                best_delta = delta;
            }
            if(result == 0) break;
        }

        // memoize
        minTMap.put(key, result);
        deltaMap.put(key, best_delta);
        return result;
    }

    private List<Integer> setS(int i, int j, int k) {
        // check if memoized
        List<Integer> key = Collections.unmodifiableList(Arrays.asList(i,j,k));
        if (setSMap.containsKey(key))
            return setSMap.get(key);

        List<Integer> ind = new ArrayList<>();
        double p_k = jobs[k][0];
        for (int l = i; l <= j; l++) {
            if (jobs[l][0] < p_k ||
                    (jobs[l][0] == p_k && l < k)) {
                ind.add(l);
            }
        }

        // memoize
        setSMap.put(key, ind);
        return ind;
    }

    private int pick_k2(List<Integer> S) {
        int k2 = -1;
        double k2_ptime = -1;
        for (Integer i : S) {
            if (jobs[i][0] >= k2_ptime) {
                k2 = i;
                k2_ptime = jobs[i][0];
            }
        }
        return k2;
    }

    private double completion(double t, int i, int k2, int delta) {
        double c = t;
        // only count jobs in set S (and job k' itself)
        List<Integer> setS = setS(i, k2 + delta, k2);
        for (Integer j: setS) {
            c += jobs[j][0];
        }
        c += jobs[k2][0];
        return c;
    }
}


