package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitControllerVariableLookahead;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitParameters;
import de.joachim.haensel.phd.scenario.vehicle.experiment.TrajectoryRecorder;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

@RunWith(Parameterized.class)
public class TestJsonSerialization
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
    private RoadMap _map;

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
            {"luebeck_small", 15, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(5579.18,3023.38), new Position2D(6375.32,3687.02)), "luebeck-roads.net.xml", "blue"},
          {"luebeck_mini_routing_challenge", 15, 120, 4.0, 4.3, 1.0, Arrays.asList(new Position2D(7882.64,4664.21), new Position2D(7797.34,4539.80), new Position2D(7894.70,4608.56), new Position2D(8051.17,5536.44), new Position2D(8039.89,5485.08)), "luebeck-roads.net.xml", "blue"},

//            {"luebeck_10_targets", 15, 120, 4.0, 4.0, 1.0, 
//                Arrays.asList
//                (
//                    new Position2D(3934.06,6377.25), 
//                    new Position2D(4209.93,7074.07), 
//                    new Position2D(5552.48,4469.20), 
//                    new Position2D(2285.37,3416.74), 
//                    new Position2D(6375.30,3695.19),
//                    new Position2D(10112.64,1288.27),
//                    new Position2D(8031.39,6647.76),
//                    new Position2D(2725.25,838.97),
//                    new Position2D(3790.37,6477.40),
//                    new Position2D(7137.29,3694.75)
//                    
//                ), "luebeck-roads.net.xml", "blue"},
//            {"chandigarh_10_targets", 15, 120, 4.0, 4.0, 1.0, 
//                Arrays.asList
//                (
//                    new Position2D(7596.09,7264.80), 
//                    new Position2D(8256.48,3253.43), 
//                    new Position2D(8135.55,3218.77), 
//                    new Position2D(8139.54,3115.05), 
//                    new Position2D(11286.49,5458.54), 
//                    new Position2D(5392.13,5345.02),
//                    new Position2D(5639.25,332.51),
//                    new Position2D(1589.71,5656.63),
//                    new Position2D(8080.86,10539.42),
//                    new Position2D(8950.33,7591.58),
//                    new Position2D(10636.35,3615.72)
//                ),
//                "chandigarh-roads.net.xml", "blue"},
            
