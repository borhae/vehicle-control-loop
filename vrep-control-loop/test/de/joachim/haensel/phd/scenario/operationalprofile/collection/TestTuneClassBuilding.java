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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

@RunWith(Parameterized.class)
public class TestTuneClassBuilding
{
    private String _experimentName;

    @Parameterized.Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]
            {{"luebeck_10_targets15.000000_120.000000_4.00_4.00_1.00_.json"},
            {"chandigarh_10_targets15.000000_120.000000_4.00_4.00_1.00_.json"},
            }
        );
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
//            System.out.println(root.countPaths());
//            System.out.println(String.format("Num of observations: %d, of configurations: %d, paths: %d", observations.values().size(), configurations.values().size(), ConfigurationObservationTreeCounter.getLeafsPerPath(root).size()));
//            List<Integer> count = NodesPerLevelCounter.count(root);
//            System.out.println(count);
//            System.out.println(ElementsPerLevelCounter.count(root));
//            List<Integer> heights = ConfigurationObservationTreeCounter.getHeight(root);
//            System.out.println("height: " + heights + "\n num of heights: " + heights.size());
            List<String> output = new ArrayList<>();
            List<List<Integer>> counts = ContentAtLevelPerNode.count(root);
            for(int level = 0; level < counts.size(); level++)
            {
                List<Integer> nodesWithContentCount = counts.get(level);
                output.add(String.format("Level %d: |%s", level, nodesWithContentCount.toString()));
            }
            List<OCStats> statsPerLevel = StatsPerLevel.count(root);
            int level = 0;
            for (OCStats curStat : statsPerLevel)
            {
                output.add(String.format("Level %2s, |%s", level, curStat.toNormyString()));
                level++;
            }
            
            
            
            Files.write(new File("./res/operationalprofiletest/serializedruns/" + _experimentName + "stats.txt").toPath(), output, Charset.defaultCharset());
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
