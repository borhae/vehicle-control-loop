package de.joachim.haensel.phd.scenario.vehicle.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitControllerVariableLookahead;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitParameters;
import de.joachim.haensel.phd.scenario.vehicle.experiment.TrajectoryRecorder;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class DriveOptimizationTest
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

    private static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";
    
    public static Stream<Arguments> parameters()
    {
        return Arrays.asList(new Object[][]
        {
            {15.0, 120.0, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(8226.83,3530.29), new Position2D(8477.42,3220.79)), "chandigarh-roads.net.xml", "blue"},
        }).stream().map(params -> Arguments.of(params));
    }

    @BeforeAll
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
    }

    @AfterAll
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

    @AfterEach
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
    }


    @ParameterizedTest
    @MethodSource("parameters")
    public void testSharpCurve(double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration, List<Position2D> targetPoints, String mapFilenName, String color) throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix;
        RoadMap _map = null;
        try
        {
            mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, RES_ROADNETWORKS_DIRECTORY + mapFilenName);
            _map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            targetPoints = targetPoints.stream().map(point -> point.transform(centerMatrix)).collect(Collectors.toList());
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
        TaskCreator taskCreator = new TaskCreator();
        PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
        config.setControlParams(lookahead, maxVelocity, maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration);

        config.setMap(_map);
        config.setCarModel("./res/simcarmodel/vehicleVisualsBrakeScript.ttm");
        config.configSimulator(_vrep, _clientID, _objectCreator);
        TrajectoryRecorder trajectoryRecorder = new TrajectoryRecorder();
        config.addLowerLayerControl(trajectoryRecorder);

        config.setTargetPoints(targetPoints);
        config.addNavigationListener(trajectoryRecorder);
        config.setLowerLayerController(new ILowerLayerFactory() {

            @Override
            public ILowerLayerControl create()
            {
                PurePursuitControllerVariableLookahead purePursuitControllerVariableLookahead = new PurePursuitControllerVariableLookahead();
                purePursuitControllerVariableLookahead.setParameters(new PurePursuitParameters(lookahead, 0.0));
                return purePursuitControllerVariableLookahead;
            }
        });
        taskCreator.configure(config);
        List<ITask> tasks = taskCreator.createTasks();

        TaskExecutor executor = new TaskExecutor();
        executor.execute(tasks);
        System.out.println("done");
    }
}
