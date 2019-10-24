package de.joachim.haensel.phd.scenario.experiment.evaluation.database;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationConfiguration;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class AngleDiffOverview
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
    
    
        List<NamedObsConf> decoded = 
                collections.map(collection -> decodeMongoCollection(collection)).into(new ArrayList<ArrayList<NamedObsConf>>()).stream().flatMap(list -> list.stream()).collect(Collectors.toList());
        List<NamedObsConf> pruned = decoded.parallelStream().filter(obsConf -> obsConf.getConfiguration().size() == 20).collect(Collectors.toList());

        List<NamedObsConf> aligned = align(pruned).parallelStream().map(obsCon -> simplifyToCity(obsCon)).collect(Collectors.toList());
        List<NamedRow<Double>> angleRows = aligned.parallelStream().map(obsCon -> new NamedRow<Double>(process(obsCon.getConfiguration()), obsCon.getName())).collect(Collectors.toList());
        
        try
        {
            database.createCollection("angles");
        } 
        catch (MongoCommandException exc)
        {
            System.out.println("angles collection (table) already created");
        }

        MongoCollection<Document> collection = database.getCollection("angles");
        List<Document> docs = angleRows.parallelStream().map(angleRow -> transformToMongo(angleRow)).collect(Collectors.toList());
        collection.insertMany(docs);
    
        mongoClient.close();
    }

    private static NamedObsConf simplifyToCity(NamedObsConf obsCon)
    {
        String name = obsCon.getName();
        if(name.contains("Luebeck"))
        {
            obsCon.setName("Luebeck");
        }
        else if(name.contains("Chandigarh"))
        {
            obsCon.setName("Chandigarh");
        }
        return obsCon;
    }

    private static Document transformToMongo(NamedRow<Double> namedRow)
    {
        Document result = new Document();
        result.append("city", namedRow.getName());
        List<Double> angleRow = namedRow.getTrajectoryAngles();
        for(int idx = 0; idx  < angleRow.size(); idx++)
        {
            String index = String.format("%02d", idx);
            result.append(index, angleRow.get(idx));
        }
        return result;
    }

    public static List<Double> process(List<TrajectoryElement> trajectory)
    {
        List<Double> result = new ArrayList<Double>();
        for (int idx = 0; idx < trajectory.size() - 1; idx++)
        {
            TrajectoryElement e1 = trajectory.get(idx);
            TrajectoryElement e2 = trajectory.get(idx + 1);
            Vector2D v1 = e1.getVector();
            Vector2D v2 = e2.getVector();
            double angleDiff = Math.toDegrees(Vector2D.computeAngle(v1, v2));
            double side = v1.side(v2.getTip());
            result.add((angleDiff * side));
        }
        return result;
    }
    
    private static ArrayList<NamedObsConf> decodeMongoCollection(MongoCollection<MongoObservationConfiguration> collection)
    {
        return collection.find().map(doc -> new NamedObsConf(doc.decode(), doc.getExperimentID())).into(new ArrayList<NamedObsConf>());
    }

    private static List<NamedObsConf> align(List<NamedObsConf> data)
    {
        return data.parallelStream().map(confObs -> (NamedObsConf)TrajectoryNormalizer.normalize(confObs)).collect(Collectors.toList());
    }
}
