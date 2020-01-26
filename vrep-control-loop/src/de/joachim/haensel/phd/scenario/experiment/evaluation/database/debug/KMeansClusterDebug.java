package de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationConfiguration;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class KMeansClusterDebug extends AbstractAnalysis
{
    private Scanner _scanner;

    public KMeansClusterDebug()
    {
        _scanner = new Scanner(System.in);
    }
    
    public static void main(String[] args) 
    {
        try 
        {
            KMeansClusterDebug debugRun = new KMeansClusterDebug();
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
//        LineStrip line1 = createStandardLine(-100.0, -100.0, 0.0, 100.0, 100.0, 0.0);
//        LineStrip line2 = createStandardLine(100.0, -100.0, 0.0, 100.0, -100.0, 50.0);
//        LineStrip line3 = createStandardLine(100.0, 100.0, 0.0, 100.0, 100.0, 50.0);
//        chart.getScene().getGraph().add(line1);
//        chart.getScene().getGraph().add(line2);
//        chart.getScene().getGraph().add(line3);
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
        return new Runnable()
        {
            @Override
            public void run()
            {
                CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
                MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
                MongoClient mongoClient = MongoClients.create(settings);

                MongoDatabase carDatabase = mongoClient.getDatabase("car");
                List<String> collectionNames = carDatabase.listCollectionNames().into(new ArrayList<String>()).parallelStream().filter(name -> name.startsWith("sim")).collect(Collectors.toList());
//                collectionNames = collectionNames.stream().filter(name -> name.contains("Luebeck")).collect(Collectors.toList());
                List<MongoCollection<MongoObservationConfiguration>> collections = 
                        collectionNames.parallelStream().map(collectionName -> carDatabase.getCollection(collectionName, MongoObservationConfiguration.class)).collect(Collectors.toList());
            
                List<NamedObsConf> decoded = 
                        collections.stream().map(collection -> decodeMongoCollection(collection, 2000)).flatMap(list -> list.stream()).collect(Collectors.toList());
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
                Collections.shuffle(trajectories);
                System.out.println("Computing path coordinates");

                Map<Integer, List<Integer>> result = cluster(trajectoryArrrays, 100, 20, visualizer);
                System.out.println("draw cluster? Enter something and <enter>");
                _scanner.next();
                visualizer.drawCluster(trajectoryArrrays, result);
                
                mongoClient.close();
            }
        };
    }
    
    public Map<Integer, List<Integer>> cluster(List<double[][]> allTrajectories, int numOfClusters, int maxIters, TrajectoryDraw visualizer)
    {
        List<double[][]> centers0 = createInitialCenters(allTrajectories, numOfClusters);
        
        visualizer.setRange(maxIters);
        visualizer.setMaxTrajectoriesToKeep(5);
        visualizer.drawTrajectories(centers0, 0);
        
        DistanceCache cache = new DistanceCache(allTrajectories);

        System.out.println("type anything!");
        _scanner.next();
        
        System.out.println("Number of random centers, press anything and <enter> to continue: " + centers0.size());
        Map<Integer, List<Integer>> cluster0 = assignToCenters(allTrajectories, centers0, cache);
        System.out.println("Centers after initial clustering: " + cluster0.keySet().size());
        Map<Integer, List<Integer>> result = cluster0;
        List<double[][]> curCenters = centers0;
        for(int cnt = 0; cnt < maxIters; cnt++)
        {
            curCenters = findNewCenters(result, allTrajectories, cache);
            System.out.format("Iteration cnt: %d with %d centers.\n", cnt, curCenters.size());
            result = assignToCenters(allTrajectories, curCenters, cache);
            visualizer.drawTrajectories(curCenters, cnt + 1);
            IntSummaryStatistics summaryStatistics = result.entrySet().stream().map(entry -> entry.getValue().size()).mapToInt(i -> i).summaryStatistics();
            System.out.format("Summary after clustering step %d : %s\n", cnt, summaryStatistics.toString());
        }
        return result;
    }

    private Map<Integer, List<Integer>> assignToCenters(List<double[][]> allTrajectories, List<double[][]> centers, DistanceCache cache)
    {
        List<Integer> indices = IntStream.range(0, allTrajectories.size()).boxed().collect(Collectors.toList());

        // format is as follows: [idx-trajectory, idx-of-centers]
        List<Integer[]> centerIndices = indices.parallelStream().map(dataIdx -> new Integer[] {dataIdx, assignToCenterIdx(allTrajectories.get(dataIdx), centers, cache)}).collect(Collectors.toList());
        Map<Integer, List<Integer>> result = centerIndices.parallelStream().collect(Collectors.toMap(t -> t[1], t -> {List<Integer> r = new ArrayList<Integer>(); r.add(t[0]); return r;}, (l1, l2) -> {l1.addAll(l2); return l1;}));
        return result;
    }

    private Integer assignToCenterIdx(double[][] trajectory, List<double[][]> centerTrajectories, DistanceCache cache)
    {
        int minIdx = 0;
        double curMin = Double.MAX_VALUE;
        for(int centersIdx = 0; centersIdx < centerTrajectories.size(); centersIdx++)
        {
            double distance = cache.distance(centerTrajectories.get(centersIdx), trajectory);
            if(distance < curMin)
            {
                minIdx = centersIdx;
                curMin = distance;
            }
        }
        return minIdx;
    }

    private List<double[][]> findNewCenters(Map<Integer, List<Integer>> cluster, List<double[][]> allTrajectories, DistanceCache cache)
    {
        List<double[][]> result = cluster.entrySet().parallelStream().map(entry -> findCenter(entry.getValue(), allTrajectories, cache)).collect(Collectors.toList());
        return result;
    }

    public double[][] findCenter(List<Integer> trajectoryIndices, List<double[][]> allTrajectories, DistanceCache cache)
    {
        List<double[][]> centerTrajectories = trajectoryIndices.stream().map(idx -> allTrajectories.get(idx)).collect(Collectors.toList());
//        TrajectoryAverager3D statistics = centerTrajectories.parallelStream().collect(TrajectoryAverager3D::new, TrajectoryAverager3D::accept, TrajectoryAverager3D::combine);
        Trajectory3DSummaryStatistics statistics = 
                centerTrajectories.parallelStream().collect(Trajectory3DSummaryStatistics::new, Trajectory3DSummaryStatistics::accept, Trajectory3DSummaryStatistics::combine);
        double[][] average = statistics.getAverage();
        return average;
    }

    private List<double[][]> createInitialCenters(List<double[][]> allTrajectories, int numberOfClusters)
    {
        MersenneTwister randomGen = new MersenneTwister();
        List<double[][]> randomRoot = new ArrayList<double[][]>(allTrajectories);
        Collections.shuffle(randomRoot, randomGen);
        return randomRoot.subList(0, numberOfClusters - 1);
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
