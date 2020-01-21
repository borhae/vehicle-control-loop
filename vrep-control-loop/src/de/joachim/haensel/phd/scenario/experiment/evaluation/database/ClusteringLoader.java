package de.joachim.haensel.phd.scenario.experiment.evaluation.database;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
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

    public Map<Trajectory3DSummaryStatistics, List<Integer>> loadClusters(String clusterName)
    {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        LogManager logManager = LogManager.getLogManager();
        Logger logger = logManager.getLogger("org.mongodb.driver.connection");
        logger.setLevel(Level.OFF);
        
        MongoClient mongoClient = MongoClients.create(settings);

        System.out.println("About to load clusters from database");
        MongoDatabase clusterDB = mongoClient.getDatabase("k-means-clusterings");
        
        MongoCollection<KMeansClassification> dbClusters = clusterDB.getCollection(clusterName, KMeansClassification.class);
        List<KMeansClassification> clusters = dbClusters.find().into(new ArrayList<KMeansClassification>());
        clusters.sort((a, b) -> Integer.compare(a.getClusterNr(), b.getClusterNr()));
        Map<Trajectory3DSummaryStatistics, List<Integer>> result = clusters.stream().collect(Collectors.toMap(cluster -> toCenter(cluster), KMeansClassification::getMembers));
    
        mongoClient.close();
        return result;
    }
    
    private Trajectory3DSummaryStatistics toCenter(KMeansClassification cluster)
    {
        int idx = cluster.getClusterNr();
        double[][] arrayClassifier = arrayClassifier(cluster);
        Trajectory3DSummaryStatistics result = new Trajectory3DSummaryStatistics(arrayClassifier, idx);
        return result;
    }

    private double[][] arrayClassifier(KMeansClassification cluster)
    {
        List<List<Double>> list = cluster.getClassifier();
        return TrajectoryLoader.fromDoubleListToDoubleArray(list);
    }
}
