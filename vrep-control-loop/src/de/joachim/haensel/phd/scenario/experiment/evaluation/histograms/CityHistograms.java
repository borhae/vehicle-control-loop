package de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.ClusteringLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;

public class CityHistograms
{
    public static void main(String[] args)
    {
        TrajectoryLoader trajectoryLoader = new TrajectoryLoader();
        Map<Integer, MongoTrajectory> dbTrajectories = trajectoryLoader.loadIndexedDataToMap("data_available_at_2020-01-18");
        
        ClusteringLoader clusteringLoader = new ClusteringLoader();
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = clusteringLoader.loadClusters("20200120DataLoad0DataUsed0K10Seed1000Iterations100Runs3");
        Set<Trajectory3DSummaryStatistics> clusterKeys = clustering.keySet();

        Map<Trajectory3DSummaryStatistics, List<Integer>> clusteringLuebeck = extractForCity(dbTrajectories, clustering, clusterKeys, "Luebeck");
        Map<Trajectory3DSummaryStatistics, List<Integer>> clusteringChandigarh = extractForCity(dbTrajectories, clustering, clusterKeys, "Chandigarh");
        ArrayList<Trajectory3DSummaryStatistics> centersNumbered = new ArrayList<Trajectory3DSummaryStatistics>(clusterKeys);
//        centersNumbered.sort((a, b) -> Integer.compare(a.getClusterNr(), b.getClusterNr()));
        centersNumbered.sort((a, b) -> sortFrequenciess(a, b, clustering));
//        centersNumbered.sort((a, b) -> sortNew(a, b));
        StringBuilder result = new StringBuilder();
        int luebeckSum = 0;
        int chandigarhSum = 0;
        for (Trajectory3DSummaryStatistics curCenter : centersNumbered)
        {
            int luebeckSize = clusteringLuebeck.get(curCenter).size();
            int chandigarhSize = clusteringChandigarh.get(curCenter).size();
            String row = String.format("%d %d %d\n", curCenter.getClusterNr(), luebeckSize, chandigarhSize);
            result.append(row);
            luebeckSum += luebeckSize;
            chandigarhSum += chandigarhSize;
        }
        System.out.println(result.toString());
        System.out.format("Luebeck: %d samples, Chandigarh: %d samples\n", luebeckSum, chandigarhSum);
    }

    private static int sortNew(Trajectory3DSummaryStatistics a, Trajectory3DSummaryStatistics b)
    {
        double[][] centerA = a.getAverage();
        double[][] centerB = b.getAverage();
        int trajectoryLength = centerA.length;
        if(trajectoryLength != centerB.length)
        {
            System.out.println("what? inequal length!");
            return 0;
        }
        double sumXA = 0.0;
        double sumXB = 0.0;
        double sumYA = 0.0;
        double sumYB = 0.0;
        double sumZA = 0.0;
        double sumZB = 0.0;
        for(int idx = 0; idx < trajectoryLength; idx++)
        {
            double[] pa = centerA[idx];
            double[] pb = centerB[idx];
            sumXA += pa[0];
            sumXB += pb[0];
            sumYA += pa[1];
            sumYB += pb[1];
            sumZA += pa[2];
            sumZB += pb[2];
        }
        int compareY = compareDouble(sumYA, sumYB, 10);
        if(compareY == 0)
        {
            int compareX = compareDouble(sumXA, sumXB, 10);
            if(compareX == 0)
            {
                return Double.compare(sumZA, sumZB);
            }
            else
            {
                return compareX;
            }
        }
        else
        {
            return compareY;
        }
    }
    
    public static int compareDouble(double d1, double d2, double epsilon)
    {
        if(Math.abs(d1 - d2) < epsilon)
        {
            return 0;
        }
        else
        {
            return Double.compare(d1, d2);
        }
    }

    /**
     * Sort over overall frequencies
     * @param a
     * @param b
     * @param clustering 
     * @return
     */
    private static int sortFrequenciess(Trajectory3DSummaryStatistics a, Trajectory3DSummaryStatistics b, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering)
    {
        int sizeA = clustering.get(a).size();
        int sizeB = clustering.get(b).size();
        return Integer.compare(sizeA, sizeB);
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