//            {"luebeck_20_targets", 15, 120, 4.0, 4.0, 1.0, 
//                Arrays.asList
//                (
//                    new Position2D(3934.06,6377.25), 
//                    new Position2D(4209.93,7074.07), 
//                    new Position2D(6355.27,3020.94),
//                    new Position2D(5552.48,4469.20), 
//                    new Position2D(2285.37,3416.74), 
//                    new Position2D(6375.30,3695.19),
//                    new Position2D(10112.64,1288.27),
//                    new Position2D(6453.97,2085.75),
//                    new Position2D(8031.39,6647.76),
//                    new Position2D(2725.25,838.97),
//                    new Position2D(7137.29,3694.75),
//                    new Position2D(3790.37,6477.40),
//                    new Position2D(8832.97,3459.52),
//                    new Position2D(5105.51,461.06),
//                    new Position2D(3289.03,5462.35),
//                    new Position2D(539.40,712.32),
//                    new Position2D(9486.77,8185.54),
//                    new Position2D(3339.02,5760.43),
//                    new Position2D(9314.18,3528.76),
//                    new Position2D(6991.20,9006.34)
//                ), "luebeck-roads.net.xml", "blue"},
//            {"chandigarh_20_targets", 15, 120, 4.0, 4.0, 1.0, 
//                Arrays.asList
//                (
//                    new Position2D(8135.55,3218.77), 
//                    new Position2D(7596.09,7264.80), 
//                    new Position2D(8139.54,3115.05), 
//                    new Position2D(11286.49,5458.54), 
//                    new Position2D(5392.13,5345.02),
//                    new Position2D(8256.48,3253.43), 
//                    new Position2D(5639.25,332.51),
//                    new Position2D(1589.71,5656.63),
//                    new Position2D(8080.86,10539.42),
//                    new Position2D(8950.33,7591.58),
//                    new Position2D(10636.35,3615.72),
//                    new Position2D(5423.86,9945.00),
//                    new Position2D(6014.70,1119.94),
//                    new Position2D(12964.27,6016.20),
//                    new Position2D(10077.58,8519.10),
//                    new Position2D(3551.35,6183.43),
//                    new Position2D(12039.51,8544.74),
//                    new Position2D(11086.44,10822.32),
//                    new Position2D(809.09,3509.83),
//                    new Position2D(5205.11,5172.52),
//                    new Position2D(5805.44,9043.60)
//                ),
//                "chandigarh-roads.net.xml", "blue"},
        });
    }

    public TestJsonSerialization(String testID, double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration, List<Position2D> targetPoints, String mapFilenName, String color)
    {
        _lookahead = lookahead;
        _maxVelocity = maxVelocity;
        _maxLongitudinalAcceleration = maxLongitudinalAcceleration;
        _maxLongitudinalDecceleration = maxLongitudinalDecceleration;
        _maxLateralAcceleration = maxLateralAcceleration;
        RoadMapAndCenterMatrix mapAndCenterMatrix = null;
         try
         {
             mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, RES_ROADNETWORKS_DIRECTORY + mapFilenName);
         }
         catch (VRepException exc)
         {
             exc.printStackTrace();
         }
        if(mapAndCenterMatrix != null)
        {
            _map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            _targetPoints = targetPoints.stream().map(point -> point.transform(centerMatrix)).collect(Collectors.toList());
            _testID = testID + String.format("%f_%f_%.2f_%.2f_%.2f_", lookahead, maxVelocity, maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration);
        }
    }
    
    @Test
    public void testSerialize() throws VRepException
    {
        final Map<Long, List<TrajectoryElement>> configurations = new HashMap<>();
        final Map<Long, ObservationTuple> observations = new HashMap<>();

        TaskCreator taskCreator = new TaskCreator();
        PointListTaskCreatorConfig taskConfiguration = new PointListTaskCreatorConfig();
        taskConfiguration.setControlParams(_lookahead, _maxVelocity, _maxLongitudinalAcceleration, _maxLongitudinalDecceleration, _maxLateralAcceleration);
        taskConfiguration.setDebug(true);
        taskConfiguration.setMap(_map);
        taskConfiguration.configSimulator(_vrep, _clientID, _objectCreator);
        TrajectoryRecorder trajectoryRecorder = new TrajectoryRecorder();
        taskConfiguration.addLowerLayerControl(trajectoryRecorder);
        taskConfiguration.setCarModel("./res/simcarmodel/vehicleVisualsBrakeScript.ttm");

        taskConfiguration.setTargetPoints(_targetPoints);
        taskConfiguration.addNavigationListener(trajectoryRecorder);
        taskConfiguration.setLowerLayerController(new ILowerLayerFactory() {
            @Override
            public ILowerLayerControl create()
            {
                PurePursuitControllerVariableLookahead purePursuitControllerVariableLookahead = new PurePursuitControllerVariableLookahead();
                purePursuitControllerVariableLookahead.setParameters(new PurePursuitParameters(_lookahead, 0.0));
                ITrajectoryRequestListener requestListener = (newTrajectories, timestamp) ->
                {
                    configurations.put(new Long(timestamp), newTrajectories);
                };
                purePursuitControllerVariableLookahead.addTrajectoryRequestListener(requestListener);
                ITrajectoryReportListener reportListener = (rearWheelCP, frontWheelCP, velocity, timeStamp) -> {
                    observations.put(new Long(timeStamp), new ObservationTuple(rearWheelCP, frontWheelCP, velocity, timeStamp));
                };
                purePursuitControllerVariableLookahead.addTrajectoryReportListener(reportListener);
                return purePursuitControllerVariableLookahead;
            }
        });
        taskCreator.configure(taskConfiguration);
        List<ITask> tasks = taskCreator.createTasks();

        TaskExecutor executor = new TaskExecutor();
        executor.execute(tasks);
        
        System.out.println("Executed all tasks, now serializing results");
        ObjectMapper mapper = new ObjectMapper();
        trajectoryRecorder.getTrajectory();
        try
        {
            mapper.writeValue(new File("./res/operationalprofiletest/serializedruns/Co" + _testID + ".json"), configurations);
            mapper.writeValue(new File("./res/operationalprofiletest/serializedruns/Ob" + _testID + ".json"), observations);
            mapper.writeValue(new File("./res/operationalprofiletest/serializedruns/TrRe" + _testID + ".json"), trajectoryRecorder.getTrajectory());
            mapper.writeValue(new File("./res/operationalprofiletest/serializedruns/Plan" + _testID + ".json"), trajectoryRecorder.getPlannedTrajectory());
        }
        catch (JsonProcessingException exc)
        {
            exc.printStackTrace();
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
