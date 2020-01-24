package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        List<Trajectory3DSummaryStatistics> centers = new ArrayList<Trajectory3DSummaryStatistics>(clustering.keySet());
        Collections.sort(centers, (a, b) -> compare(a, b));
        List<Double> result = centers.stream().mapToDouble(center -> ((double)clustering.get(center).size()/(double)sum)).boxed().collect(Collectors.toList());
        return result;
    }
    
    public static List<Double> retreiveProfileFromIndexed(Map<Integer, Integer> clusterCounts)
    {
        ArrayList<Integer> clusterNrs = new ArrayList<Integer>(clusterCounts.keySet());
        Collections.sort(clusterNrs);
        int sum = clusterCounts.values().stream().mapToInt(Integer::valueOf).sum();
        List<Double> result = 
                clusterNrs.stream().mapToDouble(clusterNr -> (((double)clusterCounts.get(clusterNr))/((double)sum))).boxed().collect(Collectors.toList());
        return result;
    }

    private static int compare(Trajectory3DSummaryStatistics a, Trajectory3DSummaryStatistics b)
    {
        return Integer.compare(a.getClusterNr(), b.getClusterNr());
    }

    public static Map<Trajectory3DSummaryStatistics, List<Integer>> clusterDataSet(Map<Integer, Trajectory3DSummaryStatistics> reversedMap, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering, List<Integer> dataPoints)
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
