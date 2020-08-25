package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.johannes.dyck.support.riskanalyzer.RiskAnalyzer;

public class RiskAnalysisAlgorithmically
{
    /**
     * Leave until I fixed the junit 5 / module / module-path / maven / eclipse issue (whatever goes wrong here, no idea yet)
     * TODO JUNITECLIPSE
     * @param args
     */
    public static void main(String[] args)
    {
//        testGivenRisk();
//        testGivenTests();
//        testGivenRiskSmallExample();
        testCompareGivenRisk();
    }

    private static void testCompareGivenRisk()
    {
        double R = Math.pow(10.0, -4.0);
        List<Double> probabilities;
        
        MersenneTwister randomGen = new MersenneTwister();
        List<Double> randomNums = randomGen.doubles(20).boxed().collect(Collectors.toList());
        double sum = randomNums.stream().mapToDouble(Double::doubleValue).sum();
        probabilities = randomNums.stream().map(num -> num / sum).collect(Collectors.toList());
        double one = probabilities.stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("Is this one?" + one);
        
        long beforeRegular = System.currentTimeMillis();
        List<Integer> t_i_regular = compute_t_iGivenRStreamBased(probabilities, R);
        long afterRegular = System.currentTimeMillis();
        long beforeParallel = System.currentTimeMillis();
        List<Integer> t_i_parallel = compute_t_iGivenR(probabilities, R);
        long afterParallel = System.currentTimeMillis();
        
        System.out.format("regular took: %d seconds, parallel took: %d\n", afterRegular - beforeRegular, afterParallel - beforeParallel);
        boolean equal_t_is = t_i_regular.equals(t_i_parallel);
        System.out.format("Are the t_i the same? %s\n", equal_t_is ? "yep" : "nope");
    }

    private static void testGivenTests()
    {
        double T = Math.pow(10, 6);
        List<Double> probabilities;
        
        MersenneTwister randomGen = new MersenneTwister(1000);
        List<Double> randomNums = randomGen.doubles(400).boxed().collect(Collectors.toList());
        double sum = randomNums.stream().mapToDouble(Double::doubleValue).sum();
        probabilities = randomNums.stream().map(num -> num / sum).collect(Collectors.toList());
        Double[] jP = new Double[probabilities.size()];
        IntStream.range(0, probabilities.size()).forEach(idx -> jP[idx] = probabilities.get(idx));
        
        
        RiskAnalyzer.pArray = jP;
        List<Double> t_johannesReal = RiskAnalyzer.findOptimalTsForTestSum(probabilities.stream(), probabilities.stream(), T).collect(Collectors.toList());
        Double[] tArray = (Double[]) t_johannesReal.stream().toArray(Double[]::new);
        List<Double> t_johannes = Stream.of(RiskAnalyzer.roundUpDown(tArray, true)).collect(Collectors.toList());
        System.out.format("Risk of johannes computation:\n%.30f\n", RiskAnalysis.computeR(t_johannes, probabilities));

        
        List<Double> t_real = RiskAnalysis.compute_t_iGivenT(probabilities, T);
        System.out.format("Risk of real valued computation:\n%.30f\n", RiskAnalysis.computeR(t_real, probabilities));
        List<Double> t_realUnrounded = RiskAnalysis.compute_t_iGivenT_real(probabilities, T);
        
        
        List<Integer> t_algo = compute_t_iGivenT(probabilities, T);
        System.out.format("%.30f\nRisk of algorithmically computed t_i\n", computeR(t_algo, probabilities));

        int sumTJohannes = computeT(t_johannes);
        int sumTReal = computeT(t_real);
        int sumTRealUnroundend = RiskAnalysis.computeT(t_realUnrounded);
        int sumTAlgo = computeTInt(t_algo);
        
        System.out.format("%d: johannes\n%d: real\n%d: real unrounded\n%d: algo", sumTJohannes, sumTReal, sumTRealUnroundend, sumTAlgo);
    }

