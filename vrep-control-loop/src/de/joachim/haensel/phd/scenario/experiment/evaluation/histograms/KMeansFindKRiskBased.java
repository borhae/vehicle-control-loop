package de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.joachim.haensel.phd.scenario.experiment.evaluation.RiskAnalysis;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.ClusteringLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;

public class KMeansFindKRiskBased implements Runnable
{
    public static void main(String[] args) 
    {
        Thread runner = new Thread(new KMeansFindKRiskBased());
        runner.run();
    }
    
    @Override
    public void run()
    {
        List<Integer> intRange = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());
        List<Double> doubleRange = intRange.stream().mapToDouble(i -> (i)).boxed().collect(Collectors.toList());
        List<Double> rValues = doubleRange.stream().map(d -> d * Math.pow(10.0, -6.0)).collect(Collectors.toList());
        rValues.forEach(s -> System.out.format("%.10f\n", s));
        TrajectoryLoader trajectoryLoader = new TrajectoryLoader();
        Map<Integer, MongoTrajectory> dbTrajectories = trajectoryLoader.loadIndexedDataToMap("data_available_at_2020-01-18");
        
        int[] ks = new int[]
                {
                  10,
                  20,
                  30,
                  40,
                  60,
                  80,
                  100,
                  120,
                  140,
                  160,
                  180,
                  200,
                  1000
                };

        StringBuilder result = new StringBuilder();
        result.append("k R t_i-sum\n");
        for(int idx = 0; idx < ks.length; idx++)
        {
            int k = ks[idx];
            Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = loadClusterK(k);

            List<String> runResult = 
                    rValues.stream().map(r -> computeTforRAndP_iFromClustering(r, dbTrajectories.size(), k, clustering)).collect(Collectors.toList());
            
            result.append(runResult.stream().collect(Collectors.joining()));
        }
        System.out.println(result.toString());
    }

    private Map<Trajectory3DSummaryStatistics, List<Integer>> loadClusterK(int k)
    {
        String clusterID = "20200120DataLoad0DataUsed0K" + Integer.toString(k) + "Seed1000Iterations100Runs3";
        
        ClusteringLoader clusteringLoader = new ClusteringLoader();
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = clusteringLoader.loadClusters(clusterID);
        return clustering;
    }

    private String computeTforRAndP_iFromClustering(double R, int dataSetSize, int k, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering)
    {
        Set<Trajectory3DSummaryStatistics> clusterKeys = clustering.keySet();

        ArrayList<Trajectory3DSummaryStatistics> centersNumbered = new ArrayList<Trajectory3DSummaryStatistics>(clusterKeys);
        centersNumbered.sort((a, b) -> Integer.compare(a.getClusterNr(), b.getClusterNr()));
        
        List<Integer> bins = new ArrayList<Integer>();
        for(int idx = 0; idx < centersNumbered.size(); idx++)
        {
            List<Integer> members = clustering.get(centersNumbered.get(idx));
            int binCount = members.size();
            bins.add(binCount);
        }
        List<Double> probabilities = bins.parallelStream().mapToDouble(binCount -> ((double)binCount/(double)dataSetSize)).boxed().collect(Collectors.toList());
        double p_sum = probabilities.stream().collect(Collectors.summarizingDouble(Double::valueOf)).getSum();
        List<Integer> t_is = RiskAnalysis.computeT(probabilities, R);
        Integer T = t_is.stream().collect(Collectors.summingInt(Integer::intValue));
        String result = String.format("%d %.10f %d\n", k, R, T);
        return result;
    }

    private List<Integer> getIndicesForCity(Map<Integer, MongoTrajectory> dbTrajectories, String cityName)
    {
        return IntStream.range(0, dbTrajectories.size()).filter(idx -> dbTrajectories.get(idx).getName().contains(cityName)).boxed().collect(Collectors.toList());
    }
    
    private static Map<Trajectory3DSummaryStatistics, List<Integer>> extractForCity(Map<Integer, MongoTrajectory> dbTrajectories, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering, Set<Trajectory3DSummaryStatistics> clusterKeys, String cityName)
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
