package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug.DistanceCache;

public class KMeansClusterer
{
    private List<double[][]> _trajectories;
    private int _k;
    private Random _randomGenerator;
    private int _maxIterations;

    public KMeansClusterer(List<double[][]> trajectories, int k, Random randomGenerator, int maxIterations)
    {
        _trajectories = trajectories;
        _k = k;
        _randomGenerator = randomGenerator;
        _maxIterations = maxIterations;
    }

    public void setData(List<double[][]> trajectories)
    {
        _trajectories = trajectories;
    }

    public void setRandomGenerator(Random randomGenerator)
    {
        _randomGenerator = randomGenerator;
    }

    public void setK(int k)
    {
        _k = k;
    }

    public void setMaxIterations(int maxIterations)
    {
        _maxIterations = maxIterations;
    }

    /**
     * Does the actual k-means clustering.
     * @return The clusters in the form: Map&lt;cluster centers and statistics, List&lt;trajectories of each cluster&gt;&gt;
     */ 
    public Map<Trajectory3DSummaryStatistics, List<double[][]>> cluster()
    {
        return cluster(_trajectories, _k, _maxIterations, _randomGenerator);
    }
    
    /**
     * Does the actual k-means clustering. 
     * @param allTrajectories The inputspace of all trajectories to be clustered
     * @param numOfClusters The desired number of clusters (k)
     * @param maxIters The maximum number of iterations
     * @param randomGen A random number generator
     * @return The clusters in the form: Map&lt;cluster centers and statistics, List&lt;trajectories of each cluster&gt;&gt;
     */
    public Map<Trajectory3DSummaryStatistics, List<double[][]>> cluster(List<double[][]> allTrajectories, int numOfClusters, int maxIters, Random randomGen)
    {
        List<double[][]> centers0 = createInitialCenters(allTrajectories, numOfClusters, randomGen);
        List<Trajectory3DSummaryStatistics> initialSummaries = centers0.parallelStream().map(center -> new Trajectory3DSummaryStatistics(center)).collect(Collectors.toList());
        
        Map<Trajectory3DSummaryStatistics, List<double[][]>> curState = assignToCenters(allTrajectories, initialSummaries);
        for(int cnt = 0; cnt < maxIters; cnt++)
        {
            System.out.format("%d", cnt % 10);
            List<Trajectory3DSummaryStatistics> clusterStatistics = findNewCenters(curState);
            curState = assignToCenters(allTrajectories, clusterStatistics);
        }
        return curState;
    }

    private Map<Trajectory3DSummaryStatistics, List<double[][]>> assignToCenters(List<double[][]> allTrajectories, List<Trajectory3DSummaryStatistics> centers)
    {
        //make sure that all averages are computed before clustering
        centers.parallelStream().map(center -> center.getAverage()).collect(Collectors.toList());
        Map<Trajectory3DSummaryStatistics, List<double[][]>> result = 
                allTrajectories.parallelStream().collect(Collectors.groupingBy(trajectory -> closestCenter(trajectory, centers)));
        return result;
    }
    
    private Trajectory3DSummaryStatistics closestCenter(double[][] trajectory, List<Trajectory3DSummaryStatistics> centers)
    {
        double curMin = Double.MAX_VALUE;
        Trajectory3DSummaryStatistics result = centers.get(0);
        for(int centersIdx = 0; centersIdx < centers.size(); centersIdx++)
        {
            Trajectory3DSummaryStatistics curCenter = centers.get(centersIdx);
            double[][] centerTrajectory = curCenter.getAverage();
            double distance = DistanceCache.distance(centerTrajectory, trajectory);
            if(distance < curMin)
            {
                curMin = distance;
                result = curCenter;
            }
        }
        return result;
    }
    
    private List<Trajectory3DSummaryStatistics> findNewCenters(Map<Trajectory3DSummaryStatistics, List<double[][]>> cluster)
    {
        List<Trajectory3DSummaryStatistics> result = cluster.entrySet().parallelStream().map(entry -> findCenter(entry.getValue())).collect(Collectors.toList());
        return result;
    }

    private Trajectory3DSummaryStatistics findCenter(List<double[][]> trajectoriesInCluster)
    {
        Trajectory3DSummaryStatistics statistics = 
                trajectoriesInCluster.parallelStream().collect(Trajectory3DSummaryStatistics::new, Trajectory3DSummaryStatistics::accept, Trajectory3DSummaryStatistics::combine);
        return statistics;
    }

    private List<double[][]> createInitialCenters(List<double[][]> allTrajectories, int numberOfClusters, Random randomGen)
    {
        List<double[][]> randomRoot = new ArrayList<double[][]>(allTrajectories);
        Collections.shuffle(randomRoot, randomGen);
        return randomRoot.subList(0, numberOfClusters);
    }
}
