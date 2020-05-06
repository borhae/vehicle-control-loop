package de.joachim.haensel.phd.scenario.operationalprofile.collection;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.test.Positioner;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.ObservationTuple;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.ConfigurationObservationTreeCounter;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.CountTreeNode;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.NodesPerLevelCounter;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.NodesPerLevelPrinter;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ppvadaptable.AtomicSetActualError;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.streamextensions.IndexAdder;

public class TestMockedInputCollection
{
    @Test
    public void testMultipleDecompositinosTrajectoryBeginningOfRoute()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        TMatrix centerTransformMatrix = roadMap.center(0.0, 0.0);

        //uncentered positions measured by sumo net-edit tool
        Position2D startPosition = new Position2D(5531.34,5485.96);
        Position2D destinationPosition = new Position2D(5879.87,4886.08);
        
        startPosition.transform(centerTransformMatrix);
        destinationPosition.transform(centerTransformMatrix);
        startPosition = roadMap.getClosestPointOnMap(startPosition);
        destinationPosition = roadMap.getClosestPointOnMap(destinationPosition);
        
        IUpperLayerControl upperCtrl = new DefaultNavigationController(5.0, 60);
        Positioner upperLayerSensors = new Positioner(startPosition, Position2D.minus(destinationPosition, startPosition));
        upperCtrl.initController(upperLayerSensors, roadMap);
        
        upperCtrl.buildSegmentBuffer(destinationPosition, roadMap);
        
        List<List<TrajectoryElement>> configurations = createSlidingWindows(getDataPoints(upperCtrl), 20);
        
        MersenneTwister randomGen = new MersenneTwister(System.currentTimeMillis());
        
        Map<Long, List<TrajectoryElement>> configs = new HashMap<>();
        configurations.stream().map(IndexAdder.indexed()).forEachOrdered(entry -> configs.put((long)entry.idx(), entry.v()));

        Map<Long, ObservationTuple> obs = new HashMap<>();
        configs.entrySet().forEach(entry -> obs.put(entry.getKey(), createObservation(entry.getValue(), randomGen, entry.getKey())));
        TrajectoryNormalizer.normalizeConfigurationsAndObservations(configs, obs);
        writeConfiguration(configs, "bla");
        writeObservation(obs, "blub");
        CountTreeNode root = ConfigurationObservationTreeCounter.count(configs, obs);
        int numOfPaths = root.countPaths();
        System.out.println("testend and got: " + numOfPaths + " paths. Input was: " + configs.size() + " configs/observations");
        List<Integer> nodesPerLevel = NodesPerLevelCounter.count(root);
        List<List<String>> nodesPerLevelString = NodesPerLevelPrinter.print(root);
        System.out.println(nodesPerLevel);
        System.out.println(nodesPerLevelString);
    }

    private ObservationTuple createObservation(List<TrajectoryElement> trajectory, MersenneTwister randomGen, long idx)
    {
        Position2D rearWheelCenterPosition = trajectory.get(0).getVector().getBase().plus(Position2D.random(1.5, randomGen));
        Position2D frontWheelCenterPosition = trajectory.get(0).getVector().getBase().plus(Position2D.random(1.5, randomGen));
        double[] velocity = new double[]{randomGen.nextDouble() * 6.0, randomGen.nextDouble() * 6.0};
        return new ObservationTuple(rearWheelCenterPosition, frontWheelCenterPosition, velocity, idx);
    }

    private List<List<TrajectoryElement>> createSlidingWindows(List<TrajectoryElement> allDataPoints, int windowSize)
    {
        List<List<TrajectoryElement>> result = new ArrayList<>();
        for(int cnt = 0; cnt < allDataPoints.size(); cnt++)
        {
            List<TrajectoryElement> newWindow = new ArrayList<>();
            for(int windowCnt = 0; windowCnt < windowSize; windowCnt++)
            {
                int dataPointsIndex = cnt + windowCnt;
                if(dataPointsIndex < allDataPoints.size())
                {
                    newWindow.add(allDataPoints.get(dataPointsIndex).deepCopy());
                }
                else
                {
                    break;
                }
            }
            result.add(newWindow);
        }
        return result;
    }
    
    private List<TrajectoryElement> getDataPoints(IUpperLayerControl upperCtrl)
    {
        List<TrajectoryElement> result = new ArrayList<>();
        List<TrajectoryElement> intermedidate;;
        intermedidate = upperCtrl.getNewElements(10, AtomicSetActualError.INITIALIZATION_REQUEST);
        while(!intermedidate.isEmpty())
        {
            result.addAll(intermedidate);
            intermedidate = upperCtrl.getNewElements(10, AtomicSetActualError.INITIALIZATION_REQUEST);
        }
        return result;
    }
    
    private void writeObservation(final Map<Long, ObservationTuple> observations, Object marker)
    {
        BiConsumer<? super Long, ? super ObservationTuple> writeObservation = (timeStamp, observation) ->
        {
            if(timeStamp != null)
            {
                List<String> observationAsString = observation.toPyplotStringNoView();
                String fileName = String.format("./res/operationalprofiletest/normalizedtrajectories/Observation%s%06d.pyplot", marker, timeStamp);
                try
                {
                    Files.write(new File(fileName).toPath(), observationAsString, Charset.defaultCharset());
                }
                catch (IOException exc)
                {
                    exc.printStackTrace();
                }
            }
        };
        observations.forEach(writeObservation);
    }

    private void writeConfiguration(final Map<Long, List<TrajectoryElement>> configurations, String marker)
    {
        for (Entry<Long, List<TrajectoryElement>> curTrajectoryEntry : configurations.entrySet())
        {
            List<TrajectoryElement> currentTrajectory = curTrajectoryEntry.getValue();
            List<Position2D> dataPoints = null;
            if(currentTrajectory != null)
            {
                dataPoints = currentTrajectory.stream().map(trajectoryElement -> trajectoryElement.getVector().getBase()).collect(Collectors.toList());
                if(!currentTrajectory.isEmpty())
                {
                    Vector2D oldLastVector = currentTrajectory.get(currentTrajectory.size() - 1).getVector();
                    Position2D oldLastVectorTip = oldLastVector.getTip();
                    dataPoints.add(oldLastVectorTip);
                    Vector2D newLastVector = new Vector2D(oldLastVector);
                    newLastVector.resetBase(oldLastVectorTip.getX(), oldLastVectorTip.getY());
                    newLastVector.setLength(0.5);
                    dataPoints.add(newLastVector.getTip());
                }
            }
        }
        System.out.println("collection done");
        Consumer<? super Entry<Long, List<TrajectoryElement>>> writeTrajectoryToFile = entry ->
        {
            try
            {
                if (entry.getValue() != null)
                {
                    List<String> trajectory = entry.getValue().stream().map(element -> element.getVector().toLine().toPyplotString()).collect(Collectors.toList());
                    String fileName = String.format("./res/operationalprofiletest/normalizedtrajectories/Trajectory%s%06d.pyplot", marker, entry.getKey());
                    Files.write(new File(fileName).toPath(), trajectory, Charset.defaultCharset());
                }
            }
            catch (IOException exc)
            {
                exc.printStackTrace();
            }
        };
        configurations.entrySet().forEach(writeTrajectoryToFile);
    }
}
