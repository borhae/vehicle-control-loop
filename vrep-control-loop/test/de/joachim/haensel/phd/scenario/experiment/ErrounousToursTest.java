package de.joachim.haensel.phd.scenario.experiment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener.IIDCreator;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable.PurePursuitControllerVariableLookahead;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class ErrounousToursTest 
{
	private static final int ROUTE_ALLTIME_MIN_IDX = 0;

    public class RouteIDCreator implements IIDCreator
    {
	    private Integer _counter = Integer.valueOf(0);

        public RouteIDCreator(int routeStartIdx)
        {
            _counter = Integer.valueOf(routeStartIdx);
        }
        
        public synchronized String getNextStringID()
        {
            Integer next = Integer.valueOf(_counter.intValue() + 1);
            _counter = next;
            return _counter.toString();
        }
    }

    private static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";

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
		waitForRunningSimulationToStop();
		_objectCreator.removeScriptloader();
		_vrep.simxFinish(_clientID);
	}

	private static void waitForRunningSimulationToStop() throws VRepException 
	{
		IntWA simStatus = new IntWA(1);
		_vrep.simxCallScriptFunction(_clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript,
				"simulationState", null, null, null, null, simStatus, null, null, null, remoteApi.simx_opmode_blocking);
		while (simStatus.getArray()[0] != remoteApi.sim_simulation_stopped) 
		{
			try
			{
				Thread.sleep(500);
			} 
			catch (InterruptedException exc)
			{
				exc.printStackTrace();
			}
			_vrep.simxCallScriptFunction(_clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript,
					"simulationState", null, null, null, null, simStatus, null, null, null,
					remoteApi.simx_opmode_blocking);
		}
	}

	@AfterEach
	public void cleanUpObjects() throws VRepException
	{
		_objectCreator.deleteAll();
	}

	@Test
	public void testChandigarhRoute() throws VRepException 
	{
        RoadMap map = null;
        
        List<Position2D> targetPositionsMapped = null;
        String mapFilenName = "chandigarh-roads-lefthand.removed.net.xml";
        List<String> pointsAsString = new ArrayList<String>();
        try
        {
            pointsAsString = Files.readAllLines(new File(RES_ROADNETWORKS_DIRECTORY + "Chandigarhpoints_spread.txt").toPath());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        List<Position2D> allPositions = pointsAsString.stream().map(string -> new Position2D(string)).collect(Collectors.toList());
        int routeAllTimeMaxIdx = allPositions.size();
        
        int minIdx = 116;
        int maxIdx = 120;
        if(minIdx >= maxIdx)
        {
            System.out.println("min idx larger or equal to max idx. returning.");
            return;
        }
        
        int routeStartIdx = Math.max(minIdx, ROUTE_ALLTIME_MIN_IDX);
        int routeEndIdx = Math.min(maxIdx, routeAllTimeMaxIdx);
        List<Position2D> positions = allPositions.subList(routeStartIdx, routeEndIdx);

        RoadMapAndCenterMatrix mapAndCenterMatrix = 
                SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, RES_ROADNETWORKS_DIRECTORY + mapFilenName);
        map = mapAndCenterMatrix.getRoadMap();
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        System.out.println("Raw targets:");
        System.out.println(positions.stream().map(point -> point.toString()).collect(Collectors.joining(", ")));
        targetPositionsMapped = positions.stream().map(point -> point.transform(centerMatrix)).collect(Collectors.toList());
        System.out.println("Mapped targets:");
        System.out.println(targetPositionsMapped.stream().map(point -> point.toString()).collect(Collectors.joining(", ")));
        TaskCreator taskCreator = new TaskCreator();
        PointListTaskCreatorConfig taskConfiguration = new PointListTaskCreatorConfig();
        taskConfiguration.setControlParams(15.0, 120.0, 3.8, 4.0, 1.0);
        taskConfiguration.setControlLoopRate(120);
        taskConfiguration.setDebug(true);
        taskConfiguration.setMap(map);
        taskConfiguration.configSimulator(_vrep, _clientID, _objectCreator);
        taskConfiguration.setCarModel("./res/simcarmodel/vehicleVisuals.ttm");

        taskConfiguration.setTargetPoints(targetPositionsMapped);
        taskConfiguration.setLowerLayerController(() -> new PurePursuitControllerVariableLookahead());
        IIDCreator routeIdCreator = new RouteIDCreator(routeStartIdx);
        VRepNavigationListener routesEndsMarker = new VRepNavigationListener(_objectCreator, routeIdCreator);
        routesEndsMarker.activateRouteEndsDebugging();
        taskConfiguration.addNavigationListener(routesEndsMarker);
        taskCreator.configure(taskConfiguration);
        List<ITask> tasks = taskCreator.createTasks();

        TaskExecutor executor = new TaskExecutor();
        executor.execute(tasks);

        System.out.println("enter arbitrary stuff an then press enter");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        System.out.println(input);
        scanner.close();
	}

	@Test
    public void testLuebeckRoute() throws VRepException 
    {
        RoadMap map = null;
        
        List<Position2D> targetPositionsMapped = null;
        String mapFilenName = "luebeck-roads.net.xml";
        List<String> pointsAsString = new ArrayList<String>();
        try
        {
//            pointsAsString = Files.readAllLines(new File(RES_ROADNETWORKS_DIRECTORY + "Luebeckpoints_spread.txt").toPath());
            pointsAsString = Files.readAllLines(new File(RES_ROADNETWORKS_DIRECTORY + "Luebeckpoints_generatedSeed4096.txt").toPath());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        List<Position2D> allPositions = pointsAsString.stream().map(string -> new Position2D(string)).collect(Collectors.toList());
        int routeAllTimeMaxIdx = allPositions.size();
        // 83 - 84 has an issue
        int minIdx = 81;
        int maxIdx = 85;
        if(minIdx >= maxIdx)
        {
            System.out.println("min idx larger or equal to max idx. returning.");
            return;
        }
        
        int routeStartIdx = Math.max(minIdx, ROUTE_ALLTIME_MIN_IDX);
        int routeEndIdx = Math.min(maxIdx, routeAllTimeMaxIdx);
        List<Position2D> positions = allPositions.subList(routeStartIdx, routeEndIdx);

        RoadMapAndCenterMatrix mapAndCenterMatrix = 
                SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, RES_ROADNETWORKS_DIRECTORY + mapFilenName);
        map = mapAndCenterMatrix.getRoadMap();
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        System.out.println("Raw targets:");
        System.out.println(positions.stream().map(point -> point.toString()).collect(Collectors.joining(", ")));
        targetPositionsMapped = positions.stream().map(point -> point.transform(centerMatrix)).collect(Collectors.toList());
        System.out.println("Mapped targets:");
        System.out.println(targetPositionsMapped.stream().map(point -> point.toString()).collect(Collectors.joining(", ")));
        TaskCreator taskCreator = new TaskCreator();
        PointListTaskCreatorConfig taskConfiguration = new PointListTaskCreatorConfig();
        taskConfiguration.setControlParams(15.0, 120.0, 3.8, 4.0, 1.0);
        taskConfiguration.setControlLoopRate(120);
        taskConfiguration.setDebug(true);
        taskConfiguration.setMap(map);
        taskConfiguration.configSimulator(_vrep, _clientID, _objectCreator);
        taskConfiguration.setCarModel("./res/simcarmodel/vehicleVisuals.ttm");

        taskConfiguration.setTargetPoints(targetPositionsMapped);
        taskConfiguration.setLowerLayerController(() -> new PurePursuitControllerVariableLookahead());
        IIDCreator routeIdCreator = new RouteIDCreator(routeStartIdx);
        VRepNavigationListener routesEndsMarker = new VRepNavigationListener(_objectCreator, routeIdCreator);
        routesEndsMarker.activateRouteEndsDebugging();
        taskConfiguration.addNavigationListener(routesEndsMarker);
        taskCreator.configure(taskConfiguration);
        List<ITask> tasks = taskCreator.createTasks();

        TaskExecutor executor = new TaskExecutor();
        executor.execute(tasks);

        System.out.println("enter arbitrary stuff an then press enter");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        System.out.println(input);
        scanner.close();
    }
	
	@Test
    public void testLuebeckRoutePart() throws VRepException 
    {
        RoadMap map = null;
        
        String mapFilenName = "luebeck-roads.net.xml";

        RoadMapAndCenterMatrix mapAndCenterMatrix = 
                SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, RES_ROADNETWORKS_DIRECTORY + mapFilenName);
        map = mapAndCenterMatrix.getRoadMap();
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        Position2D startPos = new Position2D(3066.47, 6228.96).transform(centerMatrix);
        Position2D endPos = new Position2D(3059.49, 6117.85).transform(centerMatrix);

        TaskCreator taskCreator = new TaskCreator();
        PointListTaskCreatorConfig taskConfiguration = new PointListTaskCreatorConfig();
        taskConfiguration.setControlParams(15.0, 120.0, 3.8, 4.0, 1.0);
        taskConfiguration.setControlLoopRate(120);
        taskConfiguration.setDebug(true);
        taskConfiguration.setMap(map);
        taskConfiguration.configSimulator(_vrep, _clientID, _objectCreator);
        taskConfiguration.setCarModel("./res/simcarmodel/vehicleVisuals.ttm");

        taskConfiguration.setTargetPoints(Arrays.asList(new Position2D[] {startPos, endPos}));
        taskConfiguration.setLowerLayerController(() -> new PurePursuitControllerVariableLookahead());
        IIDCreator routeIdCreator = new RouteIDCreator(1);
        VRepNavigationListener routesEndsMarker = new VRepNavigationListener(_objectCreator, routeIdCreator);
        routesEndsMarker.activateRouteEndsDebugging();
        taskConfiguration.addNavigationListener(routesEndsMarker);
        taskCreator.configure(taskConfiguration);
        List<ITask> tasks = taskCreator.createTasks();

        TaskExecutor executor = new TaskExecutor();
        executor.execute(tasks);

        System.out.println("enter arbitrary stuff an then press enter");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        System.out.println(input);
        scanner.close();
    }
}