    private static void testGivenRisk()
    {
        double R = Math.pow(10.0, -4.0);
        List<Double> probabilities;
        
        MersenneTwister randomGen = new MersenneTwister();
        List<Double> randomNums = randomGen.doubles(200).boxed().collect(Collectors.toList());
        double sum = randomNums.stream().mapToDouble(Double::doubleValue).sum();
        probabilities = randomNums.stream().map(num -> num / sum).collect(Collectors.toList());
        double one = probabilities.stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("Is this one?" + one);
        
        List<Integer> t_algo = computeT(probabilities, R );
        List<Integer> t_real = RiskAnalysis.computeT(probabilities, R);
        List<Double> t_real_asReal = RiskAnalysis.compute_t_iGivenR_real(probabilities, R);

        double algoR = computeR(t_algo, probabilities);
        double realR = computeR(t_real, probabilities);
        double realRUnrounded = RiskAnalysis.computeR(t_real_asReal, probabilities);
        
        int sumTAlgo = computeTInt(t_algo);
        int sumTReal = computeTInt(t_real);
        double sumTRealNoRounding = computeT(t_real_asReal);
        
        boolean equal_t_is = t_algo.equals(t_real);
        System.out.format("Same amount of tests? %s\n", sumTAlgo == sumTReal ? "yep" : "nope");
        System.out.format("T algo: %d\nT real: %d\nT not rounded: %.2f\n", sumTAlgo, sumTReal, sumTRealNoRounding);
        System.out.format("Are the t_i the same? %s\n", equal_t_is ? "yep" : "nope");
        
        System.out.println(t_algo + " -> computed by algorithm");
        System.out.println(t_real + " -> computed directly (real valued solution rounded)");
        System.out.println(t_real_asReal + " -> this is the base for the rounded solution");
        
        System.out.format("%.22f\n", algoR);
        System.out.format("%.22f\n", realR);
        System.out.format("%.22f\n", realRUnrounded);
        
        System.out.format("R - algoR: %.22f\n", (R - algoR));
        System.out.format("R - realR: %.22f\n", (R - realR));
        
        if(algoR < realR)
        {
            System.out.println("Algo is better by:");
            System.out.format("%.20f", realR - algoR);
        }
        else
        {
            System.out.println("Algo is same or worse by:");
            System.out.format("%.20f", algoR - realR);
        }
    }

    private static void testGivenRiskSmallExample()
    {
        double R = 0.0001;
        List<Double> probabilities;
        
        double[] arrProbabilities = new double[] {0.3, 0.3, 0.2, 0.2};
        probabilities = DoubleStream.of(arrProbabilities).boxed().collect(Collectors.toList());
        double one = probabilities.stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("Is this one?" + one);
        
        List<Integer> t_algo = computeT(probabilities, R );
        List<Integer> t_real = RiskAnalysis.computeT(probabilities, R);
        List<Double> t_real_asReal = RiskAnalysis.compute_t_iGivenR_real(probabilities, R);

        double algoR = computeR(t_algo, probabilities);
        double realR = computeR(t_real, probabilities);
        double realRUnrounded = RiskAnalysis.computeR(t_real_asReal, probabilities);
        
        int sumTAlgo = computeTInt(t_algo);
        int sumTReal = computeTInt(t_real);
        double sumTRealNoRounding = computeT(t_real_asReal);
        
        boolean equal_t_is = t_algo.equals(t_real);
        System.out.format("Same amount of tests? %s\n", sumTAlgo == sumTReal ? "yep" : "nope");
        System.out.format("T algo: %d\nT real: %d\nT not rounded: %.2f\n", sumTAlgo, sumTReal, sumTRealNoRounding);
        System.out.format("Are the t_i the same? %s\n", equal_t_is ? "yep" : "nope");
        
        System.out.println(t_algo + " -> computed by algorithm");
        System.out.println(t_real + " -> computed directly (real valued solution rounded)");
        System.out.println(t_real_asReal + " -> this is the base for the rounded solution");
        
        System.out.format("%.22f\n", algoR);
        System.out.format("%.22f\n", realR);
        System.out.format("%.22f\n", realRUnrounded);
        
        System.out.format("R - algoR: %.22f\n", (R - algoR));
        System.out.format("R - realR: %.22f\n", (R - realR));
        
        if(algoR < realR)
        {
            System.out.println("Algo is better by:");
            System.out.format("%.20f\n", realR - algoR);
        }
        else
        {
            System.out.println("Algo is same or worse by:");
            System.out.format("%.20f\n", algoR - realR);
        }
        
        arrProbabilities = new double[] {0.1, 0.1, 0.7, 0.1};
        probabilities = DoubleStream.of(arrProbabilities).boxed().collect(Collectors.toList());
        double changedR = computeR(t_algo, probabilities);
        System.out.format("Risk s_0: %.9f\n", algoR);
        System.out.format("Risk s_1: %.9f\n", changedR);
        System.out.println("delta s_0, s1: " +  (changedR - algoR));
        
//        TODO continue here
//        List<Integer> t_new = com
    }

