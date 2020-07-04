package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RiskAnalysisStrategies
{
    private static List<Double> allocateNeccessaryTestsSysout(List<Double> t_is_old, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound)
    {
        int n = currentProfile_p.size();
        
        List<Boolean> newPSmallerOrEqualOld = flagLoweredP(currentProfile_p, previousProfile_p, n);
        //equation 12
        double R_cov = computeRCov(t_is_old, currentProfile_p, n, newPSmallerOrEqualOld);
        //equation 13
        List<Double> currentProfile_p_largerOld = IntStream.range(0, n).mapToDouble(idx -> !newPSmallerOrEqualOld.get(idx) ? currentProfile_p.get(idx) : 0.0).boxed().collect(Collectors.toList());
        double sumOfp_iRoot = RiskAnalysis.sumOfp_iRoot(currentProfile_p_largerOld);
        
        IntToDoubleFunction t_i_assigner = 
                idx -> 
                {
                    if(newPSmallerOrEqualOld.get(idx))
                    {
                        return 0.0;
                    }
                    else
                    {
                        // is - 2 really a thing? shouldn't it be necessary only once?
                        // When we had in the formula - 2 I got negative numbers from this equation 
//                        double tStar_i = Math.sqrt(currentProfile_p.get(idx)) / (riskUpperBound - R_cov) * sumOfp_iRoot;// - 2;
                        double tStar_i = Math.sqrt(currentProfile_p.get(idx)) / (riskUpperBound - R_cov) * sumOfp_iRoot - 2;
                        Double old = t_is_old.get(idx);
                        double result = tStar_i - old;
                        if(result < 0.0)
                        {
                            // p'_i p_i t_diff
//                            System.out.println(String.format("%f %f %f", currentProfile_p.get(idx), previousProfile_p.get(idx), result));
                            return 0.0;
                        }
                        return result;
                    }
                };
        //equation 14
        List<Double> t_iNew = IntStream.range(0, n).mapToDouble(t_i_assigner).boxed().collect(Collectors.toList());
        return t_iNew;
    }
    public static List<Double> allocateNeccessaryTests(List<Double> t_is_old, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound)
    {
        int n = currentProfile_p.size();
        List<Boolean> newPSmallerOrEqualOld = 
                IntStream.range(0, n).boxed().map(idx -> (currentProfile_p.get(idx) - previousProfile_p.get(idx) <= 0)).collect(Collectors.toList());
        //equation 12
        double R_cov = computeRCov(t_is_old, currentProfile_p, n, newPSmallerOrEqualOld);
        //equation 13
        List<Double> currentProfile_p_largerOld = IntStream.range(0, n).mapToDouble(idx -> !newPSmallerOrEqualOld.get(idx) ? currentProfile_p.get(idx) : 0.0).boxed().collect(Collectors.toList());
        double sumOfp_iRoot = RiskAnalysis.sumOfp_iRoot(currentProfile_p_largerOld);
        
        IntToDoubleFunction t_i_assigner = 
                idx -> 
                {
                    if(newPSmallerOrEqualOld.get(idx))
                    {
                        return 0.0;
                    }
                    else
                    {
                        // is - 2 really a thing? shouldn't it be necessary only once?
                        // When we had in the formula - 2 I got negative numbers from this equation 
//                        double tStar_i = Math.sqrt(currentProfile_p.get(idx)) / (riskUpperBound - R_cov) * sumOfp_iRoot;// - 2;
                        double tStar_i = Math.sqrt(currentProfile_p.get(idx)) / (riskUpperBound - R_cov) * sumOfp_iRoot - 2;
                        Double old = t_is_old.get(idx);
                        double result = tStar_i - old;
                        return result;
                    }
                };
        //equation 14
        List<Double> t_iNew = IntStream.range(0, n).mapToDouble(t_i_assigner).boxed().collect(Collectors.toList());
        return t_iNew;
    }

    private static List<Boolean> flagLoweredP(List<Double> currentProfile_p, List<Double> previousProfile_p, int n)
    {
        return IntStream.range(0, n).boxed().map(idx -> (currentProfile_p.get(idx) <= previousProfile_p.get(idx) + 0.000001)).collect(Collectors.toList());
    }
    
    private static double computeRCov(List<Double> t_is_old, List<Double> currentProfile_p, int n, List<Boolean> newPSmallerOrEqualOld)
    {
        return IntStream.range(0, n).mapToDouble(idx -> newPSmallerOrEqualOld.get(idx) ? (currentProfile_p.get(idx)/(2 + t_is_old.get(idx))) : 0.0).sum();
    }

    public static List<Double> minimizeWithAdditionalTests(List<Double> t_is, List<Double> currentProfile_p, List<Double> previousProfile_p, double maxAdditionalTests)
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

    public static List<Double> minimizeWithAdditionalTestsAndMaintainUpperBound(List<Double> t_is, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound, double maxAdditionalTests)
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
//                        double tStar_i = (Math.sqrt(currentProfile_p.get(idx)) * (maxAdditionalTests + n_larger_old)) / (sumOfp_iRoot);
                        double tStar_i = (Math.sqrt(currentProfile_p.get(idx)) * (maxAdditionalTests)) / (sumOfp_iRoot);
                        return tStar_i;
                    }
                };
        List<Double> t_iNew = IntStream.range(0, n).mapToDouble(t_i_assigner).boxed().collect(Collectors.toList());
        List<Double> mergedTests = merge(t_iNew, t_is);
        double currentRisk = RiskAnalysis.computeR(mergedTests, currentProfile_p);
        List<Double> result = null;
        if(currentRisk <= riskUpperBound)
        {
            result = t_iNew;
        }
        else
        {
//            List<Double> prelimResult = RiskAnalysis.compute_t_iGivenR(currentProfile_p, riskUpperBound);
//            result = prelimResult;
            result = allocateNeccessaryTestsSysout(t_is, currentProfile_p, previousProfile_p, riskUpperBound);
        }
        return result;
    }

    private static List<Double> merge(List<Double> additionalTests, List<Double> currentTests)
    {
        if(currentTests == null)
        {
            return new ArrayList<Double>(additionalTests);
        }
        else
        {
            return IntStream.range(0, currentTests.size()).mapToDouble(idx -> currentTests.get(idx) + additionalTests.get(idx)).boxed().collect(Collectors.toList());
        }
    }
}
