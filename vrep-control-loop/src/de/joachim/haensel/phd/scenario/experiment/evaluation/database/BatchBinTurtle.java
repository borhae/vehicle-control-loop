package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationConfiguration;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.ObservationConfiguration;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.TurtleHash;

public class BatchBinTurtle
{
    public static void main(String[] args)
    {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        MongoClient mongoClient = MongoClients.create(settings);
        
        MongoDatabase database = mongoClient.getDatabase("car");
        MongoIterable<String> collectionsNames = database.listCollectionNames();
        MongoIterable<MongoCollection<MongoObservationConfiguration>> collections = collectionsNames.map(collectionName -> database.getCollection(collectionName, MongoObservationConfiguration.class));

        List<ObservationConfiguration> aligned = 
                collections.map(collection -> align(collection)).into(new ArrayList<List<ObservationConfiguration>>()).stream().flatMap(list -> list.stream()).collect(Collectors.toList());

        TurtleHash hasher = new TurtleHash(17.0, 5.0, 20);
        List<String> hashes = aligned.parallelStream().map(obsCon -> hasher.hash(obsCon.getConfiguration())).collect(Collectors.toList());
        Map<String, Integer> bins = new TreeMap<String, Integer>();
        for (String hash : hashes)
        {
            Integer count = bins.get(hash);
            if(count == null)
            {
                count = 0;
            }
            count++;
            bins.put(hash, count);
        }
        bins.entrySet().forEach(entry -> System.out.format("%s: %d\n", entry.getKey(), entry.getValue()));
        System.out.format("Classes: %d\n", bins.entrySet().size());
        mongoClient.close();
    }

    private static List<ObservationConfiguration> align(MongoCollection<MongoObservationConfiguration> collection)
    {
        String experimentID = collection.find().first().getExperimentID();
        List<ObservationConfiguration> aligned = collection.find().map(doc -> doc.decode()).into(new ArrayList<ObservationConfiguration>());
        List<ObservationConfiguration> result = aligned.parallelStream().map(confObs -> TrajectoryNormalizer.normalize(confObs)).collect(Collectors.toList());
        return result;
    }
}
