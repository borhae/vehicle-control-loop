package de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        TrajectoryLoader trajectoryLoader = new TrajectoryLoader();
        Map<Integer, MongoTrajectory> dbTrajectories = trajectoryLoader.loadIndexedDataToMap("data_available_at_2020-01-18");
        List<Integer> luebeckIndices = getIndicesForCity(dbTrajectories, "Luebeck");
        List<Integer> chandigarhIndices = getIndicesForCity(dbTrajectories, "Chandigarh");
        
        ClusteringLoader clusteringLoader = new ClusteringLoader();
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = clusteringLoader.loadClusters("20200120DataLoad0DataUsed0K10Seed1000Iterations100Runs3");
        Set<Trajectory3DSummaryStatistics> clusterKeys = clustering.keySet();

        Map<Trajectory3DSummaryStatistics, List<Integer>> clusteringLuebeck = extractForCity(dbTrajectories, clustering, clusterKeys, "Luebeck");
        
        Map<Trajectory3DSummaryStatistics, List<Integer>> clusteringChandigarh = extractForCity(dbTrajectories, clustering, clusterKeys, "Chandigarh");
        
        ArrayList<Trajectory3DSummaryStatistics> centersNumbered = new ArrayList<Trajectory3DSummaryStatistics>(clusterKeys);
        centersNumbered.sort((a, b) -> Integer.compare(a.getClusterNr(), b.getClusterNr()));
        
        int sumOfTrajectories = dbTrajectories.size();
        List<Integer> bins = new ArrayList<Integer>();
        for(int idx = 0; idx < centersNumbered.size(); idx++)
        {
            List<Integer> members = clustering.get(centersNumbered.get(idx));
            int binCount = members.size();
            bins.add(binCount);
        }
        List<Double> probabilities = bins.parallelStream().mapToDouble(binCount -> ((double)binCount/(double)sumOfTrajectories)).boxed().collect(Collectors.toList());
        int T = computeT(probabilities, Math.pow(10.0, -6.0));
        
//        StringBuilder result = new StringBuilder();
//        int luebeckSum = 0;
//        int chandigarhSum = 0;
//        for (Trajectory3DSummaryStatistics curCenter : centersNumbered)
//        {
//            int luebeckSize = clusteringLuebeck.get(curCenter).size();
//            int chandigarhSize = clusteringChandigarh.get(curCenter).size();
//            String row = String.format("%d %d %d\n", curCenter.getClusterNr(), luebeckSize, chandigarhSize);
//            result.append(row);
//            luebeckSum += luebeckSize;
//            chandigarhSum += chandigarhSize;
//        }
//        System.out.println(result.toString());
//        System.out.format("Luebeck: %d samples, Chandigarh: %d samples\n", luebeckSum, chandigarhSum);
    }

    private int computeT(List<Double> probabilities, double R)
    {
        double sumOfp_iSquareroot = probabilities.stream().mapToDouble(p_i -> Math.sqrt(p_i)).sum();
        List<Double> t_is = probabilities.stream().mapToDouble(p_i -> ((Math.sqrt(p_i) / R) * sumOfp_iSquareroot)).boxed().collect(Collectors.toList());
        for(int idx = 0; idx < t_is.size(); idx++)
        {
            double t_i = t_is.get(idx);
//            if(t_i > )
        }
        return 10;
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