    public static double deltaProfile(List<Double> profile1, List<Double> profile2)
    {
        double result = 0.0;
        for(int idx = 0; idx < profile1.size(); idx++)
        {
            result = result + sqr(profile1.get(idx) - profile2.get(idx));
        }
        return Math.sqrt(result);
    }

    public static Double sqr(Double val)
    {
        return val * val;
    }
    
    public static int computeTDiff(List<Double> t_is_new, List<Double> t_is_old)
    {
        //test only in bins where there were not enough tests, if there were the same amount or more, just leave it
        //the sum over all bins of this calculation is how many tests are additionally needed
        Function<Integer, Integer> t_i_diff = 
                idx -> t_is_old.get(idx) < t_is_new.get(idx) ? t_is_new.get(idx).intValue() - t_is_old.get(idx).intValue() : 0;
        return IntStream.range(0, t_is_old.size()).boxed().map(t_i_diff).mapToInt(Integer::valueOf).sum();
    }

    public static int computeT(List<Double> t_is)
    {
        return (int) t_is.stream().mapToDouble(Double::valueOf).sum();
    }
    
    // IS_NEW
    public static int computeTInt(List<Integer> values)
    {
        return values.stream().mapToInt(Integer::intValue).sum();
    }

    // IS_NEW
    public static double computeR(List<Integer> t_is, List<Double> p_is)
    {
        return IntStream.range(0, p_is.size()).mapToDouble(idx -> p_is.get(idx) / (2 + ((double)t_is.get(idx)))).sum();
    }
    
    // IS_NEW
    public static List<Integer> computeT(List<Double> currentProfile, double R)
    {
        return compute_t_iGivenR(currentProfile, R);
    }
    
    // IS_NEW
    public static List<Integer> compute_t_iGivenT(List<Double> currentProfile, double T)
    {
        List<Integer> indices = IntStream.range(0, currentProfile.size()).boxed().collect(Collectors.toList());
        List<Integer> t = IntStream.range(0, currentProfile.size()).boxed().map(idx -> 0).collect(Collectors.toList());
        List<Double> p = currentProfile;
        List<Double> diffVector = new ArrayList<Double>();
        int availableT = (int)T;
        while(availableT > 0)
        {
            //for each p_i figure out how much improvement we will get with an additional test.
            diffVector = indices.stream().collect(Collectors.mapping(idx -> (p.get(idx) / (2 + t.get(idx))) - (p.get(idx) / (2 + t.get(idx) + 1)), Collectors.toList()));
            
            //figure out at which idx we got the highest improvement
            int maxIdx = diffVector.indexOf(Collections.max(diffVector));
            
            //update the best candidate (make one more test there)
            t.set(maxIdx, t.get(maxIdx) + 1);
            availableT--;
        }
        return t;
    }
    
