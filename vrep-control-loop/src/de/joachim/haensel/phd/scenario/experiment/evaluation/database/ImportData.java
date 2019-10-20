package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationConfiguration;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationTuple;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTransform;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.ObservationTuple;
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
        MongoCollection<MongoObservationConfiguration> collection = database.getCollection(experimentTableName, MongoObservationConfiguration.class);
        try
        {
            Map<Long, List<TrajectoryElement>> configurations = jsonMapper.readValue(experimentFileNames.getConfigurationsFile(), new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            Map<Long, ObservationTuple> observations = jsonMapper.readValue(experimentFileNames.getObservationsFile(), new TypeReference<Map<Long, ObservationTuple>>(){});
            
            Map<Long, List<MongoTrajectory>> mongoConfigurations = MongoTransform.transformConfigurations(configurations);
            Map<Long, MongoObservationTuple> mongoObservations = MongoTransform.transformObservations(observations);
            List<MongoObservationConfiguration> docs = createMongoDocs(experimentID, mongoConfigurations, mongoObservations);
            collection.insertMany(docs);
        } 
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    private List<MongoObservationConfiguration> createMongoDocs(String experimentID, Map<Long, List<MongoTrajectory>> mongoConfigurations, Map<Long, MongoObservationTuple> mongoObservations)
    {
        Stream<Entry<Long, List<MongoTrajectory>>> configsStream = mongoConfigurations.entrySet().parallelStream();
        List<MongoObservationConfiguration> docs = configsStream.map(curEntry -> createMongoDocument(experimentID, curEntry, mongoObservations)).collect(Collectors.toList());
        return docs;
    }

    private MongoObservationConfiguration createMongoDocument(String experimentID, Entry<Long, List<MongoTrajectory>> entry, Map<Long, MongoObservationTuple> observations)
    {
        Long timeStamp = entry.getKey();
        MongoObservationConfiguration result = new MongoObservationConfiguration(experimentID, entry.getValue(), observations.get(timeStamp), timeStamp);
        return result;
    }
}
