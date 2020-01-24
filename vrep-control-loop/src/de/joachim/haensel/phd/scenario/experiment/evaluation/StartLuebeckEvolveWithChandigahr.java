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
import java.util.stream.Collectors;
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
        double riskUpperBound = Math.pow(10.0, -5.0);
        double maxTests = 10000.0;
//        runEvolve(dbTrajectories, clustering, randomGen, riskUpperBound, maxTests, "Luebeck", "Chandigarh");
        runEvolve(dbTrajectories, clustering, randomGen, riskUpperBound, maxTests, "Chandigarh", "Luebeck");
    }

    private static void runEvolve(Map<Integer, MongoTrajectory> dbTrajectories, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering, Random randomGen, double riskUpperBound, double maxTests, String startCityName, String evolveIntoCityName)
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
        
        double currentRGroundTrutht_is = RiskAnalysis.computeR(t_is_startCity, startCity_p);
        int cntCityStart = 0;
        int cntCityEvolveInto = 0;
        
        try
        {
            String fileName = String.format("./evolve%sTo%s_k%d.csv", startCityName, evolveIntoCityName, clustering.keySet().size());
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
            String headerRow = String.format("r_groundtruth_t_i r_current_t_i T_current_t_i diff_p cnt%1$s cnt%2$s batchCnt%1$s batchCnt%2$s", startCityName, evolveIntoCityName);
            writer.write(headerRow);
            writer.newLine();

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
                        cntCityEvolveInto++;
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
                        cntCityStart++;
                        batchCntStartCity++;
                    }
                }
                List<Double> currentProfile_p = retreiveProfileFromIndexed(clusterCounts);
                List<Double> current_t_is = RiskAnalysis.compute_t_iGivenR(currentProfile_p, riskUpperBound);
                
                currentRGroundTrutht_is = RiskAnalysis.computeR(t_is_startCity, currentProfile_p);
                double currentRcurrent_t_i_s = RiskAnalysis.computeR(current_t_is, currentProfile_p);
                
                int currentTcurrent_t_is = RiskAnalysis.computeT(current_t_is);
                double diff_p = RiskAnalysis.deltaProfile(currentProfile_p, startCity_p);
                writer.write(String.format("%f %f %d %f %d %d %d %d", currentRGroundTrutht_is, currentRcurrent_t_i_s, currentTcurrent_t_is, diff_p, cntCityStart, cntCityEvolveInto, batchCntStartCity, batchCntEvolveIntoCity));
                writer.newLine();
            }
        }
        catch (IOException  exc) 
        {
            exc.printStackTrace();
        }
    }

    private static HashMap<Integer, Integer> initializeClusterCountsFrom(Map<Trajectory3DSummaryStatistics, List<Integer>> clustering)
    {
        Stream<Entry<Trajectory3DSummaryStatistics, List<Integer>>> entrySetStream = clustering.entrySet().stream();
        Map<Integer, Integer> clusterNrs = entrySetStream.collect(Collectors.toMap(entry -> entry.getKey().getClusterNr(), entry -> entry.getValue().size()));
        return new HashMap<Integer, Integer>(clusterNrs);
    }
}
