package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.EquivalenClassEntry;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.ICountListElem;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.ObservationTuple;
import de.joachim.haensel.phd.scenario.vehicle.experiment.RecordedTrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class TestAnalysisData
{
    static Stream<Arguments> idAndSystemParameterProvider()
    {
    	Collection<Object[]> result = Arrays.asList(new Object[][]
		{
			{"luebeck_mini_routing_challenge", 15.0, 120.0, 4.0, 4.3, 1.0},
			{"luebeck_10_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
			{"chandigarh_10_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
			{"chandigarh_20_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
			{"luebeck_20_targets", 15.0, 120.0, 4.0, 4.0, 1.0}
    	});
    	return result.stream().map(params -> Arguments.of(params));
	}

    @ParameterizedTest
    @MethodSource("idAndSystemParameterProvider")
    public void testCreateHistograms(String testID, double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration)
    {
    	testID = testID + String.format("%f_%f_%.2f_%.2f_%.2f_", lookahead, maxVelocity, maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration);
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            Map<Long, ObservationTuple> observations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Ob" + testID + ".json"),new TypeReference<Map<Long, ObservationTuple>>() {});
            Map<Long, List<TrajectoryElement>> configurations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Co" + testID + ".json"),new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            List<RecordedTrajectoryElement> trajectoryRecordings = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/TrRe" + testID + ".json"), new TypeReference<List<RecordedTrajectoryElement>>() {});
            List<Position2D> plannedPath = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Plan" + testID + ".json"), new TypeReference<List<Position2D>>() {});
            
            List<EquivalenClassEntry> equivalenceClassEntries = new ArrayList<>();
            Set<Long> timeStamps = configurations.keySet();
            for(Long curTimeStamp : timeStamps)
            {
                EquivalenClassEntry equivalenceClassEntry = new EquivalenClassEntry(curTimeStamp, configurations.get(curTimeStamp), observations.get(curTimeStamp));
                equivalenceClassEntries.add(equivalenceClassEntry);
            }
            
            List<List<Double>> allValues = new ArrayList<>();
            for(int idx = 0; idx < equivalenceClassEntries.size(); idx++)
            {
                EquivalenClassEntry curEntry = equivalenceClassEntries.get(idx);
                ICountListElem curNode = curEntry.getRoot();
                List<Double> valuesOfThisEntry = new ArrayList<>();
                int cnt = 0;
                while(curNode != null)
                {
                    valuesOfThisEntry.add(curNode.getNormyValue());
                    curNode = curNode.next();
                    cnt++;
                }
                if(cnt == 43)
                {
                    allValues.add(valuesOfThisEntry);
                }
            }
            for (List<Double> curList : allValues)
            {
                curList.remove(curList.size() - 1);
            }
            List<String> lines = new ArrayList<>();               
            lines.add("Velocity, Angle, Displacement, SetVelocity0, SetAngle0, SetVelocity1, SetAngle1, SetVelocity2, SetAngle2, SetVelocity3, SetAngle3, SetVelocity4, SetAngle4, SetVelocity5, SetAngle5, SetVelocity6, SetAngle6, SetVelocity7, SetAngle7, SetVelocity8, SetAngle8, SetVelocity9, SetAngle9, SetVelocity10, SetAngle10, SetVelocity11, SetAngle11, SetVelocity12, SetAngle12, SetVelocity13, SetAngle13, SetVelocity14, SetAngle14, SetVelocity15, SetAngle15, SetVelocity16, SetAngle16, SetVelocity17, SetAngle17, SetVelocity18, SetAngle18, SetVelocity19");
            lines.addAll(allValues.stream().map(doubleList -> doubleList.stream().map(val -> String.format("%.4f", val)).collect(Collectors.joining(", "))).collect(Collectors.toList()));
            Files.write(new File("./res/operationalprofiletest/rawdata/Hist_" + testID + ".histplot").toPath(), lines);
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

}
