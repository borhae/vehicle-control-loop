package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import java.util.List;
import java.util.Map;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.ClusteringLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.histograms.DaviesBouldinIndex;

public class EvaluateClusters
{
    public static void main(String[] args)
    {
        TrajectoryLoader loader = new TrajectoryLoader();
        Map<Integer, double[][]> indexedTrajectories = loader.loadIndexedDataToMap("data_available_at_2020-01-18");
        ClusteringLoader clusterLoader = new ClusteringLoader();
        String[] setIDs = new String[] {"20", "200", "1000"};
        for(int idx = 0; idx < setIDs.length; idx++)
        {
            Map<double[][], List<Integer>> clusters = clusterLoader.loadClusters("DataLoad0DataUsed0K"+ setIDs[idx] + "Seed1000Iterations100Runs3");
            DaviesBouldinIndex metric = new DaviesBouldinIndex();
            double result = metric.evaluate(indexedTrajectories, clusters);
            System.out.format("Clustersize: %s, Davies-Bouldin-Index: %f\n", setIDs[idx], result);
        }
    }
}
