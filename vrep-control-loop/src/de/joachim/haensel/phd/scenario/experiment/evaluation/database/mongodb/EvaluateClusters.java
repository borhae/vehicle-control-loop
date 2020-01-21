package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import java.util.List;
import java.util.Map;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.ClusteringLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.histograms.DaviesBouldinIndex;
import de.joachim.haensel.phd.scenario.experiment.evaluation.histograms.DunnIndex;

public class EvaluateClusters
{
    public static void main(String[] args)
    {
        TrajectoryLoader loader = new TrajectoryLoader();
        Map<Integer, double[][]> indexedTrajectories = loader.loadIndexedTrajectoriesToMap("data_available_at_2020-01-18");
        ClusteringLoader clusterLoader = new ClusteringLoader();
        String[] setIDs = new String[] {"10", "20", "30", "40", "60", "80", "100", "120", "140", "160", "180", "200", "1000"};
        StringBuilder summary = new StringBuilder();
        summary.append("Clustersize Davies-Bouldin-Index Dunn-Index: \n");
        for(int idx = 0; idx < setIDs.length; idx++)
        {
            Map<Trajectory3DSummaryStatistics, List<Integer>> clusters = clusterLoader.loadClusters("20200120DataLoad0DataUsed0K"+ setIDs[idx] + "Seed1000Iterations100Runs3");
            
            DaviesBouldinIndex daviesBouldinEvaluator = new DaviesBouldinIndex();
            DunnIndex dunnEvaluator = new DunnIndex();
            
            double daviesBouldinIndex = daviesBouldinEvaluator.evaluate(indexedTrajectories, clusters);
            double dunnIndex  = dunnEvaluator.evaluate(indexedTrajectories, clusters);
            summary.append(String.format("%s %f %f\n", setIDs[idx], daviesBouldinIndex, dunnIndex));
        }
        System.out.println(summary.toString());
    }
}
