package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class TestTurtleHashingGroundTruth
{
    public static Stream<Arguments> parameters()
    {
        return Arrays.asList(new Object[][]
        {
            {"GroundTruthColuebeck-roads.net.xml", "GroundTruthColuebeck", 10.1},
        }).stream().map(params -> Arguments.of(params));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testReadSimulationRun(String profileInputFileName, String histogramFileName, double hashPixelSize)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            Map<Long, List<TrajectoryElement>> configurations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/" + profileInputFileName + ".json"),new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            TurtleHash hasher = new TurtleHash(hashPixelSize, 5.0, 20);
            TreeMap<Long, List<TrajectoryElement>> configsSorted = new TreeMap<Long, List<TrajectoryElement>>(configurations);
            TreeMap<String, List<Long>> configsHashed = new TreeMap<String, List<Long>>();
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
            Files.write(new File("./res/operationalprofiletest/serializedruns/" + histogramFileName + ".txt").toPath(), histogram, Charset.defaultCharset());
            System.out.println(String.format("Number of trajectories: %d, skipped trajectories: %d, number of buckets: %d", configsSorted.size(), tooShortSkipped, configsHashed.size()));
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
