package de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.NamedObsConf;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug.DistanceCache;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationConfiguration;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class KMeansFindK implements Runnable
{
    public static void main(String[] args) 
    {
        Thread runner = new Thread(new KMeansFindK());
        runner.run();
    }
    
    @Override
    public void run()
    {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        MongoClient mongoClient = MongoClients.create(settings);

        System.out.println("About to read data from database");
        MongoDatabase carDatabase = mongoClient.getDatabase("car");
        List<String> collectionNames = carDatabase.listCollectionNames().into(new ArrayList<String>()).parallelStream().filter(name -> name.startsWith("sim")).collect(Collectors.toList());
//                collectionNames = collectionNames.stream().filter(name -> name.contains("Luebeck")).collect(Collectors.toList());
        List<MongoCollection<MongoObservationConfiguration>> collections = 
                collectionNames.parallelStream().map(collectionName -> carDatabase.getCollection(collectionName, MongoObservationConfiguration.class)).collect(Collectors.toList());
    
        System.out.println("Decode the data");
//        List<NamedObsConf> decoded = 
//                collections.stream().map(collection -> decodeMongoCollection(collection, 5000)).flatMap(list -> list.stream()).collect(Collectors.toList());
        List<NamedObsConf> decoded = 
                collections.stream().map(collection -> decodeMongoCollection(collection)).flatMap(list -> list.stream()).collect(Collectors.toList());
        System.out.println("Shuffle data");
        Collections.shuffle(decoded);
//        int limit = 1000;
//        System.out.println("limit data to size: " + limit);
//        decoded = decoded.stream().limit(limit).collect(Collectors.toList());
        System.out.format("Number of decoded Trajectories: %d\n", decoded.size());
        
        List<NamedObsConf> pruned = decoded.parallelStream().filter(obsConf -> obsConf.getConfiguration().size() == 20).collect(Collectors.toList());
        System.out.format("Number after pruning too short trajectories: %d\n", pruned.size());

        System.out.println("aligning");
        List<NamedObsConf> aligned = pruned.parallelStream().map(confObs -> (NamedObsConf)TrajectoryNormalizer.normalize(confObs)).collect(Collectors.toList());
        
        System.out.println("aligned, now filtering pure trajectories");
        Function<NamedObsConf, List<TrajectoryElement>> toTrajectory = entry -> entry.getConfiguration().stream().map(elem -> elem).collect(Collectors.toList());
        List<List<TrajectoryElement>> trajectories = aligned.parallelStream().map(toTrajectory).collect(Collectors.toList());
        List<double[][]> trajectoryArrrays = toDoubleArray(trajectories);
        
        
        System.out.format("Number of pure trajectories: %s\n", trajectories.size());

        System.out.println("now trying to find k, format as follows:");
        System.out.println("set_k actual_k average_distance average_distance_variance");
    
        MongoDatabase kmeansDatabase = mongoClient.getDatabase("kmeansclusterstatistics");
        
        int maxK = 5020;
        int minK = 10;
        int stepWidth = 20;
//        int maxK = 110;
//        int minK = 10;
//        int stepWidth = 20;
        
        StringBuilder experimentOut = new StringBuilder();
        MongoCollection<KMeansState> kmeansDBCollection = 
                kmeansDatabase.getCollection(String.format("SampleSize_%d_minK%d_maxK%d_stepWidth%d", pruned.size(), minK, maxK, stepWidth), KMeansState.class);
        for(int k = minK; k <= maxK; k += stepWidth)
        {
            System.out.format("Set k: %d of %d, now starting \n", k, maxK);
            double minRunAverageDistance = Double.POSITIVE_INFINITY;
            double minRunAverageDistanceVariance = 0.0;
            Map<Trajectory3DSummaryStatistics, List<double[][]>> minRunResult = null;
            for(int runNr = 0; runNr < 3; runNr++)
            {
                int seed = runNr + 1000;
                MersenneTwister randomGen = new MersenneTwister(seed);
                System.out.format("%srun nr: %d with seed %d |", runNr == 0 ? "" : ", ", runNr, seed);
                Map<Trajectory3DSummaryStatistics, List<double[][]>> result = cluster(trajectoryArrrays, k, 100, randomGen);
                Set<Trajectory3DSummaryStatistics> centerStatistics = result.keySet();
                double averageDistance = centerStatistics.stream().mapToDouble(centerStatistic -> centerStatistic.getAverageDistance()).sum() / centerStatistics.size();
                double averageDistanceVariance = centerStatistics.stream().mapToDouble(centerStatistic -> centerStatistic.getAverageDistanceVariance()).sum() / centerStatistics.size();
                if(averageDistance < minRunAverageDistance)
                {
                    minRunAverageDistance = averageDistance;
                    minRunAverageDistanceVariance = averageDistanceVariance;
                    minRunResult = result;
                }
                System.out.print("|");
            }
            System.out.println("");
            KMeansState state = new KMeansState(k, minRunResult.keySet().size(), minRunAverageDistance, minRunAverageDistanceVariance);
            kmeansDBCollection.insertOne(state);
            experimentOut.append(String.format("%d %d %f %f\n", k, minRunResult.keySet().size(), minRunAverageDistance, minRunAverageDistanceVariance));
        }
        
        mongoClient.close();
    }

    /**
     * Does the actual k-means clustering. 
     * @param allTrajectories The inputspace of all trajectories to be clustered
     * @param numOfClusters The desired number of clusters (k)
     * @param maxIters The maximum number of iterations
     * @param randomGen A random number generator
     * @return The clusters in the form: Map&lt;cluster centers and statistics, List&lt;trajectories of each cluster&gt;&gt;
     */
    public Map<Trajectory3DSummaryStatistics, List<double[][]>> cluster(List<double[][]> allTrajectories, int numOfClusters, int maxIters, MersenneTwister randomGen)
    {
        List<double[][]> centers0 = createInitialCenters(allTrajectories, numOfClusters, randomGen);
        List<Trajectory3DSummaryStatistics> initialSummaries = centers0.parallelStream().map(center -> new Trajectory3DSummaryStatistics(center)).collect(Collectors.toList());
        
        Map<Trajectory3DSummaryStatistics, List<double[][]>> curState = assignToCenters(allTrajectories, initialSummaries);
        for(int cnt = 0; cnt < maxIters; cnt++)
        {
            System.out.format("%d", cnt % 10);
            List<Trajectory3DSummaryStatistics> clusterStatistics = findNewCenters(curState);
            curState = assignToCenters(allTrajectories, clusterStatistics);
        }
        return curState;
    }

    private Map<Trajectory3DSummaryStatistics, List<double[][]>> assignToCenters(List<double[][]> allTrajectories, List<Trajectory3DSummaryStatistics> centers)
    {
        //make sure that all averages are computed before clustering
        centers.parallelStream().map(center -> center.getAverage()).collect(Collectors.toList());
        Map<Trajectory3DSummaryStatistics, List<double[][]>> result = 
                allTrajectories.parallelStream().collect(Collectors.groupingBy(trajectory -> closestCenter(trajectory, centers)));
        return result;
    }
    
    private Trajectory3DSummaryStatistics closestCenter(double[][] trajectory, List<Trajectory3DSummaryStatistics> centers)
    {
        double curMin = Double.MAX_VALUE;
        Trajectory3DSummaryStatistics result = centers.get(0);
        for(int centersIdx = 0; centersIdx < centers.size(); centersIdx++)
        {
            Trajectory3DSummaryStatistics curCenter = centers.get(centersIdx);
            double[][] centerTrajectory = curCenter.getAverage();
            double distance = DistanceCache.distance(centerTrajectory, trajectory);
            if(distance < curMin)
            {
                curMin = distance;
                result = curCenter;
            }
        }
        return result;
    }
    
    private List<Trajectory3DSummaryStatistics> findNewCenters(Map<Trajectory3DSummaryStatistics, List<double[][]>> cluster)
    {
        List<Trajectory3DSummaryStatistics> result = cluster.entrySet().parallelStream().map(entry -> findCenter(entry.getValue())).collect(Collectors.toList());
        return result;
    }

    public Trajectory3DSummaryStatistics findCenter(List<double[][]> trajectoriesInCluster)
    {
        Trajectory3DSummaryStatistics statistics = 
                trajectoriesInCluster.parallelStream().collect(Trajectory3DSummaryStatistics::new, Trajectory3DSummaryStatistics::accept, Trajectory3DSummaryStatistics::combine);
        return statistics;
    }

    private List<double[][]> createInitialCenters(List<double[][]> allTrajectories, int numberOfClusters, MersenneTwister randomGen)
    {
        List<double[][]> randomRoot = new ArrayList<double[][]>(allTrajectories);
        Collections.shuffle(randomRoot, randomGen);
        return randomRoot.subList(0, numberOfClusters);
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
