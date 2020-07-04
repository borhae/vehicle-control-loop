package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RiskAnalysis
{
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

    public static double computeR(List<Double> t_is, List<Double> p_is)
    {
        return IntStream.range(0, p_is.size()).mapToDouble(idx -> p_is.get(idx) / (2 + t_is.get(idx))).sum();
    }
    
    public static List<Integer> computeT(List<Double> probabilities, double R)
    {
        List<Double> rounded_t_is = compute_t_iGivenR(probabilities, R);
        return rounded_t_is.stream().mapToInt(rounded_t_i -> rounded_t_i.intValue()).boxed().collect(Collectors.toList());
    }

    public static List<Double> compute_t_iGivenR(List<Double> probabilities, double R)
    {
        List<Double> t_is = compute_t_iGivenR_real(probabilities, R);
        List<Double> rounded_t_is = roundUpDown(t_is, probabilities);
        return rounded_t_is;
    }

    public static List<Double> compute_t_iGivenR_real(List<Double> probabilities, double R)
    {
        double sumOfp_iRoot = sumOfp_iRoot(probabilities);
        List<Double> t_is = probabilities.stream().mapToDouble(p_i -> ((Math.sqrt(p_i) / R) * sumOfp_iRoot) - 2.0).boxed().collect(Collectors.toList());
        return t_is;
    }
    
    public static List<Double> compute_t_iGivenT(List<Double> probabilities, double T)
    {
        List<Double> t_is = compute_t_iGivenT_real(probabilities, T);
        List<Double> rounded_t_is = roundUpDown(t_is, probabilities);
        return rounded_t_is;
    }

    public static List<Double> compute_t_iGivenT_real(List<Double> probabilities, double T)
    {
        double sumOfp_iRoot = sumOfp_iRoot(probabilities);
        ToDoubleFunction<? super Double> ti = p_i -> (( (Math.sqrt(p_i) * (T + 2.0*probabilities.size())) /sumOfp_iRoot) - 2.0);
        List<Double> t_is = new ArrayList<Double>();
        for(int idx = 0; idx < probabilities.size(); idx++)
        {
            double p_i = probabilities.get(idx);
            double t_i = ti.applyAsDouble(p_i);
            t_is.add(t_i);
        }
        return t_is;
    }
    
    public static double sumOfp_iRoot(List<Double> probabilities)
    {
        return probabilities.stream().mapToDouble(p_i -> Math.sqrt(p_i)).sum();
    }
    
    public static List<Double> roundUpDown(List<Double> t_is, List<Double> p_is)
    {
        List<Double> result = new ArrayList<Double>();
        double riskBuffer = 0;
        for (int idx = 0; idx < t_is.size(); idx++) 
        {
            double t_i = t_is.get(idx); //array[i]
            double p_i = p_is.get(idx); //pArray[i]
            if (t_i <= 0) 
            {
                result.add(0.0);
                riskBuffer = riskBuffer + p_i / (2 + t_i) - p_i / 2;  
            } 
            else 
            {
                double floor = Math.floor(t_i);
                double lostRisk = p_i / (2 + floor) - p_i / (2 + t_i);
                if (lostRisk <= riskBuffer) 
                {
                    result.add(floor);
                    riskBuffer = riskBuffer - lostRisk;
                } 
                else 
                {
                    double ceiling = Math.ceil(t_i);
                    result.add(ceiling);
                    riskBuffer = riskBuffer - p_i / (2 + ceiling) + p_i / (2 + t_i);
                }
            }
        }
        return result;
    }
}
