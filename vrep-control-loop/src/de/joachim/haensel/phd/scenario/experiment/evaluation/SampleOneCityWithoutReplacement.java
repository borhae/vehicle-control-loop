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
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

public class SampleOneCityWithoutReplacement
{
    public static void main(String[] args)
    {
        TrajectoryLoader trajectoryLoader = new TrajectoryLoader();
        Map<Integer, MongoTrajectory> dbTrajectories = trajectoryLoader.loadIndexedDataToMap("data_available_at_2020-01-18");
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = loadClusteringK(200);
        Random randomGen = new MersenneTwister(1001);
        double riskUpperBound = Math.pow(10.0, -5.0);
        double maxTests = 10000.0;
        int nrOfSamples = 10000;
        int sampleSize = 1000;
        runEvolve(dbTrajectories, clustering, randomGen, riskUpperBound, maxTests, "Luebeck", sampleSize, nrOfSamples);
    }

    private static void runEvolve(Map<Integer, MongoTrajectory> dbTrajectories, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering, Random randomGen, double riskUpperBound, double maxTests, String cityName, int sampleSize, int nrOfSamples)
    {
        Map<Integer, Trajectory3DSummaryStatistics> reversedMap = reverseClustering(clustering);
        Map<Trajectory3DSummaryStatistics, List<Integer>> cityClustering = extractForCity(dbTrajectories, clustering, clustering.keySet(), cityName);
    
        List<Integer> cityIndices = new ArrayList<Integer>(getClusterIndices(cityClustering));

        Map<Integer, Integer> clusterNrToClusterSizeLuebeck = cityClustering.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getClusterNr(), entry -> entry.getValue().size()));
        Map<Integer, Integer> clusterCounts = initializeZeroCountClusters(clusterNrToClusterSizeLuebeck);
        
        Collections.shuffle(cityIndices);
        Deque<Integer> singleCityUsableIndices = new LinkedList<Integer>(cityIndices);
        
        try
        {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get("./experimentout.csv"));
            writer.write("R T cnt");
            writer.newLine();
            int sampleCnt = 0;
            for(int cnt = 0; cnt < nrOfSamples; cnt++)
            {
                int marker = 0;
                for(int idx = 0; idx < sampleSize; idx++)
                {
                        if(singleCityUsableIndices.isEmpty())
                        {
                            //refill if empty, shuffle first
                            Collections.shuffle(cityIndices);
                            singleCityUsableIndices.addAll(cityIndices);
                            marker = 15000000;
                        }
                        Integer trajectoryIdx = singleCityUsableIndices.remove();
                        int clusterNr = reversedMap.get(trajectoryIdx).getClusterNr();
                        clusterCounts.put(clusterNr, clusterCounts.get(clusterNr) == null ? 1 : clusterCounts.get(clusterNr) + 1);
                        sampleCnt++;
                }
                List<Double> currentProfile_p = retreiveProfileFromIndexed(clusterCounts);
                List<Double> current_t_is = RiskAnalysis.compute_t_iGivenR(currentProfile_p, riskUpperBound);
                double currentRcurrent_t_is = RiskAnalysis.computeR(current_t_is, currentProfile_p);
                int currentTcurrent_t_is = RiskAnalysis.computeT(current_t_is);
    
                writer.write(String.format("%f %d %d %d", currentRcurrent_t_is, currentTcurrent_t_is, sampleCnt, marker));
                writer.newLine();
                marker = 0;
            }
            writer.flush();
            writer.close();
        } 
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static Map<Integer, Integer> initializeZeroCountClusters(
            Map<Integer, Integer> clusterNrToClusterSizeLuebeck)
    {
        return clusterNrToClusterSizeLuebeck.keySet().stream().sorted().collect(Collectors.toMap(key -> key, key -> 0));
    }
}
