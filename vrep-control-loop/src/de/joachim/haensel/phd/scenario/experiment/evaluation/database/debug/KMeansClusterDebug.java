package de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.jzy3d.chart.Chart;
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
        LineStrip line1 = createStandardLine(-100.0, -100.0, 0.0, 100.0, 100.0, 0.0);
        LineStrip line2 = createStandardLine(100.0, -100.0, 0.0, 100.0, -100.0, 100.0);
        LineStrip line3 = createStandardLine(100.0, 100.0, 0.0, 100.0, 100.0, 100.0);
        chart.getScene().getGraph().add(line1);
        chart.getScene().getGraph().add(line2);
        chart.getScene().getGraph().add(line3);
        Executors.newCachedThreadPool().execute(createClusterTask());
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

    private Runnable createClusterTask()
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
                List<MongoCollection<MongoObservationConfiguration>> collections = 
                        collectionNames.parallelStream().map(collectionName -> carDatabase.getCollection(collectionName, MongoObservationConfiguration.class)).collect(Collectors.toList());
            
                List<NamedObsConf> decoded = 
                        collections.stream().map(collection -> decodeMongoCollection(collection, 1000)).flatMap(list -> list.stream()).collect(Collectors.toList());
                List<NamedObsConf> pruned = decoded.parallelStream().filter(obsConf -> obsConf.getConfiguration().size() == 20).collect(Collectors.toList());

                List<NamedObsConf> aligned = align(pruned).parallelStream().map(obsCon -> simplifyToCity(obsCon)).collect(Collectors.toList());
                
                List<NamedObsConf> luebeckData = aligned.parallelStream().filter(obsCon -> obsCon.getName().equals("Luebeck")).collect(Collectors.toList());

                System.out.println("database read, now getting trajectories");
                Function<NamedObsConf, List<TrajectoryElement>> toTrajectory = entry -> entry.getConfiguration().stream().map(elem -> elem).collect(Collectors.toList());
                List<List<TrajectoryElement>> trajectories = luebeckData.parallelStream().map(toTrajectory).collect(Collectors.toList());
                
                System.out.format("Number of trajectories: %s\n", trajectories.size());
                Collections.shuffle(trajectories);
                System.out.println("Computing path coordinates");
                List<List<Coord3d>> allPaths = 
                        trajectories.parallelStream().map(t -> trajectoryToChartPath(t)).collect(Collectors.toList());
                

                Map<Integer, List<Integer>> result = cluster(trajectories, 10, 5, allPaths, chart);

                Map<Integer, List<LineStrip>> lineCluster = 
                        result.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().parallelStream().map(idx -> new LineStrip(allPaths.get(idx))).collect(Collectors.toList())));
                
                int numOfClusters = lineCluster.keySet().size();
                System.out.println("assigning colors");
                lineCluster.entrySet().stream().forEach(entry -> entry.getValue().stream().forEach(line -> setColor(line, entry.getKey(), numOfClusters)));

                System.out.println("assigning width, display true, showpoints true and wireframe true");
                lineCluster.entrySet().parallelStream().forEach(entry -> entry.getValue().parallelStream().forEach(line -> configureLine(line)));
                
                List<LineStrip> lines = lineCluster.values().stream().flatMap(cluster -> cluster.stream()).collect(Collectors.toList());
                System.out.println("setting up chart");
                System.out.println("adding lines to chart");
                chart.getScene().getGraph().add(lines);
                
                mongoClient.close();
            }
        };
    }

    private List<Coord3d> trajectoryToChartPath(List<TrajectoryElement> trajectory)
    {
        return trajectory.stream().map(te -> {return new Coord3d(te.getVector().getbX(), te.getVector().getbY(), te.getVelocity());}).collect(Collectors.toList());
    }

    private List<LineStrip> trajectoriesToLines(List<List<TrajectoryElement>> trajectories)
    {
        List<List<Coord3d>> chartPaths = trajectories.parallelStream().map(trajectory -> trajectoryToChartPath(trajectory)).collect(Collectors.toList());
        return chartPaths.parallelStream().map(chartPath -> new LineStrip(chartPath)).map(line -> configureLine(line)).collect(Collectors.toList());
    }

    public LineStrip configureLine(LineStrip line)
    {
        line.setWireframeWidth(1.0f); 
        line.setFaceDisplayed(true); 
        line.setShowPoints(true); 
        line.setWireframeDisplayed(true);
        return line;
    }
    
    public Map<Integer, List<Integer>> cluster(List<List<TrajectoryElement>> indexedTrajectories, int numOfClusters, int maxIters, List<List<Coord3d>> allPaths, Chart chart)
    {
        List<List<TrajectoryElement>> centers0 = createInitialCenters(indexedTrajectories, numOfClusters);
        float brightness = 0.0f/(float)maxIters;
        drawTrajectories(chart, centers0, brightness);
        System.out.println("type anything!");
        _scanner.next();
        
        System.out.println("Random centers the sample: " + centers0.size());
        Map<Integer, List<Integer>> cluster0 = assignToCenters(indexedTrajectories, centers0);
        System.out.println("Centers after initial clustering: " + cluster0.keySet().size());
        Map<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();
        List<List<TrajectoryElement>> curCenters = centers0;
        for(int cnt = 0; cnt < maxIters; cnt++)
        {
            curCenters = findNewCenters(cluster0, curCenters, indexedTrajectories);
            System.out.println("new centers, enter anything when seen");
            brightness = ((float)(cnt + 1))/(float)(maxIters);
            drawTrajectories(chart, curCenters, brightness);
            _scanner.next();
            System.out.format("Iteration cnt: %d with %d centers.\n", cnt, curCenters.size());
            result = assignToCenters(indexedTrajectories, curCenters);
            IntSummaryStatistics summaryStatistics = result.entrySet().stream().map(entry -> entry.getValue().size()).mapToInt(i -> i).summaryStatistics();
            System.out.format("Summary after clustering step %d : %s\n", cnt, summaryStatistics.toString());
            System.out.format("Number of centers after assignment %d: %d\n", cnt, result.keySet().size());
        }
        return result;
    }

    private void drawTrajectories(Chart chart, List<List<TrajectoryElement>> trajectories, float brightness)
    {
        List<LineStrip> lines = trajectoriesToLines(trajectories);
        IntStream.range(0, lines.size()).forEach(idx -> setColor(lines.get(idx), idx, lines.size(), 1.0f, brightness));
        chart.getScene().getGraph().add(lines);
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

    private List<List<TrajectoryElement>> findNewCenters(Map<Integer, List<Integer>> cluster, List<List<TrajectoryElement>> curCenters, List<List<TrajectoryElement>> data)
    {
        List<List<TrajectoryElement>> result = cluster.entrySet().stream().map(entry -> findCenter(entry.getValue(), data)).collect(Collectors.toList());
        return result;
    }

    public List<TrajectoryElement> findCenter(List<Integer> trajectoryIndices, List<List<TrajectoryElement>> allTrajectories)
    {
        List<List<TrajectoryElement>> centerTrajectories = trajectoryIndices.stream().map(idx -> allTrajectories.get(idx)).collect(Collectors.toList());
        List<List<double[]>> centerArrays = toDoubleArray(centerTrajectories);
        TrajectoryAverager3D averager = centerArrays.stream().collect(TrajectoryAverager3D::new, TrajectoryAverager3D::accept, TrajectoryAverager3D::combine);
        List<double[]> average = averager.getAverage();
        List<TrajectoryElement> result = IntStream.range(0, average.size() - 1).boxed().map(idx -> createTrajectoryElement(idx, average)).collect(Collectors.toList());
        return result;
    }

    private TrajectoryElement createTrajectoryElement(int idx, List<double[]> average)
    {
        Position2D p1 = new Position2D(average.get(idx));
        Position2D p2 = new Position2D(average.get(idx + 1));
        TrajectoryElement result = new TrajectoryElement(new Vector2D(p1, p2));
        result.setVelocity(average.get(idx)[2]);
        return result;
    }

    private List<List<double[]>> toDoubleArray(List<List<TrajectoryElement>> centerTrajectories)
    {
        List<List<double[]>> result = new ArrayList<List<double[]>>();
        for(int idx = 0; idx < centerTrajectories.size(); idx++)
        {
            List<TrajectoryElement> curTrajectory = centerTrajectories.get(idx);
            List<double[]> arrayTrajectory = curTrajectory.stream().map(t -> {Vector2D v = t.getVector(); return new double[] {v.getbX(), v.getbY(), t.getVelocity()};}).collect(Collectors.toList());
            TrajectoryElement t = curTrajectory.get(curTrajectory.size() - 1);
            Position2D tTip = t.getVector().getTip();
            arrayTrajectory.add(new double[] {tTip.getX(), tTip.getY(), t.getVelocity()});
            result.add(arrayTrajectory);
        }
        return result;
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
    
    private void setColor(LineStrip line, int idx, int range)
    {
        float hue = ((float)idx)/((float)range);
        int rgbCompund = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        int red = (rgbCompund >> 16) & 0xFF;
        int green = (rgbCompund >> 8) & 0xFF; 
        int blue = (rgbCompund >> 0) & 0xFF;
        line.setWireframeColor(new Color(red, green, blue, 128));
    }
    
    private void setColor(LineStrip line, int idx, int range, float saturation, float brightness)
    {
        float hue = ((float)idx)/((float)range);
        int rgbCompund = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        int red = (rgbCompund >> 16) & 0xFF;
        int green = (rgbCompund >> 8) & 0xFF; 
        int blue = (rgbCompund >> 0) & 0xFF;
        line.setWireframeColor(new Color(red, green, blue, 128));
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