    // IS_NEW
    // unfortunately streams with collections are significantly slower than simple arrays and loops. Not surprising considering the boxing/unboxing but
    // the extent is actually quite large
    public static List<Integer> compute_t_iGivenRStreamBased(List<Double> currentProfile, double riskUpperBound)
    {
        double currentRisk = Double.POSITIVE_INFINITY;
        List<Integer> indices = IntStream.range(0, currentProfile.size()).boxed().collect(Collectors.toList());
        List<Integer> t = IntStream.range(0, currentProfile.size()).boxed().map(idx -> 0).collect(Collectors.toList());
        List<Double> p = currentProfile;
        List<Double> diffVector = new ArrayList<Double>();
        List<Double> resultVector = new ArrayList<Double>();
        int cnt = 0;
        while(currentRisk > riskUpperBound)
        {
            //for each p_i figure out how much improvement we will get with an additional test.
            diffVector = indices.stream().collect(Collectors.mapping(idx -> (p.get(idx) / (2 + t.get(idx))) - (p.get(idx) / (2 + t.get(idx) + 1)), Collectors.toList()));
            
            //figure out at which idx we got the highest improvement
            int maxIdx = diffVector.indexOf(Collections.max(diffVector));
            
            //update the best candidate (make one more test there)
            t.set(maxIdx, t.get(maxIdx) + 1);
            resultVector = indices.stream().map(idx -> p.get(idx) / (2 + t.get(idx))).collect(Collectors.toList());
            
            //see how we improved
            currentRisk = resultVector.stream().mapToDouble(val -> val).sum();
            if(cnt == 0)
            {
                System.out.println(p);
                System.out.println(diffVector);
                System.out.println(resultVector);
                System.out.println(maxIdx);
                System.out.println(currentRisk);
                System.out.println(t);
            }
            cnt++;
        }
        System.out.println("Iterations: " + cnt);
        return t;
    }
    
    public static List<Integer> compute_t_iGivenR(List<Double> currentProfile, double riskUpperBound)
    {
        double currentRisk = Double.POSITIVE_INFINITY;
        List<Integer> indices = IntStream.range(0, currentProfile.size()).boxed().collect(Collectors.toList());
//        List<Integer> t = IntStream.range(0, currentProfile.size()).boxed().map(idx -> 0).collect(Collectors.toList());
        double[] p = currentProfile.stream().mapToDouble(d -> d).toArray();
        int[] t = new int[p.length];
        Arrays.fill(t, 0);
        double[] diffVector = new double[p.length];
        double[] resultVector = new double[p.length];
        int cnt = 0;
        while(currentRisk > riskUpperBound)
        {
            for(int idx = 0; idx < p.length; idx++)
            {
                diffVector[idx] = (p[idx] / (2 + t[idx])) - (p[idx] / (2 + t[idx] + 1));
            }
            
            //figure out at which idx we got the highest improvement
            int maxIdx = 0;
            double curMax = Double.NEGATIVE_INFINITY;
            for(int idx = 0; idx < p.length; idx++)
            {
                if(diffVector[idx] > curMax)
                {
                    maxIdx = idx;
                    curMax = diffVector[idx];
                }
            }
            
            //update the best candidate (make one more test there)
            t[maxIdx] = t[maxIdx] + 1;
            for(int idx = 0; idx < indices.size(); idx++)
            {
                resultVector[idx] = p[idx]/(2 + t[idx]);
            }
            
            //see how we improved
            currentRisk = Arrays.stream(resultVector).sum();
            if(cnt == 0)
            {
                System.out.println(Arrays.toString(p));
                System.out.println(Arrays.toString(diffVector));
                System.out.println(Arrays.toString(resultVector));
                System.out.println(maxIdx);
                System.out.println(currentRisk);
                System.out.println(Arrays.toString(t));
            }
            cnt++;
        }
        System.out.println("Iterations: " + cnt);
        return IntStream.of(t).boxed().collect(Collectors.toList());
    }
    
    // same as compute_t_iGivenT
    public static List<Integer> t_iMinimizeRiskGivenTAndp_i(List<Double> probabilities, int T)
    {
        List<Integer> indices = IntStream.range(0, probabilities.size()).boxed().collect(Collectors.toList());
        List<Integer> t = IntStream.range(0, probabilities.size()).boxed().map(idx -> 0).collect(Collectors.toList());
        List<Double> p = probabilities;
        List<Double> diffVector = new ArrayList<Double>();
        int availableT = (int)T;
        while(availableT > 0)
        {
            //for each p_i figure out how much improvement we will get with an additional test.
            diffVector = indices.stream().collect(Collectors.mapping(idx -> (p.get(idx) / (2 + t.get(idx))) - (p.get(idx) / (2 + t.get(idx) + 1)), Collectors.toList()));
            
            //figure out at which idx we got the highest improvement
            int maxIdx = diffVector.indexOf(Collections.max(diffVector));
            
            //update the best candidate (make one more test there)
            t.set(maxIdx, t.get(maxIdx) + 1);
            availableT--;
        }
        return t;
    }
    
