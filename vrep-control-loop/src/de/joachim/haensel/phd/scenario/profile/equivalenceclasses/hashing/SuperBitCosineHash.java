package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import info.debatty.java.lsh.LSHSuperBit;

public class SuperBitCosineHash
{
    public static void main(String[] args)
    {
        if(args.length != 2)
        {
            System.out.println("usage error, we need:");
            System.out.println("infile outfile");
            return;
        }
        String trajectories = args[0];
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            System.out.println("reading: " + trajectories);
            String inFileName = trajectories;
            Map<Long, List<TrajectoryElement>> configurations = 
                    mapper.readValue(new File(inFileName), new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            TreeMap<Long, List<TrajectoryElement>> configsSorted = new TreeMap<Long, List<TrajectoryElement>>(configurations);
            TreeMap<String, List<Long>> configsHashed = new TreeMap<String, List<Long>>();
            TrajectoryNormalizer.normalize(configurations);

            
            int sizeOfVectors = 40;
            int numberOfBuckets = 5;
            int stages = 15;
            LSHSuperBit lsh = new LSHSuperBit(stages, numberOfBuckets, sizeOfVectors);
            
            List<String> hashes = createHistogram(lsh, configurations, configsSorted, configsHashed);
            Files.write(Paths.get(args[1]), hashes);
        
        
//        List<String> percentages = computePercentages(configsHashed).stream().map(value -> Double.toString(value)).collect(Collectors.toList());
//        List<String> absolutes = computeHistogram(configsHashed).stream().map(value -> Integer.toString(value)).collect(Collectors.toList());
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    private static List<String> createHistogram(LSHSuperBit lsh, Map<Long, List<TrajectoryElement>> configurations, TreeMap<Long, List<TrajectoryElement>> configsSorted, TreeMap<String, List<Long>> configsHashed)
    {
        List<Long> keys = new ArrayList<Long>(configurations.keySet());
//        List<Long> someKeys = keys.subList(0, 10000);
        List<List<TrajectoryElement>> trajectories = keys.stream().map(key -> configurations.get(key)).collect(Collectors.toList());
        List<int[]> hashes = trajectories.stream().map(trajectory -> hashTrajectory(lsh, trajectory)).collect(Collectors.toList());
        List<String> outputHashes = hashes.stream().map(hash -> printList(hash)).collect(Collectors.toList());
        HashMap<String, Integer> hashCnt = new HashMap<String, Integer>();
        outputHashes.forEach(hash -> {
            if(!hashCnt.containsKey(hash))
            {
                hashCnt.put(hash, 1);
            }
            else
            {
                hashCnt.put(hash, hashCnt.get(hash) + 1);
            }
        });
        List<String> countedHashes = hashCnt.entrySet().stream().map(entry -> String.format("%s: %04d", entry.getKey(), entry.getValue())).collect(Collectors.toList());
        Collections.sort(countedHashes);
        countedHashes.forEach(stringBucket -> System.out.println(stringBucket));
        System.out.format("Number of buckets: %d\n", hashCnt.size());
        return outputHashes;
    }

    private static String printList(int[] hash)
    {
        StringBuilder sb = new StringBuilder();
        for(int idx = 0; idx < hash.length; idx++)
        {
            sb.append(String.format("%02d", hash[idx]));
            if(idx != hash.length - 1)
            {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private static int[] hashTrajectory(LSHSuperBit lsh, List<TrajectoryElement> trajectory)
    {
//        double[] hashInputVector = trajectory.stream().map(elem -> elem.getVector().getbX()).collect(Collectors.toList());
        if(trajectory.size() < 20)
        {
            System.out.println("too short");
            return new int[0];
        }
        if(trajectory.size() > 20)
        {
            System.out.println("too long");
            return new int[0];
        }
        double[] hashInputVector = new double[trajectory.size() * 2];
        for(int idxIn = 0, idxOut = 0; idxIn < trajectory.size(); idxIn++, idxOut+=2)
        {
            hashInputVector[idxOut] = trajectory.get(idxIn).getVector().getAngle();
            hashInputVector[idxOut + 1] = trajectory.get(idxIn).getVelocity();
        }
//        
//        List<Double> collect = trajectory.stream().map(elem -> elem.getVector().getbX()).collect(Collectors.toList());
//        double[] hashInputVector = new double[collect.size()];
//        for(int idx = 0; idx < collect.size(); idx++)
//        {
//            hashInputVector[idx] = collect.get(idx);
//        }
        int[] result = lsh.hash(hashInputVector);
        return result;
    }

    private static void printTrajectory(List<TrajectoryElement> trajectory)
    {
        trajectory.stream().forEach(elem -> System.out.print(elem.toString()));
        System.out.println("");
    }
}
