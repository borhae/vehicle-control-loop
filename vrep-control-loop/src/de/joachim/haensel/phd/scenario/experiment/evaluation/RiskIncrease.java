package de.joachim.haensel.phd.scenario.experiment.evaluation;

import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.extractForCity;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.getClusterIndices;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.loadClusteringK;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.retreiveProfileFrom;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.reverseClustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

public class RiskIncrease
{
    public static void main(String[] args)
    {
        TrajectoryLoader trajectoryLoader = new TrajectoryLoader();
        Map<Integer, MongoTrajectory> dbTrajectories = trajectoryLoader.loadIndexedDataToMap("data_available_at_2020-01-18");
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = loadClusteringK(10);
        Map<Trajectory3DSummaryStatistics, List<Integer>> luebeckClustering = extractForCity(dbTrajectories, clustering, clustering.keySet(), "Luebeck");
        
        Map<Integer, Trajectory3DSummaryStatistics> reversedMap = reverseClustering(clustering);
        
        List<Integer> luebeckIndices = new ArrayList<Integer>(getClusterIndices(luebeckClustering));
        MersenneTwister randomGen = new MersenneTwister(1001);
        Collections.shuffle(luebeckIndices, randomGen);
        int sampleSize = luebeckIndices.size();
        int maxRange = 1000;
        List<Integer> percentages = 
                IntStream.rangeClosed(1, maxRange).map(val -> val * (sampleSize/maxRange)).boxed().collect(Collectors.toList());
        riskDevelopment(clustering, luebeckClustering, reversedMap, luebeckIndices, maxRange, percentages);
    }

    private static void riskDevelopment(Map<Trajectory3DSummaryStatistics, List<Integer>> clustering, Map<Trajectory3DSummaryStatistics, List<Integer>> luebeckClustering, Map<Integer, Trajectory3DSummaryStatistics> reversedMap, List<Integer> luebeckIndices, int maxRange,List<Integer> percentages)
    {
//        List<Double> data_groundTruth_p = retreiveProfileFrom(luebeckClustering);
        List<Double> data_groundTruth_p = retreiveProfileFrom(clustering);
        System.out.println("ground truth p");
        data_groundTruth_p.forEach(p_i -> System.out.println(p_i));
        int T = 1000;
        List<Double> t_is_groundTruth = RiskAnalysis.compute_t_iGivenT(data_groundTruth_p, T);
        System.out.println("t_i ground truth");
        t_is_groundTruth.forEach(t_i -> System.out.println(t_i));
        double groundTruthR = RiskAnalysis.computeR(t_is_groundTruth, data_groundTruth_p);
        System.out.println("Testing based on ground truth with " + T + " tests: " + groundTruthR);
        
        int clusterSize = clustering.keySet().size();
        MersenneTwister randomGen = new MersenneTwister(1001);
        List<Double> randomNums = IntStream.range(0, clusterSize).mapToDouble(idx -> randomGen.nextDouble(true, true)).boxed().collect(Collectors.toList());
        double sum = randomNums.stream().mapToDouble(Double::valueOf).sum();
        List<Double> random_p_is = randomNums.stream().map(val -> val / sum).collect(Collectors.toList());
        List<Double> random_t_is = RiskAnalysis.compute_t_iGivenT(random_p_is, T);
        System.out.println("random p_is");
        random_p_is.forEach(val -> System.out.println(val));
        System.out.println("random t_is");
        random_t_is.forEach(val -> System.out.println(val));

        
        double uniform_p = 1.0 / (double)clusterSize;
        List<Double> uniform_p_is = IntStream.range(0, clusterSize).mapToDouble(idx -> uniform_p).boxed().collect(Collectors.toList());
        System.out.println("uniform p");
        uniform_p_is.forEach(p_i -> System.out.println(p_i));
        List<Double> t_is_uniform = RiskAnalysis.compute_t_iGivenT(uniform_p_is, T);
        System.out.println("t_i uniform");
        t_is_uniform.forEach(t_i -> System.out.println(t_i));
        double uniR = RiskAnalysis.computeR(t_is_uniform, uniform_p_is);
        System.out.println("Testing based on t_i computed from uniform distribution with " + T + 
                " tests applied to profile uniform distribution result in risk: " + uniR);
        
        double diffR = RiskAnalysis.computeR(t_is_uniform, data_groundTruth_p);
        System.out.println("Testing based on t_i computed from uniform distribution with " + T + 
                " tests but applied to profile of ground truth results in risk: " + diffR);

        
//        for(int cnt = 0; cnt < maxRange; cnt++)
//        {
//            ArrayList<Integer> data_i = new ArrayList<Integer>(luebeckIndices.subList(0, percentages.get(cnt)));
//            
//            Map<Trajectory3DSummaryStatistics, List<Integer>> data_i_cluster = clusterDataSet(reversedMap, luebeckClustering, data_i);
//            
//            List<Double> data_i_p = retreiveProfileFrom(data_i_cluster);
//            
//            if(data_i_p.size() != data_groundTruth_p.size())
//            {
//                System.out.println("why? they should be of the same size");
//                return;
//            }
//            double delta = IntStream.range(0, data_i_p.size()).mapToDouble(idx -> sqr(data_i_p.get(idx) - data_groundTruth_p.get(idx))).sum();
//            double deltaUniform = IntStream.range(0, data_i_p.size()).mapToDouble(idx -> sqr(data_i_p.get(idx) - uniform_p)).sum();
//            
//            System.out.println(cnt + " " + delta + " " + deltaUniform);
//        }
    }
}
