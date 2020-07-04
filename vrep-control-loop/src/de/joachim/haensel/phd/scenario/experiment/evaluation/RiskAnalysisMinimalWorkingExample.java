package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.util.List;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class RiskAnalysisMinimalWorkingExample
{
    public static void main(String[] args)
    {
        testOneStepEvolve4SubDomains();
    }

    private static void testOneStepEvolve4SubDomains()
    {
        double R = 0.0001;
        List<Double> probabilities;

        double[] arrProbabilities = new double[] { 0.3, 0.3, 0.2, 0.2 };
        probabilities = DoubleStream.of(arrProbabilities).boxed().collect(Collectors.toList());
        double one = probabilities.stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("Is this one?" + one);

        List<Integer> t_algo = RiskAnalysisAlgorithmically.computeT(probabilities, R);
        double algoR = RiskAnalysisAlgorithmically.computeR(t_algo, probabilities);
        int sumTAlgo = RiskAnalysisAlgorithmically.computeTInt(t_algo);

        System.out.format("%s -> operational distribution\n", probabilities.toString());
        System.out.format("%s -> test distribution computed by algorithm\n", t_algo.toString());
        System.out.format("%.22f risk if tests had been applied for expected risk: %.22f\n", algoR, R);
        System.out.format("R - algoR: %.22f\n", (R - algoR));

        arrProbabilities = new double[] { 0.1, 0.1, 0.7, 0.1 };
        probabilities = DoubleStream.of(arrProbabilities).boxed().collect(Collectors.toList());
        double changedR = RiskAnalysisAlgorithmically.computeR(t_algo, probabilities);
        System.out.format("%s -> new s_1 operational distribution\n", probabilities.toString());
        System.out.format("Risk s_0: %.9f\n", algoR);
        System.out.format("Risk s_1: %.9f\n", changedR);
        System.out.println("delta s_0, s1: " + (changedR - algoR));

        arrProbabilities = new double[] {1.0/3.0, 1.0/3.0, 0.0, 1.0/3.0 };
        probabilities = DoubleStream.of(arrProbabilities).boxed().collect(Collectors.toList());
        double forbiddenAdaptationUniformR = RiskAnalysisAlgorithmically.computeR(t_algo, probabilities);
        System.out.format("%s -> new s_2 operational distribution\n", probabilities.toString());
        System.out.format("Risk s_0: %.9f\n", algoR);
        System.out.format("Risk s_1: %.9f\n", changedR);
        System.out.format("Risk s_2: %.9f\n", forbiddenAdaptationUniformR);
        System.out.println("delta s_0, s2: " + (forbiddenAdaptationUniformR - algoR));

        arrProbabilities = new double[] {0.1, 0.45, 0.0, 0.45 };
        probabilities = DoubleStream.of(arrProbabilities).boxed().collect(Collectors.toList());
        double forbiddenAdaptationRealWorldR = RiskAnalysisAlgorithmically.computeR(t_algo, probabilities);
        System.out.format("%s -> new s_2 operational distribution\n", probabilities.toString());
        System.out.format("Risk s_0: %.9f\n", algoR);
        System.out.format("Risk s_1: %.9f\n", changedR);
        System.out.format("Risk s_2: %.9f\n", forbiddenAdaptationUniformR);
        System.out.format("Risk s_3: %.9f\n", forbiddenAdaptationRealWorldR);
        System.out.println("delta s_0, s3: " + (forbiddenAdaptationRealWorldR - algoR));
    }
    
    private static List<Double> minimizeWithAdditionalTests(List<Double> t_is, List<Double> currentProfile_p, List<Double> previousProfile_p, double maxAdditionalTests)
    {
        int n = currentProfile_p.size();
        List<Boolean> newPSmallerOrEqualOld = 
                IntStream.range(0, n).boxed().map(idx -> (currentProfile_p.get(idx) - previousProfile_p.get(idx) <= 0)).collect(Collectors.toList());
        List<Double> currentProfile_p_largerOld = IntStream.range(0, n).mapToDouble(idx -> !newPSmallerOrEqualOld.get(idx) ? currentProfile_p.get(idx) : 0.0).boxed().collect(Collectors.toList());
        int n_larger_old = currentProfile_p_largerOld.stream().mapToInt(p_i -> p_i > 0 ? 1 : 0).sum();
        double sumOfp_iRoot = RiskAnalysis.sumOfp_iRoot(currentProfile_p_largerOld);
        
        //equation 15 / 16
        IntToDoubleFunction t_i_assigner = 
                idx -> 
                {
                    if(newPSmallerOrEqualOld.get(idx))
                    {
                        return 0.0;
                    }
                    else
                    {
                        // should read maxAdditionalTests + 2 * ) / sumOfp_iRoot - 2 
                        // as in the other case I omit 2 from 1/(2+t) because 2 was already included in former computations
//                        double tStar_i = (Math.sqrt(currentProfile_p.get(idx)) * (maxAdditionalTests + 2* n_larger_old)) / (sumOfp_iRoot);
                        double tStar_i = (Math.sqrt(currentProfile_p.get(idx)) * (maxAdditionalTests)) / (sumOfp_iRoot);
                        return tStar_i;
                    }
                };
        List<Double> t_iNew = IntStream.range(0, n).mapToDouble(t_i_assigner).boxed().collect(Collectors.toList());
        return t_iNew;
    }
}