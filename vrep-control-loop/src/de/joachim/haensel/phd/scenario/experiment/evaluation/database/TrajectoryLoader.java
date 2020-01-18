package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationConfiguration;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class TrajectoryLoader
{
    public List<double[][]> loadData(boolean isLimitedLoad, boolean isLimitedUse, int limitUse, int limitLoad)
    {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        MongoClient mongoClient = MongoClients.create(settings);

        System.out.println("About to load data from database");
        MongoDatabase carDatabase = mongoClient.getDatabase("car");
        List<String> collectionNames = carDatabase.listCollectionNames().into(new ArrayList<String>()).parallelStream().filter(name -> name.startsWith("sim")).collect(Collectors.toList());
        List<MongoCollection<MongoObservationConfiguration>> collections = 
                collectionNames.parallelStream().map(collectionName -> carDatabase.getCollection(collectionName, MongoObservationConfiguration.class)).collect(Collectors.toList());
    
        System.out.println("Decode the data");
        List<NamedObsConf> decoded = new ArrayList<NamedObsConf>();
        if(isLimitedLoad)
        {
            decoded = collections.stream().map(collection -> decodeMongoCollection(collection, limitLoad)).flatMap(list -> list.stream()).collect(Collectors.toList());
        }
        else
        {
            decoded = collections.stream().map(collection -> decodeMongoCollection(collection)).flatMap(list -> list.stream()).collect(Collectors.toList());
        }
        System.out.println("Shuffle data");
        Collections.shuffle(decoded);
        if(isLimitedUse)
        {
            System.out.println("limit data to size: " + limitUse);
            decoded = decoded.stream().limit(limitUse).collect(Collectors.toList());
        }
        System.out.format("Number of decoded Trajectories: %d\n", decoded.size());
        
        List<NamedObsConf> pruned = decoded.parallelStream().filter(obsConf -> obsConf.getConfiguration().size() == 20).collect(Collectors.toList());
        System.out.format("Number after pruning too short trajectories: %d\n", pruned.size());

        System.out.println("aligning");
        List<NamedObsConf> aligned = pruned.parallelStream().map(confObs -> (NamedObsConf)TrajectoryNormalizer.normalize(confObs)).collect(Collectors.toList());
        
        System.out.println("aligned, now filtering pure trajectories");
        Function<NamedObsConf, List<TrajectoryElement>> toTrajectory = entry -> entry.getConfiguration().stream().map(elem -> elem).collect(Collectors.toList());
        List<List<TrajectoryElement>> trajectories = aligned.parallelStream().map(toTrajectory).collect(Collectors.toList());
        mongoClient.close();
        List<double[][]> trajectoryArrrays = toDoubleArray(trajectories);
        return trajectoryArrrays;
    }

    private List<double[][]> toDoubleArray(List<List<TrajectoryElement>> trajectories)
    {
        return trajectories.stream().map(trajectory -> trajectoryToDoubleArrayTrajectory(trajectory)).collect(Collectors.toList());
    }

    private double[][] trajectoryToDoubleArrayTrajectory(List<TrajectoryElement> trajectory)
    {
        Function<TrajectoryElement, double[]> toArray = t -> {Vector2D v = t.getVector(); return new double[] {v.getbX(), v.getbY(), t.getVelocity()};};
        double[][] arrayTrajectory = new double[21][3];
        for(int idx = 0; idx < trajectory.size(); idx++)
        {
            arrayTrajectory[idx] = toArray.apply(trajectory.get(idx));
        }
        TrajectoryElement t = trajectory.get(trajectory.size() - 1);
        Position2D tTip = t.getVector().getTip();
        arrayTrajectory[20] = (new double[] {tTip.getX(), tTip.getY(), t.getVelocity()});
        return arrayTrajectory;
    }
    
    private ArrayList<NamedObsConf> decodeMongoCollection(MongoCollection<MongoObservationConfiguration> collection)
    {
        return collection.find().map(doc -> new NamedObsConf(doc.decode(), doc.getExperimentID())).into(new ArrayList<NamedObsConf>());
    }
    
    private static ArrayList<NamedObsConf> decodeMongoCollection(MongoCollection<MongoObservationConfiguration> collection, int limit)
    {
        return collection.find().limit(limit).map(doc -> new NamedObsConf(doc.decode(), doc.getExperimentID())).into(new ArrayList<NamedObsConf>());
    }
}
