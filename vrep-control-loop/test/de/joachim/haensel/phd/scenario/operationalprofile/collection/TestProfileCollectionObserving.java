package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.ArcSegmentDecompositionAlgorithmByNgoEtAl;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitControllerVariableLookahead;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitParameters;
import de.joachim.haensel.phd.scenario.vehicle.experiment.TrajectoryRecorder;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

@RunWith(Parameterized.class)
public class TestProfileCollectionObserving
{
    private static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";

    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;
    
    private double _lookahead;
    private double _maxVelocity;
    private double _maxLongitudinalAcceleration;
    private double _maxLongitudinalDecceleration;
    private double _maxLateralAcceleration;
    private List<Position2D> _targetPoints;
    private String _mapFileName;
    private RoadMap _map;
    private String _color;

    private String _testID;

   @BeforeClass
   public static void setupVrep() throws VRepException
   {
       _vrep = VRepRemoteAPI.INSTANCE;
       _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
       _objectCreator = new VRepObjectCreation(_vrep, _clientID);
   }

   @AfterClass
   public static void tearDownVrep() throws VRepException 
   {
       waitForRunningSimulationToStop();
       _vrep.simxFinish(_clientID);
   }

   private static void waitForRunningSimulationToStop() throws VRepException
   {
       IntWA simStatus = new IntWA(1);
       _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript, "simulationState", null, null, null, null, simStatus, null, null, null, remoteApi.simx_opmode_blocking);
       while(simStatus.getArray()[0] != remoteApi.sim_simulation_stopped)
       {
           try
           {
               Thread.sleep(500);
           }
           catch (InterruptedException exc)
           {
               exc.printStackTrace();
           }
           _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript, "simulationState", null, null, null, null, simStatus, null, null, null, remoteApi.simx_opmode_blocking);
       }
   }

   @After
   public void cleanUpObjects() throws VRepException
   {
       _objectCreator.deleteAll();
   }

   @Parameterized.Parameters
   public static Collection<Object[]> parameters()
   {
       return Arrays.asList(new Object[][]
       {
//           {"luebeck_small", 15, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(5579.18,3023.38), new Position2D(6375.32,3687.02)), "luebeck-roads.net.xml", "blue"},
//           {"chandigarh_small", 15, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(8564.44,9559.52), new Position2D(7998.74,8151.80)), "chandigarh-roads.net.xml", "blue"},
//           {"chandigarh_mini", 15, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(6394.91,7882.57), new Position2D(6497.73,7852.91)), "chandigarh-roads.net.xml", "blue"},
//           {"chandigarh_medium", 15, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(8564.44,9559.52), new Position2D(7998.74,8151.80), new Position2D(7596.09,7264.80), new Position2D(8158.54,3236.11), new Position2D(11286.49,5458.54)), "chandigarh-roads.net.xml", "blue"},
//           {"chandigarh_medium", 15, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(7596.09,7264.80), new Position2D(8256.48,3253.43), new Position2D(8135.55,3218.77), new Position2D(8139.54,3115.05), new Position2D(11286.49,5458.54)), "chandigarh-roads.net.xml", "blue"},
//         {"luebeck_medium", 15, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(4112.28,7084.47), new Position2D(6196.74,5289.38), new Position2D(10161.11,3555.67), new Position2D(3430.39,581.66), new Position2D(7252.29,1130.89)), "luebeck-roads.net.xml", "blue"},
//           {"luebeck_mini_routing_challenge", 15, 120, 4.0, 4.3, 1.0, Arrays.asList(new Position2D(7882.64,4664.21), new Position2D(7797.34,4539.80), new Position2D(7894.70,4608.56), new Position2D(8051.17,5536.44), new Position2D(8039.89,5485.08)), "luebeck-roads.net.xml", "blue"},
           {"luebeck_7_routing_challenge", 15, 120, 4.0, 4.0, 1.0, 
               Arrays.asList
               (
                   new Position2D(3934.06,6377.25), 
                   new Position2D(4209.93,7074.07), 
                   new Position2D(5552.48,4469.20), 
                   new Position2D(2285.37,3416.74), 
                   new Position2D(6375.30,3695.19),
                   new Position2D(10112.64,1288.27),
                   new Position2D(8031.39,6647.76)
               ), "luebeck-roads.net.xml", "blue"},
       });
   }

   public TestProfileCollectionObserving(String testID, double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration, List<Position2D> targetPoints, String mapFilenName, String color)
   {
       _lookahead = lookahead;
       _maxVelocity = maxVelocity;
       _maxLongitudinalAcceleration = maxLongitudinalAcceleration;
       _maxLongitudinalDecceleration = maxLongitudinalDecceleration;
       _maxLateralAcceleration = maxLateralAcceleration;
       RoadMapAndCenterMatrix mapAndCenterMatrix;
       try
       {
           mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, RES_ROADNETWORKS_DIRECTORY + mapFilenName);
           _mapFileName = mapFilenName;
           _map = mapAndCenterMatrix.getRoadMap();
           TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
           _targetPoints = targetPoints.stream().map(point -> point.transform(centerMatrix)).collect(Collectors.toList());
       }
       catch (VRepException exc)
       {
           fail();
       }
       _color = color;
       _testID = testID + String.format("%f_%f_%.2f_%.2f_%.2f_", lookahead, maxVelocity, maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration);
   }

    @Test
    public void testProfileCollection() throws VRepException
    {
        final Map<Long, List<TrajectoryElement>> segmentBuffers = new HashMap<>();

        TaskCreator taskCreator = new TaskCreator();
        PointListTaskCreatorConfig config = new PointListTaskCreatorConfig();
        config.setControlParams(_lookahead, _maxVelocity, _maxLongitudinalAcceleration, _maxLongitudinalDecceleration, _maxLateralAcceleration);
        config.setDebug(true);
        config.setMap(_map);
        config.configSimulator(_vrep, _clientID, _objectCreator);
        TrajectoryRecorder trajectoryRecorder = new TrajectoryRecorder();
        config.addLowerLayerControl(trajectoryRecorder);
        config.setCarModel("./res/simcarmodel/vehicleVisualsBrakeScript.ttm");

        config.setTargetPoints(_targetPoints);
        config.addNavigationListener(trajectoryRecorder);
        config.setLowerLayerController(new ILowerLayerFactory() {
            @Override
            public ILowerLayerControl create()
            {
                PurePursuitControllerVariableLookahead purePursuitControllerVariableLookahead = new PurePursuitControllerVariableLookahead();
                purePursuitControllerVariableLookahead.setParameters(new PurePursuitParameters(_lookahead, 0.0));
                ITrajectoryRequestListener requestListener = (newTrajectories, timestamp) ->
                {
                    segmentBuffers.put(new Long(timestamp), newTrajectories);
                };
                purePursuitControllerVariableLookahead.addTrajectoryRequestListener(requestListener);
                return purePursuitControllerVariableLookahead;
            }
        });
        taskCreator.configure(config);
        List<ITask> tasks = taskCreator.createTasks();

        TaskExecutor executor = new TaskExecutor();
        executor.execute(tasks);
        
        System.out.println("Executed all tasks, now collecting data");
        Map<Long, List<TrajectoryElement>> normalizedTrajectories = TrajectoryNormalizer.normalize(segmentBuffers);
        ArcSegmentDecompositionAlgorithmByNgoEtAl decompositor = new ArcSegmentDecompositionAlgorithmByNgoEtAl();
        Map<Long, List<IArcsSegmentContainerElement>> decompositions = new HashMap<>();
        for (Entry<Long, List<TrajectoryElement>> curTrajectoryEntry : normalizedTrajectories.entrySet())
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
            if(dataPoints != null && dataPoints.size() > 1)
            {
                List<IArcsSegmentContainerElement> decomposition = decompositor.createSegments(dataPoints, 0.3, Math.PI / 4.0, 3.0, 1.0, 100000.0);
                decompositions.put(curTrajectoryEntry.getKey(), decomposition);
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
                    String fileName = String.format("./res/operationalprofiletest/normalizedtrajectories/Trajectory%s%06d.pyplot", _testID, entry.getKey());
                    Files.write(new File(fileName).toPath(), trajectory, Charset.defaultCharset());
                }
            }
            catch (IOException exc)
            {
                exc.printStackTrace();
            }
        };
        System.out.println("Normal trajectories written");
        normalizedTrajectories.entrySet().stream().forEach(writeTrajectoryToFile);
        Consumer<? super Entry<Long, List<IArcsSegmentContainerElement>>> writeDecompositionToFile = entry ->
        {
            try
            {
                if (entry.getValue() != null)
                {
                    List<String> decomposition = entry.getValue().stream().map(element -> element.toPyPlotString()).collect(Collectors.toList());
                    String fileName = String.format("./res/operationalprofiletest/normalizedtrajectories/Decomposition%s%06d.pyplot", _testID, entry.getKey());
                    Files.write(new File(fileName).toPath(), decomposition, Charset.defaultCharset());
                }
            }
            catch (IOException exc)
            {
                exc.printStackTrace();
            }
        };
        System.out.println("Decomposed trajectories written");
        decompositions.entrySet().stream().forEach(writeDecompositionToFile);
    }
}
