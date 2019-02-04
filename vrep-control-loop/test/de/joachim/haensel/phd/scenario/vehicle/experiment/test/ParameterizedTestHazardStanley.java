package de.joachim.haensel.phd.scenario.vehicle.experiment.test;


import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

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
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitParameters;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.StanleyController;
import de.joachim.haensel.phd.scenario.vehicle.experiment.TireBlowOutAtPositionEventGenerator;
import de.joachim.haensel.phd.scenario.vehicle.experiment.TrajectoryRecorder;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class ParameterizedTestHazardStanley
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;
    
    public static Stream<Arguments> parameters()
    {
        return Arrays.asList(new Object[][]
        {
//            {20, 120, 6.0, 8.0, 5.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {20, 120, 6.0, 8.0, 5.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
            {20, 60, 2.0, 8.0, 5.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
            {20, 60, 2.0, 8.0, 5.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
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
    
    @AfterEach
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
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
    
    @Test
    public void testTireBlowOutScenario(double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration, List<Position2D> targetPoints, Position2D eventPosition, boolean[] tiresToBlow, String paramID, String color)
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix;
        RoadMap _map = null;
        Position2D _eventPosition = null;
        List<Position2D> _targetPoints = null;
        boolean[] _tiresToBlow = null;
        try
        {
            mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            _map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            _targetPoints = targetPoints.stream().map(point -> point.transform(centerMatrix)).collect(Collectors.toList());
            if(eventPosition != null)
            {
                _eventPosition = eventPosition.transform(centerMatrix);
                _tiresToBlow = tiresToBlow;
            }
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
        try
        {
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
            config.setControlParams(lookahead, maxVelocity, maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration);

            config.setMap(_map);
            config.configSimulator(_vrep, _clientID, _objectCreator);
            if(_eventPosition != null)
            {
                config.addLowerLayerControl(new TireBlowOutAtPositionEventGenerator(_eventPosition, 10.0, _tiresToBlow, 0.5f));
            }
            TrajectoryRecorder trajectoryRecorder = new TrajectoryRecorder();
            config.addLowerLayerControl(trajectoryRecorder);

            config.setTargetPoints(_targetPoints);
            config.addNavigationListener(trajectoryRecorder);
            config.setLowerLayerController(new ILowerLayerFactory() {
                
                @Override
                public ILowerLayerControl create()
                {
                    StanleyController controller = new StanleyController();
                    controller.setParameters(new PurePursuitParameters(lookahead, 0.0));
                    
                    return controller;
                }
            });
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
            System.out.println("bla");
            
            List<String> actualTrajectory = trajectoryRecorder.getTrajectory().stream().map(te -> te.getPos()).filter(pos -> pos.getX() != 0.0 && pos.getY() != 0.0).map(pos -> pos.toPyPlotString(color)).collect(Collectors.toList());
            List<String> plannedTrajectory = trajectoryRecorder.getPlannedTrajectory().stream().map(pos -> pos.toPyPlotString("black")).collect(Collectors.toList());
            try
            {
                Files.write(new File("./res/hazardtestoutput/trajectory" + paramID + ".actual.pyplot").toPath(), actualTrajectory, Charset.defaultCharset());
                Files.write(new File("./res/hazardtestoutput/trajectory" + paramID + ".planned.pyplot").toPath(), plannedTrajectory, Charset.defaultCharset());
            }
            catch (IOException exc)
            {
                exc.printStackTrace();
            }
        }
        catch (VRepException exc)
        {
            fail(exc.toString());
        }
    }
}
