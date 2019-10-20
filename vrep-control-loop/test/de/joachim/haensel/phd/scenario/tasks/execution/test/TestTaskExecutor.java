package de.joachim.haensel.phd.scenario.tasks.execution.test;


import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.ObservationTuple;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.creation.AllSameAToBDrivingTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.FixedSourceTargetContinuousRouteTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable.PurePursuitControllerVariableLookahead;
import de.joachim.haensel.phd.scenario.vehicle.experiment.TrajectoryRecorder;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class TestTaskExecutor
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

    @BeforeAll
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
    }

    @AfterAll
    public static void tearDownVrep() throws VRepException 
    {
        _objectCreator.deleteAll();
        _vrep.simxFinish(_clientID);
    }

    @Test
    public void testExecuteOneDefinedAToBTask1()
    {
        try
        {
            RoadMap map = SimulationSetupConvenienceMethods.createMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            TaskCreator taskCreator = new TaskCreator();
            AllSameAToBDrivingTaskCreatorConfig config = new AllSameAToBDrivingTaskCreatorConfig(1, true);
            config.setMap(map);
            config.configRoutesOnMap(0.0, 0.0, 200.0, 200.0);
            config.configSimulator(_vrep, _clientID, _objectCreator);
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
    }
    
    @Test
    public void testExecute3DefinedAToBTasks()
    {
        try
        {
            RoadMapAndCenterMatrix mapAndCenterMatrix = 
                    SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            RoadMap map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            TaskCreator taskCreator = new TaskCreator();
            FixedSourceTargetContinuousRouteTaskCreatorConfig config = new FixedSourceTargetContinuousRouteTaskCreatorConfig(3);
            Position2D source = new Position2D(5769.00, 2983.0).transform(centerMatrix);
            Position2D target = new Position2D(5594.0, 4794.0).transform(centerMatrix);
            config.setSourceTarget(source, target , map);
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
    }
    
    @Test
    public void testExecuteOneDefinedAToBTask2()
    {
        try
        {
            RoadMapAndCenterMatrix mapAndCenterMatrix = 
                    SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            RoadMap map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
            
            Position2D startPoint = new Position2D(3971.66, 4968.91).transform(centerMatrix);
            Position2D endPoint = new Position2D(2998.93, 4829.77).transform(centerMatrix);
            
            config.setMap(map);
            config.configSimulator(_vrep, _clientID, _objectCreator);
            config.setTargetPoints(Arrays.asList(new Position2D[]{startPoint, endPoint}));
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
    }
    
    @Test
    public void testExecuteOneDefinedAToBTask3()
    {
        try
        {
            RoadMapAndCenterMatrix mapAndCenterMatrix = 
                    SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            RoadMap map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
            
            Position2D p3 = new Position2D(2998.93, 4829.77).transform(centerMatrix);
            Position2D p4 = new Position2D(3246.30, 2117.18).transform(centerMatrix);
      
            config.setMap(map);
            config.configSimulator(_vrep, _clientID, _objectCreator);

            config.setTargetPoints(Arrays.asList(new Position2D[]{p3, p4}));
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
    }

    @Test
    public void testExecute4PointRoute()
    {
        try
        {
            RoadMapAndCenterMatrix mapAndCenterMatrix = 
                    SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            RoadMap map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
            
            Position2D p1 = new Position2D(5841.15, 4890.38).transform(centerMatrix);
            Position2D p2 = new Position2D(3971.66, 4968.91).transform(centerMatrix);
            Position2D p3 = new Position2D(2998.93, 4829.77).transform(centerMatrix);
            Position2D p4 = new Position2D(3246.30, 2117.18).transform(centerMatrix);
            Position2D p5 = new Position2D(5647.77, 2749.04).transform(centerMatrix);

            config.setMap(map);
            config.configSimulator(_vrep, _clientID, _objectCreator);

            config.setTargetPoints(Arrays.asList(new Position2D[]{p1, p2, p3, p4, p5}));
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
    }

    @Test
    public void testExecute2PointRoute()
    {
        try
        {
            RoadMapAndCenterMatrix mapAndCenterMatrix = 
                    SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            RoadMap map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
            
            Position2D p2 = new Position2D(3122.84, 4937.96).transform(centerMatrix);
            Position2D p3 = new Position2D(2998.93, 4829.77).transform(centerMatrix);
            Position2D p4 = new Position2D(3246.30, 2117.18).transform(centerMatrix);
           
            config.setMap(map);
            config.configSimulator(_vrep, _clientID, _objectCreator);

            config.setTargetPoints(Arrays.asList(new Position2D[]{p2, p3, p4}));
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
    }
    
    @Test
    public void testMultiplePointsOnLargeMapNoDebug() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = 
                SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/luebeck-roads.net.xml");
        RoadMap map = mapAndCenterMatrix.getRoadMap();
        
        List<Position2D> targetPoints = Arrays.asList(new Position2D(4112.28,7084.47), new Position2D(6196.74,5289.38), new Position2D(10161.11,3555.67), new Position2D(3430.39,581.66), new Position2D(7252.29,1130.89));
        
        targetPoints = targetPoints.stream().map(point -> point.transform(mapAndCenterMatrix.getCenterMatrix())).collect(Collectors.toList());
        final Map<Long, List<TrajectoryElement>> configurations = new HashMap<>();
        final Map<Long, ObservationTuple> observations = new HashMap<>();

        TaskCreator taskCreator = new TaskCreator();
        PointListTaskCreatorConfig taskConfiguration = new PointListTaskCreatorConfig();
        int lookahead = 15;
        taskConfiguration.setControlParams(lookahead, 120, 4.0, 4.3, 1.0);
        taskConfiguration.setDebug(false);
        taskConfiguration.setMap(map);
        taskConfiguration.configSimulator(_vrep, _clientID, _objectCreator);
        TrajectoryRecorder trajectoryRecorder = new TrajectoryRecorder();
        taskConfiguration.addLowerLayerControl(trajectoryRecorder);
        taskConfiguration.setCarModel("./res/simcarmodel/vehicleVisualsBrakeScript.ttm");

        taskConfiguration.setControlLoopRate(120);
        taskConfiguration.setTargetPoints(targetPoints);
        taskConfiguration.addNavigationListener(trajectoryRecorder);
        taskConfiguration.setLowerLayerController(new ILowerLayerFactory() {
            @Override
            public ILowerLayerControl create()
            {
                PurePursuitControllerVariableLookahead purePursuitControllerVariableLookahead = new PurePursuitControllerVariableLookahead();
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
    }
    
    @Test
    public void testSharpTurn()
    {
        try
        {
            RoadMapAndCenterMatrix mapAndCenterMatrix = 
                    SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            RoadMap map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
            
            Position2D p2 = new Position2D(5272.78,2468.75).transform(centerMatrix);
            Position2D p3 = new Position2D(5557.04,2438.00).transform(centerMatrix);
            //Position2D p4 = new Position2D(5281.36,2829.22).transform(centerMatrix);
           
            config.setMap(map);
            config.configSimulator(_vrep, _clientID, _objectCreator);
            config.setControlParams(15.0, 120.0, 3.8, 4.0, 1.0);
            config.setDebug(true);
            config.setControlLoopRate(120);
            config.setCarModel("./res/simcarmodel/vehicleVisuals.ttm");
            config.setLowerLayerController(new ILowerLayerFactory()
            {
                @Override
                public ILowerLayerControl create() 
                {
                    PurePursuitControllerVariableLookahead purePursuitControllerVariableLookahead = new PurePursuitControllerVariableLookahead();
                    return purePursuitControllerVariableLookahead;
                }
            });
            config.setTargetPoints(Arrays.asList(new Position2D[] {p2, p3}));
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
    }

}
