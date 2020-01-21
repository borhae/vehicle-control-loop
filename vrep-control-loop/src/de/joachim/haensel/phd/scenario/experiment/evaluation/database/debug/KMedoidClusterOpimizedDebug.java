package de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory.Toolkit;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.NamedObsConf;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationConfiguration;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class KMedoidClusterOpimizedDebug extends AbstractAnalysis
{
    private static final String DISTANCE_SUM = "distance sum";
    private static final String ASSIGN_TO_CENTER = "assign to center";
    private Scanner _scanner;

    public KMedoidClusterOpimizedDebug()
    {
        _scanner = new Scanner(System.in);
    }
    
    public static void main(String[] args) 
    {
        try 
        {
            KMedoidClusterOpimizedDebug debugRun = new KMedoidClusterOpimizedDebug();
            AnalysisLauncher.open(debugRun);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    private void close()
    {
        _scanner.close();
    }

    @Override
    public void init() throws Exception 
    {
        chart = AWTChartComponentFactory.chart(Quality.Nicest, Toolkit.newt);
        chart.addMouseCameraController();
        LineStrip line1 = createStandardLine(-100.0, -100.0, 0.0, 100.0, 100.0, 0.0);
        LineStrip line2 = createStandardLine(100.0, -100.0, 0.0, 100.0, -100.0, 50.0);
        LineStrip line3 = createStandardLine(100.0, 100.0, 0.0, 100.0, 100.0, 50.0);
        chart.getScene().getGraph().add(line1);
        chart.getScene().getGraph().add(line2);
        chart.getScene().getGraph().add(line3);
        TrajectoryDraw visualizer = new TrajectoryDraw(chart);
        Executors.newCachedThreadPool().execute(createClusterTask(visualizer));
    }

    private LineStrip createStandardLine(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        LineStrip line = new LineStrip(new Coord3d(x1, y1, z1), new Coord3d(x2, y2, z2));
        line.setWireframeColor(Color.BLACK);
        line.setWireframeWidth(1.0f); 
        line.setFaceDisplayed(true); 
        line.setShowPoints(true); 
        line.setWireframeDisplayed(true);
        return line;
    }

    private Runnable createClusterTask(TrajectoryDraw visualizer)
    {
        return () -> 
        {
            CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
            MongoClient mongoClient = MongoClients.create(settings);

            MongoDatabase carDatabase = mongoClient.getDatabase("car");
            List<String> collectionNames = carDatabase.listCollectionNames().into(new ArrayList<String>()).parallelStream().filter(name -> name.startsWith("sim")).collect(Collectors.toList());
            collectionNames = collectionNames.stream().filter(name -> name.contains("Luebeck")).collect(Collectors.toList());
//                collectionNames = collectionNames.stream().filter(name -> name.contains("Chandigarh")).collect(Collectors.toList());

            List<MongoCollection<MongoObservationConfiguration>> collections = 
                    collectionNames.parallelStream().map(collectionName -> carDatabase.getCollection(collectionName, MongoObservationConfiguration.class)).collect(Collectors.toList());
        
            List<NamedObsConf> decoded = 
                    collections.stream().map(collection -> decodeMongoCollection(collection, 100)).flatMap(list -> list.stream()).collect(Collectors.toList());
//                List<NamedObsConf> decoded = 
//                        collections.stream().map(collection -> decodeMongoCollection(collection)).flatMap(list -> list.stream()).collect(Collectors.toList());
            System.out.format("Number of decoded Trajectories: %d\n", decoded.size());
            
            List<NamedObsConf> pruned = decoded.parallelStream().filter(obsConf -> obsConf.getConfiguration().size() == 20).collect(Collectors.toList());
            System.out.format("Number after pruning too short trajectories: %d\n", pruned.size());

            List<NamedObsConf> aligned = pruned.parallelStream().map(confObs -> (NamedObsConf)TrajectoryNormalizer.normalize(confObs)).collect(Collectors.toList());
            
            System.out.println("database read, now getting trajectories");
            Function<NamedObsConf, List<TrajectoryElement>> toTrajectory = entry -> entry.getConfiguration().stream().map(elem -> elem).collect(Collectors.toList());
            List<List<TrajectoryElement>> trajectories = aligned.parallelStream().map(toTrajectory).collect(Collectors.toList());

            List<double[][]> trajectoryArrrays = toDoubleArray(trajectories);
            
            System.out.format("Number of trajectories: %s\n", trajectories.size());
            Collections.shuffle(trajectoryArrrays, new MersenneTwister());
            System.out.println("Computing path coordinates");

            Map<Integer, List<Integer>> result = cluster(trajectoryArrrays, 10, 20, visualizer);
            System.out.println("draw cluster? Enter something and <enter>");
            _scanner.next();
            visualizer.drawCluster(trajectoryArrrays, result);
            
            mongoClient.close();
        };
    }

    public Map<Integer, List<Integer>> cluster(List<double[][]> allTrajectories, int numOfClusters, int maxIters, TrajectoryDraw visualizer)
    {
        List<Integer> curCenters = createInitialCenters(allTrajectories, numOfClusters);

        visualizer.setRange(maxIters);
        visualizer.setMaxTrajectoriesToKeep(5);
        visualizer.drawTrajectoriesByIndex(curCenters, allTrajectories, 0);
        
        System.out.println("type anything!");
        _scanner.next();

        DistanceCache cache = new DistanceCache(allTrajectories);
        
        Map<Integer, List<Integer>> curResult = assignToCenters(allTrajectories, curCenters, cache);
        System.out.format("Initial cluster assignement done (num of clusters: %d).\n", curResult.keySet().size());

        for(int cnt = 0; cnt < maxIters; cnt++)
        {
            curCenters = findNewCenters(curResult, allTrajectories, cache);
            System.out.println("New centers found.");
            visualizer.drawTrajectoriesByIndex(curCenters, allTrajectories, cnt + 1);

            System.out.format("Clustering step %d, assigning to new centers.\n", cnt);
            curResult = assignToCenters(allTrajectories, curCenters, cache);
            IntSummaryStatistics summaryStatistics = curResult.entrySet().stream().map(entry -> entry.getValue().size()).mapToInt(i -> i).summaryStatistics();
            System.out.format("Summary after clustering step %d : %s\n", cnt, summaryStatistics.toString());
        }
        return curResult;
    }

    private Map<Integer, List<Integer>> assignToCenters(List<double[][]> allTrajectories, List<Integer> centers0, DistanceCache cache)
    {
        List<Integer> indices = IntStream.range(0, allTrajectories.size()).boxed().collect(Collectors.toList());
        // format is as follows: [idx-trajectory, idx-of-centers]
        List<Integer[]> centerIndices = indices.parallelStream().map(dataIdx -> new Integer[] {dataIdx, assignToCenterIdx(dataIdx, centers0, allTrajectories, cache)}).collect(Collectors.toList());
        Map<Integer, List<Integer>> result = centerIndices.stream().collect(Collectors.toMap(t -> t[1], t -> {List<Integer> r = new ArrayList<Integer>(); r.add(t[0]); return r;}, (l1, l2) -> {l1.addAll(l2); return l1;}));
        return result;
    }

    private Integer assignToCenterIdx(Integer trajectoryIdx, List<Integer> centerIndices, List<double[][]> allTrajectories, DistanceCache cache)
    {
        int minIdx = 0;
        double curMin = Double.MAX_VALUE;
        for(int centersIdx = 0; centersIdx < centerIndices.size(); centersIdx++)
        {
            Integer centerIndex = centerIndices.get(centersIdx);
//            double distance = cache.getDistance(trajectoryIdx, centerIndex, ASSIGN_TO_CENTER);
            double distance = cache.getManhattenDistance(trajectoryIdx, centerIndex);
            if(distance < curMin)
            {
                minIdx = centersIdx;
                curMin = distance;
            }
        }
        return minIdx;
    }

    private List<Integer> findNewCenters(Map<Integer, List<Integer>> cluster, List<double[][]> allTrajectories, DistanceCache cache)
    {
        List<Integer> result = cluster.entrySet().stream().map(entry -> findMedoidsCenter(entry.getValue(), allTrajectories, cache)).collect(Collectors.toList());
        return result;
    }

    private Integer findMedoidsCenter(List<Integer> thisClusterTrajectoryIdxs, List<double[][]> allTrajectories, DistanceCache cache)
    {
        Stream<Integer> allTrajectoriesIdx = IntStream.range(0, allTrajectories.size()).boxed();
        Map<Integer, Double> trajectoryToSum = allTrajectoriesIdx.collect(Collectors.toMap(idx -> idx, idx -> distanceSum(idx, thisClusterTrajectoryIdxs, allTrajectories, cache)));
        
        Entry<Integer, Double> min = Collections.min(trajectoryToSum.entrySet(), Comparator.comparing(Entry::getValue));
        return min.getKey();
    }

    private Double distanceSum(Integer idxFrom, List<Integer> idxTo, List<double[][]> allTrajectories, DistanceCache cache)
    {
//        double result = idxTo.parallelStream().mapToDouble(curTrajectoryIdx -> cache.getDistance(curTrajectoryIdx, idxFrom, DISTANCE_SUM)).sum();
        double result = idxTo.parallelStream().mapToDouble(curTrajectoryIdx -> cache.getManhattenDistance(curTrajectoryIdx, idxFrom)).sum();
        return result;
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

    private List<Integer> createInitialCenters(List<double[][]> trajectoryArrrays, int k)
    {
        MersenneTwister randomGen = new MersenneTwister();
        List<Integer> randomSelection = IntStream.range(0, trajectoryArrrays.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(randomSelection, randomGen);
        List<Integer> idxs = new ArrayList<Integer>(randomSelection.subList(0, k));
        return idxs;
    }
    
    private static ArrayList<NamedObsConf> decodeMongoCollection(MongoCollection<MongoObservationConfiguration> collection, int limit)
    {
        return collection.find().limit(limit).map(doc -> new NamedObsConf(doc.decode(), doc.getExperimentID())).into(new ArrayList<NamedObsConf>());
    }

    private static ArrayList<NamedObsConf> decodeMongoCollection(MongoCollection<MongoObservationConfiguration> collection)
    {
        return collection.find().map(doc -> new NamedObsConf(doc.decode(), doc.getExperimentID())).into(new ArrayList<NamedObsConf>());
    }
}
