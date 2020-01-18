package de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.joachim.haensel.phd.scenario.experiment.evaluation.KMeansClusterer;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

public class KMeansClusterAndStore implements Runnable
{
    public static void main(String[] args) 
    {
        Thread runner = new Thread(new KMeansClusterAndStore());
        runner.run();
    }
    
    @Override
    public void run()
    {
        TrajectoryLoader loader = new TrajectoryLoader();
        List<double[][]> trajectoryArrrays = loader.loadData(true, true, 1000, 5000);
        
        int maxK = 110;
        int minK = 10;
        int stepWidth = 20;
        
        for(int k = minK; k <= maxK; k += stepWidth)
        {
            double minRunAverageDistance = Double.POSITIVE_INFINITY;
            double minRunAverageDistanceVariance = 0.0;
            Map<Trajectory3DSummaryStatistics, List<double[][]>> minRunResult = null;
            for(int runNr = 0; runNr < 3; runNr++)
            {
                int seed = runNr + 1000;
                MersenneTwister randomGen = new MersenneTwister(seed);
                KMeansClusterer clusterer = new KMeansClusterer(trajectoryArrrays, 150, randomGen, 100);
                Map<Trajectory3DSummaryStatistics, List<double[][]>> result = clusterer.cluster();
                Set<Trajectory3DSummaryStatistics> centerStatistics = result.keySet();
                double averageDistance = centerStatistics.stream().mapToDouble(centerStatistic -> centerStatistic.getAverageDistance()).sum() / centerStatistics.size();
                double averageDistanceVariance = centerStatistics.stream().mapToDouble(centerStatistic -> centerStatistic.getAverageDistanceVariance()).sum() / centerStatistics.size();
                if(averageDistance < minRunAverageDistance)
                {
                    minRunAverageDistance = averageDistance;
                    minRunAverageDistanceVariance = averageDistanceVariance;
                    minRunResult = result;
                }
            }
            KMeansState state = new KMeansState(k, minRunResult.keySet().size(), minRunAverageDistance, minRunAverageDistanceVariance);
        }
        
    }

}
