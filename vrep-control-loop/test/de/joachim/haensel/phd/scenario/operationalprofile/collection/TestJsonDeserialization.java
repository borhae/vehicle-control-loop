package de.joachim.haensel.phd.scenario.operationalprofile.collection;


import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.experiment.RecordedTrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class TestJsonDeserialization
{
    public static Stream<Arguments> parameters()
    {
        return Arrays.asList(new Object[][]
        {
            {"luebeck_extramini_routing_challenge", 15.0, 120.0, 4.0, 4.3, 1.0},
//            {"luebeck_small", 15.0, 120.0, 4.0, 4.3, 1.0},
//          {"luebeck_mini_routing_challenge", 15.0, 120.0, 4.0, 4.3, 1.0},
//          {"luebeck_10_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
//          {"chandigarh_10_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
//            {"chandigarh_20_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
//            {"luebeck_20_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
        }).stream().map(params -> Arguments.of(params));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testReadSimulationRun(String testID, double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration)
    {
        String localTestID = testID + String.format("%f_%f_%.2f_%.2f_%.2f_", lookahead, maxVelocity, maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration); 
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            Map<Long, ObservationTuple> observations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Ob" + localTestID + ".json"),new TypeReference<Map<Long, ObservationTuple>>() {});
            Map<Long, List<TrajectoryElement>> configurations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Co" + localTestID + ".json"),new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            List<RecordedTrajectoryElement> trajectoryRecordings = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/TrRe" + localTestID + ".json"), new TypeReference<List<RecordedTrajectoryElement>>() {});
            List<Position2D> plannedPath = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Plan" + localTestID + ".json"), new TypeReference<List<Position2D>>() {});
            RecordedTrajectoryElement firstRecord = trajectoryRecordings.get(0);
            RecordedTrajectoryElement lastRecord = trajectoryRecordings.get(trajectoryRecordings.size() - 1);
            long sysTimeSpanMillis = lastRecord.getSysTime() - firstRecord.getSysTime();
            long simTimeSpanMillis = lastRecord.getSimTime() - firstRecord.getSimTime();
            
            RecordedTrajectoryElement lastElem = firstRecord;
            double actualDistance = 0.0;
            for(int idx = 1; idx < trajectoryRecordings.size(); idx++)
            {
                RecordedTrajectoryElement curElem = trajectoryRecordings.get(idx);
                actualDistance += curElem.getPos().distance(lastElem.getPos());
                lastElem = curElem;
            }

            double plannedDistance = 0.0;
            Position2D firstPlanPos = plannedPath.get(0);
            Position2D lastPos = firstPlanPos;
            for (int idx = 0; idx < plannedPath.size(); idx++)
            {
                Position2D curPos = plannedPath.get(idx);
                plannedDistance += curPos.distance(lastPos);
                lastPos = curPos;
            }
            Duration sysTimeSpan = Duration.ofMillis(sysTimeSpanMillis);
            Duration simTimeSpan = Duration.ofMillis(simTimeSpanMillis);
            System.out.println(String.format("systime: %s, simTime %s, distance actual: %f, distance planned: %f", humanReadableFormat(sysTimeSpan), humanReadableFormat(simTimeSpan), actualDistance, plannedDistance));
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    public static String humanReadableFormat(Duration duration) 
    {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }
}
