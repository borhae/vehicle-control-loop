package de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

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
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
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
        boolean isLimitedLoad = false;
        int limitLoad = 0;
        boolean isLimitedUse = false;
        int limitUse = 0;

        TrajectoryLoader loader = new TrajectoryLoader();
        List<MongoTrajectory> trajectories = loader.loadIndexedData("data_available_at_2020-01-18");
        
        
        MongoClient mongoClient = openDBToWrite();
        
        int seedBase = 1000;
        int maxIterations = 100;
        int runs = 3;
        
        int[] ks = new int[] 
                {
                        20,
                        200,
                        1000
                };
        
        for(int idxK = 0; idxK < ks.length; idxK++)
        {
            int k = ks[idxK];
//            TODO (continue here)
//            cluster(limitLoad, limitUse, k, seedBase, maxIterations, runs, trajectoryArrays, mongoClient);
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

    private void cluster(int limitLoad, int limitUse, int k, int seedBase, int maxIterations, int runs, List<double[][]> trajectoryArrays, MongoClient mongoClient)
    {
        KMeansClusterer clusterer = new KMeansClusterer(trajectoryArrays, k, new MersenneTwister(), maxIterations);
        Map<Trajectory3DSummaryStatistics, List<double[][]>> minRunResult = clusterer.cluster(seedBase, runs);
        
        List<KMeansClassification> databaseEntries = minRunResult.entrySet().stream().map(entry -> createDBEntry(entry)).collect(Collectors.toList());
        
        MongoDatabase database = mongoClient.getDatabase("k-means-clusterings");
        MongoCollection<KMeansClassification> collection = database.getCollection(String.format("DataLoad%dDataUsed%dK%dSeed%dIterations%dRuns%d", limitLoad, limitUse, k, seedBase, maxIterations, runs), KMeansClassification.class);
        collection.insertMany(databaseEntries);
    }

    private KMeansClassification createDBEntry(Entry<Trajectory3DSummaryStatistics, List<double[][]>> entry)
    {
        KMeansClassification result = new KMeansClassification();
        result.setClassifier(toDoubleList(entry.getKey().getAverage()));
        result.setClusteringMethod("k-means");
        result.setClusterNr(1);
        result.setMembers(entry.getValue().stream().map(trajectory -> toDoubleList(trajectory)).collect(Collectors.toList()));
        return result;
    }

    private List<List<Double>> toDoubleList(double[][] trajectory)
    {
        List<double[]> vectors = Arrays.asList(trajectory);
        return vectors.stream().map(vector -> DoubleStream.of(vector).boxed().collect(Collectors.toList())).collect(Collectors.toList());
    }
}