    // same as computeT
    public static List<Integer> t_iMinimizeTGivenRAndp_i(List<Double> probabilities, double R)
    {
        List<Integer> indices = IntStream.range(0, probabilities.size()).boxed().collect(Collectors.toList());
        List<Integer> t = IntStream.range(0, probabilities.size()).boxed().map(idx -> 0).collect(Collectors.toList());
        List<Double> p = probabilities;
        List<Double> diffVector = new ArrayList<Double>();
        List<Double> resultVector = indices.stream().map(idx -> p.get(idx) / (2 + t.get(idx))).collect(Collectors.toList());
        int cnt = 0;
        double currentRisk = resultVector.stream().mapToDouble(val -> val).sum();
        while(currentRisk > R)
        {
            //for each p_i figure out how much improvement we will get with an additional test.
            diffVector = indices.stream().collect(Collectors.mapping(idx -> (p.get(idx) / (2 + t.get(idx))) - (p.get(idx) / (2 + t.get(idx) + 1)), Collectors.toList()));
            
            //figure out at which idx we got the highest improvement
            int maxIdx = diffVector.indexOf(Collections.max(diffVector));
            
            //update the best candidate (make one more test there)
            t.set(maxIdx, t.get(maxIdx) + 1);
            resultVector = indices.stream().map(idx -> p.get(idx) / (2 + t.get(idx))).collect(Collectors.toList());
            
            //see how we improved
            currentRisk = resultVector.stream().mapToDouble(val -> val).sum();
            cnt++;
        }
        System.out.println("Iterations: " + cnt);
        return t;
    }

    /**
     * Algorithmic improvement of existing distribution of tests. Computes a new distribution of tests for a new probability-distribution
     * @param probabilities the probability distribution
     * @param testDistribution the old distribution of tests
     * @param T the amount of new tests to add to the distribution
     * @return the new distribution of tests with T more tests than before
     */
    public static List<Integer> t_iMinimizeRiskGivenT_p_iAndOldt_i(List<Double> probabilities, List<Integer> testDistribution, int T)
    {
        List<Integer> indices = IntStream.range(0, probabilities.size()).boxed().collect(Collectors.toList());
        List<Integer> t = copyOldDistribution(testDistribution);
        List<Double> p = probabilities;
        List<Double> diffVector = new ArrayList<Double>();
        int availableT = (int)T;
        while(availableT > 0)
        {
            //for each p_i figure out how much improvement we will get with an additional test.
            diffVector = indices.stream().collect(Collectors.mapping(idx -> (p.get(idx) / (2 + t.get(idx))) - (p.get(idx) / (2 + t.get(idx) + 1)), Collectors.toList()));
            
            //figure out at which idx we got the highest improvement
            int maxIdx = diffVector.indexOf(Collections.max(diffVector));
            
            //update the best candidate (make one more test there)
            t.set(maxIdx, t.get(maxIdx) + 1);
            availableT--;
        }
        return t;
    }

