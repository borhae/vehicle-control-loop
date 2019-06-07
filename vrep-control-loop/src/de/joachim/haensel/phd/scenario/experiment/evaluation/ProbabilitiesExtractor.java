package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.TurtleHash;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class ProbabilitiesExtractor
{
    public static void main(String[] args)
    {
        String trajectories = args[0];
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            TurtleHash hasher = new TurtleHash(15.5, 5.0, 20);

            System.out.println("reading: " + trajectories);
            String inFileName = trajectories;
            Map<Long, List<TrajectoryElement>> configurations = 
                    mapper.readValue(new File(inFileName), new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            TreeMap<Long, List<TrajectoryElement>> configsSorted = new TreeMap<Long, List<TrajectoryElement>>(configurations);
            TreeMap<String, List<Long>> configsHashed = new TreeMap<String, List<Long>>();
            createHistogram(hasher, configurations, configsSorted, configsHashed);
            
            
//            List<String> percentages = computePercentages(configsHashed).stream().map(value -> Double.toString(value)).collect(Collectors.toList());
//            List<String> absolutes = computeHistogram(configsHashed).stream().map(value -> Integer.toString(value)).collect(Collectors.toList());
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    private static List<Integer> computeHistogram(TreeMap<String, List<Long>> hashedConfigurations)
    {
        List<Integer> result = new ArrayList<Integer>();
        int entries = hashedConfigurations.entrySet().stream().map(entry -> entry.getValue().size()).mapToInt(v -> v).sum();
        hashedConfigurations.entrySet().stream().forEach(entry -> result.add(entry.getValue().size()));
        return result;
    }

    private static List<Double> computePercentages(TreeMap<String, List<Long>> hashedConfigurations)
    {
        List<Double> result = new ArrayList<Double>();
        int entries = hashedConfigurations.entrySet().stream().map(entry -> entry.getValue().size()).mapToInt(v -> v).sum();
        hashedConfigurations.entrySet().stream().forEach(entry -> result.add(((double)entry.getValue().size()) / ((double) entries)));
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
                System.out.println("why?");
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
