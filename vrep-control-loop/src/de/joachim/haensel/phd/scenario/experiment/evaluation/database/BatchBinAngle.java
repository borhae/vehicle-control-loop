package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.ObservationConfiguration;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglesimple.AngleHash;

public class BatchBinAngle
{
    public static void main(String[] args)
    {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        MongoClient mongoClient = MongoClients.create(settings);
        
        MongoDatabase database = mongoClient.getDatabase("car");
        MongoIterable<String> collectionsNames = database.listCollectionNames();
        MongoIterable<MongoCollection<MongoObservationConfiguration>> collections = 
                collectionsNames.map(collectionName -> database.getCollection(collectionName, MongoObservationConfiguration.class));


        List<ObservationConfiguration> decoded = 
                collections.map(collection -> decodeMongoCollection(collection)).into(new ArrayList<ArrayList<ObservationConfiguration>>()).stream().flatMap(list -> list.stream()).collect(Collectors.toList());
        List<ObservationConfiguration> pruned = decoded.parallelStream().filter(obsConf -> obsConf.getConfiguration().size() == 20).collect(Collectors.toList());
        
        AngleHash hasher = new AngleHash(5.0, 20);
        List<String> hashes = pruned.parallelStream().map(obsCon -> hasher.hash(obsCon.getConfiguration())).collect(Collectors.toList());
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
        System.out.format("Objects processed: %d, after pruning: %d\n", decoded.size(), pruned.size());

        Map<Character, List<Entry<String, Integer>>> binnedForFirst = bins.entrySet().stream().collect(Collectors.groupingBy(entry -> entry.getKey().charAt(0)));
        Stream<String> asString = binnedForFirst.entrySet().stream().map(entry -> String.format("char: %c, cnt: %d", entry.getKey(), entry.getValue().size()));
        asString.forEach(curLine -> System.out.println(curLine));

        mongoClient.close();
    }

    private static ArrayList<ObservationConfiguration> decodeMongoCollection(MongoCollection<MongoObservationConfiguration> collection)
    {
        return collection.find().map(doc -> doc.decode()).into(new ArrayList<ObservationConfiguration>());
    }
}
