package de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryAverager3D;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationConfiguration;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.TurtleHash;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class HashBasedClustersDebug extends AbstractAnalysis
{
    private Scanner _scanner;

    public HashBasedClustersDebug()
    {
        _scanner = new Scanner(System.in);
    }
    
    public static void main(String[] args) 
    {
        try 
        {
            HashBasedClustersDebug debugRun = new HashBasedClustersDebug();
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
        
//            List<NamedObsConf> decoded = 
//                    collections.stream().map(collection -> decodeMongoCollection(collection, 100)).flatMap(list -> list.stream()).collect(Collectors.toList());
                List<NamedObsConf> decoded = 
                        collections.stream().map(collection -> decodeMongoCollection(collection)).flatMap(list -> list.stream()).collect(Collectors.toList());
            System.out.format("Number of decoded Trajectories: %d\n", decoded.size());
            
            List<NamedObsConf> pruned = decoded.parallelStream().filter(obsConf -> obsConf.getConfiguration().size() == 20).collect(Collectors.toList());
            System.out.format("Number after pruning too short trajectories: %d\n", pruned.size());

            List<NamedObsConf> aligned = pruned.parallelStream().map(confObs -> (NamedObsConf)TrajectoryNormalizer.normalize(confObs)).collect(Collectors.toList());
            
            System.out.println("database read, now getting trajectories");
            Function<NamedObsConf, List<TrajectoryElement>> toTrajectory = entry -> entry.getConfiguration().stream().map(elem -> elem).collect(Collectors.toList());
            List<List<TrajectoryElement>> trajectories = aligned.parallelStream().map(toTrajectory).collect(Collectors.toList());

            List<double[][]> trajectoryArrrays = toDoubleArray(trajectories);
            
            System.out.format("Number of trajectories: %s\n", trajectories.size());

            Map<String, List<double[][]>> clusters = cluster(trajectoryArrrays, 10, 20, visualizer);
            System.out.format("Number of clusters: %d\n", clusters.size());
            clusters.forEach((key, value) -> System.out.format("%s, %d\n", key, value.size()));
            System.out.println("type something and press <enter> to finish");
            _scanner.next();
            
            mongoClient.close();
        };
    }

    public Map<String, List<double[][]>> cluster(List<double[][]> allTrajectories, int numOfClusters, int maxIters, TrajectoryDraw visualizer)
    {
        TurtleHash hasher = new TurtleHash(25.1, 5.0, 21);
        // "clusters" by hash-function
        Map<String, List<double[][]>> result = allTrajectories.parallelStream().collect(Collectors.groupingBy(trajectory -> hash(hasher, trajectory)));
        
        visualizer.setRange(1);
        visualizer.setMaxTrajectoriesToKeep(1);

        DistanceCache cache = new DistanceCache(allTrajectories);
        Map<String, double[][]> centers = findCenters(result, cache);
        
        visualizer.drawTrajectories(new ArrayList<double[][]>(centers.values()), 1);
        return result;
    }

    private Map<String, double[][]> findCenters(Map<String, List<double[][]>> clusters, DistanceCache cache)
    {
        Map<String, double[][]> result = 
                clusters.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> findCenter(entry.getValue(), cache)));
        return result;
    }

    private String hash(TurtleHash hasher, double[][] trajectory)
    {
        double[][] copy = new double[trajectory.length][trajectory[0].length];
        for(int idxI = 0; idxI < trajectory.length; idxI++)
        {
            for(int idxJ = 0; idxJ < trajectory[idxI].length; idxJ++)
            {
                copy[idxI][idxJ] = trajectory[idxI][idxJ];
            }
        }
        return hasher.hash(copy);
    }

    public double[][] findCenter(List<double[][]> trajectories, DistanceCache cache)
    {
        TrajectoryAverager3D averager = trajectories.parallelStream().collect(TrajectoryAverager3D::new, TrajectoryAverager3D::accept, TrajectoryAverager3D::combine);
        double[][] average = averager.getAverage();
        return average;
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
    
    private static ArrayList<NamedObsConf> decodeMongoCollection(MongoCollection<MongoObservationConfiguration> collection, int limit)
    {
        return collection.find().limit(limit).map(doc -> new NamedObsConf(doc.decode(), doc.getExperimentID())).into(new ArrayList<NamedObsConf>());
    }
    
    private ArrayList<NamedObsConf> decodeMongoCollection(MongoCollection<MongoObservationConfiguration> collection)
    {
        return collection.find().map(doc -> new NamedObsConf(doc.decode(), doc.getExperimentID())).into(new ArrayList<NamedObsConf>());
    }
}
