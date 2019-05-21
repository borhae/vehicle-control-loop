package de.joachim.haensel.phd.scenario.parcourgenerator;

import java.util.ArrayList;
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
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.Segment;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.ArcSegmentDecomposition;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.mapgenerator.MapGenerator;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable.PurePursuitControllerVariableLookahead;
import de.joachim.haensel.phd.scenario.vehicle.experiment.TrajectoryRecorder;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class TestParcourGeneration
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;
    
    
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
    
    @Test
    public void testSimple1CurveParcour() throws VRepException
    {
        List<IArcsSegmentContainerElement> sourceFromProfile = new ArrayList<>();
        sourceFromProfile.add(new Segment(new Line2D(0.0, 0.0, 100.0, 0.0)));
        sourceFromProfile.add(new Segment(new Line2D(100.0, 0.0, 100.0, 100.0)));
        MapGenerator generator = new MapGenerator();
        RoadMap map = generator.generateMap(sourceFromProfile);
        float streetWidth = (float)1.5;
        float streetHeight = (float)0.4;
        VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(map);
        mapCreator.createMapSizedRectangle(map, false);
    }
    
    @Test
    public void testRecreationFromDriven() throws VRepException 
    {
        TaskCreator taskCreator = new TaskCreator();
        PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
        double lookahead = 10.0;
        config.setControlParams(lookahead, 120, 4.0, 4.3, 0.8);
        RoadMapAndCenterMatrix mapAndCenterMatrix;
        mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        RoadMap map = mapAndCenterMatrix.getRoadMap();
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();

        List<Position2D> targetPoints = Arrays.asList(new Position2D(3653.19, 4666.35), new Position2D(3845.60, 4744.58));
        targetPoints = targetPoints.stream().map(point -> point.transform(centerMatrix)).collect(Collectors.toList());

        config.setMap(map);
        config.configSimulator(_vrep, _clientID, _objectCreator);

        TrajectoryRecorder trajectoryRecorder = new TrajectoryRecorder();
        config.addLowerLayerControl(trajectoryRecorder);

        config.setTargetPoints(targetPoints);
        config.addNavigationListener(trajectoryRecorder);
        config.setLowerLayerController(() -> new PurePursuitControllerVariableLookahead());
        taskCreator.configure(config);
        List<ITask> tasks = taskCreator.createTasks();

        TaskExecutor executor = new TaskExecutor();
        executor.execute(tasks);
        
        _objectCreator.deleteAll();
        
        List<Position2D> plannedTrajectory = trajectoryRecorder.getPlannedTrajectory();
        ArcSegmentDecomposition decomposer = new ArcSegmentDecomposition();
        List<IArcsSegmentContainerElement> decomposition = decomposer.decompose(plannedTrajectory);
        
        MapGenerator mapGenerator = new MapGenerator();
        RoadMap testRunMap = mapGenerator.generateMap(decomposition);
        VRepMap mapCreator = new VRepMap(1.5f, 0.4f, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(testRunMap);
        mapCreator.createMapSizedRectangle(testRunMap, false);
    }
}
