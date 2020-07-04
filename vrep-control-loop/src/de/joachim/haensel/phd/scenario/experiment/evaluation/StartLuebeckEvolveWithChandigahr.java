package de.joachim.haensel.phd.scenario.experiment.evaluation;

import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.math.Linspace;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

public class StartLuebeckEvolveWithChandigahr
{
    public static void main(String[] args)
    {
        TrajectoryLoader trajectoryLoader = new TrajectoryLoader();
        Map<Integer, MongoTrajectory> dbTrajectories = trajectoryLoader.loadIndexedDataToMap("data_available_at_2020-01-18");
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = loadClusteringK(200);
        Random randomGen = new MersenneTwister(1001);
        double riskUpperBound = Math.pow(10.0, -4.0);
        double maxTests = 10000.0;
        double maxAdditionalTests = 6;
//        runEvolve(dbTrajectories, clustering, randomGen, riskUpperBound, maxTests, "Luebeck", "Chandigarh");
        runEvolve(dbTrajectories, clustering, randomGen, riskUpperBound, maxTests, maxAdditionalTests, "Chandigarh", "Luebeck");
    }

    private static void runEvolve(Map<Integer, MongoTrajectory> dbTrajectories, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering, Random randomGen, double riskUpperBound, double maxTests, double maxAdditionalTests, String startCityName, String evolveIntoCityName)
    {
        Map<Integer, Trajectory3DSummaryStatistics> reversedMap = reverseClustering(clustering);
        Map<Trajectory3DSummaryStatistics, List<Integer>> startCityClustering = extractForCity(dbTrajectories, clustering, clustering.keySet(), startCityName);
        Map<Trajectory3DSummaryStatistics, List<Integer>> evolveIntoCityClustering = extractForCity(dbTrajectories, clustering, clustering.keySet(), evolveIntoCityName);
    
        List<Integer> startCityIndices = new ArrayList<Integer>(getClusterIndices(startCityClustering));
        List<Integer> evlovIntoCityIndices = new ArrayList<Integer>(getClusterIndices(evolveIntoCityClustering));

        List<Double> startCity_p = retreiveProfileFrom(startCityClustering);
        List<Double> t_is_startCity = RiskAnalysis.compute_t_iGivenR(startCity_p, riskUpperBound);
        
        HashMap<Integer, Integer> clusterCounts = initializeClusterCountsFrom(startCityClustering);
        

        Collections.shuffle(startCityIndices);
        Collections.shuffle(evlovIntoCityIndices);
     
        Deque<Integer> startCityUsableIndices = new LinkedList<Integer>(startCityIndices);
        Deque<Integer> evolveIntoCityUsableIndices = new LinkedList<Integer>(evlovIntoCityIndices);
        int maxRange = 10000;
        int quarterRange = maxRange / 4;
        int dataToSample = 1000;
        List<Double> cityProbability = new ArrayList<Double>();
        for(int idx = 0; idx < quarterRange; idx++)
        {
            cityProbability.add(0.0);
        }
        List<Double> linspace = Linspace.linspace(0, 0.5, quarterRange);
        for(int idx = 0; idx < quarterRange; idx++)
        {
            cityProbability.add(linspace.get(idx));
        }
        for(int idx = 0; idx < (2 * quarterRange); idx++)
        {
            cityProbability.add(0.5);
        }
        
        List<Double> initial_tis = RiskAnalysis.compute_t_iGivenR(startCity_p, riskUpperBound);
        List<Double> t_i_maintainUpperBound = new ArrayList<Double>(initial_tis);
        List<Double> t_i_minimizeWithAdditionalTests = new ArrayList<Double>(initial_tis);
        List<Double> t_i_minimizeWithAdditionalTestsMaintainUpperBound = new ArrayList<Double>(initial_tis);
        List<Double> previousProfile_p = retreiveProfileFromIndexed(clusterCounts);
        try
        {
            String ubFileName = String.format("./evolve%sTo%s_k%dMaintainUP.csv", startCityName, evolveIntoCityName, clustering.keySet().size());
            BufferedWriter ubWriter = Files.newBufferedWriter(Paths.get(ubFileName));
            String ubHeaderRow = String.format("diff_p batchCnt%1$s batchCnt%2$s nr_newTests nr_allTests risk risk_no_added_test", startCityName, evolveIntoCityName);
            ubWriter.write(ubHeaderRow);
            ubWriter.newLine();

            String addedFileName = String.format("./evolve%sTo%s_k%dAddm%smoreTests.csv", startCityName, evolveIntoCityName, clustering.keySet().size(), maxAdditionalTests);
            BufferedWriter addedWriter = Files.newBufferedWriter(Paths.get(addedFileName));
            String addedHeaderRow = String.format("diff_p batchCnt%1$s batchCnt%2$s nr_newTests nr_allTests risk risk_no_added_test", startCityName, evolveIntoCityName);
            addedWriter.write(addedHeaderRow);
            addedWriter.newLine();

            String addedUbFileName = String.format("./evolve%sTo%s_k%dAddm%smoreTestsMaintainUB.csv", startCityName, evolveIntoCityName, clustering.keySet().size(), maxAdditionalTests);
            BufferedWriter addedUbWriter = Files.newBufferedWriter(Paths.get(addedUbFileName));
            String addedUbHeaderRow = String.format("diff_p batchCnt%1$s batchCnt%2$s nr_newTests nr_allTests risk risk_no_added_test", startCityName, evolveIntoCityName);
            addedUbWriter.write(addedUbHeaderRow);
            addedUbWriter.newLine();

            for(int cnt = 0; cnt < maxRange - 1; cnt++)
            {
                int batchCntStartCity = 0;
                int batchCntEvolveIntoCity = 0;
                for(int idx = 0; idx < dataToSample; idx++)
                {
                    if(randomGen.nextDouble() < cityProbability.get(cnt))
                    {
                        if(evolveIntoCityUsableIndices.isEmpty())
                        {
                            //refill if empty, don't forget to shuffle
                            Collections.shuffle(evlovIntoCityIndices);
                            evolveIntoCityUsableIndices.addAll(evlovIntoCityIndices);
                        }
                        Integer trajectoryIdx = evolveIntoCityUsableIndices.remove();
                        int clusterNr = reversedMap.get(trajectoryIdx).getClusterNr();
                        clusterCounts.put(clusterNr, clusterCounts.get(clusterNr) == null ? 1 : clusterCounts.get(clusterNr) + 1);
                        batchCntEvolveIntoCity++;
                    }
                    else
                    {
                        if(startCityUsableIndices.isEmpty())
                        {
                            //refill if empty, don't forget to shuffle
                            Collections.shuffle(startCityIndices);
                            startCityUsableIndices.addAll(startCityIndices);
                        }
                        Integer trajectoryIdx = startCityUsableIndices.remove();
                        int clusterNr = reversedMap.get(trajectoryIdx).getClusterNr();
                        clusterCounts.put(clusterNr, clusterCounts.get(clusterNr) == null ? 1 : clusterCounts.get(clusterNr) + 1);
                        batchCntStartCity++;
                    }
                }
                List<Double> currentProfile_p = retreiveProfileFromIndexed(clusterCounts);
                String bareRiskDiff = Double.toString(RiskAnalysis.computeR(initial_tis, currentProfile_p));
                
                List<Double> newTestsMaintainUpperBound = 
                        maintainUpperBound(t_i_maintainUpperBound, currentProfile_p, previousProfile_p, riskUpperBound);
                t_i_maintainUpperBound = merge(newTestsMaintainUpperBound, t_i_maintainUpperBound);
                //TODO do rounding each time (the t_is in the loop need to stay Integers though
                String ubNewTests = Double.toString(newTestsMaintainUpperBound.stream().mapToDouble(Double::valueOf).sum());
                String ubAllTests = Double.toString(t_i_maintainUpperBound.stream().mapToDouble(Double::valueOf).sum());
                String ubRisk = Double.toString(RiskAnalysis.computeR(t_i_maintainUpperBound, currentProfile_p));
                
                List<Double> newTestsMinimizeWithAdditional = 
                        minimizeWithAdditionalTests(t_i_minimizeWithAdditionalTests, currentProfile_p, previousProfile_p, maxAdditionalTests);
                t_i_minimizeWithAdditionalTests = merge(newTestsMinimizeWithAdditional, t_i_minimizeWithAdditionalTests);
                String addedNewTests = Double.toString(newTestsMinimizeWithAdditional.stream().mapToDouble(Double::valueOf).sum());
                String addedAllTests = Double.toString(t_i_minimizeWithAdditionalTests.stream().mapToDouble(Double::valueOf).sum());
                String addedNewTestsRisk = Double.toString(RiskAnalysis.computeR(t_i_minimizeWithAdditionalTests, currentProfile_p));

                
                List<Double> newTestsMinimizeWithAdditionalAndMaintain = 
                        minimizeWithAdditionalTestsAndMaintainUpperBound(t_i_minimizeWithAdditionalTestsMaintainUpperBound, currentProfile_p, previousProfile_p, riskUpperBound, maxAdditionalTests);
                t_i_minimizeWithAdditionalTestsMaintainUpperBound = merge(newTestsMinimizeWithAdditionalAndMaintain, t_i_minimizeWithAdditionalTestsMaintainUpperBound);
                String addedUbNewTests = Double.toString(newTestsMinimizeWithAdditionalAndMaintain.stream().mapToDouble(Double::valueOf).sum());
                String addedUbAllTests = Double.toString(t_i_minimizeWithAdditionalTestsMaintainUpperBound.stream().mapToDouble(Double::valueOf).sum());
                String addedUbTestsRisk = Double.toString(RiskAnalysis.computeR(t_i_minimizeWithAdditionalTestsMaintainUpperBound, currentProfile_p));
                
                previousProfile_p = currentProfile_p;
                
                double diff_p = RiskAnalysis.deltaProfile(currentProfile_p, startCity_p);
                ubWriter.write(String.format("%f %d %d %s %s %s %s", diff_p, batchCntStartCity, batchCntEvolveIntoCity, ubNewTests, ubAllTests, ubRisk, bareRiskDiff));
                ubWriter.newLine();
                
                addedWriter.write(String.format("%f %d %d %s %s %s %s", diff_p, batchCntStartCity, batchCntEvolveIntoCity, addedNewTests, addedAllTests, addedNewTestsRisk, bareRiskDiff));
                addedWriter.newLine();

                addedUbWriter.write(String.format("%f %d %d %s %s %s %s", diff_p, batchCntStartCity, batchCntEvolveIntoCity, addedUbNewTests, addedUbAllTests, addedUbTestsRisk, bareRiskDiff));
                addedUbWriter.newLine();
            }
            ubWriter.flush();
            ubWriter.close();
            addedWriter.flush();
            addedWriter.close();
            addedUbWriter.flush();
            addedUbWriter.close();
        }
        catch (IOException  exc) 
        {
            exc.printStackTrace();
        }
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

    private static List<Double> maintainUpperBound(List<Double> t_is_old, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound)
    {
        if(t_is_old == null)
        {
            return RiskAnalysis.compute_t_iGivenR(currentProfile_p, riskUpperBound);
        }
        else
        {
            double currentRisk = RiskAnalysis.computeR(t_is_old, currentProfile_p);
            if(currentRisk <= riskUpperBound)
            {
                return IntStream.range(0, currentProfile_p.size()).mapToDouble(idx -> 0.0).boxed().collect(Collectors.toList());
            }
            else
            {
                return allocateNeccessaryTests(t_is_old, currentProfile_p, previousProfile_p, riskUpperBound);
            }
        }
    }

    private static List<Double> allocateNeccessaryTestsAlg2(List<Double> t_is_old, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound)
    {
        List<Double> new_ti_all = RiskAnalysis.compute_t_iGivenR(currentProfile_p, riskUpperBound);
        return diffLargerZero(new_ti_all, t_is_old);
    }

    private static List<Double> diffLargerZero(List<Double> new_ti_all, List<Double> t_is_old)
    {
        List<Double> result = 
                IntStream.range(0, new_ti_all.size()).mapToDouble(idx -> new_ti_all.get(idx) > t_is_old.get(idx) ? new_ti_all.get(idx) - t_is_old.get(idx) : 0.0).boxed().collect(Collectors.toList());
        return result;
    }

    private static List<Double> allocateNeccessaryTests(List<Double> t_is_old, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound)
    {
        int n = currentProfile_p.size();
        List<Boolean> newPSmallerOrEqualOld = 
                IntStream.range(0, n).boxed().map(idx -> (currentProfile_p.get(idx) - previousProfile_p.get(idx) <= 0)).collect(Collectors.toList());
        //equation 12
        double R_cov = IntStream.range(0, n).mapToDouble(idx -> newPSmallerOrEqualOld.get(idx) ? (currentProfile_p.get(idx)/(2 + t_is_old.get(idx))) : 0.0).sum();
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

    private static List<Double> minimizeWithAdditionalTestsAndMaintainUpperBound(List<Double> t_is, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound, double maxAdditionalTests)
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
    
    private static List<Double> allocateNeccessaryTestsSysout(List<Double> t_is_old, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound)
    {
        int n = currentProfile_p.size();
        
        List<Boolean> newPSmallerOrEqualOld = 
                IntStream.range(0, n).boxed().map(idx -> (currentProfile_p.get(idx) <= previousProfile_p.get(idx) + 0.000001)).collect(Collectors.toList());
        //equation 12
        double R_cov = IntStream.range(0, n).mapToDouble(idx -> newPSmallerOrEqualOld.get(idx) ? (currentProfile_p.get(idx)/(2 + t_is_old.get(idx))) : 0.0).sum();
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

    private static HashMap<Integer, Integer> initializeClusterCountsFrom(Map<Trajectory3DSummaryStatistics, List<Integer>> clustering)
    {
        Stream<Entry<Trajectory3DSummaryStatistics, List<Integer>>> entrySetStream = clustering.entrySet().stream();
        Map<Integer, Integer> clusterNrs = entrySetStream.collect(Collectors.toMap(entry -> entry.getKey().getClusterNr(), entry -> entry.getValue().size()));
        return new HashMap<Integer, Integer>(clusterNrs);
    }
}
