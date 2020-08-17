package de.joachim.haensel.phd.scenario.experiment.evaluation;

import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.extractForCity;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.getClusterIndices;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.loadClusteringK;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.retreiveProfileFrom;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.retreiveProfileFromIndexed;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.reverseClustering;

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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.math.Linspace;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

public class StartLuebeckEvolveWithChandigahrAlgorithm
{
    public static void main(String[] args)
    {
        TrajectoryLoader trajectoryLoader = new TrajectoryLoader();
        Map<Integer, MongoTrajectory> dbTrajectories = trajectoryLoader.loadIndexedDataToMap("data_available_at_2020-01-18");
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = loadClusteringK(200);
        Random evolveRandom = new MersenneTwister(1001);
        Random shuffleRandom = new MersenneTwister(1002);
        double riskUpperBound = Math.pow(10.0, -4.0);
        double maxTests = 10000.0;
        int maxAdditionalTests = 1;
//        runEvolve(dbTrajectories, clustering, randomGen, riskUpperBound, maxTests, "Luebeck", "Chandigarh");
        runEvolve(dbTrajectories, clustering, evolveRandom, shuffleRandom, riskUpperBound, maxTests, maxAdditionalTests, "Chandigarh", "Luebeck");
    }

    private static void runEvolve(Map<Integer, MongoTrajectory> dbTrajectories, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering, Random evolveRandom, Random shuffleRandom, double riskUpperBound, double maxTests, int maxAdditionalTests, String startCityName, String evolveIntoCityName)
    {
        int maxRange = 40000;
        int tenthRange = maxRange / 10;
        int dataToSample = 1000;
        List<Double> cityProbability = new ArrayList<Double>();
        for(int idx = 0; idx < tenthRange; idx++)
        {
            cityProbability.add(0.0);
        }
        List<Double> linspace = Linspace.linspace(0, 0.5, tenthRange);
        for(int idx = 0; idx < tenthRange; idx++)
        {
            cityProbability.add(linspace.get(idx));
        }
        for(int idx = 0; idx < tenthRange; idx++)
        {
            cityProbability.add(0.5);
        }
        linspace = Linspace.linspace(0.5, 1.0, tenthRange);
        for(int idx = 0; idx < tenthRange; idx++)
        {
            cityProbability.add(linspace.get(idx));
        }
        for(int idx = 0; idx < (6 * tenthRange); idx++)
        {
            cityProbability.add(1.0);
        }

        Map<Integer, Trajectory3DSummaryStatistics> reversedMap = reverseClustering(clustering);
        Map<Trajectory3DSummaryStatistics, List<Integer>> startCityClustering = extractForCity(dbTrajectories, clustering, clustering.keySet(), startCityName);
        Map<Trajectory3DSummaryStatistics, List<Integer>> evolveIntoCityClustering = extractForCity(dbTrajectories, clustering, clustering.keySet(), evolveIntoCityName);
    
        List<Integer> startCityIndices = new ArrayList<Integer>(getClusterIndices(startCityClustering));
        List<Integer> evlovIntoCityIndices = new ArrayList<Integer>(getClusterIndices(evolveIntoCityClustering));
        
        HashMap<Integer, Integer> clusterCounts = initializeClusterCountsFrom(startCityClustering);
        
        List<Double> startCity_p = retreiveProfileFrom(startCityClustering);
        List<Double> t_is_startCity = RiskAnalysis.compute_t_iGivenR(startCity_p, riskUpperBound);
        //-------------------------------------------------------------
        //TODO (remove me) Just debugging
        //-------------------------------------------------------------
//        Random dEvolveRandom = new MersenneTwister(1001);
//        Random dShuffleRandom = new MersenneTwister(1002);
//        SimulationBySampling sampler = new SimulationBySampling(dEvolveRandom, dShuffleRandom, startCityName, evolveIntoCityName, clustering, dbTrajectories);
//        sampler.setCyclesToSample(40000);
//        sampler.setSamplesPerCycle(1000);
//        ArrayList<FromTo> samplingProfile = 
//                new ArrayList<FromTo>(Arrays.asList(
//                        new FromTo(0.0, 0.0),
//                        new FromTo(0.0, 0.5),
//                        new FromTo(0.5, 0.5),
//                        new FromTo(0.5, 1.0),
//                        new FromTo(1.0, 1.0),
//                        new FromTo(1.0, 1.0),
//                        new FromTo(1.0, 1.0),
//                        new FromTo(1.0, 1.0),
//                        new FromTo(1.0, 1.0),
//                        new FromTo(1.0, 1.0)
//                )
//        );
//        sampler.setSamplingProfile(samplingProfile);
//        sampler.initialize();
        //-------------------------------------------------------------
        //-------------------------------------------------------------
        
        Collections.shuffle(startCityIndices, shuffleRandom);
        Collections.shuffle(evlovIntoCityIndices, shuffleRandom);
     
        Deque<Integer> startCityUsableIndices = new LinkedList<Integer>(startCityIndices);
        Deque<Integer> evolveIntoCityUsableIndices = new LinkedList<Integer>(evlovIntoCityIndices);
        
        List<Integer> initial_tis = RiskAnalysisAlgorithmically.compute_t_iGivenR(startCity_p, riskUpperBound);
        List<Integer> t_i_maintainUpperBound = new ArrayList<Integer>(initial_tis);
        List<Integer> t_i_minimizeWithAdditionalTests = new ArrayList<Integer>(initial_tis);
        List<Integer> t_i_minimizeWithAdditionalTestsMaintainUpperBound = new ArrayList<Integer>(initial_tis);
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
            
            String allInfoFileName = String.format("./evolve%sTo%s_k%dAddm%smoreTestsAllInfos.csv", startCityName, evolveIntoCityName, clustering.keySet().size(), maxAdditionalTests);
            BufferedWriter allInfoWriter = Files.newBufferedWriter(Paths.get(allInfoFileName));
            String allInfoHeaderRow = String.format("diff_p batchCnt%1$s batchCnt%2$s nr_newTests_ub nr_newTests_add nr_newTests_add_ub nr_allTestsUb nr_allTestsAdd nr_allTestsAddUb risk_ub risk_add risk_add_ub risk_no_added_test", startCityName, evolveIntoCityName);
            allInfoWriter.write(allInfoHeaderRow);
            allInfoWriter.newLine();

            for(int cnt = 0; cnt < maxRange - 1; cnt++)
            {
                int batchCntStartCity = 0;
                int batchCntEvolveIntoCity = 0;
                for(int idx = 0; idx < dataToSample; idx++)
                {
                    if(evolveRandom.nextDouble() < cityProbability.get(cnt))
                    {
                        if(evolveIntoCityUsableIndices.isEmpty())
                        {
                            //refill if empty, don't forget to shuffle
                            Collections.shuffle(evlovIntoCityIndices, shuffleRandom);
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
                            Collections.shuffle(startCityIndices, shuffleRandom);
                            startCityUsableIndices.addAll(startCityIndices);
                        }
                        Integer trajectoryIdx = startCityUsableIndices.remove();
                        int clusterNr = reversedMap.get(trajectoryIdx).getClusterNr();
                        clusterCounts.put(clusterNr, clusterCounts.get(clusterNr) == null ? 1 : clusterCounts.get(clusterNr) + 1);
                        batchCntStartCity++;
                    }
                }
                List<Double> currentProfile_p = retreiveProfileFromIndexed(clusterCounts);
                String bareRiskDiff = Double.toString(RiskAnalysisAlgorithmically.computeR(initial_tis, currentProfile_p));
                
                //Strategy 1: add so many tests that we can keep the upper bound
                List<Integer> newTestsMaintainUpperBound = 
                        maintainUpperBound(t_i_maintainUpperBound, currentProfile_p, previousProfile_p, riskUpperBound);
                t_i_maintainUpperBound = merge(newTestsMaintainUpperBound, t_i_maintainUpperBound);
                String ubNewTests = Double.toString(newTestsMaintainUpperBound.stream().mapToDouble(Double::valueOf).sum());
                String ubAllTests = Double.toString(t_i_maintainUpperBound.stream().mapToDouble(Double::valueOf).sum());
                String ubRisk = Double.toString(RiskAnalysisAlgorithmically.computeR(t_i_maintainUpperBound, currentProfile_p));
                
                //Strategy 2: add a fixed amount of tests
                List<Integer> newTestsMinimizeWithAdditional = 
                        minimizeWithAdditionalTests(t_i_minimizeWithAdditionalTests, currentProfile_p, previousProfile_p, maxAdditionalTests);
                t_i_minimizeWithAdditionalTests = merge(newTestsMinimizeWithAdditional, t_i_minimizeWithAdditionalTests);
                String addedNewTests = Double.toString(newTestsMinimizeWithAdditional.stream().mapToDouble(Double::valueOf).sum());
                String addedAllTests = Double.toString(t_i_minimizeWithAdditionalTests.stream().mapToDouble(Double::valueOf).sum());
                String addedNewTestsRisk = Double.toString(RiskAnalysisAlgorithmically.computeR(t_i_minimizeWithAdditionalTests, currentProfile_p));

                //Strategy 3: add a fixed amount of tests per cycle and add more if we can't keep the upper bound
                List<Integer> newTestsMinimizeWithAdditionalAndMaintain = 
                        minimizeWithAdditionalTestsAndMaintainUpperBound(t_i_minimizeWithAdditionalTestsMaintainUpperBound, currentProfile_p, previousProfile_p, riskUpperBound, maxAdditionalTests);
                t_i_minimizeWithAdditionalTestsMaintainUpperBound = merge(newTestsMinimizeWithAdditionalAndMaintain, t_i_minimizeWithAdditionalTestsMaintainUpperBound);
                String addedUbNewTests = Double.toString(newTestsMinimizeWithAdditionalAndMaintain.stream().mapToDouble(Double::valueOf).sum());
                String addedUbAllTests = Double.toString(t_i_minimizeWithAdditionalTestsMaintainUpperBound.stream().mapToDouble(Double::valueOf).sum());
                String addedUbTestsRisk = Double.toString(RiskAnalysisAlgorithmically.computeR(t_i_minimizeWithAdditionalTestsMaintainUpperBound, currentProfile_p));
                
                previousProfile_p = currentProfile_p;
                
                double diff_p = RiskAnalysis.deltaProfile(currentProfile_p, startCity_p);
                ubWriter.write(String.format("%f %d %d %s %s %s %s", diff_p, batchCntStartCity, batchCntEvolveIntoCity, ubNewTests, ubAllTests, ubRisk, bareRiskDiff));
                ubWriter.newLine();
                
                addedWriter.write(String.format("%f %d %d %s %s %s %s", diff_p, batchCntStartCity, batchCntEvolveIntoCity, addedNewTests, addedAllTests, addedNewTestsRisk, bareRiskDiff));
                addedWriter.newLine();

                addedUbWriter.write(String.format("%f %d %d %s %s %s %s", diff_p, batchCntStartCity, batchCntEvolveIntoCity, addedUbNewTests, addedUbAllTests, addedUbTestsRisk, bareRiskDiff));
                addedUbWriter.newLine();

                allInfoWriter.write(String.format("%f %d %d %s %s %s %s %s %s %s %s %s %s", diff_p, batchCntStartCity, batchCntEvolveIntoCity, ubNewTests, addedNewTests, addedUbNewTests, ubAllTests, addedAllTests, addedUbAllTests, ubRisk, addedNewTestsRisk, addedUbTestsRisk, bareRiskDiff));
                allInfoWriter.newLine();
                //TODO debug stuff:
                //--------------------------------------------------------------------
                if(cnt >= 5)
                {
                    System.out.println("stop after 10 iterations in debug-------------------------------------");
                }
                //--------------------------------------------------------------------
            }
            ubWriter.flush();
            ubWriter.close();
            addedWriter.flush();
            addedWriter.close();
            addedUbWriter.flush();
            addedUbWriter.close();
            allInfoWriter.flush();
            allInfoWriter.close();
        }
        catch (IOException  exc) 
        {
            exc.printStackTrace();
        }
    }

    private static List<Integer> merge(List<Integer> additionalTests, List<Integer> currentTests)
    {
        if(currentTests == null)
        {
            return new ArrayList<Integer>(additionalTests);
        }
        else
        {
            return IntStream.range(0, currentTests.size()).map(idx -> currentTests.get(idx) + additionalTests.get(idx)).boxed().collect(Collectors.toList());
        }
    }

    private static List<Integer> maintainUpperBound(List<Integer> t_is_old, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound)
    {
        if(t_is_old == null)
        {
            return RiskAnalysisAlgorithmically.compute_t_iGivenR(currentProfile_p, riskUpperBound);
        }
        else
        {
            double currentRisk = RiskAnalysisAlgorithmically.computeR(t_is_old, currentProfile_p);
            if(currentRisk <= riskUpperBound)
            {
                return IntStream.range(0, currentProfile_p.size()).map(idx -> 0).boxed().collect(Collectors.toList());
            }
            else
            {
                return allocateNeccessaryTests(t_is_old, currentProfile_p, previousProfile_p, riskUpperBound);
            }
        }
    }

    private static List<Integer> allocateNeccessaryTests(List<Integer> t_is_old, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound)
    {
        return RiskAnalysisAlgorithmically.t_iMinimizeTGivenRisk_p_iAndOldt_iDiffOnly(currentProfile_p, t_is_old, riskUpperBound);
    }

    private static List<Integer> minimizeWithAdditionalTests(List<Integer> t_is, List<Double> currentProfile_p, List<Double> previousProfile_p, int maxAdditionalTests)
    {
        return RiskAnalysisAlgorithmically.t_iMinimizeRiskGivenT_p_iAndOldt_iDiffOnly(currentProfile_p, t_is, maxAdditionalTests);
    }

    private static List<Integer> minimizeWithAdditionalTestsAndMaintainUpperBound(List<Integer> t_is, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound, int maxAdditionalTests)
    {
        List<Integer> additionalTestsGivenT = RiskAnalysisAlgorithmically.t_iMinimizeRiskGivenT_p_iAndOldt_iDiffOnly(currentProfile_p, t_is, maxAdditionalTests);
        List<Integer> result = merge(additionalTestsGivenT, t_is);
        List<Integer> additionalTestsGivenUB = RiskAnalysisAlgorithmically.t_iMinimizeTGivenRisk_p_iAndOldt_iDiffOnly(currentProfile_p, result, riskUpperBound);
        result = merge(additionalTestsGivenT, additionalTestsGivenUB);
        return result;
    }
    
    private static HashMap<Integer, Integer> initializeClusterCountsFrom(Map<Trajectory3DSummaryStatistics, List<Integer>> clustering)
    {
        Stream<Entry<Trajectory3DSummaryStatistics, List<Integer>>> entrySetStream = clustering.entrySet().stream();
        Map<Integer, Integer> clusterNrs = entrySetStream.collect(Collectors.toMap(entry -> entry.getKey().getClusterNr(), entry -> entry.getValue().size()));
        return new HashMap<Integer, Integer>(clusterNrs);
    }
}
