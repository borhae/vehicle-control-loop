package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationTuple;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTransform;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.ObservationTuple;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

/** This class expects a running mongodb installation
 * 
 * @author dummy
 *
 */
public class ImportData
{
    public void importFileIntoDatabase(ExperimentFiles experimentFileNames, String experimentID, String experimentTableName, MongoClient mongoClient, ObjectMapper jsonMapper)
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
            Map<Long, List<TrajectoryElement>> configurations = jsonMapper.readValue(experimentFileNames.getConfigurationsFile(), new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            Map<Long, ObservationTuple> observations = jsonMapper.readValue(experimentFileNames.getObservationsFile(), new TypeReference<Map<Long, ObservationTuple>>(){});
            
            Map<Long, List<MongoTrajectory>> mongoConfigurations = MongoTransform.transformConfigurations(configurations);
            Map<Long, MongoObservationTuple> mongoObservations = MongoTransform.transformObservations(observations);
            List<Document> docs = createMongoDocs(experimentID, mongoConfigurations, mongoObservations);
            collection.insertMany(docs);
        } 
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    private List<Document> createMongoDocs(String experimentID, Map<Long, List<MongoTrajectory>> mongoConfigurations, Map<Long, MongoObservationTuple> mongoObservations)
    {
        Stream<Entry<Long, List<MongoTrajectory>>> configsStream = mongoConfigurations.entrySet().parallelStream();
        List<Document> docs = configsStream.map(curEntry -> createMongoDocument(experimentID, curEntry, mongoObservations)).collect(Collectors.toList());
        return docs;
    }

    private Document createMongoDocument(String experimentID, Entry<Long, List<MongoTrajectory>> entry, Map<Long, MongoObservationTuple> observations)
    {
        Long timeStamp = entry.getKey();
        Document result = new Document();
        result.append("simID", experimentID);
        result.append("timestamp", timeStamp);
        result.append("trajectory", entry.getValue());
        MongoObservationTuple observationForTimeStamp = observations.get(timeStamp);
        if(observationForTimeStamp != null)
        {
            result.append("observations", observationForTimeStamp);
        }
        else
        {
            result.append("observations", "could not find an observation for this timestamp");
        }
        return result;
    }
}
