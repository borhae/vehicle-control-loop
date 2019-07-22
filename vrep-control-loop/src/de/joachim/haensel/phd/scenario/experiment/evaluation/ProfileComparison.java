package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.TurtleHash;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class ProfileComparison
{
    public static void main(String[] args)
    {
        String trajectories1 = args[0];
        String trajectories2 = args[1];
        String outDiff = args[2];
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            TurtleHash hasher = new TurtleHash(20, 5.0, 20);

            System.out.println("reading: " + trajectories1);
            Map<Long, List<TrajectoryElement>> configurations1 = 
                    mapper.readValue(new File(trajectories1), new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            TreeMap<Long, List<TrajectoryElement>> configsSorted1 = new TreeMap<Long, List<TrajectoryElement>>(configurations1);
            TreeMap<String, List<Long>> configsHashed1 = new TreeMap<String, List<Long>>();
            createHistogram(hasher, configurations1, configsSorted1, configsHashed1);

            
            System.out.println("reading: " + trajectories2);
            Map<Long, List<TrajectoryElement>> configurations2 = 
                    mapper.readValue(new File(trajectories2), new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            TreeMap<Long, List<TrajectoryElement>> configsSorted2 = new TreeMap<Long, List<TrajectoryElement>>(configurations2);
            TreeMap<String, List<Long>> configsHashed2 = new TreeMap<String, List<Long>>();
            createHistogram(hasher, configurations2, configsSorted2, configsHashed2);
            
            TreeMap<String, Double> histogram1 = computePercentages(configsHashed1);
            TreeMap<String, Double> histogram2 = computePercentages(configsHashed2);
            
            int initialCapacity = (histogram1.keySet().size() + histogram2.keySet().size()) / 2;
            HashSet<String> combinedHashKeys = new HashSet<String>(initialCapacity);
            
            histogram1.entrySet().stream().forEach(entry -> combinedHashKeys.add(entry.getKey()));
            histogram2.entrySet().stream().forEach(entry -> combinedHashKeys.add(entry.getKey()));
            System.out.format("Combined buckets: %d", combinedHashKeys.size());
            
            TreeMap<String, Double> resultPercentages = new TreeMap<String, Double>();
            combinedHashKeys.stream().forEach(hashKey -> combinePercentages(hashKey, histogram1, histogram2, resultPercentages));
            List<String> output = 
                    resultPercentages.entrySet().stream().map(entry -> String.format("%s, %s", entry.getKey(), Double.toString(entry.getValue()))).collect(Collectors.toList());
            Files.write(Paths.get(outDiff), output, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    private static void combinePercentages(String hashKey, TreeMap<String, Double> histogram1, TreeMap<String, Double> histogram2, TreeMap<String, Double> resultPercentages)
    {
        Double h1P = histogram1.get(hashKey);
        Double h2P = histogram2.get(hashKey);
        h1P = h1P == null ? 0.0 : h1P;
        h2P = h2P == null ? 0.0 : h2P;
        Double delta = Math.abs(h1P - h2P);
        resultPercentages.put(hashKey, delta);
    }

    private static TreeMap<String, Double> computePercentages(TreeMap<String, List<Long>> hashedConfigurations)
    {
        TreeMap<String, Double> result = new TreeMap<String, Double>();
        int entries = hashedConfigurations.entrySet().stream().map(entry -> entry.getValue().size()).mapToInt(v -> v).sum();
        hashedConfigurations.entrySet().stream().forEach(entry -> result.put(entry.getKey(), ((double)entry.getValue().size()) / ((double) entries)));
        return result;
    }
    
    private static TreeMap<String, Integer> computeHistogram(TreeMap<String, List<Long>> hashedConfigurations)
    {
        TreeMap<String, Integer> result = new TreeMap<String, Integer>();
//        int entries = hashedConfigurations.entrySet().stream().map(entry -> entry.getValue().size()).mapToInt(v -> v).sum();
        hashedConfigurations.entrySet().stream().forEach(entry -> result.put(entry.getKey(), entry.getValue().size()));
        return result;
    }

    private static void createHistogram(TurtleHash hasher, Map<Long, List<TrajectoryElement>> configurations, TreeMap<Long, List<TrajectoryElement>> configsSorted, TreeMap<String, List<Long>> configsHashed)
    {
        int tooShortSkipped = 0;
        TrajectoryNormalizer.normalize(configurations);
        for (Entry<Long, List<TrajectoryElement>> curPlan : configsSorted.entrySet())
        {
            List<TrajectoryElement> curTrajectory = curPlan.getValue();
            if(curTrajectory.size() != 20)
            {
                tooShortSkipped++;
                continue;
            }
            List<int[]> pixels = hasher.pixelate(curTrajectory);
            List<Integer> steps = hasher.createSteps3D(pixels);
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
            String hash = steps.stream().map(d -> TurtleHash.toBase26(d)).collect(Collectors.joining());
            if(hash.equals(""))
            {
                System.out.print("why? ");
                String stepsString = steps.stream().map(cur -> Integer.toString(cur)).collect(Collectors.joining());
//                String trajectoryString = curTrajectory.stream().map(trjElem -> trjElem.toString()).collect(Collectors.joining());
                System.out.println(stepsString);
            }
            List<Long> bucket = configsHashed.get(hash);
            if(bucket == null)
            {
                bucket = new ArrayList<Long>();
                configsHashed.put(hash, bucket);
            }
            bucket.add(curPlan.getKey());
        }
        List<String> histogram = new ArrayList<String>();
        histogram.add("class, amount");
        histogram.addAll(configsHashed.entrySet().stream().map(entry -> String.format("%s, %d", entry.getKey(), entry.getValue().size())).collect(Collectors.toList()));
        System.out.println(String.format("Number of trajectories: %d, skipped trajectories: %d, number of buckets: %d", configsSorted.size(), tooShortSkipped, configsHashed.size()));
    }
}
