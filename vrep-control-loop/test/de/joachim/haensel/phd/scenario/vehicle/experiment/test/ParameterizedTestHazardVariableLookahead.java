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
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitControllerVariableLookahead;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitParameters;
import de.joachim.haensel.phd.scenario.vehicle.experiment.TireBlowOutAtPositionEventGenerator;
import de.joachim.haensel.phd.scenario.vehicle.experiment.TrajectoryRecorder;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

@RunWith(Parameterized.class)
public class ParameterizedTestHazardVariableLookahead
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;
    
    private double _lookahead;
    private double _maxVelocity;
    private double _maxLongitudinalAcceleration;
    private double _maxLongitudinalDecceleration;
    private double _maxLateralAcceleration;
    private List<Position2D> _targetPoints;
    private Position2D _eventPosition;
    private RoadMap _map;
    private String _paramID;
    private String _color;
    private boolean[] _tiresToBlow;
    
    @Parameterized.Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]
        {
//            {20, 120, 6.0, 8.0, 5.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {20, 120, 6.0, 8.0, 5.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// The tire blow out on outer tire goes a little further out with this setting. But both runs carry the car far out of the allowed area
//            {20, 120, 2.0, 8.0, 5.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {20, 120, 2.0, 8.0, 5.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Totally equal, cutting too much, way too far inside
//            {20, 120, 4.0, 3.0, 5.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {20, 120, 4.0, 3.0, 5.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Both cutting the curve a bit, both overshoot, blowout overshoots more
//            {20, 120, 4.0, 6.0, 2.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {20, 120, 4.0, 6.0, 2.0, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Both cutting the curve, normal operation more, both overshooting slightly, tire-blowout more
//            {20, 120, 4.0, 6.0, 1.5, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {20, 120, 4.0, 6.0, 1.5, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Almost no curve cutting, heavily overshooting both getting back to the road. Tire-Blowout overshoots significantly more
//            {15, 120, 4.0, 6.0, 1.5, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {15, 120, 4.0, 6.0, 1.5, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Almost ideal with blown out tire, undercut with all wheels
//            {15, 120, 4.0, 4.0, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {15, 120, 4.0, 4.0, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Overshoot
//            {15, 120, 4.0, 5.0, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {15, 120, 4.0, 5.0, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Almost 
//          {15, 120, 4.0, 4.5, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//          {15, 120, 4.0, 4.5, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},


// A little more decceleration and this is awesome: blow out tire overshoots, all in-tact results in slight undercut and minimal overshoot
//            {15, 120, 4.0, 4.3, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {15, 120, 4.0, 4.3, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Let's try a different speed: kind of the same, doesn't accelerate much more on the distance
//            {15, 240, 4.0, 4.3, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {15, 240, 4.0, 4.3, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Let's go slower: Both heavily undercut
//            {15, 50, 4.0, 4.3, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {15, 50, 4.0, 4.3, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Minimal lookahead: works perfect for both
//            {10, 50, 4.0, 4.3, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {10, 50, 4.0, 4.3, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Minimal lookahead and fast: works good for intact wheels and blow out overshoots: nice!            
//            {10, 120, 4.0, 4.3, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {10, 120, 4.0, 4.3, 1.2, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Let's go for a different route and turn down the lateral velocity
//            {10, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(4874.12, 2690.66), new Position2D(5213.40,2791.03), new Position2D(5370.22,2858.77)), new Position2D(5191.27, 2793.11), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {10, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(4874.12, 2690.66), new Position2D(5213.40,2791.03), new Position2D(5370.22,2858.77)), null, new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Let's blow the other front tire            
//            {10, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(4874.12, 2690.66), new Position2D(5213.40,2791.03), new Position2D(5370.22,2858.77)), new Position2D(5191.27, 2793.11), new boolean[]{false, true, false, false}, "test_20_120_6.0_8.0_5.0", "red"},
//            {10, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(4874.12, 2690.66), new Position2D(5213.40,2791.03), new Position2D(5370.22,2858.77)), null, new boolean[]{false, true, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
// Low Lateral velocity with old route and all tires blown, one after the other           
            {10, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{true, false, false, false}, "test_20_120_6.0_8.0_5.0_frontleft", "red"},
            {10, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{false, true, false, false}, "test_20_120_6.0_8.0_5.0_frontright", "orange"},
            {10, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{false, false, true, false}, "test_20_120_6.0_8.0_5.0_rearright", "magenta"},
            {10, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), new Position2D(3861.07, 4705.83), new boolean[]{false, false, false, true}, "test_20_120_6.0_8.0_5.0_reartleft", "darkviolet"},
            {10, 120, 4.0, 4.3, 0.8, Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58)), null, new boolean[]{false, true, false, false}, "test_20_120_6.0_8.0_5.0_noBlowout", "blue"},
        });
    }

    public ParameterizedTestHazardVariableLookahead(double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration, List<Position2D> targetPoints, Position2D eventPosition, boolean[] tiresToBlow, String paramID, String color)
    {
        _lookahead = lookahead;
        _maxVelocity = maxVelocity;
        _maxLongitudinalAcceleration = maxLongitudinalAcceleration;
        _maxLongitudinalDecceleration = maxLongitudinalDecceleration;
        _maxLateralAcceleration = maxLateralAcceleration;
        RoadMapAndCenterMatrix mapAndCenterMatrix;
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
            fail();
        }
        _paramID = paramID;
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
    public void testTireBlowOutScenario() throws VRepException
    {
        TaskCreator taskCreator = new TaskCreator();
        PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
        config.setControlParams(_lookahead, _maxVelocity, _maxLongitudinalAcceleration, _maxLongitudinalDecceleration, _maxLateralAcceleration);

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
                PurePursuitControllerVariableLookahead purePursuitControllerVariableLookahead = new PurePursuitControllerVariableLookahead();
                purePursuitControllerVariableLookahead.setParameters(new PurePursuitParameters(_lookahead, 0.0));
                return purePursuitControllerVariableLookahead;
            }
        });
        taskCreator.configure(config);
        List<ITask> tasks = taskCreator.createTasks();

        TaskExecutor executor = new TaskExecutor();
        executor.execute(tasks);
        
        List<String> actualTrajectory = trajectoryRecorder.getTrajectory().stream().map(te -> te.getPos()).filter(p -> p.getX() != 0 && p.getY() != 0.0).map(pos -> pos.toPyPlotString(_color, "point")).collect(Collectors.toList());
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
            Files.write(new File("./res/hazardtestoutput/trajectory" + _paramID + ".actual.pyplot").toPath(), actualTrajectory, Charset.defaultCharset());
            Files.write(new File("./res/hazardtestoutput/trajectory" + _paramID + ".plannedleft.pyplot").toPath(), plannedTrajectoryLeft, Charset.defaultCharset());
            Files.write(new File("./res/hazardtestoutput/trajectory" + _paramID + ".plannedright.pyplot").toPath(), plannedTrajectoryRight, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
