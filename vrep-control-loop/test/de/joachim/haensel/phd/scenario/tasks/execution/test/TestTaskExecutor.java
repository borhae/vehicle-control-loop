package de.joachim.haensel.phd.scenario.tasks.execution.test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.tasks.creation.AllSameTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.FixedSourceTargetContinuousRouteTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.Task;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class TestTaskExecutor
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

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
            AllSameTaskCreatorConfig config = new AllSameTaskCreatorConfig(1);
            config.setAllSameOnMap(0.0, 0.0, 200.0, 200.0, map);
            taskCreator.configure(config);
            List<Task> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor(_clientID, _vrep, _objectCreator);
            executor.setMap(map);
            executor.execute(tasks);
        }
        catch (VRepException exc)
        {
            fail(exc.toString());
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
            List<Task> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor(_clientID, _vrep, _objectCreator);
            executor.setMap(map);
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
            fail(exc.toString());
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
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(3);
            
//            Position2D p1 = new Position2D(5841.15, 4890.38).transform(centerMatrix);
            Position2D p2 = new Position2D(3971.66, 4968.91).transform(centerMatrix);
            Position2D p3 = new Position2D(2998.93, 4829.77).transform(centerMatrix);
            Position2D p4 = new Position2D(3246.30, 2117.18).transform(centerMatrix);
            Position2D p5 = new Position2D(5647.77, 2749.04).transform(centerMatrix);
            
//            config.setTargetPoints(Arrays.asList(new Position2D[]{p1, p2, p3, p4, p5}));
            config.setTargetPoints(Arrays.asList(new Position2D[]{p2, p3, p4, p5}));
            taskCreator.configure(config);
            List<Task> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor(_clientID, _vrep, _objectCreator);
            executor.setMap(map);
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
            fail(exc.toString());
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
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(4);
            
            Position2D p1 = new Position2D(5841.15, 4890.38).transform(centerMatrix);
            Position2D p2 = new Position2D(3971.66, 4968.91).transform(centerMatrix);
            Position2D p3 = new Position2D(2998.93, 4829.77).transform(centerMatrix);
            Position2D p4 = new Position2D(3246.30, 2117.18).transform(centerMatrix);
            Position2D p5 = new Position2D(5647.77, 2749.04).transform(centerMatrix);
            
            config.setTargetPoints(Arrays.asList(new Position2D[]{p1, p2, p3, p4, p5}));
            taskCreator.configure(config);
            List<Task> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor(_clientID, _vrep, _objectCreator);
            executor.setMap(map);
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
            fail(exc.toString());
        }
    }
}
