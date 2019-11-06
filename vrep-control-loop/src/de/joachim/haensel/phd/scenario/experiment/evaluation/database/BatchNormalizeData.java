package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationTuple;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectoryElement;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.ObservationConfiguration;

public class BatchNormalizeData
{
    public static void main(String[] args) throws IOException
    {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        MongoClient mongoClient = MongoClients.create(settings);
        
        MongoDatabase database = mongoClient.getDatabase("car");
        MongoIterable<String> collectionsNames = database.listCollectionNames();
        MongoIterable<MongoCollection<MongoObservationConfiguration>> collections = collectionsNames.map(collectionName -> database.getCollection(collectionName, MongoObservationConfiguration.class));

        MongoCollection<MongoObservationConfiguration> collection = collections.iterator().next();
        String experimentID = collection.find().first().getExperimentID();
        List<ObservationConfiguration> aligned = collection.find().map(doc -> doc.decode()).into(new ArrayList<ObservationConfiguration>());
        aligned.parallelStream().forEach(confObs -> TrajectoryNormalizer.normalize(confObs.getConfiguration(), confObs.getObservation()));

        String newName = experimentID + "_aligned";
        List<MongoObservationConfiguration> mongoAligned = aligned.parallelStream().map(confObs -> encode(confObs, newName)).collect(Collectors.toList());
        
        database.createCollection(newName);
        MongoCollection<MongoObservationConfiguration> targetDatabase = database.getCollection(newName, MongoObservationConfiguration.class);
        targetDatabase.insertMany(mongoAligned);
        mongoClient.close();
    }

    private static MongoObservationConfiguration encode(ObservationConfiguration confObs, String experimentName)
    {
        List<MongoTrajectoryElement> trajectory = confObs.getConfiguration().stream().map(t -> new MongoTrajectoryElement(t)).collect(Collectors.toList());
        MongoObservationConfiguration result = 
                new MongoObservationConfiguration(experimentName, trajectory, new MongoObservationTuple(confObs.getObservation()), confObs.getTimeStamp());
        return result;
    }
}