    public static List<Integer> t_iMinimizeRiskGivenT_p_iAndOldt_iDiffOnly(List<Double> probabilities, List<Integer> testDistribution, int T)
    {
        List<Integer> indices = IntStream.range(0, probabilities.size()).boxed().collect(Collectors.toList());
        List<Integer> t = copyOldDistribution(testDistribution);
        List<Integer> result = IntStream.range(0, testDistribution.size()).boxed().map(idx -> 0).collect(Collectors.toList());
        List<Double> p = probabilities;
        List<Double> diffVector = new ArrayList<Double>();
        int availableT = (int)T;
        while(availableT > 0)
        {
            //for each p_i figure out how much improvement we will get with an additional test.
            diffVector = indices.stream().collect(Collectors.mapping(idx -> (p.get(idx) / (2 + t.get(idx))) - (p.get(idx) / (2 + t.get(idx) + 1)), Collectors.toList()));
            
            //figure out at which idx we got the highest improvement
            int maxIdx = diffVector.indexOf(Collections.max(diffVector));
            
            //update the best candidate (make one more test there)
            t.set(maxIdx, t.get(maxIdx) + 1);
            result.set(maxIdx, result.get(maxIdx) + 1);
            availableT--;
        }
        return result;
    }

    public static List<Integer> t_iMinimizeTGivenRisk_p_iAndOldt_i(List<Double> probabilities, List<Integer> testDistribution, double R)
    {
        int cnt = 0;
        List<Integer> indices = IntStream.range(0, probabilities.size()).boxed().collect(Collectors.toList());
        List<Integer> t = copyOldDistribution(testDistribution);
        List<Double> p = probabilities;
        List<Double> diffVector = new ArrayList<Double>();
        List<Double> resultVector = indices.stream().map(idx -> p.get(idx) / (2 + t.get(idx))).collect(Collectors.toList());
        double currentRisk = resultVector.stream().mapToDouble(val -> val).sum();
        while(currentRisk > R)
        {
            //for each p_i figure out how much improvement we will get with an additional test.
            diffVector = indices.stream().collect(Collectors.mapping(idx -> (p.get(idx) / (2 + t.get(idx))) - (p.get(idx) / (2 + t.get(idx) + 1)), Collectors.toList()));
            
            //figure out at which idx we got the highest improvement
            int maxIdx = diffVector.indexOf(Collections.max(diffVector));
            
            //update the best candidate (make one more test there)
            t.set(maxIdx, t.get(maxIdx) + 1);
            resultVector = indices.stream().map(idx -> p.get(idx) / (2 + t.get(idx))).collect(Collectors.toList());
            
            //see how we improved
            currentRisk = resultVector.stream().mapToDouble(val -> val).sum();
            cnt++;
        }
        return t;
    }

    public static List<Integer> t_iMinimizeTGivenRisk_p_iAndOldt_iDiffOnly(List<Double> probabilities, List<Integer> testDistribution, double R)
    {
        int cnt = 0;
        List<Integer> indices = IntStream.range(0, probabilities.size()).boxed().collect(Collectors.toList());
        List<Integer> t = copyOldDistribution(testDistribution);
        List<Integer> result = IntStream.range(0, testDistribution.size()).boxed().map(idx -> 0).collect(Collectors.toList());
        List<Double> p = probabilities;
        List<Double> diffVector = new ArrayList<Double>();
        List<Double> resultVector = indices.stream().map(idx -> p.get(idx) / (2 + t.get(idx))).collect(Collectors.toList());
        double currentRisk = resultVector.stream().mapToDouble(val -> val).sum();
        while(currentRisk > R)
        {
            //for each p_i figure out how much improvement we will get with an additional test.
            diffVector = indices.stream().collect(Collectors.mapping(idx -> (p.get(idx) / (2 + t.get(idx))) - (p.get(idx) / (2 + t.get(idx) + 1)), Collectors.toList()));
            
            //figure out at which idx we got the highest improvement
            int maxIdx = diffVector.indexOf(Collections.max(diffVector));
            
            //update the best candidate (make one more test there)
            t.set(maxIdx, t.get(maxIdx) + 1);
            result.set(maxIdx, result.get(maxIdx) + 1);
            resultVector = indices.stream().map(idx -> p.get(idx) / (2 + t.get(idx))).collect(Collectors.toList());
            
            //see how we improved
            currentRisk = resultVector.stream().mapToDouble(val -> val).sum();
            cnt++;
        }
        return result;
    }
        
    private static List<Integer> copyOldDistribution(List<Integer> testDistribution)
    {
        return IntStream.range(0, testDistribution.size()).boxed().map(idx -> testDistribution.get(idx)).collect(Collectors.toList());
    }
}
