package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.ClusteringLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;

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
}
