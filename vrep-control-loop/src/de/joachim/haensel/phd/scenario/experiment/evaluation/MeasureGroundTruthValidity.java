package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.*;

public class MeasureGroundTruthValidity
{
    public static void main(String[] args)
    {
        TrajectoryLoader trajectoryLoader = new TrajectoryLoader();
        Map<Integer, MongoTrajectory> dbTrajectories = trajectoryLoader.loadIndexedDataToMap("data_available_at_2020-01-18");
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = loadClusteringK(10);
        Map<Integer, Trajectory3DSummaryStatistics> reversedMap = 
                clustering.entrySet().stream().flatMap
                (
                        entry -> entry.getValue().stream().map
                        (
                                trajectory -> Map.entry
                                (
                                        trajectory, entry.getKey()
                                ) 
                        )
                ).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        
        MersenneTwister randomGen = new MersenneTwister(1001);
        
        List<Integer> trajectoryIndices = new ArrayList<Integer>(dbTrajectories.keySet());
        int sampleSize = trajectoryIndices.size();
        int maxRange = 1000;
        List<Integer> percentages = 
                IntStream.rangeClosed(1, maxRange).map(val -> val * (sampleSize/maxRange)).boxed().collect(Collectors.toList());
        System.out.println("count delta");
        for(int cnt = 0; cnt < maxRange - 1; cnt++)
        {
            ArrayList<Integer> data_i = new ArrayList<Integer>(trajectoryIndices.subList(0, percentages.get(cnt)));
            ArrayList<Integer> data_i_prime = new ArrayList<Integer>(trajectoryIndices.subList(0, percentages.get(cnt + 1)));
            
            Map<Trajectory3DSummaryStatistics, List<Integer>> data_i_cluster = clusterDataSet(reversedMap, clustering, data_i);
            Map<Trajectory3DSummaryStatistics, List<Integer>> data_i_prime_cluster = clusterDataSet(reversedMap, clustering, data_i_prime);
            
            List<Double> data_i_p = retreiveProfileFrom(data_i_cluster);
            List<Double> data_i_prime_p = retreiveProfileFrom(data_i_prime_cluster);
            
            if(data_i_p.size() != data_i_prime_p.size())
            {
                System.out.println("why? they should be of the same size");
                return;
            }
            double delta = IntStream.range(0, data_i_p.size()).mapToDouble(idx -> sqr(data_i_p.get(idx) - data_i_prime_p.get(idx))).sum();
            System.out.println(cnt + " " + delta);
        }
    }
}
