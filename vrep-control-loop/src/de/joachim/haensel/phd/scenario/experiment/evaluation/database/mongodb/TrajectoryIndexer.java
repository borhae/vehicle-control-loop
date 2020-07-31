package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.NamedObsConf;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;

/**
 * Accumulates data from all data-sets in the car database into one data-set in the indexed_trajectories database
 * @author Joachim Haensel
 *
 */
public class TrajectoryIndexer implements Runnable
{
    public static void main(String[] args)
    {
        Thread runner = new Thread(new TrajectoryIndexer());
        runner.run();
    }

    @Override
    public void run()
    {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        MongoClient mongoClient = MongoClients.create(settings);
        
        MongoDatabase carDatabase = mongoClient.getDatabase("car");
        List<String> collectionNames = carDatabase.listCollectionNames().into(new ArrayList<String>()).parallelStream().filter(name -> name.startsWith("sim")).collect(Collectors.toList());
        List<MongoCollection<MongoObservationConfiguration>> collections = 
                collectionNames.parallelStream().map(collectionName -> carDatabase.getCollection(collectionName, MongoObservationConfiguration.class)).collect(Collectors.toList());
    
        System.out.println("Decode the data");
        List<NamedObsConf> decoded = 
                collections.stream().map(collection -> decodeMongoCollection(collection)).flatMap(list -> list.stream()).collect(Collectors.toList());
        System.out.format("Number of decoded Trajectories: %d\n", decoded.size());
        
        List<NamedObsConf> pruned = decoded.parallelStream().filter(obsConf -> obsConf.getConfiguration().size() == 20).collect(Collectors.toList());
        System.out.format("Number after pruning too short trajectories: %d\n", pruned.size());

        System.out.println("aligning");
        List<NamedObsConf> aligned = pruned.parallelStream().map(confObs -> (NamedObsConf)TrajectoryNormalizer.normalize(confObs)).collect(Collectors.toList());
        
        System.out.println("aligned, now transforming to indexed trajectories");
        
        Function<Integer, MongoTrajectory> toMongoTrajectory = idx -> new MongoTrajectory(aligned.get(idx), idx);
        List<MongoTrajectory> mongoTrajectories = IntStream.range(0, aligned.size()).boxed().map(toMongoTrajectory).collect(Collectors.toList());
        
        MongoDatabase database = mongoClient.getDatabase("indexed_trajectories");
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        String name = String.format("data_available_at_%s", formatter.format(LocalDate.now()));

        MongoCollection<MongoTrajectory> collection = database.getCollection(name, MongoTrajectory.class);
        collection.insertMany(mongoTrajectories);

        mongoClient.close();
    }

    private ArrayList<NamedObsConf> decodeMongoCollection(MongoCollection<MongoObservationConfiguration> collection)
    {
        return collection.find().map(doc -> new NamedObsConf(doc.decode(), doc.getExperimentID())).into(new ArrayList<NamedObsConf>());
    }
}
