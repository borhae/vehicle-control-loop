package de.joachim.haensel.phd.scenario.operationalprofile.collection;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

/**
 * TODO handle parameterized test
 * @author dummy
 *
 */
//@RunWith(Parameterized.class)
public class TestTuneClassBuilding
{
    private String _experimentName;

    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]
        {
            {"luebeck_10_targets15.000000_120.000000_4.00_4.00_1.00_.json"},
            {"chandigarh_10_targets15.000000_120.000000_4.00_4.00_1.00_.json"},
            {"luebeck_20_targets15.000000_120.000000_4.00_4.00_1.00_.json"},
            {"chandigarh_20_targets15.000000_120.000000_4.00_4.00_1.00_.json"},
        });
    }
    
    public TestTuneClassBuilding(String experimentName)
    {
        _experimentName = experimentName;
    }
    
    @Test
    public void testTuneClassBuilding()
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            Map<Long, ObservationTuple> observations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Ob" + _experimentName), new TypeReference<Map<Long, ObservationTuple>>() {});
            Map<Long, List<TrajectoryElement>> configurations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Co" + _experimentName), new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            CountTreeNode root = ConfigurationObservationTreeCounter.count(configurations, observations);
            System.out.println("Nr. of (o, c): " + observations.size());
            List<String> levels = new ArrayList<>();
            List<String> output = new ArrayList<>();
            List<List<Integer>> counts = ContentAtLevelPerNode.count(root);
            for(int level = 0; level < counts.size(); level++)
            {
                List<Integer> nodesWithContentCount = counts.get(level);
                output.add(String.format("Level %d: elements: %d |%s", level, nodesWithContentCount.size(), nodesWithContentCount.toString()));
                List<String> thisLevel = new ArrayList<>();
                levels.add(nodesWithContentCount.stream().map(intVal -> intVal.toString()).collect(Collectors.joining(", ")));
            }
            List<OCStats> statsPerLevel = StatsPerLevel.count(root);
            int level = 0;
            for (OCStats curStat : statsPerLevel)
            {
                output.add(String.format("Level %2s, |%s", level, curStat.toNormyString()));
                level++;
            }
            Files.write(new File("./res/operationalprofiletest/serializedruns/" + _experimentName + "stats.txt").toPath(), output, Charset.defaultCharset());
            for (int idx = 0; idx < levels.size(); idx++)
            {
                List<String> curLevel = new ArrayList<>();
                curLevel.add(Integer.toString(idx));
                List<String> entries = counts.get(idx).stream().map(value -> Integer.toString(value)).collect(Collectors.toList());
                curLevel.addAll(entries);
                Files.write(new File("./res/operationalprofiletest/equivalenceclassestuning/" + _experimentName + "_Level_" + idx + "_equivalencehist.csv").toPath(), curLevel, Charset.defaultCharset());
            }
        }
        catch (JsonParseException exc)
        {
            exc.printStackTrace();
        }
        catch (JsonMappingException exc)
        {
            exc.printStackTrace();
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
