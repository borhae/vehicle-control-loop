package de.joachim.haensel.phd.scenario.experimentrunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import de.joachim.haensel.phd.scenario.operationalprofile.collection.ObservationTuple;
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

public class ExperimentRunner
{
    private static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";

    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

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

   private void run(String testID, double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration, List<Position2D> targetPoints, String mapFilenName, String color) throws VRepException
   {
       RoadMapAndCenterMatrix mapAndCenterMatrix = null;
       RoadMap map = null;
       List<Position2D> targetPointsMapped = null;
       try 
       {
           mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, RES_ROADNETWORKS_DIRECTORY + mapFilenName);
       } 
       catch (VRepException exc) 
       {
           exc.printStackTrace();
       }
       if (mapAndCenterMatrix != null) 
       {
           map = mapAndCenterMatrix.getRoadMap();
           TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
           targetPointsMapped = targetPoints.stream().map(point -> point.transform(centerMatrix))
                   .collect(Collectors.toList());
           testID = testID + String.format("%f_%f_%.2f_%.2f_%.2f_", lookahead, maxVelocity,
                   maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration);
       }
      final Map<Long, List<TrajectoryElement>> configurations = new HashMap<>();
      final Map<Long, ObservationTuple> observations = new HashMap<>();

      TaskCreator taskCreator = new TaskCreator();
      PointListTaskCreatorConfig taskConfiguration = new PointListTaskCreatorConfig();
      taskConfiguration.setControlParams(lookahead, maxVelocity, maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration);
      taskConfiguration.setDebug(true);
      taskConfiguration.setMap(map);
      taskConfiguration.configSimulator(_vrep, _clientID, _objectCreator);
      TrajectoryRecorder trajectoryRecorder = new TrajectoryRecorder();
      taskConfiguration.addLowerLayerControl(trajectoryRecorder);
      taskConfiguration.setCarModel("./res/simcarmodel/vehicleVisualsBrakeScript.ttm");

      taskConfiguration.setTargetPoints(targetPointsMapped);
      taskConfiguration.addNavigationListener(trajectoryRecorder);
      taskConfiguration.setLowerLayerController(new ILowerLayerFactory() {
          @Override
          public ILowerLayerControl create()
          {
              PurePursuitControllerVariableLookahead purePursuitControllerVariableLookahead = new PurePursuitControllerVariableLookahead();
              purePursuitControllerVariableLookahead.setParameters(new PurePursuitParameters(lookahead, 0.0));
              ITrajectoryRequestListener requestListener = (newTrajectories, timestamp) ->
              {
                  configurations.put(Long.valueOf(timestamp), newTrajectories);
              };
              purePursuitControllerVariableLookahead.addTrajectoryRequestListener(requestListener);
              ITrajectoryReportListener reportListener = (rearWheelCP, frontWheelCP, velocity, timeStamp) -> {
                  observations.put(Long.valueOf(timeStamp), new ObservationTuple(rearWheelCP, frontWheelCP, velocity, timeStamp));
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
          mapper.writeValue(new File("./res/operationalprofiletest/serializedruns/Co" + testID + ".json"), configurations);
          mapper.writeValue(new File("./res/operationalprofiletest/serializedruns/Ob" + testID + ".json"), observations);
          mapper.writeValue(new File("./res/operationalprofiletest/serializedruns/TrRe" + testID + ".json"), trajectoryRecorder.getTrajectory());
          mapper.writeValue(new File("./res/operationalprofiletest/serializedruns/Plan" + testID + ".json"), trajectoryRecorder.getPlannedTrajectory());
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

    public static void main(String[] args)
    {
        System.out.println("hello world");
        try
        {
            _vrep = VRepRemoteAPI.INSTANCE;
            _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
            _objectCreator = new VRepObjectCreation(_vrep, _clientID);
            ExperimentRunner runner = new ExperimentRunner();
            runner.run("luebeck_extramini_routing_challenge", 15.0, 120.0, 4.0, 4.3, 1.0, Arrays.asList
                    (
                            new Position2D(7882.64,4664.21), 
                            new Position2D(7797.34,4539.80)), "luebeck-roads.net.xml", "blue");
            _objectCreator.deleteAll();            
            waitForRunningSimulationToStop();
            _vrep.simxFinish(_clientID);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }
}
