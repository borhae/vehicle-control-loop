package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.joachim.haensel.phd.scenario.experiment.evaluation.histograms.KMeansClassification;

public class ClusteringLoader
{

    public Map<double[][], List<Integer>> loadClusters(String clusterName)
    {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        MongoClient mongoClient = MongoClients.create(settings);

        System.out.println("About to load clusters from database");
        MongoDatabase clusterDB = mongoClient.getDatabase("k-means-clusterings");
        
        MongoCollection<KMeansClassification> dbClusters = clusterDB.getCollection(clusterName, KMeansClassification.class);
        List<KMeansClassification> clusters = dbClusters.find().into(new ArrayList<KMeansClassification>());
        Map<double[][], List<Integer>> result = clusters.stream().collect(Collectors.toMap(cluster -> arrayClassifier(cluster), KMeansClassification::getMembers));
    
        
        mongoClient.close();
        return result;
    }
    
    private double[][] arrayClassifier(KMeansClassification cluster)
    {
        List<List<Double>> list = cluster.getClassifier();
        return TrajectoryLoader.fromDoubleListToDoubleArray(list);
    }
}
