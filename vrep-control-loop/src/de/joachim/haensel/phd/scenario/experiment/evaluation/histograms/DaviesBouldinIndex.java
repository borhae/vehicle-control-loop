package de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug.DistanceCache;

public class DaviesBouldinIndex
{
    public double evaluate(Map<Integer, double[][]> indexedTrajectories, Map<Trajectory3DSummaryStatistics, List<Integer>> clusters)
    {
        int k = clusters.size();
        ArrayList<Trajectory3DSummaryStatistics> centroids = new ArrayList<Trajectory3DSummaryStatistics>(clusters.keySet());
        centroids.sort((a, b) -> Integer.compare(a.getClusterNr(), b.getClusterNr()));
        List<double[][]> centerArrays = centroids.stream().map(statistic -> statistic.getAverage()).collect(Collectors.toList());
        List<Double> intraClusterDistances = intraClusterDistance(indexedTrajectories, clusters, centroids);
        try
        {
            String content = intraClusterDistances.stream().map(String::valueOf).collect(Collectors.joining("\n"));
            Files.writeString(Path.of("intraclusterDistance"+k), content, StandardOpenOption.CREATE);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        double sum = 0.0;
        for(int idxI = 0; idxI < k; idxI++)
        {
            double[] distances = new double[k];
            for(int idxJ = 0; idxJ < k; idxJ++)
            {
                if(idxI != idxJ)
                {
                    double sigmaI = intraClusterDistances.get(idxI);
                    double sigmaJ = intraClusterDistances.get(idxJ);
                    double[][] centroidI = centerArrays.get(idxI);
                    double[][] centroidJ = centerArrays.get(idxJ);
                    double centroidDist = DistanceCache.distance(centroidI, centroidJ);
                    distances[idxJ] = ((sigmaI + sigmaJ) / centroidDist);
                }
            }
            double max = Arrays.stream(distances).max().getAsDouble();
            sum += max;
        }
        return sum / ((double)k);
    }

    private List<Double> intraClusterDistance(Map<Integer, double[][]> indexedTrajectories, Map<Trajectory3DSummaryStatistics, List<Integer>> clusters, ArrayList<Trajectory3DSummaryStatistics> centroids)
    {
        List<Double> result = new ArrayList<Double>();
        for(int idxI = 0; idxI < centroids.size(); idxI++)
        {
            Trajectory3DSummaryStatistics centroid = centroids.get(idxI);
            List<Integer> memberIndices = clusters.get(centroid);
            List<double[][]> members = new ArrayList<double[][]>();
            for(int idxJ = 0; idxJ < memberIndices.size(); idxJ++)
            {
                members.add(indexedTrajectories.get(memberIndices.get(idxJ)));
            }
            double average = averageDistanceInCluster(centroid.getAverage(), members);
//            double alternative = centroid.getAverageDistance();
            result.add(average);
        }
        return result;
    }

    private double averageDistanceInCluster(double[][] centroid, List<double[][]> members)
    {
        double sum = 0.0;
        for(int idxJ  = 0; idxJ < members.size(); idxJ++)
        {
            sum += DistanceCache.distance(members.get(idxJ), centroid);
        }
        double average = sum / (double)members.size();
        return average;
    }
}
