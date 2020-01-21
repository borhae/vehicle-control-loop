package de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

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
        int limitLoad = 0;
        int limitUse = 0;

        TrajectoryLoader loader = new TrajectoryLoader();
        Map<Integer, double[][]> indexedTrajectories = loader.loadIndexedTrajectoriesToMap("data_available_at_2020-01-18");
        Map<double[][], Integer> lookupMap = indexedTrajectories.entrySet().stream().collect(Collectors.toMap(entry -> entry.getValue(), entry -> entry.getKey()));
        
        MongoClient mongoClient = openDBToWrite();
        
        int seedBase = 1000;
        int maxIterations = 100;
        int runs = 3;
        
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
                        200
                };

        for(int idxK = 0; idxK < ks.length; idxK++)
        {
            int k = ks[idxK];
            //for a deterministic clustering (seed and data) data has to be in deterministic order 
            List<double[][]> trajectories = 
                    IntStream.range(0, indexedTrajectories.values().size()).boxed().map(idx -> indexedTrajectories.get(idx)).collect(Collectors.toList());
            Map<Trajectory3DSummaryStatistics, List<double[][]>> minRunResult = cluster(limitLoad, limitUse, k, seedBase, maxIterations, runs, trajectories, mongoClient);
            String collectionName = String.format("20200120DataLoad%dDataUsed%dK%dSeed%dIterations%dRuns%d", limitLoad, limitUse, k, seedBase, maxIterations, runs);
            storeToDatabase(mongoClient, minRunResult, collectionName, lookupMap);
        }

        mongoClient.close();
    }

    private MongoClient openDBToWrite()
    {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        MongoClient mongoClient = MongoClients.create(settings);
        return mongoClient;
    }

    private Map<Trajectory3DSummaryStatistics, List<double[][]>> cluster(int limitLoad, int limitUse, int k, int seedBase, int maxIterations, int runs, List<double[][]> trajectoryArrays, MongoClient mongoClient)
    {
        KMeansClusterer clusterer = new KMeansClusterer(trajectoryArrays, k, new MersenneTwister(), maxIterations);
        Map<Trajectory3DSummaryStatistics, List<double[][]>> minRunResult = clusterer.cluster(seedBase, runs);
        return minRunResult;
    }

    private void storeToDatabase(MongoClient mongoClient, Map<Trajectory3DSummaryStatistics, List<double[][]>> minRunResult, String collectionName, Map<double[][], Integer> lookUpMap)
    {
        List<KMeansClassification> databaseEntries = minRunResult.entrySet().stream().map(entry -> createDBEntry(entry, lookUpMap)).collect(Collectors.toList());
        
        MongoDatabase database = mongoClient.getDatabase("k-means-clusterings");
        MongoCollection<KMeansClassification> collection = database.getCollection(collectionName, KMeansClassification.class);
        collection.insertMany(databaseEntries);
    }

    private KMeansClassification createDBEntry(Entry<Trajectory3DSummaryStatistics, List<double[][]>> entry, Map<double[][], Integer> lookUpMap)
    {
        KMeansClassification result = new KMeansClassification();
        result.setClassifier(toDoubleList(entry.getKey().getAverage()));
        result.setClusteringMethod("k-means");
        result.setClusterNr(entry.getKey().getClusterNr());
        result.setMembers(entry.getValue().stream().map(trajectory -> lookUpMap.get(trajectory)).collect(Collectors.toList()));
        return result;
    }

    private List<List<Double>> toDoubleList(double[][] trajectory)
    {
        List<double[]> vectors = Arrays.asList(trajectory);
        return vectors.stream().map(vector -> DoubleStream.of(vector).boxed().collect(Collectors.toList())).collect(Collectors.toList());
    }
}
