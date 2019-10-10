package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectoryTransform;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

/** This class expects a running mongodb installation
 * 
 * @author dummy
 *
 */
public class ImportData
{
    public static void main(String[] args)
    {
        if(args.length != 2)
        {
            System.out.println("usage: java-prefix .. ImportData [file-to-import] [data-set-identifier]");
            return;
        }
        File experimentResultFile = new File(args[0]);
        String experimentID = args[1];
        String experimentTableName = "simulationrun" + experimentID;
        
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        MongoClient mongoClient = MongoClients.create(settings);
        ObjectMapper mapper = new ObjectMapper();
        ImportData importer = new ImportData();

        importer.importFileIntoDatabase(experimentResultFile, experimentID, experimentTableName, mongoClient, mapper);

        mongoClient.close();
    }

    public void importFileIntoDatabase(File experimentResultFile, String experimentID, String experimentTableName, MongoClient mongoClient, ObjectMapper jsonMapper)
    {
        MongoDatabase database = mongoClient.getDatabase("car");
        try
        {
            database.createCollection(experimentTableName);
        } 
        catch (MongoCommandException exc)
        {
            System.out.println(experimentTableName + " collection (table) already created");
        }
        MongoCollection<Document> collection = database.getCollection(experimentTableName);
        try
        {
            Map<Long, List<TrajectoryElement>> configurations = jsonMapper.readValue(experimentResultFile, new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            Map<Long, List<MongoTrajectory>> mongoConfigurations = MongoTrajectoryTransform.transform(configurations);
            List<Document> docs = mongoConfigurations.entrySet().parallelStream().map(curEntry -> new Document().append("simID", experimentID).append("timestamp", curEntry.getKey()).append("trajectory", curEntry.getValue())).collect(Collectors.toList());
            collection.insertMany(docs);
        } 
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
