package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MergeHistograms
{
    public static void main(String[] args)
    {
        if(args.length < 3)
        {
            System.out.println("need at least two histograms to merge and one outfilename");
        }
        List<String> argList = Arrays.asList(args);
        List<String> inFiles = argList.subList(0, args.length - 1);
        String outFile = argList.get(args.length - 1);
        
        TreeMap<String, Integer> histogram = new TreeMap<String, Integer>();
        inFiles.forEach(fileName -> merge(fileName, histogram));
        List<String> histogramString = histogram.entrySet().stream().map(entry -> String.format("%s, %d", entry.getKey(), entry.getValue())).collect(Collectors.toList());
        try
        {
            Files.write(Paths.get(outFile), histogramString);
        } 
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    private static void merge(String fileName, TreeMap<String, Integer> histogram)
    {
        try
        {
            List<String> allLines = Files.readAllLines(Paths.get(fileName));
            List<KeyValuePair> keyValues = allLines.stream().map(line -> new KeyValuePair(line)).collect(Collectors.toList());
            for (KeyValuePair curKeyValue : keyValues)
            {
                String hash = curKeyValue.getKey();
                Integer integer = histogram.get(hash);
                if(integer != null)
                {
                    integer = integer + curKeyValue.getValue();
                    histogram.put(hash, integer);
                }
                else
                {
                    histogram.put(hash, curKeyValue.getValue());
                }
            }
        } 
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
