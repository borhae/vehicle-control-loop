package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationConfiguration;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

public class KMeansClustering
{
    public static void main(String[] args)
    {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        MongoClient mongoClient = MongoClients.create(settings);
        
        MongoDatabase carDatabase = mongoClient.getDatabase("car");
        List<String> collectionNames = carDatabase.listCollectionNames().into(new ArrayList<String>()).parallelStream().filter(name -> name.startsWith("sim")).collect(Collectors.toList());
        List<MongoCollection<MongoObservationConfiguration>> collections = 
                collectionNames.parallelStream().map(collectionName -> carDatabase.getCollection(collectionName, MongoObservationConfiguration.class)).collect(Collectors.toList());
    
        List<NamedObsConf> decoded = 
                collections.stream().map(collection -> decodeMongoCollection(collection, 10000)).flatMap(list -> list.stream()).collect(Collectors.toList());
        List<NamedObsConf> pruned = decoded.parallelStream().filter(obsConf -> obsConf.getConfiguration().size() == 20).collect(Collectors.toList());

        List<NamedObsConf> aligned = align(pruned).parallelStream().map(obsCon -> simplifyToCity(obsCon)).collect(Collectors.toList());
        
        List<NamedObsConf> luebeckData = aligned.parallelStream().filter(obsCon -> obsCon.getName().equals("Luebeck")).collect(Collectors.toList());

        System.out.println("database read, now getting trajectories");
        Function<NamedObsConf, List<Vector2D>> toTrajectory = entry -> entry.getConfiguration().stream().map(elem -> elem.getVector()).collect(Collectors.toList());
        List<List<Vector2D>> trajectories = luebeckData.parallelStream().map(toTrajectory).collect(Collectors.toList());
        Collections.shuffle(trajectories);
   
        List<List<Vector2D>> smallSample = trajectories.subList(0, 10000);

        KMeansClustering clusterer = new KMeansClustering();
        Map<Integer, List<Integer>> result = clusterer.cluster(smallSample, 100);
        MongoDatabase histogramDatabase = mongoClient.getDatabase("histograms");
        histogramDatabase.createCollection("test");
        MongoCollection<MongoHistogramEntry> histogramCollection = histogramDatabase.getCollection("test", MongoHistogramEntry.class);
        List<MongoHistogramEntry> histogram = result.entrySet().stream().map(entry -> new MongoHistogramEntry(entry.getKey(), entry.getValue(), trajectories)).collect(Collectors.toList());
        histogramCollection.insertMany(histogram);
        
        mongoClient.close();
    }

    private Map<Integer, List<Integer>> cluster(List<List<Vector2D>> trajectories, int numOfClusters)
    {
        List<List<Vector2D>> centers = createInitialCenters(trajectories, numOfClusters);
        System.out.println("Centers created 0: " + centers.size());
        Map<Integer, List<Integer>> cluster0 = assignToCenters(trajectories, centers);
        System.out.println("Centers after assignment 0: " + cluster0.keySet().size());
        List<List<Vector2D>> newCenters = findNewCenters(cluster0, centers, trajectories);
        System.out.println("Centers 1 adjusted: " + newCenters.size());
        Map<Integer, List<Integer>> cluster1 = assignToCenters(trajectories, newCenters);
        System.out.println("Centers after assignment 1: " + cluster1.keySet().size());
        return cluster1;
    }

    private List<List<Vector2D>> findNewCenters(Map<Integer, List<Integer>> cluster, List<List<Vector2D>> oldCenters, List<List<Vector2D>> trajectories)
    {
        List<List<Vector2D>> result = cluster.entrySet().stream().map(entry -> findCenter(entry.getValue(), trajectories)).collect(Collectors.toList());
        return result;
    }

    
    public List<Vector2D> findCenter(List<Integer> trajectoryIndices, List<List<Vector2D>> trajectories)
    {
        TrajectoryAverager averager = trajectoryIndices.stream().map(idx -> trajectories.get(idx)).collect(TrajectoryAverager::new, TrajectoryAverager::accept, TrajectoryAverager::combine);
        List<Vector2D> average = averager.getAverage();
        return average;
    }

    private Map<Integer, List<Integer>> assignToCenters(List<List<Vector2D>> trajectories, List<List<Vector2D>> centers)
    {
        List<Integer> indices = IntStream.range(0, trajectories.size()).boxed().collect(Collectors.toList());
        List<Integer[]> centerIndices = indices.stream().map(dataIdx -> new Integer[] {dataIdx, assignToCenterIdx(trajectories.get(dataIdx), centers)}).collect(Collectors.toList());
        Map<Integer, List<Integer>> result = centerIndices.stream().collect(Collectors.toMap(t -> t[1], t -> {List<Integer> r = new ArrayList<Integer>(); r.add(t[0]); return r;}, (l1, l2) -> {l1.addAll(l2); return l1;}));
//        Stream<Integer[]> rStream = indices.parallelStream().map(dataIdx -> new Integer[] {dataIdx, assignToCenterIdx(trajectories.get(dataIdx), centers)});
//        
//        ConcurrentMap<Integer, List<Integer>> result = rStream.collect(Collectors.toConcurrentMap(t -> t[1], t -> {List<Integer> r = new ArrayList<Integer>(); r.add(t[0]); return r;}, (l1, l2) -> {l1.addAll(l2); return l1;}));
        return result;
    }

    private Integer assignToCenterIdx(List<Vector2D> trajectory, List<List<Vector2D>> centers)
    {
        int minIdx = 0;
        double curMin = Double.MAX_VALUE;
        for(int centersIdx = 0; centersIdx < centers.size(); centersIdx++)
        {
            Double distance = computeDistance(centers.get(centersIdx), trajectory);
            if(distance < curMin)
            {
                minIdx = centersIdx;
                curMin = distance;
            }
        }
        return minIdx;
    }

    private List<List<Vector2D>> createInitialCenters(List<List<Vector2D>> trajectories, int k)
    {
        MersenneTwister randomGen = new MersenneTwister();
        List<Integer> randomSelection = IntStream.range(0, trajectories.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(randomSelection, randomGen);
        List<Integer> idxs = new ArrayList<Integer>(randomSelection.subList(0, k - 1));
        List<List<Vector2D>> result = idxs.stream().map(idx -> trajectories.get(idx)).collect(Collectors.toList());
        return result;
    }

    private Double computeDistance(List<Vector2D> a, List<Vector2D> b)
    {
        if (a.size() != b.size())
        {
            System.out.println("error trajecories of different length not comparable. Result set to infinity");
            return Double.POSITIVE_INFINITY;
        }
        double result = 0.0;
        for (int idx = 0; idx < a.size(); idx++)
        {
            Position2D pI = a.get(idx).getTip();
            Position2D pJ = b.get(idx).getTip();
            result += pI.distance(pJ);
        }
        return result;
    }

    private static ArrayList<NamedObsConf> decodeMongoCollection(MongoCollection<MongoObservationConfiguration> collection, int limit)
    {
        return collection.find().limit(limit).map(doc -> new NamedObsConf(doc.decode(), doc.getExperimentID())).into(new ArrayList<NamedObsConf>());
    }

    private static List<NamedObsConf> align(List<NamedObsConf> data)
    {
        return data.parallelStream().map(confObs -> (NamedObsConf)TrajectoryNormalizer.normalize(confObs)).collect(Collectors.toList());
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
}
