package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff.ObservationTuple;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.TurtleHash;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class TestSumOptimizer
{
    public static void main(String[] args)
    {
        String testID = "luebeck_40_targets";
        double lookahead = 15.0;
        double maxVelocity = 120.0;
        double maxLongitudinalAcceleration = 4.0;
        double maxLongitudinalDecceleration = 4.0; 
        double maxLateralAcceleration = 1.0;
        String localTestID = testID + String.format(Locale.US, "%f_%f_%.2f_%.2f_%.2f_", lookahead, maxVelocity, maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration); 
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            System.out.println("reading data");
            Map<Long, ObservationTuple> observations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Ob" + localTestID + ".json"),new TypeReference<Map<Long, ObservationTuple>>() {});
            Map<Long, List<TrajectoryElement>> configurations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Co" + localTestID + ".json"),new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            System.out.println("data read");
            TurtleHash hasher = new TurtleHash(20, 5.0, 20);
            TreeMap<Long, List<TrajectoryElement>> configsSorted = new TreeMap<Long, List<TrajectoryElement>>(configurations);
            TreeMap<String, List<Long>> classHashes = new TreeMap<String, List<Long>>();
            TrajectoryNormalizer.normalizeConfigurationsAndObservations(configurations, observations);
            System.out.println("creating hashes");
            for (Entry<Long, List<TrajectoryElement>> curPlan : configsSorted.entrySet())
            {
                List<TrajectoryElement> curTrajectory = curPlan.getValue();
                ObservationTuple curObservation = observations.get(curPlan.getKey());
                if(curTrajectory.size() != 20)
                {
                    continue;
                }
                List<int[]> pixels = hasher.pixelate(curTrajectory);
                List<Integer> steps = hasher.createSteps3D(pixels);
                Position2D pZero = new Position2D(0.0, 0.0);
                for(int idx = 0; idx < pixels.size() - 1; idx++)
                {
                    int[] cur = pixels.get(idx);
                    int[] nxt = pixels.get(idx + 1);
                    boolean differentPoints = !TurtleHash.same3D(cur, nxt);
                    boolean connectedPoints = TurtleHash.connected3D(cur, nxt);
                    if(!differentPoints || !connectedPoints)
                    {
                        System.out.println("stop here");
                    }
                }
                String configurationHash = steps.stream().map(d -> TurtleHash.toBase26(d)).collect(Collectors.joining());
                double distance = Position2D.distance(pZero, curObservation.getRearWheelCP());
                double orientation = curObservation.getOrientation().getAngle();
                double velocity = Position2D.distance(pZero, new Position2D(Arrays.copyOfRange(curObservation.getVelocity(), 0, 2)));
                String observationHash = String.format("%02.0f%02.0f%02.0f", distance * 2.5, Math.toDegrees(orientation) / 40.0, velocity / 4.0);
                String hash = configurationHash + observationHash;
//                String hash = observationHash + configurationHash;
                if(hash.equals(""))
                {
                    System.out.println("empty hash!");
                }
                List<Long> bucket = classHashes.get(hash);
                if(bucket == null)
                {
                    bucket = new ArrayList<Long>();
                    classHashes.put(hash, bucket);
                }
                bucket.add(curPlan.getKey());
            }
            System.out.println("hashes created");
            
            List<Integer> histogram = classHashes.values().parallelStream().map(bucket -> bucket.size()).collect(Collectors.toList());
            int max = histogram.stream().mapToInt(v -> v).max().orElse(-1);
            int min = histogram.stream().mapToInt(v -> v).min().orElse(-1);
            int histSize = histogram.size();
            Collections.sort(histogram);

            System.out.format("histogram size: %d, min: %d, max: %d\n", histogram.size(), min, max);

            //Actually we would need to divide everything by the amount of classes. Problem: that is really huge! 
            //We are talking 10^3*26^5 if we are lucky. We will end up with close to 0 probabilities! probably hard to store in a double
            List<Double> probabilities = histogram.stream().map(v -> (double)v/(double)histSize).collect(Collectors.toList());
            List<String> pAsString = probabilities.stream().map(p -> Double.toString(p)).collect(Collectors.toList());
            Files.write(Paths.get("./res/operationalprofiletest/serializedruns/histogrampvalues.txt"), pAsString);
            double upperBound = 0.01;
            int testResources = 100;
            
            TestSumOptimizer optimizer = new TestSumOptimizer();
            List<Integer> testCountersFixUB = optimizer.minimizeAmountOfTests(probabilities, upperBound);
            List<Integer> testCounterFixT = optimizer.minimizeUpperBound(probabilities, testResources);
            IntSummaryStatistics summaryStatistics = testCountersFixUB.stream().mapToInt(v -> v).summaryStatistics();
            System.out.println(summaryStatistics);
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    public List<Integer> minimizeUpperBound(List<Double> probabilities, int testResources)
    {
        double[] p = probabilities.stream().mapToDouble(v -> v).toArray();
        int[] t = new int[p.length];
        double[] sum = new double[p.length];
        IntStream range = IntStream.range(0, p.length);
        range.forEach(i -> sum[i] = p[i]/((2.0 + (double)t[i])));
        double curUpperBound = Arrays.stream(sum).sum();
        System.out.println("upper bound: " + curUpperBound);
        while(testResources > 0)
        {
            double maxDelta = Double.NEGATIVE_INFINITY;
            int maxDeltaIdx = -1;
            double delta_i = 0.0;
            for(int i = 0; i < p.length; i++)
            {
                delta_i = p[i]/((2.0 + (double)t[i] + 1));
                if(delta_i > maxDelta)
                {
                    maxDelta = delta_i;
                    maxDeltaIdx = i;
                }
            }
            if(maxDeltaIdx != -1)
            {
                t[maxDeltaIdx]++;
            }
            testResources--;
            curUpperBound = 0.0;
            for(int i = 0; i < p.length; i++)
            {
                curUpperBound += p[i]/((2.0 + (double)t[i]));
            }
            System.out.format("better by index: %2d, resulting upper bound: %f\n", maxDeltaIdx, curUpperBound);
        }
        return Arrays.stream(t).boxed().collect(Collectors.toList());
    }

    public List<Integer> minimizeAmountOfTests(List<Double> probabilities, double upperBound)
    {
        double[] p = probabilities.stream().mapToDouble(v -> v).toArray();
        int[] t = new int[p.length];
        double[] sum = new double[p.length];
        IntStream range = IntStream.range(0, p.length);
        range.forEach(i -> sum[i] = p[i]/((2.0 + (double)t[i])));
        double curUpperBound = Arrays.stream(sum).sum();
        System.out.println("upper bound: " + curUpperBound);
        while(curUpperBound > upperBound)
        {
            double maxDelta = Double.NEGATIVE_INFINITY;
            int maxDeltaIdx = -1;
            double delta_i = 0.0;
            for(int i = 0; i < p.length; i++)
            {
                delta_i = p[i]/((2.0 + (double)t[i] + 1));
                if(delta_i > maxDelta)
                {
                    maxDelta = delta_i;
                    maxDeltaIdx = i;
                }
            }
            if(maxDeltaIdx != -1)
            {
                t[maxDeltaIdx]++;
            }
            curUpperBound = 0.0;
            for(int i = 0; i < p.length; i++)
            {
                curUpperBound += p[i]/((2.0 + (double)t[i]));
            }
            System.out.format("better by index: %2d, resulting upper bound: %f\n", maxDeltaIdx, curUpperBound);
        }
        return Arrays.stream(t).boxed().collect(Collectors.toList());
    }
}
