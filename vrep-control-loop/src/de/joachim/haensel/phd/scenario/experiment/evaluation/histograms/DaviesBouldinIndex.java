package de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug.DistanceCache;

public class DaviesBouldinIndex
{
    public double evaluate(Map<Integer, double[][]> indexedTrajectories, Map<double[][], List<Integer>> clusters)
    {
        int k = clusters.size();
        ArrayList<double[][]> centroids = new ArrayList<double[][]>(clusters.keySet());
        List<Double> intraClusterDistances = intraClusterDistance(indexedTrajectories, clusters, centroids);
        
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
                    double[][] centroidI = centroids.get(idxI);
                    double[][] centroidJ = centroids.get(idxJ);
                    double centroidDist = DistanceCache.distance(centroidI, centroidJ);
                    distances[idxJ] = ((sigmaI + sigmaJ) / centroidDist);
                }
            }
            double max = Arrays.stream(distances).max().getAsDouble();
            sum += max;
        }
        return sum / ((double)k);
    }

    private List<Double> intraClusterDistance(Map<Integer, double[][]> indexedTrajectories, Map<double[][], List<Integer>> clusters, ArrayList<double[][]> centroids)
    {
        return centroids.stream().map(centroid -> clusterStatistics(clusters.get(centroid), indexedTrajectories)).map(Trajectory3DSummaryStatistics::getAverageDistance).collect(Collectors.toList());
    }
    
    private Trajectory3DSummaryStatistics clusterStatistics(Collection<Integer> trajectoryIndices, Map<Integer, double[][]> indexedTrajectories)
    {
        Stream<double[][]> trajectories = trajectoryIndices.parallelStream().map(idx -> indexedTrajectories.get(idx));
        Trajectory3DSummaryStatistics statistics = 
                trajectories.collect(Trajectory3DSummaryStatistics::new, Trajectory3DSummaryStatistics::accept, Trajectory3DSummaryStatistics::combine);
        return statistics;
    }
}
