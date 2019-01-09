package de.joachim.haensel.phd.scenario.vehicle.experiment.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
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

@RunWith(Parameterized.class)
public class ParameterizedTestCollectInVariousMaps
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
    private String _color;
    private String _mapFileName;
    
    @Parameterized.Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]
        {
            {15, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(6656.36,3627.95), new Position2D(440.32,627.73)), "luebeck-roads.net.xml", "blue"},
            {15, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(10669.82,11374.52), new Position2D(6562.38,3497.54)), "chandigarh-roads.net.xml", "blue"},
        });
    }

    public ParameterizedTestCollectInVariousMaps(double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration, List<Position2D> targetPoints, String mapFilenName, String color)
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
    }
    
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
    
    
    @Test
    public void testDriveMapsScenario()
    {
        try
        {
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
            config.setControlParams(_lookahead, _maxVelocity, _maxLongitudinalAcceleration, _maxLongitudinalDecceleration, _maxLateralAcceleration);

            config.setMap(_map);
            config.configSimulator(_vrep, _clientID, _objectCreator);
            TrajectoryRecorder trajectoryRecorder = new TrajectoryRecorder();
            config.addLowerLayerControl(trajectoryRecorder);
            config.setLowerLayerController(new ILowerLayerFactory() {
                
                @Override
                public ILowerLayerControl create()
                {
                    PurePursuitControllerVariableLookahead purePursuitControllerVariableLookahead = new PurePursuitControllerVariableLookahead();
                    purePursuitControllerVariableLookahead.setParameters(new PurePursuitParameters(_lookahead, 0.0));
                    return purePursuitControllerVariableLookahead;
                }
            });

            config.setTargetPoints(_targetPoints);
            config.addNavigationListener(trajectoryRecorder);
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
            System.out.println("bla");
            
            List<String> actualTrajectory = trajectoryRecorder.getTrajectory().stream().filter(p -> p.getX() != 0 && p.getY() != 0.0).map(pos -> pos.toPyPlotString(_color)).collect(Collectors.toList());
            List<Position2D> plannedTrajectoryPre = trajectoryRecorder.getPlannedTrajectory();
            List<Line2D> lines = new ArrayList<>();
            for(int idx = 0; idx < plannedTrajectoryPre.size() - 1; idx++)
            {
                Position2D p1 = plannedTrajectoryPre.get(idx);
                Position2D p2 = plannedTrajectoryPre.get(idx + 1);
                lines.add(new Line2D(p1, p2));
            }
            List<Vector2D> leftBorder = lines.stream().map(line -> new Vector2D(line).shift(-1.6)).collect(Collectors.toList());
            List<Vector2D> rightBorder = lines.stream().map(line -> new Vector2D(line).shift(1.6)).collect(Collectors.toList());
            List<String> plannedTrajectoryLeft = leftBorder.stream().map(vector -> vector.getBase().toPyPlotString("black", "line") + "\n" + vector.getTip().toPyPlotString("black", "line")).collect(Collectors.toList());
            List<String> plannedTrajectoryRight = rightBorder.stream().map(vector -> vector.getBase().toPyPlotString("black", "line") + "\n" + vector.getTip().toPyPlotString("black", "line")).collect(Collectors.toList());
            try
            {
                Files.write(new File("./res/hazardtestoutput/trajectory" + _mapFileName + ".actual.pyplot").toPath(), actualTrajectory, Charset.defaultCharset());
                Files.write(new File("./res/hazardtestoutput/trajectory" + _mapFileName + ".plannedleft.pyplot").toPath(), plannedTrajectoryLeft, Charset.defaultCharset());
                Files.write(new File("./res/hazardtestoutput/trajectory" + _mapFileName + ".plannedright.pyplot").toPath(), plannedTrajectoryRight, Charset.defaultCharset());

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
