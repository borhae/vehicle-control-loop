package de.joachim.haensel.phd.scenario.experiment;

import java.util.Arrays;
import java.util.List;
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
import de.joachim.haensel.phd.scenario.debug.INavigationListener;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable.PurePursuitControllerVariableLookahead;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class ErrounousToursTest 
{
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
		RoadMapAndCenterMatrix mapAndCenterMatrix = null;
		RoadMap map = null;
		
		List<Position2D> targetPointsMapped = null;
		String mapFilenName = "chandigarh-roads-lefthand.net.xml";
		List<Position2D> targetPoints = Arrays.asList(new Position2D(6439.75, 8727.41), 
				new Position2D(11519.71, 3760.38));
		try
		{
			mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator,
					RES_ROADNETWORKS_DIRECTORY + mapFilenName);
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
		}
		TaskCreator taskCreator = new TaskCreator();
		PointListTaskCreatorConfig taskConfiguration = new PointListTaskCreatorConfig();
		taskConfiguration.setControlParams(15.0, 120.0, 3.8, 4.0, 1.0);
		taskConfiguration.setDebug(true);
		taskConfiguration.setMap(map);
		taskConfiguration.configSimulator(_vrep, _clientID, _objectCreator);
		taskConfiguration.setCarModel("./res/simcarmodel/vehicleVisuals.ttm");

		taskConfiguration.setTargetPoints(targetPointsMapped);
		taskConfiguration.addNavigationListener(new INavigationListener()
		{
			@Override
			public void notifySegmentsChanged(List<TrajectoryElement> segments) {
			}

			@Override
			public void notifyRouteChanged(List<Line2D> route) {
			}

			@Override
			public void activateSegmentDebugging() {
			}

			@Override
			public void activateRouteDebugging() {
			}
		});
		taskConfiguration.setLowerLayerController(() -> new PurePursuitControllerVariableLookahead());
		taskCreator.configure(taskConfiguration);
		List<ITask> tasks = taskCreator.createTasks();

		TaskExecutor executor = new TaskExecutor();
		executor.execute(tasks);
		System.out.println("tada!");
	}
}
