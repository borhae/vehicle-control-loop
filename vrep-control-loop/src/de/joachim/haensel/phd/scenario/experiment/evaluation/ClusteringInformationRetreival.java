package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.ClusteringLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;

public class ClusteringInformationRetreival
{
    public static double sqr(double val)
    {
        return val * val;
    }

    public static List<Double> retreiveProfileFrom(Map<Trajectory3DSummaryStatistics, List<Integer>> clustering)
    {
        int sum = clustering.values().stream().mapToInt(trajectoryList -> trajectoryList.size()).sum();
        List<Double> result = clustering.entrySet().stream().mapToDouble(entry -> ((double)entry.getValue().size()/(double)sum)).boxed().collect(Collectors.toList());
        return result;
    }

    public static Map<Trajectory3DSummaryStatistics, List<Integer>> clusterDataSet(Map<Integer, Trajectory3DSummaryStatistics> reversedMap, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering, ArrayList<Integer> dataPoints)
    {
        Map<Trajectory3DSummaryStatistics, List<Integer>> result = 
                dataPoints.stream().map(trajectory -> Map.entry(reversedMap.get(trajectory), trajectory)).collect(Collectors.groupingBy(entry -> entry.getKey(), Collectors.mapping(entry -> entry.getValue(), Collectors.toList())));
        for (Trajectory3DSummaryStatistics center : clustering.keySet())
        {
            if(result.get(center) == null || result.get(center).isEmpty())
            {
                result.put(center, new ArrayList<Integer>());
            }
        }
        return result;
    }
    
    public static Map<Trajectory3DSummaryStatistics, List<Integer>> loadClusteringK(int k)
    {
        String clusterID = "20200120DataLoad0DataUsed0K" + Integer.toString(k) + "Seed1000Iterations100Runs3";
        
        ClusteringLoader clusteringLoader = new ClusteringLoader();
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = clusteringLoader.loadClusters(clusterID);
        return clustering;
    }

    public static Map<Integer, Trajectory3DSummaryStatistics> reverseClustering(Map<Trajectory3DSummaryStatistics, List<Integer>> clustering)
    {
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
        return reversedMap;
    }
    
    public static Collection<? extends Integer> getClusterIndices(Map<Trajectory3DSummaryStatistics, List<Integer>> clustering)
    {
        return clustering.values().stream().flatMap(trajectories -> trajectories.stream()).collect(Collectors.toList());
    }

    public static Map<Trajectory3DSummaryStatistics, List<Integer>> extractForCity(Map<Integer, MongoTrajectory> dbTrajectories, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering, Set<Trajectory3DSummaryStatistics> clusterKeys, String cityName)
    {
        Map<Trajectory3DSummaryStatistics, List<Integer>> resultClustering = new HashMap<Trajectory3DSummaryStatistics, List<Integer>>();
        for (Trajectory3DSummaryStatistics curCenter : clusterKeys)
        {
            List<Integer> luebeckTrajectories = clustering.get(curCenter).stream().filter(idx -> dbTrajectories.get(idx).getName().contains(cityName)).collect(Collectors.toList());
            resultClustering.put(curCenter, luebeckTrajectories);
        }
        return resultClustering;
    }
}
