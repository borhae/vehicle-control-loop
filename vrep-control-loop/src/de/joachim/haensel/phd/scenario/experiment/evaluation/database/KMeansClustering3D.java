package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
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
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class KMeansClustering3D
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
        Function<NamedObsConf, List<TrajectoryElement>> toTrajectory = entry -> entry.getConfiguration().stream().map(elem -> elem).collect(Collectors.toList());
        List<List<TrajectoryElement>> trajectories = luebeckData.parallelStream().map(toTrajectory).collect(Collectors.toList());
        Collections.shuffle(trajectories);
   
        KMeansClustering3D clusterer = new KMeansClustering3D();
        Map<Integer, List<Integer>> result = clusterer.cluster(trajectories, 10, 300);
        MongoDatabase histogramDatabase = mongoClient.getDatabase("histograms");
        String targetCollectionName = "test3d";
        histogramDatabase.createCollection(targetCollectionName);
        String debugCollectionName = "debug";
        histogramDatabase.createCollection(debugCollectionName);
        
        MongoCollection<MongoTrajectory> debugCollection = histogramDatabase.getCollection(debugCollectionName, MongoTrajectory.class);
        
        MongoCollection<MongoHistogram3DEntry> histogramCollection = histogramDatabase.getCollection(targetCollectionName, MongoHistogram3DEntry.class);
        List<MongoHistogram3DEntry> histogram = result.entrySet().stream().map(entry -> new MongoHistogram3DEntry(entry.getKey(), entry.getValue(), trajectories)).collect(Collectors.toList());
        histogramCollection.insertMany(histogram);
        
        mongoClient.close();
    }

    public Map<Integer, List<Integer>> cluster(List<List<TrajectoryElement>> indexedTrajectories, int numOfClusters, int maxIters)
    {
        List<List<TrajectoryElement>> centers0 = createInitialCenters(indexedTrajectories, numOfClusters);
        System.out.println("Random centers the sample: " + centers0.size());
        Map<Integer, List<Integer>> cluster0 = assignToCenters(indexedTrajectories, centers0);
        System.out.println("Centers after initial clustering: " + cluster0.keySet().size());
        Map<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();
        List<List<TrajectoryElement>> curCenters = centers0;
        for(int cnt = 0; cnt < maxIters; cnt++)
        {
            curCenters = findNewCenters(cluster0, curCenters, indexedTrajectories);
            System.out.format("Iteration cnt: %d with %d centers.\n", cnt, curCenters.size());
            result = assignToCenters(indexedTrajectories, curCenters);
            IntSummaryStatistics summaryStatistics = result.entrySet().stream().map(entry -> entry.getValue().size()).mapToInt(i -> i).summaryStatistics();
            System.out.format("Summary after clustering step %d : %s\n", cnt, summaryStatistics.toString());
            System.out.format("Number of centers after assignment %d: %d\n", cnt, result.keySet().size());
        }
        return result;
    }

    private List<List<TrajectoryElement>> findNewCenters(Map<Integer, List<Integer>> cluster, List<List<TrajectoryElement>> curCenters, List<List<TrajectoryElement>> data)
    {
        List<List<TrajectoryElement>> result = cluster.entrySet().stream().map(entry -> findCenter(entry.getValue(), data)).collect(Collectors.toList());
        return result;
    }

    public List<TrajectoryElement> findCenter(List<Integer> trajectoryIndices, List<List<TrajectoryElement>> smallSample)
    {
        List<List<TrajectoryElement>> centerTrajectories = trajectoryIndices.stream().map(idx -> smallSample.get(idx)).collect(Collectors.toList());
        List<List<double[]>> centerArrays = toDoubleArray(centerTrajectories);
        TrajectoryAverager3D averager = centerArrays.stream().collect(TrajectoryAverager3D::new, TrajectoryAverager3D::accept, TrajectoryAverager3D::combine);
        List<double[]> average = averager.getAverage();
        List<TrajectoryElement> result = average.stream().map(v -> {TrajectoryElement t = new TrajectoryElement(new Vector2D(0.0, 0.0, v[0], v[1])); t.setVelocity(v[2]); return t;}).collect(Collectors.toList());
        return result;
    }

    private List<List<double[]>> toDoubleArray(List<List<TrajectoryElement>> centerTrajectories)
    {
        List<List<double[]>> result = new ArrayList<List<double[]>>();
        for(int idx = 0; idx < centerTrajectories.size(); idx++)
        {
            List<TrajectoryElement> curTrajectory = centerTrajectories.get(idx);
            List<double[]> arrayTrajectory = curTrajectory.stream().map(t -> {Vector2D v = t.getVector(); return new double[] {v.getbX(), v.getbY(), t.getVelocity()};}).collect(Collectors.toList());
            result.add(arrayTrajectory);
        }
        return result;
    }

    private Map<Integer, List<Integer>> assignToCenters(List<List<TrajectoryElement>> smallSample, List<List<TrajectoryElement>> centers0)
    {
        List<Integer> indices = IntStream.range(0, smallSample.size()).boxed().collect(Collectors.toList());
        List<Integer[]> centerIndices = indices.stream().map(dataIdx -> new Integer[] {dataIdx, assignToCenterIdx(smallSample.get(dataIdx), centers0)}).collect(Collectors.toList());
        Map<Integer, List<Integer>> result = centerIndices.stream().collect(Collectors.toMap(t -> t[1], t -> {List<Integer> r = new ArrayList<Integer>(); r.add(t[0]); return r;}, (l1, l2) -> {l1.addAll(l2); return l1;}));
        return result;
    }

    private Integer assignToCenterIdx(List<TrajectoryElement> list, List<List<TrajectoryElement>> centers0)
    {
        int minIdx = 0;
        double curMin = Double.MAX_VALUE;
        for(int centersIdx = 0; centersIdx < centers0.size(); centersIdx++)
        {
            Double distance = computeDistance(centers0.get(centersIdx), list);
            if(distance < curMin)
            {
                minIdx = centersIdx;
                curMin = distance;
            }
        }
        return minIdx;
    }

    private List<List<TrajectoryElement>> createInitialCenters(List<List<TrajectoryElement>> smallSample, int k)
    {
        MersenneTwister randomGen = new MersenneTwister();
        List<Integer> randomSelection = IntStream.range(0, smallSample.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(randomSelection, randomGen);
        List<Integer> idxs = new ArrayList<Integer>(randomSelection.subList(0, k - 1));
        List<List<TrajectoryElement>> result = idxs.stream().map(idx -> smallSample.get(idx)).collect(Collectors.toList());
        return result;
    }

    private Double computeDistance(List<TrajectoryElement> t1, List<TrajectoryElement> t2)
    {
        if (t1.size() != t2.size())
        {
            System.out.println("error trajecories of different length not comparable. Result set to infinity");
            return Double.POSITIVE_INFINITY;
        }
        double result = 0.0;
        for (int idx = 0; idx < t1.size(); idx++)
        {
            double[] pI3D = new double[] {
              t1.get(idx).getVector().getTip().getX(),
              t1.get(idx).getVector().getTip().getY(),
              t1.get(idx).getVelocity()
            };
            double[] pJ3D = new double[] {
                    t2.get(idx).getVector().getTip().getX(),
                    t2.get(idx).getVector().getTip().getY(),
                    t2.get(idx).getVelocity()
                  };
            result += distance(pI3D, pJ3D);
        }
        return result;
    }

    private double distance(double[] p1, double[] p2)
    {
        double dx = p2[0] - p1[0];
        double dy = p2[1] - p1[1];
        double dz = p2[2] - p1[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
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
